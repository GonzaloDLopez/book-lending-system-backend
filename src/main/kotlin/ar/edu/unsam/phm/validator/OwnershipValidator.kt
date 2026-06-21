package ar.edu.unsam.phm.validator

import ar.edu.unsam.phm.errors.ForbiddenException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class OwnershipValidator {

    /**
     * Verifica que el usuario autenticado sea el dueño del recurso.
     * Lee el userId directamente del token — sin query a la BD.
     * Lanza ForbiddenException si no coinciden.
     */
    fun validate(authentication: Authentication, id: Long) {
        val userIdFromToken = authentication.name.toLong()
        if (userIdFromToken != id) throw ForbiddenException("No tenés permiso para realizar esta acción")
    }
}