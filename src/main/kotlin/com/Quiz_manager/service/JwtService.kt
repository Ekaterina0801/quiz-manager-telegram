package com.Quiz_manager.service

import com.Quiz_manager.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Function
import javax.crypto.SecretKey


@Service
class JwtService {
    @Value("\${token.signing.key}")
    private val jwtSigningKey: String? = null

    fun extractUserName(token: String): String {
        return extractClaim(token) { obj: Claims -> obj.subject }
    }

    fun generateToken(user: User, expiration: Long): String {
        val claims: MutableMap<String, Any?> = HashMap()
        claims["id"] = user.id
        claims["email"] = user.email
        claims["role"] = user.role
        return generateToken(claims, user, expiration)
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val userName = extractUserName(token)
        return (userName == userDetails.username) && !isTokenExpired(token)
    }

    fun generateResetPasswordToken(user: User): String {
        val claims: MutableMap<String, Any?> = HashMap()
        claims["id"] = user.id
        return generateToken(claims, user, RESET_PASSWORD_TOKEN_EXPIRATION)
    }

    private fun <T> extractClaim(token: String, claimsResolvers: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolvers.apply(claims)
    }

    fun validateResetPasswordToken(token: String, email: String): Boolean {
        val tokenEmail = extractClaim(
            token
        ) { obj: Claims -> obj.subject }
        return tokenEmail == email && !isTokenExpired(token)
    }

    fun generateAccessToken(user: User): String {
        return generateToken(HashMap(), user, ACCESS_TOKEN_EXPIRATION)
    }

    fun generateRefreshToken(user:User): String {
        return generateToken(HashMap(), user, REFRESH_TOKEN_EXPIRATION)
    }

    private fun generateToken(claims: Map<String, Any?>, user: User, expiration: Long): String {

        val updatedClaims = claims.toMutableMap()
        updatedClaims["role"] = user.role
        return Jwts.builder()
            .setClaims(updatedClaims)
            .setSubject(user.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey)
            .compact()
    }


    // Проверка токена на просроченность
    fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    // Извлечение даты истечения токена
    fun extractExpiration(token: String): Date {
        return extractClaim(token) { obj: Claims -> obj.expiration }
    }


    // Извлечение всех данных из токена
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).payload
    }

    private val signingKey: SecretKey
        // Получение ключа для подписи токена
        get() {
            val keyBytes = Decoders.BASE64.decode(jwtSigningKey)
            return Keys.hmacShaKeyFor(keyBytes)
        }

    companion object {
        /*private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15 минут
    public static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 дней
    private static final long RESET_PASSWORD_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1 час
*/
        private const val ACCESS_TOKEN_EXPIRATION = (1000 * 60 * 10 * 10 // 100 минута
                ).toLong()
        const val REFRESH_TOKEN_EXPIRATION: Long = (1000 * 60 * 2 * 10 * 10 // 200 минуты
                ).toLong()
        private const val RESET_PASSWORD_TOKEN_EXPIRATION = (1000 * 60 * 60 // 1 час
                ).toLong()
    }
}
