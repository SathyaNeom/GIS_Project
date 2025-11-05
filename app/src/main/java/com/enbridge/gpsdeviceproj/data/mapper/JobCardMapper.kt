package com.enbridge.gpsdeviceproj.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.data.dto.JobCardDto
import com.enbridge.gpsdeviceproj.domain.entity.JobCard
import com.enbridge.gpsdeviceproj.domain.entity.JobStatus

fun JobCardDto.toDomain(): JobCard {
    return JobCard(
        id = id,
        address = address,
        municipality = municipality,
        status = status.toJobStatus(),
        serviceType = serviceType,
        connectionType = connectionType
    )
}

private fun String.toJobStatus(): JobStatus {
    return when (this.uppercase().replace(" ", "_")) {
        "ASSIGNED" -> JobStatus.ASSIGNED
        "IN_PROGRESS", "INPROGRESS" -> JobStatus.IN_PROGRESS
        "COMPLETED" -> JobStatus.COMPLETED
        else -> JobStatus.UNKNOWN
    }
}
