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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.fm.fmmedia.R
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign


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
                this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
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
    startTime: Long = 0,
    imageResult: (image: Image, rotate: Int) -> Unit = { image, rotate -> },
) {
    val context = LocalContext.current

    // 获取 Application 的上下文
    val applicationContext = context.applicationContext

    var duration by remember {
        mutableStateOf(formatSecondsToMMSS((System.currentTimeMillis() - startTime) / 1000))
    }
    val cameraController: LifecycleCameraController = remember {
        LifecycleCameraController(applicationContext).apply {
//            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(isRecord) {
        if (isRecord) {
            val resolutionSelector = ResolutionSelector.Builder().setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
            ).build()
            cameraController.imageAnalysisResolutionSelector = resolutionSelector
            cameraController.setImageAnalysisAnalyzer(executor) { imageProxy ->
//                Log.e("测试", "${imageProxy.imageInfo.rotationDegrees}")
                imageProxy.image?.let { image ->
                    imageResult(image, imageProxy.imageInfo.rotationDegrees)
                    duration = formatSecondsToMMSS((System.currentTimeMillis() - startTime) / 1000)
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
        mutableStateOf(R.drawable.record_video)
    }



    if (isRecord) {
        recordIcon = R.drawable.record_video
    } else {
        recordIcon = R.drawable.stop_record
        duration = ""
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //视频预览的界面
        VideoPreview(cameraController = cameraController, modifier = Modifier.fillMaxSize())
        //左上角的摄像头前后镜图标
        Box(
            modifier = modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            if (isRecord == false) {
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
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(50.dp)
                    .height(70.dp)
            ) {

                Text(
                    text = duration,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )


                IconButton(
                    onClick = {
                        onClick()
                    }, modifier = Modifier
                        .size(50.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(recordIcon),

                        tint = Color.White,
                        contentDescription = "",
                        modifier = Modifier.size(50.dp)
                    )
                }

            }


        }

    }
}