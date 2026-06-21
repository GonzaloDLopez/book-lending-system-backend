package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.Review
import ar.edu.unsam.phm.errors.NotFoundException
import ar.edu.unsam.phm.errors.ForbiddenException
import ar.edu.unsam.phm.repository.BookingRepository
import ar.edu.unsam.phm.repository.ReviewRepository
import ar.edu.unsam.phm.repository.mongo.BookRepository
import ar.edu.unsam.phm.validator.ReviewValidator
import java.time.LocalDate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val bookingRepository: BookingRepository,
    private val bookRepository:    BookRepository,
    private val reviewRepository:  ReviewRepository,
    private val bookService:       BookService
) {
    @Transactional
    fun reviewBooking(bookingId: Long, authenticatedUserId: Long, score: Int, comment: String) {
        ReviewValidator.validateReviewRequest(score, comment)
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { NotFoundException("No se ha encontrado la reserva") }

        if (booking.readerId != authenticatedUserId) {
            throw ForbiddenException("No tenes permiso para reseñar esta reserva")
        }

        ReviewValidator.validateReturnedBooking(booking)
        val latestReturn = bookingRepository.findReturnedBookingsForReaderAndBook(
            readerId = booking.readerId,
            bookId   = booking.bookId,
            today    = LocalDate.now()
        ).firstOrNull()
        ReviewValidator.validateLatestReturnedBooking(latestReturn, booking)

        val book = bookService.getBookById(booking.bookId)
        upsertReview(booking.readerId, booking.bookId, score, comment)
        refreshBookReviewState(book)
    }

    private fun upsertReview(readerId: Long, bookId: String, score: Int, comment: String) {
        val existing = reviewRepository.findByReaderIdAndBookId(readerId, bookId)
        if (existing != null) {
            existing.update(score, comment)
            reviewRepository.save(existing)
        } else {
            reviewRepository.save(Review(readerId = readerId, bookId = bookId, score = score, comment = comment))
        }
    }

    private fun refreshBookReviewState(book: Book) {
        val bookId    = book.id!!
        val newAvg    = reviewRepository.averageScoreByBookId(bookId)
        val latestTwo = reviewRepository.findLatestByBookId(bookId, PageRequest.of(0, 2))

        book.applyReviewsUpdate(latestTwo, newAvg)
        bookingRepository.updateBookRatingForBook(bookId, newAvg)
        bookRepository.save(book)
        bookService.evictBookCache(bookId)
    }
}
