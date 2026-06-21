package ar.edu.unsam.phm.controller

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
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class BookingController(
    private val bookingService:   BookingService,
    private val bookService:      BookService,
    private val userService:      UserService,
    private val bookClickService: BookClickService,
) {
    private val log = LoggerFactory.getLogger(BookingController::class.java)

    @GetMapping("/my-bookings")
    fun getMyBookings(
        @RequestParam type: BookingType,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "4") size: Int,
        authentication: Authentication
    ): BookingPageDto {
        val userId = authentication.name.toLong()
        return bookingService.getMyBookings(userId, type, search, page, size)
    }

    @PostMapping("/filtered-bookings")
    fun getFilteredBookings(
        @RequestBody filters: BookingFiltersRequest,
        @RequestParam(defaultValue = "0")     page:   Int,
        @RequestParam(defaultValue = "6")     size:   Int,
        @RequestParam(defaultValue = "title") sortBy: BookSortBy,
        authentication: Authentication
    ): FilteredBookingsPageDto {
        val user = userService.findById(authentication.name.toLong())
        val (requestedUsername, ownerId) = resolveOwnerId(filters)
        
        if (requestedUsername != null && ownerId == null) return FilteredBookingsPageDto.empty(page)

        if (sortBy == BookSortBy.popularity && page == 0) {
            bookClickService.tryServeFromRedis(user, filters)?.let { redisItems ->
                val total = bookService.countFilteredBooks(filters, user.id!!)
                log.info("[POPULARITY] userId=${user.id} -> REDIS page=0 items=${redisItems.size} total=$total")
                return FilteredBookingsPageDto.of(redisItems, total, size, page)
            }
        }

        val booksPage = bookService.getFilteredPage(filters, user.id!!, ownerId, page, size, sortBy)
        val owners    = userService.findAllById(booksPage.content.map { it.ownerId }.distinct()).associateBy { it.id!! }
        bookClickService.updateBookCaches(booksPage.content, owners)
        
        return bookingService.possibleBookingsList(user, booksPage, owners, filters, page)
    }

    private fun resolveOwnerId(filters: BookingFiltersRequest): Pair<String?, Long?> {
        val username = filters.ownerUsername?.takeIf { it.isNotBlank() }
        return username to username?.let { userService.findByUsername(it)?.id }
    }

    @PostMapping("/books/{bookId}/confirm-booking")
    fun confirmBooking(
        @PathVariable bookId: String,
        @Valid @RequestBody body: ConfirmBookingRequest,
        authentication: Authentication
    ) {
        val userId = authentication.name.toLong()
        val book   = bookService.getBookById(bookId)
        val user   = userService.findById(userId)
        bookingService.confirmBooking(book, user, body.from, body.to)
        val owner  = userService.findById(book.ownerId)
        bookClickService.updateBookCaches(listOf(book), mapOf(book.ownerId to owner))
    }
}
