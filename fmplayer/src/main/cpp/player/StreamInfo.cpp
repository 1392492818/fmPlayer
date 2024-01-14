//
// Created by fuweicong on 2023/11/14.
//

#include "StreamInfo.h"
namespace fm {
    AVMediaType StreamInfo::getMediaType() const {
        return mediaType;
    }

    void StreamInfo::setMediaType(AVMediaType mediaType) {
        StreamInfo::mediaType = mediaType;
    }

    AVCodecContext *StreamInfo::getCodecContext() const {
        return codecContext;
    }

    void StreamInfo::setCodecContext(AVCodecContext *codecContext) {
        StreamInfo::codecContext = codecContext;
    }

    int StreamInfo::getStreamIndex() const {
        return streamIndex;
    }

    void StreamInfo::setStreamIndex(int streamIndex) {
        StreamInfo::streamIndex = streamIndex;
    }

    StreamInfo::StreamInfo(AVStream *avStream, AVMediaType mediaType, int streamIndex, AVCodecContext *codecContext,
                           AVCodec *avCodec)
            : avStream(avStream), mediaType(mediaType),
              codecContext(
                      codecContext),
              avCodec(avCodec),
              streamIndex(
                      streamIndex) {}

    AVStream *StreamInfo::getAvStream() const {
        return avStream;
    }

    void StreamInfo::setAvStream(AVStream *avStream) {
        StreamInfo::avStream = avStream;
    }

    const AVCodec *StreamInfo::getAvCodec() const {
        return avCodec;
    }

    void StreamInfo::setAvCodec(const AVCodec *avCodec) {
        this->avCodec = avCodec;
    }
}