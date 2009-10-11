/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import vavi.gps.Channel;
import vavi.gps.GpsData;
import vavi.gps.MapVector;
import vavi.gps.PointMap3D;
import vavi.gps.PointSurface;
import vavi.gps.vendor.sony.IpsGpsData;
import vavi.gps.vendor.sony.IpsGpsFormat;
import vavi.util.Debug;


/**
 * HGR 入力のエミュレーションを行うクラスです。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030319 nsano initial version <br>
 */
public class HgrEmulator2 extends HgrEmulator {

    /** */
    JTextField latField;

    /** */
    JTextField lonField;

    private KeyListener keyListener = new KeyAdapter() {
        public void keyTyped(KeyEvent ev) {

            float lat = 0;
            float lon = 0;

            int code = ev.getKeyCode();
Debug.println("code: " + code);
            switch (code) {
            case KeyEvent.VK_UP:
                lat -= 0.1;
                break;
            case KeyEvent.VK_DOWN:
                lat += 0.1;
                break;
            case KeyEvent.VK_LEFT:
                lon -= 0.1;
                break;
            case KeyEvent.VK_RIGHT:
                lon += 0.1;
                break;
            }

            display(lat, lon);
        }
    };

    /** */
    public HgrEmulator2() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);

        JPanel panel = new JPanel(new BorderLayout());

        CrossCursor mapPanel = new CrossCursor();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel controlPanel = new JPanel(gbl);
        latField = new JTextField(10);
        latField.addKeyListener(keyListener);
        lonField = new JTextField(10);
        lonField.addKeyListener(keyListener);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel label = new JLabel("Lat");
        gbl.setConstraints(label, gbc);
        controlPanel.add(label);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbl.setConstraints(latField, gbc);
        controlPanel.add(latField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        label = new JLabel("Lon");
        gbl.setConstraints(label, gbc);
        controlPanel.add(label);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbl.setConstraints(lonField, gbc);
        controlPanel.add(lonField);

        panel.add(controlPanel, BorderLayout.EAST);
        panel.add(mapPanel, BorderLayout.CENTER);

        frame.getContentPane().add(panel);

        frame.setVisible(true);
    }

    /** */
    class CrossCursor extends JComponent {
        float lon;

        float lat;

        public void paint(Graphics g) {
            int w = getSize().width;
            int h = getSize().height;
            int x = (int) (lon / 180f * (w / 2f) + (w / 2f));
            int y = (int) (-lat / 90f * (h / 2f) + (h / 2f));

            g.setColor(Color.blue);
            g.drawLine(0, y, w, y);
            g.drawLine(x, 0, x, h);
        }

        MouseInputListener mouseListener = new MouseInputAdapter() {
            public void mouseDragged(MouseEvent ev) {
                float w = getSize().width / 2f;
                float h = getSize().height / 2f;
                int x = ev.getX();
                int y = ev.getY();

                lon = (x - w) / w * 180f;
                lat = (h - y) / h * 90f;

                display(lat, lon);

                timeOfFix = new Date();

                repaint();
            }
        };
        /** */
        {
            addMouseListener(mouseListener);
            addMouseMotionListener(mouseListener);
        }
    }

    /** */
    void display(float lat, float lon) {
        if (lon < 0) {
            longitude.setType(PointSurface.WEST_LONGITUDE);
        } else {
            longitude.setType(PointSurface.EAST_LONGITUDE);
        }

        float lonDegrees_f = Math.abs(lon);
        int lonDegrees = (int) Math.floor(lonDegrees_f);
        float lonMinutes_f = (lonDegrees_f - lonDegrees) * 60;

        int lonMinutes = (int) Math.floor(lonMinutes_f);
        float lonSeconds_f = (lonMinutes_f - lonMinutes) * 60;

        longitude.setDegrees(lonDegrees);
        longitude.setMinutes(lonMinutes);
        longitude.setSeconds(lonSeconds_f);

        lonField.setText(longitude.toString());

        if (lat < 0) {
            latitude.setType(PointSurface.SOUTH_LATITUDE);
        } else {
            latitude.setType(PointSurface.NORTH_LATITUDE);
        }

        float latDegrees_f = Math.abs(lat);
        int latDegrees = (int) Math.floor(latDegrees_f);
        float latMinutes_f = (latDegrees_f - latDegrees) * 60;

        int latMinutes = (int) Math.floor(latMinutes_f);
        float latSeconds_f = (latMinutes_f - latMinutes) * 60;

        latitude.setDegrees(latDegrees);
        latitude.setMinutes(latMinutes);
        latitude.setSeconds(latSeconds_f);

        latField.setText(latitude.toString());
    }

    /** */
    private Date timeOfFix = new Date();

    /** */
    private PointSurface latitude = new PointSurface();

    /** */
    private PointSurface longitude = new PointSurface();

    /** */
    protected TimerTask getOutputTimerTask() {
        latitude.setType(PointSurface.NORTH_LATITUDE
        /* PointSurface.SOUTH_LATITUDE */);
        // TODO dmd reader
        latitude.setDegrees(45);
        latitude.setMinutes(0);
        latitude.setSeconds(0);

        longitude.setType(PointSurface.EAST_LONGITUDE
        /* PointSurface.WEST_LONGITUDE */);
        longitude.setDegrees(137);
        longitude.setMinutes(0);
        longitude.setSeconds(0);

        return new TimerTask() {
            public void run() {
                // Debug.println("here");

                IpsGpsData data = new IpsGpsData();

                data.setVersionString("SM0020");

                Date current = new Date();
                data.setDateTime(current);

                PointMap3D map = new PointMap3D();

                map.setLatitude(latitude);
                map.setLongitude(longitude);
                map.setAltitude(0);

                data.setPoint(map);

                MapVector vector = new MapVector();
                vector.setVelocity(0);
                vector.setBearingDirection(0);

                data.setVector(vector);

                data.setTimeOfFix(timeOfFix);

                data.setPDop(0);
                data.setHDop(GpsData.INVALID_DOP);
                data.setVDop(GpsData.INVALID_DOP);

                data.setMeasurementMode(GpsData.MODE_3D);

                data.setMapDatum(1);

                for (int i = 0; i < 16; i++) {
                    Channel channel = new Channel();

                    channel.setPrn(i);
                    channel.setElevation((int) Math.round(Math.random() * 500));
                    channel.setAzimuth((int) Math.round(Math.random() * 180));
                    channel.setInfo(0);
                    channel.setSignalStrength(0);

                    data.addChannel(channel);
                }

                data.setDifferenceOfClock(0);
                data.setUnknown(new byte[] {
                    'D', '3'
                });
                data.setUnitType(IpsGpsData.UNIT_DMS);

                data.setReady(true);

                IpsGpsFormat igf = new IpsGpsFormat();

                byte[] line = igf.format(data);
                // System.err.println(StringUtil.getDump(line));

                try {
                    os.writeLine(new String(line));
                } catch (IOException e) {
                    Debug.println(e);
                }
            }
        };
    }

    // -------------------------------------------------------------------------

    /** */
    private String ioDeviceName = "dummy2";

    /** */
    protected String getIODeviceName() {
        return ioDeviceName;
    }

    public static void main(String[] args) {
        new HgrEmulator2();
    }
}

/* */
