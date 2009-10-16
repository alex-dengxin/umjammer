/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import vavi.apps.yamatonadeshiko.Shuffler;


/**
 * Shuffler.
 * 
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050830 nsano initial version <br>
 */
public class Version1Shuffler extends Shuffler {

    /** */
    private static Log log = LogFactory.getLog(Version1Shuffler.class);

    /* */
    public Map<Member, Member> shuffle(List<Member> females, List<Member> males) {
        shuffleInternal(females, males);
        shuffleInternal(males, females);
        return pair;
    }

    /**
     * @param sender Manager を前にしておかないと無限ループになることがある？？？
     * @param receiver 
     */
    private void shuffleInternal(List<Member> sender, List<Member> receiver) {

        int max;
        int startPairSize = pair.size();
        int[] usedIndices = new int[receiver.size()];

        for (Member member : sender) {
            max = (pair.size() - startPairSize) / receiver.size() + 1;
log.debug("make pair for: " + member);
            while (!pair.containsKey(member)) {
                int index = random.nextInt(receiver.size());
                Member couple = receiver.get(index);
                if (usedIndices[index] >= max) {
log.debug("over max: " + member);
                    continue;
                }
                if (member.type.isManager() && couple.type.isManager()) {
log.debug("both manager: " + member);
                    continue;
                }
                pair.put(member, couple);
                usedIndices[index]++;
            }
        }
    }
}

/* */
