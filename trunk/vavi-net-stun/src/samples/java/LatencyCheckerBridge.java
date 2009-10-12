/*
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * @author suno
 * Created on 2003/07/25
 */
public class LatencyCheckerBridge extends Thread {
    DatagramSocket socket1;
    DatagramSocket socket2;

    DatagramPacket receivePacket1;
    DatagramPacket receivePacket2;
    DatagramPacket sendPacket1;
    DatagramPacket sendPacket2;

    public LatencyCheckerBridge() throws Exception {
        socket1 = new DatagramSocket(20001);
        socket2 = new DatagramSocket(20002);
        byte[] b1 = new byte[1], b2 = new byte[1], b3 = new byte[1];
        receivePacket1 = new DatagramPacket(b1, b1.length);
        receivePacket2 = new DatagramPacket(b2, b2.length);
        sendPacket1 = new DatagramPacket(b3, b3.length, InetAddress.getByName("133.27.171.117"), 20000);
        sendPacket2 = new DatagramPacket(b3, b3.length, InetAddress.getByName("enterprise.newrong.com"), 20001);
//        byte[] b4 = new byte[1]
//        sendPacket2 = new DatagramPacket(b4, b4.length, InetAddress.getByName("gokugoku.com"), 20001);
//        sendPacket2 = new DatagramPacket(b4, b4.length, InetAddress.getByName("sunouchi.ddo.jp"), 20001);
    }

    public void run() {
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        socket1.receive(receivePacket1);
                        sendPacket2.getData()[0] = receivePacket1.getData()[0];
                        socket2.send(sendPacket2);
                        System.out.println("1 to 2" + receivePacket1.getData()[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread() {
            public void run() {
                for (;;) {
                    try {
                        socket2.receive(receivePacket2);
                        sendPacket2.setPort(receivePacket2.getPort());
                        sendPacket1.getData()[0] = receivePacket2.getData()[0];
                        socket1.send(sendPacket1);
                        System.out.println("2 to 1" + receivePacket2.getData()[0]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /** */
    public static void main(String[] args) throws Exception {
        new LatencyCheckerBridge().start();
    }
}
