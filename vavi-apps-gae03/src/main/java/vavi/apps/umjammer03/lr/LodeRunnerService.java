/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer03.lr;

import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vavi.util.openfeint.OpenFeintService;
import vavi.util.openfeint.jaxb.highscore.ResourceSections;


/**
 * LodeRunnerService. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/18 nsano initial version <br>
 */
@Component
public class LodeRunnerService {

    @Autowired(required = true)
    MixiFeintService mixiFeintService;

    @Autowired(required = true)
    OpenFeintService openFeintService;
    
    static long leaderboard = 123456;

    /** */
    public String get(String mixi) {
        MixiFeint mixiFeint = mixiFeintService.getMixiFeint(mixi);
        return mixiFeint.getFeint();
    }

    /** */
    public void add(String mixi, String feint) {
        MixiFeint mixiFeint = new MixiFeint(feint, mixi); 
        mixiFeintService.createMixiFeint(mixiFeint);
    }

    /** */
    public void update(String mixi, String feint) {
        MixiFeint mixiFeint = mixiFeintService.getMixiFeint(mixi);
        mixiFeint.setFeint(feint);
        mixiFeintService.updateMixiFeint(mixiFeint);
    }

    /** */
    public void delete(String mixi) {
        MixiFeint mixiFeint = mixiFeintService.getMixiFeint(mixi);
        mixiFeintService.deleteMixiFeint(mixiFeint.getId());
    }

    /** */
    public void rename(String mixi, String name) throws Exception {
        String[] session = login(mixi);
        openFeintService.rename(name, session);
    }

    /** */
    public void updatePicture(String mixi, InputStream is) throws Exception {
        String[] session = login(mixi);
        openFeintService.updatePicture(is, session);
    }

    /** */
    @SuppressWarnings("unchecked")
    public List<MixiFeint> list() {
        return mixiFeintService.getAllMixiFeints();
    }

    /** */
    private String[] login(String mixi) throws Exception {
        MixiFeint mixiFeint = mixiFeintService.getMixiFeint(mixi);
        String[] session = null;
        if (mixiFeint == null) {
            session = openFeintService.login();
            String feint = session[0];
System.err.println("create feint " + feint + " for: " + mixi);
            mixiFeint = new MixiFeint(feint, mixi);
            mixiFeintService.createMixiFeint(mixiFeint);
        } else {
            String feint = mixiFeint.getFeint();
System.err.println("feint is " + feint + " for: " + mixi);
            session = openFeintService.login(feint);
        }
        return session;
    }

    /** */
    public void score(String mixi, long score) throws Exception {
        String[] session = login(mixi);
        openFeintService.setScore(leaderboard, score, session);
    }

    /** */
    public ResourceSections highscore(String mixi) throws Exception {
        String[] session = login(mixi);
        return openFeintService.getLeaderboard(leaderboard, session);
    }
}

/* */
