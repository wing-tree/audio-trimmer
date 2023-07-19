cmake_minimum_required(VERSION 3.22.1)

project("trimmer")

set(ffmpeg_DIR ${CMAKE_SOURCE_DIR}/ffmpeg)

include_directories(${ffmpeg_DIR}/${ANDROID_ABI}/include)

#add_library(trimmer SHARED native-lib.cpp)

add_library(libavcodec SHARED IMPORTED)
set_target_properties(
        libavcodec
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavcodec.so
)

add_library(libavdevice SHARED IMPORTED)
set_target_properties(
        libavdevice
         PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavdevice.so
)

add_library(libavfilter SHARED IMPORTED)
set_target_properties(
        libavfilter
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavfilter.so
)

add_library(libavformat SHARED IMPORTED)
set_target_properties(
        libavformat
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavformat.so
)

add_library(libavutil SHARED IMPORTED)
set_target_properties(
        libavutil
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libavutil.so
)

add_library(libswresample SHARED IMPORTED)
set_target_properties(
        libswresample
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libswresample.so
)

add_library(libswscale SHARED IMPORTED)
set_target_properties(
        libswscale
        PROPERTIES IMPORTED_LOCATION
        ${ffmpeg_DIR}/${ANDROID_ABI}/lib/libswscale.so
)

add_library(native-lib SHARED native-lib.cpp)
set_target_properties(native-lib PROPERTIES LINKER_LANGUAGE CXX)

find_library( log-lib
        log )

target_link_libraries( native-lib
        ${log-lib} )

target_link_libraries(
        native-lib
        libavcodec
        libavdevice
        libavfilter
        libavformat
        libavutil
        libswresample
        libswscale
)