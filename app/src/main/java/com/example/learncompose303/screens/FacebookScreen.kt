package com.example.learncompose303.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.learncompose303.R
import com.example.learncompose303.data.Destinations
import kotlinx.coroutines.launch

import androidx.compose.material.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.learncompose303.app.makeToast
import com.example.learncompose303.data.Shortcut
import com.example.learncompose303.data.getRandomItems

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun FacebookScaffold(navController: NavHostController) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val onDrawerIconClick: () -> Unit = {
        coroutineScope.launch { scaffoldState.drawerState.open() }
    }
    val context = LocalContext.current.applicationContext
    val randomItems = remember { mutableStateOf(getRandomItems(10)) }
    val shortcuts = remember { mutableStateOf(Shortcut.getShortcuts()) }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            FbBottomNav(navController = navController, onDrawerIconClick)
        },
        drawerContent = { TheNavigationDrawer(navController, randomItems.value, shortcuts.value) }
    ) {
        NavHost(
            navController = navController,
            startDestination = Destinations.Home.route,
        ) {
            composable(Destinations.Home.route) { HomeScreen() }
            composable(Destinations.Notifications.route) { NotificationsScreen() }
            composable(Destinations.TTTScreen.route) { TTTScreen(navController) }
            composable(
                Destinations.Detail.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://www.fblikeapp.com/{itemId}" }
                )
            ) {
                val itemId = it.arguments?.getString("itemId")
                if (itemId == null) {
                    makeToast(context, "ID is required!")
                } else {
                    ItemDetailScreen(itemId.toInt())
                }
            }
        }
    }

}


@Composable
fun FbBottomNav(navController: NavHostController, onDrawerIconClick: () -> Unit) {
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.Home.route,
            onClick = { navController.navigate(Destinations.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(text = Destinations.Home.route) }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == Destinations.TTTScreen.route,
            onClick = { navController.navigate(Destinations.Notifications.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            label = { Text(text = Destinations.Notifications.route) }
        )

        BottomNavigationItem(
            selected = false,
            onClick = onDrawerIconClick,
            icon = { Icon(Icons.Default.Menu, contentDescription = null) },
            label = { Text(text = stringResource(id = R.string.menu)) }
        )
    }

}











