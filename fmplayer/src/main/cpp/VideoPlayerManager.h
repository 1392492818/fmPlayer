//
// Created by fuweicong on 2024/2/15.
//

#ifndef FMMEDIA_VIDEOPLAYERMANAGER_H
#define FMMEDIA_VIDEOPLAYERMANAGER_H
#include <jni.h>
#include <unordered_map>
#include "VideoPlayer.h"
#include "player/FmPlayer.h"
typedef struct {
    fm::FmPlayer *fmPlayer;
    jobject globalObject;
    VideoPlayer *videoPlayer;
} FmPlayerStruct;
class VideoPlayerManager {
private:
    std::unordered_map<std::string, FmPlayerStruct> playerHashMap;
    std::unordered_map<std::string, long> seekTimeMap;
    std::unordered_map<std::string, float> setSpeedMap;
    std::mutex player; // 保证队列进出不影响
public:
    void startPlayer(JavaVM *g_VM, JNIEnv *env, jobject globalRef, string id,  string path, long time, string cachePath);
    void stop(string id);
    void play(string id);
    void pause(string id);
    void seek(string id, long time);
    void speed(string id, float speed);
private:
    FmPlayerStruct* getFmPlayerStruct(string id);

};


#endif //FMMEDIA_VIDEOPLAYERMANAGER_H
