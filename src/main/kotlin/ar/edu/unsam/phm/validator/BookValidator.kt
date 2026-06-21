package ar.edu.unsam.phm.validator

import ar.edu.unsam.phm.domain.Book
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.errors.BusinessException

object BookValidator {

    fun validateOwner(book: Book, owner: User) {
        if (book.ownerId != owner.id) {
            throw BusinessException("No está permitido editar libros ajenos.")
        }
    }
}
