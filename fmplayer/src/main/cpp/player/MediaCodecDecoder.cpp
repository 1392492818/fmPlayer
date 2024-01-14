//
// Created by fuweicong on 2023/12/14.
//
#ifdef FM_ANDROID

#include "MediaCodecDecoder.h"

fm::MediaCodecDecoder::MediaCodecDecoder(JavaVM *g_vm, jobject jobject1) {
    this->g_vm = g_vm;
    this->decoderObject = jobject1;
}

bool fm::MediaCodecDecoder::init(int width, int height, int format, char* sps, int spsLength, char* pps, int ppsLength) {
    // 找到类
    JNIEnv *env;
    g_vm->AttachCurrentThread(&env, nullptr);

    // 定义构造函数
    jclass decoderClass = env->GetObjectClass(decoderObject);


    jmethodID  init = env->GetMethodID(decoderClass, "init", "(III[B[B)Z");
    if(init == NULL) {
        return false;
    }

    jbyteArray spsArray = env->NewByteArray(spsLength);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(spsArray, 0, spsLength, reinterpret_cast<const jbyte*>(sps));

    jbyteArray ppsArray = env->NewByteArray(ppsLength);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(ppsArray, 0, ppsLength, reinterpret_cast<const jbyte*>(pps));

    // 调用 getMyValue 方法并获取返回值
    bool flag = env->CallBooleanMethod(decoderObject, init, width, height, format, spsArray, ppsArray);
    env->DeleteLocalRef(spsArray);
    env->DeleteLocalRef(ppsArray);
    this->g_vm->DetachCurrentThread();
    return flag;
}

fm::MediaCodecDecoder::~MediaCodecDecoder() {
    LOGE("执行~MediaCodecDecoder");
}

std::queue<char*> fm::MediaCodecDecoder::onFrame(char *data, int size) {
    JNIEnv *env;
    g_vm->AttachCurrentThread(&env, nullptr);
    std::queue<char*> queue;
    // 定义构造函数
    jclass decoderClass = env->GetObjectClass(decoderObject);


    jmethodID  init = env->GetMethodID(decoderClass, "onFrame", "([B)Ljava/util/List;");
    if(init == NULL) {
        LOGE("执行失败");
        return queue;
    }

    jbyteArray dataArray = env->NewByteArray(size);
    // 将 char* 复制到 byte 数组中
    env->SetByteArrayRegion(dataArray, 0, size, reinterpret_cast<const jbyte*>(data));


    // 调用 getMyValue 方法并获取返回值
    jobject frameList = env->CallObjectMethod(decoderObject, init, dataArray);

    // 获取 ArrayList 类
    jclass arrayListClass = env->GetObjectClass(frameList);

    // 获取 ArrayList 的 size 方法
    jmethodID sizeMethod =   env->GetMethodID(arrayListClass, "size", "()I");

    // 调用 size 方法获取 ArrayList 的大小
    jint listSize = env->CallIntMethod(frameList, sizeMethod);

    // 获取 ArrayList 的 get 方法
    jmethodID getMethod = env->GetMethodID(arrayListClass, "get", "(I)Ljava/lang/Object;");

    // 遍历 ArrayList
    for (jint i = 0; i < listSize; i++) {
        // 调用 get 方法获取 List 中的每个元素
        jobject byteObject = env->CallObjectMethod(frameList, getMethod, i);

        // 将 jobject 转换为 jbyteArray
        jbyteArray byteArray = (jbyteArray)byteObject;

        // 获取 jbyteArray 的长度
        jsize arraySize = env->GetArrayLength(byteArray);
        // 获取 jbyteArray 的指针
        jbyte *byteArrayElements = env->GetByteArrayElements(byteArray, NULL);
        char*  data = new char[arraySize];
        memcpy(data, byteArrayElements, arraySize);
        queue.push(reinterpret_cast<char *const>(data));
        // 释放 jbyteArray 的指针
        env->ReleaseByteArrayElements(byteArray, byteArrayElements, JNI_ABORT);
    }
    env->DeleteLocalRef(dataArray);
    this->g_vm->DetachCurrentThread();
    return queue;
}

int fm::MediaCodecDecoder::getMediaFormat() {
    JNIEnv *env;
    g_vm->AttachCurrentThread(&env, nullptr);
    int format = -1;
    jclass decoderClass = env->GetObjectClass(decoderObject);
    jmethodID  getMediaFormat = env->GetMethodID(decoderClass, "getMediaFormat", "()I");
    if(getMediaFormat == NULL) {
        LOGE("执行失败");
        return format;
    }
    format = env->CallIntMethod(decoderObject, getMediaFormat);
    this->g_vm->DetachCurrentThread();
    return format;
}

#endif