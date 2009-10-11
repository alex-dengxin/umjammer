/*
 * Copyright (c) 2003 Dan Streetman (ddstreet@ieee.org)
 * Copyright (c) 2003 International Business Machines Corporation
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/CPLv1.0.htm
 */

package vavi.jusb.os.win32;

import java.util.Hashtable;

import javax.usb.UsbConst;
import javax.usb.UsbException;

import com.ibm.jusb.UsbConfigurationDescriptorImp;
import com.ibm.jusb.UsbConfigurationImp;
import com.ibm.jusb.UsbDeviceDescriptorImp;
import com.ibm.jusb.UsbDeviceImp;
import com.ibm.jusb.UsbEndpointDescriptorImp;
import com.ibm.jusb.UsbEndpointImp;
import com.ibm.jusb.UsbHubImp;
import com.ibm.jusb.UsbInterfaceDescriptorImp;
import com.ibm.jusb.UsbInterfaceImp;
import com.ibm.jusb.UsbPipeImp;

import vavi.util.Debug;


/**
 * Interface to/from JNI.
 * @author Dan Streetman
 */
class JavaxUsb {
    /** Load native library */
    public static void loadLibrary() throws UsbException {
        if (libraryLoaded) {
            return;
        }

        try {
            System.loadLibrary(LIBRARY_NAME);
        } catch (Exception e) {
            throw new UsbException(EXCEPTION_WHILE_LOADING_SHARED_LIBRARY + " " + System.mapLibraryName(LIBRARY_NAME) + " : " + e.getMessage());
        } catch (Error e) {
            throw new UsbException(ERROR_WHILE_LOADING_SHARED_LIBRARY + " " + System.mapLibraryName(LIBRARY_NAME) + " : " + e.getMessage());
        }
    }

    /**
     * Convert the error code to a UsbException.
     * @param error The error code.
     * @return A UsbException.
     */
    public static UsbException errorToUsbException(int error) {
        return new UsbException(nativeGetErrorMessage(error));
    }

    /**
     * Convert the error code to a UsbException using the specified text.
     * <p>
     * The string is prepended to the detail message with a colon separating
     * the specified text from the error message.
     * @param error The error code.
     * @param string The string to use in the UsbException.
     * @return A UsbException.
     */
    public static UsbException errorToUsbException(int error, String string) {
        return new UsbException(string + " : " + nativeGetErrorMessage(error));
    }

    // Native methods ----

    /**
     * Call the native function that listens for topology changes
     * @param services The WindowsUsbServices instance.
     * @return The error that caused the listener to exit.
     */
    static native int nativeTopologyListener(WindowsUsbServices services);

    /**
     * Start a WindowsDeviceProxy
     * @param proxy A WindowsInterfaceIO object
     */
    static native void nativeDeviceProxy(WindowsDeviceProxy proxy);
    
    // JavaxUsbError methods ----

    /**
     * @param error the error number
     * @return the message associated with the specified error number
     */
    static native String nativeGetErrorMessage(int error);

    // Creation methods ----

    /** @return A new UsbHubImp with max ports */
    private static UsbHubImp createUsbHubImp(String key, int maxPorts) {
        UsbHubImp hub = new UsbHubImp(maxPorts, null, null);

        WindowsDeviceOsImp windowsDeviceOsImp = new WindowsDeviceOsImp(hub);
        hub.setUsbDeviceOsImp(windowsDeviceOsImp);

        return hub;
    }

    /** @return A new UsbHubImp with variable ports */
    private static UsbHubImp createUsbHubImp(String key) {
        UsbHubImp hub = new UsbHubImp(null, null);

        WindowsDeviceOsImp windowsDeviceOsImp = new WindowsDeviceOsImp(hub);
        hub.setUsbDeviceOsImp(windowsDeviceOsImp);

        return hub;
    }

    /** @return A new UsbDeviceImp */
    private static UsbDeviceImp createUsbDeviceImp(String key) {
        UsbDeviceImp device = new UsbDeviceImp(null, null);

        WindowsDeviceOsImp windowsDeviceOsImp = new WindowsDeviceOsImp(device);
        device.setUsbDeviceOsImp(windowsDeviceOsImp);

        return device;
    }

    /** @return A new UsbConfigurationImp */
    private static UsbConfigurationImp createUsbConfigurationImp(UsbDeviceImp device, byte length, byte type, short totalLen, byte numInterfaces, byte configValue, byte configIndex, byte attributes, byte maxPowerNeeded, boolean active) {
        /* BUG - Java (IBM JVM at least) does not handle certain JNI byte -> Java byte (or shorts) */
        /* Email ddstreet@ieee.org for more info */
        length += 0;
        type += 0;
        numInterfaces += 0;
        configValue += 0;
        configIndex += 0;
        attributes += 0;
        maxPowerNeeded += 0;

        UsbConfigurationDescriptorImp desc = new UsbConfigurationDescriptorImp(length, type, totalLen, numInterfaces, configValue, configIndex, attributes, maxPowerNeeded);

        UsbConfigurationImp config = new UsbConfigurationImp(device, desc);

        if (active) {
            device.setActiveUsbConfigurationNumber(configValue);
        }

        return config;
    }

    /** @return A new UsbInterfaceImp */
    private static UsbInterfaceImp createUsbInterfaceImp(UsbConfigurationImp config, byte length, byte type, byte interfaceNumber, byte alternateNumber, byte numEndpoints, byte interfaceClass, byte interfaceSubClass, byte interfaceProtocol, byte interfaceIndex, boolean active) {
        /* BUG - Java (IBM JVM at least) does not handle certain JNI byte -> Java byte (or shorts) */
        /* Email ddstreet@ieee.org for more info */
        length += 0;
        type += 0;
        interfaceNumber += 0;
        alternateNumber += 0;
        numEndpoints += 0;
        interfaceClass += 0;
        interfaceSubClass += 0;
        interfaceProtocol += 0;
        interfaceIndex += 0;

        UsbInterfaceDescriptorImp desc = new UsbInterfaceDescriptorImp(length, type, interfaceNumber, alternateNumber, numEndpoints, interfaceClass, interfaceSubClass, interfaceProtocol, interfaceIndex);

        UsbInterfaceImp iface = new UsbInterfaceImp(config, desc);

        /* If the config is not active, neither are its interface settings */
        if (config.isActive() && active) {
            iface.setActiveSettingNumber(iface.getUsbInterfaceDescriptor().bAlternateSetting());
        }

        WindowsDeviceOsImp windowsDeviceOsImp = (WindowsDeviceOsImp) iface.getUsbConfigurationImp().getUsbDeviceImp().getUsbDeviceOsImp();
        WindowsInterfaceOsImp windowsInterfaceOsImp = new WindowsInterfaceOsImp(iface, windowsDeviceOsImp);
        iface.setUsbInterfaceOsImp(windowsInterfaceOsImp);

        return iface;
    }

    /** @return A new UsbEndpointImp */
    private static UsbEndpointImp createUsbEndpointImp(UsbInterfaceImp iface, byte length, byte type, byte endpointAddress, byte attributes, byte interval, short maxPacketSize) {
        /* BUG - Java (IBM JVM at least) does not handle certain JNI byte -> Java byte (or shorts) */
        /* Email ddstreet@ieee.org for more info */
        length += 0;
        type += 0;
        endpointAddress += 0;
        attributes += 0;
        interval += 0;
        maxPacketSize += 0;

        UsbEndpointDescriptorImp desc = new UsbEndpointDescriptorImp(length, type, endpointAddress, attributes, interval, maxPacketSize);

        UsbEndpointImp ep = new UsbEndpointImp(iface, desc);
        UsbPipeImp pipe = null;

        WindowsInterfaceOsImp windowsInterfaceOsImp = (WindowsInterfaceOsImp) iface.getUsbInterfaceOsImp();

Debug.println("end point type: " + ep.getType());
/*
        switch (ep.getType()) {
        case UsbConst.ENDPOINT_TYPE_CONTROL:
            pipe = new UsbControlPipeImp(ep, null);
            pipe.setUsbPipeOsImp(new WindowsControlPipeImp((UsbControlPipeImp) pipe, windowsInterfaceOsImp));
            break;
        case UsbConst.ENDPOINT_TYPE_ISOCHRONOUS:
            pipe = new UsbPipeImp(ep, null);
            pipe.setUsbPipeOsImp(new WindowsIsochronousPipeImp(pipe, windowsInterfaceOsImp));
            break;
        case UsbConst.ENDPOINT_TYPE_BULK:
            pipe = new UsbPipeImp(ep, null);
            pipe.setUsbPipeOsImp(new WindowsPipeOsImp(pipe, windowsInterfaceOsImp));
            break;
        case UsbConst.ENDPOINT_TYPE_INTERRUPT:
*/
            pipe = new UsbPipeImp(ep, null);
            pipe.setUsbPipeOsImp(new WindowsPipeOsImp(pipe, windowsInterfaceOsImp));
/*
            break;
        default:
//FIXME - log?
            throw new RuntimeException("Invalid UsbEndpoint type " + ep.getType());
        }
*/
        return ep;
    }

    /**
     * setup
     */
    private static void configureUsbDeviceImp(UsbDeviceImp targetDevice, byte length, byte type, byte deviceClass, byte deviceSubClass, byte deviceProtocol, byte maxDefaultEndpointSize, byte manufacturerIndex, byte productIndex, byte serialNumberIndex, byte numConfigs, short vendorId, short productId, short bcdDevice, short bcdUsb, int speed) {
        /* BUG - Java (IBM JVM at least) does not handle certain JNI byte -> Java byte (or shorts) */
        /* Email ddstreet@ieee.org for more info */
        length += 0;
        type += 0;
        deviceClass += 0;
        deviceSubClass += 0;
        deviceProtocol += 0;
        maxDefaultEndpointSize += 0;
        manufacturerIndex += 0;
        productIndex += 0;
        serialNumberIndex += 0;
        numConfigs += 0;
        vendorId += 0;
        productId += 0;
        bcdDevice += 0;
        bcdUsb += 0;

        UsbDeviceDescriptorImp desc = new UsbDeviceDescriptorImp(length, type, bcdUsb, deviceClass, deviceSubClass, deviceProtocol, maxDefaultEndpointSize, vendorId, productId, bcdDevice, manufacturerIndex, productIndex, serialNumberIndex, numConfigs);

        targetDevice.setUsbDeviceDescriptor(desc);

        switch (speed) {
        case SPEED_LOW:
            targetDevice.setSpeed(UsbConst.DEVICE_SPEED_LOW);
            break;
        case SPEED_FULL:
            targetDevice.setSpeed(UsbConst.DEVICE_SPEED_FULL);
            break;
        case SPEED_UNKNOWN:
            targetDevice.setSpeed(UsbConst.DEVICE_SPEED_UNKNOWN);
            break;
        default:
            /* log */
            targetDevice.setSpeed(UsbConst.DEVICE_SPEED_UNKNOWN);
            break;
        }
    }

    /**
     * Connect a device to a hub's port.
     * @param hub The parent hub.
     * @param port The parent port.
     * @param device The device to connect.
     */
    public static void connectUsbDeviceImp(UsbHubImp hub, int port, UsbDeviceImp device) {
        try {
            device.connect(hub, (byte) port);
        } catch (UsbException uE) {
//FIXME - log instead of stderr
            System.err.println("UsbException while connecting : " + uE.toString());
        }
    }

    // Class variables
    private static boolean libraryLoaded = false;
    private static Hashtable<?, ?> msgLevelTable = new Hashtable<Object, Object>();

    // Class constants
    public static final String LIBRARY_NAME = "JavaxUsb";
    public static final String ERROR_WHILE_LOADING_SHARED_LIBRARY = "Error while loading shared library";
    public static final String EXCEPTION_WHILE_LOADING_SHARED_LIBRARY = "Exception while loading shared library";
    private static final int SPEED_UNKNOWN = 0;
    private static final int SPEED_LOW = 1;
    private static final int SPEED_FULL = 2;

    //----

    /** */
    public static void main(String[] args) {
        System.out.println("Hello JavaxUsb");
    }
}

/* */
