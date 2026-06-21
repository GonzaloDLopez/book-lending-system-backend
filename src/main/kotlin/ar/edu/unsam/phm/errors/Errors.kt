package ar.edu.unsam.phm.errors

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BusinessException(msg: String) : RuntimeException(msg)

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(msg: String) : RuntimeException(msg)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException(msg: String = "Usuario o contraseña incorrectos") : RuntimeException(msg)

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(msg: String) : RuntimeException(msg)
