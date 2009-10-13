/*
 * http://java-house.jp/ml/archive/j-h-b/014452.html
 */

package vavi.util.bdj;



/**
 * The Cp932 class contains a utility method for converting Microsoft's
 * Cp 932 into JIS.
 *
 * @author Kazuhiro Kazama
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.0 01/06/97
 */
public class Cp932 {

    /** */
    private Cp932() {
    }

    /*
     * This method converts Cp932 to JIS.
     */
    public static char toJIS(char c) {
        switch (c) {
        case 0xff3c: // FULLWIDTH REVERSE SOLIDUS ->
            c = 0x005c; // REVERSE SOLIDUS
            break;
        case 0xff5e: // FULLWIDTH TILDE ->
            c = 0x301c; // WAVE DASH
            break;
        case 0x2225: // PARALLEL TO ->
            c = 0x2016; // DOUBLE VERTICAL LINE
            break;
        case 0xff0d: // FULLWIDTH HYPHEN-MINUS ->
            c = 0x2212; // MINUS SIGN
            break;
        case 0xffe0: // FULLWIDTH CENT SIGN ->
            c = 0x00a2; // CENT SIGN
            break;
        case 0xffe1: // FULLWIDTH POUND SIGN ->
            c = 0x00a3; // POUND SIGN
            break;
        case 0xffe2: // FULLWIDTH NOT SIGN ->
            c = 0x00ac; // NOT SIGN
            break;
        }
        return c;
    }

    /*
     * This method convert JIS to Cp932.
     */
    public static char toCp932(char c) {
        switch (c) {
        case 0x005c: // REVERSE SOLIDUS ->
            c = 0xff3c; // FULLWIDTH REVERSE SOLIDUS
            break;
        case 0x301c: // WAVE DASH ->
            c = 0xff5e; // FULLWIDTH TILDE
            break;
        case 0x2016: // DOUBLE VERTICAL LINE ->
            c = 0x2225; // PARALLEL TO
            break;
        case 0x2212: // MINUS SIGN ->
            c = 0xff0d; // FULLWIDTH HYPHEN-MINUS
            break;
        case 0x00a2: // CENT SIGN ->
            c = 0xffe0; // FULLWIDTH CENT SIGN
            break;
        case 0x00a3: // POUND SIGN ->
            c = 0xffe1; // FULLWIDTH POUND SIGN
            break;
        case 0x00ac: // NOT SIGN ->
            c = 0xffe2; // FULLWIDTH NOT SIGN
            break;
        }
        return c;
    }

    /**
     * MS932 文字列を Unicode 文字列に変換する。
     * 
     * @param s Shift JIS コードの文字列。
     * @return Unicode 文字列。
     */
    public static String toUnicode(byte[] s) {
        int sl = s.length;
        StringBuffer sb = new StringBuffer(sl);
        int bp = 0;
        char cl = '?';
        for (int i = 0; i < sl; i++) {
            char cu = (char) (s[i] & 0xff);
            if (i + 1 < sl) {
                cl = (char) (s[i + 1] & 0xff);
            }
            if (cu > 0xa0 && cu < 0xe0) {
                cu = Sjis.toUnicode(0x85, cu - 1);
            } else if (cu > 0x80) {
//System.err.println("CP932: 0x" + Integer.toHexString(cu) + Integer.toHexString(cl));
                cu = Sjis.toUnicode(cu, cl);
                i++;
            }
//System.err.println("UNICO: 0x" + Integer.toHexString(cu) + ", " + cu);
            sb.insert(bp++, toCp932(cu));
        }
        return sb.toString();
    }
}

/* */
