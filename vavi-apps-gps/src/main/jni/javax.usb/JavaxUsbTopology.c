/*
 * Copyright (c) 2003 Dan Streetman (ddstreet@ieee.org)
 * Copyright (c) 2003 International Business Machines Corporation
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/CPLv1.0.htm
 */

#include "JavaxUsb.h"

static PCHAR convert_wide_str(PWCHAR wide_str);

static HANDLE hc_to_rh(JNIEnv *env, jclass JavaxUsb, HANDLE hc_dev, PCHAR *prh_name);
static HANDLE get_hub_handle(JNIEnv *env, jclass JavaxUsb, HANDLE hub, PCHAR *pname);

static PUSB_COMMON_DESCRIPTOR get_next_desc(UCHAR *buffer, USHORT *remaining);

static int get_config_desc(HANDLE hubdev,
                           int port,
                           UCHAR config,
                           PUSB_CONFIGURATION_DESCRIPTOR *desc);

static int enumerate_hub(JNIEnv *env,
                         jclass JavaxUsb,
                         jobject windowsUsbServices,
                         jobject hub,
                         HANDLE hubdev);

/**
 *
 */
static int build_endpoint(JNIEnv *env,
                          jclass JavaxUsb,
                          jobject windowsUsbServices,
                          jobject usbInterfaceImp,
                          PUSB_ENDPOINT_DESCRIPTOR ep_desc) {

    jobject usbEndpointImp = NULL;

    jmethodID createUsbEndpointImp =
        (*env)->GetStaticMethodID(env,
                                  JavaxUsb,
                                  "createUsbEndpointImp",
                                  "(Lcom/ibm/jusb/UsbInterfaceImp;BBBBBS)Lcom/ibm/jusb/UsbEndpointImp;");

fprintf(stderr, "Start build_endpoint for endpoint 0x%x\n", ep_desc->bEndpointAddress);

    usbEndpointImp =
        (*env)->CallStaticObjectMethod(env,
                                       JavaxUsb,
                                       createUsbEndpointImp,
                                       usbInterfaceImp,
                                       ep_desc->bLength,
                                       ep_desc->bDescriptorType,
                                       ep_desc->bEndpointAddress,
                                       ep_desc->bmAttributes,
                                       ep_desc->bInterval,
                                       ep_desc->wMaxPacketSize);

    (*env)->DeleteLocalRef(env, usbEndpointImp);

fprintf(stderr, "End build_endpoint for endpoint 0x%x\n", ep_desc->bEndpointAddress);

    return 0;
}

/**
 *
 */
static int build_interface(JNIEnv *env,
                           jclass JavaxUsb,
                           jobject windowsUsbServices,
                           jobject usbConfigurationImp,
                           jobject *pinterface,
                           PUSB_INTERFACE_DESCRIPTOR iface_desc) {

    // FIXME - implement getting active alternate setting
    jboolean is_active = (0 == iface_desc->bAlternateSetting ? JNI_TRUE : JNI_FALSE);

    jmethodID createUsbInterfaceImp =
        (*env)->GetStaticMethodID(env,
                                  JavaxUsb,
                                  "createUsbInterfaceImp",
                                  "(Lcom/ibm/jusb/UsbConfigurationImp;BBBBBBBBBZ)Lcom/ibm/jusb/UsbInterfaceImp;");

fprintf(stderr, "Start build_interface for interface %d setting %d, is%s active.\n", iface_desc->bInterfaceNumber, iface_desc->bAlternateSetting, (JNI_TRUE == is_active ? "" : " not"));

    *pinterface =
        (*env)->CallStaticObjectMethod(env,
                                       JavaxUsb,
                                       createUsbInterfaceImp,
                                       usbConfigurationImp,
                                       iface_desc->bLength,
                                       iface_desc->bDescriptorType,
                                       iface_desc->bInterfaceNumber,
                                       iface_desc->bAlternateSetting,
                                       iface_desc->bNumEndpoints,
                                       iface_desc->bInterfaceClass,
                                       iface_desc->bInterfaceSubClass,
                                       iface_desc->bInterfaceProtocol,
                                       iface_desc->iInterface,
                                       is_active);

fprintf(stderr, "End build_interface for interface %d setting %d.\n", iface_desc->bInterfaceNumber, iface_desc->bAlternateSetting);

    return 0;
}

/**
 *
 */
static int build_configuration(JNIEnv *env, jclass JavaxUsb, jobject windowsUsbServices, jobject usbDeviceImp, PUSB_CONFIGURATION_DESCRIPTOR config_desc, jboolean is_active) {

    int ret = 0;
    jobject usbConfigurationImp = NULL;
    jobject usbInterfaceImp = NULL;
    PUSB_COMMON_DESCRIPTOR desc = (PUSB_COMMON_DESCRIPTOR) config_desc;
    PUSB_INTERFACE_DESCRIPTOR iface_desc = NULL;
    PUSB_ENDPOINT_DESCRIPTOR ep_desc = NULL;
    USHORT wTotalLength = config_desc->wTotalLength;

    jmethodID createUsbConfigurationImp =
        (*env)->GetStaticMethodID(env,
                                  JavaxUsb,
                                  "createUsbConfigurationImp",
                                  "(Lcom/ibm/jusb/UsbDeviceImp;BBSBBBBBZ)Lcom/ibm/jusb/UsbConfigurationImp;");

fprintf(stderr, "Start build_configuration for configuration %d, is%s active.\n", config_desc->bConfigurationValue, (JNI_TRUE == is_active ? "" : " not"));

    usbConfigurationImp =
        (*env)->CallStaticObjectMethod(env,
                                       JavaxUsb,
                                       createUsbConfigurationImp,
                                       usbDeviceImp,
                                       config_desc->bLength,
                                       config_desc->bDescriptorType,
                                       config_desc->wTotalLength,
                                       config_desc->bNumInterfaces,
                                       config_desc->bConfigurationValue,
                                       config_desc->iConfiguration,
                                       config_desc->bmAttributes,
                                       config_desc->MaxPower,
                                       is_active);

    while (desc) {
fprintf(stderr, "Parsing descriptors, %d bytes left.\n", wTotalLength);
        if (!(desc = get_next_desc((UCHAR*) desc, &wTotalLength))) {
fprintf(stderr, "No more descriptors.\n");
            break;
        }

        switch (desc->bDescriptorType) {
        case USB_DEVICE_DESCRIPTOR_TYPE:
fprintf(stderr, "Unexpected device descriptor in configuration descriptor!  Ignoring.\n");
            break;

        case USB_CONFIGURATION_DESCRIPTOR_TYPE:
fprintf(stderr, "Unexpected configuration descriptor in configuration descriptor!  Ignoring.\n");
            break;

        case USB_INTERFACE_DESCRIPTOR_TYPE:
fprintf(stderr, "Found interface descriptor.\n");
            if (usbInterfaceImp) {
                (*env)->DeleteLocalRef(env, usbInterfaceImp);
                usbInterfaceImp = NULL;
            }
            iface_desc = (PUSB_INTERFACE_DESCRIPTOR)desc;
            if ((ret = build_interface(env, JavaxUsb, windowsUsbServices, usbConfigurationImp, &usbInterfaceImp, iface_desc))) {
fprintf(stderr, "Could not build interface %d setting %d : %d\n", iface_desc->bInterfaceNumber, iface_desc->bAlternateSetting, ret);
                goto BUILD_CONFIGURATION_EXIT;
            }
            iface_desc = NULL;
            break;

        case USB_ENDPOINT_DESCRIPTOR_TYPE:
            if (!usbInterfaceImp) {
fprintf(stderr, "Found endpoint descriptor before interface descriptor!");
                ret = -EINVAL;
                goto BUILD_CONFIGURATION_EXIT;
            }
fprintf(stderr, "Found endpoint descriptor.\n");
            ep_desc = (PUSB_ENDPOINT_DESCRIPTOR)desc;
            if ((ret = build_endpoint(env, JavaxUsb, windowsUsbServices, usbInterfaceImp, ep_desc))) {
fprintf(stderr, "Could not build endpoint 0x%x : %d\n", ep_desc->bEndpointAddress, ret);
                goto BUILD_CONFIGURATION_EXIT;
            }
            ep_desc = NULL;
            break;

        default:
fprintf(stderr, "Unrecognized descriptor type %d (length %d), ignoring.\n", desc->bDescriptorType, desc->bLength);
            break;
        }
    }
fprintf(stderr, "Done parsing descriptors.\n");

BUILD_CONFIGURATION_EXIT:
    if (usbConfigurationImp) { (*env)->DeleteLocalRef(env, usbConfigurationImp); }
    if (usbInterfaceImp) { (*env)->DeleteLocalRef(env, usbInterfaceImp); }

fprintf(stderr, "End build_configuration for configuration %d.\n", config_desc->bConfigurationValue);

    return ret;
}

/**
 *
 */
static int build_device(JNIEnv *env,
                        jclass JavaxUsb,
                        jobject windowsUsbServices,
                        HANDLE hubdev,
                        int port,
                        jobject usbDeviceImp,
                        PUSB_DEVICE_DESCRIPTOR dev_desc,
                        const UCHAR active_config,
                        const int speed) {
    int ret = 0, i;
    PUSB_CONFIGURATION_DESCRIPTOR config_desc = NULL;

    jmethodID configureUsbDeviceImp = (*env)->GetStaticMethodID(env, JavaxUsb, "configureUsbDeviceImp", "(Lcom/ibm/jusb/UsbDeviceImp;BBBBBBBBBBSSSSI)V");

    (*env)->CallStaticVoidMethod(env,
                                 JavaxUsb,
                                 configureUsbDeviceImp,
                                 usbDeviceImp,
                                 dev_desc->bLength,
                                 dev_desc->bDescriptorType,
                                 dev_desc->bDeviceClass,
                                 dev_desc->bDeviceSubClass,
                                 dev_desc->bDeviceProtocol,
                                 dev_desc->bMaxPacketSize0,
                                 dev_desc->iManufacturer,
                                 dev_desc->iProduct,
                                 dev_desc->iSerialNumber,
                                 dev_desc->bNumConfigurations,
                                 dev_desc->idVendor,
                                 dev_desc->idProduct,
                                 dev_desc->bcdDevice,
                                 dev_desc->bcdUSB,
                                 speed);

fprintf(stderr, "Building %d configurations.\n", dev_desc->bNumConfigurations);
    for (i = 0; !ret && i < dev_desc->bNumConfigurations; i++) {
        if ((ret = get_config_desc(hubdev, port, (UCHAR) i, &config_desc))) {
fprintf(stderr, "Could not get configuration %d descriptor : %d\n", i, ret);
        } else {
            UCHAR config_val = config_desc->bConfigurationValue;
            jboolean is_active = (active_config == config_val ? JNI_TRUE : JNI_FALSE);
fprintf(stderr, "Building configuration %d, is%s active.\n", config_val, (JNI_TRUE == is_active ? "" : " not"));
            if ((ret = build_configuration(env, JavaxUsb, windowsUsbServices, usbDeviceImp, config_desc, is_active)))
fprintf(stderr, "Error building configuration %d : %d\n", i, ret);
        }

        if (config_desc) { GlobalFree(config_desc); }
        config_desc = NULL;
    }

    return ret;
}

/**
 * @param windowsUsbServices
 * @param hub
 * @param port
 */
static int build_connected_device(JNIEnv *env, jclass JavaxUsb, jobject windowsUsbServices, jobject hub, HANDLE hubdev, int port) {
    int ret = 0;
    ULONG size = 0;
    PUSB_NODE_CONNECTION_INFORMATION connection = NULL;
    PUSB_NODE_CONNECTION_NAME name = NULL;
//  PUSB_DEVICE_DESCRIPTOR dev_desc = NULL;
    HANDLE dev = INVALID_HANDLE_VALUE;
    PCHAR hubNodeName = NULL, full_hubNodeName = NULL;
    jobject usbDeviceImp = NULL;
    jstring str = (*env)->NewStringUTF(env, "FIXME - use a key");
    int speed = SPEED_UNKNOWN;

    jmethodID createUsbHubImp = (*env)->GetStaticMethodID(env, JavaxUsb, "createUsbHubImp", "(Ljava/lang/String;)Lcom/ibm/jusb/UsbHubImp;");
//printf("createUsbHubImp: %d\n", createUsbHubImp);
    jmethodID createUsbDeviceImp = (*env)->GetStaticMethodID(env, JavaxUsb, "createUsbDeviceImp", "(Ljava/lang/String;)Lcom/ibm/jusb/UsbDeviceImp;");
//printf("createUsbDeviceImp: %d\n", createUsbDeviceImp);
    jmethodID connectUsbDeviceImp = (*env)->GetStaticMethodID(env, JavaxUsb, "connectUsbDeviceImp", "(Lcom/ibm/jusb/UsbHubImp;ILcom/ibm/jusb/UsbDeviceImp;)V");
//printf("connectUsbDeviceImp: %d\n", connectUsbDeviceImp);

    connection = GlobalAlloc(GPTR, sizeof(*connection));

    if (!connection || !str) {
fprintf(stderr, "Out of memory!\n");
        ret = -ENOMEM;
        goto BUILD_CONNECTED_DEVICE_EXIT;
    }

    connection->ConnectionIndex = port;

fprintf(stderr, "Getting connection information for device on port %d\n", port);
    if (!DeviceIoControl(hubdev, IOCTL_USB_GET_NODE_CONNECTION_INFORMATION, connection, sizeof(*connection), connection, sizeof(*connection), &size, NULL)) {
        ret = GetLastError();
fprintf(stderr, "Could not get connection for port %d : %ld\n", port, (long) ret);
        goto BUILD_CONNECTED_DEVICE_EXIT;
    }

fprintf(stderr, "status: %d, %d (%d)\n", connection->ConnectionStatus, DeviceConnected, NoDeviceConnected);
//    if (DeviceConnected != connection->ConnectionStatus) {
//printf("No device connection on port %d\n", port);
//        if (NoDeviceConnected != connection->ConnectionStatus) {
//printf("ConnectionStatus is error %d\n", connection->ConnectionStatus);
//        }
//        ret = -ENODEV;
//        goto BUILD_CONNECTED_DEVICE_EXIT;
//    }

    if (connection->DeviceIsHub) {
fprintf(stderr, "Creating UsbHubImp\n");
        usbDeviceImp = (*env)->CallStaticObjectMethod(env, JavaxUsb, createUsbHubImp, str);
    } else {
fprintf(stderr, "Creating UsbDeviceImp\n");
        usbDeviceImp = (*env)->CallStaticObjectMethod(env, JavaxUsb, createUsbDeviceImp, str);
    }

    if (connection->LowSpeed) {
        speed = SPEED_LOW;
    } else {
        speed = SPEED_FULL;
    }

    if (!build_device(env, JavaxUsb, windowsUsbServices, hubdev, port, usbDeviceImp, &connection->DeviceDescriptor, (const UCHAR)connection->CurrentConfigurationValue, speed)) {
        (*env)->CallStaticVoidMethod(env, JavaxUsb, connectUsbDeviceImp, hub, port, usbDeviceImp);
    }

    if (connection->DeviceIsHub) {
        int name_size = sizeof(*name) + 1024;
        name = GlobalAlloc(GPTR, name_size);

fprintf(stderr, "Device is Hub, trying to enumerate ports.\n");

        if (!name) {
fprintf(stderr, "Out of memory!\n");
            ret = -ENOMEM;
            goto BUILD_CONNECTED_DEVICE_EXIT;
        }

        memset(name, 0, name_size);
        name->ConnectionIndex = port;

        // FIXME - fix this crap, both real hubs and HCDs should use
        // a common method of getting the node name, including putting
        // the stupid \\.\ prefix on the name, and creatin the handle

        if (!DeviceIoControl(hubdev, IOCTL_USB_GET_NODE_CONNECTION_NAME, name, name_size, name, name_size, &size, NULL)) {
            ret = GetLastError();
fprintf(stderr, "Could not get hub node name : %ld\n", (long) ret);
            goto BUILD_CONNECTED_DEVICE_EXIT;
        }

        hubNodeName = convert_wide_str(name->NodeName);

        if (!(full_hubNodeName = GlobalAlloc(GPTR, strlen("\\\\.\\") + strlen(hubNodeName) + 1))) {
fprintf(stderr, "Out of memory!\n");
            ret = -ENOMEM;
            goto BUILD_CONNECTED_DEVICE_EXIT;
        }

        strcpy(full_hubNodeName, "\\\\.\\");
        strcat(full_hubNodeName, hubNodeName);

fprintf(stderr, "Hub node name %s\n", full_hubNodeName);

        dev = CreateFile(full_hubNodeName, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);

        if (INVALID_HANDLE_VALUE == dev) {
            ret = -EINVAL;
fprintf(stderr, "Hub node name %s not valid.\n" ,full_hubNodeName);
            goto BUILD_CONNECTED_DEVICE_EXIT;
        } else {
fprintf(stderr, "Got handle for hub %s\n", full_hubNodeName);
        }

        enumerate_hub(env, JavaxUsb, windowsUsbServices, usbDeviceImp, dev);
    }

BUILD_CONNECTED_DEVICE_EXIT:
    if (str) { (*env)->DeleteLocalRef(env, str); }
    if (usbDeviceImp) { (*env)->DeleteLocalRef(env, usbDeviceImp); }
    if (hubNodeName) { GlobalFree(hubNodeName); }
    if (full_hubNodeName) { GlobalFree(full_hubNodeName); }
    if (connection) { GlobalFree(connection); }
    if (name) { GlobalFree(name); }
    if (INVALID_HANDLE_VALUE != dev) { CloseHandle(dev); }
    // don't free dev_desc, since it is contained in the connection structure

    return ret;
}

/**
 *
 */
static int enumerate_hub(JNIEnv *env, jclass JavaxUsb, jobject windowsUsbServices, jobject hub, HANDLE hubdev) {
    int ret = 0, ports = 0, i;
    ULONG size = 0;
    PUSB_NODE_INFORMATION node = NULL;

    node = GlobalAlloc(GPTR, sizeof(*node));

    if (!node) {
fprintf(stderr, "Out of memory!\n");
        ret = -ENOMEM;
        goto ENUMERATE_HUB_EXIT;
    }

    memset(node, 0, sizeof(*node));

    if (!DeviceIoControl(hubdev, IOCTL_USB_GET_NODE_INFORMATION, NULL, 0, node, sizeof(*node), &size, NULL)) {
        ret = GetLastError();
fprintf(stderr, "Could not get Node info : %ld\n", (long) ret);
        goto ENUMERATE_HUB_EXIT;
    } else {
        if (UsbHub != node->NodeType) {
fprintf(stderr, "Node type %d is not hub\n", node->NodeType);
            ret = -EINVAL;
            goto ENUMERATE_HUB_EXIT;
        }
    }

    ports = node->u.HubInformation.HubDescriptor.bNumberOfPorts;

fprintf(stderr, "Hub has %d ports.\n", ports);
    for (i = 0; i < ports; i++) {
        build_connected_device(env, JavaxUsb, windowsUsbServices, hub, hubdev, i+1);
    }

ENUMERATE_HUB_EXIT:
    if (node) { GlobalFree(node); }

    return ret;
}

/**
 * Java entry point.
 */
JNIEXPORT jint JNICALL Java_vavi_jusb_os_win32_JavaxUsb_nativeTopologyListener
    (JNIEnv *env, jclass JavaxUsb, jobject windowsUsbServices)
{
#define MAX_HC 4
    int hc_num;
    int ret = 0;
    char hc_name[16], *rh_name = NULL;
    HANDLE hc_dev, rh_dev;

    jmethodID createUsbHubImp, configureUsbDeviceImp, connectUsbDeviceImp;
    jclass WindowsUsbServices = NULL;
    jmethodID getRootUsbHubImp;
    jobject rootUsbHub = NULL;

    createUsbHubImp = (*env)->GetStaticMethodID(env, JavaxUsb, "createUsbHubImp", "(Ljava/lang/String;)Lcom/ibm/jusb/UsbHubImp;");
    configureUsbDeviceImp = (*env)->GetStaticMethodID(env, JavaxUsb, "configureUsbDeviceImp", "(Lcom/ibm/jusb/UsbDeviceImp;BBBBBBBBBBSSSSI)V");
    connectUsbDeviceImp = (*env)->GetStaticMethodID(env, JavaxUsb, "connectUsbDeviceImp", "(Lcom/ibm/jusb/UsbHubImp;ILcom/ibm/jusb/UsbDeviceImp;)V");

    WindowsUsbServices = (*env)->GetObjectClass(env, windowsUsbServices);
    getRootUsbHubImp = (*env)->GetMethodID(env, WindowsUsbServices, "getRootUsbHubImp", "()Lcom/ibm/jusb/UsbHubImp;");
    rootUsbHub = (*env)->CallObjectMethod(env, windowsUsbServices, getRootUsbHubImp);
    (*env)->DeleteLocalRef(env, WindowsUsbServices);

    for (hc_num = 0; hc_num < MAX_HC; hc_num++) {
        sprintf(hc_name, "\\\\.\\HCD%d", hc_num);

        hc_dev = CreateFile(hc_name, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);

        if (INVALID_HANDLE_VALUE == hc_dev) {
fprintf(stderr, "Handle %s not valid.\n", hc_name);
            continue;
        } else {
fprintf(stderr, "Found host controller %s\n", hc_name);
        }

        rh_dev = hc_to_rh(env, JavaxUsb, hc_dev, &rh_name);

        if (INVALID_HANDLE_VALUE == rh_dev) {
fprintf(stderr, "Could not get host controller %d root hub.\n",hc_num);
            ret = -EINVAL;
        } else {
            // FIXME - check ret?
            jobject usbHubImp = NULL;
            jstring str = (*env)->NewStringUTF(env, rh_name);
            // FIXME - get real dev desc
            USB_DEVICE_DESCRIPTOR dev_desc;
            SETUP_FAKE_DEV_DESC(dev_desc);

fprintf(stderr, "Creating UsbHubImp for root hub\n");
            usbHubImp = (*env)->CallStaticObjectMethod(env, JavaxUsb, createUsbHubImp, str);
            (*env)->CallStaticVoidMethod(env, JavaxUsb, configureUsbDeviceImp, usbHubImp,
                                         dev_desc.bLength, dev_desc.bDescriptorType, dev_desc.bDeviceClass, dev_desc.bDeviceSubClass,
                                         dev_desc.bDeviceProtocol, dev_desc.bMaxPacketSize0, dev_desc.iManufacturer, dev_desc.iProduct,
                                         dev_desc.iSerialNumber, dev_desc.bNumConfigurations, dev_desc.idVendor, dev_desc.idProduct,
                                         dev_desc.bcdDevice, dev_desc.bcdUSB, SPEED_UNKNOWN /*FIXME...*/);
            (*env)->CallStaticVoidMethod(env, JavaxUsb, connectUsbDeviceImp, rootUsbHub, hc_num+1, usbHubImp);

fprintf(stderr, "Enumerating root hub's ports.\n");
            ret = enumerate_hub(env, JavaxUsb, windowsUsbServices, usbHubImp, rh_dev);

            CloseHandle(rh_dev);
            (*env)->DeleteLocalRef(env, usbHubImp);
            (*env)->DeleteLocalRef(env, str);
        }

        if (rh_name) { GlobalFree(rh_name); }
        rh_name = NULL;
        CloseHandle(hc_dev);
    }

    if (rootUsbHub) { (*env)->DeleteLocalRef(env, rootUsbHub); }

    return ret;
}

/**
 *
 */
static PCHAR convert_wide_str(PWCHAR wide_str) {

    ULONG size = 0;
    PCHAR str = NULL;

    size = WideCharToMultiByte(CP_ACP, 0, wide_str, -1, NULL, 0, NULL, NULL);

    if (!size || !(str = GlobalAlloc(GPTR, size))) {
fprintf(stderr, "Out of memory!\n");
        goto CONVERT_WIDE_STR_EXIT;
    }

    if (!WideCharToMultiByte(CP_ACP, 0, wide_str, -1, str, size, NULL, NULL)) {
fprintf(stderr, "Could not convert string : %ld\n", GetLastError());
        GlobalFree(str);
        str = NULL;
        goto CONVERT_WIDE_STR_EXIT;
    }

CONVERT_WIDE_STR_EXIT:
    return str;
}

/**
 * @param hc_dev
 * @param prh_name (out) root bub handle name
 * @return root hub handle
 */
static HANDLE hc_to_rh(JNIEnv *env, jclass JavaxUsb, HANDLE hc_dev, char **prh_name) {

    USB_ROOT_HUB_NAME tmp_name;
    PUSB_ROOT_HUB_NAME rh_name_wide = NULL;
    PCHAR rh_name = NULL, full_rh_name = NULL;
    ULONG size = 0;
    HANDLE rh_dev = INVALID_HANDLE_VALUE;

    if (!DeviceIoControl(hc_dev, IOCTL_USB_GET_ROOT_HUB_NAME, NULL, 0, &tmp_name, sizeof(tmp_name), &size, NULL)) {
fprintf(stderr, "Could not get root hub name : %ld\n",GetLastError());
        goto HC_TO_RH_EXIT;
    }

    size = tmp_name.ActualLength;

    if (!(rh_name_wide = GlobalAlloc(GPTR, size))) {
fprintf(stderr, "Out of memory!\n");
        goto HC_TO_RH_EXIT;
    }

    if (!DeviceIoControl(hc_dev, IOCTL_USB_GET_ROOT_HUB_NAME, NULL, 0, rh_name_wide, size, &size, NULL)) {
fprintf(stderr, "Could not get root hub name : %ld\n",GetLastError());
        goto HC_TO_RH_EXIT;
    }

    if (!(rh_name = convert_wide_str(rh_name_wide->RootHubName))) {
fprintf(stderr, "Could not convert root hub name : %ld\n",GetLastError());
        goto HC_TO_RH_EXIT;
    }

fprintf(stderr, "Root hub has name %s\n", rh_name);

    if (!(full_rh_name = GlobalAlloc(GPTR, strlen("\\\\.\\")+strlen(rh_name)+1))) {
fprintf(stderr, "Out of memory!\n");
        goto HC_TO_RH_EXIT;
    }

    strcpy(full_rh_name, "\\\\.\\");
    strcat(full_rh_name, rh_name);

    printf("Creating handle for root hub with fullname %s\n", full_rh_name);
    rh_dev = CreateFile(full_rh_name, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);

    if (INVALID_HANDLE_VALUE == rh_dev) {
fprintf(stderr, "Could not get root hub handle %s : %ld\n",full_rh_name,GetLastError());
    } else {
fprintf(stderr, "Got root hub handle %s.\n",full_rh_name);
    }

HC_TO_RH_EXIT:
    if (rh_name_wide) { GlobalFree(rh_name_wide); }
    if (rh_name) { GlobalFree(rh_name); }

    *prh_name = full_rh_name;

    return rh_dev;
}

/**
 *
 */
static HANDLE get_hub_handle(JNIEnv *env, jclass JavaxUsb, HANDLE hub, PCHAR *pname) {

    USB_HUB_NAME tmp_name;
    PUSB_HUB_NAME name_wide = NULL;
    PCHAR name = NULL, full_name = NULL;
    ULONG size = 0;
    HANDLE dev = INVALID_HANDLE_VALUE;

    if (!DeviceIoControl(hub, IOCTL_USB_GET_NODE_CONNECTION_NAME, NULL, 0, &tmp_name, sizeof(tmp_name), &size, NULL)) {
fprintf(stderr, "Could not get hub name : %ld\n",GetLastError());
        goto GET_HUB_HANDLE_EXIT;
    }

    size = tmp_name.ActualLength;

    if (!(name_wide = GlobalAlloc(GPTR, size))) {
fprintf(stderr, "Out of memory!\n");
        goto GET_HUB_HANDLE_EXIT;
    }

    if (!DeviceIoControl(hub, IOCTL_USB_GET_NODE_CONNECTION_NAME, NULL, 0, name_wide, size, &size, NULL)) {
fprintf(stderr, "Could not get root hub name : %ld\n",GetLastError());
        goto GET_HUB_HANDLE_EXIT;
    }

    if (!(name = convert_wide_str(name_wide->HubName))) {
fprintf(stderr, "Could not convert hub name : %ld\n",GetLastError());
        goto GET_HUB_HANDLE_EXIT;
    }

    if (!(full_name = GlobalAlloc(GPTR, strlen("\\\\.\\")+strlen(name)+1))) {
fprintf(stderr, "Out of memory!\n");
        goto GET_HUB_HANDLE_EXIT;
    }

    strcpy(full_name, "\\\\.\\");
    strcat(full_name, name);

    dev = CreateFile(full_name, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);

    if (INVALID_HANDLE_VALUE == dev) {
fprintf(stderr, "Could not get hub handle %s : %ld\n",full_name,GetLastError());
    } else {
fprintf(stderr, "Got hub handle %s.\n",full_name);
    }

GET_HUB_HANDLE_EXIT:
    if (name_wide) { GlobalFree(name_wide); }
    if (name) { GlobalFree(name); }

    *pname = full_name;

    return dev;
}

/**
 *
 */
static int do_config_request(HANDLE h, int port, UCHAR config, USHORT desc_size, PUSB_DESCRIPTOR_REQUEST *prequest) {

    int ret = 0;
    PUSB_DESCRIPTOR_REQUEST request = NULL;
    PUSB_CONFIGURATION_DESCRIPTOR config_desc = NULL;
    ULONG request_size = sizeof(*request) + desc_size;
    UCHAR bLength;
    USHORT wTotalLength;
    ULONG size = 0;

    request = GlobalAlloc(GPTR, request_size);

    if (!request) {
fprintf(stderr, "Out of memory!\n");
        ret = -ENOMEM;
        goto DO_CONFIG_REQUEST_EXIT;
    }

    memset(request, 0, request_size);
    request->ConnectionIndex = port;
    // EXAMPLE CODE (there's NO documentation for this) says bmRequest and bRequest
    // are automatically set.  Gotta love UNDOCUMENTED APIs.  F@#%ing Winbloze.
    request->SetupPacket.wValue = (USB_CONFIGURATION_DESCRIPTOR_TYPE<<8) | config;
    request->SetupPacket.wLength = desc_size;

    if (!DeviceIoControl(h, IOCTL_USB_GET_DESCRIPTOR_FROM_NODE_CONNECTION, request, request_size, request, request_size, &size, NULL)) {
        ret = GetLastError();
fprintf(stderr, "Could not get configuration %d descriptor : %d\n", config, ret);
        goto DO_CONFIG_REQUEST_EXIT;
    }

    if (request_size > size) {
fprintf(stderr, "Did not get full configuration descriptor, got %d need %d\n", (int) size, (int) request_size);
        ret = -EINVAL;
        goto DO_CONFIG_REQUEST_EXIT;
    }

    config_desc = (PUSB_CONFIGURATION_DESCRIPTOR)request->Data;
    bLength = config_desc->bLength;
    wTotalLength = config_desc->wTotalLength;

    if (sizeof(*config_desc) > bLength) {
fprintf(stderr, "Invalid bLength %d, must be %d\n", bLength, sizeof(*config_desc));
        ret = -EINVAL;
        goto DO_CONFIG_REQUEST_EXIT;
    }

    if (desc_size > wTotalLength) {
fprintf(stderr, "Invalid wTotalLength %d, must be at least %d\n", wTotalLength, desc_size);
        ret = -EINVAL;
        goto DO_CONFIG_REQUEST_EXIT;
    }

    *prequest = request;
    return 0;

DO_CONFIG_REQUEST_EXIT:
    if (request) { GlobalFree(request); }

    return ret;
}

/**
 *
 */
static int get_config_desc(HANDLE hubdev, int port, UCHAR config, PUSB_CONFIGURATION_DESCRIPTOR *desc) {

    int ret = 0;
    PUSB_DESCRIPTOR_REQUEST request = NULL;
    PUSB_CONFIGURATION_DESCRIPTOR config_desc = NULL;
    USHORT desc_size = sizeof(*config_desc);

    if ((ret = do_config_request(hubdev, port, config, desc_size, &request))) {
fprintf(stderr, "Couldn't get configuration %d descriptor (pass 1, desc_size %d) : %d\n", config, desc_size, ret);
        goto GET_CONFIG_DESC_EXIT;
    }

    config_desc = (PUSB_CONFIGURATION_DESCRIPTOR)request->Data;
    desc_size = config_desc->wTotalLength;
    GlobalFree(request);
    request = NULL;
    config_desc = NULL;

    if ((ret = do_config_request(hubdev, port, config, desc_size, &request))) {
fprintf(stderr, "Couldn't get configuration %d descriptor (pass 2, desc_size %d) : %d\n", config, desc_size, ret);
        goto GET_CONFIG_DESC_EXIT;
    }

    if (!(config_desc = GlobalAlloc(GPTR,desc_size))) {
fprintf(stderr, "Out of memory!\n");
        ret = -ENOMEM;
        goto GET_CONFIG_DESC_EXIT;
    }

    RtlCopyMemory(config_desc, request->Data, desc_size);
    *desc = config_desc;

GET_CONFIG_DESC_EXIT:
    if (request) { GlobalFree(request); }

    return ret;
}

/**
 *
 */
static PUSB_COMMON_DESCRIPTOR get_next_desc(UCHAR *buffer, USHORT *remaining)
{
    USHORT length = *remaining;
    PUSB_COMMON_DESCRIPTOR desc = (PUSB_COMMON_DESCRIPTOR) buffer;

fprintf(stderr, "Starting get_next_desc with desc %p and %d bytes remaining.\n", desc, length);

    if (!desc) {
fprintf(stderr, "desc is NULL.\n");
        return NULL;
    }

    if (length <= desc->bLength) {
fprintf(stderr, "Remaining length %d less than or equal to bLength %d\n", length, desc->bLength);
        return NULL;
    }

    length -= desc->bLength;
    desc = (PUSB_COMMON_DESCRIPTOR)(buffer + desc->bLength);

fprintf(stderr, "Modified desc %p and length %d\n", desc, length);

    if (length < desc->bLength) {
fprintf(stderr, "Remaining length %d is less than bLength %d\n", length, desc->bLength);
        return NULL;
    }

    *remaining = length;

fprintf(stderr, "End get_next_desc with desc %p and %d bytes remaining.\n", desc, *remaining);

    return desc;
}

/* */
