package com.fm.fmmedia.ui.record

import android.Manifest
import android.content.Context
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.fm.fmmedia.compose.PermissionRequestScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun VideoPreview(
    cameraController: LifecycleCameraController,
    modifier: Modifier = Modifier
) {

    // 创建 ImageAnalysis 用于处理视频帧
    val analysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

//    analysis.setAnalyzer()
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
fun CameraScreen() {
    val context = LocalContext.current

    // 获取 Application 的上下文
    val applicationContext = context.applicationContext

    var isRecord by remember {
        mutableStateOf(false)
    }

    val cameraController: LifecycleCameraController = remember {
        LifecycleCameraController(applicationContext).apply {
//            setEnabledUseCases(CameraController.VIDEO_CAPTURE)
        }
    }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(isRecord) {
        if (isRecord) {
            cameraController.setImageAnalysisAnalyzer(executor) { imageProxy ->
                Log.e("测试", imageProxy.width.toString())
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
    Box(modifier = Modifier.fillMaxSize()) {
        //视频预览的界面
        VideoPreview(cameraController = cameraController, modifier = Modifier.fillMaxSize())
        //左上角的摄像头前后镜图标
        IconButton(onClick = {
            //照相机前后摄像头的切换
            if (cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                //设置后镜头
                cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

//                cameraIcon = Icons.Default.VideoCameraBack
            } else {
                //设置前镜头
                cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//                cameraIcon = Icons.Default.VideoCameraFront
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
                isRecord = !isRecord
            }, modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                tint = Color.White,
                contentDescription = "",
                modifier = Modifier.size(50.dp)
            )
        }
    }


}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionScreen() {
    // 获取摄像头权限状态
    val permissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val recordAudioPermissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)
    val isPermissionGranted =
        permissionState.hasPermission && recordAudioPermissionState.hasPermission

    val context = LocalContext.current

    // 请求权限
    LaunchedEffect(permissionState) {
        if (!isPermissionGranted) {
            permissionState.launchPermissionRequest()
            recordAudioPermissionState.launchPermissionRequest()
        }
    }

    // 绘制 UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPermissionGranted) {
            // 权限已授予，显示摄像头 UI
            CameraScreen()
        } else {
            // 权限未授予，显示权限请求 UI
            PermissionRequestScreen(
                onPermissionDenied = {
                    // 处理权限被拒绝的情况
                },
                onOpenSettings = {
                    // 用户点击打开应用设置
                    // 在这里你可以打开应用的设置页面，让用户手动授予权限
                }
            )
        }
    }
}


@Composable
fun RecordScreen() {
    Text(text = "record")
    CameraPermissionScreen()
}