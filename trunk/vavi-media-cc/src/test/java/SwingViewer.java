/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import vavi.media.ui.cc.ClosedCaption;
import vavi.media.ui.cc.Viewer;
import vavi.util.Debug;


/**
 * Swing を利用したサブタイトルビューアです。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 091202 nsano initial version <br>
 */
public class SwingViewer extends JFrame implements Viewer {

    /** */
    public SwingViewer() {
        setBackground(new Color(0x00000000, true));
        setUndecorated(true);
        setAlwaysOnTop(true);

        setSize(w, h);
        setLocation(x, y);

        addMouseListener(mover);
        addMouseMotionListener(mover);
    }

    /** */
    private MouseInputListener mover = new MouseInputAdapter() {
        int clickedX;

        int clickedY;

        public void mousePressed(MouseEvent event) {
            clickedX = event.getX();
            clickedY = event.getY();
        }

        public void mouseDragged(MouseEvent event) {
            int dX = event.getX() - clickedX;
            int dY = event.getY() - clickedY;

            setLocation(getX() + dX, getY() + dY);
        }
    };

    /** */
    public void paint(Graphics g) {
        super.paint(g);

        g.clearRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontRenderContext frc = g2.getFontRenderContext();

        float y = 0;

        StringTokenizer st = new StringTokenizer(text, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            AttributedString as = new AttributedString(line);
            as.addAttribute(TextAttribute.FONT, plainFont, 0, line.length());
            AttributedCharacterIterator aci = as.getIterator();

            TextLayout tl = new TextLayout(aci, frc);
            float sw = (float) tl.getBounds().getWidth();
            // float sh = (float) tl.getBounds().getHeight();
            y += tl.getAscent();
            Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(w / 2 - sw / 2, y));
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(stroke));
            g2.draw(sha);
            g2.setColor(Color.white);
            g2.fill(sha);

            y += tl.getDescent() + tl.getLeading();
        }
    }

    /** */
    private String text;

    /** */
    private List<Timer> timers = new ArrayList<Timer>();

    private void clearTimers() {
        Iterator<Timer> i = timers.iterator();
        while (i.hasNext()) {
            Timer timer = i.next();
            timer.cancel();
        }
    }

    /** */
    public void showClosedCaption(ClosedCaption cc) {
        clearTimers();
Debug.println(cc.getText());
        this.text = cc.getText();

        setVisible(true);
        repaint();

        if (cc.getTimeTo() != -1) {
            long delay = cc.getTimeTo() - cc.getTimeFrom();
            Timer timer = new Timer();
            timer.schedule(new ViewerTimerTask(), delay);
            timers.add(timer);
        }
    }

    /** */
    class ViewerTimerTask extends TimerTask {
        public void run() {
            SwingViewer.this.text = "";

            repaint();
        }
    };

    /** */
    private int w = 1440;

    /** */
    private int h = 100;

    /** */
    private int x;

    /** */
    private int y;

    /** */
    private Font plainFont;

    /** */
    private Font italicFont;

    /** */
    private float stroke;

    /** */
    {
        final String path = "SkinLFViewer.properties";
        final Class<?> clazz = SwingViewer.class;

        try {
            Properties props = new Properties();
            props.load(clazz.getResourceAsStream(path));

            String value = props.getProperty("window.width");
            if (value != null) {
                w = Integer.parseInt(value);
            }
            value = props.getProperty("window.height");
            if (value != null) {
                h = Integer.parseInt(value);
            }

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

            value = props.getProperty("window.x");
            if (value != null) {
                x = Integer.parseInt(value);
            } else {
                x = (screen.width - w) / 2;
            }
            value = props.getProperty("window.y");
            if (value != null) {
                y = Integer.parseInt(value);
            } else {
                y = (screen.height / 4 - h) / 2 + screen.height / 4 * 3;
            }

            int ratio = 12; // 文字の枠の太さの倍率
            int point = 32;
            String name = "MS UI Gothic";
            String fontFile = null;

            value = props.getProperty("font.size");
            if (value != null) {
                point = Integer.parseInt(value);
            }
            value = props.getProperty("font.name");
            if (value != null) {
                name = value;
            }
            value = props.getProperty("font.file");
            if (value != null) {
                fontFile = value;
System.err.println("font0: " + fontFile);
            }

            if (fontFile != null) {
                InputStream is = new FileInputStream(fontFile);
                plainFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, point);
                is.close();
                italicFont = plainFont.deriveFont(Font.ITALIC);
System.err.println("font1: " + plainFont);
            } else {
                plainFont = new Font(name, Font.PLAIN, point);
System.err.println("font2: " + plainFont);
            }

            stroke = point / (float) ratio;
        } catch (Exception e) {
            Debug.printStackTrace(e);
        }
    }
}

/* */
