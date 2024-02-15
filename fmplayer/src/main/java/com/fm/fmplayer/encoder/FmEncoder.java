package com.fm.fmplayer.encoder;

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.util.Log;

public class FmEncoder {

    static {
        System.loadLibrary("video");
    }
    private static native void encoder(String path, int width, int height,int format,int rotate, int sampleRate, int channel);

    private static native void addVideoFrame(byte[] data, long pts);

    private static native void addAudioFrame(byte[] data, long pts);

    private static native void stopEncoder();

    private int width;
    private int height;
    private int format;
    private int sampleRate;
    private int channel;

    private int rotate = 0;


    public FmEncoder(int width, int height, int format, int rotate, int sampleRate,int channel) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.rotate = rotate;
        this.encoder("/data/data/com.fm.fmmedia/files/test.mp4", this.width, this.height, this.format, this.rotate, this.sampleRate, this.channel);
    }

    public void addVideo(byte[] data, long milliSeconds){
        this.addVideoFrame(data, milliSeconds);
    }

    public void addVideo(Image image, long pts) {
        byte[] data = ImageFormatUtil.getYuv420p(image);
        this.addVideo(data, pts);
    }

    public void addAudio(byte[] data, long milliSeconds){
        this.addAudioFrame(data, milliSeconds);
    }

    public void endCoder(){
        Log.e("测试", "调用结束");
        this.stopEncoder();
    }

}
