//
// Created by Oxters Wyzgowski on 11/27/24.
//
// steam_api.c
#include <windef.h>
#include <winbase.h>
#include <stdint.h>
#include "socket_client.h"

#define STEAM_SOCKET_PATH "/tmp/.steam/steam_pipe"

__declspec(dllexport) bool __stdcall SteamAPI_Init(void) {
    if (socket_init(STEAM_SOCKET_PATH) != 0) {
        return false;
    }
    return socket_send_message(MSG_STEAM_INIT, NULL, 0) == 0;
}

__declspec(dllexport) void __stdcall SteamAPI_Shutdown(void) {
    socket_send_message(MSG_STEAM_SHUTDOWN, NULL, 0);
    socket_cleanup();
}

__declspec(dllexport) bool __stdcall SteamAPI_RestartAppIfNecessary(uint32_t appid) {
    return socket_send_message(MSG_STEAM_RESTART, &appid, sizeof(appid)) == 0;
}

__declspec(dllexport) bool __stdcall SteamAPI_IsSteamRunning(void) {
    return socket_send_message(MSG_STEAM_IS_RUNNING, NULL, 0) == 0;
}
