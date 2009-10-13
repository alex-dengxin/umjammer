/*
 * This file is part of J2ME VNC.
 *
 * Copyright (c) 2004 Michael Lloyd Lee
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

package tk.wetnet.j2me.httpsocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.nttdocomo.io.HttpConnection;

import tk.wetnet.util.Queue;


/**
 * HTTP Socket implements a client-side code for sockets proxy over HTTP.
 * 
 * @auther Mike
 */
public class HTTPSocket extends InputStream implements StreamConnection {
    /** Are the streams closed */
    private boolean closed = false;
    /** */
    private OutputStream os = new OutputStream() {
        /** */
        public void flush() throws IOException {
            if (closed) {
                throw new IOException("Closed Stream");
            }
            synchronized (host) {
                HttpConnection c1 = (HttpConnection) Connector.open(proxy + "?t=" + ticket, Connector.READ_WRITE, true);

                // Set the request method and headers
                c1.setRequestMethod(HttpConnection.POST);

                OutputStream os = c1.openOutputStream();
                while (outputs.isEmpty() == false) {
                    OutputQueueEntry oqe = (OutputQueueEntry) outputs.pop();
                    os.write(oqe.data);
                    outputsCashe.push(oqe);
                }
                for (int i = 0; i <= outPC; i--) {
                    os.write(current.data[i]);
                }
                outPC = 0;
                os.close();

                InputStream is = c1.openInputStream();

                // I should check & stuff
                c1.close();
            }
        }

        /** */
        public void write(int yum) throws IOException {
            if (closed) {
                throw new IOException("Closed Stream");
            }
            synchronized (host) {
                if (outPC == 512) {
                    outputs.push(current);
                    outPC = 0;
                    if (outputsCashe.size() == 0) {
                        current = new OutputQueueEntry();
                    } else {
                        current = (OutputQueueEntry) outputsCashe.pop();
                    }
                }
                current.data[++outPC] = (byte) yum;
            }
        }
    };

    /** pc, the current buffer counter. length the current length */
    private int pc = 0;

    /** pc, the current buffer counter. length the current length */
    private int length = 0;

    /** The buffer for the IS */
    private byte[] buffer = new byte[255];

    private OutputQueueEntry current = new OutputQueueEntry();

    private Queue outputs = new Queue();

    private Queue outputsCashe = new Queue();

    /** The current OS buffer counter */
    private int outPC = 0;

    /** Host, doubles as the lock. */
    private String host;

    private String proxy;

    private String ticket = null;

    /** */
    public HTTPSocket(String host, String proxy) {
        this.host = host;
        this.proxy = proxy;

        outputs.push(new OutputQueueEntry());
    }

    /** */
    public void ready() throws IOException {
        synchronized (host) {
            HttpConnection c = (HttpConnection) Connector.open(proxy + "?H=" + host);
            if (c.getResponseCode() == HttpConnection.HTTP_CREATED) {
                ticket = c.getHeaderField("t");
            }
            c.close();
        }
    }

    /** */
    private void request() throws IOException {
        HttpConnection c = (HttpConnection) Connector.open(proxy + "?t=" + ticket + "&G=255");
        if (c.getResponseCode() == HttpConnection.HTTP_OK) {
            InputStream is = c.openInputStream();
            int i = is.read();
            int counter = 0;
            while (i != -1) {
                buffer[counter] = (byte) i;
                i = is.read();
            }
            length = counter;
        }
        c.close();
    }

    /** InputStream */
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Closed Stream");
        }
        while (pc == length) {
            request();
            try {
                if (pc == length) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ie) {
            }
        }
        return buffer[++pc];
    }

    /** InputConnection */
    public DataInputStream openDataInputStream() throws IOException {
        if (closed) {
            throw new IOException("Closed Stream");
        }
        return new DataInputStream(this);
    }

    /** */
    public InputStream openInputStream() throws IOException {
        if (closed) {
            throw new IOException("Closed Stream");
        }
        return this;
    }

    /** javax.microedition.io.Connection */
    public void close() throws IOException {
        if (closed) {
            throw new IOException("Closed Stream");
        }
        closed = true;
    }

    /** javax.microedition.io.OutputConnection */
    public DataOutputStream openDataOutputStream() throws IOException {
        if (closed) {
            throw new IOException("Closed Stream");
        }
        return new DataOutputStream(os);
    }

    /** */
    public OutputStream openOutputStream() throws IOException {
        if (closed) {
            throw new IOException("Closed Stream");
        }
        return os;
    }
}

/**
 * OutputQueueEntry.
 * 
 * @version 0.00 040911 nsano initial version <br>
 */
class OutputQueueEntry extends Queue.QueueEntry {
    byte[] data = new byte[512];
}

/* */
