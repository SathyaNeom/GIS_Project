package com.enbridge.electronicservices.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for posting/uploading local ES data changes to the server
 * Synchronizes local geodatabase changes with the remote service
 */
class PostESDataUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return manageESRepository.postESData()
    }
}
