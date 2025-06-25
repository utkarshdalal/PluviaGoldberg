#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <stdbool.h>
#include <pthread.h>
#include <sys/ipc.h>
#include <sys/syscall.h>
#include <jni.h>
#include <android/log.h>

#define __u32 uint32_t
#include <linux/ashmem.h>
#include <sys/stat.h>
#include <errno.h>
#include <android/sharedmem.h>

#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, "System.out", __VA_ARGS__);

static int ashmemCreateRegion(const char *name, int64_t size) {
    return ASharedMemory_create(name, size);
}

//static int ashmemCreateRegion(const char* name, int64_t size) {
//    // Create /dev directory if it doesn't exist
//    if (mkdir("/dev", 0777) < 0 && errno != EEXIST) {
//        // Handle error, but ignore if directory already exists
//        perror("Failed to create /dev directory");
//        return -1;
//    }
//
//    int fd = open("/dev/ashmem", O_RDWR | O_CREAT, 0777);
//    if (fd < 0) {
//        perror("Failed to open /dev/ashmem");
//        return -1;
//    }
//    int fd = open("/dev/ashmem", O_RDWR);
//    printf("sysvshared_memory.c open %d", fd);
//    if (fd < 0) return -1;
//
//    char nameBuffer[ASHMEM_NAME_LEN] = {0};
//    strncpy(nameBuffer, name, sizeof(nameBuffer));
//    nameBuffer[sizeof(nameBuffer) - 1] = 0;
//
//    int ret = ioctl(fd, ASHMEM_SET_NAME, nameBuffer);
//    if (ret < 0) goto error;
//
//    ret = ioctl(fd, ASHMEM_SET_SIZE, size);
//    if (ret < 0) goto error;
//
//    return fd;
//error:
//    printf("SysVSharedMemory close %d", fd);
//    close(fd);
//    printf("SysVSharedMemory close %d done", fd);
//    return -1;
//}

static int memfd_create(const char *name, unsigned int flags) {
#ifdef __NR_memfd_create
    return syscall(__NR_memfd_create, name, flags);
#else
    return -1;
#endif
}

JNIEXPORT jint JNICALL
Java_com_winlator_sysvshm_SysVSharedMemory_ashmemCreateRegion(JNIEnv *env, jobject obj, jint index,
                                                              jlong size) {
    char name[32];
    sprintf(name, "sysvshm-%d", index);
    return ashmemCreateRegion(name, size);
}

JNIEXPORT jobject JNICALL
Java_com_winlator_sysvshm_SysVSharedMemory_mapSHMSegment(JNIEnv *env, jobject obj, jint fd, jlong size, jint offset, jboolean readonly) {
    char *data = mmap(NULL, size, readonly ? PROT_READ : PROT_WRITE | PROT_READ, MAP_SHARED, fd, offset);
    if (data == MAP_FAILED) return NULL;
    return (*env)->NewDirectByteBuffer(env, data, size);
}

JNIEXPORT void JNICALL
Java_com_winlator_sysvshm_SysVSharedMemory_unmapSHMSegment(JNIEnv *env, jobject obj, jobject data,
                                                           jlong size) {
    char *dataAddr = (*env)->GetDirectBufferAddress(env, data);
    munmap(dataAddr, size);
}

JNIEXPORT jint JNICALL
Java_com_winlator_sysvshm_SysVSharedMemory_createMemoryFd(JNIEnv *env, jclass obj, jstring name,
                                                          jint size) {
    const char *namePtr = (*env)->GetStringUTFChars(env, name, 0);

    int fd = memfd_create(namePtr, MFD_ALLOW_SEALING);
    printf("sysvshared_memory.c memfd_create %d", fd);
    (*env)->ReleaseStringUTFChars(env, name, namePtr);

    if (fd < 0) return -1;

    int res = ftruncate(fd, size);
    if (res < 0) {
        printf("SysVSharedMemory2 close %d", fd);
        close(fd);
        printf("SysVSharedMemory2 close %d done", fd);
        return -1;
    }

    return fd;
}
