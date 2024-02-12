package com.fm.fmplayer.encoder;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.security.auth.login.LoginException;

public class AACEncoder {
    public final static String TAG = "AACEncoder";
    private MediaCodec mMediaCodec;
    public int TIMEOUT_USEC = 0;
    public int profile;
    private int sample_rate;
    private int channels;
    public FileOutputStream outputStream;


    public AACEncoder(int sample_rate, int channel) {
        try {
//            outputStream = new FileOutputStream("/data/user/0/com.fm.rtmpclient/files/fuweicong.aac");
            outputStream = new FileOutputStream("/data/data/com.fm.fmmedia/files/fuweicong.aac");
//            outputStream = new FileOutputStream("/storage/emulated/0/DCIM/Camera/fuweicong.aac");;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.sample_rate = sample_rate;
        this.channels = channel;
        this.profile = 2;
        MediaFormat format = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC, sample_rate, channel);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, sample_rate * channel);

        try {
            mMediaCodec = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getFreqIdx(int sampleRate) {
        switch (sampleRate) {
            case 96000:
                return 0;
            case 88200:
                return 1;
            case 64000:
                return 2;
            case 48000:
                return 3;
            case 44100:
                return 4;
            case 32000:
                return 5;
            case 24000:
                return 6;
            case 22050:
                return 7;
            case 16000:
                return 8;
            case 12000:
                return 9;
            case 11025:
                return 10;
            case 8000:
                return 11;
            case 7350:
                return 12;
            default:
                return 15;  // Invalid sample rate
        }
    }

    public byte[] createADTS(int profile, int channels, int sample, int packLength) {
        byte[] adts = new byte[7];
        adts[0] = (byte) 0xFF;
        adts[1] = (byte) 0xF1;
        adts[2] = (byte) (((profile - 1) << 6) + (getFreqIdx(sample) << 2) + (channels >> 2));
        adts[3] = (byte) (((channels & 3) << 6) + (packLength >> 11));
        adts[4] = (byte) ((packLength >> 3) & 0xFF);
        adts[5] = (byte) (((packLength & 7) << 5) | 0x1F);
        adts[6] = (byte) 0xFC;
        return adts;
    }


    public void encoder(AudioRecord audioRecord) {
        // YUV 颜色格式转换
        // 从输入缓冲区队列中拿到可用缓冲区，填充数据，再入队

        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            // 计算时间戳
            ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            assert inputBuffer != null;
            int bytesRead = audioRecord.read(inputBuffer, inputBuffer.capacity());
            if (bytesRead == AudioRecord.ERROR_BAD_VALUE || bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.e(TAG, "Error reading audio data");
                return;
            }

            if (bytesRead > 0) {
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, bytesRead, System.nanoTime() / 1000, 0);
            }
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
        // 从输出缓冲区队列中拿到编码好的内容，对内容进行相应处理后在释放
        try {
            while (outputBufferIndex >= 0) {

                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                outputBuffer.get(outData);
                if (bufferInfo.flags != BUFFER_FLAG_CODEC_CONFIG) {
                    byte[] adts = createADTS(profile, channels, sample_rate, outData.length + 7);
                    outputStream.write(adts, 0, adts.length);
                    outputStream.write(outData, 0, outData.length);
                } else {

                }

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
