package com.example.musicbasic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.musicbasic.ui.home.HomeRoute
import com.example.musicbasic.ui.list.ListRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

@Serializable
data object ListDestination

object NavigationResultKey {
    const val SELECTED_MUSIC_ID = "SELECTED_MUSIC_ID"
    const val PLAY_SHUFFLE = "PLAY_SHUFFLE"
    const val NO_SELECTED_MUSIC_ID = -1
}

@Composable
fun AppNavHost(
    modifier: Modifier,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
    ) {
        composable<HomeDestination> { navBackstackEntry ->
            HomeRoute(
                modifier = modifier,
                savedStateHandle = navBackstackEntry.savedStateHandle,
                onMusicListClick = {
                    navController.navigate(ListDestination)
                },
            )
        }

        composable<ListDestination> {
            ListRoute(
                modifier = modifier,
                onMusicClick = { musicId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NavigationResultKey.SELECTED_MUSIC_ID, musicId)
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onPlayClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(NavigationResultKey.PLAY_SHUFFLE, true)

                    navController.popBackStack()
                },
            )
        }
    }
}
