/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

package vavi.jusb.os.win32;

import java.util.LinkedList;
import java.util.List;


/**
 * Abstract proxy which manages requests to JNI.
 * @author Dan Streetman
 */
abstract class WindowsRequestProxy {
    //**************************************************************************
    // Public methods

    /**
     * Submit the Request.
     * <p>
     * No checking of the Request is done.
     * @param request The LinuxRequest.
     */
    public void submit(WindowsRequest request) {
        synchronized (readyList) {
            readyList.add(request);
        }
    }

    /**
     * Cancel the Request.
     * @param request The WindowsRequest.
     */
    public void cancel(WindowsRequest request) {
        synchronized (readyList) {
            if (readyList.contains(request)) {
                readyList.remove(request);
                request.setError(-1);
                request.setCompleted(true);
                return;
            }
        }

        synchronized (cancelList) {
            cancelList.add(request);
        }
    }

    //**************************************************************************
    // JNI methods

    /**
     * If there are any requests waiting.
     * @return If there are any requests waiting.
     */
    private boolean isRequestWaiting() {
        return !readyList.isEmpty() || !cancelList.isEmpty();
    }

    /**
     * Get the next ready Request.
     * @return The next ready Request.
     */
    private WindowsRequest getReadyRequest() {
        synchronized (readyList) {
            try {
                return readyList.remove(0);
            } catch (IndexOutOfBoundsException ioobE) {
                return null;
            }
        }
    }

    /**
     * Get the next cancel Request.
     * @return The next cancel Request.
     */
    private WindowsRequest getCancelRequest() {
        synchronized (cancelList) {
            try {
                return cancelList.remove(0);
            } catch (IndexOutOfBoundsException ioobE) {
                return null;
            }
        }
    }

    //**************************************************************************
    // Instance variables
    private List<WindowsRequest> readyList = new LinkedList<WindowsRequest>();
    private List<WindowsRequest> cancelList = new LinkedList<WindowsRequest>();
}
