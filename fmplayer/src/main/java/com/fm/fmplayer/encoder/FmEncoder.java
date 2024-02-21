package com.fm.fmplayer.encoder;

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.nfc.Tag;
import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FmEncoder {
    private final static String TAG = FmEncoder.class.getSimpleName();

    static {
        System.loadLibrary("video");
    }

    private static native void encoder(String path, int width, int height, int format, int rotate, int sampleRate, int channel);

    private static native void addVideoFrame(byte[] data, long pts);

    private static native void addAudioFrame(byte[] data, long pts);

    private static native void stopEncoder();

    private int width;
    private int height;
    private int format;
    private int sampleRate;
    private int channel;

    private int rotate = 0;

    private boolean isEnd = false;
    private ConcurrentLinkedQueue<ImageData> imageDataConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    private Thread videoEncoderThread;

    public FmEncoder(String path, int width, int height, int format, int rotate, int sampleRate, int channel) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.rotate = rotate;

        this.encoder(path, this.width, this.height, this.format, this.rotate, this.sampleRate, this.channel);
        startVideoEncoderThread();
    }

    private void startVideoEncoderThread() {
        videoEncoderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isEnd && imageDataConcurrentLinkedQueue.isEmpty()) {
                        break;
                    }
                    ImageData imageData = imageDataConcurrentLinkedQueue.poll();
                    if (imageData != null) {
                        addVideo(imageData.getData(), imageData.getPts());
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
        videoEncoderThread.start();
    }

    public void addVideo(byte[] data, long milliSeconds) {
        this.addVideoFrame(data, milliSeconds);
    }

    public void addVideo(Image image, long pts) {
        long start = System.currentTimeMillis();
        imageDataConcurrentLinkedQueue.add(ImageFormatUtil.getYuv420pImageData(image, pts));
//        Log.e("测试", String.valueOf(System.currentTimeMillis() - start));

//        byte[] data = ImageFormatUtil.getYuv420p(image);
//        this.addVideo(data, pts);
    }

    public void addAudio(byte[] data, long milliSeconds) {
        this.addAudioFrame(data, milliSeconds);
    }

    public void endCoder() {
        isEnd = true;
        while (!imageDataConcurrentLinkedQueue.isEmpty()) {
            Log.e(TAG, String.valueOf(imageDataConcurrentLinkedQueue.size()));
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        this.stopEncoder();
    }

}
