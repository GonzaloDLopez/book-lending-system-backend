package ar.edu.unsam.phm.dto

import java.time.LocalDate
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserResponse(
    var firstName:       String,
    var lastName:        String,
    val email:           String,
    val username:        String,
    var avatar:          String,
    var phone:           String,
    var city:            String,
    var description:     String,
    var bibliokarmas:    Int,
    val createdAt:       LocalDate,
    val reader:          Boolean,
    val publisher:       Boolean,
    val readerPublisher: Boolean,
    val lentBooks:       Int,
    val readBooks:       Int,
)

data class OwnerDto(
    val id:        Long,
    val firstName: String,
    val lastName:  String,
    val username:  String,
)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email:    String,
    @field:NotBlank
    val password: String
)

data class LoginResponse(
    val userId:       Long,
    val avatarImg:    String?,
    val token:        String,
    val refreshToken: String
)

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String
)

data class RefreshResponse(
    val token:        String,
    val refreshToken: String
)

data class RegisterRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val firstName: String,
    @field:NotBlank
    @field:Size(max = 100)
    val lastName:  String,
    @field:Email
    @field:NotBlank
    val email:     String,
    @field:Size(min = 8, max = 128)
    val password:  String,
) {
    val suggestedUsername: String
        get() {
            val base = "$firstName $lastName"
                .trim()
                .lowercase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace("ñ", "n")
                .replace(Regex("[^a-z0-9\\s]"), "")
                .replace(Regex("\\s+"), "_")
            return base
        }
}

data class UpdateUserRequest(
    @field:NotBlank
    @field:Size(max = 100)
    var firstName:   String,
    @field:NotBlank
    @field:Size(max = 100)
    var lastName:    String,
    @field:Size(max = 1000)
    var description: String,
    @field:Pattern(regexp = "^[0-9]{1,10}$")
    var phone:       String,
    @field:NotBlank
    @field:Size(max = 100)
    var city:        String,
    @field:Size(max = 1000)
    var avatar:      String,
    var isReader:    Boolean,
    var isPublisher: Boolean
)

data class UpdateProfileResponse(
    val tokensUpdated: Boolean,
    val token:         String?  = null,
    val refreshToken:  String?  = null,
)

data class UserSummaryDto(
    val avatar:       String,
    val bibliokarmas: Int,
)
