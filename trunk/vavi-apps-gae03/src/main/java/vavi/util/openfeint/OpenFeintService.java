/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.openfeint;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import net.oauth.OAuthConsumer;
import net.oauth.OAuthProblemException;

import org.springframework.stereotype.Component;

import vavi.util.openfeint.jaxb.highscore.ResourceSections;


/**
 * OpenFeintService. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/14 nsano initial version <br>
 */
@Component
public class OpenFeintService {

    final String consumerKey = "yyyyyyyyyyyyyyyyyyyyyy";
    final String consumerSecret = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    OpenfeintOAuthUtil util;

    Cache cache;

    /** */
    public OpenFeintService() {
        util = new OpenfeintOAuthUtil(new OAuthConsumer("oob", consumerKey, consumerSecret, new OpenfeintOAuthProvider()));
        util.appBundleId = "vavi.games.loderunner.LodeRunner"; 
        util.appVersion = "2.0";

        util.udid = "ZZZZZZZZ-ZZZZ-ZZZZ-ZZZZ-ZZZZZZZZZZZZ";
        util.hwVersion = "x86_64";
        util.osVersion = "3.1.2";

        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException e) {
            throw new IllegalStateException(e);
        }
    }

    /** just login */
    @SuppressWarnings("unchecked")
    public String[] login(String feint) throws Exception {
        String[] session = (String[]) cache.get(feint);
        if (session == null) {
            session = util.getAccessToken(feint);
            cache.put(feint, session);
        }
        return session;
    }

    /** create user */
    public String[] login() throws Exception {
        return util.getAccessToken(null);
    }
    
    /** */
    public void setScore(long leaderboard, long score, String[] session) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("leaderboard_id", String.valueOf(leaderboard));
        params.put("high_score[score]", String.valueOf(score));
        params.put("high_score[lat]", "35.331689");
        params.put("high_score[lng]", "-121.030731");
        util.postResource("client_applications/@me/high_scores.xml", params, session[1], session[2]);
    }
    
    /**
     * @throws IllegalArgumentException duplicated name
     */
    public void rename(String name, String[] session) throws Exception {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("user[name]", name);
            params.put("id", "me");
            util.postResource("users/update_name.xml", params, session[1], session[2]);
        } catch (OAuthProblemException e) {
            throw new IllegalArgumentException(name);
        }
    }

    /** */
    public void updatePicture(InputStream is, String[] session) throws Exception {
        util.postPicture("profile_picture/select/http_basic", is, session[1], session[2]);
    }
    
    /** */
    public void unlockAchievement(long achievementId, String[] session) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("achievement_definition_id", String.valueOf(achievementId));
        util.getResource("users/@me/unlocked_achievements.xml", params, session[1], session[2]);
    }

    /** */ 
    public ResourceSections getLeaderboard(long leaderboard, String[] session) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("leaderboard_id", String.valueOf(leaderboard));
        params.put("page", "1");
        params.put("page_size", "10");
        InputStream is = util.getResource("client_applications/@me/high_scores.xml", params, session[1], session[2]);
        
        JAXBContext context = JAXBContext.newInstance("vavi.util.openfeint.jaxb.highscore");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        ResourceSections rss = (ResourceSections) unmarshaller.unmarshal(new InputStreamReader(is, "UTF-8"));

        return rss;
    }
}

/* */
