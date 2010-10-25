/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;



/**
 * Class for the Common Functions required by the NESCafe Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class Utils {
    /**
     * Convert an integer value into its binary equivalent.
     * 
     * @param value The decimal value.
     * @return Returns the binary String.
     */
    public static final String dec2bin(int value) {
        // Perform the Conversion
        String retVal = Integer.toBinaryString(value);
        while (retVal.length() < 16)
            retVal = "0" + retVal;
        return retVal;
    }

    /**
     * Convert a full path and filename into just a filename.
     * 
     * @param fileName The full path and filename.
     * @return Returns just the file name.
     */
    public static final String getFileName(String fileName) {
        // Take a Copy of the FileName
        String result = fileName;
        // Take of Directory Separators (leaving Filename)
        while (result.indexOf("\\") >= 0)
            result = result.substring(result.indexOf("\\") + 1);
        while (result.indexOf("/") >= 0)
            result = result.substring(result.indexOf("/") + 1);
        // Check if the FileName is a Temp File creating by the Downloading
        // Engine
        if ((result.indexOf(".tmp") >= 0) & (result.indexOf("nes-") == 0)) {
            // Retrieve Filename by Stripping Temp Information
            result = result.substring(4);
            if (result.indexOf("-") >= 0)
                result = result.substring(0, result.indexOf("-"));
        }
        // Strip the NES Extension
        while (result.toUpperCase().indexOf(".NES") >= 0)
            result = result.substring(0, result.indexOf("."));
        // Return the Actual FileName
        return result;
    }

    /**
     * Convert an integer value into its binary equivalent.
     */
    public static final String binary(int value, int len) {
        String sb = "";
        for (int i = (int) Math.pow(2, len - 1); i >= 1; i >>= 1) {
            if ((i & value) != 0)
                sb = sb + "1";
            else
                sb = sb + "0";
        }
        return sb;
    }

    /**
     * Convert an integer value into its hexadecimal equivalent.
     * 
     * @param value The decimal value.
     * @param len The minimum length of the hexadecimal String.
     * @return Returns the hexadecimal String.
     */
    public static final String hex(int value, int len) {
        // Perform the Conversion
        String str = Integer.toHexString(value).toUpperCase();
        // Make sure it has at least the required length
        while (str.length() < len)
            str = "0" + str;
        // Return the String
        return str;
    }

    /**
     * Convert an hexadecimal value into its decimal equivalent.
     * 
     * @param hexValue The hexadecimal string.
     * @return Returns the decimal value.
     */
    public static final int hex2dec(String hexValue) {
        // Declare the Lookup String
        final String lookup = "0123456789ABCDEF";
        // Start with a Total of 0 and Units of 1
        int total = 0;
        int units = 1;
        // Parse through the String
        while (hexValue.length() > 0) {
            // Take the Right-Most Character
            String curVal = hexValue.substring(hexValue.length() - 1);
            // Find it in the Lookup String
            if (lookup.indexOf(curVal) >= 0) {
                total += units * lookup.indexOf(curVal);
                units *= 16;
            } else {
                return 0;
            }
            // Chop the Right-Most Character Off
            hexValue = hexValue.substring(0, hexValue.length() - 1);
        }
        // Return the Integet Total
        return total;
    }
}
