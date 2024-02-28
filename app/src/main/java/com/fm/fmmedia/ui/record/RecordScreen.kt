package com.fm.fmmedia.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.fm.fmmedia.BuildConfig
import com.fm.fmmedia.R
import com.fm.fmmedia.compose.CameraScreen
import com.fm.fmmedia.compose.PermissionRequestScreen
import com.fm.fmmedia.compose.RequestError
import com.fm.fmmedia.compose.loading
import com.fm.fmmedia.viewmodel.MemberInfoViewModel
import com.fm.fmplayer.encoder.AACEncoder
import com.fm.fmplayer.encoder.FmEncoder
import com.fm.fmplayer.encoder.H264Encoder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


val audioSource = MediaRecorder.AudioSource.MIC
val sampleRateInHz = 44100
val channelConfig = AudioFormat.CHANNEL_IN_MONO
val channelCount = 1
val audioFormat = AudioFormat.ENCODING_PCM_16BIT
val bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
val frameSize = channelCount * 1024 * 2 // 通道数 * 位数 * 每个声道采样数 16 bit 为两位
var audioRecord: AudioRecord? = null;

@OptIn(DelicateCoroutinesApi::class)
private fun startRecording(
    context: android.content.Context,
    isRecord: MutableState<Boolean>,
    endCall: () -> Unit,
    callAudioData: (data: ByteArray) -> Unit
) {
    try {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord =
            AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSize)
        var audioData = ByteArray(frameSize)
        audioRecord?.startRecording()
        val aacEncoder = AACEncoder(sampleRateInHz, channelCount);
        // 启动处理音频数据的协程
        GlobalScope.launch {
            while (isRecord.value) {
                val bytesRead = audioRecord?.read(audioData, 0, frameSize) ?: 0
                if (bytesRead == AudioRecord.ERROR_BAD_VALUE || bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(AACEncoder.TAG, "Error reading audio data")
                    continue;
                }
                if (bytesRead > 0) {
                    callAudioData(audioData)
                }
//                Log.e("测试", bytesRead.toString())
//                aacEncoder.encoder(audioRecord)
                // 关闭文件
            }
            endCall()
        }
    } catch (e: IOException) {
        Log.e("测试", e.message.toString())
        e.printStackTrace()
    }
}

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class
)
@Composable
fun CameraPermissionScreen(memberInfoViewModel: MemberInfoViewModel, onVideoUpload: (path: String) -> Unit = {}) {
    // 获取摄像头权限状态
    val permissionState = rememberMultiplePermissionsState(
        permissions =
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    val memberPublishInfo by memberInfoViewModel.memberPublishInfo.observeAsState()
    var isRecord = remember {
        mutableStateOf(false)
    }
//    val path = "${LocalContext.current.filesDir.absolutePath}/encoder.mp4"
    val filePath = "${LocalContext.current.filesDir.absolutePath}/encoder.mp4"
    val rtmpPath =
        "${BuildConfig.RTMP_BASE_URL}${memberPublishInfo?.token}"

    var path by remember {
        mutableStateOf(filePath)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }
    val h264Encoder: H264Encoder by remember {
        mutableStateOf(H264Encoder())
    }

    var millSeconds by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var fmEncoder: FmEncoder? by remember {
        mutableStateOf(null)
    }

    DisposableEffect(Unit) {
        // 在这里执行初始化操作
        onDispose {
            // 在这里执行清理操作
            isRecord.value = false
            audioRecord?.release()
        }
    }

    val isPermissionGranted = permissionState.allPermissionsGranted

    val context = LocalContext.current
    var index = 0;
    var isError: Boolean? by remember {
        mutableStateOf(false)
    }

    var currentIndex by remember {
        mutableStateOf(0)
    }

    val window = (context as? ComponentActivity)?.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // 请求权限
    LaunchedEffect(permissionState) {
        permissionState.launchMultiplePermissionRequest()
    }



    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            if (isPermissionGranted) {
                // 权限已授予，显示摄像头 UI
                Column(modifier = Modifier.systemBarsPadding()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CameraScreen(
                            isRecord = isRecord.value,
                            modifier = Modifier
                                .padding(paddingValues),
                            startTime = millSeconds,
                            onClick = {
                                isRecord.value = !isRecord.value
                                if (isRecord.value) { //这里有待优化
                                    startRecording(context, isRecord, {
                                        isError?.let {
                                            if (!it && currentIndex == 0)
                                                GlobalScope.launch {
                                                    isLoading = true
                                                }
                                        }
                                        fmEncoder?.endCoder()
                                        fmEncoder = null
                                        isError?.let {
                                            if (!it && currentIndex == 0) {
                                                GlobalScope.launch(Dispatchers.Main) {
                                                    onVideoUpload(path)
                                                }
                                            }
                                        }

                                    }) {
                                        fmEncoder?.addAudio(
                                            it,
                                            System.currentTimeMillis() - millSeconds
                                        )

                                    }
                                }
                            }) { image, rotate ->
                            if (fmEncoder == null) {
                                millSeconds = System.currentTimeMillis()
                                fmEncoder = FmEncoder(
                                    path,
                                    image.width,
                                    image.height,
                                    0,
                                    rotate,
                                    sampleRateInHz,
                                    channelCount,
                                    currentIndex
                                )

                            }
                            isError =
                                fmEncoder?.addVideo(image, System.currentTimeMillis() - millSeconds)
                            isError?.let {
                                if (it) {
                                    isRecord.value = false
                                    fmEncoder?.endCoder();
                                }
                            }
                            image.close()
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color.Black),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentIndex = 0
                                    path = filePath
                                },
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            Text(
                                text = "录制", color =
                                if (currentIndex == 0) Color.Red else Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentIndex = 1
                                    path = rtmpPath

                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "直播",
                                color = if (currentIndex == 1) Color.Red else Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                if (isLoading) {
                    loading(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { }, color = R.color.white
                    )
                }
            } else {
                // 权限未授予，显示权限请求 UI
                PermissionRequestScreen(
                    onPermissionDenied = {
                        // 处理权限被拒绝的情况
                        permissionState.launchMultiplePermissionRequest()
                    },
                    onOpenSettings = {
                        // 用户点击打开应用设置
                        // 在这里你可以打开应用的设置页面，让用户手动授予权限
                    }
                )
            }

            if (isError == true) {
                RequestError(modifier = Modifier
                    .clickable {
                        isError = false
                    }
                    .fillMaxHeight()
                    .fillMaxWidth(), color = Color.White)
            }
        }
    }
// 绘制 UI

}


@Composable
fun RecordScreen(memberInfoViewModel: MemberInfoViewModel,onVideoUpload: (path: String) -> Unit = {}) {
    Text(text = "record")
    CameraPermissionScreen(memberInfoViewModel, onVideoUpload)
}