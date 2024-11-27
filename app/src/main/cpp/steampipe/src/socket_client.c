//
// Created by Oxters Wyzgowski on 11/27/24.
//
// socket_client.c
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <string.h>
#include "socket_client.h"

static int sock_fd = -1;

int socket_init(const char* socket_path) {
    struct sockaddr_un addr;

    sock_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock_fd == -1) {
        return -1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, socket_path, sizeof(addr.sun_path)-1);

    if (connect(sock_fd, (struct sockaddr*)&addr, sizeof(addr)) == -1) {
        close(sock_fd);
        sock_fd = -1;
        return -1;
    }

    return 0;
}

int socket_send_message(enum MessageType type, const void* data, uint32_t length) {
    struct MessageHeader header = {type, length};

    if (write(sock_fd, &header, sizeof(header)) != sizeof(header)) {
        return -1;
    }

    if (length > 0 && data != NULL) {
        if (write(sock_fd, data, length) != length) {
            return -1;
        }
    }

    return 0;
}

void socket_cleanup(void) {
    if (sock_fd != -1) {
        close(sock_fd);
        sock_fd = -1;
    }
}
