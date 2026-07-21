package br.com.atlas.atlasapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.com.atlas.atlasapp.model.AuthViewModel
import br.com.atlas.atlasapp.model.MainViewModel
import br.com.atlas.atlasapp.ui.screens.LoginScreen
import br.com.atlas.atlasapp.ui.screens.RegisterScreen

@Composable
fun AuthNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                modifier = modifier
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                modifier = modifier
            )
        }
    }
}
