/*
 * @(#) $Id: FileBasedCADatabase.java,v 1.2 2003/10/28 08:15:55 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net).
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the
 * root directory of the containing software.
 */
/* 10/27/03, Pankaj: Added storetype as an option to be read from file. */

package org.jstk.cert.ca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Vector;


public class FileBasedCADatabase implements CADatabaseSpi {
    public static final String KEYSTORE = "ca.ks";

    public static final String STORETYPE = "JCEKS";

    public static final String ALIAS = "cakey";

    public static final String STOREPASS = "changeit";

    public static final String KEYPASS = "changeit";

    public static final String ISSUED_DIR = "issued";

    public static final String REVOKED_DIR = "revoked";

    public static final String ISSUED_CERTS = "issued.certs";

    public static final String REVOKED_CERTS = "revoked.certs";

    public static final String SERIALNO_FILE = "serialno.cur";

    public static final String STORETYPE_FILE = "caks.type";

    public static final String START_SERIALNO = "999";

    public static final BigInteger ONE = new BigInteger("1");

    private String ksFileName;

    private String serialNoFileName;

    private String storeTypeFileName;

    private String caDirName;

    private PrivateKey caPrivateKey;

    private Certificate caCert;

    private CertPath caCertPath;

    private File caDir;

//    private boolean initialized = false;

    private String password;

    private String storeType;

    public FileBasedCADatabase(FileBasedCADatabaseParams params) throws CADatabaseException {
        String caDirName = params.getCADirName();
        try {
            password = params.getPassword();
            storeType = params.getStoreType();
            if (!params.getCreateCA()) {
                init(caDirName);
            } else {
                create(caDirName, params.getCACerts(), params.getCAPrivateKey());
            }
        } catch (CADatabaseException de) {
            throw de;
        } catch (Exception e) {
            throw new CADatabaseException("FileBasedCADatabase constructor failed.", e);
        }
//        initialized = true;
    }

    private void init(String caDirName) throws Exception {

        // Check for existence of caDirName and proper permissions
        this.caDirName = caDirName;
        caDir = new File(caDirName);
        if (!caDir.exists()) {
            throw new CADatabaseException("Directory not found: " + caDirName);
        } else if (!caDir.isDirectory()) {
            throw new CADatabaseException("Not a directory: " + caDirName);
        } else if (!caDir.canRead()) {
            throw new CADatabaseException("No read permission: " + caDirName);
        } else if (!caDir.canWrite()) {
            throw new CADatabaseException("No write permission: " + caDirName);
        }

        // Check for StoreType file
        storeTypeFileName = caDirName + File.separator + STORETYPE_FILE;
        BufferedReader br = new BufferedReader(new FileReader(storeTypeFileName));
        storeType = br.readLine();
        br.close();

        // Check CA KeyStore. It must have the private key and the CA certificate.
        ksFileName = caDirName + File.separator + KEYSTORE;
        FileInputStream fis = new FileInputStream(ksFileName);

        KeyStore caKeyStore = KeyStore.getInstance(storeType);
        caKeyStore.load(fis, password.toCharArray());
        fis.close();
        if (caKeyStore.isKeyEntry(ALIAS)) {
            caPrivateKey = (PrivateKey) caKeyStore.getKey(ALIAS, password.toCharArray());
            caCert = caKeyStore.getCertificate(ALIAS);
            Certificate[] caCerts = caKeyStore.getCertificateChain(ALIAS);
            if (caCert == null || caCerts == null) {
                throw new CADatabaseException("Certificate not found in keystore: " + ksFileName);
            }
            CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            Vector<Certificate> certVec = new Vector<Certificate>();
            for (int i = 0; i < caCerts.length; i++) {
                certVec.add(caCerts[i]);
            }
            caCertPath = certFac.generateCertPath(certVec);
        } else {
            throw new CADatabaseException("PrivateKey not found in keystore: " + ksFileName);
        }

        // Check for SerialNo. file
        serialNoFileName = caDirName + File.separator + SERIALNO_FILE;
        File serialNoFile = new File(serialNoFileName);
        if (!serialNoFile.exists()) {
            PrintWriter pw = new PrintWriter(new FileWriter(serialNoFileName));
            pw.println(START_SERIALNO);
            pw.close();
        }
    }

    private void create(String caDirName, Certificate[] caCerts, PrivateKey caKey) throws Exception {

        // Check for existence of caDirName and proper permissions
        this.caDirName = caDirName;
        caDir = new File(caDirName);
        if (caDir.exists()) {
            throw new CADatabaseException("Directory exists: " + caDirName);
        }
        if (!caDir.mkdirs()) {
            throw new CADatabaseException("Directory creation failed: " + caDirName);
        }

        // Check CA KeyStore. It must have the private key and the CA certificate.
        ksFileName = caDirName + File.separator + KEYSTORE;
        FileOutputStream fos = new FileOutputStream(ksFileName);

        KeyStore caKeyStore = KeyStore.getInstance(storeType);
        caKeyStore.load(null, password.toCharArray());
        caKeyStore.setKeyEntry(ALIAS, caKey, password.toCharArray(), caCerts);

        caKeyStore.store(fos, password.toCharArray());
        fos.close();

        String issuedCertsDirName = caDirName + File.separator + ISSUED_DIR;
        File issuedCertsDir = new File(issuedCertsDirName);
        issuedCertsDir.mkdirs();

        String revokedCertsDirName = caDirName + File.separator + REVOKED_DIR;
        File revokedCertsDir = new File(revokedCertsDirName);
        revokedCertsDir.mkdirs();

        // Create Serial Number file.
        serialNoFileName = caDirName + File.separator + SERIALNO_FILE;
        File serialNoFile = new File(serialNoFileName);
        if (!serialNoFile.exists()) {
            PrintWriter pw = new PrintWriter(new FileWriter(serialNoFileName));
            pw.println(START_SERIALNO);
            pw.close();
        }

        // Create StoreType file
        storeTypeFileName = caDirName + File.separator + STORETYPE_FILE;
        File storeTypeFile = new File(storeTypeFileName);
        if (!storeTypeFile.exists()) {
            PrintWriter pw = new PrintWriter(new FileWriter(storeTypeFileName));
            pw.println(storeType);
            pw.close();
        }
    }

    public IssuedCerts getIssuedCerts() {
        String issuedCertsFileName = caDirName + File.separator + ISSUED_CERTS;
        String issuedCertsDirName = caDirName + File.separator + ISSUED_DIR;
        return new FileBasedIssuedCerts(issuedCertsFileName, issuedCertsDirName);
    }

    public RevokedCerts getRevokedCerts() {
        String revokedCertsFileName = caDirName + File.separator + REVOKED_CERTS;
        String revokedCertsDirName = caDirName + File.separator + REVOKED_DIR;
        return new FileBasedRevokedCerts(revokedCertsFileName, revokedCertsDirName);
    }

    public Certificate getCACert() {
        return caCert;
    }

    public CertPath getCACertPath() {
        return caCertPath;
    }

    public PrivateKey getCAPrivateKey() {
        return caPrivateKey;
    }

    public synchronized BigInteger nextSerialNumber() throws CADatabaseException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(serialNoFileName));
            String serialNoString = br.readLine();
            BigInteger serialNo = new BigInteger(serialNoString);
            br.close();
            serialNo = serialNo.add(ONE);
            PrintWriter pw = new PrintWriter(new FileWriter(serialNoFileName));
            pw.println(serialNo.toString());
            pw.close();
            return serialNo;
        } catch (Exception e) {
            throw new CADatabaseException("cannot get next serial number.", e);
        }
    }
}
