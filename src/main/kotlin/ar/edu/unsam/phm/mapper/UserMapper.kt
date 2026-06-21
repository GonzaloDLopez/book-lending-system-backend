package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Role
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.OwnerDto
import ar.edu.unsam.phm.dto.UpdateUserRequest
import ar.edu.unsam.phm.dto.UserResponse

fun toUserDto(user: User, readBooks: Int = 0, lentBooks: Int = 0): UserResponse =
    UserResponse(
        firstName       = user.firstName,
        lastName        = user.lastName,
        email           = user.email,
        username        = user.username,
        avatar          = user.avatar,
        phone           = user.phone,
        city            = user.city,
        description     = user.description,
        bibliokarmas    = user.bibliokarmas,
        createdAt       = user.createdAt,
        reader          = user.role == Role.reader || user.role == Role.reader_publisher,
        publisher       = user.role == Role.publisher || user.role == Role.reader_publisher,
        readerPublisher = user.role == Role.reader_publisher,
        lentBooks       = lentBooks,
        readBooks       = readBooks
    )

fun User.toOwnerDto() = OwnerDto(
    id        = requireNotNull(this.id),
    firstName = this.firstName,
    lastName  = this.lastName,
    username  = this.username,
)

fun updateUserFromDto(user : User, req: UpdateUserRequest) : User{
    user.firstName   = req.firstName.trim()
    user.lastName    = req.lastName.trim()
    user.description = req.description.trim()
    user.phone       = req.phone.trim()
    user.city        = req.city.trim()
    user.avatar      = req.avatar
    user.role        = when {
        req.isReader && req.isPublisher -> Role.reader_publisher
        req.isReader                    -> Role.reader
        else                            -> Role.publisher
    }
    return user
}