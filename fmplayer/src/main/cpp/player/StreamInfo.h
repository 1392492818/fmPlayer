//
// Created by fuweicong on 2023/11/14.
//

#ifndef QTANDROID_STREAMINFO_H
#define QTANDROID_STREAMINFO_H
extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/avutil.h"
#include <libavutil/pixfmt.h>
#include <libavutil/imgutils.h>
#include <libavfilter/avfilter.h>
#include <libavfilter/buffersink.h>
#include <libavfilter/buffersrc.h>
#include <libavutil/opt.h>
#include <libswscale/swscale.h>
#include <libavcodec/bsf.h>
}

#ifdef  _WIN32
#pragma comment(lib, "avutil.lib")
#pragma comment(lib, "avformat.lib")
#pragma comment(lib, "avcodec.lib")
#pragma comment(lib, "avfilter.lib")
#pragma comment(lib, "swresample.lib")
#pragma comment(lib, "swscale.lib")
#endif
namespace fm {
    class StreamInfo {
    private:
        enum AVMediaType mediaType;
        AVCodecContext *codecContext = nullptr;
        AVStream *avStream = nullptr;
        const AVCodec *avCodec = nullptr;
    public:
        const AVCodec *getAvCodec() const;

        void setAvCodec(const AVCodec *avCodec);

    public:
        AVStream *getAvStream() const;

        void setAvStream(AVStream *avStream);

    public:
        StreamInfo(AVStream *avStream, AVMediaType mediaType, int streamIndex = -1,
                   AVCodecContext *codecContext = nullptr, AVCodec *avCodec = nullptr);

    public:
        AVMediaType getMediaType() const;

        void setMediaType(AVMediaType mediaType);

        AVCodecContext *getCodecContext() const;

        void setCodecContext(AVCodecContext *codecContext);

        int getStreamIndex() const;

        void setStreamIndex(int streamIndex);

    private:
        int streamIndex;
    };
}

#endif //QTANDROID_STREAMINFO_H
