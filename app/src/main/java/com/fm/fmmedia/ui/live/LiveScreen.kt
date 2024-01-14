package com.fm.fmmedia.ui.live

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.fm.fmmedia.ui.home.FmBottomNavLayout
import com.fm.fmmedia.ui.home.HomeSections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun liveScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {

            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null) {
                FmBottomNavLayout(HomeSections.values(), currentRoute, navController)
            }

        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            Text(text = "LIVE")
        }
    }
}