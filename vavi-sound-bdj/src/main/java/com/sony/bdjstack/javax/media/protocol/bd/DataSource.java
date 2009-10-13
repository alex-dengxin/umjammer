/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.sony.bdjstack.javax.media.protocol.bd;

import java.io.IOException;

import javax.media.Time;


/**
 * DataSource. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2008/09/14 nsano initial version <br>
 */
public class DataSource extends javax.media.protocol.DataSource {

    /* @see javax.media.protocol.DataSource#connect() */
    public void connect() throws IOException {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.protocol.DataSource#disconnect() */
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.protocol.DataSource#getContentType() */
    public String getContentType() {
        return "file";
    }

    /* @see javax.media.protocol.DataSource#start() */
    public void start() throws IOException {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.protocol.DataSource#stop() */
    public void stop() throws IOException {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.protocol.Controls#getControl(java.lang.String) */
    public Object getControl(String controlType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.protocol.Controls#getControls() */
    public Object[] getControls() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Duration#getDuration() */
    public Time getDuration() {
        // TODO Auto-generated method stub
        return null;
    }
}

/* */
