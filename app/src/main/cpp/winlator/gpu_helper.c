#include <jni.h>
#include <vulkan/vulkan.h>
#include <stdlib.h>

JNIEXPORT jlong JNICALL
Java_com_winlator_core_GPUHelper_vkGetDeviceExtensions(JNIEnv *env, jclass clazz)
{
    VkInstance instance;
    VkResult   res;

    VkInstanceCreateInfo ci = { .sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO };
    res = vkCreateInstance(&ci, NULL, &instance);
    if (res != VK_SUCCESS) goto make_empty_array;

    uint32_t pdCount = 0;
    res = vkEnumeratePhysicalDevices(instance, &pdCount, NULL);
    if (res != VK_SUCCESS || pdCount == 0) goto make_empty_array;

    pdCount = 1;                        /* original code asks for only one */
    VkPhysicalDevice pd;
    res = vkEnumeratePhysicalDevices(instance, &pdCount, &pd);
    if (!(res == VK_SUCCESS || res == VK_INCOMPLETE)) goto make_empty_array;

    uint32_t extCount = 0;
    res = vkEnumerateDeviceExtensionProperties(pd, NULL, &extCount, NULL);
    if (res != VK_SUCCESS || extCount == 0) goto make_empty_array;

    VkExtensionProperties *ext =
            calloc(extCount, sizeof(VkExtensionProperties));
    res = vkEnumerateDeviceExtensionProperties(pd, NULL, &extCount, ext);
    if (res != VK_SUCCESS) { free(ext); goto make_empty_array; }

    jclass stringCls = (*env)->FindClass(env, "java/lang/String");
    jobjectArray arr =
            (*env)->NewObjectArray(env, (jsize)extCount, stringCls, NULL);

    for (jsize i = 0; i < (jsize)extCount; ++i)
    {
        jstring js = (*env)->NewStringUTF(env, ext[i].extensionName);
        (*env)->SetObjectArrayElement(env, arr, i, js);
    }
    free(ext);
    return (jlong)arr;

    make_empty_array:
    {
        jclass stringCls = (*env)->FindClass(env, "java/lang/String");
        jobjectArray empty = (*env)->NewObjectArray(env, 0, stringCls, NULL);
        return (jlong)empty;
    }
}
