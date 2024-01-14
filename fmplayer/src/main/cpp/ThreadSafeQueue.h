//
// Created by fuweicong on 2023/12/9.
//

#ifndef FMPLAYER_THREADSAFEQUEUE_H
#define FMPLAYER_THREADSAFEQUEUE_H
#include <iostream>
#include <queue>
#include <mutex>
#include <condition_variable>

template <typename T>
class ThreadSafeQueue {
public:
    ThreadSafeQueue() {}

    void push(const T& value) {
        std::lock_guard<std::mutex> lock(mutex_);
        queue_.push(value);
        condition_.notify_one();
    }

    bool tryPop(T& value) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (queue_.empty()) {
            return false;
        }

        value = queue_.front();
        queue_.pop();
        return true;
    }

    void waitAndPop(T& value) {
        std::unique_lock<std::mutex> lock(mutex_);
        condition_.wait(lock, [this] { return !queue_.empty(); });

        value = queue_.front();
        queue_.pop();
    }

    bool isEmpty() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return queue_.empty();
    }

private:
    std::queue<T> queue_;
    mutable std::mutex mutex_;
    std::condition_variable condition_;
};


#endif //FMPLAYER_THREADSAFEQUEUE_H
