/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.openfeint;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.apache.commons.codec.binary.Base64;


/**
 * OAuthUtil. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/04 nsano initial version <br>
 */
public class OpenfeintOAuthUtil {

    /** */
    protected OAuthConsumer consumer;

    /** */
    protected OAuthClient client;

    // openfeint
    String appBundleId;
    String appVersion;
    
    // iphone
    String udid;
    String hwVersion;
    String osVersion;

    // openfeint system
    private static final String openfentVersion = "12312009";

    private static final String baseURL = "https://api.openfeint.com/";

    /** */
    public OpenfeintOAuthUtil(OAuthConsumer consumer) {
        this.consumer = consumer;
        this.client = new OAuthClient(new GaeHttpClient());
    }

    private String appid;

    /**
     * http://.../oauth/access_token
     * @return String[] { uid, accessToken, tokenSecret }
     */
    public String[] getAccessToken(String uid) throws IOException, OAuthException {
        try {
            Map<String, String> params = new HashMap<String, String>();
            // iphone device no
            params.put("udid", udid);
            if (uid == null) {
                params.put("create_new_account", "1");
            } else {
                params.put("user_id", uid);
            }
            params.put("device_hardware_version", hwVersion);
            params.put("device_os_version", osVersion);

            params.put("achievements_sync_date", "2010-01-15 06:15:19");
            params.put("leaderboards_sync_date", "2010-01-15 06:15:19");
            params.put("[user_stats]total_dashboard_duration", "547");
            params.put("[user_stats]total_dashboard_launches", "14");
            params.put("[user_stats]total_game_session_duration", "0");
            params.put("[user_stats]total_game_sessions", "14");
            params.put("[user_stats]total_online_game_sessions", "14");        
    
            params.put("info_client_application_version", appVersion);
            params.put("info_client_application_bundle_id", appBundleId);
            params.put("info_client_openfeint_version", openfentVersion);
            params.put("info_client_device_locale", "en_US");

            params.put("oauth_token", "");

            OAuthAccessor accessor = new OAuthAccessor(consumer);
            accessor.tokenSecret = "";

            OAuthMessage response = client.invoke(accessor, "POST", baseURL + "bootstrap.xml", params.entrySet());

            InputStream is = response.getBodyAsStream();
    
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(is);
    
            XPath xPath = XPathFactory.newInstance().newXPath();
            String accessToken = xPath.evaluate("/resources/bootstrap/access_token", document);
            String tokenSecret = xPath.evaluate("/resources/bootstrap/access_token_secret", document);
            appid = xPath.evaluate("/resources/bootstrap/client_application_id", document);
            if (uid == null) {
                uid = xPath.evaluate("/resources/bootstrap/user/id", document);
            }
//System.err.println("appid: " + appid);
//System.err.println("uid: " + uid);
            
            return new String[] { uid, accessToken, tokenSecret };
            
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /*
     * GET
     * client_applications/${appid}/leaderboards.xml
     *
     * GET
     * client_applications/@me/high_scores.xml
     * leaderboard_id=123456
     * page=1
     * page_size=25
     * 
     * GET
     * profiles/12345678/?compared_to_user_id=me
     * 
     * GET
     * profile_picture
     *  
     * GET
     * profiles/me/
     * 
     * GET
     * users/for_device.xml
     * 
     * GET
     * users/@me/settings.xml
     */
    public InputStream getResource(String url, String accessToken, String tokenSecret) throws IOException, OAuthException, URISyntaxException {
        Map<String, String> params = new HashMap<String, String>();
        return getResource(url, params, accessToken, tokenSecret);
    }

    /** */
    public InputStream getResource(String url, Map<String, String> params, String accessToken, String tokenSecret) throws IOException, OAuthException, URISyntaxException {
        url = String.format(url, appid);
//System.err.println("url: " + url);

        params.put("info_client_application_version", appVersion);
        params.put("info_client_application_bundle_id", appBundleId);
        params.put("info_client_openfeint_version", openfentVersion);
        params.put("info_client_device_locale", "en_US");

        params.put("oauth_token", accessToken);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.tokenSecret = tokenSecret;

        OAuthMessage response = client.invoke(accessor, baseURL + url, params.entrySet());

        return response.getBodyAsStream();
    }

    /*
     * POST
     * bootstrap.xml
     * udid=A1B23CD8-1A23-123A-1234-AB1CD2E345FG
     * &create_new_account=1
     * &device_hardware_version=x86_64
     * &device_os_version=3.1.2
     * &achievements_sync_date=1970-01-01%2000%3A00%3A00
     * &leaderboards_sync_date=1970-01-01%2000%3A00%3A00
     * &%5Buser_stats%5Dtotal_dashboard_duration=129
     * &%5Buser_stats%5Dtotal_dashboard_launches=4
     * &%5Buser_stats%5Dtotal_game_session_duration=0
     * &%5Buser_stats%5Dtotal_game_sessions=2
     * &%5Buser_stats%5Dtotal_online_game_sessions=2
     *
     * POST
     * users/update_name.xml
     * user%5Bname%5D=your_name
     * &id=me
     * 
     * POST
     * profile_picture/select/http_basic
     * uploaded_profile_picture=
     * 
     * POST
     * profile_picture/refresh
     * 
     * POST
     * users/@me/set_location.xml
     * lat=33.331689
     * &lng=-121.030731
     * 
     * POST
     * client_applications/@me/high_scores.xml
     * leaderboard_id=123456
     * &high_score[score]=30
     * &high_score[lat]=33.331689
     * &high_score[lng]=-121.030731
     * 
     * POST
     * users/@me/unlocked_achievements.xml
     * achievement_definition_id=1234567
     */
    public InputStream postResource(String url, Map<String, String> params, String accessToken, String tokenSecret) throws IOException, OAuthException, URISyntaxException {
        url = String.format(url, appid);
//System.err.println("url: " + url);

        params.put("info_client_application_version", appVersion);
        params.put("info_client_application_bundle_id", appBundleId);
        params.put("info_client_openfeint_version", openfentVersion);
        params.put("info_client_device_locale", "en_US");

        params.put("oauth_token", accessToken);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.tokenSecret = tokenSecret;

        OAuthMessage response = client.invoke(accessor, "POST", baseURL + url, params.entrySet());

        return response.getBodyAsStream();
    }


    /** */
    public InputStream postPicture(String url, InputStream is, String accessToken, String tokenSecret) throws IOException, OAuthException, URISyntaxException, GeneralSecurityException {
        url = String.format(url, appid);
System.err.println("url: " + url);

        Map<String, String> params = new HashMap<String, String>();
        params.put("info_client_application_version", appVersion);
        params.put("info_client_application_bundle_id", appBundleId);
        params.put("info_client_openfeint_version", openfentVersion);
        params.put("info_client_device_locale", "en_US");

        params.put("oauth_token", accessToken);

        params.put("uploaded_profile_picture_signature", getSignature(String.valueOf(is.available()), tokenSecret));

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.tokenSecret = tokenSecret;

        OAuthClient client = new OAuthClient(new FormDataHttpClient("uploaded_profile_picture", is));
        OAuthMessage response = client.invoke(accessor, "POST", baseURL + url, params.entrySet());

        return response.getBodyAsStream();
    }

    /** */
    private String getSignature(String baseString, String tokenSecret) throws IOException, GeneralSecurityException {
        String keyString = OAuth.percentEncode(consumer.consumerSecret + '&' + tokenSecret);
        byte[] keyBytes = keyString.getBytes(OAuth.ENCODING);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        byte[] text = baseString.getBytes(OAuth.ENCODING);
        return new String(Base64.encodeBase64(mac.doFinal(text)));
    }
}

/* */
