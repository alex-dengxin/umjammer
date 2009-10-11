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

import javax.usb.UsbException;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

import com.ibm.jusb.os.AbstractUsbServices;


/**
 * UsbServices implementation for Windows platform.
 * @author Dan Streetman
 */
public class WindowsUsbServices extends AbstractUsbServices
    implements UsbServices {

    //*************************************************************************
    // Public methods

    /** @return The virtual USB root hub */
    public synchronized UsbHub getRootUsbHub() throws UsbException {
        JavaxUsb.loadLibrary();

        if (!isListening()) {
            synchronized (topologyLock) {
                startTopologyListener();

                try {
                    topologyLock.wait();
                } catch (InterruptedException iE) {
                    throw new UsbException("Interrupted while enumerating USB devices, try again");
                }
            }
        }

        if (0 != topologyListenerError) {
            throw new UsbException(COULD_NOT_ACCESS_USB_SUBSYSTEM + " : " +
                                   topologyListenerError);
        }

        if (0 != topologyUpdateResult) {
            throw new UsbException(COULD_NOT_ACCESS_USB_SUBSYSTEM + " : " +
                                   topologyUpdateResult);
        }

        return getRootUsbHubImp();
    }

    /** @return The minimum API version this supports. */
    public String getApiVersion() {
        return WINDOWS_API_VERSION;
    }

    /** @return The version number of this implementation. */
    public String getImpVersion() {
        return WINDOWS_IMP_VERSION;
    }

    /** @return Get a description of this UsbServices implementation. */
    public String getImpDescription() {
        return WINDOWS_IMP_DESCRIPTION;
    }

    //*************************************************************************
    // Private methods

    /** @return If the topology listener is listening */
    private boolean isListening() {
        try {
            return topologyListener.isAlive();
        } catch (NullPointerException npE) {
            return false;
        }
    }

    /** Start Topology Change Listener Thread */
    private void startTopologyListener() {
        Runnable r =
            new Runnable() {
                public void run() {
                    topologyListenerExit(JavaxUsb.nativeTopologyListener(WindowsUsbServices.this));
                }
            };

        topologyListener = new Thread(r);

        topologyListener.setDaemon(true);
        topologyListener.setName("javax.usb Windows implementation Topology Listener");

        topologyListenerError = 0;
        topologyListener.start();
    }

    /**
     * Called when the topology listener exits.
     * @param error The return code of the topology listener.
     */
    private void topologyListenerExit(int error) {
//FIXME - disconnet all devices
        topologyListenerError = error;

        synchronized (topologyLock) {
            topologyLock.notifyAll();
        }
    }

    //*************************************************************************
    // Instance variables
    private int topologyListenerError = 0;
    private int topologyUpdateResult = 0;
    private Object topologyLock = new Object();
    private Thread topologyListener = null;

    //*************************************************************************
    // Class constants
    public static final String COULD_NOT_ACCESS_USB_SUBSYSTEM =
        "Could not access USB subsystem.";
    public static final String WINDOWS_API_VERSION = "0.10.0";
    public static final String WINDOWS_IMP_VERSION = "0.10.0";
    public static final String WINDOWS_IMP_DESCRIPTION =
        "\t" + "JSR80 : javax.usb" + "\n" + "\n" +
        "Implementation for Windows 98, 2000, and XP.\n" + "\n" + "\n" + "*" +
        "\n" +
        "* Copyright (c) 1999 - 2003, International Business Machines Corporation." +
        "\n" + "* All Rights Reserved." + "\n" + "*" + "\n" +
        "* This software is provided and licensed under the terms and conditions" +
        "\n" + "* of the Common Public License:" + "\n" +
        "* http://oss.software.ibm.com/developerworks/opensource/license-cpl.html" +
        "\n" + "\n" + "http://javax-usb.org/" + "\n" + "\n";

    //----

    /** */
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("vavi.jusb.os.win32.WindowsUsbServices");
        System.out.println(c.getName());
        System.out.print(WINDOWS_IMP_DESCRIPTION);
    }
}

/* */
