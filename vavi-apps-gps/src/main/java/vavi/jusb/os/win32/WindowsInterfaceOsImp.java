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

import com.ibm.jusb.UsbInterfaceImp;
import com.ibm.jusb.os.DefaultUsbInterfaceOsImp;
import com.ibm.jusb.os.UsbInterfaceOsImp;


/**
 * UsbInterfaceOsImp implementation for Windows platform.
 * <p>
 * This must be set up before use.
 * <ul>
 * <li>The {@link #getUsbInterfaceImp() UsbInterfaceImp} must be set
 *     either in the constructor or by its {@link #setUsbInterfaceImp(UsbInterfaceImp) setter}.</li>
 * <li>The {@link #getWindowsDeviceOsImp() WindowsDeviceOsImp} must be set
 *     either in the constructor or by its {@link #setWindowsDeviceOsImp(WindowsDeviceOsImp) setter}.</li>
 * </ul>
 * @author Dan Streetman
 */
class WindowsInterfaceOsImp extends DefaultUsbInterfaceOsImp
    implements UsbInterfaceOsImp {

    /** Constructor */
    public WindowsInterfaceOsImp(UsbInterfaceImp iface,
                                 WindowsDeviceOsImp device) {
        setUsbInterfaceImp(iface);
        setWindowsDeviceOsImp(device);
    }

    //*************************************************************************
    // Public methods

    /** @return The UsbInterfaceImp for this */
    public UsbInterfaceImp getUsbInterfaceImp() {
        return usbInterfaceImp;
    }

    /** @param iface The UsbInterfaceImp for this */
    public void setUsbInterfaceImp(UsbInterfaceImp iface) {
        usbInterfaceImp = iface;
    }

    /** @return The WindowsDeviceOsImp for this */
    public WindowsDeviceOsImp getWindowsDeviceOsImp() {
        return windowsDeviceOsImp;
    }

    /** @param device The WindowsDeviceOsImp for this */
    public void setWindowsDeviceOsImp(WindowsDeviceOsImp device) {
        windowsDeviceOsImp = device;
    }

    /** Claim this interface. */

/*
    public void claim() throws UsbException {
        WindowsInterfaceRequest request = new WindowsInterfaceRequest.WindowsClaimInterfaceRequest(getInterfaceNumber());
        submit(request);

        request.waitUntilCompleted();

        if (0 != request.getError())
            throw new UsbException("Could not claim interface : " + JavaxUsb.nativeGetErrorMessage(request.getError()));
    }
*/

    /** Release this interface. */
/*
    public void release() {
        WindowsInterfaceRequest request = new WindowsInterfaceRequest.WindowsReleaseInterfaceRequest(getInterfaceNumber());

        try {
            submit(request);
        } catch (UsbException uE) {
//FIXME - log this
            return;
        }

        request.waitUntilCompleted();
    }
*/

    /** @return if this interface is claimed. */
/*
    public boolean isClaimed() {
        WindowsInterfaceRequest request = new WindowsInterfaceRequest.WindowsIsClaimedInterfaceRequest(getInterfaceNumber());

        try {
            submit(request);
        } catch (UsbException uE) {
//FIXME - log this
            return false;
        }

        request.waitUntilCompleted();

        if (0 != request.getError()) {
//FIXME - log
            return false;
        }

        return request.isClaimed();
    }
*/

    public byte getInterfaceNumber() {
        if (!interfaceNumberSet) {
            interfaceNumber =
                usbInterfaceImp.getUsbInterfaceDescriptor().bInterfaceNumber();
            interfaceNumberSet = true;
        }

        return interfaceNumber;
    }

    /**
     * Submit a Request.
     * @param request The WindowsRequest.
     */
    void submit(WindowsRequest request) throws UsbException {
        getWindowsDeviceOsImp().submit(request);
    }

    /*
     * Cancel a Request.
     * @param request The WindowsRequest.
     */
/*
    void cancel(WindowsRequest request) {
        getWindowsDeviceOsImp().cancel(request);
    }
*/
    protected UsbInterfaceImp usbInterfaceImp = null;
    protected WindowsDeviceOsImp windowsDeviceOsImp = null;
    private boolean interfaceNumberSet = false;
    private byte interfaceNumber = 0;
}

/* */
