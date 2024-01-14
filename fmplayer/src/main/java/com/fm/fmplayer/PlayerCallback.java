package com.fm.fmplayer;

public interface PlayerCallback {
    public void stopLoading();
    public void softwareDecoder();
    public void voidInfo(int width, int height);
    public void progress(long currentTime, long videoTime, boolean isSeekSuccess);
    public void end();
    public void loading();
}
