//
// Created by fuweicong on 2023/11/28.
//

#ifndef QTANDROID_TESTDECODER_H
#define QTANDROID_TESTDECODER_H
#include "src/player/StreamInfo.h"
#include "src/player/CallAVFrame.h"
#include <QDebug>
class TestDecoder {
public:
    int start( char* input);
    void decoder();
    CallAVFrame *callAvFrame = nullptr;

    void setCallAvFrame(CallAVFrame *callAvFrame);
     char* input;
};


#endif //QTANDROID_TESTDECODER_H
