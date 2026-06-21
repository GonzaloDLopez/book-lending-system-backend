package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Role
import ar.edu.unsam.phm.domain.User
import ar.edu.unsam.phm.dto.LoginRequest
import ar.edu.unsam.phm.dto.RegisterRequest
import ar.edu.unsam.phm.errors.ConflictException
import ar.edu.unsam.phm.validator.UserValidator
import ar.edu.unsam.phm.errors.UnauthorizedException
import ar.edu.unsam.phm.repository.RefreshTokenRepository
import ar.edu.unsam.phm.security.TokenUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AuthServiceTest {

    private val userService:            UserService            = mockk()
    private val tokenUtils:             TokenUtils             = mockk()
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private lateinit var loginService:    LoginService
    private lateinit var registerService: RegisterService

    private lateinit var existingUser: User

    @BeforeEach
    fun setup() {
        loginService    = LoginService(userService, tokenUtils, refreshTokenRepository)
        registerService = RegisterService(userService, loginService)

        existingUser = User(
            firstName    = "Ana",
            lastName     = "García",
            email        = "ana@mail.com",
            username     = "ana_garcia",
            password     = UserValidator.hashPassword("1234"),
            avatar       = "avatar.jpg",
            phone        = "1122334455",
            city         = "Buenos Aires",
            description  = "Lectora",
            bibliokarmas = 800,
            createdAt    = LocalDate.of(2023, 1, 1),
            role         = Role.reader
        )
        existingUser.id = 1L

        // Mock tokenUtils para que no falle al generar tokens
        every { tokenUtils.createToken(any<Long>(), any()) } returns "mocked-token"
        every { tokenUtils.generateRefreshToken() } returns "mocked-refresh-token"
        every { tokenUtils.hashToken(any()) } returns "mocked-hash"
        every { tokenUtils.getRefreshTokenExpirationDays() } returns 7
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
    }

    @Test
    fun `successful login returns userId and avatar`() {
        every { userService.findByEmail("ana@mail.com") } returns existingUser

        val result = loginService.authenticate(
            LoginRequest(email = "ana@mail.com", password = "1234")
        )

        assertEquals(1L, result.userId)
        assertEquals("avatar.jpg", result.avatarImg)
    }

    @Test
    fun `login with non-existent email throws UnauthorizedException`() {
        every { userService.findByEmail("notfound@mail.com") } returns null

        assertThrows(UnauthorizedException::class.java) {
            loginService.authenticate(
                LoginRequest(email = "notfound@mail.com", password = "1234")
            )
        }
    }

    @Test
    fun `login with wrong password throws UnauthorizedException`() {
        every { userService.findByEmail("ana@mail.com") } returns existingUser

        assertThrows(UnauthorizedException::class.java) {
            loginService.authenticate(
                LoginRequest(email = "ana@mail.com", password = "wrong")
            )
        }
    }

    @Test
    fun `login trims leading and trailing spaces from email`() {
        every { userService.findByEmail("ana@mail.com") } returns existingUser

        val result = loginService.authenticate(
            LoginRequest(email = "  ana@mail.com  ", password = "1234")
        )

        assertEquals(1L, result.userId)
    }

    @Test
    fun `createDefaultUser persists the user`() {
        every { userService.emailExists("new@mail.com") }    returns false
        every { userService.usernameExists("carlos_lopez") } returns false
        every { userService.create(any()) } answers {
            firstArg<User>().apply { id = 99L }
        }
        every { userService.findByEmail("new@mail.com") } returns User(
            firstName    = "Carlos",
            lastName     = "Lopez",
            email        = "new@mail.com",
            username     = "carlos_lopez",
            password     = UserValidator.hashPassword("1234"),
            avatar       = "",
            phone        = "",
            city         = "",
            description  = "",
            bibliokarmas = 0,
            createdAt    = LocalDate.of(2023, 1, 1),
            role         = Role.reader
        ).apply { id = 99L }

        registerService.createDefaultUser(
            RegisterRequest(
                firstName = "Carlos",
                lastName  = "Lopez",
                email     = "new@mail.com",
                password  = "1234"
            )
        )

        verify(exactly = 1) { userService.create(any()) }
    }

    @Test
    fun `createDefaultUser with duplicate email throws ConflictException`() {
        every { userService.emailExists("ana@mail.com") } returns true

        assertThrows(ConflictException::class.java) {
            registerService.createDefaultUser(
                RegisterRequest(
                    firstName = "Ana",
                    lastName  = "Garcia",
                    email     = "ana@mail.com",
                    password  = "1234"
                )
            )
        }
    }
}