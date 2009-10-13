/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.dvb.ui.FontFactory;


/**
 * Console. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 081014 nsano initial version <br>
 */
class Console {

    /** */
    private Component parent;

    /* */
    public void keyPressed(KeyEvent keyEvent) {
        int keyCode= keyEvent.getKeyCode();
        switch (keyCode) {
        case 461:               //  
            break;
        case KeyEvent.VK_ENTER: // ›
            break;
        case KeyEvent.VK_UP:
            if (currentLine > Math.min(pageSize, lines.size())) {
                currentLine--;
            }
System.err.println("currentLine: " + currentLine);
            break;
        case KeyEvent.VK_DOWN:
            if (currentLine <= lines.size() - 1) {
                currentLine++;
            }
System.err.println("currentLine: " + currentLine);
            break;
        case KeyEvent.VK_LEFT:
            break;
        case KeyEvent.VK_RIGHT:
            break;
        }
status = "currentLine: " + currentLine + "/" + lines.size();
update();
parent.repaint();
    }

    /** */
    private int width, height;

    /** */
    public Console(Component parent, int width, int height) {
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.image = parent.createImage(width, height);
        this.graphics = image.getGraphics();

        this.fontSize = height > 720 ? 36 : 12;
        try {
            FontFactory fontFactory = new FontFactory();
            font = fontFactory.createFont("mikachan", Font.PLAIN, 36);
        } catch (Exception e) {
println(e.toString());
e.printStackTrace(System.err);
        }

        pageSize = height / fontSize - 1;
    }

    /** */
    private int pageSize;

    /** */
    private int fontSize;
    /** */
    private Font font;

    /** model */
    private List lines = new Vector();
    /** */
    private String status = "";

    /** off screen image */
    private Image image;
    /** graphics for off screen image */
    private Graphics graphics;

    /** */
    private int currentLine = 0;

    /** */
    public void println(String string) {
        StringTokenizer st = new StringTokenizer(string, "\n");
        while (st.hasMoreTokens()) {
            lines.add(st.nextToken());
            currentLine++;
        }
        update();
        parent.repaint();
    }

    /** */
    public void println(Throwable t) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            t.printStackTrace(ps);
            String stackTraces = baos.toString();
            StringTokenizer st = new StringTokenizer(stackTraces, "\n");
            while (st.hasMoreTokens()) {
                println(st.nextToken());
            }
        } catch (Exception e) {
e.printStackTrace(System.err);
            println(e.toString());
        }
    }

    public void setStatus(String status) {
        this.status = status;
        update();
        parent.repaint();
    }

    /* */
    private void update() {
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, width, height);

        graphics.setFont(font);

        for (int i = 0; i < Math.min(lines.size(), pageSize); i++) {
            String line = (String) lines.get(Math.max(currentLine - pageSize, 0) + i);

            graphics.setColor(Color.white);
            graphics.drawString(line, 0, i * fontSize);
        }

        graphics.setColor(Color.green);
        graphics.drawString(status, 0, pageSize * fontSize);
    }

    /** */
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, parent);
    }
}

/* */
