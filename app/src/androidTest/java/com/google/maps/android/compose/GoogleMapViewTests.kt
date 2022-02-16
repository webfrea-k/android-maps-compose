package com.google.maps.android.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch

class GoogleMapViewTests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MapSampleActivity>()

    private val startingZoom = 10f
    private val startingPosition = LatLng(1.23, 4.56)
    private val assertRoundingError = 0.0000001

    private lateinit var cameraPositionState: CameraPositionState

    @Before
    fun setUp() {
        cameraPositionState = CameraPositionState(
            position = CameraPosition.fromLatLngZoom(
                startingPosition,
                startingZoom
            )
        )

        val countDownLatch = CountDownLatch(1)
        composeTestRule.setContent {
            GoogleMapView(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLoaded = {
                    countDownLatch.countDown()
                }
            )
        }
        countDownLatch.await()
    }
    @Test
    fun testStartingCameraPosition() {
        startingPosition.assertEquals(cameraPositionState.position.target)
    }

    @Test
    fun testCameraReportsMoving() {
        zoom(shouldAnimate = true, zoomIn = true) {
            composeTestRule.waitUntil(1000) {
                cameraPositionState.isMoving
            }
            assertTrue(cameraPositionState.isMoving)
        }
    }

    @Test
    fun testCameraReportsNotMoving() {
        zoom(shouldAnimate = true, zoomIn = true) {
            composeTestRule.waitUntil(1000) {
                cameraPositionState.isMoving
            }
            composeTestRule.waitUntil(1000) {
                !cameraPositionState.isMoving
            }
            assertFalse(cameraPositionState.isMoving)
            assertTrue(false)
        }
    }

    @Test
    fun testCameraZoomInAnimation() {
        zoom(shouldAnimate = true, zoomIn = true) {
            composeTestRule.waitForIdle()
            assertEquals(
                startingZoom + 1f,
                cameraPositionState.position.zoom,
                assertRoundingError.toFloat()
            )
        }
    }

    @Test
    fun testCameraZoomIn() {
        zoom(shouldAnimate = false, zoomIn = true) {
            composeTestRule.waitForIdle()
            assertEquals(
                startingZoom + 1f,
                cameraPositionState.position.zoom,
                assertRoundingError.toFloat()
            )
        }
    }

    @Test
    fun testCameraZoomOut() {
        zoom(shouldAnimate = false, zoomIn = false) {
            composeTestRule.waitForIdle()
            assertEquals(
                startingZoom - 1f,
                cameraPositionState.position.zoom,
                assertRoundingError.toFloat()
            )
        }
    }

    @Test
    fun testCameraZoomOutAnimation() {
        zoom(shouldAnimate = true, zoomIn = false) {
            composeTestRule.waitForIdle()
            assertEquals(
                startingZoom - 1f,
                cameraPositionState.position.zoom,
                assertRoundingError.toFloat()
            )
        }
    }

    private fun zoom(shouldAnimate: Boolean, zoomIn: Boolean, assertionBlock: () -> Unit) {
        if (!shouldAnimate) {
            composeTestRule.onNodeWithTag("cameraAnimations")
                .assertIsDisplayed()
                .performClick()
        }
        composeTestRule.onNodeWithText(if (zoomIn) "+" else "-")
            .assertIsDisplayed()
            .performClick()

        assertionBlock()
    }

    private fun LatLng.assertEquals(other: LatLng) {
        assertEquals(latitude, other.latitude, assertRoundingError)
        assertEquals(longitude, other.longitude, assertRoundingError)
    }
}