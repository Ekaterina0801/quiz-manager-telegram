package com.Quiz_manager.service

import SignInRequest
import SignUpRequest
import com.Quiz_manager.domain.User
import com.Quiz_manager.dto.response.JwtAuthenticationResponse
import com.Quiz_manager.enums.Role
import com.Quiz_manager.service.UserService
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException


@Service
@RequiredArgsConstructor
class AuthenticationService {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager


    fun signUp(request: SignUpRequest): JwtAuthenticationResponse {

        if (userService.findByUsername(request.username) != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Пользователь с именем '${request.username}' уже существует"
            )
        }
        if (userService.findByEmail(request.email) != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Пользователь с email '${request.email}' уже зарегистрирован"
            )
        }
        val newUser = User(
            username = request.username,
            email    = request.email,
            fullname = request.fullname,
            password = passwordEncoder.encode(request.password),
            role     = Role.MODERATOR
        )
        userService.findOrCreateUser(newUser)

        val accessToken  = jwtService.generateAccessToken(newUser)
        val refreshToken = jwtService.generateRefreshToken(newUser)
        val refreshTokenExpiresAt = System.currentTimeMillis() + JwtService.REFRESH_TOKEN_EXPIRATION

        return JwtAuthenticationResponse(
            accessToken          = accessToken,
            refreshToken         = refreshToken,
            refreshTokenExpiresAt = refreshTokenExpiresAt
        )
    }



    fun signIn(request: SignInRequest): JwtAuthenticationResponse {
        val user: User

        try {
            val authToken = UsernamePasswordAuthenticationToken(request.username, request.password)
            authenticationManager.authenticate(authToken)
            user = userService.findByUsername(request.username)
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден")
        } catch (ex: BadCredentialsException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль")
        }

        val accessToken = jwtService.generateAccessToken(user)
        val refreshToken = jwtService.generateRefreshToken(user)
        val refreshTokenExpiresAt = System.currentTimeMillis() + JwtService.REFRESH_TOKEN_EXPIRATION

        return JwtAuthenticationResponse(accessToken, refreshToken, refreshTokenExpiresAt)
    }


    fun refreshAccessToken(refreshToken: String?): JwtAuthenticationResponse {
        val username = jwtService.extractUserName(refreshToken!!)
        val user: User? = userService.findByUsername(username)

        if (jwtService.isTokenValid(refreshToken, user!!)) {
            val newAccessToken = jwtService.generateAccessToken(user)

            val refreshTokenExpiresAt = jwtService.extractExpiration(refreshToken).time

            return JwtAuthenticationResponse(newAccessToken, refreshToken, refreshTokenExpiresAt)
        } else {
            throw RuntimeException("Invalid refresh token")
        }
    }
}
