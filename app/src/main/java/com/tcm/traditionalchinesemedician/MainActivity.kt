package com.tcm.traditionalchinesemedician

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.tcm.traditionalchinesemedician.ui.viewmodels.HerbDetailViewModel
import com.tcm.traditionalchinesemedician.ui.viewmodels.HerbsViewModel
import com.tcm.traditionalchinesemedician.ui.viewmodels.HomeViewModel
import com.tcm.traditionalchinesemedician.ui.viewmodels.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the HerbRepository instance
        // This will trigger the init block which calls initialize()
        HerbRepository.getInstance(applicationContext)
        
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
    
    // Create ViewModels at the top level
    val homeViewModel: HomeViewModel = viewModel()
    val herbsViewModel: HerbsViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    
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
                            // 重置搜索和分类状态
                            herbsViewModel.apply {
                                updateSearchQuery("")
                                updateSelectedCategory(null)
                                loadDataWithCurrentState()
                            }
                            // 导航到中药库页面
                            navController.navigate("herbs?category=null") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
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
            homeViewModel = homeViewModel,
            herbsViewModel = herbsViewModel,
            profileViewModel = profileViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    herbsViewModel: HerbsViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") { 
            HomeScreen(
                viewModel = homeViewModel,
                onHerbClick = { herbId ->
                    navController.navigate("herb_detail/$herbId")
                },
                onCategoryClick = { category ->
                    // 清除搜索内容，设置选中的分类
                    herbsViewModel.apply {
                        updateSearchQuery("")
                        updateSelectedCategory(if (category.isEmpty()) null else category)
                        loadDataWithCurrentState()
                    }
                    // 然后导航到中药库页面
                    navController.navigate("herbs?category=${if (category.isEmpty()) "null" else category}") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onSearchTermClick = { searchTerm ->
                    // 设置搜索内容，清除选中的分类
                    herbsViewModel.apply {
                        updateSearchQuery(searchTerm)
                        updateSelectedCategory(null)
                        performSearch()
                    }
                    navController.navigate("herbs?category=null&search=$searchTerm") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
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
            
            // 使用LaunchedEffect确保每次导航参数变化时更新状态
            LaunchedEffect(key1 = searchTerm, key2 = category) {
                herbsViewModel.apply {
                    // 更新搜索内容
                    if (!searchTerm.isNullOrEmpty()) {
                        updateSearchQuery(searchTerm)
                    } else {
                        updateSearchQuery("")
                    }
                    
                    // 更新选中的分类
                    if (category != null && category != "null") {
                        updateSelectedCategory(category)
                    } else {
                        updateSelectedCategory(null)
                    }
                    
                    // 加载数据
                    loadDataWithCurrentState()
                }
            }
            
            HerbsScreen(
                viewModel = herbsViewModel,
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
            
            // 使用LaunchedEffect确保每次导航参数变化时更新状态
            LaunchedEffect(key1 = category) {
                herbsViewModel.apply {
                    // 清除搜索内容
                    updateSearchQuery("")
                    
                    // 更新选中的分类
                    if (category != null && category != "null") {
                        updateSelectedCategory(category)
                    } else {
                        updateSelectedCategory(null)
                    }
                    
                    // 加载数据
                    loadDataWithCurrentState()
                }
            }
            
            HerbsScreen(
                viewModel = herbsViewModel,
                onHerbClick = { herbId ->
                    navController.navigate("herb_detail/$herbId")
                }
            )
        }
        
        composable("profile") { 
            ProfileScreen(viewModel = profileViewModel) 
        }
        
        composable(
            route = "herb_detail/{herbId}",
            arguments = listOf(
                navArgument("herbId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val herbId = backStackEntry.arguments?.getInt("herbId") ?: 0
            
            // Create a new ViewModel for each detail page
            val detailViewModel: HerbDetailViewModel = viewModel()
            
            // Load herb details
            detailViewModel.loadHerbDetail(herbId)
            
            HerbDetailScreen(
                viewModel = detailViewModel,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}