package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.BookSortBy
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.dto.ConfirmBookingRequest
import ar.edu.unsam.phm.dto.FilteredBookingsPageDto
import ar.edu.unsam.phm.dto.booking.BookingPageDto
import ar.edu.unsam.phm.dto.booking.BookingType
import ar.edu.unsam.phm.service.BookClickService
import ar.edu.unsam.phm.service.BookService
import ar.edu.unsam.phm.service.BookingService
import ar.edu.unsam.phm.service.UserService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.security.core.Authentication
import java.time.LocalDate

class BookingControllerTest {

    private val bookingService:   BookingService   = mockk()
    private val bookService:      BookService      = mockk()
    private val userService:      UserService      = mockk()
    private val bookClickService: BookClickService = mockk()
    private val authentication:   Authentication  = mockk()

    private lateinit var controller: BookingController

    private val reader = createReaderUser()   // id = 2L
    private val owner  = createOwner()        // id = 1L
    private val book   = createCommonBook(owner = owner)

    @BeforeEach
    fun setup() {
        controller = BookingController(bookingService, bookService, userService, bookClickService)
        every { authentication.name } returns reader.id.toString()
        every { userService.findById(reader.id!!) } returns reader
    }

    // ── getMyBookings ─────────────────────────────────────────────────────────

    @Test
    fun `getMyBookings delega al service con el userId del token`() {
        val expected: BookingPageDto = mockk()
        every { bookingService.getMyBookings(reader.id!!, BookingType.FOR_ME, null, 0, 4) } returns expected

        val result = controller.getMyBookings(BookingType.FOR_ME, null, 0, 4, authentication)

        assertEquals(expected, result)
    }

    // ── getFilteredBookings — ownerUsername ───────────────────────────────────

    @Test
    fun `getFilteredBookings retorna pagina vacia cuando ownerUsername no existe`() {
        val filters = BookingFiltersRequest(ownerUsername = "nonexistent")
        every { userService.findByUsername("nonexistent") } returns null

        val result = controller.getFilteredBookings(filters, 0, 6, BookSortBy.title, authentication)

        assertEquals(0, result.totalItems)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `getFilteredBookings no va a Redis cuando ownerUsername esta en blanco`() {
        val filters = BookingFiltersRequest(ownerUsername = "  ")
        val expected: FilteredBookingsPageDto = mockk()
        val emptyPage = PageImpl<ar.edu.unsam.phm.domain.Book>(emptyList())

        every { bookService.getFilteredPage(filters, reader.id!!, null, 0, 6, BookSortBy.title) } returns emptyPage
        every { userService.findAllById(emptyList()) } returns emptyList()
        every { bookClickService.updateBookCaches(emptyList(), emptyMap()) } just runs
        every { bookingService.possibleBookingsList(reader, emptyPage, emptyMap(), filters, 0) } returns expected

        val result = controller.getFilteredBookings(filters, 0, 6, BookSortBy.title, authentication)

        assertEquals(expected, result)
    }

    // ── getFilteredBookings — camino Redis ────────────────────────────────────

    @Test
    fun `getFilteredBookings retorna pagina Redis cuando Redis puede servir`() {
        val filters   = BookingFiltersRequest()
        val redisItem: ar.edu.unsam.phm.dto.BookingDto = mockk()

        every { bookClickService.tryServeFromRedis(reader, filters) } returns listOf(redisItem)
        every { bookService.countFilteredBooks(filters, reader.id!!) } returns 10L

        val result = controller.getFilteredBookings(filters, 0, 6, BookSortBy.popularity, authentication)

        assertEquals(1, result.items.size)
        assertEquals(10L, result.totalItems)
        verify(exactly = 0) { bookService.getFilteredPage(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getFilteredBookings no intenta Redis cuando page es mayor a 0`() {
        val filters  = BookingFiltersRequest()
        val booksPage = PageImpl<ar.edu.unsam.phm.domain.Book>(emptyList())
        val expected: FilteredBookingsPageDto = mockk()

        every { bookService.getFilteredPage(filters, reader.id!!, null, 1, 6, BookSortBy.popularity) } returns booksPage
        every { userService.findAllById(emptyList()) } returns emptyList()
        every { bookClickService.updateBookCaches(emptyList(), emptyMap()) } just runs
        every { bookingService.possibleBookingsList(reader, booksPage, emptyMap(), filters, 1) } returns expected

        val result = controller.getFilteredBookings(filters, 1, 6, BookSortBy.popularity, authentication)

        assertEquals(expected, result)
        verify(exactly = 0) { bookClickService.tryServeFromRedis(any(), any()) }
    }

    // ── getFilteredBookings — camino MongoDB ──────────────────────────────────

    @Test
    fun `getFilteredBookings va a MongoDB cuando Redis retorna null`() {
        val filters   = BookingFiltersRequest()
        val booksPage = PageImpl<ar.edu.unsam.phm.domain.Book>(emptyList())
        val expected: FilteredBookingsPageDto = mockk()

        every { bookClickService.tryServeFromRedis(reader, filters) } returns null
        every { bookService.getFilteredPage(filters, reader.id!!, null, 0, 6, BookSortBy.popularity) } returns booksPage
        every { userService.findAllById(emptyList()) } returns emptyList()
        every { bookClickService.updateBookCaches(emptyList(), emptyMap()) } just runs
        every { bookingService.possibleBookingsList(reader, booksPage, emptyMap(), filters, 0) } returns expected

        val result = controller.getFilteredBookings(filters, 0, 6, BookSortBy.popularity, authentication)

        assertEquals(expected, result)
    }

    @Test
    fun `getFilteredBookings va a MongoDB con sort por titulo sin intentar Redis`() {
        val filters   = BookingFiltersRequest()
        val booksPage = PageImpl<ar.edu.unsam.phm.domain.Book>(emptyList())
        val expected: FilteredBookingsPageDto = mockk()

        every { bookService.getFilteredPage(filters, reader.id!!, null, 0, 6, BookSortBy.title) } returns booksPage
        every { userService.findAllById(emptyList()) } returns emptyList()
        every { bookClickService.updateBookCaches(emptyList(), emptyMap()) } just runs
        every { bookingService.possibleBookingsList(reader, booksPage, emptyMap(), filters, 0) } returns expected

        val result = controller.getFilteredBookings(filters, 0, 6, BookSortBy.title, authentication)

        assertEquals(expected, result)
        verify(exactly = 0) { bookClickService.tryServeFromRedis(any(), any()) }
    }

    @Test
    fun `getFilteredBookings pasa el ownerId correcto cuando ownerUsername existe`() {
        val filters   = BookingFiltersRequest(ownerUsername = owner.username)
        val booksPage = PageImpl<ar.edu.unsam.phm.domain.Book>(emptyList())
        val expected: FilteredBookingsPageDto = mockk()

        every { userService.findByUsername(owner.username) } returns owner
        every { bookService.getFilteredPage(filters, reader.id!!, owner.id!!, 0, 6, BookSortBy.title) } returns booksPage
        every { userService.findAllById(emptyList()) } returns emptyList()
        every { bookClickService.updateBookCaches(emptyList(), emptyMap()) } just runs
        every { bookingService.possibleBookingsList(reader, booksPage, emptyMap(), filters, 0) } returns expected

        val result = controller.getFilteredBookings(filters, 0, 6, BookSortBy.title, authentication)

        assertEquals(expected, result)
    }

    // ── confirmBooking ────────────────────────────────────────────────────────

    @Test
    fun `confirmBooking confirma la reserva y actualiza la cache de Redis`() {
        val from = LocalDate.of(2026, 7, 1)
        val to   = LocalDate.of(2026, 7, 10)
        val body = ConfirmBookingRequest(from = from, to = to)

        every { bookService.getBookById(book.id!!) } returns book
        every { userService.findById(reader.id!!) } returns reader
        every { bookingService.confirmBooking(book, reader, from, to) } just runs
        every { userService.findById(owner.id!!) } returns owner
        every { bookClickService.updateBookCaches(listOf(book), mapOf(owner.id!! to owner)) } just runs

        controller.confirmBooking(book.id!!, body, authentication)

        verify { bookingService.confirmBooking(book, reader, from, to) }
        verify { bookClickService.updateBookCaches(listOf(book), mapOf(owner.id!! to owner)) }
    }
}
