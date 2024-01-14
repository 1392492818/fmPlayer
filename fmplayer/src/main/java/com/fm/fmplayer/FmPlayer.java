package com.fm.fmplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.fm.fmplayer.view.FmGLSurfaceView;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class FmPlayer implements FmPlayerDataCallback {
    static {
        System.loadLibrary("video");
    }

    private final static String TAG = FmPlayer.class.getSimpleName();
    public String id;

    public native void startPlayer(String url, FmPlayerDataCallback fmPlayerDataCallback, String id);

    public native void stop(String id);

    public native void play(String id);

    public native void pause(String id);

    private boolean isSeek = false;

    public native void seek(String id, long time);

    private AudioTrack audioTrack;
    private FmDecoder fmDecoder;
    private Surface surface;
    private FmGLSurfaceView fmGLSurfaceView;
    private PlayerCallback playerCallback;
    //线程池 任务队列
    BlockingQueue<Runnable> taskQueue;
    ExecutorService executorService;

    public FmPlayer() {
        Log.e(TAG, " 执行多次");
        this.id = UUID.randomUUID().toString().replace("-", "");
        taskQueue = new LinkedBlockingQueue<>(10);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void play() {
        try {
            if (executorService.isShutdown()) return;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    play(id);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void seek(long time) {
        try {
            if (executorService.isShutdown()) return;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    seek(id, time);
                    isSeek = true;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void pause() {
        try {
            if (executorService.isShutdown()) return;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    pause(id);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }


    public void start(String url, Surface surface, FmGLSurfaceView fmGLSurfaceView, PlayerCallback playerCallback) {
        Log.e(TAG, id);
        this.surface = surface;
        this.playerCallback = playerCallback;
        this.fmGLSurfaceView = fmGLSurfaceView;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                startPlayer(url, FmPlayer.this, id);
            }
        });
    }

    public void release() {
        this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, id);
                    stop(id);
                } catch (Exception e) {
                    Log.e(TAG, "stop" + e.getMessage());
                }

            }
        });
        this.executorService.shutdown();
        synchronized (this) {
            if (fmDecoder != null) fmDecoder.release();
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }
        }
    }

    private void stopLoading() {
        if (playerCallback != null) playerCallback.stopLoading();
    }

    @Override
    public void onVideoFrame(int width, int height, byte[] yData, byte[] uData, byte[] vData) {
        stopLoading();
        if (playerCallback != null) playerCallback.softwareDecoder();
        if (playerCallback != null) playerCallback.voidInfo(width, height);
        if (fmGLSurfaceView != null) fmGLSurfaceView.draw(width, height, yData, uData, vData);
    }

    @Override
    public void onAudioFrame(byte[] audioData, int sampleRate, int nbSamples, int channels, int avSampleFormat, int dataSize) {
        stopLoading();
        try {
            if (this.audioTrack == null) {
                int audioFormat = avSampleFormat % 4 + 1;
                int mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                        channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT);//计算最小缓冲区
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate,
                        (channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                        AudioFormat.ENCODING_PCM_16BIT,
                        //sampleRate * channelCount * 8 / 16 / 50,
                        mMinBufferSize,
                        AudioTrack.MODE_STREAM);
                audioTrack.play();
            }
            // 播放PCM_FLOAT数据
            audioTrack.write(audioData, 0, audioData.length);
        } catch (Exception e) {

        }

    }

    @Override
    public void onProgress(long time, long currentTime) {
//        Log.e(TAG, "time:"+ time + ", currentTime:" + currentTime);
        playerCallback.progress(currentTime, time, isSeek);
        if (isSeek) isSeek = false;
    }


    @Override
    public boolean onVideoPacket(int width, int height, int format, byte[] packet) {
        stopLoading();
        if (playerCallback != null) playerCallback.voidInfo(width, height);
        if (fmDecoder == null) {
            fmDecoder = new FmDecoder();
            fmDecoder.setSurfaceView(surface);
            boolean isInit = fmDecoder.init(width, height, format, null, null);
            if (!isInit) {
                return false;
            }
        }

//        setVideoSize(width, height);
//        return false;
//        return true;
        return fmDecoder.onPacket(packet);
    }

    @Override
    public void onEnd() {
        playerCallback.end();
    }


}
