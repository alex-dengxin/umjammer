
// Import necessary packages

import java.io.IOException;
import java.net.InetAddress;

import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;


/**
    RTP Server, version 3.0
    Michael Smith
    implements synchronization -- needed to ensure that the configured and 
    realized states get done properly
    
    ---------------------------------------------------------------
    Steps to create an RTP Session through use of a SessionManager
    As outlined in the JMF 2.0 API
    ---------------------------------------------------------------
    
    1.  Create a JMF Processor and set each track format to an RTP
      specific format.
    2.  Retrieve the output DataSource from the Processor
    3.  Call createSendStream on a previously created and initialized
      SessionManager, passing in the DataSource and a stream index.
      The SessionManager creates a SendStream for the specified 
      SourceStream.
    4.  Start the session manager by calling SessionManager startSession
    5.  Control the transmission the the SendStream methods.  A 
      SendStreamListener can be registered to listen to events on SendStream
    ---------------------------------------------------------------
    
    Referenced code from Java JMF API, JMF Solutions and JMF Guide
 */
public class RTPServer3 {

    MediaLocator medialocator;

    String ipAddress;

    int port;

    Processor processor = null;

    RTPManager rtpManager[];

    DataSource dataSource = null;

    // Constructor for RTPServer
    public RTPServer3(MediaLocator locator, String ipAddress, String portNumber, Format audioFormat) {
        // Assign the values taken in from the constructor to
        // those defined in the main class

        this.medialocator = locator;
        this.ipAddress = ipAddress;
        Integer portValue = Integer.valueOf(portNumber);
        // If port can be converted from string to integer
        // assign it to be the port number

        if (portValue != null)
            this.port = portValue.intValue();

        // Create a processor for this output
        instantiate();

    }

    public synchronized void instantiate() {
        boolean processorOK = false;
        boolean configureOK = false;

        processorOK = createProcessor();
        System.out.println("processorOK = " + processorOK);
        if (processorOK) {
            configureOK = createSend();
        }
        System.out.println("configureOK = " + configureOK);
        if (configureOK) {
            processor.start();
            System.out.println("Processor starting...");
        }
    }

    // Method to create a Processor and do error checking
    public synchronized boolean createProcessor() {
        // DataSource source ;

        // create the datasource
        try {
            dataSource = javax.media.Manager.createDataSource(medialocator);
            System.out.println("DataSource created from file");
        } catch (Exception e) {
            System.out.println("Error: Couldn't create Datasource");
            System.exit(-1);
        }

        // Create the processor to process the DataSource
        try {
            processor = javax.media.Manager.createProcessor(dataSource);
            System.out.println("Processor created");
        } catch (NoProcessorException p) {
            System.out.println("Error: Couldn't create processor");
            System.exit(-1);
        } catch (IOException i) {
            System.out.println("Error: Error reading file");
            // System.out.println( i.printStack() ) ;
            System.exit(-1);
        }

        // Run Processor methods to configure Processor
        processor.addControllerListener(new StateListener());

        int state = Processor.Configured;
        System.out.println("value of state: " + state + "\nvalue of getState: " + Processor.Configured);
        if (state == Processor.Configured) {
            processor.configure();
            System.out.println("Processor Configured");
        } else {
            System.out.println("Error: Could not Configure Processor");
            System.exit(-1);
        }

        // Now that it has been configured, program the tracks
        if (processor.getState() == Processor.Configured) {
            System.out.println("Getting Tracks");
        } else {
            try {
                System.out.println("setting datasource");
                processor.setSource(dataSource);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            processor.configure();

        }

        TrackControl[] track = processor.getTrackControls();

        boolean encodingOK = false;

        // Check to see if the datasource had tracks
        if ((track == null) || (track.length < 1)) {
            System.out.println("Error: No tracks in Source");
            System.exit(-1);
        }

        // Set the content type of the output data to RTP data
        ContentDescriptor content = new ContentDescriptor(ContentDescriptor.RAW_RTP);
        processor.setContentDescriptor(content);

        Format supportedFormats[];

        for (int i = 0; i < track.length; i++) {
            // Set format to the format of TrackControl
            // i.e. ContentDescriptor
//            Format format = track[i].getFormat();
            if (track[i].isEnabled()) {
                // Find formats that support RAW_RTP
                supportedFormats = track[i].getSupportedFormats();
                if (supportedFormats.length > 0) {
                    // Encode the track with MPEG_RTP format
                    if (track[i].setFormat(supportedFormats[i]) == null) {
                        track[i].setEnabled(false);
                        encodingOK = false;
                    } else {
                        encodingOK = true;
                    }
                }
            }
        }

        // Now, if encoding occured correctly, the processor is programmed and ready to be realized
        int realized = Controller.Realized;
        if (encodingOK) {
            if (realized == Processor.Realized) {
                processor.realize();
                System.out.println("Processor Realized");
            } else {
                System.out.println("Error: Processor could not be realized");
                System.exit(-1);
            }
        } else {
            System.out.println("Error: Encoding to MPEG format failed");
            System.exit(-1);
        }

        // Final Steps of creating Processor:
        // Link the datasource to the Processor
        if (realized == Processor.Realized) {
            System.out.println("Going to get DataSource now");
        } else {
            processor.realize();

        }
        dataSource = processor.getDataOutput();

        return true;
    }

    // Use the RTPManager to pass the application data to the lower network layer
    public synchronized boolean createSend() {
        // Make a buffered Push Source from our data source
        PushBufferDataSource bufferDatasource = (PushBufferDataSource) dataSource;
        // Make a buffered stream for each track in the processor
        PushBufferStream bufferStream[] = bufferDatasource.getStreams();

        // Make an RTPManager for each stream
        rtpManager = new RTPManager[bufferStream.length];
        // Create SessionAddresses to locate the client and server; port as well
        SessionAddress serverAddress, clientAddress;
        InetAddress ipAddr;
        int portNumber;
        // Create a SendStream that will carry the output
        SendStream outputStream;
        // Create the needed SDES list to identify sources
//        SourceDescription SDESList[];

        // For every track, make an instance in the RTPManager
        for (int i = 0; i < bufferStream.length; i++) {
            try {
                rtpManager[i] = RTPManager.newInstance();

                // make the port, since each instance will have two ports (RTP and RTCP)
                // increment the portnumber by 2 for each instance
                portNumber = port + 2 * i;

                // Get IP of source and destination to associate with send stream
                ipAddr = InetAddress.getByName(ipAddress);
                serverAddress = new SessionAddress(InetAddress.getLocalHost(), portNumber);
                clientAddress = new SessionAddress(ipAddr, portNumber);

                // Now Initialze the Manager and then add the target to the RTP Manager
                rtpManager[i].initialize(serverAddress);
                rtpManager[i].addTarget(clientAddress);
                System.out.println("RTP Session #" + i + " created: " + ipAddress + " " + portNumber);

                // Create the sendStreams and link them to the targets just added to the RTPManager
                outputStream = rtpManager[i].createSendStream(dataSource, i);
                outputStream.start();
            } catch (Exception e) {
                System.out.println("Error: SendStream could not be created");
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
        return true;

    }

    class StateListener implements ControllerListener {
        public void controllerUpdate(ControllerEvent c) {
            if (c instanceof ControllerClosedEvent) {
                if (processor != null) {
                    processor.stop();
                    processor.close();
                    processor = null;
                    for (int i = 0; i < rtpManager.length; i++) {
                        rtpManager[i].removeTargets("Sessions are done");
                        rtpManager[i].dispose();
                    }
                }
            }
            if (c instanceof EndOfMediaEvent) {
                processor.stop();
                processor.close();
                processor = null;
                for (int i = 0; i < rtpManager.length; i++) {
                    rtpManager[i].removeTargets("Sessions are done");
                    rtpManager[i].dispose();
                }

                System.out.println("End of Media Stream");
            }
        }
    }

    // Main class, which instantiates the Server
    public static void main(String[] args) {
        // Path for the file to stream
        // change for different files for now
        String file = "file:test2.wav";
        String port = "45000";
        Format format = null;

        // Session Address, supposedly supposed to be
        // the IP of the receiving client
        // String sessionAddress = "128.255.135.74" ;
        String sessionAddress = "133.27.170.62";

        new RTPServer3(new MediaLocator(file), sessionAddress, port, format);

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }
    }
}
