package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.BridgeScreen
import com.example.ui.screens.FetcherScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.OptimizerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HubitViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AppScreensScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private fun getViewModel(): HubitViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        return HubitViewModel(app)
    }

    @Test
    fun screenshot_homeScreen() {
        val viewModel = getViewModel()
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(viewModel = viewModel, onNavigateTab = {})
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
    }

    @Test
    fun screenshot_bridgeScreen() {
        val viewModel = getViewModel()
        composeTestRule.setContent {
            MyApplicationTheme {
                BridgeScreen(viewModel = viewModel, onOpenUrlInBrowser = {})
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/bridge_screen.png")
    }

    @Test
    fun screenshot_fetcherScreen() {
        val viewModel = getViewModel()
        composeTestRule.setContent {
            MyApplicationTheme {
                FetcherScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/fetcher_screen.png")
    }

    @Test
    fun screenshot_hubScreen() {
        val viewModel = getViewModel()
        composeTestRule.setContent {
            MyApplicationTheme {
                HubScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/hub_screen.png")
    }

    @Test
    fun screenshot_optimizerScreen() {
        val viewModel = getViewModel()
        composeTestRule.setContent {
            MyApplicationTheme {
                OptimizerScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/optimizer_screen.png")
    }
}
