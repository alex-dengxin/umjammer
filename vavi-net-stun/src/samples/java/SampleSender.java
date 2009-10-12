/*
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * @author suno
 * Created on 2003/07/16
 */
public class SampleSender {

    public static void main(String[] args) throws Exception {
        DatagramSocket someSocket = new DatagramSocket();
        byte[] someBuffer = new byte[256];
        DatagramPacket somePacket = new DatagramPacket(someBuffer, someBuffer.length, InetAddress.getByName("133.27.170.35"), 3000);
        DatagramPacket somePacket2 = new DatagramPacket(someBuffer, someBuffer.length, InetAddress.getByName("133.27.170.35"), 3000);
        for (;;) {
            someSocket.send(somePacket);
            System.out.println("sent");
            someSocket.receive(somePacket2);
            System.out.println("received from" + somePacket2.getAddress() + ":" + somePacket2.getPort());
            Thread.sleep(100);
        }
    }
}
