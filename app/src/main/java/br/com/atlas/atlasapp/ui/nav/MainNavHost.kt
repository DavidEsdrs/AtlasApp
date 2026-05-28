package br.com.atlas.atlasapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenDetails = { navController.navigate(Routes.DETAILS) },
                modifier = modifier
            )
        }
        composable(Routes.EXPLORE) {
            ExploreScreen(
                onOpenDetails = { navController.navigate(Routes.DETAILS) },
                modifier = modifier
            )
        }
        composable(Routes.PROFILE) { ProfileScreen(modifier = modifier) }
        composable(Routes.NEW_ROUTE) { NewRouteScreen(modifier = modifier) }
        composable(Routes.DETAILS) {
            RouteDetailsScreen(
                onBack = { navController.popBackStack() },
                onStartRoute = { navController.navigate(Routes.IN_PROGRESS) },
                modifier = modifier
            )
        }
        composable(Routes.IN_PROGRESS) {
            RouteProgressScreen(
                onBack = { navController.popBackStack() },
                modifier = modifier
            )
        }
    }
}