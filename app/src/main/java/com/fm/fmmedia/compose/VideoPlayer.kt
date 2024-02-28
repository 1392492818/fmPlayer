package com.fm.fmmedia.compose

import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fm.fmmedia.R
import com.fm.fmmedia.ui.video.setBrightness
import com.fm.fmmedia.util.generateListWithStep
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay


/**
 * 按下方向
 */
enum class TapOrientation(
    val route: String
) {
    LEFT("LEFT"),
    RIGHT("RIGHT"),
    NONE("NONE")
}

/**
 * 进度和播放的控制
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    isRelease: MutableState<Boolean>,
    isVideoLoading: MutableState<Boolean>,
    path: String,
    cacheProgress: Float,
    modifier: Modifier = Modifier,
    portrait: Modifier = Modifier
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

            },
            onValueChangeFinished = {
                isSeek.value = false
                isVideoLoading.value = true
                Log.e("slider", "设置")
                if (isRelease.value) {
                    isRelease.value = false;
                    isSeek.value = false
                    fmGlView.value?.reset(path, position.value.toLong())
                } else {
                    fmGlView.value?.seek(position.value.toLong())
                }
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White, // 修改滑块颜色
            ),
            track = { sliderState ->
//                SliderDefaults.Track(sliderPositions = sliderState)
                Track(sliderPositions = sliderState, cacheProgress = cacheProgress)
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
                fullHeight.value = portrait
            }
            onDispose {
//
            }
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


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun VideoPlayer(
    path: String,
    customModifier: Modifier = Modifier.height(250.dp),
    isDraggable: Boolean = true,
    isShowBack: Boolean = true,
    isFullWidth: Boolean = false,
    isReleasePlayer: Boolean = false,
    isShowControlDefault:Boolean = true,
    isLoop:Boolean = false,
    playerEnd: () -> Unit = {},
    isCover: (isCover:Boolean) ->Unit = {},
    onBack: () -> Unit = {}
) {

    val audioManager = LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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
        mutableStateOf(customModifier)
    }
    var cacheProgress by remember {
        mutableStateOf(0f)
    }
    var isVideoLoading = remember {
        mutableStateOf(true)
    }

    var isError by remember {
        mutableStateOf(false)
    }
    val isShowSpeed = rememberSaveable {
        mutableStateOf(false)
    }
    val configuration = LocalConfiguration.current

    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
//    val screenHeight = with(density) {configuration.screenHeightDp.dp.toPx()}
    var screenHeight by remember { mutableStateOf(0) }

    var tapOrientation: TapOrientation by remember {
        mutableStateOf(TapOrientation.LEFT)
    }
    var linearProgress by remember {
        mutableFloatStateOf(0f)
    }

    var position = rememberSaveable {
        mutableFloatStateOf(0f)
    }

    var progress = remember {
        mutableFloatStateOf(0f)
    }

    var isSeek = remember {
        mutableStateOf(false)
    }

    var fmGlView: MutableState<FmGlView?> = remember {
        mutableStateOf(null)
    };

    var isShowProgress by remember {
        mutableStateOf(false)
    }

    var isRelease = remember {
        mutableStateOf(false)
    }
    var isFull = rememberSaveable {
        mutableStateOf(false)
    }
    val playerImage: MutableState<Int> =
        remember { mutableStateOf(R.drawable.baseline_pause_24) }

    val isPlayer = rememberSaveable { mutableStateOf(true) }

    val videoSpeed = rememberSaveable {
        mutableDoubleStateOf(1.0)
    }

    val cacheDir = LocalContext.current.cacheDir.absolutePath
    var brightness by remember {
        mutableFloatStateOf(125f)
    }

    LaunchedEffect(path) {
        Log.e("videoPlayer", "修改")
        fmGlView.value?.release()
//        position.value = 0f
        isVideoLoading.value = true
        fmGlView.value?.reset(path, position.value.toLong())
    }

    LaunchedEffect(isReleasePlayer){
        Log.e("Page", isReleasePlayer.toString())
        if(isReleasePlayer) {
            Log.e("Page", "释放")
            playerEnd()
            fmGlView.value?.release()
            isRelease.value = true
        } else
            fmGlView.value?.reset(path, position.value.toLong())
    }


    val activity = LocalContext.current as? ComponentActivity
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

    DisposableEffect(Unit){
        onDispose {
            fmGlView.value?.release()

        }

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

    Box(
        modifier = modifier.value
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                screenHeight = coordinates.size.height
            }
            .background(Color.Black)
            .pointerInput(Unit) {
                // 监听点击事件
                detectTapGestures(
                    onPress = {
                        Log.e("onPress", "按下" + it.x)
                        isToucher = true
                        isShowControl = !isShowControl
                        if (it.y > screenHeight / 4 && it.y < screenHeight / 4 * 3) { // 取中间部分
                            if (it.x < screenWidth / 2) {
                                Log.e("left", "left")
                                tapOrientation = TapOrientation.LEFT
                            } else {
                                Log.e("right", "right")
                                tapOrientation = TapOrientation.RIGHT
                            }
                        } else {
                            tapOrientation = TapOrientation.NONE
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
                enabled = isDraggable,
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
                enabled = isDraggable,
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
                    } else if (tapOrientation == TapOrientation.LEFT) {
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
        AndroidView(factory = { context ->
            FmGlView(
                context = context,
                url = path,
                seekTime = position.value.toLong(),
                cachePath = cacheDir,
                isFullWidth = isFullWidth,
                isLoop = isLoop,
                width = screenWidth.toInt(),
                progress = { currentPosition, duration, cache, isSeekSuccess ->
                    isVideoLoading.value = false
                    cacheProgress = (cache.toFloat() / duration.toFloat()).toFloat()
//                    if (isSeekSuccess) {
//                        isSeek.value = false
//                    }
                    isCover(false)
                    if (!isSeek.value) //如果 seek 了，然后需要等seek完毕了，再更新了
                        position.value = currentPosition.toFloat()
                    progress.value = duration.toFloat()
                },
                endCallback = { error ->
                    fmGlView.value?.release()
                    isRelease.value = true
                    isVideoLoading.value = false

                    if (error) {
                        isError = true
                    } else {
                        playerEnd()
                    }
                }, onLoading = {
                    isVideoLoading.value = true
                })
        }, modifier = Modifier
            .fillMaxSize(), update = { it ->
            fmGlView.value = it
        }, onRelease = {
            it.release()
        }
        )
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

        if (isShowControl && isShowBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                tint = Color.White,
                contentDescription = stringResource(R.string.home_label_search),
                modifier = Modifier
                    .clickable {
                        if (isFull.value) {
                            isFull.value = false
                        } else {
                            // navController.popBackStack()
                            onBack()
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

        if (isShowControl && isShowControlDefault) {
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
                cacheProgress = cacheProgress,
                modifier = Modifier.align(
                    Alignment.BottomCenter
                ),
                portrait = customModifier,
                isRelease = isRelease,
                path = path,
                isVideoLoading = isVideoLoading
            )
        }



        DisposableEffect(isPlayer.value) {
            Log.e("测试", "修改了吗")
            if (isPlayer.value) {
                playerImage.value = R.drawable.baseline_pause_24
                fmGlView.value?.play()
            } else {
                playerImage.value = R.drawable.baseline_play_arrow_24
                fmGlView.value?.pause()
            }
            onDispose { /* cleanup logic here */ }
        }

        if((isShowControl && isShowControlDefault) || !isPlayer.value) {
            Icon(
                imageVector = ImageVector.vectorResource(playerImage.value),
                tint = Color.White,
                contentDescription = stringResource(R.string.home_label_search),
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        isPlayer.value = !isPlayer.value
                    }
                    .size(50.dp)
            )
        }



        if (isVideoLoading.value) {
            loading(color = R.color.white)
        }
        if (isError) {
            RequestError(modifier = Modifier
                .clickable {
                    fmGlView.value?.reset(
                        source = path,
                        position.value.toLong()
                    )
                    isVideoLoading.value = true
                    isError = false
                }
                .fillMaxHeight()
                .fillMaxWidth(), color = Color.White)
        }
    }
}