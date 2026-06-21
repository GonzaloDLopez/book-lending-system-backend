package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.RefreshToken
import ar.edu.unsam.phm.dto.LoginRequest
import ar.edu.unsam.phm.dto.LoginResponse
import ar.edu.unsam.phm.dto.RefreshResponse
import ar.edu.unsam.phm.dto.RefreshTokenRequest
import ar.edu.unsam.phm.errors.InvalidCredentialsException
import ar.edu.unsam.phm.repository.RefreshTokenRepository
import ar.edu.unsam.phm.security.TokenUtils
import ar.edu.unsam.phm.validator.UserValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LoginService(
    private val userService:             UserService,
    private val tokenUtils:              TokenUtils,
    private val refreshTokenRepository:  RefreshTokenRepository
) {
    fun authenticate(req: LoginRequest): LoginResponse {
        val user = UserValidator.validateCredentials(
            userService.findByEmail(req.email.trim()),
            req.password
        )
        return issueTokens(user.id!!, user.avatar, user.email, listOf(user.role.name))
    }

    @Transactional
    fun refreshAccessToken(req: RefreshTokenRequest): RefreshResponse {
        val tokenHash = tokenUtils.hashToken(req.refreshToken)
        val refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
            .orElseThrow { InvalidCredentialsException("Refresh token not found") }

        if (!refreshToken.isValid()) {
            throw InvalidCredentialsException("Refresh token expired or revoked")
        }

        val absoluteExpiration = refreshToken.absoluteExpiration

        refreshToken.revoked = true
        refreshTokenRepository.save(refreshToken)

        val user = userService.findByEmail(refreshToken.email)
            ?: throw InvalidCredentialsException("User not found")

        val loginResponse = issueTokens(user.id!!, user.avatar, user.email, listOf(user.role.name), absoluteExpiration)

        return RefreshResponse(
            token        = loginResponse.token,
            refreshToken = loginResponse.refreshToken
        )
    }

    internal fun issueTokens(
        userId:              Long,
        avatarImg:           String?,
        email:               String,
        roles:               List<String>,
        absoluteExpiration:  LocalDateTime = LocalDateTime.now().plusDays(30)
    ): LoginResponse {
        val accessToken = tokenUtils.createToken(userId, roles)
            ?: throw InvalidCredentialsException("Error generating token")
        val refreshTokenString = tokenUtils.generateRefreshToken()
        val refreshToken = RefreshToken().apply {
            tokenHash              = tokenUtils.hashToken(refreshTokenString)
            this.email             = email
            expirationDate         = LocalDateTime.now().plusDays(tokenUtils.getRefreshTokenExpirationDays().toLong())
            this.absoluteExpiration = absoluteExpiration
        }
        refreshTokenRepository.save(refreshToken)
        return LoginResponse(
            userId       = userId,
            avatarImg    = avatarImg,
            token        = accessToken,
            refreshToken = refreshTokenString
        )
    }
}
