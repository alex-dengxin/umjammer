/*
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import vavi.net.www.protocol.p2p.P2PConnection;


/**
 * @author suno
 * Created on 2003/07/20
 */
public class NetworkWhiteBoard extends JFrame {

    private OutputStream outputStream;
    private InputStream inputStream;

    private Image boardImage;

    public static final int BOARDHEIGHT = 400;

    public static final int BOARDWIDTH = 400;

    private int prevX = -1;
    private int prevY = -1;

    public NetworkWhiteBoard(P2PConnection connection) throws IOException {
        this.outputStream = connection.getOutputStream();
        this.inputStream = connection.getInputStream();
        this.setBounds(10, 10, BOARDWIDTH, BOARDHEIGHT);
        this.setResizable(false);
        this.setTitle("Network White Board");

        this.addMouseMotionListener(mouseInputListener);
        this.addMouseListener(mouseInputListener);
        new Thread() {
            public void run() {
                byte[] data = new byte[8];
                int x1, x2, y1, y2;
                while (boardImage == null) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                while (true) {
                    try {
                        int r = inputStream.read(data, 0, 8);
                        if (r == 8) {
                            x1 = ((data[0] & 0xff) << 8) | (data[1] & 0xff) + 2;
                            y1 = ((data[2] & 0xff) << 8) | (data[3] & 0xff) + 2;
                            x2 = ((data[4] & 0xff) << 8) | (data[5] & 0xff) + 2;
                            y2 = ((data[6] & 0xff) << 8) | (data[7] & 0xff) + 2;
                            NetworkWhiteBoard.this.drawLine(x1, y1, x2, y2, Color.RED);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        repaint();
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private synchronized void drawLine(int x1, int y1, int x2, int y2, Color color) {
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1) {
            System.out.println("error!!");
        }
        Graphics graphics = boardImage.getGraphics();
        graphics.setColor(color);
        graphics.drawLine(x1, y1, x2, y2);
    }

    private MouseInputListener mouseInputListener = new MouseInputAdapter() { 
        public void mouseDragged(MouseEvent event) {
            if (boardImage.getGraphics() != null) {
                if (prevX >= 0 && prevY >= 0) {
    
                    drawLine(prevX, prevY, event.getX(), event.getY(), Color.BLACK);
                    // .fillOval(evt.getX(), evt.getY(), 2, 2);
                    repaint();
                    byte[] someBuffer = new byte[] {
                        (byte) (0xff & (prevX >> 8)), (byte) (0xff & (prevX)), (byte) (0xff & (prevY >> 8)), (byte) (0xff & (prevY)), (byte) (0xff & (event.getX() >> 8)), (byte) (0xff & (event.getX())), (byte) (0xff & (event.getY() >> 8)), (byte) (0xff & (event.getY())),
                    };
                    try {
                        outputStream.write(someBuffer);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            prevX = event.getX();
            prevY = event.getY();
        }
    
        public void mouseReleased(MouseEvent e) {
            prevX = -1;
            prevY = -1;
        }

        public void mouseExited(MouseEvent event) {
            prevX = -1;
            prevY = -1;
        }
    };

    public void doLayout() {
        if (boardImage == null) {
            boardImage = createImage(BOARDWIDTH, BOARDHEIGHT);
        }
    }

    public void update(Graphics g) {
        redraw(g);
    }

    public void paint(Graphics g) {
        redraw(g);
    }

    public synchronized void redraw(Graphics g) {
        if (boardImage.getGraphics() != null) {
            boardImage.getGraphics().setColor(Color.BLACK);
            g.drawImage(this.boardImage, 0, 0, null);
        }
    }
}

/* */
