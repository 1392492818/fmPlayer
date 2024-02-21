//
// Created by fuweicong on 2024/2/17.
//

#include "VideoEncoderManager.h"

void
VideoEncoderManager::init(char *path, int width, int height, AVPixelFormat format, int rotate,
                          int sample_rate,
                          int channel) {
    std::lock_guard lockGuard(encoder);
    fmEncoder = std::make_unique<fm::FmEncoder>(path, width, height, format, rotate, sample_rate,
                                                channel);
    fmEncoder->init();
}

void VideoEncoderManager::addVideoFrame(unsigned char *data, int dataLength, int seconds) {
    std::lock_guard lockGuard(encoder);

    if (fmEncoder != nullptr)
        fmEncoder->add_video_frame(data, dataLength, seconds);
}

void VideoEncoderManager::addAudioFrame(unsigned char *data, int dataLength, int seconds) {
    std::lock_guard lockGuard(encoder);

    if (fmEncoder != nullptr)
        fmEncoder->add_audio_frame(data, dataLength, seconds);
}

void VideoEncoderManager::stop() {
    std::lock_guard lockGuard(encoder);
    fmEncoder->end();
    fmEncoder = nullptr;
}
