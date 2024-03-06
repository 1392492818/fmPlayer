//
// Created by fuweicong on 2024/3/4.
//

#ifndef FMMEDIA_LOG_H
#define FMMEDIA_LOG_H
#include <android/log.h>

#define  LOG_TAG  "OpenGlRender"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#endif //FMMEDIA_LOG_H
