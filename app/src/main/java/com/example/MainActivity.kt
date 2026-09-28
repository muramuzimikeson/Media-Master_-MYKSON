package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.DocumentReaderScreen
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MusicPlayerScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val audioState by viewModel.audioPlayer.state.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val isFullscreen = currentScreen == AppScreen.DOCUMENT_READER ||
            currentScreen == AppScreen.FULLSCREEN_MUSIC ||
            currentScreen == AppScreen.FULLSCREEN_VIDEO

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isFullscreen) {
                Column(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Persistent Mini Player above Navigation Bar
                    MiniPlayerBar(
                        playerState = audioState,
                        onTogglePlayPause = { viewModel.audioPlayer.togglePlayPause() },
                        onSkipNext = { viewModel.audioPlayer.skipNext() },
                        onClickBar = { viewModel.navigateTo(AppScreen.FULLSCREEN_MUSIC) }
                    )

                    // Material 3 Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        val navItems = listOf(
                            NavigationItem(
                                screen = AppScreen.HOME,
                                title = "Home",
                                selectedIcon = Icons.Filled.Home,
                                unselectedIcon = Icons.Outlined.Home,
                                testTag = "nav_home"
                            ),
                            NavigationItem(
                                screen = AppScreen.DOCUMENTS,
                                title = "Docs",
                                selectedIcon = Icons.Filled.Description,
                                unselectedIcon = Icons.Outlined.Description,
                                testTag = "nav_documents"
                            ),
                            NavigationItem(
                                screen = AppScreen.MUSIC,
                                title = "Music",
                                selectedIcon = Icons.Filled.MusicNote,
                                unselectedIcon = Icons.Outlined.MusicNote,
                                testTag = "nav_music"
                            ),
                            NavigationItem(
                                screen = AppScreen.VIDEOS,
                                title = "Videos",
                                selectedIcon = Icons.Filled.Videocam,
                                unselectedIcon = Icons.Outlined.Videocam,
                                testTag = "nav_videos"
                            )
                        )

                        navItems.forEach { item ->
                            val isSelected = currentScreen == item.screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullscreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.DOCUMENTS -> DocumentsScreen(viewModel = viewModel)
                AppScreen.MUSIC -> MusicScreen(viewModel = viewModel)
                AppScreen.VIDEOS -> VideosScreen(viewModel = viewModel)
                AppScreen.DOCUMENT_READER -> DocumentReaderScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.DOCUMENTS) }
                )
                AppScreen.FULLSCREEN_MUSIC -> MusicPlayerScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.MUSIC) }
                )
                AppScreen.FULLSCREEN_VIDEO -> VideoPlayerScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(AppScreen.VIDEOS) }
                )
            }
        }
    }
}

private data class NavigationItem(
    val screen: AppScreen,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
