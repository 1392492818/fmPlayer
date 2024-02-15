//
// Created by fuweicong on 2024/1/19.
//

#include <vector>
#include <strstream>
#include "VideoCache.h"
/**
 * 文件缓存数据格式
 * 4 Byte 视频格式类型
 * 8 Byte pts
 * 4 Byte avPacket data 实际数据
 * data 数据
 */
namespace fs = std::filesystem;

bool directoryExists(const std::string &path) {
    return fs::exists(path) && fs::is_directory(path);
}

bool createDirectory(const std::string &path) {
    try {
        if (fs::create_directories(path)) {
            std::cout << "Directory created: " << path << std::endl;
            return true;
        } else {
            std::cerr << "Failed to create directory: " << path << std::endl;
            return false;
        }
    } catch (const std::filesystem::filesystem_error &ex) {
        std::cerr << "Exception creating directory: " << ex.what() << std::endl;
        return false;
    }
}

bool compareFileSizes(const fs::directory_entry &file1, const fs::directory_entry &file2) {
    return fs::file_size(file1) < fs::file_size(file2);
}

fm::VideoCache::VideoCache(string path, string url) {
    this->path = path;
    try {
        fs::remove_all(this->path);
        if (!directoryExists(path)) {
            if (!createDirectory(path)) {
                return;
            }
        }
    } catch (...) {
        LOGE("删除异常");
    }

}

void fm::VideoCache::cachePacket(AVPacket *avPacket, AVRational time_base) {
    lock_guard lock(cacheMutex);

    int streamIndex = avPacket->stream_index;
    int64_t pts = avPacket->pts;

    int64_t endTimeBase = (double) ((double) pts * av_q2d(time_base)) * 1000;
    if (this->endTimeBase < endTimeBase) {
        this->endTimeBase = endTimeBase;
    }
    AVPacket *packetInfo = av_packet_alloc();
    av_packet_copy_props(packetInfo, avPacket);
    packetInfo->size = avPacket->size;
    packetInfo->time_base = time_base;
    packetInfo->stream_index = avPacket->stream_index;
    packetQueue.push(packetInfo);
    Json::Value json;
    string filename = this->path + "/" + to_string(streamIndex) + to_string(pts);
    std::ofstream output = std::ofstream(filename,
                                         std::ios::binary);
    uint8_t *data = avPacket->data;

    output.write((char *) data, avPacket->size);
    output.flush();
    output.close();
    packetQueueEmptyCondition.notify_one();
}

AVPacket *fm::VideoCache::readPacket() {
    if (!this->isEnd) {
        std::unique_lock<std::mutex> emptyLock(cacheEmptyMutex); //数据为空就先不处理
        packetQueueEmptyCondition.wait(emptyLock,
                                       [&] {
                                           return !packetQueue.empty();
                                       });
    }
    lock_guard lock(cacheMutex);
    //非空判断
    if (packetQueue.empty()) {
        return nullptr;
    }
    AVPacket *packetInfo = packetQueue.front();
    packetQueue.pop();
    int streamIndex = packetInfo->stream_index;
    int64_t pts = packetInfo->pts;
    string filename = this->path + "/" + to_string(streamIndex) + to_string(pts);
    std::ifstream inputFile = std::ifstream(filename, std::ios::binary);
    int size = packetInfo->size;
    AVPacket *packet = av_packet_alloc();
    // 创建一个足够大的缓冲区来存储整个文件
    char *data = new char[size];


    // 一次性读取整个文件到缓冲区
    inputFile.read(data, size);
    // 关闭文件
    inputFile.close();


    av_packet_copy_props(packet, packetInfo);
    packet->data = (unsigned char *) data;
    packet->size = packetInfo->size;
    packet->stream_index = packetInfo->stream_index;
    av_packet_free(&packetInfo);

    return packet;
}

void fm::VideoCache::writeCacheFile() {
    Json::Value jsonArray;

//    for (auto it = this->packetMap.begin(); it != this->packetMap.end(); ++it) {
//        const int streamIndex = it->first;
//        std::queue<PacketInfo> packetInfoQueue = it->second;
//        string path = this->path + "/" + std::to_string(streamIndex)+"/file.json";
//
//        while (!packetInfoQueue.empty()) {
//            PacketInfo packet = packetInfoQueue.front();
//            packetInfoQueue.pop();
//            jsonArray.append(packet.toJson());
//        }
//
//        Json::StreamWriterBuilder writer;
//        const std::string jsonString = Json::writeString(writer, jsonArray);
//
//    }
}

const queue<AVPacket *> &fm::VideoCache::getPacketQueue() const {
    return packetQueue;
}

bool fm::VideoCache::seekVideoCache(int64_t time) {
    lock_guard lock(cacheMutex);
    time = time * 1000;
    if (packetQueue.empty()) return false;
    AVPacket *packet = packetQueue.front();
    int64_t startTimeBase = (double) ((double) packet->pts * av_q2d(packet->time_base)) * 1000;
    LOGE("start %lld, time %lld, timeBase %lld", startTimeBase, time, this->endTimeBase);

    if (time < startTimeBase || time > this->endTimeBase) {
        std::queue<AVPacket *> emptyQueue;
        std::swap(packetQueue, emptyQueue);
        LOGE("范围找不到缓存");
        return false;
    }

    while (!packetQueue.empty()) {
        packet = packetQueue.front();
        startTimeBase = (double) ((double) packet->pts * av_q2d(packet->time_base)) * 1000;
        if (startTimeBase >= time && packet->flags == AV_PKT_FLAG_KEY) {
            LOGE("找到缓存");
            return true;
        }
        packetQueue.pop();
    }
    LOGE("end找不到缓存");

    return false;
}

int64_t fm::VideoCache::getEndTimeBase() const {
    return endTimeBase;
}

void fm::VideoCache::setIsEnd(bool isEnd) {
    packetQueueEmptyCondition.notify_one();
    this->isEnd = isEnd;
}


fm::PacketInfo::PacketInfo(int64_t pts, int streamIndex, int size,
                           AVPacketSideData *avPacketSideData, int sideDataElems,
                           AVRational timeBase) : pts(pts),
                                                  streamIndex(streamIndex),
                                                  size(size),
                                                  sideData(avPacketSideData),
                                                  sideDataElems(sideDataElems),
                                                  timeBase(timeBase) {
}

Json::Value fm::PacketInfo::toJson() {
    Json::Value jsonPacketInfo;
    jsonPacketInfo["pts"] = this->pts;
    jsonPacketInfo["streamIndex"] = this->streamIndex;
    return jsonPacketInfo;
}

int64_t fm::PacketInfo::getPts() const {
    return pts;
}

int fm::PacketInfo::getStreamIndex() const {
    return streamIndex;
}

int fm::PacketInfo::getSize() const {
    return size;
}

void fm::PacketInfo::setSize(int size) {
    PacketInfo::size = size;
}


AVPacketSideData *fm::PacketInfo::getSideData() const {
    return sideData;
}

int fm::PacketInfo::getSideDataElems() const {
    return sideDataElems;
}

const AVRational &fm::PacketInfo::getTimeBase() const {
    return timeBase;
}




