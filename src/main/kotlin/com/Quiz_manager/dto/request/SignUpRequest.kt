

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class SignUpRequest(
    @field:Size(min = 3, message = "Не меньше 3 знаков")
    @field:NotBlank(message = "Имя пользователя не может быть пустым")
    val username: String,

    @field:NotBlank
    val fullname: String,

    @field:NotBlank(message = "Адрес электронной почты не может быть пустым")
    @field:Email(message = "Email адрес должен быть в формате user@example.com")
    val email: String,

    @field:Size(min = 8, message = "Не меньше 8 знаков")
    val password: String,

    @field:NotBlank(message = "Подтверждение пароля не может быть пустым")
    val confirm: String
)
