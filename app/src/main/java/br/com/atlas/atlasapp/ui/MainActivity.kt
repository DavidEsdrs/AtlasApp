package br.com.atlas.atlasapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.atlas.atlasapp.model.AuthViewModel
import br.com.atlas.atlasapp.model.MainViewModel
import br.com.atlas.atlasapp.ui.nav.AuthNavHost
import br.com.atlas.atlasapp.ui.nav.BottomNavBar
import br.com.atlas.atlasapp.ui.nav.BottomNavItem
import br.com.atlas.atlasapp.ui.nav.MainNavHost
import br.com.atlas.atlasapp.ui.nav.Routes
import br.com.atlas.atlasapp.ui.theme.AtlasAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel by viewModels()
            val mainViewModel: MainViewModel by viewModels()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    authViewModel.loadCurrentUser(mainViewModel)
                }
            }

            AtlasAppTheme() {
                if (isLoggedIn) {
                    val navController = rememberNavController()
                    val backStackEntry = navController.currentBackStackEntryAsState().value
                    val showBottomBar = backStackEntry?.destination?.route?.let { route ->
                        route in setOf(Routes.HOME, Routes.EXPLORE, Routes.NEW_ROUTE, Routes.PROFILE)
                    } ?: true

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavBar(
                                    navController = navController,
                                    items = listOf(
                                        BottomNavItem.HomeButton,
                                        BottomNavItem.ExploreButton,
                                        BottomNavItem.CreateButton,
                                        BottomNavItem.ProfileButton
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            MainNavHost(
                                navController = navController,
                                viewModel = mainViewModel,
                                authViewModel = authViewModel
                            )
                        }
                    }
                } else {
                    val authNavController = rememberNavController()
                    AuthNavHost(
                        navController = authNavController,
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}
