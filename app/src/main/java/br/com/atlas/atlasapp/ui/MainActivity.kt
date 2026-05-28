package br.com.atlas.atlasapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.com.atlas.atlasapp.model.MainViewModel
import br.com.atlas.atlasapp.ui.nav.BottomNavBar
import br.com.atlas.atlasapp.ui.nav.BottomNavItem
import br.com.atlas.atlasapp.ui.nav.MainNavHost
import br.com.atlas.atlasapp.ui.nav.Routes
import br.com.atlas.atlasapp.ui.theme.AtlasAppTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val viewModel: MainViewModel by viewModels()
            val backStackEntry = navController.currentBackStackEntryAsState().value
            val showBottomBar = backStackEntry?.destination?.route?.let { route ->
                route in setOf(Routes.HOME, Routes.EXPLORE, Routes.NEW_ROUTE, Routes.PROFILE)
            } ?: true

            AtlasAppTheme() {
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            val items = listOf(
                                BottomNavItem.HomeButton,
                                BottomNavItem.ExploreButton,
                                BottomNavItem.CreateButton,
                                BottomNavItem.ProfileButton
                            )
                            BottomNavBar(navController = navController, items = items)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainNavHost(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}