/*
 * @(#) $Id: JSTKPerfData.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

import java.io.OutputStream;
import java.io.PrintWriter;


public class JSTKPerfData {
    private long noOfInvocations;

    private long totalBytes;

    private long minBytes;

    private long maxBytes;

    private long curBytes;

    private long totalTime;

    private long minTime;

    private long maxTime;

    private long curTime;

    private long startTime;

    private boolean collectPerfData;

    public JSTKPerfData() {
        reset();
    }

    public void reset() {
        collectPerfData = true;
        ;
        noOfInvocations = 0;
        totalBytes = totalTime = 0;
        minBytes = minTime = 999999999;
        maxBytes = maxTime = 0;
    }

    public void updateBegin() {
        if (!collectPerfData)
            return;
        this.startTime = System.currentTimeMillis();
    }

    public void updateEnd(long bytes) {
        if (!collectPerfData)
            return;

        long duration = System.currentTimeMillis() - startTime;
        ++noOfInvocations;

        curBytes = bytes;
        totalBytes += bytes;
        if (minBytes > curBytes)
            minBytes = curBytes;
        if (maxBytes < curBytes)
            maxBytes = curBytes;

        curTime = duration;
        totalTime += curTime;
        if (minTime > curTime)
            minTime = curTime;
        if (maxTime < curTime)
            maxTime = curTime;
    }

    public boolean getEnabled() {
        return collectPerfData;
    }

    public void setEnabled(boolean state) {
        collectPerfData = state;
    }

    public long getNoOfInvocations() {
        return noOfInvocations;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void store(OutputStream os) {
        PrintWriter pw = new PrintWriter(os);
        double procRate = 0;
        if (totalTime != 0)
            procRate = (totalBytes * 1000) / (totalTime * 1024);
        pw.println("(N=" + noOfInvocations + ", TT=" + totalTime + " ms, TB=" + totalBytes + " bytes, TB/TT=" + procRate + " KB/s)");
        pw.flush();
    }
}
