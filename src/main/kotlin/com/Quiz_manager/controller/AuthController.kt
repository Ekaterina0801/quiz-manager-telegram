package com.Quiz_manager.controller




import SignInRequest
import SignUpRequest
import com.Quiz_manager.dto.request.RefreshTokenRequest
import com.Quiz_manager.dto.response.JwtAuthenticationResponse
import com.Quiz_manager.service.AuthenticationService
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {
    @Autowired
    private lateinit var authenticationService: AuthenticationService

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: @Valid SignUpRequest): JwtAuthenticationResponse {
        return authenticationService.signUp(request)
    }

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: @Valid SignInRequest): JwtAuthenticationResponse {
        return authenticationService.signIn(request)
    }

    @PostMapping("/refresh")
    fun refreshAccessToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<JwtAuthenticationResponse> {
        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.refreshToken))
    }

}

