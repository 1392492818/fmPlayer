package com.fm.fmmedia.ui.video

import android.app.Activity
import android.content.ClipData.Item
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.R
import com.fm.fmmedia.api.response.VideoGroupResponse
import com.fm.fmmedia.compose.FmGlView
import com.fm.fmmedia.compose.LifecycleEffect
import com.fm.fmmedia.compose.RequestError
import com.fm.fmmedia.compose.SwipeRefresh
import com.fm.fmmedia.compose.TapOrientation
import com.fm.fmmedia.compose.Track
import com.fm.fmmedia.compose.VideoPlayer
import com.fm.fmmedia.compose.formatSecondsToHHMMSS
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.compose.videoItem
import com.fm.fmmedia.ui.theme.FmMediaTheme
import com.fm.fmmedia.util.generateListWithStep
import com.fm.fmmedia.viewmodel.VideoGroupViewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun setBrightness(activity: Activity, brightness: Float) {
    // 将 0-1 的值映射到 0-255
    val window = activity?.window
    val layoutParams = window?.attributes
    layoutParams?.screenBrightness = brightness / 255f // set the brightness value between 0 and 1
    window?.attributes = layoutParams
}

val baseUrl = BuildConfig.VIDEO_URL;


@Composable
fun setting(
    @DrawableRes settingIcon: Int,
    linearProgress: Float,
    isPlayer: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(150.dp)
            .height(40.dp)
            .padding(5.dp)
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(5.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(settingIcon),
            tint = Color.White,
            contentDescription = stringResource(
                id = R.string.play_play_icon_text
            ),
            modifier = Modifier
                .clickable {
                    isPlayer.value = !isPlayer.value
                }
                .width(30.dp)
                .height(30.dp)
        )
        LinearProgressIndicator(
            progress = linearProgress,
            modifier = Modifier
                .weight(1f)
                .padding(2.dp)
                .clip(RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
fun videoGroups(
    videoGroupViewModel: VideoGroupViewModel,
    categoryId: Int,
    id: MutableState<Int>,
    onItemClick: (id: Int) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isFinishing by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val videoGroupPage by videoGroupViewModel.videoGroupPage.observeAsState()
    LaunchedEffect(Unit) {
        videoGroupViewModel.getVideoGroup(search = "categoryId=$categoryId")
    }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    SwipeRefresh(
        items = videoGroupPage?.getData<List<VideoGroupResponse>>()?.filter { it.id != id.value },
        refreshing = isRefreshing,
        loading = isLoading,
        finishing = isFinishing,
        columns = GridCells.Fixed(2),
        onRefresh = {
            isRefreshing = true
            isFinishing = false
            scope.launch {
                delay(1000)
                videoGroupViewModel.getVideoGroup(search = "categoryId=$categoryId")
                isRefreshing = false
            }
        },
        onLoad = {
            scope.launch {
                delay(1000)
                if (videoGroupPage?.hasNextPage == false) {
                    isFinishing = true
                } else {
                    val pageNum = videoGroupPage?.nextPage
                    val pageSize = videoGroupPage?.pageSize
                    if (pageNum != null && pageSize != null) {
                        videoGroupViewModel.getVideoGroup(
                            pageNum = pageNum,
                            pageSize = pageSize,
                            isNext = true,
                            search = "categoryId=$categoryId"
                        )
                    }
                }
            }
        }) { index, videoGroup ->
        val num: Dp = (index % 2 * 40).dp
        Row {
            videoItem(
                modifier = Modifier
                    .height(250.dp)
                    .width(screenWidth / 2)
                    .padding(10.dp)
                    .clickable {
                        onItemClick(videoGroup.id)
                    },
                name = videoGroup.name,
                imageUrl = BuildConfig.API_BASE_URL + "image/" + videoGroup.cover,
                contentScale = ContentScale.Crop
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun videoScreen(
    id: Int,
    navController: NavHostController,
    videoGroupViewModel: VideoGroupViewModel
) {
    val videoGroup by videoGroupViewModel.videoGroup.observeAsState()
    val videGroupId = rememberSaveable {
        mutableIntStateOf(id)
    }
    LaunchedEffect(Unit) {
        videoGroupViewModel.findIdGroup(videGroupId.value)
    }


    val width = remember { mutableStateOf(0) }
    val height = remember { mutableStateOf(0) }
    val playerImage: MutableState<Int> =
        remember { mutableStateOf(R.drawable.baseline_pause_24) }
    val isPlayer = rememberSaveable { mutableStateOf(true) }

    var progress = remember {
        mutableFloatStateOf(0f)
    }
    var position = rememberSaveable {
        mutableFloatStateOf(0f)
    }
    var fmGlView: MutableState<FmGlView?> = remember {
        mutableStateOf(null)
    };
    var isSeek = remember {
        mutableStateOf(false)
    }

    var selectSource by remember {
        mutableStateOf("")
    }
    var selectIndex by rememberSaveable {
        mutableIntStateOf(0)
    }




    val context = LocalContext.current


    var isVideoLoading by remember {
        mutableStateOf(true)
    }


    val isRequestError by videoGroupViewModel.isRequestError.observeAsState()




    LifecycleEffect(onResume = {
        isPlayer.value = true
    }, onPause = {
        isPlayer.value = false
        Log.e("测试", "onPause")
    }, onDestroy = {
        Log.e("测试", "onDestroy")
        fmGlView.value?.release()
        isPlayer.value = false
    })


    if (videoGroup != null) {
        Scaffold { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                selectSource = videoGroup?.video!![selectIndex].source
                VideoPlayer(
                    path = baseUrl + selectSource,
                    customModifier = Modifier.height(250.dp),
                    playerEnd = {
                        if (selectIndex + 1 != videoGroup?.video?.size) {
                            selectIndex += 1
                            selectSource = videoGroup?.video!![selectIndex].source
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
//                        .verticalScroll(rememberScrollState())
                ) {
                    videoGroup?.name?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        videoGroup?.let {
                            itemsIndexed(
                                items = it.video
                            ) { index, it ->
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(50.dp)
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color.Black)
                                            .clickable {
                                                if (selectIndex != index) {
                                                    selectSource = it.source
                                                    selectIndex = index
                                                    isPlayer.value = true
                                                    isVideoLoading = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectSource == it.source) {
                                            Text(text = it.order.toString(), color = Color.White)
                                        } else {
                                            Text(text = it.order.toString(), color = Color.Gray)
                                        }

                                    }
                                }
                            }
                        }
                    }
                    Divider()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.play_recommend),
                                style = MaterialTheme.typography.h6,
                                color = FmMediaTheme.colors.brand,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.Start)
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (videoGroup?.categoryId!! > 0) {
                            videoGroups(
                                videoGroupViewModel = videoGroupViewModel,
                                categoryId = videoGroup!!.categoryId,
                                id = videGroupId,
                                onItemClick = { id ->
                                    isVideoLoading = true
                                    position.value = 0f
                                    isSeek.value = true
                                    videoGroupViewModel.findIdGroup(id)
                                    videGroupId.value = id
                                    selectIndex = 0
                                }
                            )
                        }
                    }

                }
            }
        }
    } else {
        if (isRequestError == false)
            loading()
        else
            RequestError(modifier = Modifier
                .clickable { videoGroupViewModel.findIdGroup(id) }
                .fillMaxHeight()
                .fillMaxWidth())
    }
    BackHandler(enabled = true) {
        Log.e("back", "返回")
        (context as? ComponentActivity)?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        navController.popBackStack()
    }
}