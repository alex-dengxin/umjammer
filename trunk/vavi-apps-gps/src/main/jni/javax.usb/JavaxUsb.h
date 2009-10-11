/* 
 * Copyright (c) 2003 Dan Streetman (ddstreet@ieee.org)
 * Copyright (c) 2003 International Business Machines Corporation
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/CPLv1.0.htm
 */

#ifndef _JAVAXUSB_H
#define _JAVAXUSB_H

#include "vavi_jusb_os_win32_JavaxUsb.h"

#include <windows.h>
#include <basetyps.h>
#include <winioctl.h>
#include <usbioctl.h>
#include <uusbd.h>
#include <setupapi.h>
#include <string.h>
#include <errno.h>

/* These must match the defines in JavaxUsb.java */
#define SPEED_UNKNOWN vavi_jusb_os_win32_JavaxUsb_SPEED_UNKNOWN
#define SPEED_LOW vavi_jusb_os_win32_JavaxUsb_SPEED_LOW
#define SPEED_FULL vavi_jusb_os_win32_JavaxUsb_SPEED_FULL

//FIXME - fix this
#define SETUP_FAKE_DEV_DESC(desc) do {\
  desc.bLength = 18; \
  desc.bDescriptorType = 1; \
  desc.bcdUSB = 0x0101; \
  desc.bDeviceClass = 0x09; \
  desc.bDeviceSubClass = 0; \
  desc.bDeviceProtocol = 0; \
  desc.bMaxPacketSize0 = 16; \
  desc.idVendor = 0xffff; \
  desc.idProduct = 0xffff; \
  desc.bcdDevice = 0x0101; \
  desc.iManufacturer = 0; \
  desc.iProduct = 0; \
  desc.iSerialNumber = 0; \
  desc.bNumConfigurations = 0; \
  } while (0)

#endif /* _JAVAXUSB_H */

#ifdef DEFINE_GUID

/* JavaxUsb GUID */
// {136E983A-096C-49bb-A6C6-E608A0A0CDBC}
DEFINE_GUID(GUID_DEVINTERFACE_JAVAXUSB, 
0x69910468, 0x8802, 0x11d3, 0xAB, 0xC7, 0xA7, 0x56, 0xB2, 0xFD, 0xFB, 0x29);
//0x136e983a, 0x96c, 0x49bb, 0xa6, 0xc6, 0xe6, 0x8, 0xa0, 0xa0, 0xcd, 0xbc);

#endif /* DEFINE_GUID */
