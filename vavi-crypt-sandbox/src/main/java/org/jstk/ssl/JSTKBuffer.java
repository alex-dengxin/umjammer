/*
 * @(#) $Id: JSTKBuffer.java,v 1.1.1.1 2003/10/05 18:39:22 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import org.jstk.JSTKArgs;
import org.jstk.JSTKOptions;
import java.nio.ByteBuffer;


public abstract class JSTKBuffer {
    public static class NIOByteBuffer extends JSTKBuffer {
        private ByteBuffer bb;

        protected NIOByteBuffer(int bufsize) {
            bb = ByteBuffer.allocateDirect(bufsize);
        }

        public int length() {
            return bb.capacity();
        }

        public int getNBytes() {
            return bb.position();
        }

        public void setNBytes(int n) {
        }

        public byte[] getBytes() {
            bb.flip();
            byte[] buf = new byte[bb.limit()];
            bb.get(buf);
            bb.clear();
            return buf;
        }

        public void putBytes(byte[] buf) {
            bb.put(buf);
        }

        public void putBytes(byte[] buf, int off, int len) {
            bb.put(buf, off, len);
        }

        public ByteBuffer getByteBuffer() {
            return bb;
        }

        public byte[] getByteArray() {
            return null;
        }

        public void clear() {
            bb.clear();
        }
    }

    public static class OrdByteBuffer extends JSTKBuffer {
        byte[] buf;

        int n;

        protected OrdByteBuffer(int bufsize) {
            buf = new byte[bufsize];
            n = 0;
        }

        public int length() {
            return buf.length;
        }

        public int getNBytes() {
            return n;
        }

        public void setNBytes(int n) {
            this.n = n;
        }

        public byte[] getBytes() {
            byte[] tbuf = new byte[n];
            System.arraycopy(buf, 0, tbuf, 0, n);
            n = 0;
            return tbuf;
        }

        public void putBytes(byte[] tbuf) {
            System.arraycopy(tbuf, 0, buf, n, tbuf.length);
            n += tbuf.length;
        }

        public void putBytes(byte[] tbuf, int off, int len) {
            System.arraycopy(tbuf, off, buf, n, len);
            n += len;
        }

        public ByteBuffer getByteBuffer() {
            return null;
        }

        public byte[] getByteArray() {
            return buf;
        }

        public void clear() {
        }
    }

    public static JSTKBuffer getInstance(int bufsize, JSTKArgs args) {
        boolean nio = Boolean.valueOf(args.get("nio")).booleanValue();
        if (nio)
            return new NIOByteBuffer(bufsize);
        else
            return new OrdByteBuffer(bufsize);
    }

    public static JSTKBuffer getInstance(int bufsize) {
        return new NIOByteBuffer(bufsize);
    }

    public abstract int length();

    public abstract int getNBytes();

    public abstract void setNBytes(int n);

    public abstract byte[] getBytes();

    public abstract void putBytes(byte[] buf);

    public abstract void putBytes(byte[] buf, int off, int len);

    public abstract ByteBuffer getByteBuffer();

    public abstract byte[] getByteArray();

    public abstract void clear();

    public static void main(String[] args) {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        byte[] buf;
        String data = "test data";
        JSTKBuffer jb = JSTKBuffer.getInstance(1024, opts);

        System.out.println("First Round::");
        System.out.println("jb.length() = " + jb.length() + ", jb.bytes() = " + jb.getNBytes());
        jb.putBytes(data.getBytes());
        System.out.println("jb.length() = " + jb.length() + ", jb.bytes() = " + jb.getNBytes());
        buf = jb.getBytes();
        System.out.println("buf = " + new String(buf));

        System.out.println("Second Round::");
        System.out.println("jb.length() = " + jb.length() + ", jb.bytes() = " + jb.getNBytes());
        jb.putBytes(data.getBytes());
        System.out.println("jb.length() = " + jb.length() + ", jb.bytes() = " + jb.getNBytes());
        buf = jb.getBytes();
        System.out.println("buf = " + new String(buf));
    }
}
