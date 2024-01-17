//
// Created by fuweicong on 2023/12/9.
//

#include "VideoPlayer.h"
#include "util.h"

//public void onVideoFrame(byte[] yData, byte[] uData, byte[] vData);
//public void onAudioFrame(byte[] audioData, int channels, int avSampleFormat, int dataSize);
//public void onProgress(long time, long currentTime);
//public void onEnd();

VideoPlayer::VideoPlayer(JavaVM * g_vm,JNIEnv *env, jobject callbackObj) {
    this->jobject1 = callbackObj;
    this->g_VM = g_vm;
//    this->callbackClass = env->GetObjectClass(callbackObj);
//    onVideoFrameId = env->GetMethodID(callbackClass, "onVideoFrame", "([B[B[B)V");
//    onAudioFrameId = env->GetMethodID(callbackClass, "onAudioFrame", "([BIIIII)V");
//    onProgressId = env->GetMethodID(callbackClass, "onProgress", "(JJ)V");
//    onEndId = env->GetMethodID(callbackClass, "onEnd", "()V");
}

void VideoPlayer::onVideoFrame(AVFrame *avFrame) {
// 创建 Java 的 byte 数组
// 获取当前线程的 JNIEnv

    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, nullptr);
    jclass callbackClass = env->GetObjectClass(jobject1);
    jmethodID onVideoFrameId = env->GetMethodID(callbackClass, "onVideoFrame", "(II[B[B[B)V");

    // 在这里进行 JNI 调用

    int width = avFrame->width;
    int height = avFrame->height;
    int ySize = width * height;
    int uvSize = width * height / 4;

    jbyteArray yByteArray = env->NewByteArray(ySize);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(yByteArray, 0, ySize, reinterpret_cast<const jbyte*>(avFrame->data[0]));

    jbyteArray uByteArray = env->NewByteArray(uvSize);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(uByteArray, 0, uvSize, reinterpret_cast<const jbyte*>(avFrame->data[1]));

    jbyteArray vByteArray = env->NewByteArray(uvSize);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(vByteArray, 0, uvSize, reinterpret_cast<const jbyte*>(avFrame->data[2]));

    env->CallVoidMethod(jobject1, onVideoFrameId,avFrame->width, avFrame->height, yByteArray, uByteArray, vByteArray);
    env->DeleteLocalRef(yByteArray);
    env->DeleteLocalRef(uByteArray);
    env->DeleteLocalRef(vByteArray);

    g_VM->DetachCurrentThread();

}

void VideoPlayer::onAudioFrame(AVFrame *avFrame, int channels, AVSampleFormat avSampleFormat,
                               int dataSize) {

    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, nullptr);
    jclass callbackClass = env->GetObjectClass(jobject1);
    jmethodID audioFrameId = env->GetMethodID(callbackClass, "onAudioFrame", "([BIIIII)V");;

    int length = avFrame->nb_samples * dataSize * channels;

    char* buffer = new char [length];
//    bool checkChannels = true;
//    for(int ch = 0; ch < channels;ch++) {
//        if(avFrame->data[ch] == nullptr) checkChannels = false;
//    }
//    if(checkChannels) {
        for (int i = 0; i < avFrame->nb_samples; i++)
            for (int ch = 0; ch < channels; ch++) {
                memcpy(buffer + dataSize * i * channels, avFrame->data[ch] + dataSize * i, dataSize);
            }

//    } else {
//        memcpy(buffer, avFrame->data[0], length);
//    }

    jbyteArray audioByteArray = env->NewByteArray(length);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(audioByteArray, 0, length, reinterpret_cast<const jbyte*>(buffer));


    env->CallVoidMethod(jobject1, audioFrameId,audioByteArray, avFrame->sample_rate, avFrame->nb_samples, channels, avSampleFormat, dataSize);



    env->DeleteLocalRef(audioByteArray);
    free(buffer);


    g_VM->DetachCurrentThread();
}

void VideoPlayer::onProgress(int64_t time, int64_t currentTime) {
    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, nullptr);
    jclass callbackClass = env->GetObjectClass(jobject1);
    jmethodID onProgressId = env->GetMethodID(callbackClass, "onProgress", "(JJ)V");
    env->CallVoidMethod(jobject1, onProgressId,time, currentTime);
    g_VM->DetachCurrentThread();
}

void VideoPlayer::onEnd(bool isError) {
    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, nullptr);
    jclass callbackClass = env->GetObjectClass(jobject1);
    jmethodID onEnd = env->GetMethodID(callbackClass, "onEnd", "(Z)V");
    env->CallVoidMethod(jobject1, onEnd, isError);
    g_VM->DetachCurrentThread();
}

bool VideoPlayer::onVideoPacket(int width, int height, int format, AVPacket *packet) {
    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, nullptr);
    jclass callbackClass = env->GetObjectClass(jobject1);
    jmethodID onVideoPacketId = env->GetMethodID(callbackClass, "onVideoPacket", "(III[B)Z");

    // 在这里进行 JNI 调用

    jbyteArray packetArray = env->NewByteArray(packet->size);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(packetArray, 0, packet->size, reinterpret_cast<const jbyte*>(packet->data));

    bool isHardDecoder = env->CallBooleanMethod(jobject1, onVideoPacketId, width, height, format, packetArray);
    env->DeleteLocalRef(packetArray);


    g_VM->DetachCurrentThread();

    return isHardDecoder;
}


void VideoPlayer::onLoading() {
    JNIEnv *env;
    g_VM->AttachCurrentThread(&env, nullptr);
    jclass callbackClass = env->GetObjectClass(jobject1);
    jmethodID onEnd = env->GetMethodID(callbackClass, "onLoading", "()V");
    env->CallVoidMethod(jobject1, onEnd);
    g_VM->DetachCurrentThread();
}

VideoPlayer::~VideoPlayer() {

}

void VideoPlayer::release() {
    LOGE("释放 video player");
//    JNIEnv *env;
//    g_VM->AttachCurrentThread(&env, nullptr);
//    env->DeleteGlobalRef(jobject1);
//    g_VM->DetachCurrentThread();
}




