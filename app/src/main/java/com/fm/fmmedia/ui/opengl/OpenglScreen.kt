package com.fm.fmmedia.ui.opengl

import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.fm.fmmedia.compose.OpenGlView
import com.fm.openglrender.OpenglRender


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun OpenglScreen (){
    // 获取当前的生命周期所有者
    val lifecycleOwner = LocalLifecycleOwner.current

    // 获取当前的上下文
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 创建SurfaceView，并将其包装在Compose中
        AndroidView(
            factory = { ctx ->
                OpenGlView(context)
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = {
                it.release()
            }
        )
    }
}