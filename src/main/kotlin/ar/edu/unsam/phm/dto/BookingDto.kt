package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.domain.Genre
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.mapper.toBookDetailDto
import ar.edu.unsam.phm.mapper.toUserDto
import java.time.LocalDate
import jakarta.validation.constraints.FutureOrPresent
import kotlin.math.ceil

data class BookingDto(
    val book:         BookDetailDto,
    val user:         UserResponse,
    val from:         LocalDate,
    val to:           LocalDate,
    val bibliokarmas: Int
)

fun bookingToDto(booking: Booking, book: Book, owner: User, reader: User): BookingDto =
    BookingDto(
        book         = book.toBookDetailDto(owner),
        user         = toUserDto(reader),
        from         = booking.from,
        to           = booking.to,
        bibliokarmas = booking.bibliokarmas
    )

data class BookingFiltersRequest(
    val title:         String?       = null,
    val genre:         List<Genre>   = emptyList(),
    val minPages:      Int?          = null,
    val maxPages:      Int?          = null,
    val from:          LocalDate?    = null,
    val to:            LocalDate?    = null,
    val isbn13:        String?       = null,
    val author:        String?       = null,
    val ownerUsername: String?       = null
)

fun BookingFiltersRequest.hasActiveFilters(): Boolean =
    !title.isNullOrBlank()         ||
    !author.isNullOrBlank()        ||
    !isbn13.isNullOrBlank()        ||
    genre.isNotEmpty()             ||
    from     != null               ||
    to       != null               ||
    !ownerUsername.isNullOrBlank()

data class FilteredBookingsPageDto(
    val items:       List<BookingDto>,
    val page:        Int,
    val totalItems:  Long,
    val totalPages:  Int,
    val hasNext:     Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        fun empty(page: Int) = FilteredBookingsPageDto(
            items = emptyList(), 
            page = page,
            totalItems = 0L, 
            totalPages = 0,
            hasNext = false, 
            hasPrevious = false
        )

        fun of(items: List<BookingDto>, total: Long, size: Int, page: Int): FilteredBookingsPageDto {
            val totalPages = ceil(total / size.toDouble()).toInt()
            return FilteredBookingsPageDto(
                items = items, 
                page = page, 
                totalItems = total,
                totalPages = totalPages, 
                hasNext = totalPages > 1, 
                hasPrevious = page > 0
            )
        }
    }
}

enum class BookSortBy(val field: String) {
    title("title"),
    genre("genre"),
    owner(""),
    popularity("")
}

data class ConfirmBookingRequest(
    @field:FutureOrPresent
    val from:   LocalDate,
    @field:FutureOrPresent
    val to:     LocalDate
)
