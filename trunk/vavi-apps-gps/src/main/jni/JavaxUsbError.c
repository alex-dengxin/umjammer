/**
 * Copyright (c) 2003 Dan Streetman (ddstreet@ieee.org)
 * Copyright (c) 2003 International Business Machines Corporation
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/CPLv1.0.htm
 */

#include "JavaxUsb.h"

JNIEXPORT
jstring JNICALL Java_vavi_jusb_os_win32_JavaxUsb_nativeGetErrorMessage(JNIEnv *env, jclass JavaxUsb, jint error) {
    jstring jstr = NULL;

    jstr = (*env)->NewStringUTF(env, "Not implemented");

printf("Native get-error method!\n");

    return jstr;
}

/* */
