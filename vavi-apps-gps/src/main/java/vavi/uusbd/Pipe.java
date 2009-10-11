/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.uusbd;


/**
 * Pipe.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version 0.00 030314 nsano initial version <br>
 */
public class Pipe {

    /** */
    @SuppressWarnings("unused")
    private boolean overlap;

    /** */
    @SuppressWarnings("unused")
    private int interfaceNo;

    /** */
    @SuppressWarnings("unused")
    private int pipeNo;

    /**
     * USB デバイスのエンドポイントにアクセスするためのオブジェクトを作成する。
     * 現在のバージョンではインターラプト転送、
     * バルク転送のエンドポイントについて有効である。
     * 
     * @param interfaceNo インターフェース番号
     * @param pipeNo パイプ番号
     * @throws UsbException USBデバイスに指定したパイプが無い
     */
    public Pipe(Usb usb, int interfaceNo, int pipeNo) throws UsbException {

        this.usbInstance = usb.instance;

        this.interfaceNo = interfaceNo;
        this.pipeNo = pipeNo;

        open(usbInstance, interfaceNo, pipeNo, false);
    }

    /**
     * USB デバイスのエンドポイントにアクセスするためのオブジェクトを
     * オーバーラップモードで作成する。
     * 
     * @param interfaceNo インターフェース番号
     * @param pipeNo パイプ番号
     * @param overlapped オーバーラップ
     * @throws UsbException USBデバイスに指定したパイプが無い
     */
    public Pipe(Usb usb, int interfaceNo, int pipeNo, boolean overlapped)
        throws UsbException {

        this.usbInstance = usb.instance;

        this.interfaceNo = interfaceNo;
        this.pipeNo = pipeNo;

        open(usbInstance, interfaceNo, pipeNo, overlapped);
    }

    /**
     * パイプをクローズします。
     */
    public native void close();

    /**
     * TODO
     */
    public native int read() throws UsbException;

    /**
     * 読み込みます．
     */
    public native int read(byte[] b, int off, int len) throws UsbException;

    /**
     * TODO
     */
    public native void write(int b) throws UsbException;

    /**
     * 書き込みます．
     */
    public native void write(byte[] b, int off, int len) throws UsbException;

    //-------------------------------------------------------------------------

    /** USB のハンドル */
    private long usbInstance;

    /** パイプのハンドル */
    @SuppressWarnings("unused")
    private long instance;

    /**
     * パイプを作成します。
     * 
     * @param usbHandle USB のハンドル
     * @param interfaceNo インターフェース番号
     * @param pipeNo パイプ番号
     * @param overlapped オーバーラップ
     * @throws UsbException USBデバイスに指定したパイプが無い
     */
    private native void open(long usbHandle,
                             int interfaceNo,
                             int pipeNo,
                             boolean overlapped)
        throws UsbException;

    /**
     * パイプをリセットします。
     */
    @SuppressWarnings("unused")
    private native void reset() throws UsbException;

    /**
     * パイプをアボートします。
     */
    @SuppressWarnings("unused")
    private native void abort() throws UsbException;

    /** */
    static {
        System.loadLibrary("UusbdWrapper");
    }
}

/* */
