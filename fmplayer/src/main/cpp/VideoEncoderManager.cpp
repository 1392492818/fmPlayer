//
// Created by fuweicong on 2024/2/17.
//

#include "VideoEncoderManager.h"

void
VideoEncoderManager::init(char *path, int width, int height, AVPixelFormat format, int rotate,
                          int sample_rate,
                          int channel, int type) {
    std::lock_guard lockGuard(encoder);
    fmEncoder = std::make_unique<fm::FmEncoder>(path, width, height, format, rotate, sample_rate,
                                                channel,type);
    fmEncoder->init();
}

bool VideoEncoderManager::addVideoFrame(unsigned char *data, int dataLength, int seconds) {
    std::lock_guard lockGuard(encoder);

    if (fmEncoder != nullptr)
        return fmEncoder->add_video_frame(data, dataLength, seconds);
    return false;
}

bool VideoEncoderManager::addAudioFrame(unsigned char *data, int dataLength, int seconds) {
    std::lock_guard lockGuard(encoder);

    if (fmEncoder != nullptr)
        return fmEncoder->add_audio_frame(data, dataLength, seconds);
    return false;
}

void VideoEncoderManager::stop() {
    std::lock_guard lockGuard(encoder);
    if(fmEncoder != nullptr) fmEncoder->end();
    fmEncoder = nullptr;
}
