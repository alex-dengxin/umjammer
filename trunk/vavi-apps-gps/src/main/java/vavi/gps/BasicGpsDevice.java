/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import vavi.io.IODevice;
import vavi.io.IODeviceInputStream;
import vavi.io.IODeviceOutputStream;
import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * BasicGpsDevice. 
 * サブクラスは必ず (Ljava/lang/String;) のシグネチャを持つ
 * コンストラクタを 持たなければなりません。
 * 
 * @see #newInstance(String,String)
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030326 nsano initial version <br>
 *          0.01 030328 nsano be free from SocketException <br>
 */
public abstract class BasicGpsDevice extends GpsDevice {

    /** */
    public static final GpsDevice newInstance(String className, String name)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {

        return (GpsDevice) newInstanceInternal(className, name);
    }

    /** */
    private static Object newInstanceInternal(String className, String name)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {
//Debug.println(className + ": " + name);
        Class<?> clazz = Class.forName(className);
        Constructor<?> c = clazz.getConstructor(String.class);
        return c.newInstance(name);
    }

    /** 識別子(各デバイスのプロパティファイルにリストされたものを指定) */
    protected String name;

    /**
     * @param name プロパティファイルのどの IODevice を使用するかを指定
     */
    public BasicGpsDevice(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------

    /** このデバイスのフォーマッタを取得します。 */
    protected abstract GpsFormat getGpsFormat();

    /** このデバイスの IO デバイスクラスを取得します。 */
    protected abstract String getIODeviceClass();

    /**
     * このデバイスの IO デバイスクラスの識別子を取得します。
     * (シリアルポート名や IP のポート番号が指定され重複オープンを避けます)
     */
    protected abstract String getIODeviceName();

    /** */
    protected IODeviceInputStream is;

    /** */
    protected IODeviceOutputStream os;

    /** IO デバイスの識別子、IO デバイスのペア */
    private Map<String,IODevice> ioDevices = new HashMap<String,IODevice>();

    /**
     * IO デバイスを取得します。
     * ここでインスタンス化される {@link IODevice} の実装クラスは必ず
     * (Ljava/lang/String;) のシグネチャを持つコンストラクタを
     * 持たなければなりません。
     */
    private IODevice getIODevice()
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {

        String className = getIODeviceClass();
        String name = getIODeviceName();

        if (ioDevices.containsKey(name)) {
            return ioDevices.get(name);
        } else {
            IODevice ioDevice = (IODevice) newInstanceInternal(className, name);
            ioDevices.put(name, ioDevice);
Debug.println("name: " + name + ": " + className);
            return ioDevice;
        }
    }

    /**
     * 入力ストリームをオープンしていなければオープンします。
     * コンストラクタでオープンしないのは片方のみのデバイスも在るため
     */
    protected void makeSureInputStreamOpened() {
        if (this.is != null) {
            return;
        }

        try {
            IODevice ioDevice = getIODevice();
            this.is = new IODeviceInputStream(ioDevice);
        } catch (Exception e) {
if (e instanceof InvocationTargetException)
 Debug.printStackTrace(((InvocationTargetException) e).getTargetException());
else           
 Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }

    /** */
    protected Runnable getInputThread() {
        makeSureInputStreamOpened();

        return new Runnable() {
            public void run() {
Debug.println("IN[" + getIODeviceName() + "]: thread started");

                while (loop) {
                    byte[] line = null;
                    try {
                        line = is.readLine().getBytes();
                        GpsData gpsData = getGpsFormat().parse(line);
                        fireEventHappened(new GenericEvent(this,
                                                           "data",
                                                           gpsData));
                    } catch (IllegalArgumentException e) {
//Debug.printStackTrace(e);
System.err.println("IN[" + getIODeviceName() + "]> " + new String(line));
                    } catch (IOException e) {
if (e instanceof java.net.SocketException)
 Debug.println(e.getMessage());
else
 Debug.printStackTrace(e);
                    }
                }

Debug.println("IN[" + getIODeviceName() + "]: thread stoped");
            }
        };
    }

    /**
     * 出力ストリームをオープンしていなければオープンします。
     * コンストラクタでオープンしないのは片方のみのデバイスも在るため
     */
    protected void makeSureOutputStreamOpened() {
        if (this.os != null) {
            return;
        }

        try {
            IODevice ioDevice = getIODevice();
            this.os = new IODeviceOutputStream(ioDevice);
        } catch (Exception e) {
if (e instanceof InvocationTargetException)
 Debug.printStackTrace(((InvocationTargetException) e).getTargetException());
else           
 Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }

    /** */
    protected GenericListener getOutputGenericListener() {
        makeSureOutputStreamOpened();

        // TODO check multiple instantiation
        return new GenericListener() {
            public void eventHappened(GenericEvent ev) {
                try {
                    GpsData gpsData = (GpsData) ev.getArguments()[0];
                    byte[] line = getGpsFormat().format(gpsData);
                    os.writeLine(new String(line));
                } catch (Exception e) {
if (e instanceof java.net.SocketException)
 Debug.println(e.getMessage());
else
 Debug.printStackTrace(e);
                }
            }
        };
    }
}

/* */
