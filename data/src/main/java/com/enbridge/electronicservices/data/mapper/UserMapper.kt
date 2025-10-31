package com.enbridge.electronicservices.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.data.dto.LoginResponseDto
import com.enbridge.electronicservices.domain.entity.User

fun LoginResponseDto.toDomain(): User {
    return User(
        id = userId,
        username = username,
        token = token
    )
}
