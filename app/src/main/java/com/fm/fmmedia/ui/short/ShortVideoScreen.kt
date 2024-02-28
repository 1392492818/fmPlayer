package com.fm.fmmedia.ui.short

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fm.fmmedia.api.response.ShortVideoResponse
import com.fm.fmmedia.compose.VerticalPagerVideo
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.ui.home.FmBottomNavLayout
import com.fm.fmmedia.ui.home.HomeSections
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.ShortVideoViewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShortScreen(
    navController: NavHostController,
    shortVideoViewModel: ShortVideoViewModel,
    accessTokenViewModel: AccessTokenViewModel
) {

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
        val page by shortVideoViewModel.pageAll.observeAsState()
        val shortVideoList = page?.getData<MutableList<ShortVideoResponse>>()
        var accessToken: AccessToken? = null
        val sort = "createTime=DESC"

        var isLoading by remember {
            mutableStateOf(true)
        }

        if(accessTokenViewModel.accessTokenList.value?.isEmpty() == false){
            accessToken = accessTokenViewModel.accessTokenList.value?.first()
        }

        LaunchedEffect(Unit){
            shortVideoViewModel.getShortVideoAllData(sort = sort)
        }
        LaunchedEffect(page){
            isLoading = false
        }

        Box(modifier = Modifier
            .systemBarsPadding()
            .padding(0.dp, 0.dp, 0.dp, 58.dp)){
            shortVideoList?.let {
                VerticalPagerVideo(
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
                                shortVideoViewModel.getShortVideoAllData(
                                    pageNum = pageNum,
                                    pageSize = pageSize,
                                    isNext = true,
                                    sort = sort
                                )
                            }
                        }
                    })
            }
            if(isLoading){
                loading()
            }
        }

    }
}

