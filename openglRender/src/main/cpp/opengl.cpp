#include <jni.h>

//
// Created by fuweicong on 2024/3/3.
//
#include "Log.h"
#include "LoadModule.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "render/MyGLRenderContext.h"


extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1Init(JNIEnv *env, jobject thiz) {
    // TODO: implement native_Init()
    MyGLRenderContext::GetInstance();
    LOGE("初始化成功");
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1OnSurfaceCreated(JNIEnv *env, jobject thiz) {
    MyGLRenderContext::GetInstance()->OnSurfaceCreated();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1OnSurfaceChanged(JNIEnv *env, jobject thiz,
                                                               jint width, jint height) {
    MyGLRenderContext::GetInstance()->OnSurfaceChanged(width, height);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1OnDrawFrame(JNIEnv *env, jobject thiz) {
    // TODO: implement native_OnDrawFrame()
    MyGLRenderContext::GetInstance()->OnDrawFrame();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1UpdateTransformMatrix(JNIEnv *env, jobject thiz,
                                                                    jfloat rotate_x,
                                                                    jfloat rotate_y, jfloat scale_x,
                                                                    jfloat scale_y) {
    MyGLRenderContext::GetInstance()->UpdateTransformMatrix(rotate_x, rotate_y, scale_x, scale_y);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1SetParamsFloat(JNIEnv *env, jobject thiz,
                                                             jint param_type, jfloat x,
                                                             jfloat y) {
    // TODO: implement native_SetParamsFloat()
    MyGLRenderContext::GetInstance()->SetParamsFloat(param_type,x,y );
}
extern "C"
JNIEXPORT void JNICALL
Java_com_fm_openglrender_OpenglRender_native_1UnInit(JNIEnv *env, jobject thiz) {
    MyGLRenderContext::DestroyInstance();
}