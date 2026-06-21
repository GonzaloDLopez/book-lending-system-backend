package ar.edu.unsam.phm.errors

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiError(val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(error: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = error.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "La solicitud contiene datos invalidos"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(error: IllegalArgumentException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(error.message ?: "La solicitud contiene datos invalidos"))
}
