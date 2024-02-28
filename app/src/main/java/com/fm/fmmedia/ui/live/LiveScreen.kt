package com.fm.fmmedia.ui.live

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.fm.fmmedia.api.response.LiveResponse
import com.fm.fmmedia.api.response.ShortVideoResponse
import com.fm.fmmedia.compose.LiveVerticalPagerVideo
import com.fm.fmmedia.compose.VerticalPagerVideo
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.ui.home.FmBottomNavLayout
import com.fm.fmmedia.ui.home.HomeSections
import com.fm.fmmedia.ui.profile.emptyData
import com.fm.fmmedia.viewmodel.LiveViewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(navController: NavHostController, liveViewModel: LiveViewModel) {
    val systemUiController: SystemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(color = Color.Black)

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null) {
                FmBottomNavLayout(HomeSections.values(), currentRoute, navController)
            }
        }
    ) { innerPadding ->
        // Display 10 items
        val page by liveViewModel.page.observeAsState()
        LaunchedEffect(Unit) {
            liveViewModel.pageLive()
        }
        val liveVideoList = page?.getData<MutableList<LiveResponse>>()

        var isLoading by remember {
            mutableStateOf(true)
        }
        LaunchedEffect(page) {
            isLoading = false
        }

        Box(
            modifier = Modifier
                .systemBarsPadding()
                .padding(0.dp, 0.dp, 0.dp, 58.dp)
        ) {
            liveVideoList?.let {
                if (!it.isEmpty())
                    LiveVerticalPagerVideo(
                        pageData = it,
                        initialPage = 0,
                        navController = navController,
                        isShowDelete = false,
                        onPageEnd = {
                            if (page?.hasNextPage == true) {
                                isLoading = true
                                val pageNum = page?.nextPage
                                val pageSize = page?.pageSize
                                if (pageNum != null && pageSize != null) {
                                    liveViewModel.pageLive(
                                        pageNum = pageNum,
                                        pageSize = pageSize,
                                        isNext = true,
                                    )
                                }
                            }
                        })
            }
            if (liveVideoList.isNullOrEmpty()) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        isLoading = true
                        liveViewModel.pageLive()
                    }, contentAlignment = Alignment.Center
                ) {
                    emptyData(modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .clickable {

                        })
                }

            }
            if (isLoading) {
                loading()
            }
        }
    }
}