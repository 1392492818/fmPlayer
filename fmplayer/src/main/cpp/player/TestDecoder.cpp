//
// Created by fuweicong on 2023/11/28.
//

#include <thread>
#include <QElapsedTimer>
#include "TestDecoder.h"

int TestDecoder::start(char *input) {
    this->input = input;
    auto videoRender = std::bind(&TestDecoder::decoder, this);
    std::thread video(videoRender);
    video.detach();
    return 1;

}

void TestDecoder::setCallAvFrame(CallAVFrame *callAvFrame) {
    TestDecoder::callAvFrame = callAvFrame;
}

void TestDecoder::decoder() {
    // 打开输入文件
    AVFormatContext *format_ctx = NULL;
    if (avformat_open_input(&format_ctx, "F:/jianpianDownload/西出玉门第36集超前版.mp4", NULL, NULL) != 0) {
        fprintf(stderr, "Error opening input file\n");
        return;
    }

    // 获取视频流信息
    if (avformat_find_stream_info(format_ctx, NULL) < 0) {
        fprintf(stderr, "Error finding stream information\n");
        avformat_close_input(&format_ctx);
        return;
    }

    // 寻找视频流
    int video_stream_index = -1;
    for (int i = 0; i < format_ctx->nb_streams; i++) {
        if (format_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_stream_index = i;
            break;
        }
    }

    if (video_stream_index == -1) {
        fprintf(stderr, "No video stream found in the input file\n");
        avformat_close_input(&format_ctx);
        return;
    }

    // 获取视频解码器
    const AVCodec *codec = avcodec_find_decoder(format_ctx->streams[video_stream_index]->codecpar->codec_id);
    if (!codec) {
        fprintf(stderr, "Unsupported codec\n");
        avformat_close_input(&format_ctx);
        return;
    }

    // 初始化解码器上下文
    AVCodecContext *codec_ctx = avcodec_alloc_context3(codec);
    if (!codec_ctx) {
        fprintf(stderr, "Failed to allocate codec context\n");
        avformat_close_input(&format_ctx);
        return;
    }

    if (avcodec_parameters_to_context(codec_ctx, format_ctx->streams[video_stream_index]->codecpar) < 0) {
        fprintf(stderr, "Failed to copy codec parameters to codec context\n");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&format_ctx);
        return;
    }
    AVDictionary *opts = NULL;
    AVRational new_time_base = {1, 30};  // 假设设置为30帧/秒
    codec_ctx->time_base = new_time_base;
//    codec_ctx->thread_count = 4;  // 设置解码器使用的线程数量
    if (!av_dict_get(opts, "threads", NULL, 0))
        av_dict_set(&opts, "threads", "auto", 0);
    av_dict_set(&opts, "flags", "+copy_opaque", AV_DICT_MULTIKEY);

    if (avcodec_open2(codec_ctx, codec, &opts) < 0) {
        fprintf(stderr, "Failed to open codec\n");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&format_ctx);
        return;
    }

    // 读取帧并解码
    AVPacket packet;
    av_init_packet(&packet);

    AVFrame *frame = av_frame_alloc();
    AVFrame *yuv420Frame = av_frame_alloc();
    if (!frame) {
        fprintf(stderr, "Failed to allocate frame\n");
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&format_ctx);
        return;
    }
    SwsContext *swsContext = sws_getContext(codec_ctx->width, codec_ctx->height,
                                            codec_ctx->pix_fmt,
//                                                            AV_PIX_FMT_NV12,
                                            codec_ctx->width, codec_ctx->height,
                                            AV_PIX_FMT_YUV420P, SWS_FAST_BILINEAR, 0, 0, 0);

    yuv420Frame->width = codec_ctx->width;
    yuv420Frame->height = codec_ctx->height;
    yuv420Frame->format = AV_PIX_FMT_YUV420P;
    av_image_alloc(yuv420Frame->data, yuv420Frame->linesize, codec_ctx->width, codec_ctx->height,
                   AV_PIX_FMT_YUV420P,
                   1);
    qint64 count = 0;
    int skip_frames = 3;  // 跳过两帧
    int frame_count = 0;
    while (av_read_frame(format_ctx, &packet) >= 0) {
        if (packet.stream_index == video_stream_index) {
            // 发送数据到解码器
            QElapsedTimer timer;

            // 启动计时器
            timer.start();
            if (avcodec_send_packet(codec_ctx, &packet) < 0) {
                fprintf(stderr, "Error sending packet to decoder\n");
                break;
            }

            // 接收解码后的帧
            while (avcodec_receive_frame(codec_ctx, frame) == 0) {
                // 在这里处理解码后的帧，可以进行显示、保存等操作

                // 例如，将 YUV420P 格式的帧保存为 ppm 图像
                if (frame->format == AV_PIX_FMT_YUV420P) {
                    if (frame_count % skip_frames == 0) {

                        sws_scale(swsContext, frame->data, frame->linesize, 0, codec_ctx->height,
                                  yuv420Frame->data, yuv420Frame->linesize);
                        if (this->callAvFrame != nullptr) this->callAvFrame->onVideoFrame(yuv420Frame);
                        std::this_thread::sleep_for(std::chrono::milliseconds(1000 / 119));
                    }
                    frame_count++;
                }
            }
            qint64 elapsed = timer.elapsed();
            count += elapsed;
            qDebug() << "Time elapsed:" << count << "milliseconds" << ", elapsed :" << elapsed;
        }

        av_packet_unref(&packet);
    }

    // 清理资源
    av_frame_free(&frame);
    avcodec_free_context(&codec_ctx);
    avformat_close_input(&format_ctx);
    qDebug() << "结束";

}
