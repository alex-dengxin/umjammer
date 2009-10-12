import java.awt.Dimension;
import java.io.IOException;

import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.Control;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaException;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.Time;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;


/**
    VideoTransmit.java
    
    Lab 5 -- csc561 - Multimedia Systems course
    
    basic RTP media server that will stream video
    from local files or remote files to a remote
    RTP client player...
    use with VideoTX.java (the UI)
    
    written by: Tim Ducharme
    Spring 2003
 */
public class VideoTransmit implements ControllerListener {

    // Input MediaLocator
    // Can be a file or http or capture source
    private MediaLocator locator;

    private String ipAddress;

    private String port;

    private Processor processor = null;

    private DataSink rtptransmitter = null;

    private DataSource dataOutput = null;

    public VideoTransmit(MediaLocator locator, String ipAddress, String port) {
        this.locator = locator;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Starts the transmission. Returns null if transmission started ok.
     * Otherwise it returns a string with the reason why the setup failed.
     */
    public synchronized void start() {
        // Create a processor for the specified media locator
        // and program it to output JPEG/RTP
        if (locator == null)
            Fatal("Locator is null");

        DataSource ds = null;
        try {
            ds = Manager.createDataSource(locator);
        } catch (Exception e) {
            Fatal("Couldn't create DataSource");
        }

        // Try to create a processor to handle the input media locator
        try {
            processor = Manager.createProcessor(ds);
        } catch (NoProcessorException npe) {
            Fatal("Couldn't create processor");
        } catch (IOException ioe) {
            Fatal("IOException creating processor");
        }
        try {
            processor.addControllerListener(this);
            // Wait for the processor to configure
            processor.configure();
        } catch (Exception npe) {
            Fatal("Couldn't configure processor");
        }
    }

    /**
     * Stops the transmission if already started
     */
    public void stop() {
        synchronized (this) {
            if (processor != null) {
                processor.stop();
                processor.close();
                processor = null;
            }
            if (rtptransmitter != null) {
                rtptransmitter.close();
                rtptransmitter = null;
            }
        }
    }

    void Fatal(String s) {
        // Applications will make various choices about what
        // to do here. We print a message
        System.err.println("FATAL ERROR: " + s);
        System.exit(-1);
    }

    /**
     * Setting the encoding quality to the specified value on the JPEG encoder.
     * 0.5 is a good default.
     */
    void setJPEGQuality(Player p, float val) {

        Control cs[] = p.getControls();
        QualityControl qc = null;
        VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

        // Loop through the controls to find the Quality control for
        // the JPEG encoder.
        for (int i = 0; i < cs.length; i++) {

            if (cs[i] instanceof QualityControl && cs[i] instanceof Owned) {
                Object owner = ((Owned) cs[i]).getOwner();

                // Check to see if the owner is a Codec.
                // Then check for the output format.
                if (owner instanceof Codec) {
                    Format fmts[] = ((Codec) owner).getSupportedOutputFormats(null);
                    for (int j = 0; j < fmts.length; j++) {
                        if (fmts[j].matches(jpegFmt)) {
                            qc = (QualityControl) cs[i];
                            qc.setQuality(val);
                            System.out.println("- Setting quality to " + val + " on " + qc);
                            break;
                        }
                    }
                }
                if (qc != null)
                    break;
            }
        }
    }

    // /////////// ControllerListener ////////////////////
    public synchronized void controllerUpdate(ControllerEvent event) {
        if (processor == null)
            return;

        System.out.println("ControllerEvent: " + event.toString());
        if (event instanceof ConfigureCompleteEvent) {
            // Get the tracks from the processor
            TrackControl[] tracks = processor.getTrackControls();

            // Do we have atleast one track?
            if (tracks == null || tracks.length < 1)
                Fatal("Couldn't find tracks in processor");

            boolean programmed = false;

            // Search through the tracks for a video track
            for (int i = 0; i < tracks.length; i++) {
                Format format = tracks[i].getFormat();
                if (tracks[i].isEnabled() && format instanceof VideoFormat && !programmed) {

                    // Found a video track. Try to program it to output JPEG/RTP
                    // Make sure the sizes are multiple of 8's.
                    Dimension size = ((VideoFormat) format).getSize();
                    float frameRate = ((VideoFormat) format).getFrameRate();
                    int w = (size.width % 8 == 0 ? size.width : (size.width / 8) * 8);
                    int h = (size.height % 8 == 0 ? size.height : (size.height / 8) * 8);
                    VideoFormat jpegFormat = new VideoFormat(VideoFormat.JPEG_RTP, new Dimension(w, h), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
                    tracks[i].setFormat(jpegFormat);
                    System.out.println("Video will be transmitted as:");
                    System.out.println("  " + jpegFormat);
                    // Assume succesful
                    programmed = true;
                } else
                    tracks[i].setEnabled(false);
            }

            if (!programmed)
                Fatal("Couldn't find video track");

            // Set the output content descriptor to RAW_RTP
            ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
            processor.setContentDescriptor(cd);
            // Realize the processor. This will internally create a flow
            // graph and attempt to create an output datasource for JPEG/RTP
            // video frames.
            processor.realize();

        } else if (event instanceof RealizeCompleteEvent) {
            // Set the JPEG quality to .5.
            setJPEGQuality(processor, 0.5f);
            // Get the output data source of the processor
            dataOutput = processor.getDataOutput();
            // Create a media locator for the RTP data sink.
            // For example:
            // rtp://192.168.1.100:42050/video
            String rtpURL = "rtp://" + ipAddress + ":" + port + "/video";
            MediaLocator outputLocator = new MediaLocator(rtpURL);

            // Create a data sink, open it and start transmission. It will wait
            // for the processor to start sending data. So we need to start the
            // output data source of the processor. We also need to start the
            // processor itself.
            try {
                rtptransmitter = Manager.createDataSink(dataOutput, outputLocator);
                rtptransmitter.open();
                rtptransmitter.start();
                dataOutput.start();
            } catch (MediaException me) {
                Fatal("Couldn't create RTP data sink");
            } catch (IOException ioe) {
                Fatal("Couldn't create RTP data sink");
            }
            processor.prefetch();

        } else if (event instanceof PrefetchCompleteEvent) {
            processor.start();

        } else if (event instanceof EndOfMediaEvent) {
            processor.setMediaTime(new Time(0));
            processor.start();

        } else if (event instanceof ControllerErrorEvent) {
            processor = null;
            Fatal(((ControllerErrorEvent) event).getMessage());

        } else if (event instanceof ControllerClosedEvent) {
        }
    }

    /**
     * Sample Usage for VideoTransmit class
     */
    public static void main(String[] args) {
        // We need three parameters to do the transmission
        // For example,
        // java VideoTransmit file:/C:/media/test.mov 192.168.1.100 22044
        if (args.length < 3) {
            printUsage();
        }

        // Create a video transmit object with the specified params.
        VideoTransmit vt = new VideoTransmit(new MediaLocator(args[0]), args[1], args[2]);
        // Start the transmission
        vt.start();

        System.out.println("Start transmission for 5 minutes...");

        // Transmit for 5 minutes and then close the processor
        // This is a safeguard when using a capture data source
        // so that the capture device will be properly released
        // before quitting.
        try {
            Thread.sleep(300000);
        } catch (InterruptedException ie) {
        }

        // Stop the transmission
        vt.stop();

        System.out.println("...transmission ended.");

        System.exit(0);
    }

    static void printUsage() {
        System.err.println("Usage: VideoTransmit <sourceURL> <destIP> <destPort>");
        System.exit(-1);
    }
}
