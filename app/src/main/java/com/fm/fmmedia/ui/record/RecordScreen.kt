package com.fm.fmmedia.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.fm.fmmedia.compose.CameraScreen
import com.fm.fmmedia.compose.PermissionRequestScreen
import com.fm.fmplayer.encoder.AACEncoder
import com.fm.fmplayer.encoder.FmEncoder
import com.fm.fmplayer.encoder.H264Encoder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException


val audioSource = MediaRecorder.AudioSource.MIC
val sampleRateInHz = 44100
val channelConfig = AudioFormat.CHANNEL_IN_MONO
val channelCount = 1
val audioFormat = AudioFormat.ENCODING_PCM_16BIT
val bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
val frameSize =  channelCount * 1024 * 2 // 通道数 * 位数 * 每个声道采样数
var audioRecord: AudioRecord? = null;

@OptIn(DelicateCoroutinesApi::class)
private fun startRecording(context: android.content.Context, isRecord: MutableState<Boolean>,endCall:()->Unit, callAudioData: (data: ByteArray)->Unit) {
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
    ExperimentalMaterial3Api::class
)
@Composable
fun CameraPermissionScreen() {
    // 获取摄像头权限状态
    val permissionState = rememberMultiplePermissionsState(
        permissions =
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    var isRecord = remember {
        mutableStateOf(false)
    }
    val h264Encoder:H264Encoder by remember {
        mutableStateOf(H264Encoder())
    }

    var millSeconds by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    var fmEncoder:FmEncoder? by remember {
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
                CameraScreen(isRecord = isRecord.value, modifier = Modifier.padding(paddingValues), onClick = {
                    isRecord.value = !isRecord.value
                    if(isRecord.value) {
                        millSeconds = System.currentTimeMillis()
                        startRecording(context, isRecord, {
                            fmEncoder?.endCoder()
                            fmEncoder = null
                        }){
                            fmEncoder?.addAudio(it, System.currentTimeMillis() - millSeconds)
                        }
                    }
                }) { image ->
                    if(fmEncoder == null)
                     fmEncoder = FmEncoder(image.width, image.height, 0, sampleRateInHz, channelCount)

                    fmEncoder?.addVideo(image, System.currentTimeMillis() - millSeconds)
//                    Log.e("测试", index++.toString())
                    //mediacodec 编码代码
//                    if (!h264Encoder.isInit) {
//                        h264Encoder.init(image.width, image.height, 30)
//                    } else {
//                        h264Encoder.encoder(image)
////                        image.close()
//                    }
                    image.close()
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
        }
    }
    // 绘制 UI

}


@Composable
fun RecordScreen() {
    Text(text = "record")
    CameraPermissionScreen()
}