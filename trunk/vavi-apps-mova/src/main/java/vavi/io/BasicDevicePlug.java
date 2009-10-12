/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.io;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * BasicDevice.
 * サブクラスは必ず (Ljava/lang/String;) のシグネチャを持つコンストラクタを
 * 持たなければなりません。
 *		
 * @see		#newInstance(String,String)
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version	0.00	040309	nsano	initial version <br>
 */
public abstract class BasicDevicePlug extends AbstractDevicePlug {

    /** */
    public static final AbstractDevicePlug newInstance(String className, String name)
        throws ClassNotFoundException,
               NoSuchMethodException,
               InstantiationException,
               IllegalAccessException,
               InvocationTargetException {

        return (AbstractDevicePlug) newInstanceInternal(className, name);
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
     * @param	name	プロパティファイルのどの IODevice を使用するかを指定
     */
    public BasicDevicePlug(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------

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
     * ここでインスタンス化される IODevice の実装クラスは必ず
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
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
Debug.printStackTrace(t);
            throw (RuntimeException) new IllegalStateException().initCause(t);
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    protected Runnable getInputThread() {
        makeSureInputStreamOpened();

        return new Runnable() {
            public void run() {
Debug.println("IN[" + getIODeviceName() + "]: thread started");

                while (loop) {
                    int c = -1;
                    try {
                        c = is.read();
                        if (c == -1) {
//Debug.println("IN[" + getIODeviceName() + "]: -1 received");
                        } else {
Debug.println("IN[" + getIODeviceName() + "]: " + StringUtil.toHex2(c));
                            fireEventHappened(
                                new GenericEvent(this,
                                                 "data",
                                                 new Integer(c)));
                        }
                    } catch (IllegalArgumentException e) {
//Debug.printStackTrace(e);
System.err.println("IN[" + getIODeviceName() + "]> " + (char) c);
                    } catch (java.net.SocketException e) {
Debug.println(e.getMessage());
                    } catch (IOException e) {
Debug.printStackTrace(e);
                    }
                }

Debug.println("IN[" + getIODeviceName() + "]: thread stopped");
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
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
Debug.printStackTrace(t);
            throw (RuntimeException) new IllegalStateException().initCause(t);
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    protected GenericListener getOutputGenericListener() {
        makeSureOutputStreamOpened();

        // TODO check multiple instantiation
        return new GenericListener() {
            public void eventHappened(GenericEvent ev) {
                try {
                    int c = ((Integer) ev.getArguments()[0]).intValue();
//Debug.println("OUT[" + getIODeviceName() + "]: " + StringUtil.toHex2(c));
                    os.write(c);
//                  os.flush();
                } catch (java.net.SocketException e) {
Debug.println(e.getMessage());
                } catch (Exception e) {
Debug.printStackTrace(e);
                }
            }
        };
    }
}

/* */
