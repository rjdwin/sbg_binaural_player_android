cmake_minimum_required(VERSION 3.22.1)

project("sbagen" C)

add_definitions(-DBUILD_JNI=1)

add_library(
    sbagen
    SHARED
    sbagen.c
)

if(ANDROID)
    find_library(
        log-lib
        log
    )

    target_link_libraries(
        sbagen
        ${log-lib}
        m
    )

    # Enable 16 KB page alignment for Android 15+ compatibility
    target_link_options(
        sbagen
        PRIVATE
        "-Wl,-z,max-page-size=16384"
    )
elseif(UNIX)
    target_link_libraries(
        sbagen
        m
    )
endif()
