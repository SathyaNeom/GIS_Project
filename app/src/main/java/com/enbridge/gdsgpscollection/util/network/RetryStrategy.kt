package com.enbridge.gdsgpscollection.util.network

import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Retry strategy with exponential backoff for failed operations.
 *
 * Implements a configurable retry mechanism with exponential backoff delays.
 * Useful for handling transient network failures or server issues.
 *
 * @property maxAttempts Maximum number of retry attempts (default: 3)
 * @property initialDelayMs Initial delay before first retry in milliseconds (default: 1000ms)
 * @property maxDelayMs Maximum delay cap in milliseconds (default: 30000ms)
 * @property factor Exponential backoff factor (default: 2.0 for doubling)
 */
data class RetryStrategy(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 30000,
    val factor: Double = 2.0
) {
    companion object {
        private const val TAG = "RetryStrategy"

        /**
         * Default retry strategy for network operations.
         * 3 attempts with 1s, 2s, 4s delays.
         */
        val DEFAULT = RetryStrategy(
            maxAttempts = 3,
            initialDelayMs = 1000,
            maxDelayMs = 8000,
            factor = 2.0
        )

        /**
         * Aggressive retry strategy for critical operations.
         * 5 attempts with shorter initial delay.
         */
        val AGGRESSIVE = RetryStrategy(
            maxAttempts = 5,
            initialDelayMs = 500,
            maxDelayMs = 16000,
            factor = 2.0
        )

        /**
         * Conservative retry strategy for non-critical operations.
         * 2 attempts with longer delays.
         */
        val CONSERVATIVE = RetryStrategy(
            maxAttempts = 2,
            initialDelayMs = 2000,
            maxDelayMs = 10000,
            factor = 2.0
        )
    }

    /**
     * Calculates the delay for a given retry attempt.
     *
     * Uses exponential backoff: delay = initialDelay * (factor ^ attempt)
     * Capped at maxDelayMs.
     *
     * @param attempt The current attempt number (0-based)
     * @return Delay in milliseconds
     */
    fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = (initialDelayMs * factor.pow(attempt.toDouble())).toLong()
        return exponentialDelay.coerceAtMost(maxDelayMs)
    }

    /**
     * Suspends execution for the calculated retry delay.
     *
     * @param attempt The current attempt number (0-based)
     */
    suspend fun delayForAttempt(attempt: Int) {
        val delayMs = calculateDelay(attempt)
        Logger.d(TAG, "Retry delay for attempt $attempt: ${delayMs}ms")
        delay(delayMs)
    }

    /**
     * Executes a block with retry logic.
     *
     * Automatically retries on failure up to maxAttempts times with exponential backoff.
     * Logs each retry attempt.
     *
     * @param block The suspending operation to retry
     * @param onRetry Optional callback invoked before each retry
     * @return Result<T> containing success value or last failure
     */
    suspend fun <T> execute(
        block: suspend (attempt: Int) -> T,
        onRetry: (suspend (attempt: Int, error: Throwable) -> Unit)? = null
    ): Result<T> {
        var lastError: Throwable? = null

        repeat(maxAttempts) { attempt ->
            try {
                Logger.d(TAG, "Attempt ${attempt + 1}/$maxAttempts")
                val result = block(attempt)
                Logger.i(TAG, "Operation succeeded on attempt ${attempt + 1}")
                return Result.success(result)
            } catch (e: Exception) {
                lastError = e
                Logger.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}")

                if (attempt < maxAttempts - 1) {
                    // More attempts remaining
                    onRetry?.invoke(attempt, e)
                    delayForAttempt(attempt)
                }
            }
        }

        // All attempts exhausted
        Logger.e(TAG, "All $maxAttempts attempts failed", lastError)
        return Result.failure(lastError ?: Exception("Unknown error"))
    }
}

/**
 * Extension function to retry a suspending operation with default strategy.
 *
 * @param strategy Retry strategy to use (default: RetryStrategy.DEFAULT)
 * @param block The suspending operation to retry
 * @return Result<T> containing success value or last failure
 */
suspend fun <T> retryWithBackoff(
    strategy: RetryStrategy = RetryStrategy.DEFAULT,
    block: suspend (attempt: Int) -> T
): Result<T> {
    return strategy.execute(block)
}
