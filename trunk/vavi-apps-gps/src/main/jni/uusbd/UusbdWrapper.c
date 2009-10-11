/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

#include <uusbd.h>
#include "vavi_uusbd_Usb.h"
#include "vavi_uusbd_Pipe.h"

/**
 * uusbd.dll ラッパ
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030314	nsano	initial version <br>
 */

//-----------------------------------------------------------------------------

/** */
#define USB_EXCEPTION "vavi/uusbd/UsbException"

/** */
#define getUsbHandle(env,obj) getHandle((env),(obj),"instance")
/** */
#define getPipeHandle(env,obj) getHandle((env),(obj),"instance")
/** */
#define getPipeUsbHandle(env,obj) getHandle((env),(obj),"usbInstance")

/** */
#define getInterfaceNo(env,obj) getIntField((env),(obj),"interfaceNo")
/** */
#define getPipeNo(env,obj) getIntField((env),(obj),"pipeNo")

/** */
#define getOverlap(env,obj) (isOverlapped((env),(obj))==JNI_TRUE?NULL:NULL)

/**
 * ハンドルを取得します。
 */
static jlong getHandle(JNIEnv *env, jobject obj, char *name) {
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID field = (*env)->GetFieldID(env, class, name, "J");
    return (jlong)(*env)->GetLongField(env, obj, field);
}

/**
 * int 型のフィールド値を取得します。
 */
static jint getIntField(JNIEnv *env, jobject obj, char *name) {
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID field = (*env)->GetFieldID(env, class, name, "I");
    return (jint)(*env)->GetLongField(env, obj, field);
}

/**
 * を取得します。
 */
static jboolean isOverlapped(JNIEnv *env, jobject obj) {
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID field = (*env)->GetFieldID(env, class, "overlap", "Z");
    return (jboolean)(*env)->GetBooleanField(env, obj, field);
}

/**
 * 例外を投げます。
 */
static void throwException(JNIEnv *env, char *exception, char *_message) {

    jclass class = (*env)->FindClass(env, exception);
    (*env)->ThrowNew(env, class, _message);
}

/**
 * 例外を投げます。
 */
static void throwExceptionWithIntMessage(JNIEnv *env,
                                         char *exception,
                                         int _message) {
    char _buf[64];
    sprintf(_buf, "%d", _message);

    throwException(env, exception, _buf);
}

/**
 * メッセージを出力します。
 * @param _message UTF-8 を保障してください
 */
static void debug(JNIEnv *env, const char *_message) {

    jclass class = (*env)->FindClass(env, "vavi/util/Debug");
    jmethodID mid = (*env)->GetStaticMethodID(env,
                                              class,
                                              "println",
                                              "(Ljava/lang/Object;)V");
    jstring message = (*env)->NewStringUTF(env, _message);
    (*env)->CallStaticVoidMethod(env, class, mid, message);
}

/**
 * メッセージを出力します。
 * @param _message UTF-8 を保障してください
 * @param _arg
 */
static void debug_I(JNIEnv *env, const char *_message, int _arg) {

    char buf[256];
    sprintf(buf, "%s: %ld", _message, _arg);
    debug(env, buf);
}

//-----------------------------------------------------------------------------

/**
 *
 * @param husb
 * @param if_num
 * @param pipe_num
 */
USHORT getMaxPacketLength(HUSB husb, DWORD if_num, DWORD pipe_num) {
    BOOL ok;
    USB_CONFIGURATION_DESCRIPTOR conf;
    PUSB_ENDPOINT_DESCRIPTOR endp_descriptor;
    // まずコンフィグレーションディスクリプターのみ得て全体の大きさを知る
    ok = Uusbd_GetConfigurationDescriptor(husb, (char*) &conf, sizeof(conf));
    if (!ok) {
        return 0;
    }
    DWORD len = conf.wTotalLength;
    char *buf = (char*) malloc(len);
    // 全体を得る
    ok = Uusbd_GetConfigurationDescriptor(husb, buf, len);
    if (!ok) {
        return 0;
    }

    char *p = buf;
    USHORT max_len = 0;
    DWORD pipe_count = 0;
    ok = FALSE;
    while (len > 0) {
        // interface descriptor
        if (p[1] == USB_INTERFACE_DESCRIPTOR_TYPE){
            if ((unsigned) p[2] == if_num) {
                ok = TRUE;
            } else {
                ok = FALSE;
            }
            pipe_count = 0;
        }
        // endpoint descriptor
        if (p[1] == USB_ENDPOINT_DESCRIPTOR_TYPE && ok) {
            if (pipe_count == pipe_num) { // found !!
                endp_descriptor = (PUSB_ENDPOINT_DESCRIPTOR)p;
                max_len = endp_descriptor->wMaxPacketSize;
                break;
            }
            pipe_count++; // fix Ver1.1
        }
        p += p[0];
        len -= p[0];
    }
    free(buf);
    return max_len;
}

//-----------------------------------------------------------------------------

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    open
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Usb_open__(JNIEnv *env, jobject obj) {

    HUSB _usbHandle = Uusbd_Open();

debug_I(env, "_usbHandle", _usbHandle);
    if (_usbHandle == INVALID_HANDLE_VALUE) {
        throwException(env, USB_EXCEPTION, "device not found.");
    } else {
        jclass class = (*env)->GetObjectClass(env, obj);
        jfieldID field = (*env)->GetFieldID(env, class, "instance", "J");
        (*env)->SetLongField(env, obj, field, (jlong)_usbHandle);
    }
}

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    open
 * Signature: (IIIIIB)V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Usb_open__IIIIIB(JNIEnv *env, jobject obj, jint flag, jint clazz, jint subClass, jint vendor, jint product, jbyte bcdDevice) {

    ULONG  _flag = (ULONG) flag;
    UCHAR  _class = (UCHAR) clazz;
    UCHAR  _subClass = (UCHAR) subClass;
    USHORT _vendor = (USHORT) vendor;
    USHORT _product = (USHORT) product;
    BYTE   _bcdDevice = (BYTE) bcdDevice;

    HUSB _usbHandle = Uusbd_Open_mask(_flag, _class, _subClass, _vendor, _product, _bcdDevice);
debug_I(env, "_usbHandle", _usbHandle);
    if (_usbHandle == INVALID_HANDLE_VALUE) {
        throwException(env, USB_EXCEPTION, "device not found.");
    } else {
        jclass class = (*env)->GetObjectClass(env, obj);
        jfieldID field = (*env)->GetFieldID(env, class, "instance", "J");
        (*env)->SetLongField(env, obj, field, (jlong)_usbHandle);
    }
}

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Usb_close(JNIEnv *env, jobject obj) {

    long _usbHandle = getUsbHandle(env, obj);

    Uusbd_Close((HUSB)_usbHandle);
}

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    sendClassRequest
 * Signature: (ZIIIII[B)V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Usb_sendClassRequest(JNIEnv *env, jobject obj, jboolean in, jint recipient, jint request, jint value, jint index, jint length, jbyteArray data) {

    long _usbHandle = getUsbHandle(env, obj);

    BOOL _in = (in == JNI_TRUE) ? TRUE : FALSE;
    UCHAR _recipient = (UCHAR) recipient;
    UCHAR _request = (UCHAR) request;
    USHORT _value = (USHORT) value;
    USHORT _index = (USHORT) index;
    USHORT _length = (USHORT) length;
    jboolean dummy;
    jbyte *buf = (*env)->GetByteArrayElements(env, data, &dummy);

/*
fprintf(stderr, "in: %ld\n", _in);
fprintf(stderr, "recipient: %ld\n", _recipient);
fprintf(stderr, "request: %ld\n", _request);
fprintf(stderr, "value: %ld\n", _value);
fprintf(stderr, "index: %ld\n", _index);
fprintf(stderr, "length: %ld\n", _length);
int j;
for (j = 0; j < _length; j++) {
fprintf(stderr, "%02X ", buf[j]);
}
fprintf(stderr, "\n");
fflush(stderr);
*/
    BOOL result = Uusbd_ClassRequest((HUSB) _usbHandle,
                                     _in,
                                     _recipient,
                                     _request,
                                     _value,
                                     _index,
                                     _length,
                                     buf);

    if (result == FALSE) {
        throwException(env, USB_EXCEPTION, "class request");
    }

    (*env)->ReleaseByteArrayElements(env, data, buf, 0);
}

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    sendVendorRequest
 * Signature: (ZIIIII[B)V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Usb_sendVendorRequest(JNIEnv *env, jobject obj, jboolean in, jint recipient, jint request, jint value, jint index, jint length, jbyteArray data) {

    long _usbHandle = getUsbHandle(env, obj);

    BOOL _in = (in == JNI_TRUE) ? TRUE : FALSE;
    UCHAR _recipient = (UCHAR) recipient;
    UCHAR _request = (UCHAR) request;
    USHORT _value = (USHORT) value;
    USHORT _index = (USHORT) index;
    USHORT _length = (USHORT) length;
    jboolean dummy;
    jbyte *buf = (*env)->GetByteArrayElements(env, data, &dummy);

    BOOL result = Uusbd_VendorRequest((HUSB) _usbHandle,
                                      _in,
                                      _recipient,
                                      _request,
                                      _value,
                                      _index,
                                      _length,
                                      buf);

    if (result == FALSE) {
        throwException(env, USB_EXCEPTION, "vendor request");
    }

    (*env)->ReleaseByteArrayElements(env, data, buf, 0);
}

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    available
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_vavi_uusbd_Usb_available(JNIEnv *env, jobject obj) {

    long _usbHandle = getUsbHandle(env, obj);

    int result = Uusbd_Check((HUSB) _usbHandle);

    switch (result) {
    case UU_CHECK_OK:
        return JNI_TRUE;
    case UU_CHECK_NODEVICE:
        return JNI_FALSE;
    case UU_CHECK_NOTOPEN:
    default:
        throwExceptionWithIntMessage(env, USB_EXCEPTION, result);
        return JNI_FALSE;
    }
}

/**
 *
 * Class:     vavi_uusbd_Usb
 * Method:    reset
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Usb_reset(JNIEnv *env, jobject obj) {

    long _usbHandle = getUsbHandle(env, obj);

    BOOL result = Uusbd_ResetDevice((HUSB) _usbHandle);

    if (result == FALSE) {
        throwExceptionWithIntMessage(env, USB_EXCEPTION, result);
    }
}

//-----------------------------------------------------------------------------

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    open
 * Signature: (JIIZ)V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Pipe_open(JNIEnv *env, jobject obj, jlong usbHandle, jint interfaceNo, jint pipeNo, jboolean overlap) {

    UCHAR _interfaceNo = (UCHAR) interfaceNo;
    UCHAR _pipeNo = (UCHAR) pipeNo;

    HANDLE _pipeHandle;
    if (overlap == JNI_TRUE) {
//fprintf(stderr, "usb: %ld\n", usbHandle);
        _pipeHandle = Uusbd_OpenPipe_Overlapped((HUSB) usbHandle, _interfaceNo, _pipeNo);
    }
    else {
        _pipeHandle = Uusbd_OpenPipe((HUSB) usbHandle, _interfaceNo, _pipeNo);
    }

    if (_pipeHandle == NULL) {
        throwExceptionWithIntMessage(env, USB_EXCEPTION, -1);
    }
    else {
//fprintf(stderr, "pipe: %ld\n", _pipeHandle);
//fflush(stderr);
        jclass class = (*env)->GetObjectClass(env, obj);
        jfieldID field = (*env)->GetFieldID(env, class, "instance", "J");
        (*env)->SetLongField(env, obj, field, (jlong) _pipeHandle);

        field = (*env)->GetFieldID(env, class, "overlap", "Z");
        (*env)->SetLongField(env, obj, field, overlap);
    }

    Java_vavi_uusbd_Pipe_reset(env, obj);
}

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    reset
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Pipe_reset(JNIEnv *env, jobject obj) {

    long _pipeHandle = getPipeHandle(env, obj);

    BOOL result = Uusbd_ResetPipe((HANDLE) _pipeHandle);

    if (result == FALSE) {
        throwExceptionWithIntMessage(env, USB_EXCEPTION, result);
    }
}

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    abort
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Pipe_abort(JNIEnv *env, jobject obj) {

    long _pipeHandle = getPipeHandle(env, obj);

    BOOL result = Uusbd_AbortPipe((HANDLE)_pipeHandle);

    if (result == FALSE) {
        throwExceptionWithIntMessage(env, USB_EXCEPTION, result);
    }
}

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Pipe_close(JNIEnv *env, jobject obj) {

    long _pipeHandle = getPipeHandle(env, obj);

    CloseHandle((HANDLE)_pipeHandle);
}

/**
 * Class:     vavi_uusbd_Pipe
 * Method:    read
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_vavi_uusbd_Pipe_read__(JNIEnv *env, jobject obj) {

    long _pipeHandle = getPipeHandle(env, obj);
//fprintf(stderr, "pipe: %ld\n", _pipeHandle);
//fflush(stderr);
    char _buf[1];
    int _size;

    BOOL result = ReadFile((HANDLE)_pipeHandle,
                           _buf,
                           1,
                           &_size,
                           getOverlap(env, obj));

//fprintf(stderr, "result: %d, %d\n", result, _size);
//fflush(stderr);
    if (result == FALSE || _size == 0) {
        return -1;
    }

    return _buf[0];
}

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    read
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_vavi_uusbd_Pipe_read___3BII(JNIEnv *env, jobject obj, jbyteArray b, jint off, jint len) {

    long _usbHandle = getPipeUsbHandle(env, obj);
    int _interfaceNo = getInterfaceNo(env, obj);
    int _pipeNo = getPipeNo(env, obj);

    int _packetLength = getMaxPacketLength(_usbHandle, _interfaceNo, _pipeNo);
    if (_packetLength == 0) {
        throwException(env, USB_EXCEPTION, "bad packet length");
        return -1;
    }
//fprintf(stderr, "max packet size: %d\n", _packetLength);
//fflush(stderr);

    int _totalSize = 0; 

    jboolean dummy;
    jbyte *buf = (*env)->GetByteArrayElements(env, b, &dummy);

    long _pipeHandle = getPipeHandle(env, obj);
//fprintf(stderr, "pipe: %ld\n", _pipeHandle);
//fflush(stderr);
    char *_buf = (char*) malloc(_packetLength + 1);

    while (_totalSize < len) {
        int _size;
        memset(_buf, 0, _packetLength + 1);

        BOOL result = ReadFile((HANDLE) _pipeHandle,
                               _buf,
                               _packetLength,
                               &_size,
                               getOverlap(env, obj));

//fprintf(stderr, "result: %d, %d\n", result, _size);
//fflush(stderr);
        if (result == FALSE || _size == 0) {
            (*env)->ReleaseByteArrayElements(env, b, buf, 0);
            free(_buf);
            return -1;
        } else {
            int _offset = off + _totalSize;
            int _length = min(len - _totalSize, _size);
/*
fprintf(stderr, "offset: %d, %d\n", _offset, _length);
int j;
for (j = 0; j < _packetLength; j++) {
fprintf(stderr, "%02X ", _buf[j]);
}
fprintf(stderr, "\n");
fflush(stderr);
*/
            memcpy(buf + _offset, _buf, _length);
            (*env)->SetByteArrayRegion(env, b, _offset, _length, buf);
            _totalSize += _size;
        }
    }

    (*env)->ReleaseByteArrayElements(env, b, buf, 0);
    free(_buf);
    return _totalSize;
}

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Pipe_write__I(JNIEnv *env, jobject obj, jint b) {

    long _pipeHandle = getPipeHandle(env, obj);
//fprintf(stderr, "pipe: %ld\n", _pipeHandle);
//fflush(stderr);
    char _buf[1];
    int _size;

    _buf[0] = b;

    BOOL result = WriteFile((HANDLE)_pipeHandle,
                            _buf,
                            1,
                            &_size,
                            getOverlap(env, obj));

//fprintf(stderr, "result: %d, %d\n", result, _size);
//fflush(stderr);
    if (result == FALSE || _size == 0) {
        throwException(env, USB_EXCEPTION, "write(I)V");
    }
}

/**
 *
 * Class:     vavi_uusbd_Pipe
 * Method:    write
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_vavi_uusbd_Pipe_write___3BII(JNIEnv *env, jobject obj, jbyteArray b, jint off, jint len) {

    long _usbHandle = getPipeUsbHandle(env, obj);
    int _interfaceNo = getInterfaceNo(env, obj);
    int _pipeNo = getPipeNo(env, obj);

    int _packetLength = getMaxPacketLength(_usbHandle, _interfaceNo, _pipeNo);
    if (_packetLength == 0) {
        throwException(env, USB_EXCEPTION, "bad packet length");
        return;
    }
//fprintf(stderr, "max packet size: %d\n", _packetLength);
//fflush(stderr);

    int _totalSize = 0; 

    long _pipeHandle = getPipeHandle(env, obj);
//fprintf(stderr, "pipe: %ld\n", _pipeHandle);
    jboolean dummy;
    jbyte *buf = (*env)->GetByteArrayElements(env, b, &dummy);
    char *_buf = (char*) malloc(_packetLength + 1);
    char *p = buf;

    while (_totalSize < len) {
        int _size;
        memset(_buf, 0, _packetLength + 1);
        memcpy(_buf, p, min(len - _totalSize, _packetLength));
//fprintf(stderr, "[%s] %d\n", _buf, min(len - _totalSize, _packetLength));
//int j;
//for (j = 0; j < _packetLength; j++) {
//fprintf(stderr, "%02X ", _buf[j]);
//}
//fprintf(stderr, "\n");

        BOOL result = WriteFile((HANDLE)_pipeHandle,
                                _buf,
                                _packetLength,
                                &_size,
                                getOverlap(env, obj));
//fprintf(stderr, "result: %d, %d\n", result, _size);
//fflush(stderr);
        if (result == FALSE || _size == 0) {
            (*env)->ReleaseByteArrayElements(env, b, buf, 0);
            throwException(env, USB_EXCEPTION, "write([BII)V");
            free(_buf);
            return;
        }
        else {
            _totalSize += _size;
            p += _size;
        }
    }

    (*env)->ReleaseByteArrayElements(env, b, buf, 0);
    free(_buf);
}

/* */
