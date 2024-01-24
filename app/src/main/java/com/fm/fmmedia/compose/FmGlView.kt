package com.fm.fmmedia.compose

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fm.fmplayer.FmPlayer
import com.fm.fmplayer.PlayerCallback
import com.fm.fmplayer.render.VideoRenderOES


@Composable
fun TextureView(glView: FmGlView) {

}

@Composable
fun LifecycleEffect(
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> onCreate?.invoke()
                Lifecycle.Event.ON_START -> onStart?.invoke()
                Lifecycle.Event.ON_RESUME -> onResume?.invoke()
                Lifecycle.Event.ON_PAUSE -> onPause?.invoke()
                Lifecycle.Event.ON_STOP -> onStop?.invoke()
                Lifecycle.Event.ON_DESTROY -> onDestroy?.invoke()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

fun formatSecondsToHHMMSS(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}


@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("ViewConstructor")
class FmGlView(context: Context, url:String, seekTime:Long,cachePath:String, progress:(currentPosition:Long, duration:Long, isSeek:Boolean)->Unit, endCallback:(isError:Boolean)->Unit = {}, onLoading: ()->Unit) : GLSurfaceView(context),PlayerCallback {
    private lateinit var videoRenderOES: VideoRenderOES;
    private lateinit var surface: Surface;
    private val TAG:String = FmGlView::class.simpleName.toString();
    private var  fmPlayer:FmPlayer = FmPlayer();
    private val baseUrl = "http://192.168.0.149:9090/videos/"
    var callback: (currentPosition:Long, duration:Long, isSeek: Boolean)->Unit;
    var endCallback: (isError:Boolean)->Unit;
    var onLoading: ()->Unit;
    var seekTime:Long
    var cachePath:String
    init {
        this.setEGLContextClientVersion(2)
        this.cachePath = cachePath
        callback = progress;
        this.seekTime = seekTime
        this.endCallback = endCallback
        this.onLoading = onLoading
        val sourceUrl = baseUrl + url

        videoRenderOES = VideoRenderOES(context, this.width, this.height) { videoTexture ->
            this.surface = Surface(videoTexture)
            startPlayer(sourceUrl)
        }
        this.setRenderer(videoRenderOES)
    }

    fun startPlayer(source:String){
        fmPlayer.start(source, this.surface, null, this, seekTime, this.cachePath)
        fmPlayer.play()
//        fmPlayer.seek(seekTime)
    }

    fun reset(source: String, seekTime: Long = 0){
        try{
            this.seekTime = seekTime
            fmPlayer.release()
            fmPlayer = FmPlayer()
            startPlayer(baseUrl+source)
        }catch (e:Exception){

        }

    }

    fun release(){
        Log.e(TAG, "停止")
        fmPlayer.release()
    }

    fun pause(){
        fmPlayer.pause()
    }
    fun play() {
        fmPlayer.play()
    }

    fun seek(time:Long) {
        fmPlayer.seek(time)
    }

    fun setSpeed(speed: Float){
        fmPlayer.setSpeed(speed)
    }

    override fun stopLoading() {
    }

    override fun softwareDecoder() {
    }

    override fun loading() {
        this.onLoading()
    }

    override fun voidInfo(width: Int, height: Int) {
        this.videoRenderOES.draw(width, height)
    }

    override fun progress(currentTime: Long, duration: Long, isSeek:Boolean) {
        this.callback(currentTime, duration, isSeek);
    }

    override fun end(isError: Boolean) {
        this.endCallback(isError)
    }
}