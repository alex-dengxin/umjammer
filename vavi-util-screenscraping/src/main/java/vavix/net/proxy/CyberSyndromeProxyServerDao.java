/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.net.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.HeadMethod;

import vavi.util.Debug;
import vavix.net.proxy.ProxyChanger.InternetAddress;
import vavix.util.screenscrape.annotation.Target;
import vavix.util.screenscrape.annotation.WebScraper;


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
    private List<InternetAddress> proxyAddresses = new ArrayList<InternetAddress>();

    /* */
    public List<InternetAddress> getProxyInetSocketAddresses() {
        return proxyAddresses;
    }

    @WebScraper(url = "http://www.cybersyndrome.net/pla5.html",
                encoding = "ISO_8859-1")
    public static class ProxyInternetAddress extends InternetAddress {
        /** */
        @Target("//A[@class='B']/text()")
        private String address;
        public String getHostName() {
            if (hostName == null) {
                String[] data = address.split(":");
                hostName = data[0];
            }
            return hostName; 
        }
        public int getPort() {
            if (port == 0) {
                String[] data = address.split(":");
                port = Integer.parseInt(data[1]);
            }
            return port;
        }
        /** */
        public String toString() {
            return getHostName() + ":" + getPort() + " " + (alive ? "OK" : "NG");
        }
        private boolean alive;
        public void setAlive(boolean alive) {
            this.alive = alive;
        }
    }

    /** TODO use timer */
    private void updateProxyAddresses() throws IOException {

        ExecutorService executorService = Executors.newCachedThreadPool();

        List<ProxyInternetAddress> addresses = WebScraper.Util.scrape(ProxyInternetAddress.class);
        for (ProxyInternetAddress address : addresses) {
            try {
                executorService.submit(new ProxyChecker(address));
                Thread.sleep(300);
            } catch (Exception e) {
                Debug.println(e);
            }
        }
    }

    class ProxyChecker implements Runnable {
        ProxyInternetAddress address;
        ProxyChecker(ProxyInternetAddress address) {
            this.address = address;
        }
        /** */
        public void run() {
            try {
                HttpClient client = new HttpClient();
//System.err.println("TRY: " + address);
                client.getHostConfiguration().setProxy(address.getHostName(), address.getPort());

                HeadMethod head = new HeadMethod("http://www.yahoo.co.jp/");
                int status = client.executeMethod(head);
//System.err.println("STA: " + status);

                boolean alive = status == HttpStatus.SC_OK;
                address.setAlive(alive);
            } catch (Exception e) {
//System.err.println("ERR: " + e);
                address.setAlive(false);
            } finally {
System.err.println("ADD: " + address);
            }
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        CyberSyndromeProxyServerDao proxyServerDao = new CyberSyndromeProxyServerDao();
        proxyServerDao.proxyAddresses = proxyServerDao.getProxyInetSocketAddresses();
        for (InternetAddress proxyAddress : proxyServerDao.proxyAddresses) {
            System.err.println("proxy: " + proxyAddress);
        }
    }
}

/* */
