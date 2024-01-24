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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.fm.fmmedia.R
import com.fm.fmmedia.api.response.VideoGroupResponse
import com.fm.fmmedia.compose.FmGlView
import com.fm.fmmedia.compose.LifecycleEffect
import com.fm.fmmedia.compose.RequestError
import com.fm.fmmedia.compose.SwipeRefresh
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


/**
 * 按下方向
 */
enum class TapOrientation(
    val route: String
) {
    LEFT("LEFT"),
    RIGHT("RIGHT")
}

fun setBrightness(activity: Activity, brightness: Float) {
    // 将 0-1 的值映射到 0-255
    val window = activity?.window
    val layoutParams = window?.attributes
    layoutParams?.screenBrightness = brightness / 255f // set the brightness value between 0 and 1
    window?.attributes = layoutParams
}

/**
 * 进度和播放的控制
 */
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun control(
    playerImage: MutableState<Int>,
    isPlayer: MutableState<Boolean>,
    fmGlView: MutableState<FmGlView?>,
    position: MutableState<Float>,
    progress: MutableState<Float>,
    isSeek: MutableState<Boolean>,
    isFull: MutableState<Boolean>,
    fullHeight: MutableState<Modifier>,
    isShowSpeed: MutableState<Boolean>,
    videoSpeed: MutableState<Double>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    // 设置屏幕保持唤醒状态

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(Color.Black)
            .padding(5.dp),
    ) {

        Icon(
            imageVector = ImageVector.vectorResource(playerImage.value),
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
        Text(text = formatSecondsToHHMMSS(position.value.toLong()), color = Color.White)

        Slider(
            value = position.value,
            onValueChange = {
                position.value = it
                isSeek.value = true
                fmGlView.value?.seek(position.value.toLong())
            },
            modifier = Modifier
                .padding(5.dp)
                .weight(1f),
            valueRange = 0f..progress.value
        )

        Text(text = formatSecondsToHHMMSS(progress.value.toLong()), color = Color.White)
        if (isFull.value) {
            Text(
                text = "${videoSpeed.value.toString()}X",
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp, 0.dp, 8.dp, 0.dp)
                    .clickable {
//                        fmGlView.value?.setSpeed(5f)
                        isShowSpeed.value = !isShowSpeed.value
                    }
            )
        }
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_fullscreen_24),
            tint = Color.White,
            contentDescription = stringResource(
                id = R.string.play_play_icon_text
            ),
            modifier = Modifier
                .clickable {
//                    fmGlView?.release()
                    isFull.value = !isFull.value
                }
                .width(30.dp)
                .height(30.dp)
        )
        val systemUiController: SystemUiController = rememberSystemUiController()

        DisposableEffect(isFull.value) {
            Log.e("测试", "反向旋转了")
            if (isFull.value) {
                // 设置横屏方向
                (context as? ComponentActivity)?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                systemUiController.isStatusBarVisible = false // Status bar
                systemUiController.isNavigationBarVisible = false // Navigation bar
                systemUiController.isSystemBarsVisible = false
                fullHeight.value = Modifier.fillMaxHeight()
            } else {
                // 恢复为竖屏方向
                (context as? ComponentActivity)?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                fullHeight.value = Modifier.height(250.dp)
            }
            onDispose { }
        }

    }

}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun Speed(
    modifier: Modifier = Modifier,
    select: MutableState<Double>,
    fmGlView: MutableState<FmGlView?>,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.play_audio_speed), color = Color.White)
            LazyRow {
                itemsIndexed(items = generateListWithStep(0.5)) { index, item ->
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
                                .background(
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .clickable {
                                    Log.e("测试", "点击了" + item)
                                    select.value = item
                                    fmGlView.value?.setSpeed(select.value.toFloat())
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.toString(), color = Color.White)
                            if (select.value == item) {
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .align(Alignment.BottomEnd)
                                        .fillMaxWidth()

                                        .background(
                                            color = Color.Black, shape = RoundedCornerShape(5.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 音量 跟 亮度 设置
 */
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
                imageUrl = "https://img0.baidu.com/it/u=428280756,4053559961&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500"
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
    Log.e("测试", "videoScreen")
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

    var isFull = rememberSaveable {
        mutableStateOf(false)
    }


    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    // 获取临时目录
    val cacheDir = context.cacheDir.absolutePath
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    var tapOrientation: TapOrientation by remember {
        mutableStateOf(TapOrientation.LEFT)
    }
    var linearProgress by remember {
        mutableFloatStateOf(0f)
    }


    var brightness by remember {
        mutableFloatStateOf(125f)
    }
    val activity = LocalContext.current as? ComponentActivity


    Log.e("测试", "亮度" + brightness)

    val audioManager = LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager


    var isShowProgress by remember {
        mutableStateOf(false)
    }

    var settingIcon by
    remember { mutableStateOf(R.drawable.baseline_volume_mute_24) }

    val volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val volumeMin = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
    val volumeMinProgress: Float = 0f
    val volumeMaxProgress: Float = 100f
    var volume: Float by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / (volumeMax - volumeMin) * volumeMaxProgress)
    }
    var isShowControl by remember {
        mutableStateOf(true)
    }
    var isToucher by remember {
        mutableStateOf(false)
    }
    var modifier = remember {
        mutableStateOf(Modifier.height(250.dp))
    }
    var isVideoLoading by remember {
        mutableStateOf(true)
    }

    var isError by remember {
        mutableStateOf(false)
    }
    val isShowSpeed = rememberSaveable {
        mutableStateOf(false)
    }
    val isRequestError by videoGroupViewModel.isRequestError.observeAsState()
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (isToucher) {
                isToucher = false
            } else {
                isShowSpeed.value = false
                isShowProgress = false
                isShowControl = false
            }
        }
    }
    val videoSpeed = rememberSaveable {
        mutableDoubleStateOf(1.0)
    }


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

    if (videoGroup != null
//        && videoGroup?.id == id
    ) {
        Scaffold { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Box(
                    modifier = modifier.value
                        .fillMaxWidth()
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            // 监听点击事件
                            detectTapGestures(
                                onPress = {
                                    Log.e("onPress", "按下" + it.x)
                                    isToucher = true
                                    isShowControl = !isShowControl
                                    if (it.x < screenWidth / 2) {
                                        Log.e("left", "left")
                                        tapOrientation = TapOrientation.LEFT
                                    } else {
                                        Log.e("right", "right")
                                        tapOrientation = TapOrientation.RIGHT
                                    }
                                },
                                onDoubleTap = {
                                    Log.e("onPress", "双击")
                                    isPlayer.value = !isPlayer.value
                                },
                                onLongPress = {
                                    Log.e("onPress", "长按")

                                },
                                onTap = {
                                    Log.e("onPress", "单击")
                                }
                            )
                        }
                        .draggable(
                            // 设置orientation为横向
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                // 得到相比于上次改变的位移
                                isShowControl = true
                                Log.e("Vertical", delta.toString())
                                val newPosition = position.value + delta.toInt()
                                if (newPosition < 0) {
                                    position.value = 0f
                                } else if (newPosition > progress.value) {
                                    position.value = progress.value
                                } else {
                                    position.value = newPosition
                                }
                                isSeek.value = true
                                fmGlView.value?.seek(position.value.toLong())
                            })
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                // 得到相比于上次改变的位移
//                                offsetX += delta
                                if (tapOrientation == TapOrientation.RIGHT) {

                                    volume -= delta.toInt()
                                    if (volume > volumeMaxProgress) volume = volumeMaxProgress
                                    if (volume < volumeMinProgress) volume = volumeMinProgress
                                    Log.e(
                                        "测试",
                                        (volume / volumeMaxProgress * (volumeMax - volumeMin)).toString()
                                    )

                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        (volume / volumeMaxProgress * (volumeMax - volumeMin)).toInt(),
                                        0
                                    );
                                    linearProgress =
                                        (volume / volumeMaxProgress)
                                    isShowProgress = true
                                    settingIcon = R.drawable.baseline_volume_mute_24
                                } else {
                                    brightness -= delta
                                    if (brightness > 255) brightness = 255f
                                    if (brightness < 0) brightness = 0f
                                    if (activity != null) {
                                        linearProgress = brightness.toFloat() / 255f
                                        setBrightness(activity = activity, brightness = brightness)
                                    }
                                    isShowProgress = true
                                    settingIcon = R.drawable.baseline_brightness_7_24
                                }
                            }
                        )
                ) {
                    if (videoGroup?.video?.isEmpty() == false) {
                        selectSource = videoGroup?.video!![selectIndex].source
                        AndroidView(factory = { context ->
                            FmGlView(
                                context = context,
                                url = videoGroup?.video!![selectIndex].source,
                                seekTime = position.value.toLong(),
                                cachePath = cacheDir,
                                progress = { currentPosition, duration, isSeekSuccess ->
                                    isVideoLoading = false
                                    if (isSeekSuccess) {
                                        isSeek.value = false
                                    }
                                    if (!isSeek.value) //如果 seek 了，然后需要等seek完毕了，再更新了
                                        position.value = currentPosition.toFloat()
                                    progress.value = duration.toFloat()
                                },
                                endCallback = { error ->
                                    fmGlView.value?.release()
                                    if (error) {
                                        isError = true
                                        isVideoLoading = false
                                    } else {
                                        if (selectIndex + 1 != videoGroup?.video?.size) {
                                            selectIndex += 1
                                            isVideoLoading = true
                                            val source = videoGroup?.video?.get(selectIndex)
                                            source?.let {
                                             //   fmGlView.value?.release()
                                                fmGlView.value?.reset(
                                                    source = it.source,
//                                                    seekTime = position.value.toLong()
                                                )
                                                selectSource = it.source
                                                isPlayer.value = true
                                            }
                                        }
                                    }
                                }, onLoading = {
                                    isVideoLoading = true
                                })
                        }, modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { layoutCoordinates ->
                                // 获取宽度和高度
                                width.value = layoutCoordinates.size.width
                                height.value = layoutCoordinates.size.height
                            }, update = { it ->
                            fmGlView.value = it
                        }, onRelease = {
                            it.release()
                        }
                        )
                    }
                    if (isShowSpeed.value && isShowControl && isFull.value) {
                        Speed(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .align(Alignment.Center),
                            select = videoSpeed,
                            fmGlView = fmGlView
                        )
                    }

                    if (isShowControl) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            tint = Color.White,
                            contentDescription = stringResource(R.string.home_label_search),
                            modifier = Modifier
                                .clickable {
                                    if (isFull.value) {
                                        isFull.value = false
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                                .padding(10.dp)
                        )
                    }

                    if (isShowProgress) {
                        setting(
                            settingIcon = settingIcon,
                            isPlayer = isPlayer,
                            linearProgress = linearProgress,
                            modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                    }

                    if (isShowControl) {
                        control(
                            playerImage = playerImage,
                            isPlayer = isPlayer,
                            fmGlView = fmGlView,
                            position = position,
                            isSeek = isSeek,
                            progress = progress,
                            isFull = isFull,
                            fullHeight = modifier,
                            isShowSpeed = isShowSpeed,
                            videoSpeed = videoSpeed,
                            modifier = Modifier.align(
                                Alignment.BottomCenter
                            )
                        )
                    }
                    DisposableEffect(isPlayer.value) {
                        if (isPlayer.value) {
                            playerImage.value = R.drawable.baseline_pause_24
                            fmGlView.value?.play()
                        } else {
                            playerImage.value = R.drawable.baseline_play_arrow_24
                            fmGlView.value?.pause()
                        }
                        onDispose { /* cleanup logic here */ }
                    }

                    DisposableEffect(videoGroup) {
                        isSeek.value = false
                        fmGlView.value?.reset(
                            videoGroup!!.video[0].source,
                            seekTime = position.value.toLong()
                        )
                        fmGlView.value?.setSpeed(videoSpeed.value.toFloat())
                        onDispose { }
                    }


                    if (isVideoLoading) {
                        loading(color = R.color.white)
                    }
                    if (isError) {
                        RequestError(modifier = Modifier
                            .clickable {
                                val source = videoGroup?.video?.get(selectIndex)
                                source?.let {
                                    fmGlView.value?.reset(
                                        source = it.source,
                                        position.value.toLong()
                                    )
                                }
                                isVideoLoading = true
                                isError = false
                            }
                            .fillMaxHeight()
                            .fillMaxWidth(), color = Color.White)
                    }
                }
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
                                                    fmGlView.value?.reset(
                                                        it.source,
                                                        seekTime = position.value.toLong()
                                                    )
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
        if (isFull.value) {
            isFull.value = false
        } else {
            navController.popBackStack()
        }
    }
}