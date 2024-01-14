package com.fm.fmplayer;

public interface FmPlayerDataCallback {
    public void onVideoFrame(int width, int height, byte[] yData, byte[] uData, byte[] vData);
    public void onAudioFrame(byte[] audioData,int sampleRate,int nbSamples, int channels, int avSampleFormat, int dataSize);
    public void onProgress(long time, long currentTime);
    public boolean onVideoPacket(int width, int height,int format, byte[] packet);
    public void onEnd();
}
