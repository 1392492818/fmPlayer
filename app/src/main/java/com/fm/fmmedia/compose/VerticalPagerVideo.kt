package com.fm.fmmedia.compose

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.R
import com.fm.fmmedia.api.response.MemberInfoResponse
import com.fm.fmmedia.api.response.ShortVideoResponse
import com.fm.fmmedia.ui.profile.circleWidth
import kotlinx.coroutines.launch
import okhttp3.internal.notify

@Composable
fun videoSetting(
    modifier: Modifier = Modifier,
    profilePhoto: Any,
    shortVideoResponse: ShortVideoResponse,
    isShowDelete: Boolean = true,
    onDelete: () -> Unit = {}
) {
    var showDialog by remember {
        mutableStateOf(false)
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(40.dp / 2))
                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
                    .border(2.dp, Color.White, shape = CircleShape)
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profilePhoto)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                placeholder = painterResource(R.drawable.profile),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(40.dp / 2)),
                contentScale = ContentScale.Crop,
            )
        }



        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                tint = Color.White,
                contentDescription = "like",
                modifier = Modifier
                    .size(40.dp)
            )
            Text(text = shortVideoResponse.like.toString(), color = Color.White)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.StarRate,
                tint = Color.White,
                contentDescription = "collect",
                modifier = Modifier
                    .size(40.dp)
            )
            Text(text = shortVideoResponse.collect.toString(), color = Color.White)
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(isShowDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = Color.White,
                    contentDescription = "delete",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            showDialog = true
                        }
                )
            }

        }
        if (showDialog)
            AlertDialog(showDialogDefault = showDialog) { isConfirm ->
                Log.e("测试", "确认删除")
                showDialog = false
                if (isConfirm) onDelete()
            }

    }

}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PageContent(
    shortVideoResponse: ShortVideoResponse,
    index: Int,
    currentPageNum: Int,
    modifier: Modifier = Modifier,
    isShowDelete: Boolean = true,
    onDelete: (index: Int) -> Unit = {}
) {
    val page = index;
    val path = BuildConfig.VIDEO_URL + shortVideoResponse.source
    val cover = BuildConfig.API_BASE_URL + "image/" + shortVideoResponse.cover

    val member = shortVideoResponse.member
    val profilePhoto =
        if (member.avatar == null) R.drawable.profile else BuildConfig.API_BASE_URL + "profile_photo/" + member.avatar
//    Log.e("测试", profilePhoto)
    var isRelease by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(currentPageNum) {
        Log.e("Page", "currentPageNum ${currentPageNum}, page ${page}")
        isRelease = currentPageNum != page
    }
    var isShowCover by remember {
        mutableStateOf(true)
    }
    // 页面创建时执行一次
    Box(
        modifier = modifier
            .systemBarsPadding()
            .padding(0.dp, 0.dp, 0.dp, 0.dp)
    ) {


        VideoPlayer(
            path = path,
            customModifier = Modifier.fillMaxSize(),
            isDraggable = false,
            isShowBack = false,
            isFullWidth = true,
            isLoop = true,
            isShowControlDefault = false,
            isReleasePlayer = isRelease,
            isCover = {
                isShowCover = it
            },
            playerEnd = {
                isRelease = true
            }
        )
        if (isRelease)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cover)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                placeholder = painterResource(R.drawable.no_cover),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(text = "@${member.username}", color = Color.White, fontSize = 18.sp)
            Text(
                text = shortVideoResponse.desc,
                color = Color.White,
                modifier = Modifier.padding(3.dp)
            )
        }

        videoSetting(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(10.dp)
                .height(320.dp),
            profilePhoto = profilePhoto,
            isShowDelete = isShowDelete,
            onDelete = {
                onDelete(index)
            },
            shortVideoResponse = shortVideoResponse
        )
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerticalPagerVideo(
    pageData: MutableList<ShortVideoResponse>,
    initialPage: Int = 0,
    navController: NavController,
    isShowDelete: Boolean = true,
    onPageEnd: () -> Unit = {},
    onDelete: (index: Int) -> Unit = {}
) {

    var pageCount by remember {
        mutableIntStateOf(pageData.size)
    }

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
            Log.d("Page", "Page changed to $page")
        }
    }

    VerticalPager(
        state = pagerState,
    ) { page ->
        // Our page content
        if (page == pageData.size - 1) {
            onPageEnd()
        }
        val pageIndex by remember {
            mutableIntStateOf(page)
        }
        val coroutineScope = rememberCoroutineScope()

        PageContent(
            shortVideoResponse = pageData[page],
            index = pageIndex,
            currentPageNum = currentPageNum,
            isShowDelete = isShowDelete,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            onDelete = { index ->
                Log.e("delete", index.toString())

                onDelete(pageData[index].id)
                pageData.removeAt(index)
                pageCount = pageData.size
//                currentPageNum = 0
                if (pageCount == 0) navController.popBackStack()

                coroutineScope.launch {
                    if (index + 1 >= pageCount - 1) {
                        pagerState.scrollToPage(index + 1)
                        currentPageNum = index + 1
                    } else {
                        pagerState.scrollToPage(pageCount - 1)
                        currentPageNum = pageCount - 1
                    }
                }
            }
        )
    }
}