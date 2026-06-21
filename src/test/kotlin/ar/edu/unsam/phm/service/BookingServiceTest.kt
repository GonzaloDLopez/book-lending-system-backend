package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.dto.booking.BookingPageDto
import ar.edu.unsam.phm.dto.booking.BookingType
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.mapper.BookingCardData
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

class BookingServiceTest {

    private val bookingRepository:       BookingRepository       = mockk()
    private val userRepository:          UserRepository          = mockk()
    private val bookRepository:          BookRepository          = mockk()
    private val bookingCardDataResolver: BookingCardDataResolver = mockk()

    private lateinit var service: BookingService

    private val owner  = createOwner()       // id = 1L
    private val reader = createReaderUser()  // id = 2L
    private val book   = createCommonBook(owner = owner)

    @BeforeEach
    fun setup() {
        service = BookingService().also {
            it.bookingRepository       = bookingRepository
            it.userRepository          = userRepository
            it.bookRepository          = bookRepository
            it.bookingCardDataResolver = bookingCardDataResolver
        }
    }

    // ── confirmBooking ────────────────────────────────────────────────────────

    @Test
    fun `confirmBooking crea la reserva y descuenta bibliokarmas`() {
        val from = LocalDate.of(2026, 8, 1)
        val to   = LocalDate.of(2026, 8, 10)

        every { bookingRepository.lockBookingForBook(book.id!!) } just runs
        every { bookingRepository.existsOverlappingBooking(book.id!!, from, to) } returns false
        every { bookingRepository.countByBookIdAndCancelledFalse(book.id!!) } returns 0
        every { userRepository.addBibliokarmas(reader.id!!, any()) } just runs
        every { bookingRepository.save(any()) } answers { firstArg() }
        every { bookRepository.save(any()) } answers { firstArg() }

        service.confirmBooking(book, reader, from, to)

        verify { bookingRepository.save(any()) }
        verify { userRepository.addBibliokarmas(eq(reader.id!!), any()) }
        verify { bookRepository.save(book) }
    }

    @Test
    fun `confirmBooking lanza BusinessException cuando hay superposicion de fechas`() {
        val from = LocalDate.of(2026, 8, 1)
        val to   = LocalDate.of(2026, 8, 10)

        every { bookingRepository.lockBookingForBook(book.id!!) } just runs
        every { bookingRepository.existsOverlappingBooking(book.id!!, from, to) } returns true

        val ex = assertThrows(BusinessException::class.java) {
            service.confirmBooking(book, reader, from, to)
        }

        assertEquals("El libro no está disponible para las fechas seleccionadas.", ex.message)
        verify(exactly = 0) { bookingRepository.save(any()) }
    }

    @Test
    fun `confirmBooking rechaza reservar un libro propio`() {
        val from = LocalDate.now().plusDays(1)

        assertThrows(BusinessException::class.java) {
            service.confirmBooking(book, owner, from, from.plusDays(1))
        }

        verify(exactly = 0) { bookingRepository.lockBookingForBook(any()) }
    }

    @Test
    fun `confirmBooking rechaza libros inactivos`() {
        val from = LocalDate.now().plusDays(1)
        book.deactivate()

        assertThrows(BusinessException::class.java) {
            service.confirmBooking(book, reader, from, from.plusDays(1))
        }

        verify(exactly = 0) { bookingRepository.lockBookingForBook(any()) }
    }

    @Test
    fun `confirmBooking rechaza fechas pasadas`() {
        val from = LocalDate.now().minusDays(1)

        assertThrows(BusinessException::class.java) {
            service.confirmBooking(book, reader, from, LocalDate.now())
        }

        verify(exactly = 0) { bookingRepository.lockBookingForBook(any()) }
    }

    // ── possibleBookingsList ──────────────────────────────────────────────────

    @Test
    fun `possibleBookingsList construye la pagina de bookings con los owners dados`() {
        val filters   = BookingFiltersRequest()
        val booksPage = PageImpl<Book>(listOf(book), PageRequest.of(0, 6), 1L)

        every { bookingRepository.countByBookIdAndCancelledFalse(book.id!!) } returns 0

        val result = service.possibleBookingsList(reader, booksPage, mapOf(owner.id!! to owner), filters, 0)

        assertEquals(1, result.items.size)
        assertEquals(0, result.page)
        assertEquals(1L, result.totalItems)
    }

    @Test
    fun `possibleBookingsList retorna pagina vacia cuando no hay libros`() {
        val emptyPage = PageImpl<ar.edu.unsam.phm.domain.Book>(emptyList(), PageRequest.of(0, 6), 0L)
        val filters   = BookingFiltersRequest()

        val result = service.possibleBookingsList(reader, emptyPage, emptyMap(), filters, 0)

        assertTrue(result.items.isEmpty())
        assertEquals(0L, result.totalItems)
    }

    // ── getMyBookings ─────────────────────────────────────────────────────────

    @Test
    fun `getMyBookings delega al repositorio con tipo FOR_ME`() {
        val emptyPage = PageImpl<ar.edu.unsam.phm.domain.Booking>(emptyList())
        val expected: BookingPageDto = mockk()

        every { bookingRepository.findBookingsForReader(any(), any(), any()) } returns emptyPage
        every { bookingCardDataResolver.resolve(emptyList(), BookingType.FOR_ME) } returns emptyList()

        val result = service.getMyBookings(reader.id!!, BookingType.FOR_ME, null, 0, 4)

        verify { bookingRepository.findBookingsForReader(eq(reader.id!!), any(), any()) }
    }

    @Test
    fun `getMyBookings delega al repositorio con tipo BY_ME`() {
        val emptyPage = PageImpl<ar.edu.unsam.phm.domain.Booking>(emptyList())

        every { bookingRepository.findBookingsForOwner(any(), any(), any()) } returns emptyPage
        every { bookingCardDataResolver.resolve(emptyList(), BookingType.BY_ME) } returns emptyList()

        service.getMyBookings(reader.id!!, BookingType.BY_ME, null, 0, 4)

        verify { bookingRepository.findBookingsForOwner(eq(reader.id!!), any(), any()) }
    }

    @Test
    fun `getMyBookings usa busqueda por texto cuando search no es nulo`() {
        val emptyPage = PageImpl<ar.edu.unsam.phm.domain.Booking>(emptyList())

        every { bookingRepository.findBookingsForReaderBySearch(any(), any(), any(), any()) } returns emptyPage
        every { bookingCardDataResolver.resolve(emptyList(), BookingType.FOR_ME) } returns emptyList()

        service.getMyBookings(reader.id!!, BookingType.FOR_ME, "aleph", 0, 4)

        verify { bookingRepository.findBookingsForReaderBySearch(eq(reader.id!!), eq("aleph"), any(), any()) }
    }
}
