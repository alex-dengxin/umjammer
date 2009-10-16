/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko.impl;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import vavi.apps.yamatonadeshiko.MailDAO;
import vavi.apps.yamatonadeshiko.Shuffler;
import vavi.apps.yamatonadeshiko.UnitDAO;
import vavi.apps.yamatonadeshiko.Shuffler.Member;

import static junit.framework.Assert.assertTrue;


/**
 * Version1ShufflerTest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2009/04/20 nsano initial version <br>
 */
public class Version1ShufflerTest {

    @Test
    public void test01() throws Exception {
        for (int i = 0; i < 100; i++) {
            test01Internal();
            System.err.println(i + " times clear.");
        }
        assertTrue(true);
    }

    private void test01Internal() throws Exception {
        MailDAO mailDAO = new MySQLMailDAO();
        UnitDAO unitDAO = new LocalFileUnitDAO();
        Shuffler shuffler = new Version1Shuffler();
        String unit = unitDAO.load();
        List<Member>[] memberLists = mailDAO.load(unit);
        List<Member> femaleManagers = memberLists[0];
        List<Member> females = memberLists[1];
        List<Member> maleManagers = memberLists[2];
        List<Member> males = memberLists[3];
        females.addAll(0, femaleManagers);
        males.addAll(0, maleManagers);
        Map<Member, Member> pair = shuffler.shuffle(females, males);
System.err.println("---- start ----");
        for (Member member : pair.keySet()) {
            Member couple = pair.get(member);
            System.err.println(member + " -> " + couple);
        }
System.err.println("---- end ----");
    }
}

/* */
