//
// Created by fuweicong on 2023/11/14.
//

#include "FmPlayer.h"

namespace fm {
#ifdef _WIN32

    void setThreadName(const std::string &name) {
        const std::wstring wideName = std::wstring(name.begin(), name.end());
        HRESULT hr = SetThreadDescription(GetCurrentThread(), wideName.c_str());
        if (FAILED(hr)) {
            std::cerr << "Failed to set thread name" << std::endl;
        }
    }

#endif

#ifdef FM_ANDROID

    void setThreadName(const std::string &name) {
        pthread_t pthreadId = pthread_self();

        // 设置线程名称
        pthread_setname_np(pthreadId, name.data());
    }

#endif

    FmPlayer::FmPlayer() {
        this->outputFile = std::ofstream("/data/data/com.marvel.fmPlayer/files/test.h264",
                                         std::ios::binary);
    }

    FmPlayer::~FmPlayer() {
        this->stop();
        if (this->callAvFrame != nullptr) {
            this->callAvFrame->release();
        }
        this->videoDecoder = nullptr;
    }

    void FmPlayer::start() {
        auto decoderMedia = std::bind(&FmPlayer::decoderMedia, this);
        // 创建线程，并调用绑定的成员函数
        this->decoderMediaThread = std::thread(decoderMedia);
        decoderMediaThread.detach();
        plusSignal();

        // 视频流解码
        auto video = std::bind(&FmPlayer::decoderVideo, this);
        this->videoDecoderThread = std::thread(video);
        videoDecoderThread.detach();
        plusSignal();

        // 音频流解码
        auto audio = std::bind(&FmPlayer::decoderAudio, this);
        this->audioDecoderThread = std::thread(audio);
        audioDecoderThread.detach();
        plusSignal();

        // 视频流渲染
        auto videoRender = std::bind(&FmPlayer::renderVideo, this);
        this->videoRenderThread = std::thread(videoRender);
        videoRenderThread.detach();
        plusSignal();

        // 音频流播放
        auto audioRender = std::bind(&FmPlayer::renderAudio, this);
        this->audioRenderThread = std::thread(audioRender);
        audioRenderThread.detach();
        plusSignal();

    }

    void FmPlayer::stop() {
        {
            std::lock_guard<std::mutex> lockDecoderMutex(decoderMutex);  // 解码视频互斥锁

            std::lock_guard<std::mutex> playerLock(playerMutex);
            this->isRunning = false; //锁存在先后顺序，先锁作用域大的
            this->isPlayer = false;
            this->isStop = true;
            this->isDecoderEnd = true;

            clearAllQueue();
            LOGE("stop");


        }
        while (this->signal != 0) {
            LOGE("signal %d isEnd %d", signal, this->isDecoderEnd);
            LOGE("audio frame size %d, video frame size %d, audio packet size %d, video packet size %d",
                 this->audioFrameQueue.size(), this->videoFrameQueue.size(),
                 this->audioPacketQueue.size(), this->videoPacketQueue.size());
//            qDebug() << "signal:" << this->signal
//                     << ", running:" << this->isRunning
//                     << ", videoPacketSize:" << this->videoPacketQueue.size()
//                     << ", audioPacketSize:" << this->audioPacketQueue.size()
//                     << ", videoFrame:" << this->videoFrameQueue.size()
//                     << ", audioFrame:" << this->audioFrameQueue.size();
            std::this_thread::sleep_for(std::chrono::milliseconds(10));
        }
        LOGE("fmPlayer停止了");

        this->videoDecoder = nullptr;

        this->videoCodecContext = nullptr;
        this->audioCodecContext = nullptr;
        this->videoStream = nullptr;
        this->audioStream = nullptr;
        this->swsContext = nullptr;
        this->sink = nullptr;
        this->graph = nullptr;
        this->src = nullptr;
        this->hwSwsContext = nullptr;
        LOGE("fmPlayer停止了");
    }


    void FmPlayer::startPlayer(const char *input, long time, string cache) {
//        qDebug() << input;
        this->input = input;
//        std::thread([&]() {
        std::lock_guard<std::mutex> lockGuard(decoderMutex);
        this->videoDecoder = std::make_unique<VideoDecoder>(this->input.data(), false);
        if (this->videoDecoder->init(time, cache)) {
            if (!this->videoDecoder->isPrepare1()) return;
            this->isRunning = true;
            this->isStop = false;
        } else {
            isError = true;
        }
//        }).detach();

    }


    //解码音视频
    void FmPlayer::decoderMedia() {
#ifdef _WIN32
        setThreadName("decoderMedia");
#endif

        while (true) {
//            qDebug() << "decoderMedia";
            {
                std::lock_guard<std::mutex> lock(decoderMutex);
                if (!this->isRunning) break;
            }
            {
                // 播放暂停互斥锁
                if (!checkPlayer()) {
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                    continue;
                }
            }
            {
                std::lock_guard<std::mutex> seekLock(decoderSeekMutex);
                if (this->isDecoderEnd && this->allQueueIsEmpty()) {
                    this->isRunning = false;
                    break;
                }
                if (this->isDecoderEnd) {
//                LOGE("isDecoderEnd %d", this->isDecoderEnd);
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                    continue;
                }
            }
            AVPacketData avPacketData;
            { // seek 互斥锁

                std::lock_guard<std::mutex> seekLock(decoderSeekMutex);
                avPacketData = this->videoDecoder->readPacket();
                if (avPacketData.isError1()) {
                    this->isError = true;
                }
                if (avPacketData.isEnd1()) {
//                    LOGE("运行结束");
//                    this->isRunning = false;
                    this->isDecoderEnd = true;
                    continue;
                }
            }
            std::queue<AVPacket *> avPacketQueue = avPacketData.getAvPacketQueue();
            while (!avPacketQueue.empty()) {
                AVPacket *avPacket = nullptr;
                if (avPacketData.getMediaType() == AVMEDIA_TYPE_VIDEO) {
                    {    // 视频avPacket互斥锁
                        std::lock_guard<std::mutex> lock(videoDecoderMutex);
                        avPacket = avPacketQueue.front();
                        if (videoCodecContext == nullptr)
                            videoCodecContext = avPacketData.getAvCodecContext();
                        if (videoStream == nullptr) {
                            videoStream = avPacketData.getAvStream();
                            AVDictionaryEntry *tag = NULL;
                            int m_Rotate = 0;

                            uint8_t *displaymatrix = av_stream_get_side_data(videoStream,
                                                                             AV_PKT_DATA_DISPLAYMATRIX,
                                                                             NULL);
                            if (displaymatrix) {
                                double theta = -av_display_rotation_get((int32_t *) displaymatrix);
                                theta -= 360 * floor(theta / 360 + 0.9 / 360);
                                m_Rotate = (int) theta;
                            }
                            this->callAvFrame->onRotate(m_Rotate);
                        }
                        this->videoPacketQueue.push(avPacket);
                    }

                    std::unique_lock<std::mutex> fullLock(fullVideoMutex);
                    videoFull.wait(fullLock,
                                   [&] { return this->videoPacketQueue.size() <= maxQueueSize; });

                } else if (avPacketData.getMediaType() == AVMEDIA_TYPE_AUDIO) {
                    {
                        // 音频avPacket互斥锁
                        std::lock_guard<std::mutex> lock(audioDecoderMutex);
                        avPacket = avPacketQueue.front();
                        if (audioCodecContext == nullptr)
                            audioCodecContext = avPacketData.getAvCodecContext();
                        if (audioStream == nullptr) audioStream = avPacketData.getAvStream();
                        this->audioPacketQueue.push(avPacket);
                    }

                    std::unique_lock<std::mutex> fullLock(fullAudioMutex);
                    audioFull.wait(fullLock,
                                   [&] { return this->audioPacketQueue.size() <= maxQueueSize; });
                }
                avPacketQueue.pop();
                std::this_thread::sleep_for(std::chrono::milliseconds(1));
            }
        }
        LOGE("decoderMedia end");
//        qDebug() << "decoderMedia end";
        minusSignal();
    }


    void FmPlayer::decoderVideo() {

#ifdef _WIN32
        setThreadName("decoderVideoThread");
#endif
        AVFrame *avFrame = av_frame_alloc();
        AVFrame *tmpFrame = av_frame_alloc();
        int64_t count = 0;
        int skipFrames = 2;
        int frameCount = 0;
        while (true) {
//            qDebug() << "decoderVideo";
            if (!isRunning && videoPacketQueue.empty()) {
                break;
            }

            {
                //播放暂停互斥锁
                if (!checkPlayer()) {
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                    continue;
                }
            }

            AVPacket *avPacket = nullptr;
            {
                std::lock_guard<std::mutex> lock(videoDecoderMutex);
                //获取解码AvPacket 互斥锁
                if (!videoPacketQueue.empty() && videoCodecContext != nullptr) {
                    avPacket = videoPacketQueue.front();
                    videoPacketQueue.pop();
                } else {
                    callAvFrame->onLoading();
                }
            }
            if (avPacket == nullptr || videoCodecContext == nullptr) continue;
#ifdef FM_ANDROID
            if (!this->isAndroidError)
                this->videoAndroidDecoder(av_packet_clone(avPacket), avFrame);
#endif
            if (this->isAndroidError) {
                std::lock_guard<std::mutex> lockVideoSeekMutex(videoSeekMutex);
                int ret = avcodec_send_packet(videoCodecContext, avPacket);
                if (ret < 0) {
                    LOGE("1 Error sending packet to decoder\n");
                    std::cout << ret << std::endl;
                    if (ret != AVERROR(EAGAIN)) { //如果是需要更多数据，那么不用退出
                        this->isRunning = false;
                        break; //跳出就可以，如果break 需要设置 running 为 false,不然出不去
                    }
                }

                // 接收解码后的帧
                if (avcodec_receive_frame(videoCodecContext, avFrame) >= 0) {
                    {
                        double rate = av_q2d(this->videoStream->avg_frame_rate);
                        if (rate > 30)
                            skipFrames = rate / 30.0;
                        std::lock_guard<std::mutex> lock(videoRenderMutex);
//                                    if(rate < 60 || frameCount % skipFrames == 0) {
                        if (this->videoDecoder->isHardDecoder1()) {
                            if (av_hwframe_transfer_data(tmpFrame, avFrame, 0) <
                                0) { //av_hwframe_transfer_data
                                fprintf(stderr, "Error transferring the data to system memory\n");
                                this->isRunning = false;
                                break;
                            }
                            tmpFrame->width = avFrame->width;
                            tmpFrame->height = avFrame->height;
                            tmpFrame->pts = avFrame->pts;
                            av_frame_copy_props(tmpFrame,
                                                avFrame);                        // 仅将“metadata”字段从src复制到dst。

                            videoFrameQueue.push(av_frame_clone(tmpFrame));
                            av_frame_unref(tmpFrame);
                        } else {
                            videoFrameQueue.push(av_frame_clone(avFrame));
                        }
                        frameCount++;
                        av_frame_unref(avFrame);
                    }
                    std::unique_lock<std::mutex> fullLock(fullVideoFrameMutex);
                    videoFrameFull.wait(fullLock,
                                        [&] {
                                            return this->videoFrameQueue.size() <= maxQueueSize;
                                        });
                }
            }
            av_packet_free(&avPacket);
            if (this->videoPacketQueue.size() <= maxQueueSize)
                videoFull.notify_one();
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }

        av_frame_free(&avFrame);
        av_frame_free(&tmpFrame);
//        qDebug() << "decoder video end";
        LOGE("decoder video end");
        minusSignal();

    }

    void FmPlayer::decoderAudio() {
#ifdef _WIN32
        setThreadName("decoderAudioThread");
#endif
        AVFrame *avFrame = av_frame_alloc();

        while (true) {
            if (!isRunning && audioPacketQueue.empty()) break;
            {
                //播放互斥锁
                if (!checkPlayer()) {
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                    continue;
                }
            }

            {
                AVPacket *avPacket = nullptr;
                {
                    std::lock_guard<std::mutex> lock(audioDecoderMutex);
                    //音频解码AvPacket 互斥锁
                    if (!audioPacketQueue.empty() && audioCodecContext != nullptr) {
                        avPacket = audioPacketQueue.front();
                        audioPacketQueue.pop();
                    }
                }
                if (avPacket == nullptr || audioCodecContext == nullptr) continue;
                {
                    std::lock_guard<std::mutex> lockAudioSeekMutex(audioSeekMutex);

                    int ret = avcodec_send_packet(audioCodecContext, avPacket);
                    if (ret < 0 && ret != AVERROR(EAGAIN)) {
                        std::cout << ret << std::endl;
                        fprintf(stderr, "Error sending packet to decoder\n");
                        LOGE("2 Error sending packet to decoder");
                        this->isError = true;
                        av_packet_free(&avPacket);
                        this->isRunning = false;
                        break;
                    }

                    // 接收解码后的帧
                    while (avcodec_receive_frame(audioCodecContext, avFrame) >= 0) {
                        {
                            std::lock_guard<std::mutex> lock(audioRenderMutex);
                            audioFrameQueue.push(av_frame_clone(avFrame));
                            av_frame_unref(avFrame);
                        }
                        std::unique_lock<std::mutex> fullLock(fullAudioFrameMutex);
                        audioFrameFull.wait(fullLock, [&] {
                            return this->audioFrameQueue.size() <= maxQueueSize;
                        });
                    }
                }
                av_packet_free(&avPacket);
                if (this->audioPacketQueue.size() <= maxQueueSize) {
                    audioFull.notify_one();
                }
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
        av_frame_free(&avFrame);
//        qDebug() << "decoderAudio end";
        LOGE("decoder audio end");
        minusSignal();

    }

    void FmPlayer::renderVideo() {
#ifdef _WIN32
        setThreadName("renderVideoThread");
#endif

#ifdef FM_ANDROID
        setThreadName("renderVideoThread");
#endif
        AVFrame *yuv420Frame = av_frame_alloc();  //存放RGB数据的缓冲区
        uint8_t *outBuffer = nullptr;
        int skipFrames = 2;  // 跳过两帧
        int frameCount = 0;
        while (true) {

            if (!isRunning && this->videoFrameQueue.empty()) break;
            {
                if (!checkPlayer()) { //播放互斥锁
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                    continue;
                }
            }
            {
                if (!this->videoFrameQueue.empty()) {
                    {
                        // 视频格式转换
                        //渲染互斥锁
                        AVFrame *avFrame = nullptr;
                        auto currentTime = std::chrono::system_clock::now();
                        {
                            std::lock_guard<std::mutex> lock(videoRenderMutex);
                            if (this->videoFrameQueue.empty()) continue;
                            avFrame = this->videoFrameQueue.front();
                            this->videoFrameQueue.pop();

                        }
                        if (swsContext == nullptr && videoCodecContext != nullptr) {
                            int width = videoCodecContext->width;
                            int height = videoCodecContext->height;
                            int newWidth = width;
                            if (avFrame->linesize[0] > width) {
                                newWidth = avFrame->linesize[0];
                            }

                            swsContext = sws_getContext(width, height,
//                                                            videoCodecContext->pix_fmt,
                                                        (AVPixelFormat) avFrame->format,
                                                        newWidth, height,
                                                        AV_PIX_FMT_YUV420P, SWS_BICUBIC, NULL, NULL,
                                                        NULL);
                            int alignment = avFrame->linesize[0] / avFrame->width; // 对齐方式
                            yuv420Frame->width = newWidth;
                            yuv420Frame->height = avFrame->height;
                            yuv420Frame->format = AV_PIX_FMT_YUV420P;
                            av_image_alloc(yuv420Frame->data, yuv420Frame->linesize, newWidth,
                                           height,
                                           AV_PIX_FMT_YUV420P,
                                           1);

                        }
                        sws_scale(swsContext, avFrame->data, avFrame->linesize, 0,
                                  videoCodecContext->height,
                                  yuv420Frame->data, yuv420Frame->linesize);
                        if (this->callAvFrame != nullptr)
                            this->callAvFrame->onVideoFrame(yuv420Frame);


                        asyncTime(currentTime, avFrame->pts, MediaType::VIDEO);
                        av_frame_free(&avFrame);
                    }
                }
                if (this->videoFrameQueue.size() <= maxQueueSize) {
                    videoFrameFull.notify_one();
                }
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
        av_frame_free(&yuv420Frame);
//        qDebug() << "renderVideo end";
        LOGE("renderVideo end");
        minusSignal();
    }

    void FmPlayer::renderAudio() {
#ifdef _WIN32
        setThreadName("renderAudioThread");
#endif
        while (true) {
//            LOGE("rednerAudio isRunning %d size %d", isRunning, this->audioFrameQueue.size());
//            LOGE("apacketSize %d", this->audioPacketQueue.size());
//            LOGE("vpacketSize %d", this->videoPacketQueue.size());
//            LOGE("aframe %d", this->audioFrameQueue.size());
//            LOGE("vframe %d", this->videoFrameQueue.size());
            if (!isRunning && this->audioFrameQueue.empty()) {
                break;
            }
            {
                if (!checkPlayer()) { //播放互斥锁
                    std::this_thread::sleep_for(std::chrono::milliseconds(1));
                    continue;
                }
            }
            {
                if (!this->audioFrameQueue.empty()) {
                    if (sink == nullptr && src == nullptr && audioCodecContext != nullptr) {
                        init_filter_graph();
                    }
                    {
                        AVFrame *avFrame = nullptr;
                        auto currentTime = std::chrono::system_clock::now();
                        {
                            std::lock_guard<std::mutex> lock(audioRenderMutex);

                            //渲染互斥锁
                            if (this->audioFrameQueue.empty()) continue;
                            avFrame = this->audioFrameQueue.front();
                            this->audioFrameQueue.pop();
                        }
                        std::lock_guard<std::mutex> lock(speedAudioMutex);
                        this->speedAudioFilter(av_frame_clone(avFrame));

//                        int data_size = av_get_bytes_per_sample(audioCodecContext->sample_fmt);
//                        if (data_size >= 0 && this->callAvFrame != nullptr) {
//                            this->callAvFrame->onAudioFrame(avFrame, audioCodecContext->ch_layout.nb_channels,
//                                                            audioCodecContext->sample_fmt, data_size);
//                        }
//                        asyncTime(currentTime, avFrame->pts, MediaType::AUDIO);
                        av_frame_free(&avFrame);

                    }
                    if (this->audioFrameQueue.size() <= maxQueueSize) {
                        this->audioFrameFull.notify_one();
                    }
                }
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(1));

        }
        LOGE("renderAudio end");
        minusSignal();
        if (this->callAvFrame != nullptr && this->isStop == false) {
            LOGE("音频结束了");
            this->callAvFrame->onEnd(this->isError);
        }


    }

    void FmPlayer::setCallAvFrame(CallAVFrame *callAvFrame) {
        FmPlayer::callAvFrame = callAvFrame;
    }

/**
 * 音视频同步代码， 根据pts 和 Stream得 time base 获取 pts 对应毫秒
 * 根据音频时间对准视频延迟， 音频延迟计算  int64_t distance = (1024 * 1000000 / audioCodecContext->sample_rate / 1000) / speedAudio;  //算出延迟时间 1024 nb_sample
 * 视频同步 根据pts 计算与音频查询，查询多少延迟多少
 * @param currentTime
 * @param pts
 * @param mediaType
 */
    void
    FmPlayer::asyncTime(std::chrono::time_point<std::chrono::system_clock> currentTime, int64_t pts,
                        MediaType mediaType) {
        auto timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
                currentTime.time_since_epoch()
        ).count();
        {
            // 将时间点转换为时间戳
            std::lock_guard<std::mutex> lock(this->timeMutex);
            if (mediaType == MediaType::VIDEO) {
                double ptsInSeconds = (double) pts * av_q2d(videoStream->time_base);
                this->videoPts = ptsInSeconds * 1000;
            }
            if (mediaType == MediaType::AUDIO) {
                double ptsInSeconds = (double) pts * av_q2d(audioStream->time_base);
//                this->audioPts = ptsInSeconds * 1000 * speedAudio; // speedAudio 倍速计算，pts * 2.0 //sleep 除以 2.0
                this->audioPts = ptsInSeconds * 1000; // speedAudio 倍速计算，pts * 2.0 //sleep 除以 2.0
            }
        }
        auto elapsedTime = std::chrono::system_clock::now();

        auto elapsedTimestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
                elapsedTime.time_since_epoch()
        ).count();
        auto distanceTimestamp = elapsedTimestamp - timestamp;
        if (mediaType == MediaType::AUDIO) {
            AVCodecParameters *audioCodecParams = audioStream->codecpar;
            int frameSize = audioCodecParams->frame_size;
            int sampleRate = audioCodecParams->sample_rate;

            double playbackInterval = (double) frameSize / sampleRate;
//            int64_t distance = (1024 * 1000000 / audioCodecContext->sample_rate / 1000) / speedAudio;
            int64_t distance = playbackInterval * 1000 / speedAudio;
            std::this_thread::sleep_for(std::chrono::milliseconds(distance - distanceTimestamp));
        }
//        LOGE("videoPts %lld, audioPts %lld, videoFrame %d, audioFrame %d", this->videoPts, this->audioPts, this->videoFrameQueue.size(), this->audioFrameQueue.size());
//        qDebug() << "videoPts:" << this->videoPts << "audioPts:" << this->audioPts
//                 << "videoFrame:" << this->videoFrameQueue.size() << ",audioFrame:" << this->audioFrameQueue.size()
//                 << "videoPacket:" << this->videoPacketQueue.size() << ",audioPacket:" << this->audioPacketQueue.size();

        if (mediaType == MediaType::VIDEO) {
            if (this->audioStream != nullptr) {
                if (this->videoPts > this->audioPts && this->videoPts - this->audioPts < 1000) {
//                    LOGE("sleep %lld", this->videoPts - this->audioPts);
                    std::this_thread::sleep_for(
                            std::chrono::milliseconds(this->videoPts - this->audioPts));
                }
            } else {
                double rate = av_q2d(this->videoStream->avg_frame_rate);
                std::this_thread::sleep_for(
                        std::chrono::milliseconds((int64_t) (1000 / rate) - distanceTimestamp));
            }

            int64_t duration = videoStream->duration * av_q2d(videoStream->time_base);
            if (duration < 0) duration = 0;
            if (this->callAvFrame != nullptr && !isError)
                this->callAvFrame->onProgress(duration, this->videoPts / 1000,
                                              this->videoDecoder->getCacheTime() / 1000);
        }
    }

/**
 * 初始化音频 filter
 * @return
 */
    int FmPlayer::init_filter_graph() {
        //初始化AVFilterGraph
        this->graph = avfilter_graph_alloc();
        //获取abuffer用于接收输入端
        const AVFilter *abuffer = avfilter_get_by_name("abuffer");
        AVFilterContext *abuffer_ctx = avfilter_graph_alloc_filter(graph, abuffer, "src");
        //设置参数，这里需要匹配原始音频采样率8000、数据格式（位数:16）、单声道
        char initArgs[100];
        char ch_layout[100];
        av_channel_layout_describe(&audioCodecContext->ch_layout, ch_layout, sizeof(ch_layout));

        sprintf(initArgs, "sample_rate=%d:sample_fmt=%s:channel_layout=%s",
                audioCodecContext->sample_rate,
                av_get_sample_fmt_name(audioCodecContext->sample_fmt), ch_layout);
        // fprintf(stderr, initArgs);
        if (avfilter_init_str(abuffer_ctx, initArgs) < 0) {
            fprintf(stderr, "error init abuffer filter");
            return -1;
        }

        //初始化atempo filter
        const AVFilter *atempo = avfilter_get_by_name("atempo");
        AVFilterContext *atempo_ctx = avfilter_graph_alloc_filter(graph, atempo, "atempo");
        //这里采用av_dict_set设置参数
        AVDictionary *args = NULL;
        char speed[20]; //倍数
        // 使用 sprintf 将 float 转为 char*
        std::sprintf(speed, "%f", speedAudio);
        av_dict_set(&args, "tempo", speed, 0);//这里传入外部参数，可以动态修改
        if (avfilter_init_dict(atempo_ctx, &args) < 0) {
            fprintf(stderr, "error init atempo filter");
            return -1;
        }

//        if (audioCodecContext->ch_layout.nb_channels > 2) { // 当通道数大于2的时候，切换成立体声
//            int64_t channel_layout = AV_CH_LAYOUT_STEREO;  // 例如，立体声
        int64_t channel_layout = AV_CH_LAYOUT_MONO; //TODO 避免数据读取出错，用了滤镜，全部改成一通道
        av_get_channel_layout_string(ch_layout, sizeof(ch_layout), 0, channel_layout);
//        }

        const AVFilter *aformat = avfilter_get_by_name("aformat");
        char aformatArgs[100];
//        sprintf(aformatArgs, "sample_rates=%d:sample_fmts=%s:channel_layouts=%s", audioCodecContext->sample_rate,
//                av_get_sample_fmt_name(audioCodecContext->sample_fmt), ch_layout);

        sprintf(aformatArgs, "sample_rates=%d:sample_fmts=%s:channel_layouts=%s",
                audioCodecContext->sample_rate,
                av_get_sample_fmt_name(AV_SAMPLE_FMT_S16), ch_layout);

        AVFilterContext *aformat_ctx = avfilter_graph_alloc_filter(graph, aformat, "aformat");
        if (avfilter_init_str(aformat_ctx, aformatArgs) < 0) {
            fprintf(stderr, "error init aformat filter");
            return -1;
        }
        //初始化sink用于输出
        const AVFilter *sink = avfilter_get_by_name("abuffersink");
        AVFilterContext *sink_ctx = avfilter_graph_alloc_filter(graph, sink, "sink");
        if (avfilter_init_str(sink_ctx, NULL) < 0) {//无需参数
            fprintf(stderr, "error init sink filter");
            return -1;
        }
        //链接各个filter上下文
        if (avfilter_link(abuffer_ctx, 0, atempo_ctx, 0) != 0) {
            fprintf(stderr, "error link to atempo filter");
            return -1;
        }
        if (avfilter_link(atempo_ctx, 0, aformat_ctx, 0) != 0) {
            fprintf(stderr, "error link to aformat filter");
            return -1;
        }
        if (avfilter_link(aformat_ctx, 0, sink_ctx, 0) != 0) {
            fprintf(stderr, "error link to sink filter");
            return -1;
        }
        if (avfilter_graph_config(graph, NULL) < 0) {
            fprintf(stderr, "error config filter graph");
            return -1;
        }
        this->src = abuffer_ctx;
        this->sink = sink_ctx;
        fprintf(stderr, "init filter success...");
        return 0;

    }

/**
 * 倍速播放数据处理
 * @param avFrame
 * @return
 */
    int FmPlayer::speedAudioFilter(AVFrame *avFrame) {
// 初始化过滤器
        int64_t pts = avFrame->pts;
        AVFrame *filtered_frame = av_frame_alloc();
        int ret = av_buffersrc_add_frame(src, avFrame);
        if (ret < 0) {
            return -1;
        }
        int response = -1;
        while (true) {
            auto currentTime = std::chrono::system_clock::now();

            response = av_buffersink_get_frame(sink, filtered_frame);
            if (response == AVERROR(EAGAIN) || response == AVERROR_EOF)
                break;

            // 在这里可以对 filtered_frame 进行处理，例如输出到文件或播放}
            int data_size = av_get_bytes_per_sample(
                    static_cast<AVSampleFormat>(filtered_frame->format));
            if (data_size >= 0 && this->callAvFrame != nullptr) {

                this->callAvFrame->onAudioFrame(filtered_frame,
                                                filtered_frame->ch_layout.nb_channels,
                                                static_cast<AVSampleFormat>(filtered_frame->format),
                                                data_size);
            }
            av_frame_unref(filtered_frame);
            asyncTime(currentTime, pts, MediaType::AUDIO);
        }
        av_frame_free(&filtered_frame);
        av_frame_free(&avFrame);
        return 0;
    }

    void FmPlayer::lock() {
        std::lock_guard<std::mutex> lockDecoderMutex(decoderMutex);  // 解码视频互斥锁
        std::lock_guard<std::mutex> lockTimeMutex(timeMutex); //时间互斥锁
        std::lock_guard<std::mutex> playerLock(playerMutex);
        clearAllQueue();
    }

    void FmPlayer::clearAllQueue() {
        {
            std::lock_guard<std::mutex> lockVideoDecoderMutex(videoDecoderMutex); // 视频解码互斥锁
            while (!this->videoPacketQueue.empty()) {
                av_packet_free(&this->videoPacketQueue.front());
                this->videoPacketQueue.pop();
            }
        }

        {
            std::lock_guard<std::mutex> lockAudioDecoderMutex(audioDecoderMutex); //音频解码互斥锁
            while (!this->audioPacketQueue.empty()) {
                av_packet_free(&this->audioPacketQueue.front());
                this->audioPacketQueue.pop();
            }
        }
        {
            std::lock_guard<std::mutex> lockVideoRenderMutex(videoRenderMutex); //视频播放互斥锁
            while (!this->videoFrameQueue.empty()) {
                av_frame_free(&this->videoFrameQueue.front());
                this->videoFrameQueue.pop();
            }
        }

        {
            std::lock_guard<std::mutex> lockAudioRenderMutex(audioRenderMutex); //音频播放互斥锁
            while (!this->audioFrameQueue.empty()) {
                av_frame_free(&this->audioFrameQueue.front());
                this->audioFrameQueue.pop();
            }
        }

        this->notifyFull();
    }

/**
 * seek
 * @param time
 */
    int FmPlayer::seek(int64_t time) {
        if (this->videoDecoder != nullptr && videoStream != nullptr) {
            {
                std::lock_guard<std::mutex> seekLock(decoderSeekMutex);

                std::lock_guard<std::mutex> lockAudioSeekMutex(audioSeekMutex);

                std::lock_guard<std::mutex> lockVideoSeekMutex(
                        videoSeekMutex); // seek 释放缓冲区，解码时候必须互斥
                clearAllQueue();

                int ret = this->videoDecoder->seek(time * AV_TIME_BASE);
                if (ret >= 0) this->isDecoderEnd = false;

                return ret;
            }
        }
        return 0;
    }

    float FmPlayer::getSpeedAudio() const {
        return speedAudio;
    }

    void FmPlayer::updateSpeedAudio(float speed) {
//        std::lock_guard<std::mutex> lock(audioRenderMutex);
        // qDebug() << speed;
        std::lock_guard<std::mutex> lock(speedAudioMutex);
        this->speedAudio = speed;
        LOGE("1 %f", speed);

        if (audioCodecContext != nullptr) {
            LOGE("2 %f", speed);
            init_filter_graph();
        }
    }

    void FmPlayer::play() {
        std::lock_guard<std::mutex> playerLock(playerMutex);
        LOGE("play");
        this->isPlayer = true;
    }

    void FmPlayer::pause() {
        std::lock_guard<std::mutex> playerLock(playerMutex);
        this->isPlayer = false;
    }

    bool FmPlayer::checkPlayer() {
        std::lock_guard<std::mutex> playerLock(playerMutex);
        if (!this->isPlayer && this->isRunning) { //运行中 停止播放
            return false;
        }
        return true;
    }

#ifdef FM_ANDROID

    void FmPlayer::videoAndroidDecoder(AVPacket *avPacket, AVFrame *frame) {
        auto currentTime = std::chrono::system_clock::now();
        std::lock_guard<std::mutex> lock(videoRenderMutex);

        AVPacket *out = av_packet_alloc();
        if (!this->isAndroidError) {
            this->isAnnexb = findAnnexb();
        }
        out->pts = avPacket->pts;
        if (this->isAnnexb && StreamFilter::filter_stream(bsf_ctx, avPacket, &out, 0) < 0) {
            av_packet_free(&out);
            this->isAndroidDecoder = false;
            return;
        }

        if (this->callAvFrame != nullptr) {
            if (this->isAnnexb) {
                this->isAndroidDecoder = this->callAvFrame->onVideoPacket(videoCodecContext->width,
                                                                          videoCodecContext->height,
                                                                          videoCodecContext->codec_id,
                                                                          out);
            } else {
                this->isAndroidDecoder = this->callAvFrame->onVideoPacket(videoCodecContext->width,
                                                                          videoCodecContext->height,
                                                                          videoCodecContext->codec_id,
                                                                          avPacket);
            }

            if (!this->isAndroidDecoder) {
                this->isAndroidError = true;
            }
        }
        if (this->isAndroidDecoder) {
            std::unique_lock<std::mutex> fullLock(fullVideoFrameMutex);
            videoFrameFull.wait(fullLock,
                                [&] {
                                    return this->videoFrameQueue.size() <= maxQueueSize;
                                });
            asyncTime(currentTime, out->pts, MediaType::VIDEO);
        }
        av_packet_free(&out);
        av_packet_free(&avPacket);


        //数据回调回来的做法，存在数据拷贝损耗，上面是直接交付给mediacodec解码
//        if (!this->isAndroidDecoder) {
//            initAndroidDecoder();
//        }
//        if (isAndroidDecoder) {
//            AVPacket *out = av_packet_alloc();
//            if (StreamFilter::filter_stream(bsf_ctx, avPacket, &out, 0) < 0) {
//                av_packet_free(&out);
//                this->isAndroidDecoder = false;
//                return;
//            }
//            this->outputFile.write(reinterpret_cast<char *>(out->data), out->size);
//            std::queue<char *> queue = this->mediaCodecDecoder->onFrame(
//                    reinterpret_cast<char *>(out->data), out->size);
//
//            while (!queue.empty()) {
//                {
//                    std::lock_guard<std::mutex> lock(videoRenderMutex);
//                    int format = this->mediaCodecDecoder->getMediaFormat();
//                    if (format < 0) break;
//
//                    char *frameData = queue.front();
//                    if (!isInitFrame) {
//                        frame->width = videoCodecContext->width;
//                        frame->height = videoCodecContext->height;
//                        frame->format = getAVPixelFormat(format);
//                        av_image_alloc(frame->data, frame->linesize, frame->width,
//                                       frame->height,
//                                       getAVPixelFormat(format),
//                                       1);
//                        isInitFrame = true;
//                    }
//                    frame->pts = out->pts;
//                    int ySize = frame->width * frame->height;
//                    int uvSize = frame->width * frame->height / 4;
//                    if (frame->format == AV_PIX_FMT_YUV420P) {
//                        memcpy(frame->data[0], frameData, ySize);
//                        memcpy(frame->data[1], frameData + ySize, uvSize);
//                        memcpy(frame->data[2], frameData + ySize + uvSize, uvSize);
//                    } else if (frame->format == AV_PIX_FMT_NV12) {
//                        memcpy(frame->data[0], frameData, ySize);
//                        memcpy(frame->data[1], frameData + ySize, uvSize * 2);
//                    }
//                    videoFrameQueue.push(av_frame_clone(frame));
////                    free(frameData);
//                    queue.pop();
//                }
//            }
//            av_packet_free(&out);
//
//            std::unique_lock<std::mutex> fullLock(fullVideoFrameMutex);
//            videoFrameFull.wait(fullLock,
//                                [&] {
//                                    return this->videoFrameQueue.size() <= maxQueueSize;
//                                });
//        }
    }


    bool FmPlayer::findAnnexb() {
        if (videoStream->codecpar->codec_id == AV_CODEC_ID_H264) {
            if (StreamFilter::open_bitstream_filter(videoStream, &bsf_ctx,
                                                    "h264_mp4toannexb") >= 0) {
                return true;
            }
        }
        if (videoStream->codecpar->codec_id == AV_CODEC_ID_H265) {
            if (StreamFilter::open_bitstream_filter(videoStream, &bsf_ctx,
                                                    "hevc_mp4toannexb") >= 0) {
                return true;
            }
        }
        return false;
    }

    bool FmPlayer::checkKeyframe(AVPacket *packet) {
        return packet->flags & AV_PKT_FLAG_KEY;
    }

    void FmPlayer::resetPlayer(int64_t time) {
        this->videoDecoder = nullptr;
        this->videoCodecContext = nullptr;
        this->audioCodecContext = nullptr;
        this->videoStream = nullptr;
        this->audioStream = nullptr;
        startPlayer(this->input.data(), time);
    }

    void FmPlayer::notifyFull() {
        videoFull.notify_one();
        videoFrameFull.notify_one();
        audioFull.notify_one();
        audioFrameFull.notify_one();
    }

    bool FmPlayer::allQueueIsEmpty() {
        return
                this->audioFrameQueue.size() == 0 &&
                this->audioPacketQueue.size() == 0 &&
                this->videoFrameQueue.size() == 0 &&
                this->videoPacketQueue.size() == 0;
    }

    void FmPlayer::minusSignal() {
        std::lock_guard lockGuard(signalMutex);
        this->signal--;
    }

    void FmPlayer::plusSignal() {
        std::lock_guard lockGuard(signalMutex);
        this->signal++;
    }


#endif


}