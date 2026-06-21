package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.UpdateUserRequest
import ar.edu.unsam.phm.dto.UpdateProfileResponse
import ar.edu.unsam.phm.dto.UserResponse
import ar.edu.unsam.phm.dto.UserSummaryDto
import ar.edu.unsam.phm.mapper.toUserDto
import ar.edu.unsam.phm.service.ProfileService
import ar.edu.unsam.phm.service.BookingService
import ar.edu.unsam.phm.service.UserService
import jakarta.validation.Valid
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ProfileController(
    private val profileService: ProfileService,
    private val userService:    UserService,
    private val bookingService: BookingService
) {
    @GetMapping("/profile")
    fun getUserProfile(authentication: Authentication): UserResponse {
        val userId    = authentication.name.toLong()
        val user      = userService.findById(userId)
        val readBooks = bookingService.readBookCountByUser(userId)
        val lentBooks = bookingService.lentBookCountByUser(userId)
        return toUserDto(user, readBooks, lentBooks)
    }

    @GetMapping("/profile/summary")
    fun getUserSummary(authentication: Authentication): UserSummaryDto {
        val userId = authentication.name.toLong()
        val user   = userService.findById(userId)
        return UserSummaryDto(
            avatar       = user.avatar,
            bibliokarmas = user.bibliokarmas
        )
    }

    @PutMapping("/profile")
    fun updateUserProfile(
        @Valid @RequestBody req: UpdateUserRequest,
        authentication: Authentication
    ): UpdateProfileResponse {
        val userId = authentication.name.toLong()
        val user   = userService.findById(userId)
        return profileService.updateUser(user, req)
    }
}
