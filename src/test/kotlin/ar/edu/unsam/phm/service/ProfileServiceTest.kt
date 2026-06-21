package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Role
import ar.edu.unsam.phm.domain.createOwner
import ar.edu.unsam.phm.domain.createReaderUser
import ar.edu.unsam.phm.dto.LoginResponse
import ar.edu.unsam.phm.dto.UpdateUserRequest
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProfileServiceTest {
    private val userService: UserService = mockk()
    private val bookingService: BookingService = mockk()
    private val loginService: LoginService = mockk()
    private val bookService: BookService = mockk()
    private val service = ProfileService().apply {
        this.userService = this@ProfileServiceTest.userService
        this.bookingService = this@ProfileServiceTest.bookingService
        this.loginService = this@ProfileServiceTest.loginService
        this.bookService = this@ProfileServiceTest.bookService
    }

    @BeforeEach
    fun setup() {
        every { userService.update(any()) } answers { firstArg() }
        every { bookService.evictBooksFromUser(any()) } just runs
        every { bookService.disableBooksFromUser(any()) } just runs
        every { bookService.enableBooksFromUser(any()) } just runs
        every { loginService.issueTokens(any(), any(), any(), any(), any()) } returns
            LoginResponse(1, null, "access", "refresh")
    }

    @Test
    fun `publisher becoming reader disables books and renews tokens`() {
        val user = createOwner()
        val result = service.updateUser(user, request(reader = true, publisher = false))

        assertTrue(result.tokensUpdated)
        assertTrue(user.role == Role.reader)
        verify { bookService.disableBooksFromUser(user.id!!) }
        verify { loginService.issueTokens(user.id!!, user.avatar, user.email, listOf("reader"), any()) }
    }

    @Test
    fun `reader becoming publisher enables books and renews tokens`() {
        val user = createReaderUser()
        val result = service.updateUser(user, request(reader = false, publisher = true))

        assertTrue(result.tokensUpdated)
        verify { bookService.enableBooksFromUser(user.id!!) }
    }

    @Test
    fun `unchanged role updates profile without renewing tokens`() {
        val user = createReaderUser()
        val result = service.updateUser(user, request(reader = true, publisher = false))

        assertFalse(result.tokensUpdated)
        verify(exactly = 0) { loginService.issueTokens(any(), any(), any(), any(), any()) }
    }

    private fun request(reader: Boolean, publisher: Boolean) = UpdateUserRequest(
        firstName = "Ana",
        lastName = "Garcia",
        description = "Perfil actualizado",
        phone = "1122334455",
        city = "Rosario",
        avatar = "avatar.png",
        isReader = reader,
        isPublisher = publisher
    )
}
