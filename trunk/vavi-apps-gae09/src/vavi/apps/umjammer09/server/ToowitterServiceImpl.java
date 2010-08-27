
package vavi.apps.umjammer09.server;

import java.io.ByteArrayInputStream;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import twitter4j.Twitter;
import twitter4j.http.OAuthAuthorization;
import twitter4j.util.ImageUpload;

import vavi.apps.umjammer09.client.ToowitterService;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ToowitterServiceImpl extends RemoteServiceServlet implements ToowitterService {

    // toowitter@twitpic
    private static final String twitpicKey = "cccccccccccccccccccccccccccccc";

    public boolean isSigned() {
        return getThreadLocalRequest().getSession().getAttribute("signin") != null;
    }

    public String upload(String input) {
        if (!isSigned()) {
            throw new IllegalStateException("not signed in");
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decodeBase64(input.getBytes()));

            Twitter twitter = (Twitter) getThreadLocalRequest().getSession().getAttribute("twitter");
            OAuthAuthorization OAuth = (OAuthAuthorization) twitter.getAuthorization();
            ImageUpload imageUpload = ImageUpload.getTwitpicUploader(twitpicKey, OAuth);
            String url = imageUpload.upload("#toowitter " + new Date(), bais, "#toowitter you can tweet over 140 letters! http://umjammer09.appspot.com/");
            twitter.updateStatus("#toowitter you can tweet over 140 letters! http://umjammer09.appspot.com/ " + url);
            return url;
        } catch (Exception e) {
e.printStackTrace(System.err);
            throw new IllegalStateException(e);
        }
    }

    public String uploadAsQrcode(String input) {
        try {
            return null;
        } catch (Exception e) {
e.printStackTrace(System.err);
            throw new IllegalStateException(e);
        }
    }
}
