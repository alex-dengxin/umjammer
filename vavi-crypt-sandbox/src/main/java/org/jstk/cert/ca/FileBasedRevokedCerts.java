/*
 * @(#) $Id: FileBasedRevokedCerts.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.ca;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Iterator;


public class FileBasedRevokedCerts implements RevokedCerts {
    private String indexFileName;

//    private String dir;

    public class FileBasedRevokedCertsIterator implements Iterator<RevokedCert> {
        private String[] records = null;

        private BufferedReader br = null;

        public FileBasedRevokedCertsIterator() {
            try {
                br = new BufferedReader(new FileReader(indexFileName));
                String line = br.readLine();
                if (line != null)
                    records = line.split(CADatabase.escapedRecordSeparator);
            } catch (FileNotFoundException fnfe) {
                // Do nothing.
            } catch (IOException ioe) {
                System.err.println("Unexpected exception: " + ioe);
                records = null;
            }
        }

        public boolean hasNext() {
            return (records != null);
        }

        public RevokedCert next() {
            RevokedCert rc = new RevokedCert(records[0], records[1]);
            try {
                String line = br.readLine();
                if (line != null)
                    records = line.split(CADatabase.escapedRecordSeparator);
                else
                    records = null;
            } catch (IOException ioe) {
                System.err.println("Unexpected exception: " + ioe);
                records = null;
            }
            return rc;
        }

        public void remove() {
        }
    }

    public FileBasedRevokedCerts(String indexFileName, String dir) {
        this.indexFileName = indexFileName;
//        this.dir = dir;
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
        add(x509Cert.getSerialNumber());
    }

    public void add(BigInteger serialNo) throws CADatabaseException {
//        String certFileName = dir + File.separator + serialNo.toString() + ".cer";

        StringBuffer sb = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        sb.append(cal.getTime().toString() + CADatabase.recordSeparator);
        sb.append(serialNo.toString() + CADatabase.recordSeparator);

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

    public Iterator<RevokedCert> iterator() {
        return new FileBasedRevokedCertsIterator();
    }
}
