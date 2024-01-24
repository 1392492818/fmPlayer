//
// Created by fuweicong on 2024/1/19.
//

#ifndef FMMEDIA_VIDEOCACHE_H
#define FMMEDIA_VIDEOCACHE_H

#include "../util.h"
#include <iostream>
#include <iostream>
#include "StreamInfo.h"
#include <fstream>
#include <string>
#include <filesystem>
#include <queue>
#include <thread>
#include "json/json.h"
using namespace std;
namespace fm {

    class PacketInfo{
    private:
        int64_t pts;
        int size;
        AVPacketSideData *sideData;
        int sideDataElems = 0;
        AVRational timeBase;
    public:
        const AVRational &getTimeBase() const;

    public:
        int getSideDataElems() const;

    public:
        AVPacketSideData *getSideData() const;



    public:
        int getSize() const;

        void setSize(int size);

    public:
        int64_t getPts() const;

        int getStreamIndex() const;

    private:
        int streamIndex;
    public:
        explicit PacketInfo(int64_t pts,int streamIndex, int size, AVPacketSideData *sideData, int sideDataElems,AVRational time_base);
        Json::Value toJson();
    };

    class VideoCache {
    private:
        string path;
        string id;
        string filename;
        mutex cacheEmptyMutex;
        mutex cacheMutex;
        std::condition_variable packetQueueEmptyCondition;
        std::queue<AVPacket*> packetQueue;
        int64_t endTimeBase = 0;
    public:
        const queue <AVPacket*> &getPacketQueue() const;

    public:
        VideoCache(string path, string url);
        bool seekVideoCache(int64_t time);

        void cachePacket(AVPacket *avPacket, AVRational time_base);

        AVPacket* readPacket();

        void writeCacheFile();
    };
}


#endif //FMMEDIA_VIDEOCACHE_H
