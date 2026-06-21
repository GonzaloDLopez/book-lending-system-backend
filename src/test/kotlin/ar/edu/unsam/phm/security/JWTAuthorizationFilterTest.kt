package ar.edu.unsam.phm.security

import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.errors.InvalidCredentialsException
import ar.edu.unsam.phm.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

class JWTAuthorizationFilterTest {
    private val tokenUtils: TokenUtils = mockk()
    private val userRepository: UserRepository = mockk()
    private val filter = JWTAuthorizationFilter().apply {
        this.tokenUtils = this@JWTAuthorizationFilterTest.tokenUtils
        this.userRepository = this@JWTAuthorizationFilterTest.userRepository
    }

    @AfterEach
    fun cleanup() = SecurityContextHolder.clearContext()

    @Test
    fun `valid bearer token authenticates and continues the chain`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer valid") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()
        val authentication = UsernamePasswordAuthenticationToken("2", null, emptyList())
        every { tokenUtils.getAuthentication("valid") } returns authentication
        every { userRepository.findById(2) } returns Optional.of(createReaderUser())

        filter.doFilter(request, response, chain)

        assertEquals(authentication, SecurityContextHolder.getContext().authentication)
        assertEquals(200, response.status)
        assertEquals(request, chain.request)
    }

    @Test
    fun `missing bearer token leaves request anonymous and continues`() {
        val request = MockHttpServletRequest()
        val chain = MockFilterChain()
        filter.doFilter(request, MockHttpServletResponse(), chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        assertEquals(request, chain.request)
    }

    @Test
    fun `invalid token returns 401 and does not continue`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer invalid") }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()
        every { tokenUtils.getAuthentication("invalid") } throws InvalidCredentialsException("invalid")

        filter.doFilter(request, response, chain)

        assertEquals(401, response.status)
        assertNull(chain.request)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `token for deleted user returns 401`() {
        val request = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer orphan") }
        val response = MockHttpServletResponse()
        val authentication = UsernamePasswordAuthenticationToken("99", null, emptyList())
        every { tokenUtils.getAuthentication("orphan") } returns authentication
        every { userRepository.findById(99) } returns Optional.empty()

        filter.doFilter(request, response, MockFilterChain())

        assertEquals(401, response.status)
    }
}
