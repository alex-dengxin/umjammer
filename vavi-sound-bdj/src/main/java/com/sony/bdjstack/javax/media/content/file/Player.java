/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.sony.bdjstack.javax.media.content.file;

import java.awt.Component;
import java.io.IOException;

import javax.media.ClockStoppedException;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerListener;
import javax.media.GainControl;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.protocol.DataSource;


/**
 * Player. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2008/09/14 nsano initial version <br>
 */
public class Player implements javax.media.Player {

    /* @see javax.media.Player#addController(javax.media.Controller) */
    public void addController(Controller arg0) throws IncompatibleTimeBaseException {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Player#getControlPanelComponent() */
    public Component getControlPanelComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Player#getGainControl() */
    public GainControl getGainControl() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Player#getVisualComponent() */
    public Component getVisualComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Player#removeController(javax.media.Controller) */
    public void removeController(Controller arg0) {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Player#start() */
    public void start() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.MediaHandler#setSource(javax.media.protocol.DataSource) */
    public void setSource(DataSource arg0) throws IOException, IncompatibleSourceException {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Controller#addControllerListener(javax.media.ControllerListener) */
    public void addControllerListener(ControllerListener arg0) {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Controller#close() */
    public void close() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Controller#deallocate() */
    public void deallocate() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Controller#getControl(java.lang.String) */
    public Control getControl(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Controller#getControls() */
    public Control[] getControls() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Controller#getStartLatency() */
    public Time getStartLatency() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Controller#getState() */
    public int getState() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* @see javax.media.Controller#getTargetState() */
    public int getTargetState() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* @see javax.media.Controller#prefetch() */
    public void prefetch() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Controller#realize() */
    public void realize() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Controller#removeControllerListener(javax.media.ControllerListener) */
    public void removeControllerListener(ControllerListener arg0) {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Clock#getMediaNanoseconds() */
    public long getMediaNanoseconds() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* @see javax.media.Clock#getMediaTime() */
    public Time getMediaTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Clock#getRate() */
    public float getRate() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* @see javax.media.Clock#getStopTime() */
    public Time getStopTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Clock#getSyncTime() */
    public Time getSyncTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Clock#getTimeBase() */
    public TimeBase getTimeBase() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Clock#mapToTimeBase(javax.media.Time) */
    public Time mapToTimeBase(Time arg0) throws ClockStoppedException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see javax.media.Clock#setMediaTime(javax.media.Time) */
    public void setMediaTime(Time arg0) {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Clock#setRate(float) */
    public float setRate(float arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* @see javax.media.Clock#setStopTime(javax.media.Time) */
    public void setStopTime(Time arg0) {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Clock#setTimeBase(javax.media.TimeBase) */
    public void setTimeBase(TimeBase arg0) throws IncompatibleTimeBaseException {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Clock#stop() */
    public void stop() {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Clock#syncStart(javax.media.Time) */
    public void syncStart(Time arg0) {
        // TODO Auto-generated method stub

    }

    /* @see javax.media.Duration#getDuration() */
    public Time getDuration() {
        // TODO Auto-generated method stub
        return null;
    }
}

/* */
