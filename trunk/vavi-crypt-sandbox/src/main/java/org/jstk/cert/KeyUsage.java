/*
 * @(#) $Id: KeyUsage.java,v 1.1.1.1 2003/10/05 18:39:14 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert;

public class KeyUsage {
    public static final int MAX_KEYUSAGE_INDEX = 8;

    public static final int DIGITAL_SIGNATURE = 0;

    public static final int NON_REPUDIATION = 1;

    public static final int KEY_ENCIPHERMENT = 2;

    public static final int DATA_ENCIPHERMENT = 3;

    public static final int KEY_AGREEMENT = 4;

    public static final int KEY_CERTSIGN = 5;

    public static final int CRL_SIGN = 6;

    public static final int ENCIPHER_ONLY = 7;

    public static final int DECIPHER_ONLY = 8;

    public static final String[] keyUsageString = new String[] {
        "digitalSignature", "nonRepudiation", "keyEncipherment", "dataEncipherment", "keyAgreement", "keyCertSign", "cRLSign", "encipherOnly", "decipherOnly"
    };

    private boolean[] keyUsage = new boolean[MAX_KEYUSAGE_INDEX + 1];

    public KeyUsage() {
        // No key usage set by default.
    }

    public KeyUsage(boolean[] keyUsage) {
        if (keyUsage == null)
            return;
        for (int i = 0; (i < this.keyUsage.length) || (i < keyUsage.length); i++) {
            this.keyUsage[i] = keyUsage[i];
        }
    }

    public void setKeyUsage(String kuString, boolean flag) {
        for (int i = 0; i < keyUsage.length; i++) {
            if (keyUsageString[i].equalsIgnoreCase(kuString)) {
                keyUsage[i] = flag;
            }
        }
    }

    public void setKeyUsage(int index, boolean flag) {
        if (index >= 0 && index <= MAX_KEYUSAGE_INDEX)
            keyUsage[index] = flag;
    }

    public String getKeyUsageString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (int i = 0; i < this.keyUsage.length; i++) {
            if (keyUsage[i]) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(keyUsageString[i]);
            }
        }
        return sb.toString();
    }

    public static String getKeyUsageString(int index) {
        if (index >= 0 && index <= MAX_KEYUSAGE_INDEX)
            return keyUsageString[index];
        return null;
    }

    public byte[] getBitString() {
        byte[] value = new byte[2];
        int mask = 0x80;
        for (int i = 0; i < 8; i++) {
            if (keyUsage[i])
                value[0] |= (byte) mask;
            mask >>= 1;
        }
        mask = 0x80;
        if (keyUsage[8])
            value[1] = (byte) mask;
        return value;
    }

    public int getNumUnusedBits() {
        return 7;
    }

    public static void main(String[] args) throws Exception {
        KeyUsage ku = new KeyUsage();
        ku.setKeyUsage("crlSign", true);
        System.out.println("KeyUsage: " + ku.getKeyUsageString());
        ku.setKeyUsage("nonRepudiation", true);
        System.out.println("KeyUsage: " + ku.getKeyUsageString());
    }
}
