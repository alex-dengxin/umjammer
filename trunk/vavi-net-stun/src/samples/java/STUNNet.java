

import java.net.DatagramPacket;
import java.net.DatagramSocket;


/**
 * @author Administrator
 * Created on 2003/06/25
 */
public class STUNNet {
    /** */
    private static DatagramSocket[] sockets = null;

    /** */
    public static void main(String[] args) {
        try {
            sockets = new DatagramSocket[] {
                new DatagramSocket(3000), new DatagramSocket(3001),
            };
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        new Thread() {
            public void run() {
                while (true) {
                    byte[] someBuffer = new byte[256];
                    DatagramPacket somePacket = new DatagramPacket(someBuffer, someBuffer.length);
                    try {
                        sockets[1].receive(somePacket);
                        System.out.println("aaa");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.start();
        byte[] someBuffer = new byte[256];
        DatagramPacket somePacket = new DatagramPacket(someBuffer, someBuffer.length);
        while (true) {
            try {
                System.out.println("---beginloop");
                sockets[0].receive(somePacket);
                System.out.println("received");
                Thread.sleep(100);
                sockets[1].send(somePacket);
                System.out.println("sent");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
