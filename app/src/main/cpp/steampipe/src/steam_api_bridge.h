//
// Created by Oxters Wyzgowski on 11/26/24.
//

#ifndef PLUVIA_STEAM_API_BRIDGE_H
#define PLUVIA_STEAM_API_BRIDGE_H

#include <cstddef>

enum SteamMessageType {
    INIT = 1,
    SHUTDOWN,
    RESTART_APP,
    IS_RUNNING,
    GET_AUTH_TICKET,
    BEGIN_AUTH_SESSION,
    CHECK_LICENSE,
    GET_DIGITAL_ORIGINS,
    GET_ANALOG_HANDLE,
    GET_ANALOG_DATA,
    GET_LAUNCH_PARAM,
    GET_LAUNCH_CMD,
    GC_SEND_MESSAGE,
    GC_CHECK_MESSAGE,
    GC_RETRIEVE_MESSAGE
};

struct SteamMessage {
    SteamMessageType type;
    char data[1024];
    size_t length;
};

#endif //PLUVIA_STEAM_API_BRIDGE_H
