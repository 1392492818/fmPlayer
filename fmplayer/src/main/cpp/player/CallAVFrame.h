//
// Created by fuweicong on 2023/11/18.
//

#ifndef QTANDROID_CALLAVFRAME_H
#define QTANDROID_CALLAVFRAME_H
extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/avutil.h"
#include <libavutil/pixfmt.h>
#include <libavutil/imgutils.h>
}
class CallAVFrame{
public:
    virtual void onVideoFrame(AVFrame* avFrame) = 0;
    virtual bool onVideoPacket(int width, int height,int format, AVPacket *packet) = 0;
    virtual void onAudioFrame(AVFrame* avFrame, int channels, AVSampleFormat avSampleFormat, int dataSize) = 0;
    virtual void onProgress(int64_t time, int64_t currentTime) = 0;
    virtual void onEnd() = 0;
    virtual void release() = 0;
};
#endif //QTANDROID_CALLAVFRAME_H
