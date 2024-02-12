package com.fm.fmplayer.encoder;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class H264Encoder {
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private final static String TAG = "h264Encoder";
    private final static String MIME_TYPE = "video/avc";
    private long pts = 0L;
    private long second = 1000;
    private int fps = -1;
    private int TIMEOUT_USEC = 0;
    private int videoFrameNum = 0;
    private int width;
    private int height;

    public boolean isInit() {
        return isInit;
    }

    private boolean isInit = false;

    private int format = -1;
    byte packetData[] = null;
    public FileOutputStream outputStream;

    public boolean init(int width, int height, int fps) {
        return this.init(width, height, fps, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
    }

    public boolean init(int width, int height, int fps, int format) {
        this.width = width;
        this.height = height;
        try {
            outputStream = new FileOutputStream("/data/data/com.fm.fmmedia/files/fuweicong.h264");
//        outputStream = new FileOutputStream("/storage/emulated/0/DCIM/Camera/fuweicong.h264");

            this.fps = fps;
            mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);

            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            // 码率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 30);
            // 调整码率的控流模式
            mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            // 设置帧率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
            // 设置 I 帧间隔
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);


            // 创建 MediaCodec，此时是 Uninitialized 状态
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
            // 调用 configure 进入 Configured 状态
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            MediaFormat outputFormat = mediaCodec.getOutputFormat(); // option B
            // 调用 start 进入 Executing 状态，开始编解码工作

            mediaCodec.start();
            this.isInit = true;
            this.format = mediaCodec.getInputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT);
            return true;
        } catch (Exception e) {
            this.isInit = false;
            Log.e("fuweicong", "mediaCodec error");
        }
        return false;
    }

    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / this.fps;
    }


    private byte[] checkInputFormat(byte[] yuvData, int width, int height) {
        switch (format) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar: // yuv420 直接返回就可以
                return yuvData;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                return ImageFormatUtil.yuv420toNv21(yuvData, width, height);
            default:
                return ImageFormatUtil.yuv420toNv21(yuvData, width, height);
        }
    }

    private byte[] getImageData(Image image) {
        int imageFormat = image.getFormat();
        switch (imageFormat) {
            case ImageFormat.YUV_420_888:
                return checkInputFormat(
                        ImageFormatUtil.getYuv420p(image),
                        image.getWidth(),
                        image.getHeight());
            default:
                return ImageFormatUtil.getYuv420p(image);
        }
    }

    public void encoder(Image image) {
        encoder(getImageData(image));
    }


    public void encoder(byte[] buffer) {
        // YUV 颜色格式转换
        // 从输入缓冲区队列中拿到可用缓冲区，填充数据，再入队

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            // 计算时间戳
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(buffer);
            pts = computePresentationTime(videoFrameNum++);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, buffer.length, pts, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        // 从输出缓冲区队列中拿到编码好的内容，对内容进行相应处理后在释放
        try {

            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                // flags 利用位操作，定义的 flag 都是 2 的倍数
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) { // 配置相关的内容，也就是 SPS，PPS
                    outputStream.write(outData, 0, outData.length);


//                    Log.e("fuweicong", "sps");
                } else if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) { // 关键帧
                    outputStream.write(outData, 0, outData.length);


//                    Log.e("fuweicong", "BUFFER_FLAG_KEY_FRAME");
                } else {
                    // 非关键帧和SPS、PPS,直接写入文件，可能是B帧或者P帧
                    outputStream.write(outData, 0, outData.length);

//                    Log.e("fuweicong", "no sps pps");
                }

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }
}
