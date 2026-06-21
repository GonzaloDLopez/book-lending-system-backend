package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.dto.FilteredBookingsPageDto
import ar.edu.unsam.phm.dto.booking.BookingPageDto
import ar.edu.unsam.phm.dto.booking.BookingType
import ar.edu.unsam.phm.dto.bookingToDto
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.mapper.toBookingPageDto
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.validator.BookingValidator
import java.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingService {
    @Autowired
    lateinit var bookingRepository: BookingRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var bookingCardDataResolver: BookingCardDataResolver

    @Transactional(readOnly = true)
    fun getMyBookings(userId: Long, type: BookingType, search: String?, page: Int, size: Int): BookingPageDto {
        BookingValidator.validatePagination(page, size)
        val pageable = PageRequest.of(
            page, size,
            Sort.by(Sort.Order.desc("from"), Sort.Order.asc("id"))
        )
        val normalizedSearch = search?.trim()?.takeIf { it.isNotBlank() }
        val today = LocalDate.now()
        val bookingsPage = when (type) {
            BookingType.FOR_ME ->
                if (normalizedSearch == null)
                    bookingRepository.findBookingsForReader(userId, today, pageable)
                else
                    bookingRepository.findBookingsForReaderBySearch(userId, normalizedSearch, today, pageable)
            BookingType.BY_ME ->
                if (normalizedSearch == null)
                    bookingRepository.findBookingsForOwner(userId, today, pageable)
                else
                    bookingRepository.findBookingsForOwnerBySearch(userId, normalizedSearch, today, pageable)
        }
        val itemsData = bookingCardDataResolver.resolve(bookings = bookingsPage.content, type = type)
        return toBookingPageDto(
            items      = itemsData,
            type       = type,
            page       = page,
            size       = size,
            totalItems = bookingsPage.totalElements.toInt(),
            totalPages = bookingsPage.totalPages
        )
    }

    fun generatePossibleBooking(book: Book, user: User, from: LocalDate? = null, to: LocalDate? = null): Booking {
        val dateFrom     = from ?: LocalDate.now()
        val dateTo       = to   ?: dateFrom
        val bookingCount = bookingRepository.countByBookIdAndCancelledFalse(book.id!!)
        return Booking(book, user, dateFrom, dateTo, bookingCount)
    }

    fun possibleBookingsList(user: User, booksPage: Page<Book>, owners: Map<Long, User>, filters: BookingFiltersRequest, page: Int): FilteredBookingsPageDto {
        val items = booksPage.content.map { book ->
            val owner = owners[book.ownerId] ?: error("Owner ${book.ownerId} no encontrado")
            bookingToDto(generatePossibleBooking(book, user, filters.from, filters.to), book, owner, user)
        }
        return FilteredBookingsPageDto(
            items       = items,
            page        = page,
            totalItems  = booksPage.totalElements,
            totalPages  = booksPage.totalPages,
            hasNext     = booksPage.hasNext(),
            hasPrevious = booksPage.hasPrevious()
        )
    }

    @Transactional(readOnly = true)
    fun readBookCountByUser(userId: Long): Int =
        bookingRepository.countByReaderIdAndCancelledFalse(userId)

    @Transactional(readOnly = true)
    fun lentBookCountByUser(userId: Long): Int =
        bookingRepository.countByOwnerIdAndCancelledFalse(userId)

    @Transactional
    fun confirmBooking(book: Book, user: User, from: LocalDate, to: LocalDate) {
        BookingValidator.validateBooking(book, user, from, to)
        val bookId = book.id ?: throw BusinessException("El libro debe estar persistido antes de reservar.")
        bookingRepository.lockBookingForBook(bookId)
        if (bookingRepository.existsOverlappingBooking(bookId, from, to))
            throw BusinessException("El libro no está disponible para las fechas seleccionadas.")
        val bookingCount = bookingRepository.countByBookIdAndCancelledFalse(bookId)
        val booking      = user.book(book, from, to, bookingCount)
        userRepository.addBibliokarmas(user.id!!, booking.bibliokarmas)
        bookingRepository.save(booking)
        bookRepository.save(book)
    }
}
