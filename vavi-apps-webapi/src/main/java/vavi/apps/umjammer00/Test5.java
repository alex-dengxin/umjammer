/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import vavi.net.rest.Rest;


/**
 * Test5. YahooJapanSearch (JAXB)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080319 nsano initial version <br>
 */
public class Test5 {
    /** */
    public static void main(String[] args) throws Exception {
        Test5 test = new Test5();
        test.test(args);
    }

    public Random random = new Random(System.currentTimeMillis());

    /** */
    void test(String[] args) throws Exception {
        String query1 = args[0];

        String target = "小学校";
        vavi.apps.umjammer00.jaxb1.ResultSet result1 = query1(query1 + " " + target);
        String result2 = query2(result1, "名詞-固有名詞-地域", "名詞");
        String result3 = query2(result1, "名詞-固有名詞", "名詞");
        System.err.println(result2 + result3 + " " + target);
        target = "中学校";
        result1 = query1(query1 + " " + target);
        result2 = query2(result1, "名詞-固有名詞-地域", "名詞");
        result3 = query2(result1, "名詞-固有名詞", "名詞");
        System.err.println(result2 + result3 + " " + target);
        target = "高等学校";
        result1 = query1(query1 + " " + target);
        result2 = query2(result1, "名詞-固有名詞-地域", "名詞");
        result3 = query2(result1, "名詞-固有名詞", "名詞");
        System.err.println(result2 + result3 + " " + target);
        target = random.nextInt(1) == 1 ? "大学" : "専門学校";
        result1 = query1(x(query1) + " " + target);
        result2 = query2(result1, "名詞-固有名詞-地域", "名詞");
        result3 = query2(result1, "名詞-固有名詞", "名詞");
        System.err.println(result2 + result3 + " " + target);
        target = "趣味";
        result1 = query1(x(query1) + " " + target);
        result2 = query2(result1, "名詞-一般", "名詞");
        result3 = query2(result1, "動詞", "名詞");
        System.err.println(target + ": " + result2 + result3);
        target = "性格";
        result1 = query1(x(query1) + " " + target);
        result2 = query2(result1, "名詞-一般", "名詞");
        result3 = query2(result1, "動詞", "名詞");
        System.err.println(target + ": " + result2 + result3);
        target = "志望動機";
        result1 = query1(x(query1) + " " + target);
        result2 = query2(result1, "名詞-一般", "名詞");
        result3 = query2(result1, "動詞", "名詞");
        System.err.println(target + ": " + result2 + result3);
    }

    public vavi.apps.umjammer00.jaxb1.ResultSet query1(String query) throws IOException {
System.err.println("query: " + query);
        YahooJapanSearch queryBean = new YahooJapanSearch();
        queryBean.appid = System.getProperty("yjws.appid");
        queryBean.query = query;

        vavi.apps.umjammer00.jaxb1.ResultSet resultBean = getResult(queryBean);
        
        return resultBean;
    }

    public String query2(vavi.apps.umjammer00.jaxb1.ResultSet resultBean, String first, String second) throws IOException {
        StringTagger tagger = StringTagger.getInstance();

        List<String> results = new ArrayList<String>();

        for (vavi.apps.umjammer00.jaxb1.ResultType result : resultBean.getResult()) {
            for (Token token : tagger.analyze(result.getSummary())) {
//System.err.println("token: " + token + "\t" + token.getPos());
            
                if (token.getPos().startsWith(first)) {
                    results.add(token.toString());
                }
            }
        }

        if (results.size() == 0) {
            for (vavi.apps.umjammer00.jaxb1.ResultType result : resultBean.getResult()) {
                for (Token token : tagger.analyze(result.getSummary())) {
//System.err.println("token: " + token + "\t" + token.getPos());
            
                    if (token.getPos().startsWith(second)) {
                        results.add(token.toString());
                    }
                }
            }
        }

        if (results.size() > 0) {
            return results.get(random.nextInt(results.size()));
        } else {
            return null;
        }
    }

    public String x(String name) throws IOException {
        StringTagger tagger = StringTagger.getInstance();

        Token[] tokens = tagger.analyze(name);
        StringBuilder sb = new StringBuilder(); 
        for (int i = 0; i < tokens.length; i++) {
            sb.append(tokens[i].toString());
            sb.append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /** as RPC */
    public vavi.apps.umjammer00.jaxb1.ResultSet getResult(YahooJapanSearch queryBean) {
        String url = Rest.Util.getUrl(queryBean);
System.err.println("url: " + url);
        Request request = new Request(Method.GET, url);

        // Handle it using an HTTP client connector
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        // Write the response entity on the console
        Representation output = response.getEntity();
        try {
            JAXBContext context = JAXBContext.newInstance("vavi.apps.umjammer00.jaxb1");
            Unmarshaller unmarshaller = context.createUnmarshaller();

//Reader r = new InputStreamReader(output.getStream(), "JISAutoDetect");
//while (r.ready()) {
//  System.err.print((char) r.read());
//}
            return (vavi.apps.umjammer00.jaxb1.ResultSet) unmarshaller.unmarshal(new InputStreamReader(output.getStream(), "UTF-8"));
        } catch (Exception e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
