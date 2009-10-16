/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Shuffler.
 * 
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050830 nsano initial version <br>
 */
public abstract class Shuffler {

    /** メンバーのタイプを表します。 */
    public enum Type {
        /** 未定義 */
        Unknown,
        /** 女幹事 */
        FemaleManager,
        /** 女メンバー */
        Female,
        /** 男幹事  */
        MaleManager,
        /** 男メンバー */
        Male;
        /** 幹事かどうか。 */
        public boolean isManager() {
            return (ordinal() & 0x01) != 0;
        }
    }

    /** メンバーを表します。 */
    public static class Member implements Comparable<Member> {
        public String email;
        public Type type;
        public String toString() {
            return email;
        }
        public int compareTo(Member member) {
            return email.compareToIgnoreCase(member.email);
        }
    }

    /**
     * ランダマイザー
     * TODO 場所いまいち、それぞれで実装すべき
     */
    protected Random random = new Random(System.currentTimeMillis());

    /**
     * シャッフル結果をストアします。
     * TODO 場所いまいち、それぞれで実装すべき
     */
    protected SortedMap<Member, Member> pair = new TreeMap<Member, Member>();

    /** シャッフルアルゴリズムを実装してください。 */
    public abstract Map<Member, Member> shuffle(List<Member> females, List<Member> males);
}

/* */
