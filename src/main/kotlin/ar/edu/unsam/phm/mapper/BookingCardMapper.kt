package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.booking.BookingCardDto
import ar.edu.unsam.phm.dto.booking.BookingPageDto
import ar.edu.unsam.phm.dto.booking.BookingStatus
import ar.edu.unsam.phm.dto.booking.BookingType
import ar.edu.unsam.phm.dto.booking.UserReviewDto

data class BookingCardData(
    val booking:    Booking,
    val bookOwner:  User,
    val reader:     User,
    val userReview: Review? = null,
    val canReview:  Boolean = false
)

fun toBookingCardDto(data: BookingCardData, type: BookingType): BookingCardDto {
    val booking = data.booking

    val (personLabel, personName) =
        if (type == BookingType.FOR_ME)
            "Prestado por" to "${data.bookOwner.firstName} ${data.bookOwner.lastName}"
        else
            "Prestado a"   to "${data.reader.firstName} ${data.reader.lastName}"

    val rating = booking.bookRating

    val status = when {
        booking.wasReturned()                            -> BookingStatus.RETURNED
        booking.isActive() && booking.isAboutToExpire() -> BookingStatus.EXPIRING_SOON
        booking.isActive()                               -> BookingStatus.ACTIVE
        else -> throw IllegalStateException("Booking ${booking.id} has no valid status for BookingStatus")
    }

    return BookingCardDto(
        bookingId    = booking.id!!,
        title        = booking.bookTitle,
        author       = booking.bookAuthor,
        image        = booking.bookImage,
        personLabel  = personLabel,
        personName   = personName,
        startDate    = booking.from,
        endDate      = booking.to,
        bibliokarmas = booking.bibliokarmas,
        rating       = rating,
        status       = status,
        canReview    = data.canReview,
        hasReview    = data.userReview != null,
        userReview   = data.userReview?.let { UserReviewDto(it.score, it.comment) }
    )
}

fun toBookingPageDto(
    items:      List<BookingCardData>,
    type:       BookingType,
    page:       Int,
    size:       Int,
    totalItems: Int,
    totalPages: Int
): BookingPageDto =
    BookingPageDto(
        items       = items.map { toBookingCardDto(it, type) },
        page        = page,
        size        = size,
        totalItems  = totalItems,
        totalPages  = totalPages,
        hasNext     = page + 1 < totalPages,
        hasPrevious = page > 0
)
