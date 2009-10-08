/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * utils for debugging
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 30.11.00 first draw oli4 <br>
 *          20.01.01 add debug levels oli4 <br>
 *          23.01.01 add timestamp oli4 <br>
 *          10.03.01 print level too oli4 <br>
 *          24.03.01 print top legend oli4 <br>
 *          13.06.02 exception tracing integrated oli4 <br>
 *          14.06.02 optional trace-file introduced oli4 <br>
 */
public class Debug {
    private static int verboseLevel = 0;

    private static int lineCounter = 0;

    private static boolean timeStampEnable = false;

    private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.GERMAN);

    /**
     * set the verbose-level: the higher the level, the verbose the system is... level 0 means no print!
     */
    public static void setVerboseLevel(int l) {
        verboseLevel = l;
    }

    private static File outputFile = null;

    private static FileOutputStream fOutputStream;

    private static PrintStream pOutputStream;

    /**
     * defines the filename to which the the text is written.
     * if null, the text is printed into the shell where LAoE was started.
     */
    public static void setOutputFile(String fileName) {
        if (fileName != null) {
            try {
                outputFile = new File(fileName);

                // size-control...
                if (outputFile.length() > 10e6) {
                    outputFile.delete();
                }
                outputFile.createNewFile();

                // redirect standardstreams to this file...
                fOutputStream = new FileOutputStream(outputFile, true);
                pOutputStream = new PrintStream(fOutputStream);
                System.setOut(pOutputStream);
                System.setErr(pOutputStream);
            } catch (Exception e) {
            }
        }

    }

    /**
     * set the timestamp at beginning of each line enabled/disabled.
     */
    public static void setTimeStampEnabled(boolean b) {
        timeStampEnable = b;
    }

    /**
     * level-depending println
     */
    public static void println(int level, String s) {
        // verbose level ?
        if (verboseLevel != 0) {
            // legend ?
            if (lineCounter++ == 0) {
                System.out.println("debug logging:");

                // timestamp enable ?
                if (timeStampEnable) {
                    System.out.println("date");
                    System.out.println("|          time");
                    System.out.println("|          |         level");
                    System.out.println("|          |         |  text");
                    System.out.println("|          |         |  |");
                } else {
                    System.out.println("level");
                    System.out.println("| text");
                    System.out.println("|  |");
                }
            }

            // verbose level ?
            if (level <= verboseLevel) {
                // timestamp enable ?
                if (timeStampEnable) {
                    System.out.print(dateFormat.format(new Date()) + "  ");
                }
                System.out.print("" + level + "  ");
                System.out.println(s);
            }
        }
    }

    /**
     * level-depending printStackTrace
     */
    public static void printStackTrace(int level, Exception e) {
        // verbose level ?
        if (verboseLevel != 0) {
            // verbose level ?
            if (level <= verboseLevel) {
                // timestamp enable ?
                if (timeStampEnable) {
                    System.out.print(dateFormat.format(new Date()) + "  ");
                }
                System.out.println("" + level + " exception thrown below:");
                e.printStackTrace();
            }
        }
    }

//    public static void main(String arg[]) {
//        Debug.setVerboseLevel(9);
//        Debug.setTimeStampEnabled(false);
//        Debug.println(1, "gaga");
//    }
}
