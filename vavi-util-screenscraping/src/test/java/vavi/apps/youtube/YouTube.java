/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.youtube;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import vavi.util.Debug;
import vavix.util.screenscrape.Scraper;
import vavix.util.screenscrape.StringSimpleXPathScraper;


/**
 * YouTube Downloader.
 *
 * @author Takashi Ohida (pichon@gmail.com)
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060705 nsano initial version <br>
 */
class YouTube {

    static String videoXpath;
    static String videoTagRregex;
    static String urlVideoIdRegex;
    static String watchUrlFormat;
    static String getUrlFormat;

    /** */
    static {
        try {
            Properties props = new Properties();
            props.load(YouTube.class.getResourceAsStream("/vavi/apps/youtube/YouTube.properties"));

            videoXpath = props.getProperty("video.xpath");
            videoTagRregex = props.getProperty("video.tag.regex");
            urlVideoIdRegex = props.getProperty("url.videoId.regex");
            watchUrlFormat = props.getProperty("watch.url.format");
            getUrlFormat = props.getProperty("get.url.format");
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.exit(1);
        }
    }

    /** TODO  */
    static class YouTubeURLScraper implements Scraper<URL, File> {
        /** */
        private HttpClient httpClient = new HttpClient();

        /**
         * xpath <code>"//DIV[@ID='interactDiv']/SCRIPT"</code> 
         * tag 
         */
        StringSimpleXPathScraper myStreamXPathScraper = new StringSimpleXPathScraper(videoXpath) {
            Pattern pattern = Pattern.compile(videoTagRregex);
            public String scrape(InputStream source) {
                String tag;
                String script = super.scrape(source);
                Matcher matcher = pattern.matcher(script);
                if (matcher.find()) {
                    tag = matcher.group(1);
                } else {
                    throw new IllegalArgumentException("no tag found");
                }
                return tag;
            }
        };

        /**
         * @param url YouTube  URL 
         */
        public File scrape(URL url) {
            try {
                return pattern2(url);
            } catch (IOException e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }

        /** */
        File pattern1(URL url) throws IOException {
            String videoId;

            // videoId 
            Pattern pattern = Pattern.compile(urlVideoIdRegex);
            Matcher matcher = pattern.matcher(url.toString());
            if (matcher.find()) {
                videoId = matcher.group(1);
            } else {
                throw new IllegalArgumentException("no video id found");
            }

            // tag 
            String watchUrl = String.format(watchUrlFormat, videoId);
System.err.println("watch: " + watchUrl);
            GetMethod get = new GetMethod(watchUrl);
            httpClient.getState().addCookie(new Cookie("youtube.com/", "VISITOR_INFO1_LIVE", "H5LU-y_SA9w"));
            httpClient.getState().addCookie(new Cookie("youtube.com/", "LOGIN_INFO", "9bb3bfa8da28d9518e35b22193026217e3QgAAAAbV91c2VyX2lkX0FORF9zZXNzaW9uX251bWJlcl9tZDVzIAAAADRmNDJmZmU3MTYyNzg0N2UzZTRkZjQxNzcyOWQ4Yjc1dAkAAABtX3VzZXJfaWRsAgAAAKEK9QIw"));
            httpClient.getState().addCookie(new Cookie("youtube.com/", "is_adult", "8d3a778dcb047f7c9a6ab4917e55b74adAEAAAAx"));
            httpClient.getState().addCookie(new Cookie("youtube.com/", "watched_video_id_list_vavivavi", "17bc3ed9329253d96fa33e53eea4c750WwEAAABzCwAAAGxHenVtMU5zdDJj"));
            int status = httpClient.executeMethod(get);
            if (status != 200) {
                throw new IllegalStateException("unexpected result when 'watch': " + status);
            }

            String tag = myStreamXPathScraper.scrape(get.getResponseBodyAsStream());

            get.releaseConnection();

            // flv 
            String getUrl = String.format(getUrlFormat, videoId, tag);
System.err.println("getUrl: " + getUrl);
            get = new GetMethod(getUrl);
            status = httpClient.executeMethod(get);
            if (status == 303) {
                // 
                getUrl = get.getResponseHeader("Location").getValue();
System.err.println("redirectUrl: " + getUrl);
                get = new GetMethod(getUrl);
                status = httpClient.executeMethod(get);
            }
            if (status != 200) {
                throw new IllegalStateException("unexpected result when 'get': " + status);
            } else {
                for (Header header : get.getResponseHeaders()) {
System.err.println(header.getName() + "=" + header.getValue());
                }
            }

            // flv 
            InputStream is = get.getResponseBodyAsStream();
            int length = Integer.parseInt(get.getResponseHeader("Content-Length").getValue());
            ReadableByteChannel inputChannel = Channels.newChannel(is); 

            File file = File.createTempFile("youtube", ".flv");
            FileOutputStream out = new FileOutputStream(file);
            FileChannel outputChannel = out.getChannel();
System.err.println("downloading... size: " + length);
            outputChannel.transferFrom(inputChannel, 0, length);

            get.releaseConnection();

            return file;
        }

        /** 2007/8/17 ~ */
        File pattern2(URL url) throws IOException {
            String videoId;

            // videoId 
            Pattern pattern = Pattern.compile(urlVideoIdRegex);
            Matcher matcher = pattern.matcher(url.toString());
            if (matcher.find()) {
                videoId = matcher.group(1);
            } else {
                throw new IllegalArgumentException("no video id found");
            }

            // flv 
            String getUrl = String.format(getUrlFormat, videoId);
System.err.println("getUrl: " + getUrl);
            GetMethod get = new GetMethod(getUrl);
            int status = httpClient.executeMethod(get);
            while (status == 302) {
System.err.println("status2: " + status);
                // 
                getUrl = get.getResponseHeader("Location").getValue();
System.err.println("redirectUrl: " + getUrl);
                get = new GetMethod(getUrl);
                status = httpClient.executeMethod(get);
            }
            if (status != 200) {
                throw new IllegalStateException("unexpected result when 'get': " + status);
            } else {
                for (Header header : get.getResponseHeaders()) {
System.err.println(header.getName() + "=" + header.getValue());
                }
            }

            // flv
            InputStream is = get.getResponseBodyAsStream();
            int length = Integer.parseInt(get.getResponseHeader("Content-Length").getValue());
            ReadableByteChannel inputChannel = Channels.newChannel(is); 

            File file = File.createTempFile("youtube", ".flv");
            FileOutputStream out = new FileOutputStream(file);
            FileChannel outputChannel = out.getChannel();
System.err.println("downloading... size: " + length);
            outputChannel.transferFrom(inputChannel, 0, length);

            get.releaseConnection();

            return file;
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        String url = args[0];

        Scraper<URL, File> scraper = new YouTubeURLScraper();
        File file = scraper.scrape(new URL(url));
System.err.println("file:\n" + file);
    }
}

/* */
