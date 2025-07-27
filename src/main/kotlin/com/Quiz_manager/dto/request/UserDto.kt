package com.Quiz_manager.dto.request

import lombok.*
import java.util.*

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@RequiredArgsConstructor
class UserDto {
    val id: Long? = null

    val username: String? = null

    val fullname: String? = null

    val email: String?=null

    val password: String?=null;
}