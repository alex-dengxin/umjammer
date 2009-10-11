/** 
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

#include "JavaxUsb.h"

/* These MUST match those defined in com/ibm/jusb/os/linux/LinuxRequest.java */
#define WINDOWS_PIPE_REQUEST 1
#define WINDOWS_SET_INTERFACE_REQUEST 2
#define WINDOWS_SET_CONFIGURATION_REQUEST 3
#define WINDOWS_CLAIM_INTERFACE_REQUEST 4
#define WINDOWS_IS_CLAIMED_INTERFACE_REQUEST 5
#define WINDOWS_RELEASE_INTERFACE_REQUEST 6
#define WINDOWS_ISOCHRONOUS_REQUEST 7

static void submitRequest(JNIEnv *env, int fd, jobject windowsRequest);
static void cancelRequest(JNIEnv *env, int fd, jobject windowsRequest);
static void completeRequest(JNIEnv *env, jobject windowsRequest);

/*
 * Proxy for all I/O with a device
 * @author Dan Streetman
 */
JNIEXPORT void JNICALL Java_vavi_jusb_os_win32_JavaxUsb_nativeDeviceProxy(JNIEnv *env, jclass JavaxUsb, jobject windowsDeviceProxy) {
    int fd = 0;
    struct usbdevfs_urb *urb;
    int loop_count = 0;

    jclass WindowsDeviceProxy;
    jobject windowsRequest;
    jstring jkey;
    jmethodID startCompleted, isRequestWaiting, getReadyRequest, getCancelRequest;
    jmethodID getKey;

    WindowsDeviceProxy = (*env)->GetObjectClass(env, windowsDeviceProxy);
    startCompleted = (*env)->GetMethodID(env, WindowsDeviceProxy, "startCompleted", "(I)V");
    isRequestWaiting = (*env)->GetMethodID(env, WindowsDeviceProxy, "isRequestWaiting", "()Z");
    getReadyRequest = (*env)->GetMethodID(env, WindowsDeviceProxy, "getReadyRequest", "()Lvavi/jusb/os/win32/WindowsRequest;");
    getCancelRequest = (*env)->GetMethodID(env, WindowsDeviceProxy, "getCancelRequest", "()Lvavi/jusb/os/win32/WindowsRequest;");
    getKey = (*env)->GetMethodID(env, WindowsDeviceProxy, "getKey", "()Ljava/lang/String;");
    jkey = (*env)->CallObjectMethod(env, windowsDeviceProxy, getKey);
    (*env)->DeleteLocalRef(env, WindowsDeviceProxy);

    errno = 0;
    HANDLE h = open_device(env, jkey, O_RDWR);
    (*env)->DeleteLocalRef(env, jkey);

    if (0 > fd) {
fprintf(stderr, "Could not open node for device!");
        (*env)->CallVoidMethod(env, windowsDeviceProxy, startCompleted, errno);
        return;
    }

    (*env)->CallVoidMethod(env, windowsDeviceProxy, startCompleted, 0);

    /* run forever...? */
    while (1) {
        /* FIXME - stop using polling! */
        if (loop_count > 20) {
            usleep(0);
            loop_count = 0;
        }
        loop_count ++;

        if (JNI_TRUE == (*env)->CallBooleanMethod(env, windowsDeviceProxy, isRequestWaiting)) {
            if ((windowsRequest = (*env)->CallObjectMethod(env, windowsDeviceProxy, getReadyRequest))) {
fprintf(stderr, "Got Request");
                submitRequest(env, fd, windowsRequest);
                (*env)->DeleteLocalRef(env, windowsRequest);
fprintf(stderr, "Completed Request");
            }

            if ((windowsRequest = (*env)->CallObjectMethod(env, windowsDeviceProxy, getCancelRequest))) {
fprintf(stderr, "Got Abort Request");
                cancelRequest(env, fd, windowsRequest);
                (*env)->DeleteLocalRef(env, windowsRequest);
fprintf(stderr, "Completed Abort Request");
            }
        }

        errno = 0;
        if (!(ioctl(fd, USBDEVFS_REAPURBNDELAY, &urb))) {
fprintf(stderr, "Got completed URB");
            windowsRequest = urb->usercontext;
            completeRequest(env, windowsRequest);
            (*env)->DeleteGlobalRef(env, windowsRequest);
fprintf(stderr, "Finished completed URB");
        } else if (ENODEV == errno) {
            break;
        }
    }

fprintf(stderr, "Device Proxy exiting.");

    close(fd);
}

/**
 * Submit a WindowsRequest.
 * @param env The JNIEnv.
 * @param fd The file descriptor.
 * @param windowsRequest The WindowsRequest.
 */
static void submitRequest(JNIEnv *env, int fd, jobject windowsRequest) {
    int type, err, sync = 0;

    jclass WindowsRequest;
    jmethodID getType, setError, setCompleted;

    WindowsRequest = (*env)->GetObjectClass(env, windowsRequest);
    getType = (*env)->GetMethodID(env, WindowsRequest, "getType", "()I");
    setCompleted = (*env)->GetMethodID(env, WindowsRequest, "setCompleted", "(Z)V");
    setError = (*env)->GetMethodID(env, WindowsRequest, "setError", "(I)V");
    (*env)->DeleteLocalRef(env, WindowsRequest);

    type = (*env)->CallIntMethod(env, windowsRequest, getType);

fprintf(stderr, "Submitting Request.");

    switch (type) {
    case WINDOWS_PIPE_REQUEST:
fprintf(stderr, "Submitting Pipe Request.");
        err = pipe_request(env, fd, windowsRequest);
        break;
    case WINDOWS_SET_INTERFACE_REQUEST:
fprintf(stderr, "Submitting SetInterface Request.");
        err = set_interface(env, fd, windowsRequest);
        sync = 1;
        break;
    case WINDOWS_SET_CONFIGURATION_REQUEST:
fprintf(stderr, "Submitting SetConfiguration Request.");
        err = set_configuration(env, fd, windowsRequest);
        sync = 1;
        break;
    case WINDOWS_CLAIM_INTERFACE_REQUEST:
fprintf(stderr, "Submitting ClaimInterface Request.");
        err = claim_interface(env, fd, 1, windowsRequest);
        sync = 1;
        break;
    case WINDOWS_RELEASE_INTERFACE_REQUEST:
fprintf(stderr, "Submitting ReleaseInterface Request.");
        err = claim_interface(env, fd, 0, windowsRequest);
        sync = 1;
        break;
    case WINDOWS_IS_CLAIMED_INTERFACE_REQUEST:
fprintf(stderr, "Submitting IsClaimed Request.");
        err = is_claimed(env, fd, windowsRequest);
        sync = 1;
        break;
    case WINDOWS_ISOCHRONOUS_REQUEST:
fprintf(stderr, "Submitting Isochronous Request.");
        err = isochronous_request(env, fd, windowsRequest);
        break;
    default: /* ? */
ffprintf(stderr, "Unknown Request type %d", type);
        err = -EINVAL;
        break;
    }

    if (err) {
        (*env)->CallVoidMethod(env, windowsRequest, setError, err);
    }

    if (sync || err) {
        (*env)->CallVoidMethod(env, windowsRequest, setCompleted, JNI_TRUE);
    }
}

/**
 * Cancel a WindowsRequest.
 * @param env The JNIEnv.
 * @param fd The file descriptor.
 * @param windowsRequest The WindowsRequest.
 */
static void cancelRequest(JNIEnv *env, int fd, jobject windowsRequest) {
    int type;

    jclass WindowsRequest;
    jmethodID getType;

    WindowsRequest = (*env)->GetObjectClass(env, windowsRequest);
    getType = (*env)->GetMethodID(env, WindowsRequest, "getType", "()I");
    (*env)->DeleteLocalRef(env, WindowsRequest);

    type = (*env)->CallIntMethod(env, windowsRequest, getType);

    switch (type) {
    case WINDOWS_PIPE_REQUEST:
        cancel_pipe_request(env, fd, windowsRequest);
        break;
    case WINDOWS_SET_INTERFACE_REQUEST:
    case WINDOWS_SET_CONFIGURATION_REQUEST:
    case WINDOWS_CLAIM_INTERFACE_REQUEST:
    case WINDOWS_IS_CLAIMED_INTERFACE_REQUEST:
    case WINDOWS_RELEASE_INTERFACE_REQUEST:
        /* cannot abort these synchronous requests */
        break;
    case WINDOWS_ISOCHRONOUS_REQUEST:
        cancel_isochronous_request(env, fd, windowsRequest);
        break;
    default: /* ? */
ffprintf(stderr, "Unknown Request type %d", type);
        break;
    }    
}

/**
 * Complete a WindowsRequest.
 * @param env The JNIEnv.
 * @param windowsRequest The WindowsRequest.
 */
static void completeRequest(JNIEnv *env, jobject windowsRequest) {
    int type, err;
    
    jclass WindowsRequest;
    jmethodID getType, setError, setCompleted;
    
    WindowsRequest = (*env)->GetObjectClass(env, windowsRequest);
    getType = (*env)->GetMethodID(env, WindowsRequest, "getType", "()I");
    setCompleted = (*env)->GetMethodID(env, WindowsRequest, "setCompleted", "(Z)V");
    setError = (*env)->GetMethodID(env, WindowsRequest, "setError", "(I)V");
    (*env)->DeleteLocalRef(env, WindowsRequest);
    
    type = (*env)->CallIntMethod(env, windowsRequest, getType);
    
    switch (type) {
    case WINDOWS_PIPE_REQUEST:
        err = complete_pipe_request(env, windowsRequest);
        break;
    case WINDOWS_SET_INTERFACE_REQUEST:
    case WINDOWS_SET_CONFIGURATION_REQUEST:
    case WINDOWS_CLAIM_INTERFACE_REQUEST:
    case WINDOWS_IS_CLAIMED_INTERFACE_REQUEST:
    case WINDOWS_RELEASE_INTERFACE_REQUEST:
        /* these are synchronous, completion happens during submit */
        break;
    case WINDOWS_ISOCHRONOUS_REQUEST:
        err = complete_isochronous_request(env, windowsRequest);
        break;
    default: /* ? */
fprintf(stderr, "Unknown Request type %d", type);
        err = -EINVAL;
        break;
    }
    
    if (err) {
        (*env)->CallVoidMethod(env, windowsRequest, setError, err);
    }
    
    (*env)->CallVoidMethod(env, windowsRequest, setCompleted, JNI_TRUE);
}
