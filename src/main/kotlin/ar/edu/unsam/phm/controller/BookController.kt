package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.BookClickCountDto
import ar.edu.unsam.phm.dto.BookDetailDto
import ar.edu.unsam.phm.dto.BookDetailWithoutOwnerDto
import ar.edu.unsam.phm.dto.BookPageDto
import ar.edu.unsam.phm.dto.BookRequest
import ar.edu.unsam.phm.dto.IdResponse
import ar.edu.unsam.phm.dto.ReviewDetailDto
import ar.edu.unsam.phm.dto.toReviewDetailDto
import ar.edu.unsam.phm.mapper.toBookDetailDto
import ar.edu.unsam.phm.mapper.toBookDetailWithoutOwnerDto
import ar.edu.unsam.phm.service.BookClickService
import ar.edu.unsam.phm.service.BookService
import ar.edu.unsam.phm.service.UserService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/books")
class BookController(
    private val bookService:      BookService,
    private val bookClickService: BookClickService,
    private val userService:      UserService,
) {
    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): BookDetailDto {
        val book  = bookService.getBookById(id)
        val owner = userService.findById(book.ownerId)
        return book.toBookDetailDto(owner)
    }

    @GetMapping("/{id}/detail")
    fun getByIdForDetail(@PathVariable id: String): BookDetailWithoutOwnerDto =
        bookService.getBookForView(id).toBookDetailWithoutOwnerDto()

    @GetMapping("/{id}/reviews")
    fun getBookReviews(
        @PathVariable id: String,
        @RequestParam(defaultValue = "false") onlyLatest: Boolean
    ): List<ReviewDetailDto> {
        val reviews = bookService.getBookReviews(id, onlyLatest)
        val readers = userService.findAllById(reviews.map { it.readerId }.distinct()).associateBy { it.id!! }
        return reviews.mapNotNull { review ->
            readers[review.readerId]?.let { review.toReviewDetailDto(it) }
        }
    }

    @PostMapping
    fun create(
        @Valid @RequestBody req: BookRequest,
        authentication: Authentication
    ): IdResponse {
        val userId = authentication.name.toLong()
        val owner  = userService.findById(userId)
        val book   = bookService.create(req, owner)
        return IdResponse(book.id!!)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody req: BookRequest,
        authentication: Authentication
    ): IdResponse {
        val userId       = authentication.name.toLong()
        val owner        = userService.findById(userId)
        val existingBook = bookService.getBookById(id)
        val updatedBook  = bookService.update(req, owner, existingBook)
        return IdResponse(updatedBook.id!!)
    }

    @GetMapping("/user")
    fun getUserBooks(
        @RequestParam(defaultValue = "1")    page:      Int,
        @RequestParam(defaultValue = "3")    pageSize:  Int,
        @RequestParam(defaultValue = "none") field:     String,
        @RequestParam(defaultValue = "asc")  direction: String,
        @RequestParam(defaultValue = "All")  filter:    String,
        authentication: Authentication
    ): BookPageDto {
        val userId = authentication.name.toLong()
        return bookService.getUserBooks(userId, page, pageSize, field, direction, filter)
    }

    @DeleteMapping("/{bookId}")
    fun deleteUserBook(
        @PathVariable bookId: String,
        authentication: Authentication
    ) {
        val userId = authentication.name.toLong()
        bookService.deleteBookOfUser(userId, bookId)
    }

    @PostMapping("/{id}/click")
    fun registerClick(
        @PathVariable id: String,
        authentication: Authentication
    ) {
        val userId = authentication.name.toLong()
        val user   = userService.findById(userId)
        bookClickService.registerClick(id, user.username)
    }

    @GetMapping("/click-stats")
    fun getClickStats(authentication: Authentication): List<BookClickCountDto> {
        val userId = authentication.name.toLong()
        return bookClickService.getClickStatsByOwner(userId)
    }
}
