
package vavi.apps.docopin.jmf;

/**
 * ストリーミング送信クラス
 * 
 * $Id: TransmitControl.java,v 1.11 2003/01/27 19:05:43 matsu Exp $
 */
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;


public class TransmitControl implements MediaController {

    private static String url = "";

    private MediaLocator mediaLocator = null;

    private DataSink dataSink = null;

    private Processor mediaProcessor = null;

    private static Format[] formats;

    private ContentDescriptor descriptor = new ContentDescriptor(ContentDescriptor.RAW_RTP);

    /**
     * Creates a new transmitter frame.
     * 
     * ex: rtp://192.168.0.102:45000/video
     */

    public TransmitControl(String _url) {

        err("TransmitControl()");
        url = _url;
        setLocator(url);
    }

    /** set locator from Capture Device */
    public void setLocator(String url) {

        err("setLocator()");

        try {
            err("1");
            MediaLocator deviceLocator = searchCaptureDevice();
            err("2");
            DataSource source = Manager.createDataSource(deviceLocator);
            err("3");
            mediaLocator = new MediaLocator(url);
            err("4");
            mediaProcessor = Manager.createRealizedProcessor(new ProcessorModel(source, formats, descriptor));
            err("5");
            // suno/System.out.println("setting datasink");
            dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), mediaLocator);
            err("6");

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /** Search Capture Device for Video */
    public static MediaLocator searchCaptureDevice() {

        StringTokenizer st = new StringTokenizer(url, "/");
        String media_type = "";
        /*
         * for(int i=0; i<st.countTokens()+1; i++){ System.out.println("TOKEN:"+i); media_type = st.nextToken();
         * System.out.println("MEDIATYPE:"+media_type); }
         */

        while (st.hasMoreTokens()) {
            String t_data = st.nextToken();
            if (t_data.equals("audio")) {
                formats = new Format[] {
                    new AudioFormat(AudioFormat.LINEAR)
                };
                media_type = "audio";
            } else if (t_data.equals("video")) {
                formats = new Format[] {
                    new VideoFormat(VideoFormat.JPEG_RTP)
                };
                // formats = new Format[] { new VideoFormat(VideoFormat.MPEG_RTP)};
                // formats = new Format[] { new VideoFormat(VideoFormat.JPEG)};
                media_type = "video";
            }
        }

        // rtp://hostname:port/type

        Vector<?> list = new Vector<Object>();

        if (media_type.equals("video")) {
            list = CaptureDeviceManager.getDeviceList(new VideoFormat(VideoFormat.YUV));
        } else if (media_type.equals("audio")) {
            list = CaptureDeviceManager.getDeviceList(new AudioFormat(AudioFormat.LINEAR));
        }
        if (list.size() > 0) {
            System.out.println("DEVICE NUM======" + list.size());
            CaptureDeviceInfo info = (CaptureDeviceInfo) list.elementAt(0);
            return info.getLocator();
        }
        System.err.println("No Capture Device");
        return null;
    }

    /** Starts transmitting the media. */
    public void startPlayer() {
        err("startPlayer()");
        try {
            err("s1");
            mediaProcessor.start();
            err("s2");
            dataSink.open();
            err("s3");
            dataSink.start();
            err("s4");
        } catch (IOException ioe) {
            err("e1");
            ioe.printStackTrace();
        }
    }

    /** Stop transmitting the media. */
    public void stopPlayer() {

        try {
            mediaProcessor.stop();
            dataSink.close();
            dataSink.stop();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void err(String s) {
        // System.out.println(s);
    }
}
