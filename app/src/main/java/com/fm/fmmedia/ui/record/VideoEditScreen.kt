package com.fm.fmmedia.ui.record

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.fm.fmmedia.R
import com.fm.fmmedia.compose.FmGlView
import com.fm.fmmedia.compose.LifecycleEffect
import com.fm.fmmedia.ui.theme.FmMediaTheme

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun VideoEditScreen(navController: NavController, path: String, next: (path: String) -> Unit) {
    val context = LocalContext.current
    var fmGlView: MutableState<FmGlView?> = remember {
        mutableStateOf(null)
    };
    LifecycleEffect(onResume = {
    }, onPause = {

    }, onDestroy = {
        Log.e("测试", "onDestroy")
        fmGlView.value?.release()
    })
    // 获取临时目录
    val cacheDir = context.cacheDir.absolutePath
    Scaffold { paddingValues ->
        Box {

            AndroidView(factory = { context ->
                FmGlView(
                    context = context,
                    url = path,
                    seekTime = 0,
                    isLocalFile = true,
                    isLoop = true,
                    cachePath = cacheDir,
                    progress = { currentPosition, duration, cache, isSeekSuccess ->
                    },
                    endCallback = { error ->
                        Log.e("测试", "播放结束")
                    }, onLoading = {
                    })
            }, modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    // 获取宽度和高度

                }, update = { it ->
                fmGlView.value = it
            }, onRelease = {
                it.release()
            }
            )

            Row(modifier = Modifier
                .padding(10.dp, paddingValues.calculateTopPadding())
                .clickable {
                    navController.popBackStack()
                }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    tint = Color.White,
                    contentDescription = "",
                )
            }

            Row(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)) {
                Button(
                    onClick = {
                        next(path)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(5.dp), // 圆角形状，8dp的圆角
                    contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FmMediaTheme.colors.iconPrimary, // 按钮的背景颜色
                        contentColor = Color.White // 按钮的内容颜色（文本和图标）
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.video_screen_edit_next_step),
                        fontSize = 16.sp
                    )
                }
            }

        }

    }

}