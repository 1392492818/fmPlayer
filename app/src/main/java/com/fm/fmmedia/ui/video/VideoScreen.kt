package com.fm.fmmedia.ui.video

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.navigation.NavHostController
import com.fm.fmmedia.R
import com.fm.fmmedia.compose.FmGlView
import com.fm.fmmedia.compose.LifecycleEffect
import com.fm.fmmedia.compose.formatSecondsToHHMMSS
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.repository.VideoGroupRepository
import com.fm.fmmedia.ui.Screen
import com.fm.fmmedia.viewmodel.VideoGroupViewModel
import kotlinx.coroutines.delay


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
    fmGlView: FmGlView?,
    position: MutableState<Float>,
    progress: MutableState<Float>,
    isSeek: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLandscape by remember {
        mutableStateOf(false)
    }

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
                fmGlView?.seek(position.value.toLong())
            },
            modifier = Modifier
                .padding(5.dp)
                .weight(1f),
            valueRange = 0f..progress.value
        )

        Text(text = formatSecondsToHHMMSS(progress.value.toLong()), color = Color.White)
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_fullscreen_24),
            tint = Color.White,
            contentDescription = stringResource(
                id = R.string.play_play_icon_text
            ),
            modifier = Modifier
                .clickable {
                    isLandscape = !isLandscape
                    if (isLandscape) {
                        // 设置横屏方向
                        (context as? ComponentActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        // 恢复为竖屏方向
                        (context as? ComponentActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
                .width(30.dp)
                .height(30.dp)
        )

        DisposableEffect(isPlayer.value) {
            if (isPlayer.value) {
                playerImage.value = R.drawable.baseline_pause_24
                fmGlView?.play()
            } else {
                playerImage.value = R.drawable.baseline_play_arrow_24
                fmGlView?.pause()
            }
            onDispose { /* cleanup logic here */ }
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun videoScreen(
    id: Int,
    navController: NavHostController,
    videoGroupViewModel: VideoGroupViewModel
) {
    Log.e("测试", "videoScreen")
    val videoGroup by videoGroupViewModel.videoGroup.observeAsState()
    LaunchedEffect(Unit) {
        videoGroupViewModel.findIdGroup(id)
    }


    val width = remember { mutableStateOf(0) }
    val height = remember { mutableStateOf(0) }
    val playerImage: MutableState<Int> =
        remember { mutableStateOf(R.drawable.baseline_pause_24) }
    val isPlayer = remember { mutableStateOf(true) }

    var progress = remember {
        mutableFloatStateOf(0f)
    }
    var position = remember {
        mutableFloatStateOf(0f)
    }
    var fmGlView: FmGlView? = null;
    var isSeek = remember {
        mutableStateOf(false)
    }
    var selectSource by remember {
        mutableStateOf("")
    }
    var selectIndex by remember {
        mutableIntStateOf(0)
    }


    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    var tapOrientation: TapOrientation by remember {
        mutableStateOf(TapOrientation.LEFT)
    }
    var linearProgress by remember {
        mutableFloatStateOf(0f)
    }
    LifecycleEffect(onResume = {
        isPlayer.value = true
    }, onPause = {
        isPlayer.value = false
    })


    var brightness by remember {
        mutableFloatStateOf(125f)
    }
    val activity = LocalContext.current as? ComponentActivity


    Log.e("测试", "亮度" + brightness)

    val audioManager = LocalContext.current.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var volume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }

    var isShowProgress by remember {
        mutableStateOf(false)
    }

    var settingIcon by
    remember { mutableStateOf(R.drawable.baseline_volume_mute_24) }

    val volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val volumeMin = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
    var isShowControl by remember {
        mutableStateOf(true)
    }
    var isToucher by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if(isToucher) {
                isToucher = false
            } else {
                isShowProgress = false
                isShowControl = false
            }

        }
    }

    if (videoGroup != null && videoGroup?.id == id) {
        Scaffold { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth()
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            // 监听点击事件
                            detectTapGestures(
                                onPress = {
                                    Log.e("onPress", "按下" + it.x)
                                    isToucher = true
                                    isShowControl = true
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
                                fmGlView?.seek(position.value.toLong())
//                                offsetX += delta
                            })
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                // 得到相比于上次改变的位移
//                                offsetX += delta
                                if (tapOrientation == TapOrientation.RIGHT) {
                                    Log.e(
                                        "音量",
                                        delta.toString() + "min:" + volumeMin + ",max:" + volumeMax
                                    )
                                    volume -= delta.toInt()
                                    if (volume > volumeMax) volume = volumeMax
                                    if (volume < volumeMin) volume = volumeMin

                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        volume,
                                        0
                                    );
                                    linearProgress =
                                        (volume.toFloat() / volumeMax.toFloat()).toFloat()
                                    isShowProgress = true
                                    settingIcon = R.drawable.baseline_volume_mute_24
                                } else {
                                    Log.e("亮度", delta.toString())
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
                                progress = { currentPosition, duration, isSeekSuccess ->
                                    if (isSeekSuccess) {
                                        isSeek.value = false
                                    }
                                    if (!isSeek.value) //如果 seek 了，然后需要等seek完毕了，再更新了
                                        position.value = currentPosition.toFloat()
                                    progress.value = duration.toFloat()
                                },
                                endCallback = {
                                    if (selectIndex + 1 != videoGroup?.video?.size) {
                                        selectIndex += 1

                                        val source = videoGroup?.video?.get(selectIndex)
                                        source?.let {
                                            fmGlView?.reset(source = it.source)
                                            selectSource = it.source
                                            isPlayer.value = true
                                        }

                                    }
                                })
                        }, modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { layoutCoordinates ->
                                // 获取宽度和高度
                                width.value = layoutCoordinates.size.width
                                height.value = layoutCoordinates.size.height
                            }, update = { it ->
                            fmGlView = it
                        }
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        tint = Color.White,
                        contentDescription = stringResource(R.string.home_label_search),
                        modifier = Modifier
                            .clickable {
                                navController.popBackStack()
                                fmGlView?.release()
                            }
                            .padding(10.dp)
                    )

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
                            modifier = Modifier.align(
                                Alignment.BottomCenter
                            )
                        )
                    }

                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
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
                                                fmGlView?.reset(it.source)
                                                selectSource = it.source
                                                selectIndex = index
                                                isPlayer.value = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectSource == it.source) {
                                            Text(text = it.order.toString(), color = Color.Red)
                                        } else {
                                            Text(text = it.order.toString(), color = Color.White)
                                        }

                                    }
                                }
                            }
                        }
                    }



                    repeat(100) {
                        Text("Item $it", modifier = Modifier.padding(2.dp))
                    }


                }

            }

        }
    } else {
        loading()
    }
    BackHandler(enabled = true) {
        Log.e("back", "返回")
        fmGlView?.release()
        navController.popBackStack()
    }
}