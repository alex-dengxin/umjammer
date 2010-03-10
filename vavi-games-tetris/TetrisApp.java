/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * TetrisApp. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/03/11 nsano initial version <br>
 */
public class TetrisApp extends Applet implements Tetris.View, Runnable {

    Tetris game;

    Thread thread;

    Image image;
    Image mg;
    AudioClip clip;
    
    public void init() {
        game = new Tetris();
        game.setView(this);

        resize(640, 400);
        addKeyListener(keyListener);

        mg = createImage(640, 400);

        clip = getAudioClip(getDocumentBase(), "segatris.mid");
        try {
            MediaTracker med = new MediaTracker(this);
            image = getImage(getDocumentBase(), "tetris.gif");
            med.addImage(image, 0);
            med.waitForID(0);
        } catch (InterruptedException e) {
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void destroy() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void run() {
        game.loop();
    }

    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_NUMPAD8:
            case KeyEvent.VK_UP:
                game.up();
                break;
            case KeyEvent.VK_NUMPAD4:
            case KeyEvent.VK_LEFT:
                game.left();
                break;
            case KeyEvent.VK_NUMPAD6:
            case KeyEvent.VK_RIGHT:
                game.right();
                break;
            case KeyEvent.VK_NUMPAD2:
            case KeyEvent.VK_DOWN:
                game.down();
                break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_NUMPAD5:
                game.rotate();
                break;
            }
        }
    };

    public void paint(Graphics g) {
        g.drawImage(mg, 0, 0, this);
    }

    public void drawImage(int c, int l, int x, int y) {
        x <<= 4;
        y <<= 4;
        c <<= 4;
        l <<= 4;
        mg.getGraphics().drawImage(image, x, y, x + 16, y + 16, c, l, c + 16, l + 16, this);
        repaint(x, y, 16, 16);
    }

    public void loopClip() {
        clip.loop();
    }

    public void stopClip() {
        clip.stop();
    }
}

/* */
