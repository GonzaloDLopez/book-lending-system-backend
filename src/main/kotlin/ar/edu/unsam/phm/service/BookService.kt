package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Genre
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookPageDto
import ar.edu.unsam.phm.dto.BookRequest
import ar.edu.unsam.phm.dto.BookSortBy
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.factory.BookFactory
import ar.edu.unsam.phm.mapper.toBookCardDto
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.ReviewRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookClickRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.repository.redis.BookRankingRepository
import ar.edu.unsam.phm.service.notification.BookingCancelledNotifier
import ar.edu.unsam.phm.validator.BookValidator
import java.time.LocalDate
import java.util.regex.Pattern
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.dao.DataAccessException

@Service
class BookService(
    private val bookRepository:           BookRepository,
    private val bookingRepository:        BookingRepository,
    private val reviewRepository:         ReviewRepository,
    private val bookingCancelledNotifier: BookingCancelledNotifier,
    private val bookClickRepository:      BookClickRepository,
    private val bookRankingRepository:    BookRankingRepository,
    private val userRepository:           UserRepository
) {
    private val log = LoggerFactory.getLogger(BookService::class.java)

    fun getFilteredPage(
        filters: BookingFiltersRequest,
        userId:  Long,
        ownerId: Long?,
        page:    Int,
        size:    Int,
        sortBy:  BookSortBy
    ): Page<Book> {
        if (sortBy == BookSortBy.popularity) {
            log.info("[POPULARITY] userId=$userId -> MONGODB page=$page")
            return filteredBooksOrderedByPopularity(filters, userId, ownerId, page, size)
        }
        if (sortBy == BookSortBy.owner) {
            return filteredBooksOrderedByOwner(filters, userId, ownerId, page, size)
        }
        return filteredBooks(filters, userId, ownerId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy.field)))
    }

    fun getBookById(id: String): Book =
        bookRepository.findById(id)
            .orElseThrow { NotFoundException("No se ha encontrado el libro") }

    fun getBookForView(id: String): Book {
        val book = getBookById(id)
        if (book.deleted || !book.active)
            throw NotFoundException("No se ha encontrado el libro")
        return book
    }

    fun getBookReviews(id: String, onlyLatest: Boolean): List<Review> {
        if (onlyLatest) return getBookById(id).lastReviews.toList()
        return reviewRepository.findAllByBookId(id)
    }

    fun filteredBooks(filters: BookingFiltersRequest, userId: Long, ownerId: Long?, pageable: Pageable): Page<Book> {
        val genres    = filters.genre.ifEmpty { Genre.entries }
        val checkFrom = filters.from ?: LocalDate.now()
        val checkTo   = filters.to   ?: LocalDate.now()

        return bookRepository.findFilteredBooks(
            userId    = userId,
            title     = filters.title?.takeIf { it.isNotBlank() }?.let(::toRegex),
            isbn13    = filters.isbn13?.takeIf { it.isNotBlank() },
            author    = filters.author?.takeIf { it.isNotBlank() }?.let(::toRegex),
            ownerId   = ownerId,
            genres    = genres,
            minPages  = filters.minPages,
            maxPages  = filters.maxPages,
            checkFrom = checkFrom,
            checkTo   = checkTo,
            pageable  = pageable
        )
    }

    fun countFilteredBooks(filters: BookingFiltersRequest, userId: Long): Long =
        filteredBooks(filters, userId, null, PageRequest.of(0, 1, Sort.by("title"))).totalElements

    fun filteredBooksOrderedByPopularity(
        filters: BookingFiltersRequest,
        userId:  Long,
        ownerId: Long?,
        page:    Int,
        size:    Int
    ): Page<Book> {
        val allFiltered = filteredBooks(filters, userId, ownerId, Pageable.unpaged()).content

        val orderedIds   = bookClickRepository.findAllByClickCount().map { it.id }
        val indexById    = orderedIds.withIndex().associate { (idx, id) -> id to idx }
        val sorted       = allFiltered.sortedWith(compareBy<Book> { indexById[it.id] ?: Int.MAX_VALUE }.thenBy { it.id })

        val pageContent  = sorted.drop(page * size).take(size)
        return PageImpl(pageContent, PageRequest.of(page, size), sorted.size.toLong())
    }

    private fun filteredBooksOrderedByOwner(
        filters: BookingFiltersRequest,
        userId: Long,
        ownerId: Long?,
        page: Int,
        size: Int
    ): Page<Book> {
        val books = filteredBooks(filters, userId, ownerId, Pageable.unpaged()).content
        val owners = userRepository.findAllById(books.map { it.ownerId }.distinct())
            .associateBy { it.id }
        val sorted = books.sortedWith(
            compareBy<Book> { owners[it.ownerId]?.username?.lowercase() ?: "" }
                .thenBy { it.title.lowercase() }
        )
        return PageImpl(sorted.drop(page * size).take(size), PageRequest.of(page, size), sorted.size.toLong())
    }

    private fun toRegex(value: String): String =
        Pattern.quote(value.trim().removeSurrounding("%"))

    @Transactional
    fun deleteBookOfUser(userId: Long, bookId: String) {
        val book = bookRepository.findByIdAndOwnerIdAndDeletedFalse(bookId, userId)
            ?: throw NotFoundException("No se ha encontrado el libro para el usuario indicado")
        deleteBookAndCancelFutureBookings(book)
        bookRepository.save(book)
        evictBookCache(book.id)
    }

    @Transactional
    fun disableBooksFromUser(userId: Long) {
        val books = bookRepository.findAllByOwnerIdAndActiveTrueAndDeletedFalse(userId)
        books.forEach { deactivateBookAndCancelFutureBookings(it) }
        bookRepository.saveAll(books)
        books.forEach { evictBookCache(it.id) }
    }

    @Transactional
    fun enableBooksFromUser(userId: Long) {
        val books = bookRepository.findAllByOwnerIdAndActiveFalseAndDeletedFalse(userId)
        books.forEach { it.activate() }
        bookRepository.saveAll(books)
        books.forEach { evictBookCache(it.id) }
    }

    fun getUserBooks(
        id: Long,
        page: Int,
        pageSize: Int,
        field: String,
        direction: String,
        filter: String
    ): BookPageDto {
        val pageable  = userBooksPageable(page, pageSize, field, direction)
        val today     = LocalDate.now()
        val booksPage = when (filter) {
            "Available" -> bookRepository.findAvailableNowByOwnerId(id, today, pageable)
            "Lent"      -> bookRepository.findBorrowedNowByOwnerId(id, today, pageable)
            "Disabled"  -> bookRepository.findAllByOwnerIdAndActiveFalseAndDeletedFalse(id, pageable)
            else        -> userBooksPage(id, today, pageable, field, direction)
        }

        return BookPageDto(
            books      = toBookCardDto(booksPage.content),
            totalPages = booksPage.totalPages
        )
    }

    fun create(req: BookRequest, owner: User): Book {
        val book = BookFactory.create(req, owner)
        if (!owner.canPublish()) book.deactivate()
        return bookRepository.save(book)
    }

    fun update(req: BookRequest, owner: User, existingBook: Book): Book {
        BookValidator.validateOwner(existingBook, owner)
        val updatedBook = BookFactory.update(req, existingBook)
        return bookRepository.save(updatedBook).also { evictBookCache(it.id) }
    }

    fun evictBooksFromUser(userId: Long) {
        bookRepository.findAllByOwnerId(userId).forEach { evictBookCache(it.id) }
    }

    fun evictBookCache(bookId: String?) {
        if (bookId == null) return
        try {
            bookRankingRepository.evictBookCache(bookId)
        } catch (e: DataAccessException) {
            log.warn("[REDIS] no disponible para invalidar cache de libro $bookId: ${e.message}")
        }
    }

    private fun userBooksPageable(page: Int, pageSize: Int, field: String, direction: String): Pageable {
        val normalizedPage = if (page <= 0) 0 else page - 1
        val safeSize       = pageSize.coerceAtLeast(1)

        if (field != "addedAt" || direction.equals("none", ignoreCase = true)) {
            return PageRequest.of(normalizedPage, safeSize, Sort.by(Sort.Direction.ASC, "_id"))
        }

        val sortDirection =
            if (direction.equals("desc", ignoreCase = true)) Sort.Direction.DESC
            else Sort.Direction.ASC

        return PageRequest.of(normalizedPage, safeSize, Sort.by(sortDirection, "createdAt"))
    }

    private fun userBooksPage(
        id: Long,
        today: LocalDate,
        pageable: Pageable,
        field: String,
        direction: String
    ): Page<Book> =
        when {
            field == "available" && direction.equals("asc", ignoreCase = true)  ->
                bookRepository.findUserBooksOrderByAvailableAsc(id, today, pageable)
            field == "available" && direction.equals("desc", ignoreCase = true) ->
                bookRepository.findUserBooksOrderByAvailableDesc(id, today, pageable)
            else ->
                bookRepository.findAllByOwnerIdAndActiveTrue(id, pageable)
        }

    private fun deleteBookAndCancelFutureBookings(book: Book) {
        book.delete()
        cancelFutureBookings(book)
    }

    private fun deactivateBookAndCancelFutureBookings(book: Book) {
        book.deactivate()
        cancelFutureBookings(book)
    }

    private fun cancelFutureBookings(book: Book) {
        val today = LocalDate.now()
        val futureBookings = bookingRepository.findFutureBookingsByBookId(book.id!!, today)
        futureBookings.forEach { booking ->
            booking.cancel()
            bookingCancelledNotifier.notify(booking)
            book.bookedRanges.removeIf { it.from == booking.from && it.to == booking.to }
        }
        bookingRepository.saveAll(futureBookings)
    }
}
