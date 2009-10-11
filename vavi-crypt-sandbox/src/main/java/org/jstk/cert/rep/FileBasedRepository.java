/*
 * @(#) $Id: FileBasedRepository.java,v 1.1.1.1 2003/10/05 18:39:15 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.rep;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class FileBasedRepository {
    private static class FBRInput {
        private InputStream is;

        private int off;

        public FBRInput(InputStream is) {
            this.is = is;
            off = 0;
        }

        public int getOffset() {
            return off;
        }

        public byte readByte() throws IOException {
            int b = is.read();
            if (b == -1)
                throw new EOFException();
            off += 1;
            return (byte) b;
        }

        public int readInt() throws IOException {
            int b1 = is.read();
            int b2 = is.read();
            int b3 = is.read();
            int b4 = is.read();
            if ((b1 | b2 | b3 | b4) < 0) // Encountered EOF
                throw new EOFException();
            off += 4;
            return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
        }

        public byte[] readBytes(int len) throws IOException {
            if (len < 0)
                throw new IOException("Negative no. of bytes to read");
            byte[] data = new byte[len];
            int nRead = 0;
            while (nRead < len) {
                int n = is.read(data, nRead, len - nRead);
                if (n == -1)
                    throw new EOFException();
                nRead += n;
                off += n;
            }
            return data;
        }

        public X509Certificate readX509Certificate() throws Exception {
            int len = readInt();
            byte[] data = readBytes(len);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            X509Certificate cert = (X509Certificate) cf.generateCertificate(bais);
            return cert;
        }

        public X509CRL readX509CRL() throws Exception {
            int len = readInt();
            byte[] data = readBytes(len);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            X509CRL crl = (X509CRL) cf.generateCRL(bais);
            return crl;
        }
    }

    private static class FBROutput {
        private OutputStream os;

        private int off;

        public FBROutput(OutputStream os) {
            this.os = os;
            off = 0;
        }

        public int getOffset() {
            return off;
        }

        public void writeByte(byte b) throws IOException {
            os.write(b);
            this.off += 1;
        }

        public void writeInt(int v) throws IOException {
            os.write((v >>> 24) & 0xFF);
            os.write((v >>> 16) & 0xFF);
            os.write((v >>> 8) & 0xFF);
            os.write((v >>> 0) & 0xFF);
            off += 4;
        }

        public void writeBytes(byte[] data, int off, int len) throws IOException {
            os.write(data, off, len);
            this.off += len;
        }

        public void writeX509Certificate(X509Certificate cert) throws Exception {
            byte[] data = cert.getEncoded();
            writeInt(data.length);
            writeBytes(data, 0, data.length);
        }

        public void writeX509CRL(X509CRL crl) throws Exception {
            byte[] data = crl.getEncoded();
            writeInt(data.length);
            writeBytes(data, 0, data.length);
        }
    }

    public static final int FBREP_MAGIC = 15071968;

    public static final byte X509CERT = 0x01;

    public static final byte X509CRL = 0x02;

    public static final byte FBREP_END = 0x00;

    private String filename = null;

    private List<Object> list = new ArrayList<Object>();

    public FileBasedRepository(String filename) throws Exception {
        this.filename = filename;
        File file = new File(filename);
        if (!file.exists()) { // Create an empty repository;
            save();
        }
        load();
    }

    public Collection<Object> getRepository() {
        return list;
    }

    public synchronized void load() throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        FBRInput fbrInput = new FBRInput(fis);
        int magicNo = fbrInput.readInt();
        if (magicNo != FBREP_MAGIC)
            throw new IOException("Not a File Based Repository");

        list = new ArrayList<Object>();
        boolean scanComplete = false;
        while (!scanComplete) {
            byte entryId = fbrInput.readByte();
            switch (entryId) {
            case X509CERT:
                X509Certificate cert = fbrInput.readX509Certificate();
                list.add(cert);
                break;
            case X509CRL:
                X509CRL crl = fbrInput.readX509CRL();
                list.add(crl);
                break;
            case FBREP_END:
                scanComplete = true;
                break;
            default:
                throw new IOException("Unexpected Data at offset = " + fbrInput.getOffset());
            }
        }

    }

    public synchronized void save() throws Exception {
        FileOutputStream fos = new FileOutputStream(filename);
        FBROutput fbrOutput = new FBROutput(fos);
        fbrOutput.writeInt(FBREP_MAGIC);
        Iterator<?> itr = list.iterator();
        while (itr.hasNext()) {
            Object entry = itr.next();
            if (entry instanceof X509Certificate) {
                fbrOutput.writeByte(X509CERT);
                fbrOutput.writeX509Certificate((X509Certificate) entry);
            } else if (entry instanceof X509CRL) {
                fbrOutput.writeByte(X509CRL);
                fbrOutput.writeX509CRL((X509CRL) entry);
            }
        }
        fbrOutput.writeByte(FBREP_END);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage:: java FileBasedRepository <filename>");
            return;
        }
        /*FileBasedRepository fbr =*/ new FileBasedRepository(args[0]);
    }
}
