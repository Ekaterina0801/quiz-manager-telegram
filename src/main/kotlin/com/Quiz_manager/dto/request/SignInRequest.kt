

import jakarta.validation.constraints.Size

data class SignInRequest(
    val username: String,

    @field:Size(min = 8, message = "Не меньше 8 знаков")
    val password: String,
)