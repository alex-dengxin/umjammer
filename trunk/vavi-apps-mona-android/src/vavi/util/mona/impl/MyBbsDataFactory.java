/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import vavi.util.mona.BbsData;
import vavi.util.mona.BbsDataFactory;
import vavi.util.mona.BbsThread;


/**
 * MyBbsDatumFactory. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080901 nsano initial version <br>
 */
public class MyBbsDataFactory implements BbsDataFactory {

    /** 
     * @param thread lastModified, size
     */
    public List<BbsData> readFrom(BbsThread thread) throws IOException {
        HttpURLConnection uc = null;
        try {
            List<BbsData> datum = new ArrayList<BbsData>();
//System.err.println("threadUrl: " + thread.getThreadUrl());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            uc = (HttpURLConnection) new URL(thread.getThreadUrl()).openConnection();
            uc.setRequestProperty("User-Agent", "Monazilla/0.00");
            uc.setRequestProperty("Connection", "close");
            if (thread.getIndex() > 0) {
//final String rfc1123 = "EEE, d MMM yyyy HH:mm:ss Z";
//System.err.println("DIFF: " + thread.size + "-, " + new SimpleDateFormat(rfc1123, Locale.ENGLISH).format(new Date(thread.lastModified)));
                uc.setIfModifiedSince(thread.getLastModified());
                uc.setRequestProperty("Range", "bytes=" + thread.getSize() + "-");
            } else {
//System.err.println("NORMAL");
                uc.setRequestProperty("Accept-Encoding", "gzip");
            }
            uc.connect();
            int result = uc.getResponseCode();
//System.err.println("RESPONSE CODE: " + result);
//System.err.println("LastModified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(uc.getLastModified())));
//System.err.println("Content-Length: " + uc.getContentLength());
//System.err.println("Content-Type: " + uc.getContentType());
//System.err.println("Content-Encoding: " + uc.getContentEncoding());
            if (result == HttpURLConnection.HTTP_OK) { // 200
//System.err.println("SUCCESS");
                thread.setIndex(1);
                thread.setLastModified(uc.getLastModified());
            } else if (result == HttpURLConnection.HTTP_PARTIAL) { // 206
//System.err.println("DIFF");
                thread.setLastModified(uc.getLastModified());
            } else if (result == HttpURLConnection.HTTP_NOT_MODIFIED) { // 304
System.err.println("NO DIFF");
                return datum;
            } else if (result == 416) { // HTTP_RANGE_NOT_SATISFIABLE
System.err.println("ABON");
                return datum;
            } else {
                throw new IllegalStateException(String.valueOf(result));
            }

            InputStream is = null;
            if (uc.getContentEncoding() == null || uc.getContentEncoding().indexOf("gzip") < 0) {
//System.err.println("NORMAL");
                is = uc.getInputStream();
            } else {
//System.err.println("GZIP");
                is = new GZIPInputStream(new BufferedInputStream(uc.getInputStream()));
            }
            byte[] buffer = new byte[2048];
            while (true) {
                int r = is.read(buffer);
                if (r < 0) {
                    break;
                }
                baos.write(buffer, 0, r);
            }
            thread.setSize(thread.getSize() + baos.size());
//System.err.println("baos: " + baos.size() + ", " + uc.getContentLength());
            String threadListText = new String(baos.toByteArray(), "MS932");
//System.err.println(threadListText);

            StringTokenizer rows = new StringTokenizer(threadListText, "\n");
            while (rows.hasMoreTokens()) {
                String row = rows.nextToken();
//System.err.println("row: " + row);
                String name = null;
                String email = null;
                String id = null;
                String text = null;
                String title = null;
                int p = 0;
                for (int i = 0; i < 5; i++) {
                    int q = row.indexOf("<>", p);
//System.err.println("q: " + q);
                    switch (i) {
                    case 0:
                        name = row.substring(p, q);
//System.err.println("name: " + name);
                        break;
                    case 1:
                        email = row.substring(p, q);
//System.err.println("email: " + email);
                        break;
                    case 2:
                        id = row.substring(p, q);
//System.err.println("id: " + id);
                        break;
                    case 3:
                        text = row.substring(p, q);
//System.err.println("text: " + text);
                        break;
                    case 4:
                        title = row.substring(p);
//System.err.println("title: " + title);
                        break;
                    }
                    p = q + 2;
                }
                datum.add(new BbsData(thread.getIndex(), name, email, id, text, title));
                datum.get(datum.size() - 1).setRaw(row); 
                thread.setIndex(thread.getIndex() + 1);
            }   
            return datum;
        } finally {
            uc.disconnect();
        }
    }
}

/* */
