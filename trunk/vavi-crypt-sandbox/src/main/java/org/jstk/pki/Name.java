/*
 * @(#) $Id: Name.java,v 1.1.1.1 2003/10/05 18:39:21 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.pki;

import java.io.IOException;

import org.jstk.asn1.ASN1Any;
import org.jstk.asn1.ASN1Oid;
import org.jstk.asn1.ASN1PrintableString;
import org.jstk.asn1.ASN1PullParser;
import org.jstk.asn1.ASN1PullParserException;
import org.jstk.asn1.ASN1Seq;
import org.jstk.asn1.ASN1Set;


public class Name extends ASN1Seq {
    public static class OVPair extends ASN1Seq {
        ASN1Oid oid = new ASN1Oid();

        ASN1Any val = new ASN1Any();

        OVPair() {
            super();
            add(oid);
            add(val);
        }
    }

    public Name() {
        super();
    }

    public void reinitialize(Name name) {
        elems = name.elems; // Shallow copy. beware !!
    }

    public void decode(ASN1PullParser parser) throws ASN1PullParserException, IOException {
        int event;
        if (parser.next() != ASN1PullParser.START_SEQ)
            throw new ASN1PullParserException("unexpected type");
        while ((event = parser.next()) != ASN1PullParser.END_SEQ) {
            if (event != ASN1PullParser.START_SET)
                throw new ASN1PullParserException("unexpected type");
            ASN1Set rdn = new ASN1Set();
            while ((event = parser.next()) != ASN1PullParser.END_SET) {
                parser.prev();
                OVPair ovp = new OVPair();
                ovp.decode(parser);
                rdn.add(ovp);
            }
            add(rdn);
        }
    }

    public void add(String oid, String value) {
        ASN1PrintableString ps = new ASN1PrintableString();
        ps.setString(value);

        OVPair ovp = new OVPair();
        ovp.oid.setOid(oid);
        ovp.val.setInstance(ps);
        ASN1Set rdn = new ASN1Set();
        rdn.add(ovp);
        add(rdn);
    }

    public String getOid(int index) {
        ASN1Set rdn = (ASN1Set) get(index);
        OVPair ovp = (OVPair) rdn.elementAt(0);
        return ovp.oid.toString();
    }

    public String getValue(int index) {
        ASN1Set rdn = (ASN1Set) get(index);
        OVPair ovp = (OVPair) rdn.elementAt(0);
        return ovp.val.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            ASN1Set rdn = (ASN1Set) get(i);
            for (int j = 0; j < rdn.size(); j++) {
                if (j > 0)
                    sb.append("; ");
                OVPair ovp = (OVPair) rdn.elementAt(j);
                sb.append(ovp.oid.toString() + "=" + ovp.val.toString());
            }
            if (i > 0)
                sb.append("; ");
        }
        return sb.toString();
    }
}
