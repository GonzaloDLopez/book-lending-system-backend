package ar.edu.unsam.phm.security

import ar.edu.unsam.phm.errors.InvalidCredentialsException
import ar.edu.unsam.phm.errors.TokenExpiredException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TokenUtilsTest {
    private fun tokenUtils(minutes: Int = 30) = TokenUtils().apply {
        secretKey = "booklibre-test-secret-key-with-32-chars"
        accessTokenMinutes = minutes
        refreshTokenDays = 9
    }

    @Test
    fun `malformed tokens are rejected as invalid credentials`() {
        assertFailsWith<InvalidCredentialsException> {
            tokenUtils().getAuthentication("not-a-jwt")
        }
    }

    @Test
    fun `valid token restores subject and roles`() {
        val utils = tokenUtils()
        val token = utils.createToken(42, listOf("reader", "publisher"))!!

        val authentication = utils.getAuthentication(token)

        assertEquals("42", authentication.name)
        assertEquals(setOf("reader", "publisher"), authentication.authorities.map { it.authority }.toSet())
    }

    @Test
    fun `expired token is reported separately`() {
        val utils = tokenUtils(-1)
        val token = utils.createToken(42, listOf("reader"))!!

        assertFailsWith<TokenExpiredException> { utils.getAuthentication(token) }
    }

    @Test
    fun `refresh tokens are random and expiration is configurable`() {
        val utils = tokenUtils()
        val first = utils.generateRefreshToken()
        val second = utils.generateRefreshToken()

        assertNotEquals(first, second)
        assertTrue(first.length >= 40)
        assertEquals(9, utils.getRefreshTokenExpirationDays())
    }

    @Test
    fun `hash is deterministic and changes with input`() {
        val utils = tokenUtils()
        assertEquals(utils.hashToken("same"), utils.hashToken("same"))
        assertNotEquals(utils.hashToken("same"), utils.hashToken("different"))
    }
}
