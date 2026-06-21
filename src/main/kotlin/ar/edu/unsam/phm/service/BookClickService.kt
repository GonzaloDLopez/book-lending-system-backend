package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookClick
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.dto.BookRedisDto
import ar.edu.unsam.phm.dto.BookingDto
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.dto.ConversionStatsDto
import ar.edu.unsam.phm.dto.hasActiveFilters
import ar.edu.unsam.phm.dto.passesFilters
import ar.edu.unsam.phm.dto.toBookingDto
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.mongo.BookClickRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.repository.redis.BookRankingRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled

@Service
class BookClickService(
    private val bookClickRepository:   BookClickRepository,
    private val bookRepository:        BookRepository,
    private val bookingRepository:     BookingRepository,
    private val bookRankingRepository: BookRankingRepository,
) {
    private val log = LoggerFactory.getLogger(BookClickService::class.java)

    fun registerClick(bookId: String, username: String) {
        if (!bookRepository.existsById(bookId)) return
        bookClickRepository.save(BookClick(bookId = bookId, username = username))
        try {
            bookRankingRepository.incrementClick(bookId)
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible al registrar click de $bookId: ${e.message}")
        }
    }

    private fun getTop10BookIds(): List<String> = bookRankingRepository.getTop10BookIds()

    private fun getBookDto(bookId: String): BookRedisDto? = bookRankingRepository.getBookDto(bookId)

    fun updateBookCaches(books: List<Book>, owners: Map<Long, User>) {
        books.forEach { book ->
            val owner        = owners[book.ownerId] ?: return@forEach
            val bookingCount = bookingRepository.countByBookIdAndCancelledFalse(book.id!!)
            try {
                bookRankingRepository.updateBookCache(book, bookingCount, owner)
            } catch (e: DataAccessException) {
                log.warn("[REDIS] no disponible para cache de libro ${book.id}: ${e.message}")
            }
        }
    }

    fun getClickStatsByOwner(ownerId: Long): List<BookClickCountDto> {
        val bookIds = bookRepository.findAllByOwnerIdAndActiveTrueAndDeletedFalse(ownerId).mapNotNull { it.id }
        if (bookIds.isEmpty()) return emptyList()
        return bookClickRepository.countClicksByBookIds(bookIds)
    }

    fun getKpiTasaConversion(): List<ConversionStatsDto> {
        ensureRankingLoaded()
        return bookRankingRepository.getTop5WithScores()
            .map { (bookId, clicks) -> buildConversionStats(bookId, clicks) }
    }

    private fun buildConversionStats(bookId: String, clicks: Double): ConversionStatsDto {
        val title    = resolveTitle(bookId)
        val reservas = bookingRepository.countByBookIdAndCancelledFalse(bookId)
        return ConversionStatsDto(
            bookId         = bookId,
            title          = title,
            clicks         = clicks.toInt(),
            reservas       = reservas,
            tasaConversion = tasaDeConversion(clicks, reservas),
        )
    }

    private fun tasaDeConversion(clicks: Double, reservas: Int): Double =
        if (clicks > 0.0) reservas.toDouble() / clicks else 0.0

    private fun resolveTitle(bookId: String): String =
        getBookDto(bookId)?.title
            ?: bookRepository.findById(bookId).map { it.title }.orElse(bookId)

    private fun ensureRankingLoaded() {
        try {
            if (bookRankingRepository.isRankingInitialized()) return
            val stats = bookClickRepository.findAllByClickCount()
            bookRankingRepository.rebuildRanking(stats)
            log.info("[REDIS] ranking reconstruido desde MongoDB: ${stats.size} libros")
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible para reconstruir ranking: ${e.message}")
        }
    }

    @Scheduled(
        initialDelayString = "\${cache.ranking-sync-initial-delay-ms:10000}",
        fixedDelayString = "\${cache.ranking-sync-ms:300000}"
    )
    fun synchronizeRanking() {
        try {
            val stats = bookClickRepository.findAllByClickCount()
            bookRankingRepository.rebuildRanking(stats)
            log.info("[REDIS] ranking sincronizado desde MongoDB: ${stats.size} libros")
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible para sincronizar ranking: ${e.message}")
        }
    }

    fun tryServeFromRedis(user: User, filters: BookingFiltersRequest): List<BookingDto>? {
        if (filters.hasActiveFilters()) {
            log.info("[REDIS] skip — filtros activos: $filters")
            return null
        }
        return try {
            ensureRankingLoaded()
            val userId   = user.id!!
            val top10Ids = getTop10BookIds()
            if (top10Ids.isEmpty()) {
                log.info("[REDIS] skip — ZSet vacío")
                return null
            }

            val showable = top10Ids
                .mapNotNull { getBookDto(it) }
                .filter { it.ownerId != userId && it.active && !it.deleted && it.passesFilters(filters) }

            if (showable.size < 6) null
            else showable.take(6).map { dto -> dto.toBookingDto(user, filters) }
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible, respondiendo con MongoDB: ${e.message}")
            null
        }
    }
}
