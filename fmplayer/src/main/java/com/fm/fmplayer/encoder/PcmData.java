package com.fm.fmplayer.encoder;

public class PcmData {
    public byte[] getData() {
        return data;
    }

    public long getPts() {
        return pts;
    }

    private byte[] data;

    public PcmData(byte[] data, long pts) {
        this.data = data;
        this.pts = pts;
    }

    private long pts;
}
