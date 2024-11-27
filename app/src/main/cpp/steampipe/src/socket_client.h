//
// Created by Oxters Wyzgowski on 11/27/24.
//

// socket_client.h
#ifndef PLUVIA_SOCKET_CLIENT_H
#define PLUVIA_SOCKET_CLIENT_H

#include <stdint.h>
#include "protocol.h"

int socket_init(const char* socket_path);
int socket_send_message(enum MessageType type, const void* data, uint32_t length);
int socket_receive_message(void* buffer, uint32_t max_length);
void socket_cleanup(void);

#endif //PLUVIA_SOCKET_CLIENT_H
