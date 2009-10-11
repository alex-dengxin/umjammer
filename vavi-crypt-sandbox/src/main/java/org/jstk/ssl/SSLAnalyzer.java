/*
 * @(#) $Id: SSLAnalyzer.java,v 1.1.1.1 2003/10/05 18:39:26 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.ssl;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.jstk.JSTKUtil;


public class SSLAnalyzer implements ProtocolAnalyzer {
    public static abstract class SSLProtocolMessage implements Cloneable {
        protected static String[] isa = new String[] { // Indent String Array
            "", " ", "  ", "   ", "    ", "     ", "      "
        };

        protected int consumed;

        public abstract boolean parse(byte[] buf, int offset, int n);

        public abstract void print(int indent);

        public int bytesConsumed() {
            return consumed;
        }

        public int int24(byte[] buf, int off) {
            return ((buf[off] & 0x000000ff) << 16) | ((buf[off + 1] & 0x000000ff) << 8) | (buf[off + 2] & 0x000000ff);
        }

        public int int16(byte[] buf, int off) {
            return ((buf[off] & 0x000000ff) << 8) | (buf[off + 1] & 0x000000ff);
        }

        public void printHexData(int indent, String label, byte[] buf) {
            System.out.println(isa[indent] + label + "[" + buf.length + "]:");
            String[] sa = JSTKUtil.hexStringArrayFromBytes(buf, 16);
            for (int i = 0; i < sa.length; i++)
                System.out.println(isa[indent] + "  " + sa[i]);
        }

        public boolean matchHeader(byte[] buf, int off, int n, byte type) {
            if (buf[off] != type)
                return false;
            int length = int24(buf, off + 1);
            if (off + 4 + length > n)
                return false;
            return true;
        }
    }

    public static class SSLv2ClientHelloMessage extends SSLProtocolMessage {
        private static String[] SSLv2CipherSuites = new String[] {
            "Unknown Cipher Suite", "SSL_RC4_128_WITH_MD5", "SSL_RC4_128_EXPORT40_WITH_MD5", "SSL_RC2_CBC_128_CBC_WITH_MD5", "SSL_RC2_CBC_128_CBC_EXPORT40_WITH_MD5", "SSL_IDEA_128_CBC_WITH_MD5", "SSL_DES_64_CBC_WITH_MD5", "SSL_DES_192_EDE3_CBC_WITH_MD5"
        };

        private static String[] TLSv1CipherSuites = new String[] {
            "Unknown Cipher Suite", "TLS_RSA_WITH_NULL_MD5", "TLS_RSA_WITH_NULL_SHA", "TLS_RSA_EXPORT_WITH_RC4_40_MD5", "TLS_RSA_WITH_RC4_128_MD5", "TLS_RSA_WITH_RC4_128_SHA", "TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5", "TLS_RSA_WITH_IDEA_CBC_SHA", "TLS_RSA_EXPORT_WITH_DES40_CBC_SHA", "TLS_RSA_WITH_DES_CBC_SHA", "TLS_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_DH_DSS_WITH_DES_CBC_SHA", "TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA", "TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA",
            "TLS_DH_RSA_WITH_DES_CBC_SHA", "TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", "TLS_DHE_DSS_WITH_DES_CBC_SHA", "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA", "TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "TLS_DHE_RSA_WITH_DES_CBC_SHA", "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA", "TLS_DH_anon_EXPORT_WITH_RC4_40_MD5", "TLS_DH_anon_WITH_RC4_128_MD5", "TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA", "TLS_DH_anon_WITH_DES_CBC_SHA", "TLS_DH_anon_WITH_3DES_EDE_CBC_SHA"
        };

        int majorVersion;

        int minorVersion;

        int cipherSpecsLen;

        int sessionIdLen;

        int challengeLen;

        byte[] cipherSpecsData;

        byte[] sessionIdData;

        byte[] challengeData;

        public boolean parse(byte[] buf, int offset, int n) {
            int recordLength;
            consumed = 0;
            if ((buf[offset] & (byte) 0x80) != 0x00) { // 2 byte record header. No padding.
                recordLength = (buf[offset] & 0x7f) << 8 | buf[offset + 1];
                if (recordLength > n - 2)
                    return false;
                consumed += 2;
                if (buf[offset + consumed] != 0x01)
                    return false;
                consumed += 1;
                majorVersion = buf[offset + consumed];
                minorVersion = buf[offset + consumed + 1];
                consumed += 2;
                cipherSpecsLen = (buf[offset + consumed]) << 8 | buf[offset + consumed + 1];
                consumed += 2;
                sessionIdLen = (buf[offset + consumed]) << 8 | buf[offset + consumed + 1];
                consumed += 2;
                challengeLen = (buf[offset + consumed]) << 8 | buf[offset + consumed + 1];
                consumed += 2;
                if (offset + consumed + cipherSpecsLen + sessionIdLen + challengeLen > n)
                    return false;

                cipherSpecsData = new byte[cipherSpecsLen];
                System.arraycopy(buf, offset + consumed, cipherSpecsData, 0, cipherSpecsLen);
                consumed += cipherSpecsLen;

                sessionIdData = new byte[sessionIdLen];
                System.arraycopy(buf, offset + consumed, sessionIdData, 0, sessionIdLen);
                consumed += sessionIdLen;

                challengeData = new byte[challengeLen];
                System.arraycopy(buf, offset + consumed, challengeData, 0, challengeLen);
                consumed += challengeLen;
                return true;
            }
            return false;
        }

        private String cipherSuite(byte[] ba) {
            if (ba[0] == 0x00 && ba[1] == 0x00) { // TLSv1 cipher suite
                int cipherSuiteIndex = ba[2];
                if (cipherSuiteIndex < TLSv1CipherSuites.length)
                    return TLSv1CipherSuites[cipherSuiteIndex];
            } else if (ba[1] == 0x00) { // Could be SSLv2 cipher suite
                int cipherSuiteIndex = ba[0];
                if (cipherSuiteIndex < SSLv2CipherSuites.length)
                    return SSLv2CipherSuites[cipherSuiteIndex];
            }
            return "Unknown Cipher Suite";
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "ClientHello (SSLv2 format mapped to TLSv1 fields)");
            System.out.println(isa[indent] + "  Version " + majorVersion + "." + minorVersion);
            System.out.println(isa[indent] + "  Random[" + challengeLen + "]:");
            int noCipherSuites = cipherSpecsLen / 3;
            System.out.println(isa[indent] + "  Cipher Suites[" + noCipherSuites + "]:");
            for (int i = 0; i < noCipherSuites; i++) {
                byte[] ba = new byte[3];
                System.arraycopy(cipherSpecsData, i * 3, ba, 0, 3);
                System.out.println(isa[indent] + "      " + cipherSuite(ba) + " (0x" + JSTKUtil.hexStringFromBytes(ba) + ")");
            }
        }
    }

    public static class ProtocolVersion {
        private int majorVersion;

        private int minorVersion;

        public ProtocolVersion(byte[] buf, int offset) {
            majorVersion = buf[offset];
            minorVersion = buf[offset + 1];
        }

        public String toString() {
            return "" + majorVersion + "." + minorVersion;
        }
    }

    public static class SSLRecordHeader extends SSLProtocolMessage {
        public static final byte CHANGE_CIPHER_SPEC = 0x14;

        public static final byte ALERT = 0x15;

        public static final byte HAND_SHAKE = 0x16;

        public static final byte APPLICATION_DATA = 0x17;

        byte contentType;

        ProtocolVersion version;

        int length;

        public boolean parse(byte[] buf, int offset, int n) {
            consumed = 0;
            contentType = buf[offset + consumed];

            if (contentType < 0x14 || contentType > 0x17)
                return false;
            ++consumed;
            version = new ProtocolVersion(buf, offset + consumed);
            consumed += 2;
            length = int16(buf, offset + consumed);
            consumed += 2;
            if (offset + consumed + length > n)
                return false;
            return true;
        }

        public byte getContentType() {
            return contentType;
        }

        public String getContentTypeAsString() {
            switch (contentType) {
            case CHANGE_CIPHER_SPEC:
                return "ChangeCipherSpec";
            case ALERT:
                return "Alert";
            case HAND_SHAKE:
                return "HandShake";
            case APPLICATION_DATA:
                return "ApplicationData";
            }
            return "UnRecognized";
        }

        public int getContentLength() {
            return length;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "Record Header. " + "(ContentType: " + getContentTypeAsString() + ", Version: " + version + ", Len: " + length + ")");
        }
    }

    public static class ServerHello extends SSLProtocolMessage {
        public static byte SERVER_HELLO = 2;

        int length;

        ProtocolVersion version;

        byte[] random = new byte[32];

        byte[] sessionId = null;

        int cipherSuite;

        byte compMethod;

        public boolean parse(byte[] buf, int offset, int n) {
            if (!matchHeader(buf, offset, n, SERVER_HELLO))
                return false;
            consumed = 4;
            version = new ProtocolVersion(buf, offset + consumed);

            consumed += 2;
            System.arraycopy(buf, offset + consumed, random, 0, 32);
            consumed += 32;

            int sessionIdLen = buf[offset + consumed] & 0x000000ff;
            if (sessionIdLen > 32)
                return false;
            ++consumed;
            sessionId = new byte[sessionIdLen];
            System.arraycopy(buf, offset + consumed, sessionId, 0, sessionIdLen);
            consumed += sessionIdLen;

            cipherSuite = int16(buf, offset + consumed);
            consumed += 2;

            compMethod = buf[offset + consumed];
            ++consumed;
            return true;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "ServerHello. (Version: " + version + ", Len: " + length + ")");

            printHexData(indent + 2, "Random", random);
            printHexData(indent + 2, "Session Id", sessionId);

            System.out.println(isa[indent] + "  Cipher Suite: " + cipherSuite);
            System.out.println(isa[indent] + "  Compression Method: " + (int) compMethod);
        }
    }

    public static class SSLCertificate extends SSLProtocolMessage {
        public static byte CERTIFICATE = 11;

        byte[] cert = null;

        public boolean parse(byte[] buf, int offset, int n) {
            if (!matchHeader(buf, offset, n, CERTIFICATE))
                return false;
            consumed = 4;

            int certLen = int24(buf, offset + consumed);
            consumed += 3;

            cert = new byte[certLen];
            System.arraycopy(buf, offset + consumed, cert, 0, certLen);
            consumed += certLen;
            return true;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "Certificate. (Bytes: " + cert.length + ")");
            // printHexData(indent + 2, "Certificate", cert);
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream bais = new ByteArrayInputStream(cert);

                int index = 0;
                while (bais.available() > 0) {
                    byte[] ba = new byte[3];
                    bais.read(ba, 0, 3); // Need to eat up these 3 bytes.
                    /*int len =*/ int24(ba, 0);
                    X509Certificate c = (X509Certificate) cf.generateCertificate(bais);
                    System.out.println(isa[indent] + "Certificate[" + index + "]:");
                    System.out.println(isa[indent] + "  Data:");
                    System.out.println(isa[indent] + "    Version: " + c.getVersion());
                    System.out.println(isa[indent] + "    Serial Number: " + c.getSerialNumber());
                    System.out.println(isa[indent] + "    Signature Algorithm: " + c.getSigAlgName());
                    System.out.println(isa[indent] + "    Issuer: " + c.getIssuerX500Principal());
                    System.out.println(isa[indent] + "    Validity:");
                    System.out.println(isa[indent] + "      Not Before: " + c.getNotBefore());
                    System.out.println(isa[indent] + "      Not After: " + c.getNotAfter());
                    System.out.println(isa[indent] + "    Subject: " + c.getSubjectX500Principal());
                    System.out.println(isa[indent] + "    Extensions:");
                    System.out.println(isa[indent] + "      Basic Constraints: " + c.getBasicConstraints());
                    ++index;
                }
            } catch (CertificateException ce) {
                System.out.println("Cannot parse input as Certificate");
            }
        }
    }

    public static class ServerHelloDone extends SSLProtocolMessage {
        public static byte SERVER_HELLO_DONE = 14;

        public boolean parse(byte[] buf, int offset, int n) {
            if (!matchHeader(buf, offset, n, SERVER_HELLO_DONE))
                return false;
            consumed = 4;
            return true;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "ServerHelloDone.");
        }
    }

    public static class ClientKeyExchange extends SSLProtocolMessage {
        public static byte CLIENT_KEY_EXCHANGE = 16;

        byte[] data = null;

        public boolean parse(byte[] buf, int offset, int n) {
            if (!matchHeader(buf, offset, n, CLIENT_KEY_EXCHANGE))
                return false;
            consumed = 4;
            int length = int24(buf, offset + 1);
            data = new byte[length];
            System.arraycopy(buf, offset + consumed, data, 0, length);
            consumed += length;
            return true;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "ClientKeyExchange.");
            printHexData(indent + 2, "Client Key Exchange Data", data);
        }
    }

    public static class ServerKeyExchange extends SSLProtocolMessage {
        public static byte SERVER_KEY_EXCHANGE = 12;

        byte[] data = null;

        public boolean parse(byte[] buf, int offset, int n) {
            if (!matchHeader(buf, offset, n, SERVER_KEY_EXCHANGE))
                return false;
            consumed = 4;
            int length = int24(buf, offset + 1);
            data = new byte[length];
            System.arraycopy(buf, offset + consumed, data, 0, length);
            consumed += length;
            return true;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "ServerKeyExchange.");
            printHexData(indent + 2, "Server Key Excahnge Data", data);
        }
    }

    public static class Finished extends SSLProtocolMessage {
        public static byte FINISHED = 20;

        byte[] verifyData = null;

        byte[] md5Hash = null;

        byte[] shaHash = null;

        public boolean parse(byte[] buf, int offset, int n) {
            if (!matchHeader(buf, offset, n, FINISHED))
                return false;
            consumed = 4;
            int length = int24(buf, offset + 1);

            verifyData = new byte[length];
            System.arraycopy(buf, offset + consumed, verifyData, 0, length);
            consumed += length;
            return true;
        }

        public void print(int indent) {
            System.out.println(isa[indent] + "Finished.");
            printHexData(indent + 2, "Verify Data", verifyData);
        }
    }

    public static class SSLProtocolMessageFactory {
        private static SSLProtocolMessage[] msgs = new SSLProtocolMessage[] {
            new ServerHello(), new SSLCertificate(), new ServerHelloDone(), new ServerKeyExchange(), new ClientKeyExchange(), new Finished(), new SSLRecordHeader()
        };

        public static SSLProtocolMessage createProtocolMessage(byte[] buf, int offset, int n) {
            for (int i = 0; i < msgs.length; i++) {
                if (msgs[i].parse(buf, offset, n))
                    return msgs[i];
            }
            return null;
        }
    }

    private String label = null;

    public SSLAnalyzer(String label) {
        this.label = label;
    }

    public void analyze(JSTKBuffer buf) {
        int n = buf.getNBytes();
        byte[] tbuf = buf.getByteArray();
        System.out.println("[SSL] C " + label + " S (" + buf.getNBytes() + " bytes)");
        SSLv2ClientHelloMessage clientHello = new SSLv2ClientHelloMessage();
        SSLRecordHeader recordHeader = new SSLRecordHeader();

        if (clientHello.parse(tbuf, 0, n)) {
            clientHello.print(2);
        } else {
            int offset = 0;
            while (offset < n) {
                if (recordHeader.parse(tbuf, offset, n)) {
                    recordHeader.print(2);
                    offset += recordHeader.bytesConsumed();
                    int recordLimit = offset + recordHeader.getContentLength();
                    while (offset < recordLimit) {
                        if (recordHeader.getContentType() == SSLRecordHeader.HAND_SHAKE) {
                            SSLProtocolMessage sslPM = SSLProtocolMessageFactory.createProtocolMessage(tbuf, offset, n);
                            if (sslPM != null) {
                                sslPM.print(4);
                                offset += sslPM.bytesConsumed();
                            } else {
                                System.out.println("Encrypted Handshake Message");
                                offset += recordHeader.getContentLength();
                            }
                        } else {
                            System.out.println("Encrypted " + recordHeader.getContentTypeAsString() + " Message");
                            offset += recordHeader.getContentLength();
                        }
                    }
                } else {
                    System.out.println("Unknown format.");
                    offset = n;
                } // if(recordHeader.parse(...
            } // while
        } // if (clientHello.parse( ...
    }
}
