//
// Created by Oxters Wyzgowski on 11/26/24.
//

#include "socket_manager.h"
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <android/log.h>

SocketManager::SocketManager() : socket_fd(-1) {}

SocketManager::~SocketManager() {
    disconnect();
}

bool SocketManager::connect(const std::string& socket_path) {
    socket_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (socket_fd == -1) {
        return false;
    }

    struct sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, socket_path.c_str(), sizeof(addr.sun_path) - 1);

    if (::connect(socket_fd, (struct sockaddr*)&addr, sizeof(addr)) == -1) {
        disconnect();
        return false;
    }

    return true;
}

bool SocketManager::sendMessage(const SteamMessage& msg) {
    return send(socket_fd, &msg, sizeof(msg), 0) == sizeof(msg);
}

bool SocketManager::receiveMessage(SteamMessage& msg) {
    return recv(socket_fd, &msg, sizeof(msg), 0) == sizeof(msg);
}

void SocketManager::disconnect() {
    if (socket_fd != -1) {
        close(socket_fd);
        socket_fd = -1;
    }
}