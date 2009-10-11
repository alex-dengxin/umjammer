/*
 * @(#) $Id: FileBasedIssuedCerts.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.ca;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Calendar;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.security.cert.X509Certificate;


public class FileBasedIssuedCerts implements IssuedCerts {
    private String indexFileName;

    private String dir;

    public FileBasedIssuedCerts(String indexFileName, String dir) {
        this.indexFileName = indexFileName;
        this.dir = dir;
    }

    public void add(Certificate cert) throws CADatabaseException {
        X509Certificate x509Cert;
        if (cert == null)
            throw new IllegalArgumentException("null argument");

        if (cert instanceof X509Certificate) {
            x509Cert = (X509Certificate) cert;
        } else {
            throw new CADatabaseException("unsupported certificate type: " + cert.getType());
        }
        String certFileName = dir + File.separator + x509Cert.getSerialNumber().toString() + ".cer";
        File certFile = new File(certFileName);
        if (certFile.exists()) {
            throw new CADatabaseException("certificate file exists: " + certFileName);
        }

        try {
            FileOutputStream fis = new FileOutputStream(certFileName);
            byte[] certBytes = x509Cert.getEncoded();
            fis.write(certBytes);
            fis.close();
        } catch (Exception exc) {
            throw new CADatabaseException("cannot write certificate to file: " + certFileName, exc);
        }

        StringBuffer sb = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        sb.append(cal.getTime().toString() + CADatabase.recordSeparator);
        sb.append(x509Cert.getSerialNumber().toString() + CADatabase.recordSeparator);
        sb.append(x509Cert.getNotBefore().toString() + CADatabase.recordSeparator);
        sb.append(x509Cert.getNotAfter().toString() + CADatabase.recordSeparator);
        sb.append(x509Cert.getSubjectDN().toString());

        try {
            PrintWriter pw = new PrintWriter(new FileWriter(indexFileName, true)); // Append mode
            pw.println(sb.toString());
            pw.close();
        } catch (Exception exc) {
            throw new CADatabaseException("cannot write to index file: " + indexFileName, exc);
        }
    }

    public boolean exists(Certificate cert) throws CADatabaseException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(indexFileName));
            X509Certificate x509Cert = (X509Certificate) cert;
            String serialNo = x509Cert.getSerialNumber().toString();
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] records = line.split(CADatabase.escapedRecordSeparator);
                if (serialNo.equals(records[1]))
                    return true;
            }
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (IOException ioe) {
            throw new CADatabaseException("CA database corrupted.", ioe);
        }
        return false;
    }

    // TODO
    public Iterator<?> iterator() {
        return null;
    }
}
