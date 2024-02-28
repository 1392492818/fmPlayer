package com.fm.fmmedia.ui.video

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavController
import com.fm.fmmedia.api.response.ShortVideoResponse
import com.fm.fmmedia.compose.VerticalPagerVideo
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.ShortVideoViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun VideoPageScreen(
    index: Int,
    shortVideoViewModel: ShortVideoViewModel,
    accessTokenViewModel: AccessTokenViewModel,
    navController: NavController

) {
    val page by shortVideoViewModel.page.observeAsState()
    val shortVideoList = page?.getData<MutableList<ShortVideoResponse>>()
    val accessToken = accessTokenViewModel.accessTokenList.value?.first()


    shortVideoList?.let {
        VerticalPagerVideo(pageData = it, initialPage = index, navController = navController, onDelete = { index ->
            accessToken?.let {
                shortVideoViewModel.deleteShortVideo(accessToken.accessToken, index)
            }
        })
    }

}