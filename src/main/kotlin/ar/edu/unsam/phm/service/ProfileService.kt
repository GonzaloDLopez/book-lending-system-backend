package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.UpdateProfileResponse
import ar.edu.unsam.phm.dto.UpdateUserRequest
import ar.edu.unsam.phm.mapper.updateUserFromDto
import ar.edu.unsam.phm.validator.UserValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileService {
    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var bookingService: BookingService

    @Autowired
    lateinit var loginService: LoginService

    @Autowired
    lateinit var bookService: BookService

    @Transactional
    fun updateUser(user: User, req: UpdateUserRequest): UpdateProfileResponse {
        val phone = req.phone.trim()
        UserValidator.validateFields(req, phone)

        val oldRole = user.role

        updateUserFromDto(user, req)
        userService.update(user)
        bookService.evictBooksFromUser(user.id!!)

        val newRole = user.role

        if (oldRole.canPublish() && !newRole.canPublish()) {
            bookService.disableBooksFromUser(user.id!!)
        }

        if (!oldRole.canPublish() && newRole.canPublish()) {
            bookService.enableBooksFromUser(user.id!!)
        }

        // Si el rol cambió, generamos nuevos tokens con el rol actualizado
        if (oldRole != newRole) {
            val loginResponse = loginService.issueTokens(
                userId    = user.id!!,
                avatarImg = user.avatar,
                email     = user.email,
                roles     = listOf(newRole.name)
            )
            return UpdateProfileResponse(
                tokensUpdated = true,
                token         = loginResponse.token,
                refreshToken  = loginResponse.refreshToken
            )
        }

        return UpdateProfileResponse(tokensUpdated = false)
    }

}

data class UserProfile(
    val user:      User,
    val readBooks: Int,
    val lentBooks: Int
)
