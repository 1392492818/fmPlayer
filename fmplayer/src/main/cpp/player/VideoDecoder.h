//
// Created by fuweicong on 2023/11/14.
//

#ifndef QTANDROID_VIDEODECODER_H
#define QTANDROID_VIDEODECODER_H

#include "StreamInfo.h"
#include <iostream>
#include <vector>
#include <queue>
#include "StreamFilter.h"

namespace fm {

    // 流 avPacket 信息
    class AVPacketData {
    private:
        std::queue<AVPacket*> avPacketQueue;
        enum AVCodecID avCodecId;
        AVCodecContext* avCodecContext;
        AVStream* avStream;
    public:
        AVStream *getAvStream() const;

        void setAvStream(AVStream *avStream);

    public:
        AVCodecContext *getAvCodecContext() const;

        void setAvCodecContext(AVCodecContext *avCodecContext);

    public:
        const AVCodecParameters &getAvCodecParameters() const;

        void setAvCodecParameters(const AVCodecParameters &avCodecParameters);

    public:
        AVCodecID getAvCodecId() const;

        void setAvCodecId(AVCodecID avCodecId);

    private:
        AVCodecParameters avCodecParameters;


    public:
        const AVCodec &getAvCodec() const;

        void setAvCodec(const AVCodec &avCodec);

    private:
        AVMediaType mediaType;
    public:
        AVMediaType getMediaType() const;

        void setMediaType(AVMediaType mediaType);

    public:


        const std::queue<AVPacket*> &getAvPacketQueue() const;

        void setAvPacketQueue(const std::queue<AVPacket*> &avPacketQueue);

        bool isEnd1() const;

        void setIsEnd(bool isEnd);

    private:
        bool isEnd = true;
    };

    class AvFrameInfo {
    private:
        std::queue<AVFrame> avFrameQueue;
    public:
        void setAvFrameQueue(const std::queue<AVFrame> &avFrameQueue);

    private:
        bool isEnd = true;
    public:
        bool isEnd1() const;

        void setIsEnd(bool isEnd);

    public:
        AvFrameInfo(const std::queue<AVFrame> &avFrameQueue, AVMediaType mediaType);

        const std::queue<AVFrame> &getAvFrameQueue() const;

        AVMediaType getMediaType() const;

    private:
        AVMediaType mediaType;
    public:
        void setMediaType(AVMediaType mediaType);
    };

    class VideoDecoder {
    private:
        AVFormatContext *formatContext = nullptr;
        AVBufferRef *hwDeviceCtx = NULL;
        bool isHardDecoder = false;
        bool isPrepare = true;
        AVPixelFormat hwVideoFormat;
        enum AVHWDeviceType hwType = AV_HWDEVICE_TYPE_NONE;
        const char *input;
        bool isHardware;

    public:
        void setIsAnnexb(bool isAnnexb);
// 是否将数据转换成 h264格式

    public:
        bool isPrepare1() const;
        bool init();

    public:
        bool isHardDecoder1() const;
    public:
        VideoDecoder(const char *input, bool isOpenHardware);

        ~VideoDecoder();

        void findStreamInfo();

        void getStreams();

        void findDecoder();

        void hardwareDecoder();

        void openDecoder();
        void seek(int64_t);
        AvFrameInfo readFrame();
        AVFrame *readVideoFrame(int64_t &duration, int width = 100, int height = 60);
        AVPacketData readPacket();

        std::vector<StreamInfo *> streamInfos;
    };

}
#endif //QTANDROID_VIDEODECODER_H
