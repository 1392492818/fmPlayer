package com.fm.fmplayer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FmDecoder {

    private final static String TAG = FmDecoder.class.getSimpleName();
    private MediaFormat h264MediaFormat = null;
    private MediaCodec h264MediaCodec = null;
    private long timeOutUs = 1000;
//    private final static String MIME_TYPE = "video/x-vnd.on2.vp8";
    private String MIME_TYPE = "video/avc";

    public void setSurfaceView(Surface surfaceView) {
        this.surfaceView = surfaceView;
    }

    private Surface surfaceView = null;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int width;
    private byte[] sps;
    private byte[] pps;
    public int height;
    public boolean isRunning = false;
    public int mediaFormatNum = 0;
    public int decoderWidth = 0;
    public int decoderHeight = 0;

    public final int AV_CODEC_ID_H264  = 27;
    public final int AV_CODEC_ID_VP8 = 140;

    public final int AV_CODE_ID_MPEG4 = 12;

    public final int AV_CODEC_ID_VP9 = 167;
    public final int AV_CODEC_ID_HEVC = 173;

    public FmDecoder() {
        Log.e(TAG,"实例化了");
    }

    public boolean init(int width, int height,int format, byte[] sps, byte[] pps) {
//        this.release();
        this.width = width;
        this.height = height;
        this.sps = sps;
        this.pps = pps;
        isRunning = true;
        Log.e(TAG, "format:" + String.valueOf(format));
        switch (format) {
            case AV_CODEC_ID_H264:
                this.MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
                break;
            case AV_CODEC_ID_HEVC:
                this.MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_HEVC;
                break;
            case AV_CODEC_ID_VP8:
                this.MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_VP8;
                break;
            case AV_CODEC_ID_VP9:
                this.MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_VP9;
                break;
            case AV_CODE_ID_MPEG4:
                this.MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_MPEG4;
                break;
            default:
                return false;
        }
        return hardDecoder();
    }



    public int getMediaFormat(){
        return this.mediaFormatNum;
    }


    /**
     * 硬解码
     *
     * @return
     */
    private boolean hardDecoder() {
        try {

            h264MediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
//            if(this.MIME_TYPE.equals(MediaFormat.MIMETYPE_VIDEO_AVC)) {
//                h264MediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(this.sps));
//                h264MediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(this.pps));
//            }
            if (surfaceView == null) {
                Log.e(TAG, "surfaceView 为null");
            }
            h264MediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
            h264MediaCodec.configure(h264MediaFormat, surfaceView, null, 0); //没有填 surface 可以获取到数据
            h264MediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "解码异常");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "异常");
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void getMediaOutFormat(int outputIndex) {
        MediaFormat mediaFormat = h264MediaCodec.getOutputFormat(outputIndex);
        int keyColor = mediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
        decoderWidth = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        decoderHeight = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        mediaFormatNum = keyColor;
//        Log.e("decoder", decoderWidth + " " + width);
//        Log.e("decoder", decoderHeight + " " + height);
    }



    public boolean onPacket(byte[] buf) {
//        Log.e(TAG, buf.length + "");
        if (surfaceView != null && !surfaceView.isValid()) { //屏幕暂时不可以渲染，等待可渲染
            Log.e(TAG, "不可以渲染");
            return false;
        }
        if(h264MediaCodec == null) {
            Log.e(TAG, "h264MediaCodec 为 null");
            return false;
        }

        try {
            while (true) {
                int inputBufferIndex = h264MediaCodec.dequeueInputBuffer(timeOutUs);//获取输入缓冲区下标
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputByteBuffer = h264MediaCodec.getInputBuffer(inputBufferIndex);
                    inputByteBuffer.clear();
                    inputByteBuffer.put(buf);
                    h264MediaCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, timeOutUs, 0);
                    break;
                } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // 暂时无法获取输入缓冲区，稍后重试
                    // 这可能是因为输入缓冲区已满，需要等待一段时间再次尝试
                    Log.e(TAG, "暂时无法获取输入缓冲区，稍后重试");
                } else if (inputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 输入格式发生了变化
                    // 获取新的输出格式信息
                    Log.e(TAG, "输入格式发生了变化");

                    // 处理新的输出格式信息
                }  else {
                    Log.e(TAG, "input 异常");
                    return false;
                }
                Thread.sleep(1);
            }

            try {
                // Get output buffer index
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = h264MediaCodec.dequeueOutputBuffer(bufferInfo, timeOutUs);

                while (outputBufferIndex >= 0) {
                    h264MediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = h264MediaCodec.dequeueOutputBuffer(bufferInfo, timeOutUs);
                }

            } catch (Exception e) {
                Log.e(TAG, "output异常" + e.getMessage());

                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            Log.e(TAG, "缓冲区硬解码失败" + message);
            return false;
        }
        return true;
    }

    /**
     * 解码过程
     *
     * @param buf
     * @return
     */
    public  List<byte[]> onFrame(byte[] buf) {
        List<byte[]> list = new ArrayList<>();
        if (surfaceView != null && !surfaceView.isValid()) { //屏幕暂时不可以渲染，等待可渲染
            Log.e("fuweicong", "不可以渲染");
            return list;
        }

        try {
            int inputBufferIndex = h264MediaCodec.dequeueInputBuffer(timeOutUs);//获取输入缓冲区下标
            if (inputBufferIndex >= 0) {
                ByteBuffer inputByteBuffer = h264MediaCodec.getInputBuffer(inputBufferIndex);
                inputByteBuffer.clear();
                inputByteBuffer.put(buf);
                h264MediaCodec.queueInputBuffer(inputBufferIndex, 0, buf.length, timeOutUs, 0);
            } else {
                return list;
            }

            try {
                // Get output buffer index
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = h264MediaCodec.dequeueOutputBuffer(bufferInfo, timeOutUs);
                while (outputBufferIndex >= 0) {
                    getMediaOutFormat(outputBufferIndex);
                    ByteBuffer buffer = h264MediaCodec.getOutputBuffer(outputBufferIndex);
                    byte[] outData = new byte[bufferInfo.size];
                    buffer.get(outData);
//                    Log.e(TAG, "解码长度" + outData.length);
                    h264MediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    outputBufferIndex = h264MediaCodec.dequeueOutputBuffer(bufferInfo, timeOutUs);
//                    return outData;
                    list.add(outData);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "缓冲区硬解码失败");
        }
        return list;
    }



    public  void release() {
        isRunning = false;
        try{
        if (h264MediaCodec != null) {
            Log.e(TAG, "mediacodec 释放");
            h264MediaCodec.stop();
            h264MediaCodec.release();
        }
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "释放");
        }
    }


}
