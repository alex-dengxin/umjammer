/*
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * @author suno
 * Created on 2003/07/24
 */
public class LatencyCheckerClient {
    private InetAddress address;

    private int port;

    private DatagramSocket socket;

    private DatagramPacket packet;

    public final int TRIAL = 100;

    private long[][] time = new long[TRIAL][2];

    public LatencyCheckerClient(InetAddress address, int port) throws Exception {
        this.address = address;
        this.port = port;
        byte[] buffer = new byte[1];
        this.packet = new DatagramPacket(buffer, buffer.length, address, port);
        this.socket = new DatagramSocket(20000);
        this.socket.setSoTimeout(1000);
    }

    public double measure() throws Exception {
        for (int i = 0; i < TRIAL; i++) {
            time[i][0] = -1;
            time[i][1] = -1;
        }
        byte[] someBuffer = new byte[1];

        DatagramPacket _RecvPacket = new DatagramPacket(someBuffer, someBuffer.length);
        for (int i = 0; i < TRIAL; i++) {
            try {
                packet.getData()[0] = (byte) i;
                time[i][0] = System.currentTimeMillis();
                socket.send(packet);
                socket.receive(_RecvPacket);
                time[_RecvPacket.getData()[0]][1] = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int validCount = 0;
        long totalTime = 0;

        for (int i = 0; i < TRIAL; i++) {
            if (time[i][0] != -1 && time[i][1] != -1) {
                totalTime += (time[i][1] - time[i][0]);
                System.out.println((time[i][1] - time[i][0]));
                validCount++;
            } else {
                System.out.println("packet loss");
                System.out.println("count " + i + " average:" + (totalTime / (i - 1)));
                break;
            }
        }
        System.out.println(" average:" + ((time[TRIAL - 1][1] - time[0][0]) / validCount));
        System.out.println(validCount + " packets. average:" + totalTime / validCount + " msec");
        return totalTime / validCount;
    }

    public static void main(String[] args) throws Exception {
        InetAddress address = InetAddress.getByName(args[0]);
        int somePort = Integer.parseInt(args[1]);
        LatencyCheckerClient someClient = new LatencyCheckerClient(address, somePort);
        someClient.measure();
    }
}
