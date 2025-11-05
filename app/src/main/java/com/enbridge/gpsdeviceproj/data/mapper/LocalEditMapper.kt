package com.enbridge.gpsdeviceproj.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.data.local.entity.LocalEditEntity
import com.enbridge.gpsdeviceproj.domain.entity.LocalEdit

fun LocalEditEntity.toDomain(): LocalEdit {
    return LocalEdit(
        id = id,
        entityId = entityId,
        entityType = entityType,
        isSynced = isSynced
    )
}

fun LocalEdit.toEntity(): LocalEditEntity {
    return LocalEditEntity(
        id = id,
        entityId = entityId,
        entityType = entityType,
        isSynced = isSynced
    )
}
