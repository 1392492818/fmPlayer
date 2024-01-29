package com.fm.fmmedia.ui.live

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.fm.fmmedia.ui.home.FmBottomNavLayout
import com.fm.fmmedia.ui.home.HomeSections

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun liveScreen(navController: NavHostController) {

    var pageCount by remember {
        mutableIntStateOf(10)
    }

    var pagerState = rememberPagerState(pageCount = {
        pageCount
    })


    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            // Do something with each page change, for example:
            // viewModel.sendPageSelectedEvent(page)
            Log.d("Page change", "Page changed to $page")
        }
    }
    Scaffold(
        bottomBar = {

            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null) {
                FmBottomNavLayout(HomeSections.values(), currentRoute, navController)
            }

        }
    ) { innerPadding ->
        Row {
            // Display 10 items
            VerticalPager(state = pagerState) { page ->
                // Our page content
                if (page == pageCount - 1) {
                    pageCount += 10
                }
                var color = Color.Red
                if (page % 2 == 0) color = Color.Gray
                else color = Color.Black
                Text(
                    text = "Page: $page",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
}