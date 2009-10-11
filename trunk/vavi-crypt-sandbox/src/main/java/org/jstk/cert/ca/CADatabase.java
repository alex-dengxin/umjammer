/*
 * @(#) $Id: CADatabase.java,v 1.1.1.1 2003/10/05 18:39:14 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.ca;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.security.PrivateKey;
import java.math.BigInteger;
import java.io.FileInputStream;
import java.io.BufferedInputStream;


public class CADatabase {
    public final static String recordSeparator = "|";

    public final static String escapedRecordSeparator = "\\|";

    private CADatabaseSpi spi;

//    private String type;

    protected CADatabase(CADatabaseSpi spi, String type) {
        this.spi = spi;
//        this.type = type;
    }

    public static CADatabase getInstance(String type, CADatabaseParams params) throws CADatabaseException {
        if (params instanceof FileBasedCADatabaseParams)
            return new CADatabase(new FileBasedCADatabase((FileBasedCADatabaseParams) params), type);
        throw new CADatabaseException("CADatabaseParams not supported: " + params.getClass());
    }

    public IssuedCerts getIssuedCerts() {
        return spi.getIssuedCerts();
    }

    public RevokedCerts getRevokedCerts() {
        return spi.getRevokedCerts();
    }

    public Certificate getCACert() {
        return spi.getCACert();
    }

    public CertPath getCACertPath() {
        return spi.getCACertPath();
    }

    public PrivateKey getCAPrivateKey() {
        return spi.getCAPrivateKey();
    }

    public BigInteger nextSerialNumber() throws CADatabaseException {
        return spi.nextSerialNumber();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage:: java CADatabase {add|revoke} <cert_file>");
            return;
        }
        String cmd = args[0];
        String certFileName = args[1];
        FileInputStream fis = new FileInputStream(certFileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);

        FileBasedCADatabaseParams fbParams = new FileBasedCADatabaseParams("cadir");
        CADatabase cadb = CADatabase.getInstance("file", fbParams);
        if (cmd.equals("add")) {
            cadb.getIssuedCerts().add(cert);
            System.out.println("Added to Issued Certs: " + cert.getSerialNumber());
        } else if (cmd.equals("revoke")) {
            cadb.getRevokedCerts().add(cert);
            System.out.println("Added to Revoked Certs: " + cert.getSerialNumber());
        }
    }
}
