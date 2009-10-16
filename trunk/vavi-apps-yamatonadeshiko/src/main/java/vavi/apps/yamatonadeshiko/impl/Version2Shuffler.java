/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import vavi.apps.yamatonadeshiko.MailDAO;
import vavi.apps.yamatonadeshiko.Shuffler;
import vavi.apps.yamatonadeshiko.UnitDAO;


/**
 * Shuffler.
 * 
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050926 nsano initial version <br>
 */
public class Version2Shuffler extends Shuffler {

    /** */
    private static Log log = LogFactory.getLog(Version2Shuffler.class);

    /* */
    public Map<Member, Member> shuffle(List<Member> females, List<Member> males) {
        do {
            pair.clear();
            shuffleInternal(females, males);
            shuffleInternal(males, females);
        } while (!isShuffledValid());

        return pair;
    }

    /**
     * 片性をシャッフルします。
     * @param sender
     * @param receiver 
     */
    private void shuffleInternal(List<Member> sender, List<Member> receiver) {
        int max;
        int startPairSize = pair.size();
        int[] usedIndices = new int[receiver.size()];

        for (Member member : sender) {
            max = (pair.size() - startPairSize) / receiver.size() + 1;
//log.debug("make pair for: " + member);
            while (!pair.containsKey(member)) {
                int index = random.nextInt(receiver.size());
                Member couple = receiver.get(index);
                if (usedIndices[index] >= max) {
//log.debug("over max: " + member);
                    continue;
                }
                pair.put(member, couple);
                usedIndices[index]++;
            }
        }
    }

    /** シャッフル結果が正しいかどうかチェックします。 */
    private boolean isShuffledValid() {
        Set<String> sendReceivePair = new HashSet<String>();

        for (Member member : pair.keySet()) {
            Member couple = pair.get(member);

            // 幹事同士は無し
            if (member.type.isManager() && couple.type.isManager()) {
//log.debug("both manager: " + member);
                return false;
            }

            // 送り、送られが同じもの同士は無し
            // A -> B
            // B -> A

            // A: if pair.containsKey(B) && pair.get(B) == A
            if (pair.containsKey(couple) && pair.get(couple).email.equals(member.email)) {
//log.debug("already coupled: " + member + ", " + couple);
                return false;
            }

            // 送った＋送られた、送られた＋送ったペアが同じのも無し
            // A -> 女1
            // 女2 -> A
            // 女2 -> B
            // B -> 女1

            // A: if pair.containsValue(A)
            if (pair.containsValue(member)) {
                for (Member member2: pair.keySet()) {
                    Member couple2 = pair.get(member2);
                    // if couple2 = A 
                    if (couple2.email.equals(member.email)) {
                        String pairString1 = couple.email + ":" + member2.email;
                        String pairString2 = member2.email + ":" + couple.email;
                        if (sendReceivePair.contains(pairString1) || sendReceivePair.contains(pairString2)) {
log.debug("already paired: " + member + ": " + member2 + ", " + couple);
                            return false;
                        } else {
                            sendReceivePair.add(pairString1);
                            sendReceivePair.add(pairString2);
                        }
                    }
                }
            }

        }

        return true;
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            test();
            System.err.println(i + " times clear.");
        }
    }

    /** */
    public static void test() throws Exception {
        MailDAO mailDAO = new MySQLMailDAO();
        UnitDAO unitDAO = new LocalFileUnitDAO();
        Shuffler shuffler = new Version2Shuffler();
        String unit = unitDAO.load();
        List<Member>[] memberLists = mailDAO.load(unit);
        List<Member> femaleManagers = memberLists[0];
        List<Member> females = memberLists[1];
        List<Member> maleManagers = memberLists[2];
        List<Member> males = memberLists[3];
        females.addAll(0, femaleManagers);
        males.addAll(0, maleManagers);
        Map<Member, Member> pair = shuffler.shuffle(females, males);
System.err.println("---- start ----: " + unit + ", " + females.size() + ", " + males.size());
        for (Member member : pair.keySet()) {
            Member couple = pair.get(member);
            System.err.println(member + " -> " + couple);
        }
System.err.println("---- end ----: " + pair.size());
    }
}

/* */
