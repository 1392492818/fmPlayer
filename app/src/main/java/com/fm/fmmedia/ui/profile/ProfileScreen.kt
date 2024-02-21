package com.fm.fmmedia.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.swipeable
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fm.fmmedia.R
import com.fm.fmmedia.ui.home.FmBottomNavLayout
import com.fm.fmmedia.ui.home.HomeSections
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat.startActivity
import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.MainActivity
import com.fm.fmmedia.api.response.MemberInfoResponse
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.repository.MemberInfoRepository
import com.fm.fmmedia.ui.Screen
import com.fm.fmmedia.ui.login.loginScreen
import com.fm.fmmedia.viewmodel.AccessTokenViewModel
import com.fm.fmmedia.viewmodel.MemberInfoViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import com.fm.fmmedia.api.Error
import com.fm.fmmedia.api.response.ShortVideoResponse
import com.fm.fmmedia.api.response.VideoGroupResponse
import com.fm.fmmedia.compose.SwipeRefresh
import com.fm.fmmedia.compose.videoItem
import com.fm.fmmedia.repository.ShortVideoRepository
import com.fm.fmmedia.util.FileHelper
import com.fm.fmmedia.viewmodel.ShortVideoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import kotlin.math.ceil

val maxHeader = 200.dp
val minHeader = 68.dp
val paddingHeader = 20.dp // 为了滑动时候不显示底部
val offsetTopHeader = maxHeader - minHeader + paddingHeader
val circleWidth = 160.dp
val minCircleWidth = 120.dp
val titleMinHeader = circleWidth / 2 + 130.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun profileScreen(
    navController: NavHostController,
    accessTokenViewModel: AccessTokenViewModel,
    memberInfoViewModel: MemberInfoViewModel,
    shortVideoViewModel: ShortVideoViewModel,
    onRecord: () -> Unit,
    onVideoUpload: (path: String) -> Unit
) {
    val systemUiController: SystemUiController = rememberSystemUiController()
    val accessTokenList by accessTokenViewModel.accessTokenList.observeAsState()

    Scaffold(
        bottomBar = {

            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null) {
                FmBottomNavLayout(HomeSections.values(), currentRoute, navController)
            }

        },

        ) { innerPadding ->

        profile(
            innerPadding = innerPadding,
            accessTokenViewModel = accessTokenViewModel,
            memberInfoViewModel = memberInfoViewModel,
            shortVideoViewModel = shortVideoViewModel,
            onRecord = onRecord,
            onVideoUpload = onVideoUpload
        )


    }
}

@Composable
private fun profile(
    innerPadding: PaddingValues,
    accessTokenViewModel: AccessTokenViewModel,
    memberInfoViewModel: MemberInfoViewModel,
    shortVideoViewModel: ShortVideoViewModel,
    onRecord: () -> Unit,
    onVideoUpload: (path: String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var moved by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val moverDuration = screenWidth / 2;
    val maxMoverDuration = screenWidth / 3 * 2

    val memberInfo by memberInfoViewModel.memberInfo.observeAsState()
    val accessTokenList by accessTokenViewModel.accessTokenList.observeAsState()
    val errorCode by memberInfoViewModel.errorCode.observeAsState()
    var accessToken: AccessToken? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(accessTokenList) {
        accessTokenList?.let {
            for (token in it) {
                memberInfoViewModel.memberInfo(token.accessToken)
                accessToken = token
            }
        }
    }

    LaunchedEffect(errorCode) {
        if (errorCode == Error.tokenOutTimeLimit) {
            accessTokenList?.let {
                for (accessToken in it) {
                    accessTokenViewModel.delete(accessToken.id)
                }
            }
        }
    }


    val rightOffset by animateIntOffsetAsState(
        targetValue =
        IntOffset(screenWidth.toInt() + offsetX.roundToInt(), 0),
        label = "offset"
    )

    val contentOffset by animateIntOffsetAsState(
        targetValue =
        IntOffset(offsetX.roundToInt(), 0),
        label = "offset"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {

                    },
                    onDragEnd = {
                        if (abs(offsetX) > moverDuration) {
                            offsetX = -moverDuration
                            moved = true
                        } else {
                            offsetX = 0f
                            moved = false
                        }
                    },
                    onDragCancel = {
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (offsetX <= 0)
                            offsetX += dragAmount
                        if (abs(offsetX) > maxMoverDuration) {
                            offsetX = -maxMoverDuration
                        }
                        moved = true
                    }
                )

            }
    ) {

        Box(modifier = Modifier
            .fillMaxSize()
            .offset { contentOffset }
        ) {

            val scroll = rememberScrollState(0)
            header(innerPadding)
            IconButton(
                modifier = Modifier
                    .padding(0.dp, 15.dp, 0.dp, 0.dp)
                    .align(
                        Alignment.TopEnd
                    ),
                onClick = {
                    // 处理按钮点击事件
                    // 例如，打开/关闭侧滑菜单
                    if (moved) {
                        offsetX = 0f
                        moved = false
                    } else {
                        offsetX = -moverDuration
                        moved = true
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    tint = Color.White,
                    contentDescription = "Menu",
                )
            }
            accessToken?.let { body(scroll, shortVideoViewModel, it, innerPadding = innerPadding) }
            memberInfo?.let {
                title(
                    memberInfo = it,
                    scrollProvider = { scroll.value },
                    onRecord = onRecord,
                    onVideoUpload = onVideoUpload,
                    shortVideoViewModel = shortVideoViewModel
                )
            }
            image {
                scroll.value
            }
            if (moved) { //遮罩
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        moved = !moved
                        offsetX = 0f
                    }
                    .background(Color(0x44000000)))
            }
        }


        // 右侧菜单
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { rightOffset }
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState(0))) {
                var index = 1
                while (index-- > 0) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                tint = Color.Black,
                                modifier = Modifier.padding(3.dp),
                                contentDescription = "exit"
                            )
                            Text(
                                text = stringResource(id = R.string.profile_logout),
                                modifier = Modifier
                                    .padding(5.dp)
                                    .clickable {
                                        val accessTokenList =
                                            accessTokenViewModel.accessTokenList.value
                                        accessTokenList?.let {
                                            for (accessToken in it) {
                                                accessTokenViewModel.delete(accessToken.id)
                                            }
                                        }
                                    }
                            )
                        }
                        Divider()
                    }

                }
            }
        }
    }
}

@Composable
private fun header(innerPadding: PaddingValues) {
    Spacer(
        modifier = Modifier
            .height(maxHeader)
            .fillMaxWidth()
            .background(Brush.horizontalGradient(FmMediaTheme.colors.tornado1))
    )

    Text(
        text = stringResource(id = R.string.profile_member_setting),
        style = androidx.compose.material.MaterialTheme.typography.h6,
        modifier = Modifier.padding(5.dp, innerPadding.calculateTopPadding(), 0.dp, 0.dp),
        color = Color.White
    )


}

@Composable
private fun body(
    scroll: ScrollState,
    shortVideoModel: ShortVideoViewModel,
    accessToken: AccessToken,
    innerPadding: PaddingValues
) {
    val page by shortVideoModel.page.observeAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isFinishing by remember {
        mutableStateOf(false)
    }
    val sort = "createTime=ASC"
    LaunchedEffect(Unit) {
        shortVideoModel.getShortVideoData(accessToken = accessToken.accessToken, sort = sort)
    }

    LaunchedEffect(page?.list) {
        isLoading = false
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columnNum = 3
    val columnItemHeight = 180.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(scroll.value) {

        if (scroll.value == scroll.maxValue) {
            if (page?.hasNextPage == false) {
                isFinishing = true
            } else {
                isLoading = true
                val pageNum = page?.nextPage
                val pageSize = page?.pageSize
                if (pageNum != null && pageSize != null) {
                    shortVideoModel.getShortVideoData(
                        pageNum = pageNum,
                        pageSize = pageSize,
                        isNext = true,
                        accessToken = accessToken.accessToken,
                        sort = sort
                    )
                }
            }
        }
    }


    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeader)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(5.dp, 5.dp, 5.dp, innerPadding.calculateBottomPadding())
        )
        {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(offsetTopHeader + titleMinHeader - paddingHeader)
            )
            val shortVideoList = page?.getData<List<ShortVideoResponse>>()
            shortVideoList?.let { shortVideoList ->
                shortVideoList.chunked(columnNum).forEachIndexed { index, chunk ->
                    Row(modifier = Modifier.height(columnItemHeight)) {
                        chunk.forEach { shortVideo ->
                            videoItem(
                                modifier = Modifier
                                    .height(columnItemHeight)
                                    .width(screenWidth / columnNum)
                                    .padding(2.dp)
                                    .clickable {
                                    },
                                name = shortVideo.title,
                                imageUrl = BuildConfig.API_BASE_URL + "image/" + shortVideo.cover,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

            }


        }


    }
}

@Composable
private fun title(
    scrollProvider: () -> Int,
    memberInfo: MemberInfoResponse,
    shortVideoViewModel: ShortVideoViewModel,
    onRecord: () -> Unit,
    onVideoUpload: (path: String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val screenWidth = configuration.screenWidthDp.dp
    val page by shortVideoViewModel.page.observeAsState()
    val chooseFileLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {

                val path = FileHelper.getFileAbsolutePath(context, uri)
                if (path != null) {
                    Log.e("测hi是", path)
                    onVideoUpload(path)
                }


            }
        }
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(titleMinHeader)
        .offset {
            val scroll = scrollProvider()
            val offset = (maxHeader.toPx() - scroll).coerceAtLeast(minHeader.toPx())
            IntOffset(x = 0, y = offset.toInt())
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(titleMinHeader)
                .background(Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(screenWidth / 2 - circleWidth / 2 + scrollProvider().dp / 2),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = memberInfo.username,
                        maxLines = 1,
                        style = androidx.compose.material.MaterialTheme.typography.h6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(screenWidth / 2 - circleWidth / 2 + scrollProvider().dp / 2),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = memberInfo.email,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${page?.total} 视频数",
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "0 朋友",
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "0 喜欢",
                            modifier = Modifier.weight(2f)
                        )
                    }
                }

                Row(modifier = Modifier.padding(5.dp)) { //这里可以用循环优化
                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable {
                                onRecord()
                            }
                            .padding(6.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            tint = Color.Black,
                            contentDescription = "",
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .padding(2.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.profile_video_camera),
                            modifier = Modifier.padding(5.dp, 3.dp),
                            fontSize = 10.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .wrapContentSize()
                            .clickable {
                                chooseFileLauncher.launch("video/*")
                            }
                            .padding(10.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            tint = Color.Black,
                            contentDescription = "",
                            modifier = Modifier
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .padding(2.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.profile_file_upload),
                            modifier = Modifier
                                .padding(5.dp, 3.dp),
                            fontSize = 10.sp
                        )
                    }

                }

                var state by remember { mutableStateOf(0) }
                val titles = listOf(
                    stringResource(id = R.string.profile_tab_my_video),
                    stringResource(id = R.string.profile_tab_my_like_video),
                    stringResource(id = R.string.profile_tab_my_friend)
                )
                TabRow(
                    selectedTabIndex = state,
                    indicator = { tabPositions ->
                        // 设置指示条的样式
                        TabRowDefaults.Indicator(
                            color = Color.Black, // 指示条颜色
                            height = 2.dp, // 指示条高度
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[state]) // 设置指示条位置
                        )
                    }
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = state == index,
                            onClick = { state = index },
                            selectedContentColor = Color.Black,
                            text = {
                                Text(
                                    text = title,
                                    color = Color.Black,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

            }


        }

    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
private fun image(scrollProvider: () -> Int) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    var circleWidthState by remember {
        mutableStateOf(circleWidth)
    }
    Box(modifier = Modifier
        .width(circleWidthState)
        .height(circleWidthState)
        .offset {
            val scroll = scrollProvider()
            val minOffsetY = minCircleWidth.toPx() / 2 + paddingHeader.toPx()
            val offsetY =
                (maxHeader.toPx() - scroll).coerceAtLeast(minOffsetY) - circleWidthState.toPx() / 2
            var offsetX = (screenWidth / 2 - (circleWidthState.toPx() / 2))
            if (circleWidth - scroll.dp > minCircleWidth) { //设置圆圈最小值
                circleWidthState = circleWidth - scroll.dp / 2
            } else {
                circleWidthState = minCircleWidth
            }
            val maxOffsetX = screenWidth - minCircleWidth.toPx() - paddingHeader.toPx() * 2;
            offsetX += scroll / 2
            if (offsetX > maxOffsetX) {
                offsetX = maxOffsetX
            }

            IntOffset(x = offsetX.toInt(), y = offsetY.toInt())
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(circleWidth / 2))
                .border(2.dp, Color.White, shape = CircleShape)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF")
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                placeholder = painterResource(R.drawable.no_cover),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

    }
}


