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

import java.util.LinkedList;
import java.util.List;

import javax.usb.UsbException;

import com.ibm.jusb.UsbIrpImp;
import com.ibm.jusb.UsbPipeImp;
import com.ibm.jusb.os.DefaultUsbPipeOsImp;
import com.ibm.jusb.os.UsbPipeOsImp;


/**
 * UsbPipeOsImp implementation for Windows platform.
 * <p>
 * This must be set up before use.
 * <ul>
 * <li>The {@link #getUsbPipeImp() UsbPipeImp} must be set
 *     either in the constructor or by its {@link #setUsbPipeImp(UsbPipeImp) setter}.</li>
 * <li>The {@link #getWindowsInterfaceOsImp() WindowsInterfaceOsImp} must be set
 *     either in the constructor or by its {@link #setWindowsInterfaceOsImp(WindowsInterfaceOsImp) setter}.</li>
 * </ul>
 * @author Dan Streetman
 */
public class WindowsPipeOsImp extends DefaultUsbPipeOsImp
    implements UsbPipeOsImp, WindowsRequest.Completion {

    /** Constructor */
    public WindowsPipeOsImp(UsbPipeImp pipe, WindowsInterfaceOsImp iface) {
        setUsbPipeImp(pipe);
        setWindowsInterfaceOsImp(iface);
    }

    /** @return The UsbPipeImp for this */
    public UsbPipeImp getUsbPipeImp() {
        return usbPipeImp;
    }

    /** @param pipe The UsbPipeImp for this */
    public void setUsbPipeImp(UsbPipeImp pipe) {
        usbPipeImp = pipe;
    }

    /** @return The WindowsInterfaceOsImp */
    public WindowsInterfaceOsImp getWindowsInterfaceOsImp() {
        return windowsInterfaceOsImp;
    }

    /** @param iface The WindowsInterfaceOsImp */
    public void setWindowsInterfaceOsImp(WindowsInterfaceOsImp iface) {
        windowsInterfaceOsImp = iface;
    }

    /**
     * Asynchronous submission using a UsbIrpImp.
     * @param irp the UsbIrpImp to use for this submission
     * @exception javax.usb.UsbException if error occurs
     */
    public void asyncSubmit(UsbIrpImp irp ) throws UsbException {
        WindowsPipeRequest request = usbIrpImpToWindowsPipeRequest(irp);
        
        getWindowsInterfaceOsImp().submit(request);
        
        synchronized(inProgressList) {
            inProgressList.add(request);
        }
    }

    /**
     * Stop all submissions in progress
     */
/*
        public void abortAllSubmissions() {
                Object[] requests = null;

                synchronized(inProgressList) {
                    requests = inProgressList.toArray();
                    inProgressList.clear();
                }

                for (int i = 0; i < requests.length; i++) {
                    getWindowsInterfaceOsImp().cancel((WindowsPipeRequest) requests[i]);
                }

                for (int i = 0; i < requests.length; i++) {
                    ((WindowsPipeRequest) requests[i]).waitUntilCompleted();
                }
        }
*/

    /** @param request The WindowsRequest that completed. */
/*
        public void windowsRequestComplete(WindowsRequest request) {
                synchronized (inProgressList) {
                        inProgressList.remove(request);
                }
        }
*/

    /** */
    public void windowsRequestComplete(WindowsRequest request) {
        synchronized (inProgressList) {
            inProgressList.remove(request);
        }
    }

    /**
     * Create a WindowsPipeRequest to wrap a UsbIrpImp.
     * @param usbIrpImp The UsbIrpImp.
     * @return A WindowsPipeRequest for a UsbIrpImp.
     */
    protected WindowsPipeRequest usbIrpImpToWindowsPipeRequest(UsbIrpImp usbIrpImp) {
        WindowsPipeRequest request = new WindowsPipeRequest(getPipeType(),getEndpointAddress());
        request.setUsbIrpImp(usbIrpImp);
        request.setCompletion(this);
        return request;
    }

    /** @return The endpoint address */
    protected byte getEndpointAddress() {
        if (0 == endpointAddress) {
            endpointAddress =
                usbPipeImp.getUsbEndpointImp().getUsbEndpointDescriptor()
                .bEndpointAddress();
        }

        return endpointAddress;
    }

    /** @return The pipe type */
    protected byte getPipeType() {
        if (0 == pipeType) {
            pipeType = usbPipeImp.getUsbEndpointImp().getType();
        }

        return pipeType;
    }

    private UsbPipeImp usbPipeImp = null;
    private WindowsInterfaceOsImp windowsInterfaceOsImp = null;
    protected byte pipeType = 0;
    protected byte endpointAddress = 0;
    protected List<WindowsRequest> inProgressList = new LinkedList<WindowsRequest>();
}

/* */
