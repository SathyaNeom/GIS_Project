package com.enbridge.gdsgpscollection.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.data.local.entity.LocalEditEntity
import com.enbridge.gdsgpscollection.domain.entity.LocalEdit

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
