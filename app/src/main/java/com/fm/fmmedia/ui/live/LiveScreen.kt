package com.fm.fmmedia.ui.live

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.fm.fmmedia.compose.FmGlView
import com.fm.fmmedia.compose.LifecycleEffect
import com.fm.fmmedia.compose.VideoPlayer
import com.fm.fmmedia.ui.home.FmBottomNavLayout
import com.fm.fmmedia.ui.home.HomeSections
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun liveScreen(navController: NavHostController) {

    var pageCount by remember {
        mutableIntStateOf(10)
    }

    val initialPage = 0

    var pagerState = rememberPagerState(pageCount = {
        pageCount
    }, initialPage = initialPage)

    var currentPageNum by remember {
        mutableStateOf(initialPage)
    }


    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            // Do something with each page change, for example:
            // viewModel.sendPageSelectedEvent(page)
            currentPageNum = page
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
                PageContent(
                    text = page.toString(),
                    index = page,
                    currentPageNum = currentPageNum,
                    modifier = Modifier.padding(
                        0.dp,
                        0.dp,
                        0.dp,
                        58.dp
                    )
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PageContent(
    text: String,
    index: Int,
    currentPageNum: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val page = index;

    var isRelease by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(currentPageNum) {
//        if (currentPageNum != page) {
//            Log.e("currentPageNum", "current: ${currentPageNum} , page :${page}")
            if (currentPageNum != page) {
                isRelease = true
            } else {
                isRelease = false
            }
//        }
    }
    val path = "${LocalContext.current.filesDir.absoluteFile}/encoder.mp4"

    // 页面创建时执行一次
    Box(modifier = modifier) {
        VideoPlayer(
            path = path,
            customModifier = Modifier.fillMaxSize(),
            isDraggable = false,
            isShowBack = false,
            isFullWidth = true,
            isReleasePlayer = isRelease
        )
    }
}