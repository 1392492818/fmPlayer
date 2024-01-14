//
// Created by fuweicong on 2023/12/10.
//

#ifndef FMPLAYER_FMENCODER_H
#define FMPLAYER_FMENCODER_H
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

    class FmEncoder {
    public:
        FmEncoder(const char* input, int width, int height, AVPixelFormat format);
        bool init();
        int add_video_frame(char* data, int width, int height,int format);
        int add_audio_frame(char* data);
        void end();
    private:
        const char* output;
        int width;
        int height;
        AVPixelFormat format;
        AVDictionary *opt = NULL;
        const AVOutputFormat *fmt;
        AVFormatContext *outputFormatContext;
        const AVOutputFormat *outputFormat;
        AVCodecContext *videoCodecContext;
        AVStream *videoStream;
        AVFormatContext *oc;
        int ret;
        const AVCodec *audio_codec, *video_codec;
        int have_video = 0, have_audio = 0;
        int encode_video = 0, encode_audio = 0;

        void add_stream(OutputStream *ost, AVFormatContext *oc,
                                       const AVCodec **codec,
                                       enum AVCodecID codec_id);
        void open_video(AVFormatContext *oc, const AVCodec *codec,
                        OutputStream *ost, AVDictionary *opt_arg);
        AVFrame *alloc_picture(enum AVPixelFormat pix_fmt, int width, int height);
        AVFrame *alloc_audio_frame(enum AVSampleFormat sample_fmt,
                                   const AVChannelLayout *channel_layout,
                                   int sample_rate, int nb_samples);
        void open_audio(AVFormatContext *oc, const AVCodec *codec,
                        OutputStream *ost, AVDictionary *opt_arg);
        OutputStream video_st = { 0 }, audio_st = { 0 };

        int write_video_frame(AVFormatContext *oc, OutputStream *ost);

        int write_frame(AVFormatContext *fmt_ctx, AVCodecContext *c,
                        AVStream *st, AVFrame *frame, AVPacket *pkt);

        int write_audio_frame(AVFormatContext *oc, OutputStream *ost);


        void close_stream(AVFormatContext *oc, OutputStream *ost);




    };
}


#endif //FMPLAYER_FMENCODER_H
