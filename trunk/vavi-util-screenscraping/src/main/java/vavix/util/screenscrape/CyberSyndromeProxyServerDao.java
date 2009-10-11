/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

import vavi.util.Debug;
import vavix.util.screenscrape.SimpleURLScraper;
import vavix.util.screenscrape.XPathScraper;
import vavix.util.screenscrape.ProxyChanger.InternetAddress;


/**
 * CyberSyndromeProxyServerDao. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071004 nsano initial version <br>
 */
public class CyberSyndromeProxyServerDao implements ProxyServerDao {

    /** */
    public CyberSyndromeProxyServerDao() {
        try {
            updateProxyAddresses();
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    private static List<InternetAddress> proxyAddresses;

    /* */
    public List<InternetAddress> getProxyInetSocketAddresses() {
        return proxyAddresses;
    }

    /** */
    private static String xpath;

    /** */
    private static String url;

    /** TODO use timer */
    private void updateProxyAddresses() throws IOException {
        SimpleURLScraper<List<InternetAddress>> scraper = new SimpleURLScraper<List<InternetAddress>>(new MyScraper(xpath));
        proxyAddresses = scraper.scrape(new URL(url));
    }

    /** */
    private static class MyScraper extends XPathScraper<InputStream, List<InternetAddress>> {
        /** */
        private XPath xPath = XPathFactory.newInstance().newXPath();
        /** */
        protected MyScraper(String xpath) {
            super(xpath);
        }
        /** */
        public List<InternetAddress> scrape(InputStream source) {
            try {
                InputSource in = new InputSource(source);
                NodeList nodeList = (NodeList) xPath.evaluate(xpath, in, XPathConstants.NODESET);
                final List<InternetAddress> proxyAddress = new ArrayList<InternetAddress>();
                class ProxyChecker implements Runnable {
                    String host;
                    int port;
                    ProxyChecker(String host, int port) {
                        this.host = host;
                        this.port = port;
                    }
                    /** */
                    public void run() {
                        if (isOk(host, port)) {
                            InternetAddress inetSocketAddress = new InternetAddress(host, port);
                            proxyAddress.add(inetSocketAddress);
System.err.println("OK: " + host + ":" + port);
                        } else {
System.err.println("NG: " + host + ":" + port);
                        }
                    }
                    boolean isOk(String host, int port) {
                        try {
                            HttpClient client = new HttpClient();
    
                            HostConfiguration hc = new HostConfiguration();
                            hc.setHost("yahoo.co.jp");
                            hc.setProxy(host, port);
                            HeadMethod head = new HeadMethod();
                            head.setHostConfiguration(hc);
                            int status = client.executeMethod(head);
                            return status == 200;
                        } catch (IOException e) {
//Debug.println(e);
                            return false;
                        }
                    }
                }
                ExecutorService executorService = Executors.newCachedThreadPool();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    try {
                        String[] data = nodeList.item(i).getTextContent().split(":");
                        String host = data[0];
                        int port = Integer.parseInt(data[1]);

                        executorService.execute(new ProxyChecker(host, port));
                    } catch (Exception e) {
                        Debug.println(e);
                    }
                }
                return proxyAddress;
            } catch (XPathExpressionException e) {
                throw (RuntimeException) new IllegalArgumentException("wrong input").initCause(e);
            }
        }
    }

    /* */
    static {
        try {
            Properties props = new Properties();
            props.load(PropertiesUserAgentDao.class.getResourceAsStream("CyberSyndromeProxyServerDao.properties"));

            url = props.getProperty("url");
            xpath = props.getProperty("xpath");
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        for (InternetAddress proxyAddress : proxyAddresses) {
            System.err.println("proxy: " + proxyAddress);
        }
    }
}

/* */
