/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.trader;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import vavix.util.screenscrape.ApacheURLScraper;


/**
 * ApacheURLHtmlData.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051014 nsano initial version <br>
 */
public class MonexURLScraper extends ApacheURLScraper<String> {

    /** */
    private Cookie[] cookies;

    /** */
    private HttpClient httpClient = new HttpClient();

    /**
     * @param props use followings
     * <pre>
     *  "account" BASIC 認証アカウント名
     *  "password" BASIC 認証パスワード
     *  "realm" BASIC 認証レルム
     *  "host"
     * </pre>
     */
    public MonexURLScraper(Properties props) {
        super(null);
        String account = props.getProperty("account");
        String password = props.getProperty("password");
        String loginUrl = props.getProperty("login.url");

        // https://www.monex.co.jp/Login/00000000/login/ipan_web/exec
        // SJIS encoding
        try {
            PostMethod postMethod = new PostMethod(loginUrl);
            postMethod.setRequestHeader("uid", "NULLGWDOCOMO");
            postMethod.setRequestHeader("loginid", account);
            postMethod.setRequestHeader("koza1", "");
            postMethod.setRequestHeader("koza2", "");
            postMethod.setRequestHeader("passwd", password);
            postMethod.setRequestHeader("syokiGamen", "0");
            postMethod.setRequestHeader("frameMode.x", "フレーム");
            postMethod.setRequestHeader("submit", " ログイン ");
            int status = httpClient.executeMethod(postMethod);
            if (status != 200) {
                throw new IllegalStateException("unexpected result: " + status);
            }
            // Cookie: 
            // MenuColor=0,80;
            // custom_num=11+22+0+49+31,clr;
            // loginmode=frm;
            // Apache=221.186.108.105.183131132897275897;
            // JSESSIONID=0000ocMrG07m8PhTlF2BMCqSsqj:-1;
            // nbsid=04019dbc4f3129054653;
            // s_cc=true;
            // s_sq=monexcojp%3D%2526pid%253Dhttps%25253A//www.monex.co.jp/Login/00000000/login/ipan_web/hyoji%2526oid%253D%252520%2525u30ED%2525u30B0%2525u30A4%2525u30F3%252520%2526oidt%253D3%2526ot%253DSUBMIT
            cookies = httpClient.getState().getCookies();

            // /Etc/0000JK8D/member/M901/menu/frame/topmenu2.htm
            // HTML/BODY/TABLE[1]/TR[1]/TD[3]/TABLE/TR[2]/FROM/@onsubmit
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    public String scrape(URL url) {
        try {

            GetMethod get = new GetMethod(url.toString());
            httpClient.getState().addCookies(cookies);
            int status = httpClient.executeMethod(get);
            if (status != 200) {
                throw new IllegalStateException("unexpected result: " + status);
            }

            String value = scraper.scrape(get.getResponseBodyAsStream());

            get.releaseConnection();

            return value;
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
