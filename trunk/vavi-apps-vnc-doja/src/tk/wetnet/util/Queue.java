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

package tk.wetnet.util;

public class Queue {
    static public class QueueEntry {
        public QueueEntry() {
        }

        private QueueEntry next;

        private QueueEntry prev;
    }

    public Queue() {
    }

    QueueEntry first = null;

    QueueEntry last = null;

    public synchronized boolean isEmpty() {
        return first == null;
    }

    public synchronized int size() {
        if (isEmpty())
            return 0;
        QueueEntry e = first;
        int c = 0;
        while (e != null) {
            c++;
            e = e.next;
        }
        return c;
    }

    public synchronized void push(QueueEntry o) {
        if (first == null) {
            first = o;
            o.next = null;
            o.prev = null;
            last = o;
            return;
        }
        o.next = first;
        first.prev = o;
        o.prev = null;
        first = o;
    }

    public synchronized QueueEntry pop() {
        QueueEntry r = last;
        if (r != null) {
            if (last == first) {
                first = null;
                last = null;
                return r;
            }
            last = r.prev;
            r.prev = null;
        }
        return r;
    }
}

/* */
