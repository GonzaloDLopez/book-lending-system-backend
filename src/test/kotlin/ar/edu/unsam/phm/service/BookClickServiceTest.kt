package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.BookCondition
import ar.edu.unsam.phm.domain.Genre
import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.dto.BookRedisDto
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.dto.CachedBookedRange
import ar.edu.unsam.phm.dto.ConversionStatsDto
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.mongo.BookClickRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.repository.redis.BookRankingRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.dao.DataAccessResourceFailureException
import java.time.LocalDate

class BookClickServiceTest {

    private val bookClickRepository:   BookClickRepository   = mockk()
    private val bookRepository:        BookRepository        = mockk()
    private val bookingRepository:     BookingRepository     = mockk()
    private val bookRankingRepository: BookRankingRepository = mockk()
    private lateinit var bookClickService: BookClickService

    private val owner  = createOwner()         // id = 1L
    private val reader = createReaderUser()    // id = 2L
    private val book   = createCommonBook(owner = owner)

    @BeforeEach
    fun setup() {
        bookClickService = BookClickService(bookClickRepository, bookRepository, bookingRepository, bookRankingRepository)
        every { bookRankingRepository.isRankingInitialized() } returns true
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeRedisDto(
        bookId:       String                  = "book-1",
        ownerId:      Long                    = owner.id!!,
        active:       Boolean                 = true,
        deleted:      Boolean                 = false,
        bookedRanges: List<CachedBookedRange> = emptyList()
    ) = BookRedisDto(
        bookId          = bookId,
        title           = "Libro $bookId",
        image           = "",
        description     = "",
        genre           = Genre.classic_literature,
        author          = "Autor",
        pageCount       = 300,
        isbn13          = "978-0000000001",
        language        = "spanish",
        publisher       = "Ed",
        publicationDate = LocalDate.of(2020, 1, 1),
        condition       = BookCondition.good,
        bookType        = "common",
        ranking         = 4.0,
        bookingCount    = 0,
        bookedRanges    = bookedRanges,
        ownerId         = ownerId,
        ownerFirstName  = owner.firstName,
        ownerLastName   = owner.lastName,
        ownerUsername   = owner.username,
        active          = active,
        deleted         = deleted
    )

    // ── registerClick ─────────────────────────────────────────────────────────

    @Test
    fun `registerClick guarda el click cuando el libro existe`() {
        every { bookRepository.existsById("book-1") } returns true
        every { bookClickRepository.save(any()) } answers { firstArg() }
        every { bookRankingRepository.incrementClick(any()) } just runs

        bookClickService.registerClick("book-1", "pedrito")

        verify {
            bookClickRepository.save(match {
                it.bookId == "book-1" && it.username == "pedrito"
            })
        }
        verify { bookRankingRepository.incrementClick("book-1") }
    }

    @Test
    fun `registerClick no guarda nada cuando el libro no existe`() {
        every { bookRepository.existsById("libro-inexistente") } returns false

        bookClickService.registerClick("libro-inexistente", "pedrito")

        verify(exactly = 0) { bookClickRepository.save(any()) }
        verify(exactly = 0) { bookRankingRepository.incrementClick(any()) }
    }

    @Test
    fun `registerClick no propaga excepcion cuando Redis no esta disponible`() {
        every { bookRepository.existsById("book-1") } returns true
        every { bookClickRepository.save(any()) } answers { firstArg() }
        every { bookRankingRepository.incrementClick(any()) } throws DataAccessResourceFailureException("Redis down")

        bookClickService.registerClick("book-1", "pedrito")

        verify { bookClickRepository.save(any()) }
    }

    // ── getClickStatsByOwner ──────────────────────────────────────────────────

    @Test
    fun `getClickStatsByOwner devuelve las estadísticas de los libros del publicador`() {
        val stats = listOf(BookClickCountDto(id = "book-1", clickCount = 5L))

        every { bookRepository.findAllByOwnerIdAndActiveTrueAndDeletedFalse(owner.id!!) } returns listOf(book)
        every { bookClickRepository.countClicksByBookIds(listOf("book-1")) } returns stats

        val result = bookClickService.getClickStatsByOwner(owner.id!!)

        assertEquals(1, result.size)
        assertEquals("book-1", result[0].id)
        assertEquals(5L, result[0].clickCount)
    }

    @Test
    fun `getClickStatsByOwner devuelve lista vacía cuando el publicador no tiene libros`() {
        every { bookRepository.findAllByOwnerIdAndActiveTrueAndDeletedFalse(owner.id!!) } returns emptyList()

        val result = bookClickService.getClickStatsByOwner(owner.id!!)

        assertEquals(emptyList<BookClickCountDto>(), result)
        verify(exactly = 0) { bookClickRepository.countClicksByBookIds(any()) }
    }

    @Test
    fun `getClickStatsByOwner no llama al repositorio de clicks cuando ningún libro tiene id`() {
        val bookSinId = createCommonBook(owner = owner).apply { id = null }

        every { bookRepository.findAllByOwnerIdAndActiveTrueAndDeletedFalse(owner.id!!) } returns listOf(bookSinId)

        val result = bookClickService.getClickStatsByOwner(owner.id!!)

        assertEquals(emptyList<BookClickCountDto>(), result)
        verify(exactly = 0) { bookClickRepository.countClicksByBookIds(any()) }
    }

    // ── updateBookCaches ──────────────────────────────────────────────────────

    @Test
    fun `updateBookCaches actualiza la caché para cada libro con su conteo de reservas activas`() {
        every { bookingRepository.countByBookIdAndCancelledFalse("book-1") } returns 3
        every { bookRankingRepository.updateBookCache(book, 3, owner) } just runs

        bookClickService.updateBookCaches(listOf(book), mapOf(owner.id!! to owner))

        verify { bookRankingRepository.updateBookCache(book, 3, owner) }
    }

    @Test
    fun `updateBookCaches no propaga excepcion cuando Redis no esta disponible`() {
        every { bookingRepository.countByBookIdAndCancelledFalse("book-1") } returns 0
        every { bookRankingRepository.updateBookCache(book, 0, owner) } throws DataAccessResourceFailureException("Redis down")

        bookClickService.updateBookCaches(listOf(book), mapOf(owner.id!! to owner))
    }

    // ── tryServeFromRedis ─────────────────────────────────────────────────────

    @Test
    fun `tryServeFromRedis retorna null cuando hay filtros activos`() {
        val result = bookClickService.tryServeFromRedis(reader, BookingFiltersRequest(title = "Aleph"))

        assertNull(result)
        verify(exactly = 0) { bookRankingRepository.getTop10BookIds() }
    }

    @Test
    fun `tryServeFromRedis retorna null cuando el ZSet esta vacio y MongoDB no tiene clicks`() {
        every { bookRankingRepository.isRankingInitialized() } returns false
        every { bookRankingRepository.getTop10BookIds() } returns emptyList()
        every { bookClickRepository.findAllByClickCount() } returns emptyList()
        every { bookRankingRepository.rebuildRanking(emptyList()) } just runs

        val result = bookClickService.tryServeFromRedis(reader, BookingFiltersRequest())

        assertNull(result)
    }

    @Test
    fun `tryServeFromRedis reconstruye el ZSet cuando estaba vacio y MongoDB tiene datos`() {
        val ids   = (1..6).map { "book-$it" }
        val dtos  = ids.map { makeRedisDto(bookId = it) }
        val stats = listOf(BookClickCountDto(id = "book-1", clickCount = 10L))

        every { bookRankingRepository.isRankingInitialized() } returns false
        every { bookRankingRepository.getTop10BookIds() } returns ids
        every { bookClickRepository.findAllByClickCount() } returns stats
        every { bookRankingRepository.rebuildRanking(stats) } just runs
        dtos.forEach { dto -> every { bookRankingRepository.getBookDto(dto.bookId) } returns dto }

        val result = bookClickService.tryServeFromRedis(reader, BookingFiltersRequest())

        assertEquals(6, result?.size)
        verify { bookRankingRepository.rebuildRanking(stats) }
    }

    @Test
    fun `tryServeFromRedis retorna null cuando hay menos de 6 libros elegibles`() {
        val ids  = listOf("book-1", "book-2")
        val dtos = ids.map { makeRedisDto(bookId = it) }

        every { bookRankingRepository.getTop10BookIds() } returns ids
        dtos.forEach { dto -> every { bookRankingRepository.getBookDto(dto.bookId) } returns dto }

        val result = bookClickService.tryServeFromRedis(reader, BookingFiltersRequest())

        assertNull(result)
    }

    @Test
    fun `tryServeFromRedis sirve exactamente 6 items cuando hay suficientes libros elegibles`() {
        val ids  = (1..8).map { "book-$it" }
        val dtos = ids.map { makeRedisDto(bookId = it) }

        every { bookRankingRepository.getTop10BookIds() } returns ids
        dtos.forEach { dto -> every { bookRankingRepository.getBookDto(dto.bookId) } returns dto }

        val result = bookClickService.tryServeFromRedis(reader, BookingFiltersRequest())

        assertEquals(6, result?.size)
    }

    @Test
    fun `tryServeFromRedis retorna null cuando Redis lanza DataAccessException`() {
        every { bookRankingRepository.getTop10BookIds() } throws DataAccessResourceFailureException("Redis down")

        val result = bookClickService.tryServeFromRedis(reader, BookingFiltersRequest())

        assertNull(result)
    }

    // ── getKpiTasaConversion ──────────────────────────────────────────────────

    @Test
    fun `getKpiTasaConversion devuelve estadisticas de conversion para el top 5`() {
        val scores = listOf("book-1" to 10.0, "book-2" to 5.0)

        every { bookRankingRepository.getTop10BookIds() } returns listOf("book-1", "book-2")
        every { bookRankingRepository.getTop5WithScores() } returns scores
        every { bookRankingRepository.getBookDto("book-1") } returns makeRedisDto(bookId = "book-1")
        every { bookRankingRepository.getBookDto("book-2") } returns makeRedisDto(bookId = "book-2")
        every { bookingRepository.countByBookIdAndCancelledFalse("book-1") } returns 3
        every { bookingRepository.countByBookIdAndCancelledFalse("book-2") } returns 1

        val result = bookClickService.getKpiTasaConversion()

        assertEquals(2, result.size)
        assertEquals("book-1", result[0].bookId)
        assertEquals(10, result[0].clicks)
        assertEquals(3, result[0].reservas)
        assertEquals(0.3, result[0].tasaConversion, 0.001)
    }

    @Test
    fun `getKpiTasaConversion retorna lista vacia cuando no hay datos`() {
        every { bookRankingRepository.isRankingInitialized() } returns false
        every { bookRankingRepository.getTop10BookIds() } returns emptyList()
        every { bookClickRepository.findAllByClickCount() } returns emptyList()
        every { bookRankingRepository.rebuildRanking(emptyList()) } just runs
        every { bookRankingRepository.getTop5WithScores() } returns emptyList()

        val result = bookClickService.getKpiTasaConversion()

        assertEquals(emptyList<ConversionStatsDto>(), result)
    }
}
