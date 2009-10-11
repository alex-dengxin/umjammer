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

import com.ibm.jusb.UsbDeviceImp;
import com.ibm.jusb.os.DefaultUsbDeviceOsImp;
import com.ibm.jusb.os.UsbDeviceOsImp;


/**
 * UsbDeviceOsImp implemenation for Windows platform.
 * <p>
 * This must be set up before use.
 * <ul>
 * <li>The {@link #getUsbDeviceImp() UsbDeviceImp} must be set
 *     either in the constructor or by its {@link #setUsbDeviceImp(UsbDeviceImp) setter}.</li>
 * </ul>
 * @author Dan Streetman
 */
class WindowsDeviceOsImp extends DefaultUsbDeviceOsImp
    implements UsbDeviceOsImp {

    /** Constructor */
    public WindowsDeviceOsImp(UsbDeviceImp device) {
        setUsbDeviceImp(device);
    }

    /** @return The UsbDeviceImp for this */
    public UsbDeviceImp getUsbDeviceImp() {
        return usbDeviceImp;
    }

    /** @param device The UsbDeviceImp for this */
    public void setUsbDeviceImp(UsbDeviceImp device) {
        usbDeviceImp = device;
    }

    /**
     * Get the WindowsDeviceProxy.
     * <p>
     * This will start up the WindowsDeviceProxy if not running.
     * @return The WindowsDeviceProxy.
     * @exception UsbException If an UsbException occurred while starting the WindowsDeviceProxy.
     */
    public WindowsDeviceProxy getWindowsDeviceProxy() throws UsbException {
        synchronized(windowsDeviceProxy) {
            if (!windowsDeviceProxy.isRunning()) {
                windowsDeviceProxy.start();
            }
        }
        
        return windowsDeviceProxy;
    }
    
    /** AsyncSubmit a UsbControlIrpImp */
/*
    public void asyncSubmit(UsbControlIrpImp usbControlIrpImp)
        throws UsbException {

        WindowsControlRequest request = null;

        if (usbControlIrpImp.isSetConfiguration()) {
            request = new WindowsSetConfigurationRequest();
        } else if (usbControlIrpImp.isSetInterface()) {
            request = new WindowsSetInterfaceRequest();
        } else {
            request = new WindowsControlRequest();
            request.setUsbIrpImp(usbControlIrpImp);
            submit(request);
        }
    }
*/

    /** Submit a Request. */
    void submit(WindowsRequest request) throws UsbException {
        getWindowsDeviceProxy().submit(request);
    }

    private UsbDeviceImp usbDeviceImp = null;
    
    private WindowsDeviceProxy windowsDeviceProxy = null;
}

/* */
