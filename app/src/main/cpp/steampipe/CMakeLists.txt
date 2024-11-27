cmake_minimum_required(VERSION 3.22.1)

project(steam_api_bridge)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(steam_api SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/src/steam_api_bridge.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/src/socket_manager.cpp
)

target_include_directories(steam_api PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}
)

find_library(log-lib log)

target_link_libraries(steam_api
        ${log-lib}
)

# Rename the output to steam_api.dll.so
set_target_properties(steam_api PROPERTIES
        OUTPUT_NAME "steam_api.dll"
        PREFIX ""
        SUFFIX ".so"
)
