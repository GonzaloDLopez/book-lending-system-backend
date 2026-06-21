package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.BookCondition
import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.domain.CommonBook
import ar.edu.unsam.phm.domain.Genre
import ar.edu.unsam.phm.domain.Language
import ar.edu.unsam.phm.domain.Role
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.ActivityEventType
import ar.edu.unsam.phm.dto.BookCreatedActivityDto
import ar.edu.unsam.phm.dto.BookingConfirmedActivityDto
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ActivityFeedServiceTest {

    private val bookRepository: BookRepository = mockk()
    private val bookingRepository: BookingRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val service = ActivityFeedService(bookRepository, bookingRepository, userRepository)

    @Test
    fun `getRecentActivity unifica libros y reservas ordenados por fecha descendente y devuelve top 5`() {
        val owner = user(1, "ana")
        val reader = user(2, "gonza")

        val bookNewest = book("book-newest", owner.id!!, "Libro nuevo", LocalDate.of(2026, 6, 18))
        val bookOldest = book("book-oldest", owner.id!!, "Libro viejo", LocalDate.of(2026, 6, 10))
        val bookingA = booking(10, bookNewest, reader, LocalDate.of(2026, 6, 17))
        val bookingB = booking(11, bookNewest, reader, LocalDate.of(2026, 6, 16))
        val bookingC = booking(12, bookNewest, reader, LocalDate.of(2026, 6, 15))
        val bookingD = booking(13, bookNewest, reader, LocalDate.of(2026, 6, 14))

        every { bookRepository.findTop5ByActiveTrueAndDeletedFalseOrderByCreatedAtDescIdDesc() } returns listOf(bookNewest, bookOldest)
        every { bookingRepository.findRecentConfirmedBookings(any()) } returns listOf(bookingA, bookingB, bookingC, bookingD)
        every { userRepository.findAllById(match<Iterable<Long>> { it.toSet() == setOf(1L, 2L) }) } returns listOf(owner, reader)

        val result = service.getRecentActivity()

        assertEquals(5, result.size)
        assertEquals(
            listOf(
                ActivityEventType.BOOK_CREATED,
                ActivityEventType.BOOKING_CONFIRMED,
                ActivityEventType.BOOKING_CONFIRMED,
                ActivityEventType.BOOKING_CONFIRMED,
                ActivityEventType.BOOKING_CONFIRMED,
            ),
            result.map { it.tipoEvento }
        )
        assertEquals(
            listOf(
                "2026-06-18",
                "2026-06-17",
                "2026-06-16",
                "2026-06-15",
                "2026-06-14",
            ),
            result.map { it.fecha }
        )
        assertTrue(result.first() is BookCreatedActivityDto)
        assertTrue(result[1] is BookingConfirmedActivityDto)
        verify { userRepository.findAllById(match<Iterable<Long>> { it.toSet() == setOf(1L, 2L) }) }
    }

    @Test
    fun `getRecentActivity ignora eventos cuyo usuario ya no existe`() {
        val owner = user(1, "ana")
        val bookWithOwner = book("book-1", owner.id!!, "Libro visible", LocalDate.of(2026, 6, 18))
        val bookWithoutOwner = book("book-2", 99, "Libro huerfano", LocalDate.of(2026, 6, 19))

        every { bookRepository.findTop5ByActiveTrueAndDeletedFalseOrderByCreatedAtDescIdDesc() } returns listOf(bookWithoutOwner, bookWithOwner)
        every { bookingRepository.findRecentConfirmedBookings(any()) } returns emptyList()
        every { userRepository.findAllById(match<Iterable<Long>> { it.toSet() == setOf(1L, 99L) }) } returns listOf(owner)

        val result = service.getRecentActivity()

        assertEquals(1, result.size)
        assertEquals("ana", result.single().usuario.username)
        assertEquals("book-1", (result.single() as BookCreatedActivityDto).bookId)
    }

    private fun user(id: Long, username: String): User =
        User(
            firstName = username.replaceFirstChar { it.uppercase() },
            lastName = "Tester",
            email = "$username@mail.com",
            username = username,
            password = "1234",
            avatar = "",
            phone = "",
            city = "Buenos Aires",
            description = "",
            bibliokarmas = 0,
            createdAt = LocalDate.of(2025, 1, 1),
            role = Role.reader_publisher,
        ).apply { this.id = id }

    private fun book(id: String, ownerId: Long, title: String, createdAt: LocalDate): Book =
        CommonBook(
            title = title,
            image = "",
            description = "Descripcion",
            genre = Genre.drama,
            author = "Autor",
            pageCount = 100,
            isbn13 = "isbn-$id",
            language = Language.spanish,
            publisher = "Editorial",
            publicationDate = LocalDate.of(2000, 1, 1),
            condition = BookCondition.good,
            ownerId = ownerId,
        ).apply {
            this.id = id
            this.createdAt = createdAt
        }

    private fun booking(id: Long, book: Book, reader: User, createdAt: LocalDate): Booking =
        Booking(
            book = book,
            reader = reader,
            from = LocalDate.of(2026, 7, 1),
            to = LocalDate.of(2026, 7, 7),
            createdAt = createdAt,
        ).apply { this.id = id }
}
