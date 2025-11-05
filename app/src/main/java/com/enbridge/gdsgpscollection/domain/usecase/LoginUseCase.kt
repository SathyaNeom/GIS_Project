package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.User
import com.enbridge.gdsgpscollection.domain.repository.AuthRepository
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject

/**
 * Use case for handling user authentication operations.
 *
 * This use case encapsulates the business logic for user login, following
 * the Single Responsibility Principle. It acts as an intermediary between
 * the presentation layer (ViewModels) and the data layer (Repositories).
 *
 * Benefits of this pattern:
 * - Separates business logic from presentation and data layers
 * - Makes the code more testable and maintainable
 * - Allows for easy composition of multiple operations
 * - Provides a clear contract for authentication operations
 *
 * @property authRepository The repository that handles authentication data operations
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "LoginUseCase"
    }

    /**
     * Executes the login operation with the provided credentials.
     *
     * @param username The user's username for authentication
     * @param password The user's password for authentication
     * @return Result<User> containing the authenticated user on success, or an error on failure
     */
    suspend operator fun invoke(username: String, password: String): Result<User> {
        Logger.i(TAG, "Executing login use case for username: $username")
        val result = authRepository.login(username, password)

        result.onSuccess { user ->
            Logger.i(TAG, "Login use case successful - User: ${user.username}")
        }.onFailure { error ->
            Logger.e(TAG, "Login use case failed", error)
        }

        return result
    }
}
