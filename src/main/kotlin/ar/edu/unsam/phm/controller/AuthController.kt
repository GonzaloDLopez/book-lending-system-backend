package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.LoginRequest
import ar.edu.unsam.phm.dto.LoginResponse
import ar.edu.unsam.phm.dto.RefreshResponse
import ar.edu.unsam.phm.dto.RefreshTokenRequest
import ar.edu.unsam.phm.dto.RegisterRequest
import ar.edu.unsam.phm.service.LoginService
import ar.edu.unsam.phm.service.RegisterService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val loginService:    LoginService,
    private val registerService: RegisterService,
) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): LoginResponse =
        loginService.authenticate(req)

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): LoginResponse =
        registerService.createDefaultUser(req)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshTokenRequest): RefreshResponse =
        loginService.refreshAccessToken(req)
}
