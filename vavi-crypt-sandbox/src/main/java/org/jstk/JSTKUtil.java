/*
 * @(#) $Id: JSTKUtil.java,v 1.1.1.1 2003/10/05 18:39:11 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

import java.io.*;


public class JSTKUtil {
    private static char[] hexChars = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String hexStringFromBytes(byte[] ba) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ba.length; i++) {
            if (i > 0 && (i & 0x0000001f) == 0)
                sb.append("\n");
            else if (i > 0 && (i & 0x00000003) == 0)
                sb.append("  ");
            sb.append(hexChars[(0x000000f0 & ba[i]) >> 4]);
            sb.append(hexChars[(0x0000000f & ba[i])]);
        }
        return sb.toString();
    }

    public static String[] hexStringArrayFromBytes(byte[] ba, int bps) {
        int n = ba.length / bps; // bps: bytes per string.
        n += (ba.length > (n * bps) ? 1 : 0);

        String[] sa = new String[n];
        for (int i = 0; i < n; i++) {
            StringBuffer sb = new StringBuffer();
            int off = i * bps;
            int remaining = (ba.length - off < bps ? ba.length - off : bps);
            for (int j = 0; j < remaining; j++) {
                if (j > 0 && (j & 0x00000003) == 0)
                    sb.append("  ");
                sb.append(hexChars[(0x000000f0 & ba[off + j]) >> 4]);
                sb.append(hexChars[(0x0000000f & ba[off + j])]);
            }
            sa[i] = sb.toString();
        }
        return sa;
    }

    public static String readableFromBytes(byte[] ba) {
        return readableFromBytes(ba, 0, ba.length);
    }

    public static String readableFromBytes(byte[] ba, int off, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = off; i < off + length; i += 20) { // 16 bytes per line.
            // Format::
            // offset: hhhh hhhh hhhh hhhh hhhh hhhh hhhh hhhh pppppppppppppppp
            // 1(b + 6 + 1(:) + 2(b) + [4(h) + 1(b)]*4 + 4(b) + [4(h) + 1(b)]*4 + 8(b) + 16(p)
            // sb.append(" ");
            String addr = String.valueOf(i);
            for (int j = 6 - addr.length(); j > 0; j--)
                sb.append("0");
            sb.append(addr);
            sb.append(":");
            for (int j = 0; j < 20; j++) {
                if (j == 10)
                    sb.append("  ");
                else if ((j & 0x00000003) == 0) // j is an odd number.
                    sb.append(" ");
                if (i + j < off + length) {
                    sb.append(hexChars[(0x000000f0 & ba[i + j]) >> 4]);
                    sb.append(hexChars[(0x0000000f & ba[i + j])]);
                } else {
                    sb.append("  ");
                }
            }
            sb.append("    ");
            String s = null;
            try {
                s = new String(ba, i, Math.min(20, off + length - i), "US-ASCII");
            } catch (Exception exc) {
                System.err.println("Condition Impossible. Exception: " + exc);
            }
            char[] ca = s.toCharArray();
            for (int j = 0; j < ca.length; j++) {
                if (Character.isISOControl(ca[j]))
                    sb.append(".");
                else
                    sb.append(ca[j]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static byte[] bytesFromHexString(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean firstHalf = true;

        int bb = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int b;
            switch (ch) {
            case '0':
                b = 0x00;
                break;
            case '1':
                b = 0x01;
                break;
            case '2':
                b = 0x02;
                break;
            case '3':
                b = 0x03;
                break;
            case '4':
                b = 0x04;
                break;
            case '5':
                b = 0x05;
                break;
            case '6':
                b = 0x06;
                break;
            case '7':
                b = 0x07;
                break;
            case '8':
                b = 0x08;
                break;
            case '9':
                b = 0x09;
                break;
            case 'a':
                b = 0x0a;
                break;
            case 'b':
                b = 0x0b;
                break;
            case 'c':
                b = 0x0c;
                break;
            case 'd':
                b = 0x0d;
                break;
            case 'e':
                b = 0x0e;
                break;
            case 'f':
                b = 0x0f;
                break;
            default:
                b = 0x10;
                break;
            }
            if (b != 0x10) {
                if (firstHalf) {
                    bb = (b << 4);
                    firstHalf = false;
                } else {
                    bb |= b;
                    firstHalf = true;
                    baos.write(bb);
                }
            }
        }
        return baos.toByteArray();
    }

    public static byte[] bytesFromFile(String filename) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = bis.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, n);
        }
        bis.close();
        return baos.toByteArray();
    }

    public static void bytesToFile(byte[] ba, String filename) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
        bos.write(ba);
        bos.close();
    }

    public static boolean equals(byte[] a, byte[] b) {
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        String s1 = "My name is Akriti Singh";
        String s2 = "My name is \tAkriti Singh\nWhat \bis your name?";
        byte[] buf1 = s1.getBytes();
        System.out.println("Hex readable of \"" + s1 + "\"::");
        System.out.println(readableFromBytes(buf1));
        System.out.println("Hex readable of \"" + s2 + "\"::");
        System.out.println(readableFromBytes(s2.getBytes()));
    }
}
