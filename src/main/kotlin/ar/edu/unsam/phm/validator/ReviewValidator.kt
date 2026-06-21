package ar.edu.unsam.phm.validator

import ar.edu.unsam.phm.domain.Booking
import ar.edu.unsam.phm.errors.BusinessException

object ReviewValidator {

    fun validateReviewRequest(score: Int, comment: String) {
        if (score !in 1..5) throw BusinessException("El puntaje debe estar entre 1 y 5")
        if (comment.isBlank()) throw BusinessException("El comentario no puede estar vacio")
    }

    fun validateReturnedBooking(booking: Booking) {
        if (!booking.wasReturned()) {
            throw BusinessException("No podes reseñar una reserva que no fue devuelta")
        }
    }

    fun validateLatestReturnedBooking(latestReturned: Booking?, currentBooking: Booking) {
        if (latestReturned?.id != currentBooking.id) {
            throw BusinessException("Solo podes reseñar o editar la reseña desde tu ultima reserva devuelta de este libro")
        }
    }

    fun canReview(latestReturned: Booking?, currentBooking: Booking): Boolean =
        latestReturned?.id == currentBooking.id
}
