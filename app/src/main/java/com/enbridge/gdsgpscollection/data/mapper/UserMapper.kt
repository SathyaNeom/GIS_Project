package com.enbridge.gdsgpscollection.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.data.dto.LoginResponseDto
import com.enbridge.gdsgpscollection.domain.entity.User

fun LoginResponseDto.toDomain(): User {
    return User(
        id = userId,
        username = username,
        token = token
    )
}
