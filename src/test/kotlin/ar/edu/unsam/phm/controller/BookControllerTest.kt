package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.createCommonBook
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.dto.BookDetailDto
import ar.edu.unsam.phm.dto.BookDetailWithoutOwnerDto
import ar.edu.unsam.phm.dto.BookPageDto
import ar.edu.unsam.phm.dto.BookRequest
import ar.edu.unsam.phm.dto.IdResponse
import ar.edu.unsam.phm.mapper.toBookDetailDto
import ar.edu.unsam.phm.mapper.toBookDetailWithoutOwnerDto
import ar.edu.unsam.phm.service.BookClickService
import ar.edu.unsam.phm.service.BookService
import ar.edu.unsam.phm.service.UserService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication

class BookControllerTest {

    private val bookService:      BookService      = mockk()
    private val bookClickService: BookClickService = mockk()
    private val userService:      UserService      = mockk()
    private val authentication:   Authentication  = mockk()

    private lateinit var controller: BookController

    private val owner  = createOwner()       // id = 1L
    private val reader = createReaderUser()  // id = 2L
    private val book   = createCommonBook(owner = owner)

    @BeforeEach
    fun setup() {
        controller = BookController(bookService, bookClickService, userService)
        every { authentication.name } returns reader.id.toString()
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    fun `getById retorna el BookDetailDto del libro`() {
        val expected: BookDetailDto = mockk()
        every { bookService.getBookById(book.id!!) } returns book
        every { userService.findById(owner.id!!) } returns owner

        val result = controller.getById(book.id!!)

        assertEquals(book.toBookDetailDto(owner), result)
    }

    // ── getByIdForDetail ──────────────────────────────────────────────────────

    @Test
    fun `getByIdForDetail retorna el BookDetailWithoutOwnerDto`() {
        every { bookService.getBookForView(book.id!!) } returns book

        val result = controller.getByIdForDetail(book.id!!)

        assertEquals(book.toBookDetailWithoutOwnerDto(), result)
    }

    // ── getBookReviews ────────────────────────────────────────────────────────

    @Test
    fun `getBookReviews retorna lista vacia cuando no hay resenas`() {
        every { bookService.getBookReviews(book.id!!, false) } returns emptyList()
        every { userService.findAllById(emptyList()) } returns emptyList()

        val result = controller.getBookReviews(book.id!!, false)

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `getBookReviews usa onlyLatest cuando se indica`() {
        every { bookService.getBookReviews(book.id!!, true) } returns emptyList()
        every { userService.findAllById(emptyList()) } returns emptyList()

        controller.getBookReviews(book.id!!, true)

        verify { bookService.getBookReviews(book.id!!, true) }
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    fun `create crea el libro con el owner del token y retorna su id`() {
        val req: BookRequest = mockk()
        every { userService.findById(reader.id!!) } returns reader
        every { bookService.create(req, reader) } returns book

        val result = controller.create(req, authentication)

        assertEquals(IdResponse(book.id!!), result)
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    fun `update actualiza el libro y retorna su id`() {
        val req: BookRequest = mockk()
        val updated = createCommonBook(owner = owner).apply { id = "book-updated" }
        every { userService.findById(reader.id!!) } returns reader
        every { bookService.getBookById(book.id!!) } returns book
        every { bookService.update(req, reader, book) } returns updated

        val result = controller.update(book.id!!, req, authentication)

        assertEquals(IdResponse("book-updated"), result)
    }

    // ── getUserBooks ──────────────────────────────────────────────────────────

    @Test
    fun `getUserBooks delega al service con los parametros correctos`() {
        val expected: BookPageDto = mockk()
        every { bookService.getUserBooks(reader.id!!, 1, 3, "none", "asc", "All") } returns expected

        val result = controller.getUserBooks(1, 3, "none", "asc", "All", authentication)

        assertEquals(expected, result)
    }

    // ── deleteUserBook ────────────────────────────────────────────────────────

    @Test
    fun `deleteUserBook delega al service con userId y bookId`() {
        every { bookService.deleteBookOfUser(reader.id!!, book.id!!) } just runs

        controller.deleteUserBook(book.id!!, authentication)

        verify { bookService.deleteBookOfUser(reader.id!!, book.id!!) }
    }

    // ── registerClick ─────────────────────────────────────────────────────────

    @Test
    fun `registerClick registra el click con el username del usuario`() {
        every { userService.findById(reader.id!!) } returns reader
        every { bookClickService.registerClick(book.id!!, reader.username) } just runs

        controller.registerClick(book.id!!, authentication)

        verify { bookClickService.registerClick(book.id!!, reader.username) }
    }

    // ── getClickStats ─────────────────────────────────────────────────────────

    @Test
    fun `getClickStats retorna las estadisticas de clicks del usuario`() {
        val stats = listOf(BookClickCountDto(id = book.id!!, clickCount = 5L))
        every { bookClickService.getClickStatsByOwner(reader.id!!) } returns stats

        val result = controller.getClickStats(authentication)

        assertEquals(stats, result)
    }
}
