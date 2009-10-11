/**
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

package vavi.jusb.os.win32;


/**
 * Abstract class for Linux requests.
 * @author Dan Streetman
 */
abstract class WindowsRequest {
    /**
     * Get the type of this request.
     * @return The type of this request.
     */
    public abstract int getType();

    /** @return The error that occured, or 0 if none occurred. */
    public int getError() {
        return errorNumber;
    }

    /** @param error The number of the error that occurred. */
    public void setError(int error) {
        errorNumber = error;
    }

    /** Wait forever until completed. */
    public void waitUntilCompleted() {
        waitUntilCompleted(0);
    }

    /** Wait until completed. */
    public void waitUntilCompleted(long timeout) {
        long start = System.currentTimeMillis();
        boolean use_timeout = 0 < timeout;

        synchronized (waitLock) {
            waitCount++;
            while (!isCompleted()) {
                long elapsed = System.currentTimeMillis() - start;
                if (use_timeout && (elapsed > timeout)) {
                    break;
                }
                try {
                    waitLock.wait(1000);
                } catch (InterruptedException iE) {
                }
            }
            waitCount--;
        }
    }

    /** @return If this is completed. */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Set completed.
     * @param c If this is completed or not.
     */
    public void setCompleted(boolean c) {
        completed = c;

        if (completed) {
            notifyCompleted();
            executeCompletion();
        }
    }

    /** Notify waiteers of completion. */
    public void notifyCompleted() {
        synchronized (waitLock) {
            if (0 < waitCount) {
                waitLock.notifyAll();
            }
        }
    }

    /** Run the Completion. */
    protected void executeCompletion() {
        try {
            getCompletion().windowsRequestComplete(this);
        } catch (NullPointerException npE) { /* no Completion */
        } catch (Exception e) { /* log? */
        }
    }

    /** @param c The Completion. */
    public void setCompletion(WindowsRequest.Completion c) {
        completion = c;
    }

    /** @return The Completion */
    public WindowsRequest.Completion getCompletion() {
        return completion;
    }

    private WindowsRequestProxy linuxRequestProxy = null;
    private Object waitLock = new Object();
    private int waitCount = 0;
    private boolean completed = false;
    private int errorNumber = 0;
    private WindowsRequest.Completion completion = null;

    /* These MUST be the same as those defined in jni/linux/JavaxUsbDeviceProxy.c */
    public static final int LINUX_PIPE_REQUEST = 1;
    public static final int LINUX_SET_INTERFACE_REQUEST = 2;
    public static final int LINUX_SET_CONFIGURATION_REQUEST = 3;
    public static final int LINUX_CLAIM_INTERFACE_REQUEST = 4;
    public static final int LINUX_IS_CLAIMED_INTERFACE_REQUEST = 5;
    public static final int LINUX_RELEASE_INTERFACE_REQUEST = 6;
    public static final int LINUX_ISOCHRONOUS_REQUEST = 7;

    public static interface Completion {
        public void windowsRequestComplete(WindowsRequest request);
    }
}
