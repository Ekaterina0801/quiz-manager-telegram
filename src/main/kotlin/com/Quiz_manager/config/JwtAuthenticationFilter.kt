package com.Quiz_manager.config


import com.Quiz_manager.service.JwtService
import com.Quiz_manager.service.UserService
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.micrometer.common.util.StringUtils
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@RequiredArgsConstructor
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService
) : OncePerRequestFilter() {

    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/api/auth/sign-up")
                || uri.startsWith("/api/auth/sign-in")
                || uri.startsWith("/api/auth/refresh")
                || uri.startsWith("/api/auth/forgot-password")
                || uri.startsWith("/api/auth/reset-password")
                || uri.startsWith("/api/whatsapp")
    }



    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        if (request.requestURI.startsWith("/api/auth/sign-up") || request.requestURI.startsWith("/api/auth/sign-in")) {
            println("✅ Пропускаем без проверки: " + request.requestURI)
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader(HEADER_NAME)
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Authorization token is missing")
            return
        }

        val jwt = authHeader.substring(BEARER_PREFIX.length)
        var username: String?
        var tokenExpired = false

        try {
            username = jwtService.extractUserName(jwt)
        } catch (e: ExpiredJwtException) {
            tokenExpired = true
            username = e.claims.subject
        } catch (e: JwtException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid access token")
            return
        }

        if (!username.isNullOrEmpty() && SecurityContextHolder.getContext().authentication == null) {
            val user = userService.findByUsername(username) ?: run {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User not found")
                return
            }

            if (!tokenExpired && jwtService.isTokenValid(jwt, user)) {
                authenticateUser(user, request)
            } else if (tokenExpired) {
                val refreshToken = request.getHeader(REFRESH_HEADER_NAME)
                if (StringUtils.isEmpty(refreshToken)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Missing refresh token")
                    return
                }

                try {
                    if (jwtService.isTokenValid(refreshToken, user)) {
                        val newAccessToken = jwtService.generateAccessToken(user)
                        response.setHeader("Authorization", "Bearer $newAccessToken")
                        authenticateUser(user, request)
                    } else {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid refresh token")
                        return
                    }
                } catch (e: JwtException) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid refresh token")
                    return
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun authenticateUser(userDetails: UserDetails, request: HttpServletRequest) {
        val context = SecurityContextHolder.createEmptyContext()
        val authToken = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        context.authentication = authToken
        SecurityContextHolder.setContext(context)
    }

    companion object {
        const val BEARER_PREFIX = "Bearer "
        const val HEADER_NAME = "Authorization"
        private const val REFRESH_HEADER_NAME = "Refresh-Token"
    }
}
