//
// Created by fuweicong on 2023/11/14.
//

#ifndef QTANDROID_FMPLAYER_H
#define QTANDROID_FMPLAYER_H

#include "VideoDecoder.h"
#include "CallAVFrame.h"
#include <iostream>
#include <chrono>
#include <thread>
#include "../util.h"

#ifdef _WIN32
#include <thread>
#include <windows.h>
#endif
#ifdef FM_ANDROID

#include <jni.h>
#include "MediaCodecDecoder.h"
#include "StreamFilter.h"
#include <pthread.h>
#include <fstream>

#endif
namespace fm {
    enum MediaType {
        VIDEO,
        AUDIO
    };

    class FmPlayer {
    private:
        CallAVFrame *callAvFrame = nullptr;
        AVFilterGraph *graph = nullptr;
        AVFilterContext *src = nullptr, *sink = nullptr;
        float speedAudio = 1.0;
        std::thread decoderMediaThread;
        std::thread videoDecoderThread;
        std::thread audioDecoderThread;
        std::thread videoRenderThread;
        std::thread audioRenderThread;
        int signal = 0;
    public:
        float getSpeedAudio() const;

    private:
        int init_filter_graph();

        struct SwsContext *swsContext = nullptr;
        struct SwsContext *hwSwsContext = nullptr;

        void lock();

        void clearAllQueue();

        bool checkPlayer();

    public:
        void setCallAvFrame(CallAVFrame *callAvFrame);

    public:
        FmPlayer();

        ~FmPlayer();

        int maxQueueSize = 100;

        void startPlayer(const char *input);

        std::unique_ptr<VideoDecoder> videoDecoder = nullptr;

        int speedAudioFilter(AVFrame *avFrame);

        // 解码音视频线程函数
        void decoderMedia();

        std::mutex startMutex;
        std::mutex decoderMutex;  // 解码视频互斥锁
        std::mutex audioDecoderMutex; //音频解码互斥锁
        std::mutex videoDecoderMutex; // 视频解码互斥锁
        std::mutex audioRenderMutex; //音频播放互斥锁
        std::mutex videoRenderMutex; //视频播放互斥锁
        std::mutex audioSeekMutex; // 音频seek
        std::mutex videoSeekMutex; // 视频seek
        std::mutex decoderSeekMutex; //获取packet时候 互斥
        std::mutex playerMutex;
        std::mutex timeMutex; //时间互斥锁
        std::mutex fullVideoMutex; //数据队列满互斥
        std::mutex fullAudioMutex;
        std::condition_variable videoFull;   // 队列不满的条件变量
        std::condition_variable audioFull;
        std::mutex fullVideoFrameMutex;
        std::mutex fullAudioFrameMutex;
        std::mutex speedAudioMutex;
        std::condition_variable videoFrameFull;
        std::condition_variable audioFrameFull;
        //视频解码
        AVCodecContext *videoCodecContext = nullptr;
        AVStream *videoStream = nullptr;
        int64_t videoPts = 0;
        int64_t audioPts = 0;
        //音频解码
        AVCodecContext *audioCodecContext = nullptr;
        AVStream *audioStream = nullptr;

        // 音视频数据队列
        std::queue<AVPacket *> videoPacketQueue;
        std::queue<AVPacket *> audioPacketQueue;
        std::queue<AVFrame *> videoFrameQueue;
        std::queue<AVFrame *> audioFrameQueue;

        void decoderVideo();

        void decoderAudio();

        void renderVideo();

        void renderAudio();

        void asyncTime(std::chrono::time_point<std::chrono::system_clock> currentTime, int64_t,
                       MediaType);

        void seek(int64_t time);

        void start();

        void stop();

        void play();

        void pause();

        void updateSpeedAudio(float speed);

        AVCodecContext *codecContext;
        bool isPlayer = false;
        bool isRunning = false;
        bool isStop = false; //却分用户触发结束，还是自动播放结束
        std::string input;

        bool checkKeyframe(AVPacket *packet);

        bool findAnnexb();

        AVBSFContext *bsf_ctx; //将avPacket 变成 h264

#ifdef FM_ANDROID
        void videoAndroidDecoder(AVPacket *avPacket, AVFrame *avFrame);


        bool isAndroidDecoder = false;
        bool isAndroidError = false;
        bool isAnnexb = false;
        std::ofstream outputFile;
#endif
    };
}

#endif //QTANDROID_FMPLAYER_H
