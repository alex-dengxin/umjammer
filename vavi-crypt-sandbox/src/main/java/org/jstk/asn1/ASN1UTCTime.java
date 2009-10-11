/*
 * @(#) $Id: ASN1UTCTime.java,v 1.1.1.1 2003/10/05 18:39:12 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.asn1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * ASN1UTCTime.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2005/03/17 nsano initial version <br>
 */
public class ASN1UTCTime extends ASN1Type {
    /** */
//    private static final byte[] bytes = {
//        0x39, 0x31, 0x30, 0x35, 0x30, 0x36, 0x32, 0x33, 0x34, 0x35, 0x34, 0x30, 0x5a
//    };

    /** */
    public ASN1UTCTime() {
        super(UNIVERSAL, NONE, UTCTime, UTCTime);
    }

    /** */
    public java.util.Date getDate() {
        DateFormat sdf = null;
        if (value.length == 11 || value.length == 15) {
            sdf = new SimpleDateFormat("yyMMddHHmmZ");
        } else {
            sdf = new SimpleDateFormat("yyMMddHHmmssZ");
        }
        String text = new String(value);
        text = text.replaceAll("Z", "+0000");
        java.util.Date date = sdf.parse(text, new java.text.ParsePosition(0));

        return date;
    }

    /** */
    public void setDate(java.util.Date date) {
        StringBuffer sb = new StringBuffer();
        DateFormat sdf = new SimpleDateFormat("yyMMddHHmmZ");
        sdf.format(date, sb, new java.text.FieldPosition(0));
        String dt = sb.toString().substring(0, 10) + "Z";
        setValue(dt.getBytes());
    }

    /** */
    public String toString() {
        if (value == null) {
            return "ASN1UTCTime: null";
        }
        return "ASN1UTCTime: " + new String(value);
    }

    /** */
    public static void main(String[] args) {
        final byte[] bytes1 = {
            0x39, 0x31, 0x30, 0x35, 0x30, 0x36, 0x32, 0x33, 0x34, 0x35, 0x34, 0x30, 0x5a
        };
        ASN1UTCTime ut1 = new ASN1UTCTime();
        ut1.setValue(bytes1);
        System.out.println(ut1.toString());
        System.out.println(ut1.getDate().toString());

        final byte[] bytes2 = {
            0x39, 0x31, 0x30, 0x35, 0x30, 0x36, 0x32, 0x33, 0x34, 0x35, 0x34, 0x30, 0x2d, 0x30, 0x37, 0x30, 0x30
        };
        ASN1UTCTime ut2 = new ASN1UTCTime();
        ut2.setValue(bytes2);
        System.out.println(ut2.toString());
        System.out.println(ut2.getDate().toString());
    }
}

/* */
