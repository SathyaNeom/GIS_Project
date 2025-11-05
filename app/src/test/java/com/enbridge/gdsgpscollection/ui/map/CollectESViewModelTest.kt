package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import app.cash.turbine.test
import com.enbridge.gdsgpscollection.domain.entity.AttributeType
import com.enbridge.gdsgpscollection.domain.entity.FeatureAttribute
import com.enbridge.gdsgpscollection.domain.entity.FeatureType
import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.domain.usecase.GetFeatureTypesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CollectESViewModel
 *
 * Tests collect ES view model operations ensuring proper handling of:
 * - Feature types loading
 * - Loading states
 * - Error handling
 * - Auto-loading on initialization
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectESViewModelTest {

    private lateinit var getFeatureTypesUseCase: GetFeatureTypesUseCase
    private lateinit var viewModel: CollectESViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getFeatureTypesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CollectESViewModel {
        return CollectESViewModel(getFeatureTypesUseCase)
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        coEvery { getFeatureTypesUseCase() } returns Result.success(emptyList())

        // When
        viewModel = createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.featureTypes.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `init should automatically load feature types`() = runTest {
        // Given
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Gas Pipe",
                geometryType = GeometryType.POLYLINE,
                legendColor = "#E57373",
                attributes = emptyList()
            )
        )
        coEvery { getFeatureTypesUseCase() } returns Result.success(featureTypes)

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.featureTypes.size)
        assertEquals("Gas Pipe", state.featureTypes[0].name)
        assertNull(state.error)

        coVerify(exactly = 1) {
            getFeatureTypesUseCase()
        }
    }

    @Test
    fun `loadFeatureTypes should load feature types successfully`() = runTest {
        // Given
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Service Point",
                geometryType = GeometryType.POINT,
                legendColor = "#64B5F6",
                attributes = emptyList()
            ),
            FeatureType(
                id = "FT002",
                name = "Gas Pipe",
                geometryType = GeometryType.POLYLINE,
                legendColor = "#E57373",
                attributes = emptyList()
            )
        )
        coEvery { getFeatureTypesUseCase() } returns Result.success(featureTypes)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadFeatureTypes()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.featureTypes.size)
        assertEquals("Service Point", state.featureTypes[0].name)
        assertEquals("Gas Pipe", state.featureTypes[1].name)
        assertNull(state.error)

        coVerify(exactly = 2) { // Once in init, once in explicit load
            getFeatureTypesUseCase()
        }
    }

    @Test
    fun `loadFeatureTypes should handle empty list`() = runTest {
        // Given
        coEvery { getFeatureTypesUseCase() } returns Result.success(emptyList())
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When & Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.featureTypes.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadFeatureTypes should handle failure and show error`() = runTest {
        // Given
        val exception = Exception("Failed to load feature types")
        coEvery { getFeatureTypesUseCase() } returns Result.failure(exception)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.featureTypes.isEmpty())
        assertEquals("Failed to load feature types", state.error)

        coVerify(exactly = 1) {
            getFeatureTypesUseCase()
        }
    }

    @Test
    fun `loadFeatureTypes should handle exception without message`() = runTest {
        // Given
        val exception = Exception()
        coEvery { getFeatureTypesUseCase() } returns Result.failure(exception)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Failed to load feature types", state.error)
    }

    @Test
    fun `loadFeatureTypes should set loading state during operation`() = runTest {
        // Given
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Test Feature",
                geometryType = GeometryType.POINT,
                legendColor = "#000000",
                attributes = emptyList()
            )
        )
        coEvery { getFeatureTypesUseCase() } returns Result.success(featureTypes)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When & Then
        viewModel.uiState.test {
            // Current state
            assertFalse(awaitItem().isLoading)

            viewModel.loadFeatureTypes()
            testDispatcher.scheduler.advanceUntilIdle()

            // Loading state
            assertTrue(awaitItem().isLoading)

            // Success state
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(1, finalState.featureTypes.size)
        }
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        // Given
        val exception = Exception("Test error")
        coEvery { getFeatureTypesUseCase() } returns Result.failure(exception)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is present
        assertEquals("Test error", viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadFeatureTypes should load features with attributes`() = runTest {
        // Given
        val attributes = listOf(
            FeatureAttribute(
                id = "ATTR001",
                label = "Name",
                type = AttributeType.TEXT,
                isRequired = true,
                options = emptyList(),
                hint = "Enter name",
                defaultValue = ""
            )
        )
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Complex Feature",
                geometryType = GeometryType.POINT,
                legendColor = "#E57373",
                attributes = attributes
            )
        )
        coEvery { getFeatureTypesUseCase() } returns Result.success(featureTypes)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.featureTypes.size)
        assertEquals(1, state.featureTypes[0].attributes.size)
        assertEquals("Name", state.featureTypes[0].attributes[0].label)
    }

    @Test
    fun `loadFeatureTypes should handle network errors`() = runTest {
        // Given
        val exception = Exception("Network connection failed")
        coEvery { getFeatureTypesUseCase() } returns Result.failure(exception)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Network connection failed", state.error)
    }

    @Test
    fun `loadFeatureTypes should clear previous error on new attempt`() = runTest {
        // Given
        val firstException = Exception("First error")
        coEvery { getFeatureTypesUseCase() } returns Result.failure(firstException)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("First error", viewModel.uiState.value.error)

        // Setup for successful load
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Test",
                geometryType = GeometryType.POINT,
                legendColor = "#000000",
                attributes = emptyList()
            )
        )
        coEvery { getFeatureTypesUseCase() } returns Result.success(featureTypes)

        // When
        viewModel.loadFeatureTypes()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.featureTypes.size)
    }

    @Test
    fun `loadFeatureTypes should handle multiple geometry types`() = runTest {
        // Given
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Point Feature",
                geometryType = GeometryType.POINT,
                legendColor = "#FFD54F",
                attributes = emptyList()
            ),
            FeatureType(
                id = "FT002",
                name = "Line Feature",
                geometryType = GeometryType.POLYLINE,
                legendColor = "#81C784",
                attributes = emptyList()
            ),
            FeatureType(
                id = "FT003",
                name = "Polygon Feature",
                geometryType = GeometryType.POLYGON,
                legendColor = "#9575CD",
                attributes = emptyList()
            )
        )
        coEvery { getFeatureTypesUseCase() } returns Result.success(featureTypes)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.featureTypes.size)
        assertEquals(GeometryType.POINT, state.featureTypes[0].geometryType)
        assertEquals(GeometryType.POLYLINE, state.featureTypes[1].geometryType)
        assertEquals(GeometryType.POLYGON, state.featureTypes[2].geometryType)
    }
}
