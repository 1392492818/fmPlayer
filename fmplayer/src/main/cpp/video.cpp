//
// Created by fuweicong on 2021/4/18.
//

#include <jni.h>
#include "util.h"
#include "player/FmPlayer.h"
#include "encoder/FmEncoder.h"
#include "VideoPlayer.h"
#include "ThreadSafeQueue.h"
#include "VideoPlayerManager.h"
#include <android/bitmap.h>
#include <unordered_map>


JavaVM *g_VM;
std::unordered_map<std::string, FmPlayerStruct> playerHashMap;
std::mutex player; // 保证队列进出不影响
std::mutex encoder;
std::unique_ptr<VideoPlayerManager> videoPlayerManager = std::make_unique<VideoPlayerManager>();
fm::FmEncoder *fmEncoder = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    g_VM = vm;  // 保存 JavaVM 指针到全局变量
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        // 获取 JNIEnv 失败
        return JNI_ERR;
    }

    // 在这里进行其他初始化工作

    return JNI_VERSION_1_6;  // 返回 JNI 版本
}


extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_startPlayer(JNIEnv *env, jobject clazz,
                                          jstring url, jobject callbackObj,
                                          jstring id, jlong time, jstring cachePath) {
    // TODO: implement startPlayer()
    const char *urlString = env->GetStringUTFChars(url, JNI_FALSE);
    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    const char *cstr = env->GetStringUTFChars(cachePath, nullptr);

    // 数据回调给 java渲染
    jobject globalRef = env->NewGlobalRef(callbackObj);
    std::string idString(nativeString);
    std::string cacheString(cstr);

    videoPlayerManager->startPlayer(g_VM, env, globalRef, idString, urlString, time, cacheString);

    env->ReleaseStringUTFChars(cachePath, cstr);
    env->ReleaseStringUTFChars(url, urlString);
    env->ReleaseStringUTFChars(id, nativeString);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_stop(JNIEnv *env, jobject clazz, jstring id) {

    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    // 将 const char* 转换为 std::string
    std::string idString(nativeString);
    // 释放 jstring 获取的字符串
    videoPlayerManager->stop(idString);
    env->ReleaseStringUTFChars(id, nativeString);

//    FmPlayerStruct *fmPlayerStruct = nullptr;
//    {
//        std::lock_guard<std::mutex> lockGuard(player);
//
//        const char *nativeString = env->GetStringUTFChars(id, nullptr);
//        // 将 const char* 转换为 std::string
//        std::string idString(nativeString);
//        // 释放 jstring 获取的字符串
//        env->ReleaseStringUTFChars(id, nativeString);
//        if (playerHashMap.find(idString) != playerHashMap.end()) {
//            fmPlayerStruct = &playerHashMap[idString];
//            LOGE("删除 %s", idString.data());
////            playerHashMap.erase(idString);
//            // env停止全局变量线程
//            if (fmPlayerStruct != nullptr) {
//                fmPlayerStruct->fmPlayer->stop();
////                if(fmPlayerStruct->globalObject != nullptr) {
////                    LOGE("不为null 删除");
////                    jboolean isGlobalRefValid = env->IsSameObject(fmPlayerStruct->globalObject, NULL);
////                    if (isGlobalRefValid == JNI_FALSE) {
////                        LOGE("全局引用有效");
////                        LOGE("删除 global");
////                        env->DeleteGlobalRef(fmPlayerStruct->globalObject);
////                    } else {
////                        LOGE("全局引用无效");
////                    }
////                }
////
////                if (fmPlayerStruct->videoPlayer != nullptr) {
////                    LOGE("删除 videoPlayer");
////                    delete fmPlayerStruct->videoPlayer;
////                }
//                LOGE("停止了吗");
//            }
//        }
//        LOGE("size %d ", playerHashMap.size());
//    }
//
////    env->DeleteGlobalRef(fmPlayer->gDecoderObject);
//    LOGE("停止");
}





extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_play(JNIEnv *env, jobject thiz, jstring id) {
    // TODO: implement play()

    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    // 将 const char* 转换为 std::string
    std::string idString(nativeString);
    // 释放 jstring 获取的字符串
    videoPlayerManager->play(idString);
    env->ReleaseStringUTFChars(id, nativeString);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_pause(JNIEnv *env, jobject thiz, jstring id) {
    // TODO: implement pause()

    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    // 将 const char* 转换为 std::string
    std::string idString(nativeString);
    videoPlayerManager->pause(idString);
    // 释放 jstring 获取的字符串
    env->ReleaseStringUTFChars(id, nativeString);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_seek(JNIEnv *env, jobject thiz, jstring id, jlong time) {
    // TODO: implement seek()

    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    // 将 const char* 转换为 std::string
    std::string idString(nativeString);
    // 释放 jstring 获取的字符串
    videoPlayerManager->seek(idString, time);
    env->ReleaseStringUTFChars(id, nativeString);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_setSpeed(JNIEnv *env, jobject thiz, jstring id, jfloat speed) {
    // TODO: implement setSpeed()

    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    // 将 const char* 转换为 std::string
    std::string idString(nativeString);
    // 释放 jstring 获取的字符串
    videoPlayerManager->speed(idString, speed);
    env->ReleaseStringUTFChars(id, nativeString);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_encoder_FmEncoder_encoder(JNIEnv *env, jclass thiz, jstring path, jint width,
                                               jint height,
                                               jint format, jint rotate, jint sample_rate,
                                               jint channel) {
    // TODO: implement encoder()

    std::lock_guard<std::mutex> lockGuard(encoder);
    const char *str = env->GetStringUTFChars(path, NULL);

    fmEncoder = new fm::FmEncoder(str,
                                  width,
                                  height, static_cast<AVPixelFormat>(format), rotate, sample_rate,
                                  channel);
    env->ReleaseStringUTFChars(path, str);

    fmEncoder->init();
}


extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_encoder_FmEncoder_addVideoFrame(JNIEnv *env, jclass thiz, jbyteArray data,
                                                     jlong seconds) {
    // TODO: implement addVideoFrame()
    {
        std::lock_guard<std::mutex> lockGuard(encoder);
        if (fmEncoder == nullptr) {
            return;
            LOGE("视频null");
        }
    }
    jsize len = env->GetArrayLength(data);
    jbyte *bytes = env->GetByteArrayElements(data, NULL);
    fmEncoder->add_video_frame(reinterpret_cast<char *>(bytes), len, seconds);

    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_encoder_FmEncoder_addAudioFrame(JNIEnv *env, jclass thiz, jbyteArray data,
                                                     jlong seconds) {
    // TODO: implement addAudioFrame()

    {
        std::lock_guard<std::mutex> lockGuard(encoder);
        if (fmEncoder == nullptr) {
            LOGE("音频null");
            return;
        }
    }
    jsize len = env->GetArrayLength(data);
    jbyte *bytes = env->GetByteArrayElements(data, NULL);

    fmEncoder->add_audio_frame(reinterpret_cast<char * >(bytes), len, seconds);
    env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_encoder_FmEncoder_stopEncoder(JNIEnv *env, jclass thiz) {
    // TODO: implement endCoder()
    std::lock_guard<std::mutex> lockGuard(encoder);
    LOGE("结束");
    if (fmEncoder != nullptr) {
        fmEncoder->end();
    } else {
        LOGE("encoder 为 null");
    }
    fmEncoder = nullptr;
}