/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import net.java.sen.StringTagger;
import net.java.sen.Token;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import vavi.awt.image.resample.AwtResampleOp;
import vavi.net.rest.Rest;
import vavi.util.CharConverterJa;


/**
 * Test6. YahooJapanImageSearch (JAXB)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080319 nsano initial version <br>
 */
public class Test6 {
    /** */
    public static void main(String[] args) throws Exception {
        Test6 test = new Test6();
        test.test(args);
    }

    /** */
    void test(String[] args) throws Exception {
        String query = args[0];
        String url = query(x(query));
        if (url == null) {
            throw new NoSuchElementException(query);
        }
        BufferedImage image = ImageIO.read(new URL(url));
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImageOp filter = new AwtResampleOp(120f / w, 160f / h);
        BufferedImage filteredImage = filter.filter(image, null);
        ImageIO.write(filteredImage, "JPEG", new File("out.jpg"));
    }

    Random random = new Random(System.currentTimeMillis());

    public String query(String query) throws IOException {
System.err.println("query: " + query);
        YahooJapanImageSearch queryBean = new YahooJapanImageSearch();
        queryBean.appid = System.getProperty("yjws.appid");
        queryBean.query = query;

        vavi.apps.umjammer00.jaxb2.ResultSet resultBean = getResult(queryBean);
        if (resultBean.getResult().size() > 0) {
            return resultBean.getResult().get(random.nextInt(resultBean.getResult().size())).getUrl();
        } else {
            return null;
        }
    }

    public String x(String name) throws IOException {
        StringTagger tagger = StringTagger.getInstance();

        Token[] tokens = tagger.analyze(name);
        StringBuilder sb = new StringBuilder(); 
        for (int i = 0; i < tokens.length; i++) {
//System.err.println("token: " + tokens[i].getPos());
            sb.append(CharConverterJa.toHiragana(tokens[i].getPronunciation()));
            sb.append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /** as RPC */
    vavi.apps.umjammer00.jaxb2.ResultSet getResult(YahooJapanImageSearch queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
            JAXBContext context = JAXBContext.newInstance("vavi.apps.umjammer00.jaxb2");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (vavi.apps.umjammer00.jaxb2.ResultSet) unmarshaller.unmarshal(new InputStreamReader(output.getStream(), "UTF-8"));
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
