package com.example.showoff
import DetailsScreenUI
import ReviewScreenUI
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.showoff.ui.CreateAccountUI
import com.example.showoff.ui.EditProfileScreen
import com.example.showoff.ui.ExploreScreenUI
import com.example.showoff.ui.HomeScreenUI
import com.example.showoff.ui.LoginUI
import com.example.showoff.ui.PlaylistDetailScreen
import com.example.showoff.ui.PlaylistsScreenUI
import com.example.showoff.ui.ProfileScreenUI
import com.example.showoff.ui.ShowScreenUI
import com.example.showoff.ui.theme.ShowOffTheme
import com.example.showoff.util.UserManager
import com.example.showoff.util.resetAllRatings
import com.example.showoff.viewmodel.HomeViewModel
import com.example.showoff.viewmodel.PlaylistViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Verificando se o usuário já está autenticado
        setContent {
            ShowOffTheme {
                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = viewModel()
                val playlistViewModel: PlaylistViewModel = viewModel()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    enterTransition = {
                        fadeIn(animationSpec = tween(durationMillis = 300))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(durationMillis = 300))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(durationMillis = 300))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(durationMillis = 300))
                    }
                ) 

                {
                    composable("login") {
                        LoginUI(navController)
                    }
                    composable("signup") {
                        CreateAccountUI(navController)
                    }
                    composable("home") {
                        HomeScreenUI(navController, homeViewModel)
                    }
                    composable("explore") {
                        ExploreScreenUI(navController)
                    }
                    composable("profile") {
                        ProfileScreenUI(navController)
                    }
                    composable("edit_profile") {
                        EditProfileScreen(navController)
                    }
                    composable("playlists") {
                        PlaylistsScreenUI(navController, playlistViewModel)
                    }
                    composable(
                        route = "show/{showName}",
                        arguments = listOf(navArgument("showName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val showName =
                            backStackEntry.arguments?.getString("showName") ?: return@composable
                        ShowScreenUI(showName, navController, homeViewModel)
                    }
                    composable(
                        route = "playlist/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val playlistId =
                            backStackEntry.arguments?.getString("playlistId") ?: return@composable
                        PlaylistDetailScreen(playlistId, navController, playlistViewModel)
                    }
                    composable(
                        route = "profile/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId") ?: ""
                        ProfileScreenUI(navController, userId)
                    }
                    composable(
                        route = "details/{episodeId}",
                        arguments = listOf(navArgument("episodeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                        DetailsScreenUI(
                            episodeId = episodeId,
                            viewModel = homeViewModel,
                            onReviewClick = { navController.navigate("review/$episodeId") },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "review/{episodeId}",
                        arguments = listOf(navArgument("episodeId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                        ReviewScreenUI(
                            episodeId = episodeId,
                            viewModel = homeViewModel,
                            onBack = { navController.popBackStack() },
                            onReviewSubmitted = { navController.popBackStack() }
                        )
                    }
                }

            }

        }

    }
}
