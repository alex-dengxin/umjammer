/*
 * This file is part of J2ME VNC.
 *
 * Copyright (c) 2003 Michael Lloyd Lee
 * 
 * Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
 *
 * J2ME VNC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * J2ME VNC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2ME VNC; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package tk.wetnet.vnc;

import  tk.wetnet.j2me.vnc.VNC;

import tk.wetnet.util.Queue;

import java.io.DataInputStream;
import java.io.OutputStream;

/**
 * A simple API for VNC clients.
 * To use, create a RFBProto object, then stick it in a thread.
 * This is a mono-threaded implementation, due to problems in Nokia phones.
 * @author Michael Lloyd Lee
 */
public class RFBProto implements Runnable {

    private class Event extends tk.wetnet.util.Queue.QueueEntry {
        private byte[] buffer = new byte[10];
        private int size = 0;
        private int pc = 0;
    }

    private int returnDataExpected = 0;

    Queue eventQueue = null;
    Queue eventQueueCashe = null;

    private static final int NO_AUTH = 1;
    private static final int NORMAL_AUTH = 2;
    
    private boolean sharedFlag;
    
    /** The input stream */
    public DataInputStream sin;
    /** The output stream*/
    public OutputStream sout ;
       
    /** The X ( height ) of the screen */
    public int x = 0;
    /** The Y ( width ) of the screen */
    public int y = 0;
    
    private DrawToable drawOnMe = null;
    
    /** The desktops name */
    public String desktopName = null;
    
    /** Is the RFBProto currently running */
    public volatile boolean running = false;
    
    private boolean ncm;
    
    byte[] key;

    /**
     * Constructor
     * @param sin The input stream to use
     * @param sout the output stream to use
     * @param key The password, if you are not expecting to need a password
     *            it may be null.
     * @param d The object interested in receiving updates.
     * @param ncm Nokia Compatibility Mode 
     */
    public RFBProto(DataInputStream sin, OutputStream sout,
        		    byte[] key, DrawToable d, boolean sharedFlag, 
        		    boolean ncm) {
        VNC.log("RFBProto.create");
        this.sin = sin;
        VNC.log("RFBProto.create sin");
        this.sout = sout;
        VNC.log("RFBProto.create sout");

        this.key = key;

        VNC.log("RFBP.create ncm" + ncm);

        this.drawOnMe = d;
        this.sharedFlag = sharedFlag;
        this.ncm = ncm;
        eventQueueCashe = new Queue();
        if (ncm) {
            VNC.log("creating event queue");
            eventQueue = new Queue();
            for (int i = 0; i < 5; i++) {
                eventQueueCashe.push(new Event());
            }
        }
    }
    
    /** Fucking evil hack! Not happy with!! */
    private void readFully(byte[] b) throws Throwable {
        readFully(b, b.length);
    }
 
    /** Fucking evil hack! Not happy with!! */
    private void readFully(byte[] b, int bl) throws Throwable {
        for (int i = 0; i < bl; i++) {
            b[i] = (byte) sin.read();
        }
    }
 
    /** Send initalization data to the VNC server 
     */
    private boolean init() throws Throwable {
VNC.log("init");
        byte[] hi = new byte[12];
        for (int i = 0; i < 12; i++) {
            hi[i] = (byte) sin.read();
        }
        String hiS = new String(hi);
VNC.log(hiS);
        if (!(hi[0] == 'R' && hi[1] == 'F' && hi[2] == 'B')) {
            drawOnMe.error("Not a VNC Server!");
            return false;
        } 
VNC.log("Writing Version");

        sout.write(0x52);
        sout.write(0x46);
        sout.write(0x42);
        sout.write(0x20);
        sout.write(0x30);
        sout.write(0x30);
        sout.write(0x33);
        sout.write(0x2e);
        sout.write(0x30);
        sout.write(0x30);
        sout.write(0x33);
        sout.write(0x0a);

VNC.log("Flushing Version");
        sout.flush();
VNC.log("Waiting for AUTH");
        int c = readCard32();
VNC.log("AUTH:" + c);

        drawOnMe.incConnectionStatus();

        switch (c) {
        case NO_AUTH:
//VNC.log("NO_AUTH");
            break;
        case NORMAL_AUTH:
VNC.log("NORMAL_AUTH");
            byte[] fc = new byte[16];

            readFully(fc);

VNC.log("desing");
            DesCipher des = new DesCipher(key);
            des.encrypt(fc, 0, fc, 0);
            des.encrypt(fc, 8, fc, 8);
            key = null;
            des = null;
VNC.log("dessing done");
            sout.write(fc);
            sout.flush();
            int authResult = readCard32();
VNC.log("Password:" + authResult);
            if (authResult != 0) {
                drawOnMe.error("Password Incorrect");
                return false;
            }

            break;
        default:
            byte[] reason = new byte[readCard32()];
            readFully(reason);
            String string = new String(reason);
VNC.log("Connection Refused\n" + string);
            drawOnMe.error("Connection Refused\n" + string);
            return false;
        }
        drawOnMe.incConnectionStatus();
        // share flag
        Event e = allocate(1);
        writeCard8(e, (sharedFlag ? 1 : 0));
        flush(e);
VNC.log("Shared flag sent");

        x = readCard16();
        y = readCard16();

        /*
         * The following is the pixel format, which I could not care less about. I will tell the server to use my format
         */
        readCard8(); // bpp
        readCard8(); // depth

        readCard8(); // big endian
        readCard8(); // truecolour

        readCard16(); // Max - red
        readCard16(); // green
        readCard16(); // blue
        readCard8(); // shift - red
        readCard8(); // green
        readCard8(); // blue

        // Padding
        readCard8();
        readCard8();
        readCard8();
VNC.log("Read Colour info");

        byte[] name = new byte[readCard32()];
        readFully(name);
        desktopName = new String(name);

VNC.log(desktopName);

        drawOnMe.incConnectionStatus();

        // see, could not care less ;-)
        setDefaultSupportedPixelFormat();
        drawOnMe.incConnectionStatus();
        setDefaultEncodings();

VNC.log("init-end");

        return true;
    }

    private Thread me = null;

    /**
     * The run method which is executed in its own thread by the creator.
     * 
     * @todo Redo the Throwable handing
     */
    public void run() {
        me = Thread.currentThread();
VNC.log("run started");
        try {
            if (!init())
                return;
        } catch (Throwable t) {
            // this.pwd = null;
            drawOnMe.error("Problem Connection To VNC Server.\n" + t);
            return;
        }
        int code = 0;
        running = true;
        drawOnMe.ready();
        try {
            while (running) {
                if (ncm) {
                    while (running && eventQueue.isEmpty() && returnDataExpected <= 0) {
                        Thread.yield();
                    }

                    // do out going events
                    while (!eventQueue.isEmpty() && running) {
                        Event e = (Event) eventQueue.pop();

                        if (e != null) {
VNC.log("sending event: " + e.size);
                            for (int i = 0; i < e.size; i++) {
                                sout.write(e.buffer[i]);
                            }
                            // sout.write( e.buffer, 0, e.size-1 );
                            sout.flush();
                            eventQueueCashe.push(e);
                        } else {
VNC.log("EventQueue == " + eventQueue.isEmpty());
                        }
                    }
                }
VNC.log("check incoming events");
                // check incoming events
                code = readCard8();
VNC.log("CODE:" + code);
                switch (code) {
                case COMMAND_CODE_REPAINT:
                    update();
                    returnDataExpected--;
                    break;
                case COMMAND_CODE_BELL:
                    drawOnMe.ringBell();
                    break;
                case COMMAND_CODE_CUT:
                    serverCutText();
                    break;
                default:
                    drawOnMe.error("Server Sent Unknown Command! Command:" + code);
                    running = false;
                    break;
                }
            }
        } catch (Throwable t) {
            drawOnMe.error("rfb.run: t:" + t);
        }
    }

    /** Received a request from the VNC server to repaint */
    private static final int COMMAND_CODE_REPAINT = 0;

    /** Received a request from the VNC server to ring the bell */
    private static final int COMMAND_CODE_BELL = 2;

    /**
     * Received a request from the VNC server that the servers clipboard has changed
     */
    private static final int COMMAND_CODE_CUT = 3;

    /* The encodings */
    private static final int RAW_ENCODING = 0;

    private static final int COPY_RECT_ENCODING = 1;

    private static final int RRE_ENCODING = 2;

    private static final int CORRE_ENCODING = 4;

    private static final int HEXTILE_ENCODING = 5;

    /* The sub encodings for hexitile */
    private static final int RAW_SUB_ENCODING = 1;

    private static final int BG_SUB_ENCODING = 2;

    private static final int FG_SUB_ENCODING = 4;

    private static final int AS_SUB_ENCODING = 8;

    private static final int SC_SUB_ENCODING = 16;

    private int fg = 0;

    private int bg = 0;

    /**
     * An update request as been received.
     */
    private void update() throws Throwable {
        drawOnMe.startUpdate();
        readCard8(); // padding
        int noOfRects = readCard16();
// VNC.log("noOfRects " + noOfRects);
        for (int i = 0; i < noOfRects; i++) {
// VNC.log("rect" + i);
            int mx = readCard16(); // The offset x
            int my = readCard16(); // The offset y
            int w = readCard16(); // The width of the update
            int h = readCard16(); // The height of the update
            int e = readCard32(); // The encoding to use/
            int pixel; // The current pixel.

            int lx;
            int ly;
            int lw;
            int lh;
            int noRects;

            switch (e) {
            case RAW_ENCODING:
                for (int cy = 0; cy < h; cy++) {
                    for (int cx = 0; cx < w; cx++) {
                        pixel = readCard8();
                        drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx + cx, my + cy, 1, 1);
                    }
                }
                break;
            case RRE_ENCODING:
                noRects = readCard32();
                pixel = readCard8();
                drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx, my, w, h);
                for (int r = 0; r < noRects; r++) {
                    pixel = readCard8();
                    lx = readCard16();
                    ly = readCard16();
                    lw = readCard16();
                    lh = readCard16();
                    drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx + lx, my + ly, lw, lh);
                }
                break;
            case CORRE_ENCODING:
                noRects = readCard32();
                pixel = readCard8();
                drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx, my, w, h);
                for (int r = 0; r < noRects; r++) {
                    pixel = readCard8();
                    lx = readCard8();
                    ly = readCard8();
                    lw = readCard8();
                    lh = readCard8();
                    drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx + lx, my + ly, lw, lh);
                }
                break;
            case COPY_RECT_ENCODING:
                lx = readCard16();
                ly = readCard16();
                drawOnMe.copyRect(my, mx, w, h, lx, ly);
                break;
            case HEXTILE_ENCODING:

                int rows = w / 16;
                int sparew = 16;
                if (w != (rows * 16)) {
                    rows++;
                    sparew = 16 - ((rows * 16) - w);
                }
                int cols = h / 16;
                int spareh = 16;
                if (h != (cols * 16)) {
                    cols++;
                    spareh = 16 - ((cols * 16) - h);
                }

                for (int c = 0; c < cols; c++) {
                    for (int r = 0; r < rows; r++) {
                        int sub = readCard8();
                        lx = (r == rows - 1 ? sparew : 16);
                        ly = (c == cols - 1 ? spareh : 16);

                        if ((sub & RAW_SUB_ENCODING) != 0) {

                            for (int cy = 0; cy < ly; cy++) {
                                for (int cx = 0; cx < lx; cx++) {
                                    pixel = readCard8();
                                    drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx + (r * 16) + cx, my + (c * 16) + cy, 1, 1);
                                }
                            }
                        } else {
                            if ((sub & BG_SUB_ENCODING) != 0) {
                                bg = readCard8();
                            }
                            if ((sub & FG_SUB_ENCODING) != 0) {
                                fg = readCard8();
                            }
                            drawOnMe.draw((bg >> 0 & 7) * 36, (bg >> 3 & 7) * 36, (bg >> 6 & 3) * 85, mx + (r * 16), my + (c * 16), lx, ly);
                            boolean eachRectColoured = ((sub & SC_SUB_ENCODING) != 0);
                            if ((sub & AS_SUB_ENCODING) != 0) {
                                pixel = fg;
                                noRects = readCard8();
                                for (int loop = 0; loop < noRects; loop++) {
                                    if (eachRectColoured)
                                        pixel = readCard8();
                                    int xY = readCard8();
                                    int hW = readCard8();
                                    int tx = xY >> 4;
                                    int ty = xY & 0xf;
                                    int tw = (hW >> 4) + 1;
                                    int th = (hW & 0xf) + 1;
                                    drawOnMe.draw((pixel >> 0 & 7) * 36, (pixel >> 3 & 7) * 36, (pixel >> 6 & 3) * 85, mx + (r * 16) + tx, my + (c * 16) + ty, tw, th);
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
        drawOnMe.endUpdate();
    }

    private Event allocate(int size) {
// VNC.log("allocate");
        if (size > 10) {
            throw new IllegalArgumentException("allocate");
        }
        Event e = (Event) eventQueueCashe.pop();
        if (e == null) {
            e = new Event();
        }
        e.size = size;
        e.pc = 0;
// VNC.log("allocated" + e.size);
        return e;
    }

    /**
     * Move the mouse
     * 
     * @param x The X location to move the mouse too
     * @param y the Y location to move the mouse too
     * @param but The buttons status.
     */
    public void mouse(int x, int y, int but) throws Throwable {
        Event e = allocate(6);
        writeCard8(e, 5);
        writeCard8(e, but);
        writeCard16(e, x);
        writeCard16(e, y);
        flush(e);
    }

    /**
     * The VNC Server has a new clipboard
     * 
     * @todo Unnull it.
     */
    private void serverCutText() throws Throwable {
        readCard8();
        readCard8();
        readCard8(); // Padding

        for (int i = readCard32(); i > 0; i--) {
            readCard8();
        }
    }

    /**
     * Send a string to the VNC server.
     * 
     * @param value The string to send.
     */
    public void sendString(String value) throws Throwable {
        byte[] str = value.getBytes();
        for (int i = 0; i < str.length; i++) {
            key(str[i], true);
            key(str[i], false);
        }
    }

    /**
     * Sends a single key event to the server
     * 
     * @param keyCode The key
     * @param down If the key has gone down (true) or up (false)
     */
    public void key(int keyCode, boolean down) throws Throwable {
        Event e = allocate(8);
        writeCard8(e, 4); // Message Type
        writeCard8(e, (down ? 1 : 0));
        writeCard16(e, 0); // Padding
        writeCard32(e, keyCode);
        flush(e);
    }

    /**
     * Request that the VNC server sends you an update of the screen.
     * 
     * @param x The x offset.
     * @param y The y offset.
     * @param w The width of the rectangle.
     * @param h The height of the rectangle.
     * @param incremental Should the server assume the client knows what is currently on the screen
     */
    public void requestUpdate(int x, int y, int w, int h, boolean incremental) throws Throwable {
// VNC.log("requestUpdate");
        Event e = allocate(10);
        writeCard8(e, 3);
        writeCard8(e, (incremental ? 1 : 0));
        writeCard16(e, x);
        writeCard16(e, y);
        writeCard16(e, w);
        writeCard16(e, h);
        flush(e);
        returnDataExpected++;
// VNC.log("requestUpdated");
    }

    /**
     * Sets the default supported pixel format This implementation
     * only supports on pixel format this is it.
     * 
     * @todo deprecated and support other pixel formats
     */
    private void setDefaultSupportedPixelFormat() throws Throwable {
// VNC.log( "setDefaultSupportedPixelFormat" );
        Event e = allocate(8);
        writeCard8(e, 0); // msg type
        writeCard8(e, 0); // padding
        writeCard8(e, 0); // padding
        writeCard8(e, 0); // padding
        writeCard8(e, 8);
        writeCard8(e, 8);
        writeCard8(e, 0);
        writeCard8(e, 1);
        flush(e);
        e = allocate(9);
        writeCard16(e, 7);
        writeCard16(e, 7);
        writeCard16(e, 3);
        writeCard8(e, 0);
        writeCard8(e, 3);
        writeCard8(e, 6);
        flush(e);
        e = allocate(3);
        writeCard8(e, 0);
        writeCard8(e, 0);
        writeCard8(e, 0);
        flush(e);
    }

    /**
     * Sets the default encodings, it does not by default use
     * copy rect, as I don't trust my implementation. 
     * @todo Deprecated, and allow the creator to choose what is and is not
     *       supported.
     */
    private void setDefaultEncodings() throws Throwable {
//VNC.log( "setDefaultEncodings" );
        Event e = allocate(8);
        writeCard8(e, 2); // Msg type
        writeCard8(e, 0); // padding
        writeCard16(e, 3); // number of encodings
        writeCard32(e, HEXTILE_ENCODING);
        flush(e);

        e = allocate(8);
        writeCard32(e, CORRE_ENCODING);
        writeCard32(e, RRE_ENCODING);
        flush(e);
        /* removed, to save sending data over 
         e = allocate( 4 );  
         writeCard32( e, RAW_ENCODING );
         flush( e );	*/
    }

    /** Writes a 32bit card as defined by the RFB Protocol to the buffer 
     * ready to be flushed */
    private void writeCard32(Event e, int v) {
        e.buffer[e.pc++] = (byte) ((v & 0xff000000) >>> 24);
        e.buffer[e.pc++] = (byte) ((v & 0x00ff0000) >>> 16);
        e.buffer[e.pc++] = (byte) ((v & 0x0000ff00) >>> 8);
        e.buffer[e.pc++] = (byte) (v & 0x000000ff);
    }

    /** Writes a 16bit card as defined by the RFB Protocol to the buffer 
     * ready to be flushed  */
    private void writeCard16(Event e, int v) {
        e.buffer[e.pc++] = (byte) ((v & 0x0000ff00) >>> 8);
        e.buffer[e.pc++] = (byte) (v & 0x000000ff);
    }

    /** Writes a 8bit card as defined by the RFB Protocol to the buffer 
     * ready to be flushed  */
    private void writeCard8(Event e, int v) {
        e.buffer[e.pc++] = (byte) (v & 0xff);
    }

    /** Flushes the buffer to the VNC Server */
    private void flush(Event e) throws Throwable {
        if (Thread.currentThread() == me || !ncm) {
//VNC.log( "flushedCT" );
            for (int i = 0; i < e.size; i++) {
                sout.write(e.buffer[i]);
            }
            //sout.write( e.buffer, 0, e.size-1 );
            sout.flush();
            eventQueueCashe.push(e);
        } else {
//VNC.log("flushed " + e.size + " " + eventQueue.size());
            eventQueue.push(e);
        }
    }

    /** Reads a 8bit card as defined by the RFB Protocol */
    private int readCard8() throws Throwable {
        return ((byte) sin.read() & 0xff);
    }

    /** Reads a 16bit card as defined by the RFB Protocol */
    private int readCard16() throws Throwable {
        return (((byte) sin.read() << 8) & 0xff00) | ((byte) sin.read() & 0x00ff);
    }

    /** Reads a 32bit card as defined by the RFB Protocol */
    private int readCard32() throws Throwable {
        return (((byte) sin.read() << 24) & 0xff000000) | (((byte) sin.read() << 16) & 0x00ff0000) | (((byte) sin.read() << 8) & 0x0000ff00) | ((byte) sin.read() & 0x000000ff);
    }
}

/* */
