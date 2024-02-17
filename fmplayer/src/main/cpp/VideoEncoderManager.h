//
// Created by fuweicong on 2024/2/17.
//

#ifndef FMMEDIA_VIDEOENCODERMANAGER_H
#define FMMEDIA_VIDEOENCODERMANAGER_H

#include "encoder/FmEncoder.h"
#include <mutex>
#include <thread>
class VideoEncoderManager {
private:
    std::unique_ptr<fm::FmEncoder> fmEncoder = nullptr;
    std::mutex encoder;
public:
    void init(char* path, int width, int height, AVPixelFormat format,int rotate,int  sample_rate,
             int channel);
    void addVideoFrame(char* data, int dataLength,int seconds);
    void addAudioFrame(char* data, int dataLength, int seconds);
    void stop();
};


#endif //FMMEDIA_VIDEOENCODERMANAGER_H
