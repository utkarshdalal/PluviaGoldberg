# app/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.10.2)
project(steam_api_bridge)

# Include Wine headers
include_directories($ENV{ANDROID_NDK}/sources/wine-headers)

# Set up the winelib build
add_library(steam_api SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/src/steam_api.spec
        ${CMAKE_CURRENT_SOURCE_DIR}/src/socket_client.c)

# Configure for wine compatibility
set_target_properties(steam_api PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
        PREFIX "lib"
        SUFFIX ".dll.so")
