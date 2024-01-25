//
// Created by fuweicong on 2023/12/9.
//

#ifndef FMPLAYER_VIDEOPLAYER_H
#define FMPLAYER_VIDEOPLAYER_H

#include "player/CallAVFrame.h"
#include "VideoPlayer.h"
#include <jni.h>

class VideoPlayer: public CallAVFrame{
public:
    VideoPlayer(JavaVM* g_vm, JNIEnv *env, jobject callbackObj);
    ~VideoPlayer();
    virtual void onVideoFrame(AVFrame *avFrame);

    virtual void
    onAudioFrame(AVFrame *avFrame, int channels, AVSampleFormat avSampleFormat, int dataSize);

    virtual void onProgress(int64_t time, int64_t currentTime, int64_t cacheTime);

    virtual void onEnd(bool isError);

    virtual void release();

    virtual void onLoading();


    bool onVideoPacket(int width,int height,int format, AVPacket *packet) override;

private:
    jclass callbackClass;
    JNIEnv *env;
    JavaVM *g_VM;
    jmethodID onVideoFrameId;
    jmethodID  onAudioFrameId;
    jmethodID  onProgressId;
    jmethodID  onEndId;
    jobject jobject1;
    bool mNeedDetach = false;

};


#endif //FMPLAYER_VIDEOPLAYER_H
