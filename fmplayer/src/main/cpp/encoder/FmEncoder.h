//
// Created by fuweicong on 2023/12/10.
//

#ifndef FMPLAYER_FMENCODER_H
#define FMPLAYER_FMENCODER_H

#include <string>

#include "../util.h"
#include <mutex>
#include <queue>
#include <thread>
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
#include <libswresample/swresample.h>
#include "libavutil/time.h"

}
namespace fm {
#define STREAM_DURATION   10.0
#define STREAM_FRAME_RATE 25 /* 25 images/s */
#define STREAM_PIX_FMT    AV_PIX_FMT_YUV420P /* default pix_fmt */

#define SCALE_FLAGS SWS_BICUBIC
    typedef struct OutputStream {
        AVStream *st;
        AVCodecContext *enc;

        /* pts of the next frame that will be generated */
        int64_t next_pts;
        int samples_count;

        AVFrame *frame;
        AVFrame *tmp_frame;

        AVPacket *tmp_pkt;

        float t, tincr, tincr2;

        struct SwsContext *sws_ctx;
        struct SwrContext *swr_ctx;
    } OutputStream;

    class FrameInfo{
    private:
        char* data;
    public:
        char *getData() const;

        int getDataLength() const;

        long getSeconds() const;

    public:
        FrameInfo(char *data, int dataLength, long seconds);

    private:
        int dataLength;
        long seconds;
    };

    class FmEncoder {
    public:
        FmEncoder(const char* input, int width, int height, AVPixelFormat format, int sample_rate, int channels);
        bool init();
        int encoder_video_frame(char* data, int dataLength, long seconds);
        int encoder_audio_frame(int16_t * data, int dataLength, long seconds);
        void start_encoder_video();
        void start_encoder_audio();
        void add_video_frame(char* data, int dataLength, long seconds);
        void add_audio_frame(char * data, int dataLength, long seconds);
        void end();
    private:
        const char* output;
        int width;
        int height;
        int isExit = false;
        int imageIndex = 0;
        int sample_rate;
        int channels;
        AVPixelFormat format;
        AVDictionary *opt = NULL;
        const AVOutputFormat *fmt;
        AVFormatContext *outputFormatContext;
        const AVOutputFormat *outputFormat;
        AVCodecContext *videoCodecContext;
        AVStream *videoStream;
        AVFormatContext *oc;

        std::mutex videoQueueMutex;
        std::mutex audioQueueMutex;
        std::condition_variable videoQueueFull;   // 队列不满的条件变量
        std::condition_variable audioQueueFull;
        std::queue<FrameInfo> videoQueue;
        std::queue<FrameInfo> audioQueue;

        int ret;
        const AVCodec *audio_codec, *video_codec;
        int have_video = 0, have_audio = 0;
        int encode_video = 0, encode_audio = 0;

        void add_stream(OutputStream *ost, AVFormatContext *oc,
                                       const AVCodec **codec,
                                       std::string codecName);
        void open_video(AVFormatContext *oc, const AVCodec *codec,
                        OutputStream *ost, AVDictionary *opt_arg);
        AVFrame *alloc_picture(enum AVPixelFormat pix_fmt, int width, int height);
        AVFrame *alloc_audio_frame(enum AVSampleFormat sample_fmt,
                                   const AVChannelLayout *channel_layout,
                                   int sample_rate, int nb_samples);
        void open_audio(AVFormatContext *oc, const AVCodec *codec,
                        OutputStream *ost, AVDictionary *opt_arg);
        OutputStream video_st = { 0 }, audio_st = { 0 };


        int write_frame(AVFormatContext *fmt_ctx, AVCodecContext *c,
                        AVStream *st, AVFrame *frame, AVPacket *pkt);



        void close_stream(AVFormatContext *oc, OutputStream *ost);




    };
}


#endif //FMPLAYER_FMENCODER_H
