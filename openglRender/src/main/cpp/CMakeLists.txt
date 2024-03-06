# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 20)
add_definitions(-DFM_ANDROID)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(
        util
        opencv_4_9_0
        assimp/include
        glm
        model
        sample
        render
)
set(assimp_libs ${CMAKE_SOURCE_DIR}/assimp/${ANDROID_ABI})


add_library(libassimp SHARED IMPORTED)
set_target_properties(libassimp PROPERTIES
        IMPORTED_LOCATION "${assimp_libs}/libassimp.so")

add_library(opencv_java4 SHARED IMPORTED )
set_target_properties(opencv_java4 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/opencv/${ANDROID_ABI}/libopencv_java4.so)


file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/util/*.cpp
        ${CMAKE_SOURCE_DIR}/render/*.cpp
        ${CMAKE_SOURCE_DIR}/sample/*.cpp
)

add_library( # Sets the name of the library.
        opengl-render
        # Sets the library as a shared library.
        SHARED
        opengl.cpp
        LoadModule.cpp
        LoadModule.h
        Log.h
        ${src-files}
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
        opengl-render

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        jnigraphics
        android
        libassimp
        opencv_java4
        EGL
        GLESv3
        )