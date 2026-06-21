package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.errors.ForbiddenException
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.ReviewRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.Optional

class ReviewServiceTest {

    private val bookingRepository: BookingRepository = mockk(relaxed = true)
    private val bookRepository:    BookRepository    = mockk()
    private val reviewRepository:  ReviewRepository  = mockk()
    private val bookService:       BookService       = mockk()
    private lateinit var service:  ReviewService

    private lateinit var owner:  User
    private lateinit var reader: User
    private lateinit var book:   Book

    @BeforeEach
    fun setup() {
        service = ReviewService(bookingRepository, bookRepository, reviewRepository, bookService)
        every { bookService.evictBookCache(any()) } returns Unit

        owner = User(
            firstName    = "Ana",
            lastName     = "Garcia",
            email        = "ana@mail.com",
            username     = "anita",
            password     = "1234",
            avatar       = "avatar.jpg",
            phone        = "1122334455",
            city         = "Buenos Aires",
            description  = "Owner",
            bibliokarmas = 800,
            createdAt    = LocalDate.of(2023, 1, 1),
            role         = Role.reader_publisher
        )
        owner.id = 1L

        reader = User(
            firstName    = "Pedro",
            lastName     = "Lopez",
            email        = "pedro@mail.com",
            username     = "pedrito",
            password     = "1234",
            avatar       = "avatar2.jpg",
            phone        = "1166666666",
            city         = "Buenos Aires",
            description  = "Reader",
            bibliokarmas = 500,
            createdAt    = LocalDate.of(2023, 1, 1),
            role         = Role.reader
        )
        reader.id = 10L

        book = CommonBook(
            title           = "El Aleph",
            image           = "imagen.jpg",
            description     = "Cuentos fantasticos",
            genre           = Genre.classic_literature,
            author          = "Jorge Luis Borges",
            pageCount       = 224,
            isbn13          = "978-0-06-093528-6",
            language        = Language.spanish,
            publisher       = "Emece",
            publicationDate = LocalDate.of(1949, 1, 1),
            condition       = BookCondition.good,
            ownerId         = owner.id!!
        )
        book.id = "book-1"
    }

    @Test
    fun `reviewBooking creates a review if none existed for user and book`() {
        val booking = Booking(
            book   = book,
            reader = reader,
            from   = LocalDate.now().minusDays(10),
            to     = LocalDate.now().minusDays(2)
        )
        booking.id = 1L

        val savedReview = slot<Review>()
        val newReview = Review(readerId = 10L, bookId = "book-1", score = 5, comment = "Excelente")
            .also { it.id = 99L }

        every { bookingRepository.findById(1L) } returns Optional.of(booking)
        every {
            bookingRepository.findReturnedBookingsForReaderAndBook(10L, "book-1", any())
        } returns listOf(booking)
        every { bookService.getBookById("book-1") } returns book
        every { reviewRepository.findByReaderIdAndBookId(10L, "book-1") } returns null
        every { reviewRepository.save(capture(savedReview)) } returns newReview
        every { reviewRepository.averageScoreByBookId("book-1") } returns 5.0
        every { reviewRepository.findLatestByBookId("book-1", any<Pageable>()) } returns listOf(newReview)
        every { bookRepository.save(any<Book>()) } answers { firstArg() }

        service.reviewBooking(1, reader.id!!, 5, "Excelente")

        assertEquals(5, savedReview.captured.score)
        assertEquals("Excelente", savedReview.captured.comment)
        assertEquals(5.0, book.ranking)
        assertEquals(1, book.lastReviews.size)
        verify(exactly = 1) { bookRepository.save(book) }
    }

    @Test
    fun `reviewBooking updates existing review if user already reviewed the book`() {
        val booking = Booking(
            book   = book,
            reader = reader,
            from   = LocalDate.now().minusDays(10),
            to     = LocalDate.now().minusDays(2)
        )
        booking.id = 1L

        val existingReview = Review(readerId = 10L, bookId = "book-1", score = 3, comment = "Regular")
            .also { it.id = 50L }

        every { bookingRepository.findById(1L) } returns Optional.of(booking)
        every {
            bookingRepository.findReturnedBookingsForReaderAndBook(10L, "book-1", any())
        } returns listOf(booking)
        every { bookService.getBookById("book-1") } returns book
        every { reviewRepository.findByReaderIdAndBookId(10L, "book-1") } returns existingReview
        every { reviewRepository.save(existingReview) } returns existingReview
        every { reviewRepository.averageScoreByBookId("book-1") } returns 5.0
        every { reviewRepository.findLatestByBookId("book-1", any<Pageable>()) } returns listOf(existingReview)
        every { bookRepository.save(any<Book>()) } answers { firstArg() }

        service.reviewBooking(1, reader.id!!, 5, "Excelente")

        assertEquals(5, existingReview.score)
        assertEquals("Excelente", existingReview.comment)
        assertEquals(5.0, book.ranking)
        verify(exactly = 1) { bookRepository.save(book) }
    }

    @Test
    fun `reviewBooking fails when booking does not exist`() {
        every { bookingRepository.findById(99L) } returns Optional.empty()

        assertThrows(NotFoundException::class.java) {
            service.reviewBooking(99, reader.id!!, 5, "Excelente")
        }
    }

    @Test
    fun `reviewBooking rejects a user that does not own the booking`() {
        val booking = Booking(
            book = book,
            reader = reader,
            from = LocalDate.now().minusDays(10),
            to = LocalDate.now().minusDays(2)
        ).also { it.id = 1L }
        every { bookingRepository.findById(1L) } returns Optional.of(booking)

        assertThrows(ForbiddenException::class.java) {
            service.reviewBooking(1, 999L, 5, "Excelente")
        }

        verify(exactly = 0) { reviewRepository.save(any<Review>()) }
    }

    @Test
    fun `reviewBooking fails when booking is not latest returned booking`() {
        val booking = Booking(
            book = book,
            reader = reader,
            from = LocalDate.now().minusDays(10),
            to = LocalDate.now().minusDays(2)
        )
        booking.id = 1L

        val latestBooking = Booking(
            book = book,
            reader = reader,
            from = LocalDate.now().minusDays(5),
            to = LocalDate.now().minusDays(1)
        )
        latestBooking.id = 2L

        every { bookingRepository.findById(1L) } returns Optional.of(booking)
        every { bookService.getBookById("book-1") } returns book
        every {
            bookingRepository.findReturnedBookingsForReaderAndBook(10L, "book-1", any())
        } returns listOf(latestBooking, booking)

        assertThrows(BusinessException::class.java) {
            service.reviewBooking(1, reader.id!!, 5, "Excelente")
        }

        verify(exactly = 0) { bookRepository.save(any<Book>()) }
        verify(exactly = 0) { reviewRepository.save(any<Review>()) }
    }
}
