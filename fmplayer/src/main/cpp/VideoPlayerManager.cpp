//
// Created by fuweicong on 2024/2/15.
//

#include "VideoPlayerManager.h"


void VideoPlayerManager::startPlayer(JavaVM *g_VM, JNIEnv *env, jobject globalRef, string id,
                                     string path, long time, string cachePath) {
    auto *fmPlayer = new fm::FmPlayer();
    auto *videoPlayer = new VideoPlayer(g_VM, env, globalRef);

    {
//        std::lock_guard<std::mutex> lockGuard(player);
        FmPlayerStruct playerStruct;
        playerStruct.videoPlayer = videoPlayer;
        playerStruct.globalObject = globalRef;
        playerStruct.fmPlayer = fmPlayer;
        playerHashMap[id] = playerStruct;
        LOGE("插入id %s %d", id.data(), playerHashMap.size());
    }
    fmPlayer->setCallAvFrame(videoPlayer);
    fmPlayer->startPlayer(path.data(), time, cachePath);
    fmPlayer->start();
}

void VideoPlayerManager::stop(string id) {
//    std::lock_guard<std::mutex> lockGuard(player);
    FmPlayerStruct *fmPlayerStruct = getFmPlayerStruct(id);
    if (fmPlayerStruct != nullptr) {
        fmPlayerStruct->fmPlayer->stop();
        playerHashMap.erase(id);
    }
//    LOGE("size %d ", playerHashMap.size());
}

FmPlayerStruct *VideoPlayerManager::getFmPlayerStruct(string id) {
    FmPlayerStruct *fmPlayerStruct = nullptr;
    if (playerHashMap.find(id) != playerHashMap.end()) {
        fmPlayerStruct = &playerHashMap[id];
    }
    return fmPlayerStruct;
}

void VideoPlayerManager::play(string id) {
    FmPlayerStruct *fmPlayerStruct = getFmPlayerStruct(id);
    if (fmPlayerStruct != nullptr) {
        fmPlayerStruct->fmPlayer->play();
    }
}

void VideoPlayerManager::pause(string id) {
    FmPlayerStruct *fmPlayerStruct = getFmPlayerStruct(id);
    if (fmPlayerStruct != nullptr) {
        fmPlayerStruct->fmPlayer->pause();
    }
}

void VideoPlayerManager::seek(string id, long time) {
    FmPlayerStruct *fmPlayerStruct = getFmPlayerStruct(id);
    if (fmPlayerStruct != nullptr) {
        fmPlayerStruct->fmPlayer->seek(time);
    }
}

void VideoPlayerManager::speed(string id, float speed) {
    FmPlayerStruct *fmPlayerStruct = getFmPlayerStruct(id);
    if (fmPlayerStruct != nullptr) {
        fmPlayerStruct->fmPlayer->updateSpeedAudio(speed);
    }
}