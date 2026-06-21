package ar.edu.unsam.phm.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import ar.edu.unsam.phm.errors.InvalidCredentialsException
import ar.edu.unsam.phm.errors.TokenExpiredException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Component
class TokenUtils {

    @Value("\${security.secret-key}")
    lateinit var secretKey: String

    @Value("\${security.access-token-minutes}")
    var accessTokenMinutes: Int = 30

    @Value("\${security.refresh-token-days}")
    var refreshTokenDays: Int = 7

    fun createToken(userId: Long, roles: List<String>): String? {
        val longExpirationTime = accessTokenMinutes.minutes.inWholeMilliseconds
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + longExpirationTime))
            .claim("roles", roles)
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()
    }

    fun generateRefreshToken(): String {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun getRefreshTokenExpirationDays(): Int = refreshTokenDays

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest((token + secretKey).toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        try {
            val secret = Keys.hmacShaKeyFor(secretKey.toByteArray())
            val claims = Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .payload

            if (claims.subject == null || claims.subject.isBlank()) {
                throw InvalidCredentialsException()
            }

            val roleClaims = claims["roles"] as? List<*>
                ?: throw InvalidCredentialsException("Invalid token")
            val roles = roleClaims.map { SimpleGrantedAuthority(it.toString()) }
            return UsernamePasswordAuthenticationToken(claims.subject, null, roles)
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException("Session expired")
        } catch (e: JwtException) {
            throw InvalidCredentialsException("Invalid token")
        } catch (e: IllegalArgumentException) {
            throw InvalidCredentialsException("Invalid token")
        }
    }
}
