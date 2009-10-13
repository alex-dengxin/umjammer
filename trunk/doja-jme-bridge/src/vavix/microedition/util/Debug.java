/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.microedition.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;

import com.nttdocomo.io.BufferedReader;


/**
 * デバッグのユーティリティクラスです． 表示レベルの設定で、標準エラー出力に表示するメッセージを制御します.
 * 
 * <pre>
 *  参照するシステムプロパティ
 * 
 *   &quot;debug.stackDepth&quot;  スタックダンプの深さ，VM 実装によって違う．
 *                       3 が J2SE for Windows の値でデフォルト．
 *   &quot;debug.level&quot;   デバッグレベル
 * </pre>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.08a 021025 nsano kkyx version <br>
 *          0.09a 021211 nsano fix dump <br>
 *          0.10a 030114 nsano add withTime related <br>
 */
public class Debug {

    /**
     * 完全にデバッグコードを取り除く場合は以下を false に してすべてを再コンパイルしてください．
     */
    private static final boolean isDebug = true;

    // private static final boolean isDebug = false;

    // -------------------------------------------------------------------------

    /**
     * 現在デバッグ中のレベル
     * 
     * @see #level
     */
    public static final int SPECIAL = 0;

    /**
     * エラーのレベル
     * 
     * @see #level
     */
    public static final int ERROR = 1;

    /**
     * 警告のレベル
     * 
     * @see #level
     */
    public static final int WARNING = 2;

    /**
     * 情報を表示するレベル
     * 
     * @see #level
     */
    public static final int INFO = 3;

    /**
     * デバッグ情報を表示するレベル
     * 
     * @see #level
     */
    public static final int DEBUG = 99;

    /**
     * 時間情報を表示する
     * 
     * @see #level
     */
    private static boolean withTime;

    /** 表示するレベル (default:{@link kkyx.util.Debug#DEBUG} Level) */
    private static int level;

    /** デバッグ情報の出力先ストリーム */
    private static PrintStream out = System.err;

    /** スタックトレース情報を取る深さ */
    private static int depth;

    /**
     * 初期化します．
     */
    static {
        depth = 3;
        level = DEBUG;
        withTime = false;
    }

    /**
     * アクセスできません．
     */
    private Debug() {
    }

    /**
     * 表示するレベルを設定します．
     * 
     * @param level 設定する表示レベル
     */
    public static final void setDebugLevel(int level) {
        if (isDebug) {
            Debug.level = level;
        }
    }

    /** */
    public static boolean isDebug() {
        return isDebug;
    }

    /** */
    private static void printTime() {
        out.print(new Date() + " ");
    }

    /**
     * 表示レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param level このメッセージの表示レベル
     * @param message 表示メッセージ
     */
    public static final void println(int level, Object message) {
        if (isDebug && level <= Debug.level) {
            if (withTime) {
                printTime();
            }
            Context c = getContext(depth);
            out.println(getClassName(c.className) + "::" + c.methodName + ": " + message);
        }
    }

    /**
     * 表示レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param level このメッセージの表示レベル
     * @param message 表示メッセージ
     */
    public static final synchronized void println(int level, boolean message) {
        if (isDebug) {
            depth++;
            println(level, String.valueOf(message));
            depth--;
        }
    }

    /**
     * 表示レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param level このメッセージの表示レベル
     * @param message 表示メッセージ
     */
    public static final synchronized void println(int level, int message) {
        if (isDebug) {
            depth++;
            println(level, String.valueOf(message));
            depth--;
        }
    }

    /**
     * デバッグ #Debug レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param message 表示メッセージ
     */
    public static final synchronized void println(Object message) {
        if (isDebug) {
            depth++;
            println(Debug.DEBUG, message);
            depth--;
        }
    }

    /**
     * デバッグ #Debug レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param message 表示メッセージ
     */
    public static final synchronized void println(int message) {
        if (isDebug) {
            depth++;
            println(Debug.DEBUG, String.valueOf(message));
            depth--;
        }
    }

    /**
     * デバッグ #Debug レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param message 表示メッセージ
     */
    public static final synchronized void println(boolean message) {
        if (isDebug) {
            depth++;
            println(Debug.DEBUG, String.valueOf(message));
            depth--;
        }
    }

    /**
     * デバッグ #Debug レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param message 表示メッセージ
     */
    public static final synchronized void println(double message) {
        if (isDebug) {
            depth++;
            println(Debug.DEBUG, String.valueOf(message));
            depth--;
        }
    }

    /**
     * 表示レベルよりメッセージレベルが低い時， 標準エラー出力にメッセージを出力します. フォーマットは以下のようになります．
     * <p>
     * <i>クラス名(パッケージ名を除く)</i>::<i>メソッド名</i>: <i>メッセージ</i>
     * 
     * @param level このメッセージの表示レベル
     * @param message 表示メッセージ
     */
    public static final void print(int level, Object message) {
        if (isDebug && level <= Debug.level) {
            if (withTime) {
                printTime();
            }
            Context c = getContext(depth);
            out.print(getClassName(c.className) + "::" + c.methodName + ": " + message);
        }
    }

    /**
     * デバッグ #Debug レベルよりメッセージレベルが低い時， 標準エラー出力に改行つきでメッセージを出力します．
     * 
     * @param message 表示メッセージ
     */
    public static final synchronized void print(Object message) {
        if (isDebug) {
            depth++;
            print(Debug.DEBUG, message);
            depth--;
        }
    }

    /**
     * パッケージ名を取り除いたクラス名を取得します． Debug#print 中でのみ使ってください． isDebug の最適化が利かなくなります．
     * 
     * @param name パッケージつきのクラス名
     * @return パッケージ名を取り除いたクラス名
     */
    public static final String getClassName(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * ストリームを 16 進数でダンプします．
     */
    public static final void dump(InputStream is) {
        try {
            byte[] buf = new byte[16];
            boolean breakFlag = false;
            int m = 0;
            top: while (true) {
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        int c = is.read();
                        if (c == -1) {
                            if (!breakFlag) {
                                breakFlag = true;
                                m = x;
                            }
                            if (m > 0)
                                out.print("   ");
                            else
                                break;
                        } else {
                            out.print(toHex2(c) + " ");
                            buf[x] = (byte) c;
                        }
                    }
                    for (int x = 0; x < 16; x++) {
                        if (breakFlag && x == m) {
                            out.println();
                            break top;
                        } else {
                            out.print(0x20 <= buf[x] && buf[x] <= 0x7e ? (char) buf[x] : '.');
                        }
                    }
                    out.println();
                }
                out.println();
            }
        } catch (IOException e) {
        }
    }

    /**
     * 先頭を 0 で埋めた 2 桁の大文字の 16 進数を返します．
     */
    public static final String toHex2(int i) {
        String s = "0" + Integer.toHexString(i).toUpperCase();
        return s.substring(s.length() - 2);
    }

    /**
     * 現在実行中のプログラムのコンテキスト情報を取得します． 現在は JavaSoft の Java2 SDK にしか対応していません．(たぶん)
     * 
     * level には 2 以上を指定する． getContext() を直接呼ぶ場合は 2， getContext() を呼ぶメソッドを呼ぶ場合は
     * 3， のように指定する。
     * <p>
     * 使用例 1：getContext() を直接呼ぶ場合 <tt><pre>
     * Context c = Debug.getContext(2);
     * System.err.println(&quot;現在実行中のメソッドは&quot; + c.methodName + &quot;です&quot;);
     * </pre></tt>
     * <p>
     * 使用例 2：getContext()を間接的に呼ぶ場合 <tt><pre>
     * Context check() {
     *     Debug.getContext(3);
     * }
     * </pre></tt>
     * <p>
     * <tt><pre>
     * Context c = check();
     * System.err.println(&quot;現在実行中のメソッドは&quot; + c.methodName + &quot;です&quot;);
     * </pre></tt>
     */
    private static final Context getContext(int level) {

        Context context = new Context();

        // printStackTrace()の出力結果を取得する
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//      PrintStream pout = new PrintStream(out);
        new Exception().printStackTrace();
        // new Exception().printStackTrace(System.err);

        // 欲しい行を切り出す
        BufferedReader din = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
        String targetLine = "  at CLASS.METHOD(FILE.java:0000)"; // ダミー
        try {
            for (int i = 0; i < level; i++) {
                din.readLine();
            }
            targetLine = din.readLine();

            // 切り出した行をトークンに分割
            // System.err.println("Context::getContext: " + targetLine);
            StringTokenizer tk = new StringTokenizer(targetLine, " \n\t():,");
            String[] tokens = new String[5];
            int i;
            for (i = 0; tk.hasMoreTokens(); i++) {
                tokens[i] = tk.nextToken();
                // System.err.println("Context::getContext: token[" + i + "]" +
                // tokens[i]);
            }
            /*
             * if (i == 4) ; else if (i == 5) tokens[3] += " " + tokens[4]; else
             * return null;
             */
            if (i != 4 && i != 5)
                throw new IllegalArgumentException(targetLine);

            // Contextにセット
            // tokens[0]は "at" なので捨てる
            context.className = tokens[1].substring(0, tokens[1].lastIndexOf('.'));

            context.methodName = tokens[1].substring(tokens[1].lastIndexOf('.') + 1, tokens[1].length());
            context.sourceFile = tokens[2];
            try {
                context.lineNumber = Integer.parseInt(tokens[3]);
            } catch (NumberFormatException e) {
                // "Compiled Code", "Native Code" 等
                context.lineNumber = -1;
            }

            // System.err.println("Context::getContext: " + context.className);
            // System.err.println("Context::getContext: " + context.methodName);
            // System.err.println("Context::getContext: " + context.sourceFile);
            // System.err.println("Context::getContext: " + context.lineNumber);
        } catch (Exception e) {
            // System.err.println("Context::getContext: " + e);
            context.className = "Unknown";
            context.methodName = "unknown";
            context.sourceFile = "unknown";
            context.lineNumber = -1;
        }

        return context;
    }

    /**
     * プログラムのコンテキスト情報のクラスです．
     */
    private static final class Context {
        public String className;

        public String methodName;

        public String sourceFile;

        public int lineNumber;

        public String toString() {
            return getClass().getName() + "[" + className + ", " + methodName + ", " + sourceFile + ", " + lineNumber + "]";
        }
    }
}

/* */
