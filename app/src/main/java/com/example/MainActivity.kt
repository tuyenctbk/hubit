package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hub.ExoPlayerDialog
import com.example.ui.screens.BridgeScreen
import com.example.ui.screens.FetcherScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HubScreen
import com.example.ui.screens.OptimizerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import androidx.compose.material.icons.filled.Settings
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.dpadFocusable
import com.example.ui.viewmodel.HubitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HubitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    HubitAppContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun HubitAppContent(viewModel: HubitViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val activeVideo by viewModel.activeVideoToPlay.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val tabs = listOf(
        NavigationTab(0, stringResource(R.string.nav_home), Icons.Default.Home),
        NavigationTab(1, stringResource(R.string.nav_bridge), Icons.Default.Share),
        NavigationTab(2, stringResource(R.string.nav_fetcher), Icons.Default.Download),
        NavigationTab(3, stringResource(R.string.nav_hub), Icons.Default.Folder),
        NavigationTab(4, stringResource(R.string.nav_optimizer), Icons.Default.Speed),
        NavigationTab(5, stringResource(R.string.nav_settings), Icons.Default.Settings)
    )

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab.index,
                            onClick = { viewModel.selectTab(tab.index) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.dpadFocusable(
                                shape = RoundedCornerShape(16.dp),
                                onClick = { viewModel.selectTab(tab.index) }
                            ),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Navigation Rail for Wide Screens / Android TV
            if (isWideScreen) {
                NavigationRail(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CyanPrimary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("H!", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Hubit!", color = CyanPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    tabs.forEach { tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab.index,
                            onClick = { viewModel.selectTab(tab.index) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.dpadFocusable(
                                shape = RoundedCornerShape(16.dp),
                                onClick = { viewModel.selectTab(tab.index) }
                            ),
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = CyanPrimary,
                                indicatorColor = CyanPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Screen Content View with animated tab transitions
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width / 4 } + fadeIn(animationSpec = tween(250))) togetherWith
                                    (slideOutHorizontally { width -> -width / 4 } + fadeOut(animationSpec = tween(200)))
                        } else {
                            (slideInHorizontally { width -> -width / 4 } + fadeIn(animationSpec = tween(250))) togetherWith
                                    (slideOutHorizontally { width -> width / 4 } + fadeOut(animationSpec = tween(200)))
                        }
                    },
                    label = "TabContentAnimation"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeScreen(viewModel, onNavigateTab = { viewModel.selectTab(it) })
                        1 -> BridgeScreen(viewModel, onOpenUrlInBrowser = { url ->
                            viewModel.selectTab(2)
                        })
                        2 -> FetcherScreen(viewModel)
                        3 -> HubScreen(viewModel)
                        4 -> OptimizerScreen(viewModel)
                        5 -> SettingsScreen(viewModel)
                    }
                }
            }
        }

        // ExoPlayer Video Dialog Overlay
        activeVideo?.let { (title, pathOrUrl) ->
            ExoPlayerDialog(
                videoTitle = title,
                videoUrlOrPath = pathOrUrl,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissVideoPlayer() }
            )
        }
    }
}

data class NavigationTab(
    val index: Int,
    val title: String,
    val icon: ImageVector
)
