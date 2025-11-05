package com.enbridge.gpsdeviceproj.data.mapper

import com.enbridge.gpsdeviceproj.data.dto.WorkOrderDto
import com.enbridge.gpsdeviceproj.domain.entity.WorkOrder
import javax.inject.Inject

/**
 * Mapper class for converting between WorkOrderDto and WorkOrder domain entity
 */
class WorkOrderMapper @Inject constructor() {

    /**
     * Convert single WorkOrderDto to domain entity
     */
    fun mapToDomain(dto: WorkOrderDto): WorkOrder {
        return WorkOrder(
            id = dto.id,
            workOrderNumber = dto.workOrderNumber,
            address = dto.address,
            poleType = dto.poleType,
            distance = dto.distance,
            displayText = dto.displayText
        )
    }

    /**
     * Convert list of WorkOrderDto to list of domain entities
     */
    fun mapToDomain(dtoList: List<WorkOrderDto>): List<WorkOrder> {
        return dtoList.map { mapToDomain(it) }
    }
}

/**
 * Extension function to convert WorkOrderDto to domain WorkOrder entity
 */
fun WorkOrderDto.toDomain(): WorkOrder {
    return WorkOrder(
        id = id,
        workOrderNumber = workOrderNumber,
        address = address,
        poleType = poleType,
        distance = distance,
        displayText = displayText
    )
}

/**
 * Extension function to convert list of WorkOrderDto to list of domain WorkOrder entities
 */
fun List<WorkOrderDto>.toDomain(): List<WorkOrder> {
    return map { it.toDomain() }
}
