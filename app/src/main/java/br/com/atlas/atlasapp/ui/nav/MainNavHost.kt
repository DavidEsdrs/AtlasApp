package br.com.atlas.atlasapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.com.atlas.atlasapp.model.AuthViewModel
import br.com.atlas.atlasapp.model.MainViewModel
import br.com.atlas.atlasapp.ui.screens.ExploreScreen
import br.com.atlas.atlasapp.ui.screens.HomeScreen
import br.com.atlas.atlasapp.ui.screens.NewRouteScreen
import br.com.atlas.atlasapp.ui.screens.ProfileScreen
import br.com.atlas.atlasapp.ui.screens.RouteDetailsScreen
import br.com.atlas.atlasapp.ui.screens.RouteProgressScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenDetails = { routeId -> navController.navigate(Routes.detailsWithId(routeId)) },
                modifier = modifier
            )
        }
        composable(Routes.EXPLORE) {
            ExploreScreen(
                viewModel = viewModel,
                onOpenDetails = { routeId -> navController.navigate(Routes.detailsWithId(routeId)) },
                modifier = modifier
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onLogout = { authViewModel.logout(viewModel) },
                modifier = modifier
            )
        }
        composable(Routes.NEW_ROUTE) {
            NewRouteScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCreated = {
                    navController.navigate(Routes.HOME) {
                        launchSingleTop = true
                        popUpTo(Routes.HOME)
                    }
                },
                modifier = modifier
            )
        }
        composable(
            route = Routes.DETAILS_WITH_OPTIONAL_ID,
            arguments = listOf(
                navArgument(Routes.DETAILS_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            RouteDetailsScreen(
                viewModel = viewModel,
                routeId = backStackEntry.arguments?.getString(Routes.DETAILS_ID_ARG),
                onBack = { navController.popBackStack() },
                onStartRoute = { navController.navigate(Routes.IN_PROGRESS) },
                modifier = modifier
            )
        }
        composable(Routes.IN_PROGRESS) {
            RouteProgressScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRouteCompleted = {
                    navController.navigate(Routes.HOME) {
                        launchSingleTop = true
                        popUpTo(Routes.HOME)
                    }
                },
                modifier = modifier
            )
        }
    }
}