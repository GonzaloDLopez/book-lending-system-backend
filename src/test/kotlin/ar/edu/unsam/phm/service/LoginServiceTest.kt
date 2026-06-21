package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.RefreshToken
import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.RefreshTokenRequest
import ar.edu.unsam.phm.errors.InvalidCredentialsException
import ar.edu.unsam.phm.repository.RefreshTokenRepository
import ar.edu.unsam.phm.security.TokenUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.Optional

class LoginServiceTest {
    private val userService: UserService = mockk()
    private val tokenUtils: TokenUtils = mockk()
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val service = LoginService(userService, tokenUtils, refreshTokenRepository)

    @Test
    fun `refresh rota el token bloqueado y conserva el vencimiento absoluto`() {
        val user = createReaderUser()
        val absoluteExpiration = LocalDateTime.now().plusDays(20)
        val stored = validRefreshToken(user.email, absoluteExpiration)

        every { tokenUtils.hashToken("old-token") } returns "old-hash"
        every { refreshTokenRepository.findByTokenHashForUpdate("old-hash") } returns Optional.of(stored)
        every { userService.findByEmail(user.email) } returns user
        every { tokenUtils.createToken(user.id!!, listOf(user.role.name)) } returns "new-access"
        every { tokenUtils.generateRefreshToken() } returns "new-refresh"
        every { tokenUtils.hashToken("new-refresh") } returns "new-hash"
        every { tokenUtils.getRefreshTokenExpirationDays() } returns 7
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        val result = service.refreshAccessToken(RefreshTokenRequest("old-token"))

        assertEquals("new-access", result.token)
        assertEquals("new-refresh", result.refreshToken)
        assertTrue(stored.revoked)
        verify { refreshTokenRepository.findByTokenHashForUpdate("old-hash") }
        verify(exactly = 0) { refreshTokenRepository.findByTokenHash(any()) }
        verify { refreshTokenRepository.save(match { it.tokenHash == "new-hash" && it.absoluteExpiration == absoluteExpiration }) }
    }

    @Test
    fun `refresh rechaza un token revocado sin emitir credenciales nuevas`() {
        val stored = validRefreshToken("reader@mail.com", LocalDateTime.now().plusDays(20)).apply {
            revoked = true
        }
        every { tokenUtils.hashToken("revoked") } returns "revoked-hash"
        every { refreshTokenRepository.findByTokenHashForUpdate("revoked-hash") } returns Optional.of(stored)

        assertThrows<InvalidCredentialsException> {
            service.refreshAccessToken(RefreshTokenRequest("revoked"))
        }

        verify(exactly = 0) { tokenUtils.createToken(any(), any()) }
    }

    private fun validRefreshToken(email: String, absoluteExpiration: LocalDateTime) = RefreshToken().apply {
        tokenHash = "old-hash"
        this.email = email
        expirationDate = LocalDateTime.now().plusDays(5)
        this.absoluteExpiration = absoluteExpiration
    }
}
