//
// Created by Oxters Wyzgowski on 11/26/24.
//

#ifndef PLUVIA_SOCKET_MANAGER_H
#define PLUVIA_SOCKET_MANAGER_H

#pragma once
#include <string>
#include "steam_api_bridge.h"

class SocketManager {
        public:
        SocketManager();
        ~SocketManager();
        bool connect(const std::string& socket_path);
        bool sendMessage(const SteamMessage& msg);
        bool receiveMessage(SteamMessage& msg);
        void disconnect();

        private:
        int socket_fd;
};

#endif //PLUVIA_SOCKET_MANAGER_H
