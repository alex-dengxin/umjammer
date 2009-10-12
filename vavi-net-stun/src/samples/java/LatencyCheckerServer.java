/*
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * @author suno
 * Created on 2003/07/25
 */
public class LatencyCheckerServer extends Thread {
    /** */
    private DatagramSocket socket;

    /** */
    private DatagramPacket packet;

    /** */
    public LatencyCheckerServer() throws Exception {
        socket = new DatagramSocket(20001);
        byte[] someBuffer = new byte[1];
        packet = new DatagramPacket(someBuffer, someBuffer.length);
    }

    /** */
    public void run() {
        while (true) {
            try {
                socket.receive(packet);
                System.out.println("reply");
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** */
    public static void main(String[] args) throws Exception {
        new LatencyCheckerServer().start();
    }
}

/* */
