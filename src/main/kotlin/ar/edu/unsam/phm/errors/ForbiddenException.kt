package ar.edu.unsam.phm.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException(msg: String = "No tenés permiso para realizar esta acción") : RuntimeException(msg)
