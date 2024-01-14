//
// Created by fuweicong on 2023/12/14.
//

#ifndef FMPLAYER_MEDIACODECDECODER_H
#define FMPLAYER_MEDIACODECDECODER_H
#ifdef FM_ANDROID

#include <jni.h>
#include <queue>
#include "../util.h"
namespace fm {
    class MediaCodecDecoder {
    public:
        MediaCodecDecoder(JavaVM *g_vm, jobject);
        bool init(int width, int heightj, int format,char* sps, int spsLength, char* pps,int ppslength);
        int getMediaFormat();
        std::queue<char*> onFrame(char* data,int size);
        ~MediaCodecDecoder();
    private:
//        JNIEnv *env;
        char* data = nullptr;
        JavaVM *g_vm;
        jobject decoderObject;

    };
}
#endif


#endif //FMPLAYER_MEDIACODECDECODER_H
