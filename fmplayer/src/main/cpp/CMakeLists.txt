# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 20)
add_definitions(-DFM_ANDROID)
include_directories(jsoncpp/include)
add_subdirectory(jsoncpp)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(ffmpeg/include)
set(ffmpeg_libs ${CMAKE_SOURCE_DIR}/ffmpeg/${ANDROID_ABI})


add_library(postproc
        SHARED
        IMPORTED)
set_target_properties(postproc
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libpostproc.so)

add_library(avcodec
        SHARED
        IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libavcodec.so)

add_library(avdevice
        SHARED
        IMPORTED)
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libavdevice.so)
add_library(avformat
        SHARED
        IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libavformat.so)
add_library(avutil
        SHARED
        IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libavutil.so)
add_library(swresample
        SHARED
        IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libswresample.so)
add_library(swscale
        SHARED
        IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libswscale.so)
add_library(avfilter
        SHARED
        IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_libs}/libavfilter.so)


add_library( # Sets the name of the library.
        video
        # Sets the library as a shared library.
        SHARED
        util.cpp
        video.cpp
        VideoPlayer.cpp
        VideoPlayer.h
        VideoPlayerManager.cpp
        VideoPlayerManager.h
        VideoEncoderManager.cpp
        VideoEncoderManager.h
        player/CallAVFrame.h
        player/FmPlayer.cpp
        player/FmPlayer.h
        player/StreamInfo.cpp
        player/StreamInfo.h
        player/VideoDecoder.cpp
        player/VideoDecoder.h
        player/StreamFilter.cpp
        player/StreamFilter.h
        player/MediaCodecDecoder.cpp
        player/MediaCodecDecoder.h
        player/VideoCache.cpp
        player/VideoCache.h
        encoder/FmEncoder.cpp
        encoder/FmEncoder.h
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        video
        postproc
        avfilter
        avcodec
        avdevice
        avformat
        avutil
        swresample
        swscale
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        jnigraphics
        android
        jsoncpp
        )