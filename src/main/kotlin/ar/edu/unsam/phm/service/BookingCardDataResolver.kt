package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.dto.booking.BookingType
import ar.edu.unsam.phm.mapper.BookingCardData
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.ReviewRepository
import ar.edu.unsam.phm.repository.UserRepository
import ar.edu.unsam.phm.validator.ReviewValidator
import java.time.LocalDate
import org.springframework.stereotype.Service

@Service
class BookingCardDataResolver(
    private val userRepository:    UserRepository,
    private val bookingRepository: BookingRepository,
    private val reviewRepository:  ReviewRepository
) {
    fun resolve(bookings: List<Booking>, type: BookingType): List<BookingCardData> {
        if (bookings.isEmpty()) return emptyList()

        val bookIds = bookings.map { it.bookId }.distinct()

        val userIds = (bookings.map { it.readerId } + bookings.map { it.ownerId }).distinct()
        val users   = userRepository.findAllById(userIds).associateBy { it.id!! }

        val isForMe  = type == BookingType.FOR_ME
        val readerId = bookings.first().readerId

        val latestReturnedByBookId = if (isForMe)
            bookingRepository
                .findLatestReturnedBookingsForUserAndBooks(readerId, bookIds, LocalDate.now())
                .associateBy { it.bookId }
        else
            emptyMap()

        val userReviewsByBookId = if (isForMe)
            bookIds.mapNotNull { bookId ->
                reviewRepository.findByReaderIdAndBookId(readerId, bookId)?.let { bookId to it }
            }.toMap()
        else
            emptyMap()

        return bookings.map { booking ->
            val owner  = users[booking.ownerId]  ?: error("Owner ${booking.ownerId} no encontrado")
            val reader = users[booking.readerId] ?: error("Reader ${booking.readerId} no encontrado")

            val userReview = if (isForMe) userReviewsByBookId[booking.bookId] else null
            val canReview  = isForMe && ReviewValidator.canReview(latestReturnedByBookId[booking.bookId], booking)

            BookingCardData(
                booking    = booking,
                bookOwner  = owner,
                reader     = reader,
                userReview = userReview,
                canReview  = canReview
            )
        }
    }
}
