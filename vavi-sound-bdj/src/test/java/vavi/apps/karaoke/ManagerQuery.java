/*
 * Java Media APIs: Cross-Platform Imaging, Media and Visualization
 * Alejandro Terrazas
 * Sams, Published November 2002, 
 * ISBN 0672320940
 */

package vavi.apps.karaoke;

import java.util.Vector;

import javax.media.Manager;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;


/**
 * ManagerQuery - Query the manager class about the configuration and support of
 * the installed JMF version. ManagerQuery is a text-based application that
 * provides a report on the support of the JMF for Players, Processors and
 * DataSinks.
 * 
 * Without any command-line arguments ManagerQuery prints a complete (LONG) list
 * of Player, Processor, and DataSource classes that support the various
 * formats, protocols, and content types.
 * 
 * Alternatively it is possible to provide command-line arguments specifying the
 * format or protocol for which support is to be checked. The means of calling
 * is as follows: java ManagerQuery [ [-h|-p|-d] support1 support2 ... supportN]
 * The -h flag specifies handlers (Players) only. The -p flag specifies
 * Processors only. The -d flag specifies DataSources only. Leaving off the flag
 * defaults behaviour to checking for Players only.
 * 
 * For instance: java ManagerQuery -h mp3 ulaw would list the classes capable of
 * Playing the MP3 (MPEG, Layer 3) and U-Law formats (codecs).
 * 
 * ManagerQuery always prints the version of JMF, caching directory, and hints
 * prior to any other output.
 * 
 * @author Spike Barlow
 */
public class ManagerQuery {
    // 
    // Constants to facilitate selection of the
    // approprite get*List() method.
    // 
    public static final int HANDLERS = 1;

    public static final int PROCESSORS = 2;

    public static final int DATASOURCES = 3;

    // 
    // Array containing all the content types that JMF2.1.1
    // supports. This is used when the user provides no
    // command-line arguments in order to generate a
    // complete list of support for all the content types.
    // 
    private static final String[] CONTENTS = {
        ContentDescriptor.CONTENT_UNKNOWN, FileTypeDescriptor.AIFF, FileTypeDescriptor.BASIC_AUDIO, FileTypeDescriptor.GSM, FileTypeDescriptor.MIDI, FileTypeDescriptor.MPEG, FileTypeDescriptor.MPEG_AUDIO, FileTypeDescriptor.MSVIDEO, FileTypeDescriptor.QUICKTIME, FileTypeDescriptor.RMF, FileTypeDescriptor.VIVO, FileTypeDescriptor.WAVE, VideoFormat.CINEPAK, VideoFormat.H261, VideoFormat.H263, VideoFormat.H261_RTP,
        VideoFormat.H263_RTP, VideoFormat.INDEO32, VideoFormat.INDEO41, VideoFormat.INDEO50, VideoFormat.IRGB, VideoFormat.JPEG, VideoFormat.JPEG_RTP, VideoFormat.MJPEGA, VideoFormat.MJPEGB, VideoFormat.MJPG, VideoFormat.MPEG_RTP, VideoFormat.RGB, VideoFormat.RLE, VideoFormat.SMC, VideoFormat.YUV, AudioFormat.ALAW, AudioFormat.DOLBYAC3, AudioFormat.DVI, AudioFormat.DVI_RTP, AudioFormat.G723, AudioFormat.G723_RTP, AudioFormat.G728, AudioFormat.G728_RTP, AudioFormat.G729, AudioFormat.G729_RTP,
        AudioFormat.G729A, AudioFormat.G729A_RTP, AudioFormat.GSM, AudioFormat.GSM_MS, AudioFormat.GSM_RTP, AudioFormat.IMA4, AudioFormat.IMA4_MS, AudioFormat.LINEAR, AudioFormat.MAC3, AudioFormat.MAC6, AudioFormat.MPEG, AudioFormat.MPEG_RTP, AudioFormat.MPEGLAYER3, AudioFormat.MSADPCM, AudioFormat.MSNAUDIO, AudioFormat.MSRT24, AudioFormat.TRUESPEECH, AudioFormat.ULAW, AudioFormat.ULAW_RTP, AudioFormat.VOXWAREAC10, AudioFormat.VOXWAREAC16, AudioFormat.VOXWAREAC20, AudioFormat.VOXWAREAC8,
        AudioFormat.VOXWAREMETASOUND, AudioFormat.VOXWAREMETAVOICE, AudioFormat.VOXWARERT29H, AudioFormat.VOXWARETQ40, AudioFormat.VOXWARETQ60, AudioFormat.VOXWAREVR12, AudioFormat.VOXWAREVR18
    };

    // 
    // The protocols that JMF supports.
    // 
    private static final String[] PROTOCOLS = {
        "ftp", "file", "rtp", "http"
    };

    /**
     * Return a String being a list of all hints settings.
     */
    public static String getHints() {

        return "";
    }

    /**
     * Produce a list of all classes that support the content types or protocols
     * passed to the method. The list is returned as a formatted String, while
     * the 2nd parameter (which) specifies whether it is Player (Handler),
     * Processor, or DataSource classes.
     */
    public static String getHandlersOrProcessors(String[] contents, int which) {
        String str = "";
        Vector classes;
//        int NUM_PER_LINE = 2;
        String LEADING = "\t    ";
        String SEPARATOR = "  ";

        if (contents == null)
            return null;

        // 
        // Generate a separate list for each content-type/protocol specified.
        // 
        for (int i = 0; i < contents.length; i++) {
            str = str + "\t" + contents[i] + ":\n";
            if (which == HANDLERS) {
                classes = Manager.getHandlerClassList(contents[i]);
//          } else if (which == PROCESSORS) {
//              classes = Manager.getProcessorClassList(contents[i]);
            } else {
                classes = Manager.getDataSourceList(contents[i]);
            }
            if (classes == null) {
                str = str + "\t    <None>\n";
            } else {
                str = str + formatVectorStrings(classes, LEADING, 2, SEPARATOR);
            }
        }
        return str;
    }

    /**
     * Get a list of all Handler (Player) classes that support each of the
     * formats (content types).
     */
    public static String getHandlers() {
        return getHandlersOrProcessors(CONTENTS, HANDLERS);
    }

    /**
     * Get a list of all Processor classes that support each of the formats
     * (content types).
     */
    public static String getProcessors() {
        return getHandlersOrProcessors(CONTENTS, PROCESSORS);
    }

    /**
     * Get a list of all DataSources classes that support each of the protocols.
     */
    public static String getDataSources() {
        return getHandlersOrProcessors(PROTOCOLS, DATASOURCES);
    }

    /**
     * Format the Vector of Strings returned by the get*List() methods into a
     * single String. A simple formatting method.
     */
    public static String formatVectorStrings(Vector vec, String leading, int count, String separator) {
        String str = leading;

        for (int i = 0; i < vec.size(); i++) {
            boolean ok;
if (i % 2 == 1) {
String c = (String) vec.elementAt(i);
try {
 Class.forName(c);
 ok = true;
 System.err.println("class " + c + ": " + "OK");
} catch (Exception e) {
 System.err.println("class " + c + ": " + "NG");
}
} else {
 ok = false;
}
            str = str + (String) vec.elementAt(i);
            if ((i + 1) == vec.size()) {
                str = str + "\n";
            } else if ((i + 1) % count == 0) {
                str = str + "\n" + leading;
            } else {
                str = str + separator;
            }
        }
        return str;
    }

    /**
     * Produce a list showing total support (i.e., Player, Processors, and
     * DataSinks) for all content types and protocols.
     */
    public static void printTotalList() {
        System.out.println("\nPlayer Handler Classes:");
        System.out.println(getHandlers());
        System.out.println("\nProcessor Class List:");
        System.out.println(getProcessors());
        System.out.println("\nDataSink Class List: ");
        System.out.println(getDataSources());
    }

    /**
     * Main method. Produce a version and hints report. Then if no command line
     * arguments produce a total class list report. Otherwise process the
     * command line arguments and produce a report on their basis only.
     */
    public static void main(String args[]) {

//        System.out.println("JMF: " + Manager.getVersion());
//        String cacheArea = Manager.getCacheDirectory();
//        if (cacheArea == null)
//            System.out.println("No cache directory specified.");
//        else
//            System.out.println("Cache Directory: " + cacheArea);
        System.out.println("Hints:");
        System.out.println(getHints());

        // No command-line arguments. Make a toral report.
        if (args == null || args.length == 0)
            printTotalList();
        else {
            // Command-line. Process flags and then support to be
            // queried upon in order to generate appropriate report.
            String header = "";
            int whichCategory = 0;
            String[] interested;
            int i;
            int start;
            if (args[0].equalsIgnoreCase("-h")) {
                header = "\nPlayer Handler Classes: ";
                whichCategory = HANDLERS;
//            } else if (args[0].equalsIgnoreCase("-p")) {
//                header = "\nProcessor Class List:";
//                whichCategory = PROCESSORS;
            } else if (args[0].equalsIgnoreCase("-d")) {
                header = "\nDataSink Class List: ";
                whichCategory = DATASOURCES;
            }
            if (whichCategory == 0) {
                whichCategory = HANDLERS;
                header = "\nPlayer Handler Classes: ";
                interested = new String[args.length];
                start = 0;
            } else {
                interested = new String[args.length - 1];
                start = 1;
            }
            for (i = start; i < args.length; i++)
                interested[i - start] = args[i];
            System.out.println(header);
            System.out.println(getHandlersOrProcessors(interested, whichCategory));
        }
    }
}
