package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.UsernameGenerator
import ar.edu.unsam.phm.dto.LoginRequest
import ar.edu.unsam.phm.dto.LoginResponse
import ar.edu.unsam.phm.dto.RegisterRequest
import ar.edu.unsam.phm.factory.UserFactory
import ar.edu.unsam.phm.validator.UserValidator
import org.springframework.stereotype.Service

@Service
class RegisterService(
    private val userService:  UserService,
    private val loginService: LoginService
) {
    fun createDefaultUser(req: RegisterRequest): LoginResponse {
        UserValidator.validateRegistration(userService.emailExists(req.email))

        val username = UsernameGenerator.generate(req.suggestedUsername) {
            userService.usernameExists(it)
        }

        val user = UserFactory.createDefault(
            firstName = req.firstName,
            lastName  = req.lastName,
            email     = req.email,
            password  = UserValidator.hashPassword(req.password),
            username  = username,
        )

        userService.create(user)

        return loginService.authenticate(LoginRequest(email = req.email, password = req.password))
    }
}