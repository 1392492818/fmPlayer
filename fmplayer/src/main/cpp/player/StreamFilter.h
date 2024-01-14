//
// Created by fuweicong on 2023/12/13.
//

#ifndef QTANDROID_H264_H
#define QTANDROID_H264_H

#include "StreamInfo.h"


#ifndef AV_RB16
#   define AV_RB16(x)                           \
    ((((const uint8_t*)(x))[0] << 8) |          \
      ((const uint8_t*)(x))[1])
#endif


#ifndef AV_WB32
#   define AV_WB32(p, val) do {                 \
        uint32_t d = (val);                     \
        ((uint8_t*)(p))[3] = (d);               \
        ((uint8_t*)(p))[2] = (d)>>8;            \
        ((uint8_t*)(p))[1] = (d)>>16;           \
        ((uint8_t*)(p))[0] = (d)>>24;           \
    } while(0)
#endif

namespace fm {

    class StreamFilter {
    public:
        static int open_bitstream_filter(AVStream *stream, AVBSFContext **bsf_ctx, const char *name) {
            int ret = 0;
            const AVBitStreamFilter *filter = av_bsf_get_by_name(name);
            if (!filter) {
                ret = -1;
                fprintf(stderr, "Unknow bitstream filter.\n");
            }
            if((ret = av_bsf_alloc(filter, bsf_ctx) < 0)) {
                fprintf(stderr, "av_bsf_alloc failed\n");
                return ret;
            }
            if ((ret = avcodec_parameters_copy((*bsf_ctx)->par_in, stream->codecpar)) < 0) {
                fprintf(stderr, "avcodec_parameters_copy failed, ret=%d\n", ret);
                return ret;
            }
            if ((ret = av_bsf_init(*bsf_ctx)) < 0) {
                fprintf(stderr, "av_bsf_init failed, ret=%d\n", ret);
                return ret;
            }
            return ret;
        }

        static int filter_stream(AVBSFContext *bsf_ctx, AVPacket *pkt, AVPacket** outPkt, int eof) {
            int ret = 0;
            if (ret = av_bsf_send_packet(bsf_ctx, eof ? NULL : pkt) < 0) {
                fprintf(stderr, "av_bsf_send_packet failed, ret=%d\n", ret);
                return ret;
            }
            while ((ret = av_bsf_receive_packet(bsf_ctx, *outPkt) == 0)) {
                if (ret < 0) return ret;
            }
            if (ret == AVERROR(EAGAIN)) return 0;
            return ret;
        }

    };

}
#endif //QTANDROID_H264_H
