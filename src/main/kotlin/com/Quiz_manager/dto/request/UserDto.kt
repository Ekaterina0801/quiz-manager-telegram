package com.Quiz_manager.dto.request

import lombok.*
import java.util.*

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
class UserDto {
    private val id: UUID? = null

    private val username: String? = null

    private val fullname: String? = null

    private val email: String?=null

}