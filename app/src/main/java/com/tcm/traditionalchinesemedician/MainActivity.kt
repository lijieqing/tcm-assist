package com.tcm.traditionalchinesemedician

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcm.traditionalchinesemedician.data.HerbRepository
import com.tcm.traditionalchinesemedician.ui.screens.*
import com.tcm.traditionalchinesemedician.ui.theme.TraditionalChineseMedicianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize HerbRepository with application context
        HerbRepository.initialize(applicationContext)
        
        setContent {
            TraditionalChineseMedicianTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Only show bottom navigation for main screens
            val showBottomBar = when {
                currentRoute == "home" -> true
                currentRoute == "profile" -> true
                currentRoute?.startsWith("herbs") == true -> true
                else -> false
            }
            
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.home_screen)) },
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text(stringResource(R.string.herbs_screen)) },
                        selected = currentDestination?.hierarchy?.any { 
                            it.route == "herbs" || it.route?.startsWith("herbs?") == true 
                        } == true,
                        onClick = {
                            navController.navigate("herbs?category=null") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text(stringResource(R.string.profile_screen)) },
                        selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") { 
            HomeScreen(
                onHerbClick = { herbId ->
                    navController.navigate("herb_detail/$herbId")
                },
                onCategoryClick = { category ->
                    if (category.isNotEmpty()) {
                        navController.navigate("herbs?category=$category")
                    } else {
                        navController.navigate("herbs?category=null")
                    }
                },
                onSearchTermClick = { searchTerm ->
                    navController.navigate("herbs?category=null&search=$searchTerm")
                }
            ) 
        }
        
        composable(
            route = "herbs?category={category}&search={searchTerm}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("searchTerm") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            val searchTerm = backStackEntry.arguments?.getString("searchTerm")
            HerbsScreen(
                selectedCategory = category,
                initialSearchQuery = searchTerm ?: "",
                onHerbClick = { herbId ->
                    navController.navigate("herb_detail/$herbId")
                }
            )
        }
        
        composable(
            route = "herbs?category={category}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            HerbsScreen(
                selectedCategory = category,
                onHerbClick = { herbId ->
                    navController.navigate("herb_detail/$herbId")
                }
            )
        }
        
        composable("profile") { ProfileScreen() }
        
        composable(
            route = "herb_detail/{herbId}",
            arguments = listOf(
                navArgument("herbId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val herbId = backStackEntry.arguments?.getInt("herbId") ?: 0
            HerbDetailScreen(
                herbId = herbId,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}