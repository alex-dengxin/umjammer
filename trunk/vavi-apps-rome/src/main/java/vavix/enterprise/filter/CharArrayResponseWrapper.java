/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.enterprise.filter;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * テキスト形式の出力をバッファするレスポンスラッパーです。
 * 
 * @see http://jodd.sourceforge.net/
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060222 nsano initial version <br>
 */
public class CharArrayResponseWrapper extends HttpServletResponseWrapper {

    /** */
    private CharArrayWriter writer;

    /** */
    private String encoding = "ISO-8859-1";

    /** */
    public CharArrayResponseWrapper(HttpServletResponse response, String encoding) {
        super(response);
        writer = new CharArrayWriter();
        this.encoding = encoding;
    }

    /** */
    public PrintWriter getWriter() {
        return new PrintWriter(writer);
    }

    /**
     * Get a String representation of the entire buffer.
     * 
     * Be sure <B>not</B> to call this method multiple times on the same
     * wrapper. The API for CharArrayWriter does not guarantee that it
     * "remembers" the previous value, so the call is likely to make a new
     * String every time.
     */
    public String toString() {
        return writer.toString();
    }

    /**
     * Get the underlying character array.
     * 
     * @return content as char array
     */
    public char[] toCharArray() {
        return writer.toCharArray();
    }

    /**
     * Get the underlying as byte array.
     * 
     * @return content as byte array
     */
    public byte[] toByteArray() {
        try {
            return writer.toString().getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            return writer.toString().getBytes();
        }
    }

    /**
     * This empty method <b>must</b> exist.
     * 
     * @param len
     */
    public void setContentLength(int len) {
    }

    /**
     * Returns the size (number of characters) of written data.
     * 
     * @return size of written data
     */
    public int getSize() {
        return toByteArray().length;
    }

    /** */
    private String contentType = "";

    /**
     * Sets the content type.
     * 
     * @param type
     */
    public void setContentType(String type) {
        super.setContentType(type);
        contentType = type;
    }

    /**
     * Returns content type.
     * 
     * @return content type
     */
    public String getContentType() {
        return contentType;
    }

    /** */
    public void close() {
        writer.close();
    }

    /**
     * Returns output stream.
     * 
     * @return output stream
     * @exception java.io.IOException
     */
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            /** */
            public void write(int b) throws IOException {
                writer.write(b);
            }

            /** */
            public void write(byte b[]) throws IOException {
                char[] chars = new String(b, encoding).toCharArray();
                writer.write(chars, 0, chars.length);
            }

            /** */
            public void write(byte b[], int off, int len) throws IOException {
                char[] chars = new String(b, off, len, encoding).toCharArray();
                writer.write(chars, 0, chars.length);
            }
        };
    }
}

/* */
