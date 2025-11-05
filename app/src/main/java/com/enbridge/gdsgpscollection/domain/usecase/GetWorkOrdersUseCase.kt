package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import com.enbridge.gdsgpscollection.domain.repository.ProjectSettingsRepository
import javax.inject.Inject

/**
 * Use case for retrieving work orders based on pole type and location
 */
class GetWorkOrdersUseCase @Inject constructor(
    private val repository: ProjectSettingsRepository
) {
    /**
     * Execute the use case to get work orders
     *
     * @param poleType The type of pole (6 Foot Pole, 8 Foot Pole, Handheld)
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @param distance Search radius in meters
     * @return Result containing list of work orders or error
     */
    suspend operator fun invoke(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrder>> {
        return repository.getWorkOrders(poleType, latitude, longitude, distance)
    }
}
