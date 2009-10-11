/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * SharedFifo.
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030331	nsano	initial version <br>
 */
public class SharedFifo<E> extends LinkedList<E> {

    /** */
    private static Map<String, SharedFifo<?>> instances = new HashMap<String, SharedFifo<?>>();

    /** */
    @SuppressWarnings("unchecked")
    public static <E> SharedFifo<E> newInstance(String name) {
        if (instances.containsKey(name)) {
            return (SharedFifo<E>) instances.get(name);
        } else {
            synchronized (instances) {
                SharedFifo<E> instance = new SharedFifo<E>();
                instances.put(name, instance);
                return instance;
            }
        }
    }
}

/* */
