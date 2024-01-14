//
// Created by fuweicong on 2021/4/18.
//

#include <jni.h>
#include "util.h"
#include "player/FmPlayer.h"
#include "encoder/FmEncoder.h"
#include "VideoPlayer.h"
#include "ThreadSafeQueue.h"
#include <android/bitmap.h>
#include <unordered_map>


typedef struct {
    fm::FmPlayer *fmPlayer;
    jobject globalObject;
    VideoPlayer *videoPlayer;
} FmPlayerStruct;
JavaVM *g_VM;
std::unordered_map<std::string, FmPlayerStruct> playerHashMap;
std::mutex player; // 保证队列进出不影响
std::mutex encoder;

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
                                                       jstring id) {
    // TODO: implement startPlayer()
    const char *urlString = env->GetStringUTFChars(url, JNI_FALSE);

    // 数据回调给 java渲染
    jobject globalRef = env->NewGlobalRef(callbackObj);

    // Do something with the native string (char*)
    // Don't forget to release the native string to avoid memory leaks
    fm::FmPlayer *fmPlayer = new fm::FmPlayer();
    VideoPlayer *videoPlayer = new VideoPlayer(g_VM, env, globalRef);

    const char *nativeString = env->GetStringUTFChars(id, nullptr);
    // 将 const char* 转换为 std::string
    std::string idString(nativeString);
    // 释放 jstring 获取的字符串
    env->ReleaseStringUTFChars(id, nativeString);

    {
        std::lock_guard<std::mutex> lockGuard(player);
        FmPlayerStruct playerStruct;
        playerStruct.videoPlayer = videoPlayer;
        playerStruct.globalObject = globalRef;
        playerStruct.fmPlayer = fmPlayer;

        playerHashMap[idString] = playerStruct;
        LOGE("插入id %s %d", idString.data(), playerHashMap.size());

    }
    fmPlayer->setCallAvFrame(videoPlayer);
    fmPlayer->startPlayer(urlString);
    fmPlayer->start();
    env->ReleaseStringUTFChars(url, urlString);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_stop(JNIEnv *env, jobject clazz, jstring id) {
    FmPlayerStruct *fmPlayerStruct = nullptr;
    {
        std::lock_guard<std::mutex> lockGuard(player);

        const char *nativeString = env->GetStringUTFChars(id, nullptr);
        // 将 const char* 转换为 std::string
        std::string idString(nativeString);
        // 释放 jstring 获取的字符串
        env->ReleaseStringUTFChars(id, nativeString);
        if (playerHashMap.find(idString) != playerHashMap.end()) {
            fmPlayerStruct = &playerHashMap[idString];
            LOGE("删除 %s", idString.data());
//            playerHashMap.erase(idString);
            // env停止全局变量线程
            if (fmPlayerStruct != nullptr) {
                fmPlayerStruct->fmPlayer->stop();
//                if(fmPlayerStruct->globalObject != nullptr) {
//                    LOGE("不为null 删除");
//                    jboolean isGlobalRefValid = env->IsSameObject(fmPlayerStruct->globalObject, NULL);
//                    if (isGlobalRefValid == JNI_FALSE) {
//                        LOGE("全局引用有效");
//                        LOGE("删除 global");
//                        env->DeleteGlobalRef(fmPlayerStruct->globalObject);
//                    } else {
//                        LOGE("全局引用无效");
//                    }
//                }
//
//                if (fmPlayerStruct->videoPlayer != nullptr) {
//                    LOGE("删除 videoPlayer");
//                    delete fmPlayerStruct->videoPlayer;
//                }
                LOGE("停止了吗");
            }
        }
        LOGE("size %d ", playerHashMap.size());
    }

//    env->DeleteGlobalRef(fmPlayer->gDecoderObject);
    LOGE("停止");
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_com_marvel_fmPlayer_fragment_camera_CameraFragment_addVideoFrame(JNIEnv *env, jobject clazz,
                                                                      jobject bitmap) {

    std::lock_guard<std::mutex> lockGuard(encoder);

    AndroidBitmapInfo info;
    void *pixels;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        // 处理获取信息失败的情况
        return false;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        // 处理锁定像素失败的情况
        return false;
    }

    {
        if (fmEncoder == nullptr) {
            AVPixelFormat avPixelFormat = AV_PIX_FMT_RGBA;
            if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
                avPixelFormat = AV_PIX_FMT_RGBA;
            } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
                avPixelFormat = AV_PIX_FMT_RGB565BE;
            } else {
                return false;
            }
//            fmEncoder = new fm::FmEncoder("/data/data/com.marvel.fmPlayer/files/test.mp4",
//                                          info.width,
//                                          info.height, avPixelFormat);
            fmEncoder = new fm::FmEncoder("rtmp://webrtc.duxingzhe.top:1935/live/fmtest",
                                          info.width,
                                          info.height, avPixelFormat);
            fmEncoder->init();
        }
    }


    if (fmEncoder != nullptr) {
        fmEncoder->add_video_frame(static_cast<char *>(pixels), info.width, info.height,
                                   info.format);
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return true;
    // TODO: implement addVideoFrame()
}
extern "C"
JNIEXPORT void JNICALL
Java_com_marvel_fmPlayer_fragment_camera_CameraFragment_stopEncoder(JNIEnv *env, jobject clazz) {
    // TODO: implement stopEncoder()
    std::lock_guard<std::mutex> lockGuard(encoder);
    if (fmEncoder != nullptr) fmEncoder->end();
    fmEncoder = nullptr;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_play(JNIEnv *env, jobject thiz, jstring id) {
    // TODO: implement play()
    FmPlayerStruct *fmPlayerStruct = nullptr;
    {
        std::lock_guard<std::mutex> lockGuard(player);

        const char *nativeString = env->GetStringUTFChars(id, nullptr);
        // 将 const char* 转换为 std::string
        std::string idString(nativeString);
        // 释放 jstring 获取的字符串
        env->ReleaseStringUTFChars(id, nativeString);
        if (playerHashMap.find(idString) != playerHashMap.end()) {
            fmPlayerStruct = &playerHashMap[idString];
            LOGE("播放 %s", idString.data());
            fmPlayerStruct->fmPlayer->play();
        } else {
            LOGE("没有找到");
        }

    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_pause(JNIEnv *env, jobject thiz, jstring id) {
    // TODO: implement pause()
    FmPlayerStruct *fmPlayerStruct = nullptr;
    {
        std::lock_guard<std::mutex> lockGuard(player);

        const char *nativeString = env->GetStringUTFChars(id, nullptr);
        // 将 const char* 转换为 std::string
        std::string idString(nativeString);
        // 释放 jstring 获取的字符串
        env->ReleaseStringUTFChars(id, nativeString);
        if (playerHashMap.find(idString) != playerHashMap.end()) {
            fmPlayerStruct = &playerHashMap[idString];
            fmPlayerStruct->fmPlayer->pause();
            LOGE("找到 %s", idString.data());
        } else {
            LOGE("找不到 %s %d", idString.data(), playerHashMap.size());
        }

    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_fm_fmplayer_FmPlayer_seek(JNIEnv *env, jobject thiz, jstring id, jlong time) {
    // TODO: implement seek()
    FmPlayerStruct *fmPlayerStruct = nullptr;
    {
        std::lock_guard<std::mutex> lockGuard(player);

        const char *nativeString = env->GetStringUTFChars(id, nullptr);
        // 将 const char* 转换为 std::string
        std::string idString(nativeString);
        // 释放 jstring 获取的字符串
        env->ReleaseStringUTFChars(id, nativeString);
        if (playerHashMap.find(idString) != playerHashMap.end()) {
            fmPlayerStruct = &playerHashMap[idString];
            fmPlayerStruct->fmPlayer->seek(time);
            LOGE("找到 %s", idString.data());
        } else {
            LOGE("找不到 %s %d", idString.data(), playerHashMap.size());
        }

    }
}