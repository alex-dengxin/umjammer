/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import vavi.net.inet.InetServer;
import vavi.net.inet.SocketHandlerFactory;
import vavi.util.Debug;


/**
 * Output data to virtual com port via network.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 *          0.01 030322 nsano repackage <br>
 *          0.02 030328 nsano remove input task <br>
 */
public class InetServerDevice implements IODevice {

    /** */
    private int port = 5750;

    /** */
    private Socket socket;

    /** */
    private InputStream is;

    /** */
    private OutputStream os;

    /**
     * @param name IP のポート番号を指定します
     */
    public InetServerDevice(String name) throws IOException {

        this.port = Integer.parseInt(name);

        InetServer server = new InetServer(port);
        server.setSocketHandlerFactory(acceptanceListenerFactory);
        server.start();
    }

    /** */
    private SocketHandlerFactory acceptanceListenerFactory = new SocketHandlerFactory() {
        private Runnable socketHandler = new Runnable() {
            public void run() {
            }
        };
        /** */
        public Runnable getSocketHandler(Socket socket) {
            InetServerDevice.this.socket = socket;
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
            } catch (IOException e) {
Debug.println(e);
            }
            return socketHandler;
        }
    };

    /** */
    public int read() throws IOException {
        while (is == null) { // TODO
            Thread.yield();
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }
        }

        try {
            return is.read();
        } catch (SocketException e) {
            is = null;
            os = null;
            throw e;
        }
    }

    /** */
    public int available() throws IOException {
        if (is != null) {
            try {
                return is.available();
            } catch (SocketException e) {
                is = null;
                os = null;
                throw e;
            }
        } else {
            return 0;
        }
    }

    /**
     * コネクションが確立していない時点の書き込みは破棄されます。
     */
    public void write(int b) throws IOException {
        if (os != null) {
            try {
// Debug.println(StringUtil.toHex2(b) + ": " + (char) b);
                os.write(b);
            } catch (SocketException e) {
                is = null;
                os = null;
                throw e;
            }
        }
    }

    /**
     * コネクションが確立していない時点のフラッシュは破棄されます。
     */
    public void flush() throws IOException {
        if (os != null) {
            try {
                os.flush();
            } catch (SocketException e) {
                is = null;
                os = null;
                throw e;
            }
        }
    }

    /** */
    public void close() throws IOException {

        try {
            os.close();
            is.close();

            socket.close();
        } catch (SocketException e) {
            throw e;
        } finally {
            os = null; // コネクション確立のフラグとして使用
            is = null; // コネクション確立のフラグとして使用
        }
    }
}

/* */
