//
// Created by fuweicong on 2023/11/14.
//

#include "VideoDecoder.h"

namespace fm {

    /*********************************** FFmpeg获取GPU硬件解码帧格式的回调函数 *****************************************/
    static enum AVPixelFormat g_pixelFormat;

/**
 * @brief      回调函数，获取GPU硬件解码帧的格式
 * @param s
 * @param fmt
 * @return
 */
    AVPixelFormat get_hw_format(AVCodecContext *s, const enum AVPixelFormat *fmt) {

        const enum AVPixelFormat *p;
        std::cout << *fmt << std::endl;
        for (p = fmt; *p != -1; p++) {
            if (*p == g_pixelFormat) {
                return *p;
            }
        }

        std::cout << "get hw format no found."
                  << std::endl;         // 当同时打开太多路视频时，如果超过了GPU的能力，可能会返回找不到解码帧格式
        return *fmt;
    }

/**
 * 获取流信息
 */
    void VideoDecoder::findStreamInfo() {
        // 获取流信息
        if (avformat_find_stream_info(formatContext, nullptr) < 0) {
            fprintf(stderr, "Error finding stream information\n");
            avformat_close_input(&formatContext);
        }
    }

/**
 * 获取流id
 * @param codec_type
 * @return
 */
    void VideoDecoder::getStreams() {
        for (unsigned int i = 0; i < formatContext->nb_streams; i++) {
            AVStream *avStream = formatContext->streams[i];
            streamInfos.emplace_back(new StreamInfo(avStream, avStream->codecpar->codec_type, i));
        }
    }

    void VideoDecoder::findDecoder() {
        for (StreamInfo *streamInfo: this->streamInfos) {

            if (streamInfo->getAvStream()->codecpar->codec_id == AV_CODEC_ID_H264 &&
                this->hwDeviceCtx != nullptr) {
                const AVCodec *codec;
                switch (this->hwType) {
                    case AV_HWDEVICE_TYPE_CUDA:
                        codec = avcodec_find_decoder_by_name("h264_cuvid");
                        break;
                    case AV_HWDEVICE_TYPE_MEDIACODEC:
                        codec = avcodec_find_decoder_by_name("h264_mediacodec");
                        break;
                }
                if (!codec) {
                    this->isPrepare = false;
                    std::cout << "not found decoder" << std::endl;
                    continue;
                }
                //查找解码器和支持格式
                for (int i = 0;; i++) {
                    const AVCodecHWConfig *config = avcodec_get_hw_config(codec, i);
                    if (!config) {
                        fprintf(stderr, "Decoder %s does not support device type %s.\n",
                                codec->name, av_hwdevice_get_type_name(hwType));
                        this->isPrepare = false;
                        return;
                    }
                    if (config->methods & AV_CODEC_HW_CONFIG_METHOD_HW_DEVICE_CTX &&
                        config->device_type == hwType) {
                        //把硬件支持的像素格式设置进去
                        g_pixelFormat = config->pix_fmt;
                        this->hwVideoFormat = config->pix_fmt;
                        if (this->hwType != AV_HWDEVICE_TYPE_MEDIACODEC)
                            isHardDecoder = true;
                        break;
                    }
                }

                streamInfo->setAvCodec(codec);
                continue;
            }
            const AVCodec *codec = avcodec_find_decoder(
                    streamInfo->getAvStream()->codecpar->codec_id);
            streamInfo->setAvCodec(codec);
        }
    }

    void VideoDecoder::openDecoder() {
        AVDictionary *opts = NULL;
        // 创建解码器上下文
        for (StreamInfo *streamInfo: streamInfos) {
            if (streamInfo->getAvCodec()) {
                AVCodecContext *codecContext = avcodec_alloc_context3(streamInfo->getAvCodec());
                if (streamInfo->getMediaType() == AVMEDIA_TYPE_VIDEO &&
                    this->hwDeviceCtx != nullptr && isHardDecoder) {
                    codecContext->hw_device_ctx = av_buffer_ref(hwDeviceCtx);
                    codecContext->get_format = get_hw_format;
                }
                if (!codecContext) {
                    this->isPrepare = false;
                    continue;
                }
                streamInfo->setCodecContext(codecContext);
                // 将流参数拷贝到解码器上下文
                if (avcodec_parameters_to_context(codecContext,
                                                  streamInfo->getAvStream()->codecpar) < 0) {
                    fprintf(stderr, "Error copying codec parameters to context\n");
                    avcodec_free_context(&codecContext);
                    this->isPrepare = false;
                    continue;
                }
                if (!av_dict_get(opts, "threads", NULL, 0))
                    av_dict_set(&opts, "threads", "auto", 0);
                // 打开解码器
                if (avcodec_open2(codecContext, streamInfo->getAvCodec(), &opts) < 0) {
                    fprintf(stderr, "Error opening video codec\n");
                    avcodec_free_context(&codecContext);
                    this->isPrepare = false;
                    continue;
                }
            }
        }
    }

    AVPacketData VideoDecoder::readPacket() {
        AVPacketData avPacketData;
        std::queue<AVPacket *> avPacketQueue;

        // 分配 AVPacket
        AVPacket *packet = av_packet_alloc();
        if (!packet) {
            return avPacketData;
        }
        // 读取帧并解码
        if (av_read_frame(formatContext, packet) >= 0) {
            avPacketData.setIsEnd(false);

            for (StreamInfo *streamInfo: streamInfos) {

                if (streamInfo->getStreamIndex() == packet->stream_index) {
                    avPacketData.setMediaType(streamInfo->getMediaType());
//                    AVCodecParameters *codecpar = streamInfo->getAvStream()->codecpar;
//                    avPacketData.setAvCodecParameters(*codecpar);
                    avPacketData.setAvCodecContext(streamInfo->getCodecContext());
                    avPacketData.setAvStream(streamInfo->getAvStream());
//                    avPacketData.setAvCodecId(streamInfo->getAvStream()->codecpar->codec_id);
                    avPacketQueue.push(packet);
                }
            }
        }
        avPacketData.setAvPacketQueue(avPacketQueue);
        return avPacketData;
    }

    AvFrameInfo VideoDecoder::readFrame() {

        std::queue<AVFrame> avFrameQueue;

        AvFrameInfo avFrameInfo(avFrameQueue, AVMEDIA_TYPE_UNKNOWN);

        // 分配帧
        AVFrame *frame = av_frame_alloc();
        if (!frame) {
            return avFrameInfo;
        }

        // 分配 AVPacket
        AVPacket *packet = av_packet_alloc();
        if (!packet) {
            return avFrameInfo;
        }

        // 读取帧并解码
        if (av_read_frame(formatContext, packet) >= 0) {
            avFrameInfo.setIsEnd(false);

            for (StreamInfo *streamInfo: streamInfos) {
                if (streamInfo->getStreamIndex() == packet->stream_index) {
                    avFrameInfo.setMediaType(streamInfo->getMediaType());
                    // 发送数据到解码器
                    if (avcodec_send_packet(streamInfo->getCodecContext(), packet) < 0) {
                        fprintf(stderr, "Error sending packet to decoder\n");
                        break;
                    }

                    // 接收解码后的帧
                    while (avcodec_receive_frame(streamInfo->getCodecContext(), frame) >= 0) {
                        // 转换帧格式到RGB24
                        avFrameQueue.push(*frame);
                    }
                }
            }
            av_packet_unref(packet);
        }
        avFrameInfo.setAvFrameQueue(avFrameQueue);
        return avFrameInfo;
    }

    bool VideoDecoder::init() {
        AVDictionary *opts = NULL;
        std::cout << input << std::endl;
        av_dict_set(&opts, "rw_timeout", "5000000", 0);//设置超时3秒
        if (avformat_open_input(&formatContext, input, nullptr, &opts) != 0) {
            fprintf(stderr, "Error opening input file\n");
            return false;
        }


        //获取流信息
        findStreamInfo();
        // 存储流信息
        getStreams();
        //查找硬解码器
        if (isHardware) {
            hardwareDecoder();
        }
        // 查找解码器
        findDecoder();
        // 打开解码器
        openDecoder();
        return true;
    }

    VideoDecoder::VideoDecoder(const char *input, bool isHardware) {
        formatContext = avformat_alloc_context();
        this->input = input;
        this->isHardware = isHardware;
    }

    VideoDecoder::~VideoDecoder() {
        if (formatContext != nullptr) {
            avformat_close_input(&formatContext);
        }
        if (hwDeviceCtx != nullptr)
            av_buffer_unref(&hwDeviceCtx);
        for (StreamInfo *streamInfo: streamInfos) {
            if (streamInfo->getCodecContext() != nullptr) {
                AVCodecContext *codecContext = streamInfo->getCodecContext();
                avcodec_free_context(&codecContext);
            }
        }
    }

    void VideoDecoder::seek(int64_t time) {
        if (this->formatContext != nullptr) {
            if (av_seek_frame(formatContext, -1, time, AVSEEK_FLAG_BACKWARD) < 0) {
                fprintf(stderr, "Seek error\n");
                return;
            }
            for (StreamInfo *streamInfo : streamInfos) {
                avcodec_flush_buffers(streamInfo->getCodecContext());
            }
        }
    }

    void VideoDecoder::hardwareDecoder() {
        // 查找可用的硬解码器类型
        hwType = av_hwdevice_iterate_types(hwType);
        if (hwType == AV_HWDEVICE_TYPE_NONE) return;
        std::cout << "hardware type:" << hwType << std::endl;
        // 创建硬件设备上下文
        if (av_hwdevice_ctx_create(&hwDeviceCtx, hwType, NULL, NULL, 0) < 0) {
            // 创建失败
            std::cout << "av_hwdevice_ctx_create error" << std::endl;
            return;
        }
    }

    bool VideoDecoder::isHardDecoder1() const {
        return isHardDecoder;
    }

    bool VideoDecoder::isPrepare1() const {
        return isPrepare;
    }

    AVFrame *VideoDecoder::readVideoFrame(int64_t &duration, int width, int height) {
        // 分配帧
        AVFrame *frame = av_frame_alloc();
        if (!frame) {
            return nullptr;
        }
        AVFrame *tmpFrame = av_frame_alloc();
        if (!tmpFrame) return nullptr;
        AVFrame *rgbFrame = av_frame_alloc();
        if (!rgbFrame) return nullptr;

        // 分配 AVPacket
        AVPacket *packet = av_packet_alloc();
        if (!packet) {
            return nullptr;
        }
        int videoStreamIndex = -1;
        for (StreamInfo *streamInfo: streamInfos) {
            if (streamInfo->getMediaType() == AVMEDIA_TYPE_VIDEO) {
                videoStreamIndex = streamInfo->getStreamIndex();
                break;
            }
        }
        if (videoStreamIndex == -1) return nullptr;

        // 读取帧并解码
        while (av_read_frame(formatContext, packet) >= 0) {

            for (StreamInfo *streamInfo: streamInfos) {
                if (streamInfo->getStreamIndex() == videoStreamIndex &&
                    streamInfo->getStreamIndex() == packet->stream_index) {
                    // 发送数据到解码器
                    if (avcodec_send_packet(streamInfo->getCodecContext(), packet) < 0) {
                        fprintf(stderr, "Error sending packet to decoder\n");
                        break;
                    }

                    // 接收解码后的帧
                    while (avcodec_receive_frame(streamInfo->getCodecContext(), frame) >= 0) {
                        // 转换帧格式到RGB24
                        if (this->isHardDecoder1()) {
                            if (av_hwframe_transfer_data(tmpFrame, frame, 0) <
                                0) { //av_hwframe_transfer_data
                                fprintf(stderr, "Error transferring the data to system memory\n");
                                return nullptr;
                            }

                            tmpFrame->width = frame->width;
                            tmpFrame->height = frame->height;
                            tmpFrame->pts = frame->pts;
                            av_frame_copy_props(tmpFrame,
                                                frame);
                            // 仅将“metadata”字段从src复制到dst
                            frame = tmpFrame;
                        }
                        int width = frame->width;
                        int height = frame->height;
                        int newWidth = width;
                        int newHeight = height;
                        SwsContext *swsContext = sws_getContext(width, height,
                                                                (AVPixelFormat) frame->format,
                                                                newWidth, newHeight,
                                                                AV_PIX_FMT_BGRA, SWS_BICUBIC, NULL,
                                                                NULL, NULL);
                        rgbFrame->width = newWidth;
                        rgbFrame->height = newHeight;
                        rgbFrame->format = AV_PIX_FMT_BGRA;
                        av_image_alloc(rgbFrame->data, rgbFrame->linesize, newWidth, newHeight,
                                       AV_PIX_FMT_BGRA,
                                       1);
                        sws_scale(swsContext, frame->data, frame->linesize, 0, frame->height,
                                  rgbFrame->data, rgbFrame->linesize);
                        av_frame_free(&frame);
                        sws_freeContext(swsContext);
                        av_frame_free(&tmpFrame);
                        av_packet_unref(packet);
                        duration = streamInfo->getAvStream()->duration *
                                   av_q2d(streamInfo->getAvStream()->time_base);
                        return rgbFrame;
                    }
                }
            }

            av_packet_unref(packet);
        }
        av_frame_free(&frame);
        av_frame_free(&tmpFrame);
        return frame;
    }





    AvFrameInfo::AvFrameInfo(const std::queue<AVFrame> &avFrameQueue, AVMediaType mediaType)
            : avFrameQueue(
            avFrameQueue),
              mediaType(mediaType) {}

    const std::queue<AVFrame> &AvFrameInfo::getAvFrameQueue() const {
        return avFrameQueue;
    }

    AVMediaType AvFrameInfo::getMediaType() const {
        return mediaType;
    }

    bool AvFrameInfo::isEnd1() const {
        return isEnd;
    }

    void AvFrameInfo::setIsEnd(bool isEnd) {
        AvFrameInfo::isEnd = isEnd;
    }

    void AvFrameInfo::setAvFrameQueue(const std::queue<AVFrame> &avFrameQueue) {
        AvFrameInfo::avFrameQueue = avFrameQueue;
    }

    void AvFrameInfo::setMediaType(AVMediaType mediaType) {
        AvFrameInfo::mediaType = mediaType;
    }

    const std::queue<AVPacket *> &AVPacketData::getAvPacketQueue() const {
        return avPacketQueue;
    }

    void AVPacketData::setAvPacketQueue(const std::queue<AVPacket *> &avPacketQueue) {
        AVPacketData::avPacketQueue = avPacketQueue;
    }


    bool AVPacketData::isEnd1() const {
        return isEnd;
    }

    void AVPacketData::setIsEnd(bool isEnd) {
        AVPacketData::isEnd = isEnd;
    }


    AVMediaType AVPacketData::getMediaType() const {
        return mediaType;
    }

    void AVPacketData::setMediaType(AVMediaType mediaType) {
        AVPacketData::mediaType = mediaType;
    }

    AVCodecID AVPacketData::getAvCodecId() const {
        return avCodecId;
    }

    void AVPacketData::setAvCodecId(AVCodecID avCodecId) {
        AVPacketData::avCodecId = avCodecId;
    }

    const AVCodecParameters &AVPacketData::getAvCodecParameters() const {
        return avCodecParameters;
    }

    void AVPacketData::setAvCodecParameters(const AVCodecParameters &avCodecParameters) {
        AVPacketData::avCodecParameters = avCodecParameters;
    }

    AVCodecContext *AVPacketData::getAvCodecContext() const {
        return avCodecContext;
    }

    void AVPacketData::setAvCodecContext(AVCodecContext *avCodecContext) {
        AVPacketData::avCodecContext = avCodecContext;
    }

    AVStream *AVPacketData::getAvStream() const {
        return avStream;
    }

    void AVPacketData::setAvStream(AVStream *avStream) {
        AVPacketData::avStream = avStream;
    }


}