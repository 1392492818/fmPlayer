//
// Created by fuweicong on 2023/11/14.
//

#include "VideoDecoder.h"

namespace fm {

    // 进度回调函数
    static int progress_callback(void *opaque, int64_t dltotal, int64_t dlnow, int64_t ultotal,
                                 int64_t ulnow) {
        // 在这里处理进度信息
        // dltotal：总字节数，dlnow：已下载字节数，ultotal：总上传字节数，ulnow：已上传字节数
        // 可以通过这些信息计算下载进度或上传进度
        return 0;
    }

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
        int ret = 0;
        // 读取帧并解码
        if ((ret = av_read_frame(formatContext, packet)) >= 0) {
            avPacketData.setIsEnd(false);
//            videoCache->readPacket(0);
//            videoCache->cachePacket(packet);
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
        if(ret < 0 && ret != AVERROR_EOF){
            avPacketData.setIsError(true);
        }
        avPacketData.setAvPacketQueue(avPacketQueue);
        return avPacketData;
    }
//    AVPacketData VideoDecoder::readPacket() {
//        std::lock_guard lockGuard(seekMutex);
//        AVPacketData avPacketData;
//        std::queue<AVPacket *> avPacketQueue;
//        if (this->videoCache->getPacketQueue().size() == 0 && isError) {
//            LOGE("出错了");
//            avPacketData.setIsEnd(true);
//            avPacketData.setIsError(isError);
//            return avPacketData;
//        }
//        AVPacket *packet = videoCache->readPacket();
//        if (packet != nullptr) {
//            if (this->videoCache->getPacketQueue().size() == 0 && this->isEnd) {
//                LOGE("readPacket end");
//                avPacketData.setIsEnd(this->isEnd);
//            } else {
//                avPacketData.setIsEnd(false);
//            }
//        } else {
//            LOGE("readPacket end");
//            this->isEnd = true;
//            avPacketData.setIsEnd(this->isEnd);
//        }
//        if (packet != nullptr) {
//            for (StreamInfo *streamInfo: streamInfos) {
//                if (streamInfo->getStreamIndex() == packet->stream_index) {
//                    avPacketData.setMediaType(streamInfo->getMediaType());
//                    avPacketData.setAvCodecContext(streamInfo->getCodecContext());
//                    avPacketData.setAvStream(streamInfo->getAvStream());
//                    avPacketQueue.push(packet);
//                }
//            }
//        } else {
//            isError = true;
//            isRelease = true;
//        }
//
//        avPacketData.setAvPacketQueue(avPacketQueue);
//        return avPacketData;
//    }

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

    bool VideoDecoder::init(long time, string cache) {
        formatContext = avformat_alloc_context();
        this->isEnd = false;
        AVDictionary *opts = NULL;
        this->cache = cache;
        std::cout << input << std::endl;
        this->videoCache = new VideoCache(this->cache, input);
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
        seek(time * AV_TIME_BASE);
//        av_opt_set(formatContext, "progress_callback",
//                   reinterpret_cast<const char *>(progress_callback), 0);
        //启动视频缓存
//        setCacheVideoThread();
        return true;
    }

    void VideoDecoder::setCacheVideoThread() {
        this->isEnd = false;
        this->isExit = false;
        this->videoCache->setIsEnd(false);
        auto cacheVideo = std::bind(&VideoDecoder::cacheVideo, this);
        // 创建线程，并调用绑定的成员函数
        this->cacheThread = std::thread(cacheVideo);
        this->cacheThread.detach();
    }

    VideoDecoder::VideoDecoder(const char *input, bool isHardware) {
        this->input = input;
        this->isHardware = isHardware;
    }

    VideoDecoder::~VideoDecoder() {
        isRelease = true;
        while (!isExit) {
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
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

    int VideoDecoder::seek(int64_t time) {

        if (av_seek_frame(formatContext, -1, time, AVSEEK_FLAG_BACKWARD) < 0) {
            fprintf(stderr, "Seek error\n");
            return -1;
        }
        LOGE("VideoDecoder::seek %lld isEnd %d", time / AV_TIME_BASE, this->isEnd);
//        if (videoCache->seekVideoCache(
//                time /
//                AV_TIME_BASE)) { // 从缓存中提取数据，看下有没有，如果没有，就请求，有就执行执行就可以, 另外情况， av_read_frame 已经结束了
//            return 0;
//        }
//
//        std::lock_guard seekLock(seekMutex);
//        std::lock_guard lockGuard(cacheMutex);
//
//        videoCache->setEndTimeBase(0);
//        if (!(this->formatContext != nullptr && !isEnd)) {
//            LOGE("seek吗 %d", isEnd);
//            setCacheVideoThread();
//        }
//        if (av_seek_frame(formatContext, -1, time, AVSEEK_FLAG_BACKWARD) < 0) {
//            fprintf(stderr, "Seek error\n");
//            return -1;
//        }
//
//        for (StreamInfo *streamInfo: streamInfos) {
//            avcodec_flush_buffers(streamInfo->getCodecContext());
//        }
        return 0;

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

    int64_t VideoDecoder::getPbBufferSize() {
        if (formatContext != nullptr) {
            return formatContext->pb->bytes_read;
        }
        return 0;
    }

    void VideoDecoder::cacheVideo() {
        AVPacket *packet = av_packet_alloc();
        int ret = -1;
        // 读取帧并解码
        while (true) {
            std::lock_guard lockGuard(cacheMutex);
            if (!((ret = av_read_frame(formatContext, packet)) >= 0 && !isRelease)) break;
            for (StreamInfo *streamInfo: streamInfos) {
                if (streamInfo->getStreamIndex() == packet->stream_index) {
                    videoCache->cachePacket(packet, streamInfo->getAvStream()->time_base);
                }
            }
            av_packet_unref(packet);
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
        std::lock_guard lockGuard(cacheMutex);
        LOGE("cache end");
        if (ret < 0 && ret != AVERROR_EOF) {
            this->isError = true;
            LOGE("error ret %d", ret);
        } else {
            this->isEnd = true;
        }
        this->videoCache->setIsEnd(true);
        isExit = true;
    }

    int64_t VideoDecoder::getCacheTime() {
        return this->videoCache->getEndTimeBase();
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

    bool AvFrameInfo::isError1() const {
        return isError;
    }

    void AvFrameInfo::setIsError(bool isError) {
        AvFrameInfo::isError = isError;
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

    bool AVPacketData::isError1() const {
        return isError;
    }

    void AVPacketData::setIsError(bool isError) {
        AVPacketData::isError = isError;
    }
}