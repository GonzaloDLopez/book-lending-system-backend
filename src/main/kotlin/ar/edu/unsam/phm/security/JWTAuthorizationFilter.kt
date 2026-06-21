package ar.edu.unsam.phm.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ar.edu.unsam.phm.errors.TokenExpiredException
import ar.edu.unsam.phm.errors.InvalidCredentialsException
import ar.edu.unsam.phm.repository.UserRepository

@Component
class JWTAuthorizationFilter : OncePerRequestFilter() {

    @Autowired
    lateinit var tokenUtils: TokenUtils

    @Autowired
    lateinit var userRepository: UserRepository

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val bearerToken = request.getHeader("Authorization")
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                val token = bearerToken.substringAfter("Bearer ")
                val authentication = tokenUtils.getAuthentication(token)
                val userId = authentication.name.toLong()
                userRepository.findById(userId)
                    .orElseThrow { InvalidCredentialsException("User not found") }
                SecurityContextHolder.getContext().authentication = authentication
            }
            filterChain.doFilter(request, response)
        } catch (e: TokenExpiredException) {
            logger.warn("Expired token: ${e.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\", error_description=\"The access token expired\"")
            response.contentType = "application/json"
            response.writer.write("{\"error\":\"Token expired\",\"message\":\"${e.message}\"}")
            // No llamamos a filterChain.doFilter() — el token vencido es un error definitivo
        } catch (e: InvalidCredentialsException) {
            logger.warn("Invalid token: ${e.message}")
            SecurityContextHolder.clearContext()
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"")
            response.contentType = "application/json"
            response.writer.write("{\"error\":\"Invalid token\",\"message\":\"Authentication failed\"}")
        }
    }
}
