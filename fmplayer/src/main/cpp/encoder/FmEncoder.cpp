//
// Created by fuweicong on 2023/12/10.
//

#include "FmEncoder.h"

fm::FmEncoder::FmEncoder(const char *outputPath, int width, int height, AVPixelFormat format) {
    this->output = outputPath;
    this->width = width;
    this->height = height;
    this->format = format;
}

// 音频编码
AVFrame *fm::FmEncoder::alloc_audio_frame(enum AVSampleFormat sample_fmt,
                                          const AVChannelLayout *channel_layout,
                                          int sample_rate, int nb_samples) {
    AVFrame *frame = av_frame_alloc();
    if (!frame) {
        fprintf(stderr, "Error allocating an audio frame\n");
        exit(1);
    }

    frame->format = sample_fmt;
    av_channel_layout_copy(&frame->ch_layout, channel_layout);
    frame->sample_rate = sample_rate;
    frame->nb_samples = nb_samples;

    if (nb_samples) {
        if (av_frame_get_buffer(frame, 0) < 0) {
            fprintf(stderr, "Error allocating an audio buffer\n");
            exit(1);
        }
    }

    return frame;
}

void fm::FmEncoder::open_audio(AVFormatContext *oc, const AVCodec *codec,
                               OutputStream *ost, AVDictionary *opt_arg) {
    AVCodecContext *c;
    int nb_samples;
    int ret;
    AVDictionary *opt = NULL;

    c = ost->enc;

    /* open it */
    av_dict_copy(&opt, opt_arg, 0);
    ret = avcodec_open2(c, codec, &opt);
    av_dict_free(&opt);
    if (ret < 0) {
        fprintf(stderr, "Could not open audio codec: %s\n", av_err2str(ret));
        exit(1);
    }

    /* init signal generator */
    ost->t = 0;
    ost->tincr = 2 * M_PI * 110.0 / c->sample_rate;
    /* increment frequency by 110 Hz per second */
    ost->tincr2 = 2 * M_PI * 110.0 / c->sample_rate / c->sample_rate;

    if (c->codec->capabilities & AV_CODEC_CAP_VARIABLE_FRAME_SIZE)
        nb_samples = 10000;
    else
        nb_samples = c->frame_size;

    ost->frame = alloc_audio_frame(c->sample_fmt, &c->ch_layout,
                                   c->sample_rate, nb_samples);
    ost->tmp_frame = alloc_audio_frame(AV_SAMPLE_FMT_S16, &c->ch_layout,
                                       c->sample_rate, nb_samples);

    /* copy the stream parameters to the muxer */
    ret = avcodec_parameters_from_context(ost->st->codecpar, c);
    if (ret < 0) {
        fprintf(stderr, "Could not copy the stream parameters\n");
        exit(1);
    }

    /* create resampler context */
    ost->swr_ctx = swr_alloc();
    if (!ost->swr_ctx) {
        fprintf(stderr, "Could not allocate resampler context\n");
        exit(1);
    }

    /* set options */
    av_opt_set_chlayout(ost->swr_ctx, "in_chlayout", &c->ch_layout, 0);
    av_opt_set_int(ost->swr_ctx, "in_sample_rate", c->sample_rate, 0);
    av_opt_set_sample_fmt(ost->swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_S16, 0);
    av_opt_set_chlayout(ost->swr_ctx, "out_chlayout", &c->ch_layout, 0);
    av_opt_set_int(ost->swr_ctx, "out_sample_rate", c->sample_rate, 0);
    av_opt_set_sample_fmt(ost->swr_ctx, "out_sample_fmt", c->sample_fmt, 0);

    /* initialize the resampling context */
    if ((ret = swr_init(ost->swr_ctx)) < 0) {
        fprintf(stderr, "Failed to initialize the resampling context\n");
        exit(1);
    }
}

// 音频编码 END
// 视频编码代码
AVFrame *fm::FmEncoder::alloc_picture(enum AVPixelFormat pix_fmt, int width, int height) {
    AVFrame *picture;
    int ret;

    picture = av_frame_alloc();
    if (!picture)
        return NULL;

    picture->format = pix_fmt;
    picture->width = width;
    picture->height = height;

    /* allocate the buffers for the frame data */
    ret = av_frame_get_buffer(picture, 0);
    if (ret < 0) {
        fprintf(stderr, "Could not allocate frame data.\n");
        exit(1);
    }

    return picture;
}

void fm::FmEncoder::open_video(AVFormatContext *oc, const AVCodec *codec,
                               OutputStream *ost, AVDictionary *opt_arg) {
    int ret;
    AVCodecContext *c = ost->enc;
    AVDictionary *opt = NULL;

    av_dict_copy(&opt, opt_arg, 0);

    /* open the codec */
    ret = avcodec_open2(c, codec, &opt);
    av_dict_free(&opt);
    if (ret < 0) {
        fprintf(stderr, "Could not open video codec: %s\n", av_err2str(ret));
        exit(1);
    }

    /* allocate and init a re-usable frame */
    ost->frame = alloc_picture(c->pix_fmt, c->width, c->height);
    if (!ost->frame) {
        fprintf(stderr, "Could not allocate video frame\n");
        exit(1);
    }

    /* If the output format is not YUV420P, then a temporary YUV420P
     * picture is needed too. It is then converted to the required
     * output format. */
    ost->tmp_frame = NULL;
//    if (c->pix_fmt != AV_PIX_FMT_YUV420P) {
        ost->tmp_frame = alloc_picture(this->format, c->width, c->height);
        if (!ost->tmp_frame) {
            fprintf(stderr, "Could not allocate temporary picture\n");
            exit(1);
        }
//    }

    /* copy the stream parameters to the muxer */
    ret = avcodec_parameters_from_context(ost->st->codecpar, c);
    if (ret < 0) {
        fprintf(stderr, "Could not copy the stream parameters\n");
        exit(1);
    }
}
// 视频编码END




/**
 * 添加输出流信息
 * @param ost
 * @param oc
 * @param codec
 * @param codec_id
 */
void fm::FmEncoder::add_stream(OutputStream *ost, AVFormatContext *oc,
                               const AVCodec **codec,
                               enum AVCodecID codec_id) {
    AVCodecContext *c;
    int i;

    /* find the encoder */

//    *codec = avcodec_find_encoder(codec_id);
    *codec = avcodec_find_encoder_by_name("libx264");

    if (!(*codec)) {
        fprintf(stderr, "Could not find encoder for '%s'\n",
                avcodec_get_name(codec_id));
        exit(1);
    }

    ost->tmp_pkt = av_packet_alloc();
    if (!ost->tmp_pkt) {
        fprintf(stderr, "Could not allocate AVPacket\n");
        exit(1);
    }

    ost->st = avformat_new_stream(oc, NULL);
    if (!ost->st) {
        fprintf(stderr, "Could not allocate stream\n");
        exit(1);
    }
    ost->st->id = oc->nb_streams - 1;
    c = avcodec_alloc_context3(*codec);
    if (!c) {
        fprintf(stderr, "Could not alloc an encoding context\n");
        exit(1);
    }
    ost->enc = c;
    AVChannelLayout avChannelLayout = AV_CHANNEL_LAYOUT_STEREO;

    switch ((*codec)->type) {
        case AVMEDIA_TYPE_AUDIO:
            c->sample_fmt = (*codec)->sample_fmts ?
                            (*codec)->sample_fmts[0] : AV_SAMPLE_FMT_FLTP;
            c->bit_rate = 64000;
            c->sample_rate = 44100;
            if ((*codec)->supported_samplerates) {
                c->sample_rate = (*codec)->supported_samplerates[0];
                for (i = 0; (*codec)->supported_samplerates[i]; i++) {
                    if ((*codec)->supported_samplerates[i] == 44100)
                        c->sample_rate = 44100;
                }
            }
            av_channel_layout_copy(&c->ch_layout, &avChannelLayout);
            ost->st->time_base = (AVRational) {1, c->sample_rate};
            break;
        case AVMEDIA_TYPE_VIDEO:
            c->codec_id = codec_id;

            c->bit_rate = 400000;
            /* Resolution must be a multiple of two. */
            c->width = this->width;
            c->height = this->height;
            /* timebase: This is the fundamental unit of time (in seconds) in terms
             * of which frame timestamps are represented. For fixed-fps content,
             * timebase should be 1/framerate and timestamp increments should be
             * identical to 1. */
            ost->st->time_base = (AVRational) {1, STREAM_FRAME_RATE};
            c->time_base = ost->st->time_base;

            c->gop_size = 12; /* emit one intra frame every twelve frames at most */
            c->pix_fmt = STREAM_PIX_FMT;
            if (c->codec_id == AV_CODEC_ID_MPEG2VIDEO) {
                /* just for testing, we also add B-frames */
                c->max_b_frames = 2;
            }
            if (c->codec_id == AV_CODEC_ID_MPEG1VIDEO) {
                /* Needed to avoid using macroblocks in which some coeffs overflow.
                 * This does not happen with normal video, it just happens here as
                 * the motion of the chroma plane does not match the luma plane. */
                c->mb_decision = 2;
            }
            break;

        default:
            break;
    }

    /* Some formats want stream headers to be separate. */
    if (oc->oformat->flags & AVFMT_GLOBALHEADER)
        c->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
}


int fm::FmEncoder::write_frame(AVFormatContext *fmt_ctx, AVCodecContext *c,
                               AVStream *st, AVFrame *frame, AVPacket *pkt) {
    int ret;

    // send the frame to the encoder
    ret = avcodec_send_frame(c, frame);
    if (ret < 0) {
        fprintf(stderr, "Error sending a frame to the encoder: %s\n",
                av_err2str(ret));
        exit(1);
    }

    while (ret >= 0) {
        ret = avcodec_receive_packet(c, pkt);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            break;
        else if (ret < 0) {
            fprintf(stderr, "Error encoding a frame: %s\n", av_err2str(ret));
            exit(1);
        }

        /* rescale output packet timestamp values from codec to stream timebase */
        av_packet_rescale_ts(pkt, c->time_base, st->time_base);
        pkt->stream_index = st->index;

        /* Write the compressed frame to the media file. */
        //log_packet(fmt_ctx, pkt);
        ret = av_interleaved_write_frame(fmt_ctx, pkt);
        /* pkt is now blank (av_interleaved_write_frame() takes ownership of
         * its contents and resets pkt), so that no unreferencing is necessary.
         * This would be different if one used av_write_frame(). */
        if (ret < 0) {
            fprintf(stderr, "Error while writing output packet: %s\n", av_err2str(ret));
            exit(1);
        }
    }

    return ret == AVERROR_EOF ? 1 : 0;
}


int fm::FmEncoder::write_video_frame(AVFormatContext *oc, OutputStream *ost) {
//    return write_frame(oc, ost->enc, ost->st, get_video_frame(ost), ost->tmp_pkt);
    return 0;
}

int fm::FmEncoder::write_audio_frame(AVFormatContext *oc, OutputStream *ost) {
    AVCodecContext *c;
    AVFrame *frame;
    int ret;
    int dst_nb_samples;

    c = ost->enc;
    return 0;
//    frame = get_audio_frame(ost);
//
//    if (frame) {
//        /* convert samples from native format to destination codec format, using the resampler */
//        /* compute destination number of samples */
//        dst_nb_samples = av_rescale_rnd(swr_get_delay(ost->swr_ctx, c->sample_rate) + frame->nb_samples,
//                                        c->sample_rate, c->sample_rate, AV_ROUND_UP);
////        av_assert0(dst_nb_samples == frame->nb_samples);
//
//        /* when we pass a frame to the encoder, it may keep a reference to it
//         * internally;
//         * make sure we do not overwrite it here
//         */
//        ret = av_frame_make_writable(ost->frame);
//        if (ret < 0)
//            exit(1);
//
//        /* convert to destination format */
//        ret = swr_convert(ost->swr_ctx,
//                          ost->frame->data, dst_nb_samples,
//                          (const uint8_t **)frame->data, frame->nb_samples);
//        if (ret < 0) {
//            fprintf(stderr, "Error while converting\n");
//            exit(1);
//        }
//        frame = ost->frame;
//
//        frame->pts = av_rescale_q(ost->samples_count, (AVRational){1, c->sample_rate}, c->time_base);
//        ost->samples_count += dst_nb_samples;
//    }
//
//    return write_frame(oc, c, ost->st, frame, ost->tmp_pkt);
}

void fm::FmEncoder::close_stream(AVFormatContext *oc, OutputStream *ost) {
    avcodec_free_context(&ost->enc);
    av_frame_free(&ost->frame);
    av_frame_free(&ost->tmp_frame);
    av_packet_free(&ost->tmp_pkt);
    sws_freeContext(ost->sws_ctx);
    swr_free(&ost->swr_ctx);
}


bool fm::FmEncoder::init() {
    av_dict_set(&opt, "author", "fm", 0);

    avformat_alloc_output_context2(&oc, NULL, "flv", output);
    fmt = oc->oformat;


    /* Add the audio and video streams using the default format codecs
    * and initialize the codecs. */
    if (fmt->video_codec != AV_CODEC_ID_NONE) {
//        add_stream(&video_st, oc, &video_codec, fmt->video_codec);
        add_stream(&video_st, oc, &video_codec, AV_CODEC_ID_H264);

        have_video = 1;
        encode_video = 1;
    }
//    if (fmt->audio_codec != AV_CODEC_ID_NONE) {
//        add_stream(&audio_st, oc, &audio_codec, fmt->audio_codec);
//        have_audio = 1;
//        encode_audio = 1;
//    }


    /* Now that all the parameters are set, we can open the audio and
     * video codecs and allocate the necessary encode buffers. */
    if (have_video)
        open_video(oc, video_codec, &video_st, opt);

    if (have_audio)
        open_audio(oc, audio_codec, &audio_st, opt);

    av_dump_format(oc, 0, this->output, 1);

    /* open the output file, if needed */
    if (!(fmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&oc->pb, this->output, AVIO_FLAG_WRITE);
        if (ret < 0) {
            fprintf(stderr, "Could not open '%s': %s\n", this->output,
                    av_err2str(ret));
            return 1;
        }
    }

    /* Write the stream header, if any. */
    ret = avformat_write_header(oc, nullptr);
    if (ret < 0) {
        fprintf(stderr, "Error occurred when opening output file: %s\n",
                av_err2str(ret));
        return 1;
    }
    return 0;

//    while (encode_video || encode_audio) {
//        /* select the stream to encode */
//        if (encode_video &&
//            (!encode_audio || av_compare_ts(video_st.next_pts, video_st.enc->time_base,
//                                            audio_st.next_pts, audio_st.enc->time_base) <= 0)) {
//            encode_video = !write_video_frame(oc, &video_st);
//        } else {
//            encode_audio = !write_audio_frame(oc, &audio_st);
//        }
//    }



}

void fm::FmEncoder::end() {
    av_write_trailer(oc);

    /* Close each codec. */
    if (have_video)
        close_stream(oc, &video_st);
    if (have_audio)
        close_stream(oc, &audio_st);

    if (!(fmt->flags & AVFMT_NOFILE))
        /* Close the output file. */
        avio_closep(&oc->pb);

    /* free the stream */
    avformat_free_context(oc);
}

int fm::FmEncoder::add_video_frame(char *data, int width, int height, int format) {
    OutputStream *ost = &video_st;
    AVCodecContext *c = ost->enc;

    /* check if we want to generate more frames */
//    if (av_compare_ts(ost->next_pts, c->time_base,
//                      STREAM_DURATION, (AVRational) {1, 1}) > 0)
//        return 0;

    /* when we pass a frame to the encoder, it may keep a reference to it
     * internally; make sure we do not overwrite it here */
    if (av_frame_make_writable(ost->frame) < 0)
        return -1;

    /* as we only generate a YUV420P picture, we must convert it
     * to the codec pixel format if needed */
    if (!ost->sws_ctx) {
        ost->sws_ctx = sws_getContext(width, height,
                                      this->format,
                                      c->width, c->height,
                                      c->pix_fmt,
                                      SCALE_FLAGS, NULL, NULL, NULL);
        if (!ost->sws_ctx) {
            fprintf(stderr,
                    "Could not initialize the conversion context\n");
            return -1;
        }
    }

    av_image_fill_arrays(ost->tmp_frame->data, ost->tmp_frame->linesize,
                         reinterpret_cast<const uint8_t *>(data),
                         this->format, this->width, this->height, 1);

    sws_scale(ost->sws_ctx, (const uint8_t *const *) ost->tmp_frame->data,
              ost->tmp_frame->linesize, 0, c->height, ost->frame->data,
              ost->frame->linesize);


    ost->frame->pts = ost->next_pts++;

    return write_frame(oc, ost->enc, ost->st, ost->frame, ost->tmp_pkt);
}

int fm::FmEncoder::add_audio_frame(char *data) {
    return !write_audio_frame(oc, &audio_st);
}





