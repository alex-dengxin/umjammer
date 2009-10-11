/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * Multicast.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030326 nsano initial version <br>
 *          0.01 030328 nsano fix addGenericListener <br>
 */
public class Multicast extends GpsDevice {

    /** */
    private List<GpsDevice> outputDevices = new ArrayList<GpsDevice>();

    /** */
    public Multicast(String file) {

        try {
            Properties props = new Properties();

            props.load(Multicast.class.getResourceAsStream(file));

            int i = 0;
            while (true) {
                String key = "multicast.outputDevices.class." + i;
                String clazz = props.getProperty(key);
                if (clazz == null) {
Debug.println("device." + i + " not found, break");
                    break;
                }
                key = "multicast.outputDevices.name." + i;
                String name = props.getProperty(key);
                GpsDevice device = BasicGpsDevice.newInstance(clazz, name);
Debug.println("device." + i + ": " + name + ": " + clazz);
                outputDevices.add(device);
                i++;
            }
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** Starts children. */
    public void start() {
Debug.println("here");
        Iterator<GpsDevice> i = outputDevices.iterator();
        while (i.hasNext()) {
            i.next().start();
        }
    }

    /** @throws UnsupportedOperationException always be thrown */
    protected Runnable getInputThread() {
        throw new UnsupportedOperationException("This class cannot be input device.");
    }

    /** */
    protected GenericListener getOutputGenericListener() {

        return new GenericListener() {
            public void eventHappened(GenericEvent ev) {

                Iterator<GpsDevice> i = outputDevices.iterator();
                while (i.hasNext()) {
                    GenericListener listener = i.next().getOutputGenericListener();
                    listener.eventHappened(ev);
                }
            }
        };
    }

    /** 汎用イベントリスナを追加します。 */
    public void addGenericListener(GenericListener listener) {
        Iterator<GpsDevice> i = outputDevices.iterator();
        while (i.hasNext()) {
            i.next().addGenericListener(listener);
        }
    }
}

/* */
