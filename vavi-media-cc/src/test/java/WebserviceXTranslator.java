/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import vavi.net.ws.webservicex.translation.TranslateService;
import vavi.net.ws.webservicex.translation.TranslateServiceSoap;
import vavi.util.Debug;
import vavix.util.screenscrape.CyberSyndromeProxyServerDao;
import vavix.util.screenscrape.ProxyChanger;
import vavix.util.screenscrape.ProxyServerDao;
import vavix.util.screenscrape.ProxyChanger.InternetAddress;
import vavix.util.translation.Translator;


/**
 * WebserviceXTranslator. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2007/10/06 nsano initial version <br>
 */
class WebserviceXTranslator implements Translator {

    /** */
    private static ProxyChanger proxyChanger;

    /** */
    private static String originalDocumentBuilderFactory;

    /* */
    static {
        originalDocumentBuilderFactory = System.getProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
Debug.println("originalDocumentBuilderFactory: " + originalDocumentBuilderFactory);
        // TODO do in ProxyChanger
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "vavi.xml.jaxp.html.cyberneko.DocumentBuilderFactoryImpl");

        proxyChanger = new ProxyChanger();
        ProxyServerDao proxyServerDao = new CyberSyndromeProxyServerDao();
        proxyChanger.setProxyServerDao(proxyServerDao);
        while (proxyServerDao.getProxyInetSocketAddresses().size() < 5) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

//      System.setProperty("javax.xml.parsers.DocumentBuilderFactory", originalDocumentBuilderFactory);
    }

    /** */
    private TranslateServiceSoap translator;

    /** don't change!!! */
    private static final String ERROR_MESSAGE1 = "The remote server returned an error";
    /** don't change!!! */
    private static final String ERROR_MESSAGE2 = "Error occured when translating text"; //  please contact support@webservicex.net

    /** */
    private String languageMode; // TODO

    /** */
    private boolean useProxy;

    /** */
    WebserviceXTranslator(String languageMode, boolean useProxy) {
        this.languageMode = languageMode;
        this.useProxy = useProxy;

        // TODO to be delete
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", originalDocumentBuilderFactory);

        TranslateService service = new TranslateService();
        translator = service.getTranslateServiceSoap();
    }

    /* TODO */
    public Locale getGlobalLocal() {
        return null;
    }

    /* TODO */
    public Locale getLocalLocale() {
        return null;
    }

    /* TODO */
    public String toGlobal(String word) throws IOException {
        return translator.translate(languageMode, word);
    }

    /** */
    private Set<InternetAddress> badProxies = new HashSet<InternetAddress>();

    /** */
    private InternetAddress applyProxy() { 
        if (!useProxy) {
Debug.println("NOT USE PROXY");
            return null;
        }

        Client cxfClient = ClientProxy.getClient(translator); 
        HTTPConduit httpConduit = (HTTPConduit) cxfClient.getConduit();
        HTTPClientPolicy httpClientPolicy = httpConduit.getClient();

        InternetAddress proxy;
        int retryCount = 0;
        do {
            proxy = proxyChanger.getInetSocketAddress();
            retryCount++;
            if (retryCount > 1000) {
Debug.println("NO GOOD PROXY: badProxies: " + badProxies.size());
                return null;
            }
        } while (badProxies.contains(proxy));

        httpClientPolicy.setProxyServer(proxy.getHostName());
        httpClientPolicy.setProxyServerPort(proxy.getPort());
//          httpClientPolicy.setProxyServer("localhost");
//          httpClientPolicy.setProxyServerPort(8080);
Debug.println("PROXY: " + proxy + ", badProxies: " + badProxies.size());
        return proxy;
    }

    /** */
    private static final int maxError = 10;

    /** */
    private int interval = 10 * 000;

    /* */
    public String toLocal(String word) throws IOException {

        for (int i = 0; i < maxError; i++) {
            InternetAddress proxy = applyProxy();

            String translated;
            try {
                translated = translator.translate(languageMode, word);
            } catch (Exception e) {
Debug.println("SOAP ERROR: " + e);
                if (!useProxy) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException f) {
                        f.printStackTrace(System.err);
                    }
                } else {
                    badProxies.add(proxy);
                }
                continue;
            }
            if (translated.startsWith(WebserviceXTranslator.ERROR_MESSAGE1) ||
                translated.startsWith(WebserviceXTranslator.ERROR_MESSAGE2)) {
Debug.println("SERVICE ERROR: " + translated);
                if (!useProxy) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException f) {
                        f.printStackTrace(System.err);
                    }
                } else {
                    badProxies.add(proxy);
                }
                continue;
            } else {
                return translated;
            }
        }

        throw new IOException("too many errors: " + word);
    }

    /**
     * @param args [0] japanese sentence, [1] english sentence 
     */
    public static void main(String[] args) throws IOException {

        Translator translator = new WebserviceXTranslator("EnglishTOJapanese", false);

        System.out.println("---- E to J ----");
        System.out.println("I: " + args[0]);
long t1 = System.currentTimeMillis();
        System.out.println("O: " + translator.toLocal(args[0]));
System.out.println("This translation costs " + (System.currentTimeMillis() - t1) + " ms");

//        System.out.println("---- J to E ----");
//        System.out.println("I: " + args[1]);
//long t2 = System.currentTimeMillis();
//        System.out.println("O: " + translator.toGlobal(args[1]));
//System.out.println("This translation costs " + (System.currentTimeMillis() - t2) + " ms");
        System.exit(0);
    }
}

/* */
