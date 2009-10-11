/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import vavi.gps.Channel;
import vavi.gps.GpsData;
import vavi.gps.GpsDevice;
import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * HGR Viewer.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030410 nsano initial version <br>
 */
public class HgrViewer extends GpsDevice {

    /** TODO name を何に使うか？ */
    public HgrViewer(String name) {

        try {
            Properties props = new Properties();

            props.load(Hgr.class.getResourceAsStream("Hgr.properties"));

            String value = props.getProperty("hgr.viewer.interval");
            if (value != null) {
                int dummy = Integer.parseInt(value);
Debug.println("interval: " + dummy);
            }
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw new InternalError(e.toString());
        }
    }

    /** */
    private JPanel panel = new JPanel() {
        {
            setPreferredSize(new Dimension(200, 200));
        }
        public void paint(Graphics g) {

            final double R = 100;

            if (gpsData == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;

            RenderingHints qualityHints =
	        new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON);

            qualityHints.put(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);

            g2.setRenderingHints(qualityHints);

            g2.setColor(Color.blue);
            g2.fillRect(0, 0, (int) (R * 2), (int) (R * 2)); 

            g2.setColor(Color.black);
            g2.fillOval(0, 0, (int) (R * 2), (int) (R * 2));

            //----

            Iterator<Channel> i = gpsData.getChannels().iterator();
            while (i.hasNext()) {
                Channel channel = i.next();
                double r = 2 * R * Math.sin((90 - channel.getElevation()) / 180d);
                Point2D p = trueNorthToPoint2D(channel.getAzimuth(), r);
                double X = R + p.getX();
                double Y = R - p.getY();
                switch (channel.getInfo()) {
                case Channel.INFO_OK:
                    g2.setColor(Color.cyan);
                    g2.fillOval((int) (X - 2), (int) (Y - 2), 4, 4);
                    g2.setColor(Color.white);
                    g2.fillOval((int) (X - 1), (int) (Y - 1), 2, 2);
                    break;
                case Channel.INFO_READY:
                    g2.setColor(Color.green);
                    g2.fillOval((int) (X - 2), (int) (Y - 2), 4, 4);
                    break;
                case Channel.INFO_HOLD:
                    g2.setColor(Color.yellow);
                    g2.fillOval((int) (X - 2), (int) (Y - 2), 4, 4);
                    break;
                case Channel.INFO_LOCK:
                    g2.setColor(Color.magenta);
                    g2.fillOval((int) (X - 2), (int) (Y - 2), 4, 4);
                    break;
                case Channel.INFO_ILL:
                    g2.setColor(Color.red);
                    g2.fillOval((int) (X - 2), (int) (Y - 2), 4, 4);
                    break;
                case Channel.INFO_SCAN:
                default:
                    g2.setColor(Color.gray);
                    g2.fillOval((int) (X - 2), (int) (Y - 2), 4, 4);
                    break;
                }
            }

            //----

            double r = (gpsData.getVector().getVelocity() + 40) / 220d * R;

            Point2D p = trueNorthToPoint2D(
                gpsData.getVector().getBearingDirection(), r);

    	    // Java 座標への変換
    	    double X = R + p.getX();
    	    double Y = R - p.getY();

            if (gpsData.ready()) {
                if (gpsData.getMeasurementMode() == GpsData.MODE_3D) {
                    g2.setColor(Color.red);
                } else {
                    g2.setColor(Color.yellow);
                }
            } else {
                g2.setColor(Color.gray);
            }
            g2.drawLine((int) R, (int) R, (int) X, (int) Y);

            //----

            g2.setColor(Color.green);
            g2.drawString("Dir: " +
                          gpsData.getVector().getBearingDirection() + "°",
                          60, 130);
            g2.drawString("Velocity: " +
                          gpsData.getVector().getVelocity() + " km/h",
                          60, 150);
            g2.drawString("Altitude: " +
                          gpsData.getPoint().getAltitude() + " m",
                          60, 170);
        }
    };

    /**
     * @param	trueNorth	degrees
     * @param	r		半径
     */
    private Point2D trueNorthToPoint2D(double trueNorth, double r) {

        // TODO theta の座標は正しくない
        double theta = trueNorth / 360d * (Math.PI * 2) - Math.PI / 2;
        // X, Y は正規座標
        double X = Math.sqrt(
            (r*r * r*r) / (r*r + r*r * Math.pow(Math.tan(theta), 2)));
        double Y = Math.sqrt(
            (r*r * r*r) / (r*r / Math.pow(Math.tan(theta), 2) + r*r));

        // TODO 正しくない theta の座標に対しての補正
        if (theta > Math.PI / 2 && theta < Math.PI * 1.5) X *= -1;
        if (theta < Math.PI     && theta > 0)             Y *= -1;

        return new Point2D.Double(X, Y);
    }

    /** Does nothing. */
    public void start() {
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /** @throws IllegalStateException always be thrown */
    protected Runnable getInputThread() {
        throw new IllegalStateException("This class cannot be input device.");
    }

    /** */
    private GpsData gpsData;

    /** */
    protected GenericListener getOutputGenericListener() {

        return new GenericListener() {
            public void eventHappened(GenericEvent ev) {
                try {
                    gpsData = (GpsData) ev.getArguments()[0];
                    panel.repaint();
                } catch (Exception e) {
Debug.printStackTrace(e);
                }
            }
        };
    }
}

/* */
