package com.fm.fmmedia.compose

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors


import android.media.Image
import android.util.Size
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun VideoPreview(
    cameraController: LifecycleCameraController,
    modifier: Modifier = Modifier
) {


    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    AndroidView(
        factory = { context: Context ->
            //预览视图
            PreviewView(context).apply {
                this.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    isRecord: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    imageResult: (image: Image) -> Unit = {}
) {
    val context = LocalContext.current

    // 获取 Application 的上下文
    val applicationContext = context.applicationContext


    val cameraController: LifecycleCameraController = remember {
        LifecycleCameraController(applicationContext).apply {
//            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(isRecord) {
        if (isRecord) {
            val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY).build()
            cameraController.imageAnalysisResolutionSelector = resolutionSelector

            cameraController.setImageAnalysisAnalyzer(executor) { imageProxy ->
                imageProxy.image?.let { image ->
                    imageResult(image)
                }
                imageProxy.close()
            }
        } else {
            cameraController.clearImageAnalysisAnalyzer()
        }

        onDispose {
            cameraController.clearImageAnalysisAnalyzer()
        }
    }


    //设置前后摄像头的图标状态
    var cameraIcon by remember { mutableStateOf(Icons.Default.Autorenew) }
    var recordIcon by remember {
        mutableStateOf(Icons.Default.PlayCircleOutline)
    }
    if (isRecord) {
        recordIcon = Icons.Default.PauseCircleOutline
    } else {
        recordIcon = Icons.Default.PlayCircleOutline
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //视频预览的界面
        VideoPreview(cameraController = cameraController, modifier = Modifier.fillMaxSize())
        //左上角的摄像头前后镜图标
        Box(modifier = modifier.align(Alignment.BottomEnd).fillMaxWidth()){
            IconButton(onClick = {
                //照相机前后摄像头的切换
                if (cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    //设置后镜头
                    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    //设置前镜头
                    cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                }
            }, modifier = Modifier.align(Alignment.BottomEnd)) {
                Icon(
                    imageVector = cameraIcon,
                    tint = Color.White,
                    contentDescription = "摄像头",
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = {
                    onClick()
                }, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(50.dp)
            ) {
                Icon(
                    imageVector = recordIcon,
                    tint = Color.White,
                    contentDescription = "",
                    modifier = Modifier.size(50.dp)
                )
            }

        }

    }
}