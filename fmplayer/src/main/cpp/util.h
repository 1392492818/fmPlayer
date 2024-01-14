//
// Created by fuweicong on 2021/4/18.
//

#ifndef RTMPCLIENT_UTIL_H
#define RTMPCLIENT_UTIL_H

typedef unsigned char byte;
typedef  unsigned int UINT;
typedef  unsigned char BYTE;
typedef  unsigned long DWORD;
#include <android/log.h>

#define  LOG_TAG  "nativeprint"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

void short2bytes(short i, byte* bytes, int size);
int bytes2short(byte* bytes, int size);
void int2bytes(int i, byte* bytes, int size);
int bytes2int(byte* bytes, int size);
long long bytes2long(byte *instr);
bool h264_decode_sps(BYTE * buf,unsigned int nLen,int &width,int &height,int &fps);
void de_emulation_prevention(BYTE* buf,unsigned int* buf_size);
DWORD u(UINT BitCount,BYTE * buf,UINT &nStartBit);
int Se(BYTE *pBuff, UINT nLen, UINT &nStartBit);
UINT Ue(BYTE *pBuff, UINT nLen, UINT &nStartBit);


#endif //RTMPCLIENT_UTIL_H
