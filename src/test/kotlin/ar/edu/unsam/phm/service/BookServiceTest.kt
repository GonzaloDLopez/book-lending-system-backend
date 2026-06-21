package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.*
import ar.edu.unsam.phm.dto.BookRequest
import ar.edu.unsam.phm.dto.BookSortBy
import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.dto.BookingFiltersRequest
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.ReviewRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.repository.mongo.BookClickRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.repository.redis.BookRankingRepository
import ar.edu.unsam.phm.service.notification.BookingCancelledNotifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.Optional

class BookServiceTest {

    private val bookRepository:           BookRepository           = mockk()
    private val bookingRepository:        BookingRepository        = mockk()
    private val reviewRepository:         ReviewRepository         = mockk()
    private val bookingCancelledNotifier: BookingCancelledNotifier = mockk(relaxed = true)
    private val bookClickRepository:      BookClickRepository      = mockk()
    private val bookRankingRepository:    BookRankingRepository    = mockk(relaxed = true)
    private val userRepository:           UserRepository           = mockk()
    private lateinit var bookService: BookService

    private lateinit var owner:  User
    private lateinit var reader: User
    private lateinit var book:   Book

    @BeforeEach
    fun setup() {
        bookService = BookService(bookRepository, bookingRepository, reviewRepository, bookingCancelledNotifier, bookClickRepository, bookRankingRepository, userRepository)

        owner = User(
            firstName    = "Ana",
            lastName     = "Garcia",
            email        = "ana@mail.com",
            username     = "anita",
            password     = "1234",
            avatar       = "avatar.jpg",
            phone        = "1122334455",
            city         = "Buenos Aires",
            description  = "Lectora",
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
            description  = "Lector",
            bibliokarmas = 500,
            createdAt    = LocalDate.of(2023, 1, 1),
            role         = Role.reader
        )
        reader.id = 2L

        book = CommonBook(
            title           = "El Aleph",
            image           = "https://sitio.com/ElAleph.jpg",
            description     = "Cuentos de Borges",
            genre           = Genre.classic_literature,
            author          = "Jorge Luis Borges",
            pageCount       = 224,
            isbn13          = "978-9500400011",
            language        = Language.spanish,
            publisher       = "Emece",
            publicationDate = LocalDate.of(1949, 1, 1),
            condition       = BookCondition.good,
            ownerId         = owner.id!!
        )
        book.id = "book-1"
    }

    @Test
    fun `getBookById returns the correct book`() {
        every { bookRepository.findById("book-1") } returns Optional.of(book)

        val result = bookService.getBookById("book-1")

        assertEquals(book, result)
    }

    @Test
    fun `getBookById throws NotFoundException when book does not exist`() {
        every { bookRepository.findById("book-99") } returns Optional.empty()

        assertThrows(NotFoundException::class.java) {
            bookService.getBookById("book-99")
        }
    }

    @Test
    fun `getBookReviews returns all reviews from repository when onlyLatest is false`() {
        val reviews = listOf(
            Review(reader.id!!, "book-1", 5, "Excelente"),
            Review(3L, "book-1", 4, "Muy bueno"),
            Review(4L, "book-1", 3, "Bueno")
        )
        every { reviewRepository.findAllByBookId("book-1") } returns reviews

        val result = bookService.getBookReviews("book-1", false)

        assertEquals(3, result.size)
    }

    @Test
    fun `getBookReviews returns last 2 reviews stored in book when onlyLatest is true`() {
        book.lastReviews.add(Review(3L, "book-1", 4, "Muy bueno"))
        book.lastReviews.add(Review(4L, "book-1", 3, "Bueno"))

        every { bookRepository.findById("book-1") } returns Optional.of(book)

        val result = bookService.getBookReviews("book-1", true)

        assertEquals(2, result.size)
        assertEquals("Muy bueno", result[0].comment)
        assertEquals("Bueno", result[1].comment)
    }

    @Test
    fun `getBookReviews throws NotFoundException when onlyLatest is true and book does not exist`() {
        every { bookRepository.findById("book-99") } returns Optional.empty()

        assertThrows(NotFoundException::class.java) {
            bookService.getBookReviews("book-99", true)
        }
    }

    @Test
    fun `getBookReviews returns empty list when book has no reviews`() {
        every { reviewRepository.findAllByBookId("book-1") } returns emptyList()

        val result = bookService.getBookReviews("book-1", false)

        assertEquals(0, result.size)
    }

    @Test
    fun `getBookReviews with onlyLatest true returns whatever is stored in the book`() {
        book.lastReviews.add(Review(reader.id!!, "book-1", 5, "Excelente"))

        every { bookRepository.findById("book-1") } returns Optional.of(book)

        val result = bookService.getBookReviews("book-1", true)

        assertEquals(1, result.size)
    }

    @Test
    fun `create persiste un libro nuevo`() {
        val req = BookRequest(
            title = "Rayuela",
            image = "https://sitio.com/rayuela.jpg",
            description = "Novela",
            genre = "classic_literature",
            author = "Julio Cortazar",
            pageCount = 600,
            isbn13 = "978-9500400017",
            language = "spanish",
            publisher = "Sudamericana",
            publicationDate = "1963-06-28",
            condition = "good",
            type = "common"
        )

        every { bookRepository.save(any<Book>()) } answers { firstArg() }

        val result = bookService.create(req, owner)

        assertEquals("Rayuela", result.title)
        assertEquals(owner.id, result.ownerId)
    }

    @Test
    fun `update actualiza un libro propio`() {
        val req = BookRequest(
            title = "El Aleph Editado",
            image = "https://sitio.com/ElAleph.jpg",
            description = "Nueva descripcion",
            genre = "classic_literature",
            author = "Jorge Luis Borges",
            pageCount = 230,
            isbn13 = "978-9500400011",
            language = "spanish",
            publisher = "Emece",
            publicationDate = "1950-01-01",
            condition = "very_good",
            type = "common"
        )

        every { bookRepository.save(any<Book>()) } answers { firstArg() }

        val result = bookService.update(req, owner, book)

        assertEquals("El Aleph Editado", result.title)
        assertEquals("https://sitio.com/ElAleph.jpg", result.image)
        assertEquals(BookCondition.very_good, result.condition)
    }

    @Test
    fun `update lanza excepcion cuando el usuario no es el owner`() {
        val req = BookRequest(
            title = "El Aleph Editado",
            image = "https://sitio.com/ElAleph.jpg",
            description = "Nueva descripcion",
            genre = "classic_literature",
            author = "Jorge Luis Borges",
            pageCount = 230,
            isbn13 = "978-9500400011",
            language = "spanish",
            publisher = "Emece",
            publicationDate = "1950-01-01",
            condition = "very_good",
            type = "common"
        )

        assertThrows(BusinessException::class.java) {
            bookService.update(req, reader, book)
        }
    }

    // ── getFilteredPage ───────────────────────────────────────────────────────

    @Test
    fun `getFilteredPage con sortBy title llama a filteredBooks con pageable`() {
        val filters   = BookingFiltersRequest()
        val booksPage = PageImpl(listOf(book))

        every { bookRepository.findFilteredBooks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns booksPage

        val result = bookService.getFilteredPage(filters, owner.id!!, null, 0, 6, BookSortBy.title)

        assertEquals(1, result.totalElements)
        verify(exactly = 0) { bookClickRepository.findAllByClickCount() }
    }

    @Test
    fun `getFilteredPage con sortBy popularity llama a filteredBooksOrderedByPopularity`() {
        val filters   = BookingFiltersRequest()
        val booksPage = PageImpl(listOf(book))

        every { bookRepository.findFilteredBooks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns booksPage
        every { bookClickRepository.findAllByClickCount() } returns listOf(BookClickCountDto(book.id!!, 5L))

        val result = bookService.getFilteredPage(filters, owner.id!!, null, 0, 6, BookSortBy.popularity)

        assertEquals(1, result.totalElements)
        verify { bookClickRepository.findAllByClickCount() }
    }

    @Test
    fun `getFilteredPage con popularity ordena por clicks descendentes`() {
        val book2 = CommonBook(
            title = "Neuromancer", description = "", genre = Genre.science_fiction,
            author = "Gibson", pageCount = 300, isbn13 = "978-0000000002",
            language = Language.english, publisher = "Ace", publicationDate = LocalDate.of(1984, 1, 1),
            condition = BookCondition.good, ownerId = owner.id!!, image = ""
        ).apply { id = "book-2" }

        val filters   = BookingFiltersRequest()
        val booksPage = PageImpl(listOf(book, book2))

        every { bookRepository.findFilteredBooks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns booksPage
        every { bookClickRepository.findAllByClickCount() } returns listOf(
            BookClickCountDto("book-2", 10L),
            BookClickCountDto("book-1", 5L)
        )

        val result = bookService.getFilteredPage(filters, owner.id!!, null, 0, 6, BookSortBy.popularity)

        assertEquals("book-2", result.content[0].id)
        assertEquals("book-1", result.content[1].id)
    }

    @Test
    fun `getFilteredPage con owner ordena por username`() {
        val zBook = createCommonBook(owner = owner).apply { id = "book-z" }
        val aBook = createCommonBook(owner = reader).apply { id = "book-a" }
        val booksPage = PageImpl<Book>(listOf(aBook, zBook))

        every { bookRepository.findFilteredBooks(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns booksPage
        every { userRepository.findAllById(any<Iterable<Long>>()) } returns listOf(owner, reader)

        val result = bookService.getFilteredPage(BookingFiltersRequest(), owner.id!!, null, 0, 6, BookSortBy.owner)

        assertEquals(listOf("book-z", "book-a"), result.content.map { it.id })
    }
}
