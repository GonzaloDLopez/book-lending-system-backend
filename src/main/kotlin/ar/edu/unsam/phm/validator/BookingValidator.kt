package ar.edu.unsam.phm.validator

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.errors.BusinessException
import java.time.LocalDate

object BookingValidator {

    fun validatePagination(page: Int, size: Int) {
        if (page < 0) throw BusinessException("La pagina no puede ser negativa")
        if (size <= 0) throw BusinessException("El size debe ser mayor a 0")
    }

    fun validateBooking(book: Book, user: User, from: LocalDate, to: LocalDate) {
        if (!book.active || book.deleted) {
            throw BusinessException("El libro no esta disponible.")
        }
        if (book.ownerId == user.id) {
            throw BusinessException("No podes reservar tu propio libro.")
        }
        if (!user.canBook()) {
            throw BusinessException("No tenes permiso para reservar libros.")
        }
        if (from.isBefore(LocalDate.now())) {
            throw BusinessException("La fecha de inicio no puede estar en el pasado.")
        }
        if (to.isBefore(from)) {
            throw BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.")
        }
    }
}
