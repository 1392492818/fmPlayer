package com.fm.fmplayer;

public interface PlayerCallback {
    public void stopLoading();
    public void softwareDecoder();
    public void voidInfo(int width, int height);
    public void progress(long currentTime, long videoTime, long cacheTime, boolean isSeekSuccess);
    public void end(Boolean isError);
    public void rotate(int rotate);
    public void loading();
}
