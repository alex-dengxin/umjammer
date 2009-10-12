/*
 */

import javax.swing.JFrame;

import vavi.apps.docopin.jmf.TransmitControl;



/**
 * @author suno
 * Created on 2003/07/19
 */
public class SampleVideoSender extends JFrame {
    /** */
    private TransmitControl control;

    /** */
    private final String MEDIATYPE = "VIDEO";

    /** */
    public SampleVideoSender(String address, int port) {
        String url = "rtp://" + address + ":" + port + "/" + MEDIATYPE + "/3";
        this.control = new TransmitControl(url);
    }

    /** */
    public static void main(String[] args) {
        String destinationAddress;
        int destinationPort;
        try {
            destinationAddress = "133.27.170.62";
            destinationPort = 40000;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        new SampleVideoSender(destinationAddress, destinationPort).setVisible(true);
    }
}

/* */
