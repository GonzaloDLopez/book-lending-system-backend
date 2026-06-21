package ar.edu.unsam.phm.validator

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.UpdateUserRequest
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.errors.UnauthorizedException
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder

object UserValidator {

    private fun encoder() = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()

    /**
     * Validates registration business rules.
     * Throws ConflictException if the email is already registered.
     */
    fun validateRegistration(emailExists: Boolean) {
        if (emailExists) {
            throw ConflictException("Si el email no está registrado, recibirás un correo para completar el registro.")
        }
    }

    /**
     * Validates login credentials.
     * Throws UnauthorizedException if user does not exist or password is incorrect.
     * Returns the validated user to avoid !! in the service.
     */
    fun validateCredentials(user: User?, password: String): User {
        if (user == null) throw UnauthorizedException()
        if (!encoder().matches(password, user.password)) throw UnauthorizedException()
        return user
    }

    fun validateFields(req: UpdateUserRequest, phone: String) {
        require(req.firstName.isNotBlank())     { "El nombre no puede estar vacío" }
        require(req.lastName.isNotBlank())       { "El apellido no puede estar vacío" }
        require(phone.isNotBlank())              { "El celular no puede estar vacío" }
        require(phone.all { it.isDigit() })     { "El celular debe contener solo números" }
        require(phone.length <= 10)              { "El celular no puede tener más de 10 cifras" }
        require(req.city.isNotBlank())           { "La ciudad no puede estar vacía" }
        require(req.isReader || req.isPublisher) { "El usuario debe ser al menos lector o publicador" }
    }

    /**
     * Hashea una password en crudo antes de persistirla.
     */
    fun hashPassword(rawPassword: String): String = encoder().encode(rawPassword)
}