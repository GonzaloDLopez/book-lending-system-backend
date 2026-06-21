package ar.edu.unsam.phm.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentialsException(message: String = "Invalid credentials") : RuntimeException(message)