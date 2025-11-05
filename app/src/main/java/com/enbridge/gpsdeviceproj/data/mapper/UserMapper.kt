package com.enbridge.gpsdeviceproj.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.data.dto.LoginResponseDto
import com.enbridge.gpsdeviceproj.domain.entity.User

fun LoginResponseDto.toDomain(): User {
    return User(
        id = userId,
        username = username,
        token = token
    )
}
