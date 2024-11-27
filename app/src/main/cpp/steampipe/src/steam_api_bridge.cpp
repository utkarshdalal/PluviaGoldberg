//
// Created by Oxters Wyzgowski on 11/26/24.
//

#include "steam_api_bridge.h"
#include "socket_manager.h"
#include <android/log.h>

static SocketManager g_SocketManager;
static const char* SOCKET_PATH = "/tmp/.steam/steam_pipe";

extern "C" {

    bool SteamAPI_Init() {
        SteamMessage msg = {INIT};
        if (!g_SocketManager.connect(SOCKET_PATH)) return false;
        return g_SocketManager.sendMessage(msg);
    }

    void SteamAPI_Shutdown() {
        SteamMessage msg = {SHUTDOWN};
        g_SocketManager.sendMessage(msg);
        g_SocketManager.disconnect();
    }

    bool SteamAPI_RestartAppIfNecessary(unsigned int appid) {
        SteamMessage msg = {RESTART_APP};
        memcpy(msg.data, &appid, sizeof(appid));
        msg.length = sizeof(appid);
        return g_SocketManager.sendMessage(msg);
    }

    bool SteamAPI_IsSteamRunning() {
        SteamMessage msg = {IS_RUNNING};
        if (!g_SocketManager.sendMessage(msg)) return false;

        SteamMessage response{};
        if (!g_SocketManager.receiveMessage(response)) return false;
        return *reinterpret_cast<bool*>(response.data);
    }

// Additional Steam API implementations would follow similar pattern
}