cmake_minimum_required(VERSION 3.22.1)

project("sbagen")

add_definitions(-DBUILD_JNI=1)

# Enable 16 KB page alignment for Android 15+ compatibility
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

add_library(
    sbagen
    SHARED
    sbagen.c
)

find_library(
    log-lib
    log
)

target_link_libraries(
    sbagen
    ${log-lib}
    m
)

target_link_options(
    sbagen
    PRIVATE
    "-Wl,-z,max-page-size=16384"
)
