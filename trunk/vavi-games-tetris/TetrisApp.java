/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.applet.Applet;
import java.applet.AudioClip;
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
public class TetrisApp extends Applet implements Runnable {

    Tetris game;

    Thread thread = null;
    Image bufer;
    Image image;
    Image mg;
    MediaTracker med;
    AudioClip clip;

    public void init() {
        game.init();
        resize(640, 400);
        bufer = createImage(16, 16);
        mg = createImage(640, 400);
        image = getImage(getDocumentBase(), "tetris.gif");
        clip = getAudioClip(getDocumentBase(), "segatris.mid");
        med = new MediaTracker(this);
        med.addImage(image, 0);
        addKeyListener(keyListener);
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void destroy() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void run() {
        try {
            med.waitForID(0);
        } catch (InterruptedException e) {
            return;
        }
        
        game.loop();
    }

    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();
            if ((k == KeyEvent.VK_NUMPAD8) || (k == KeyEvent.VK_UP)) {
                kf8++;
            }
            if ((k == KeyEvent.VK_NUMPAD4) || (k == KeyEvent.VK_LEFT)) {
                kf4++;
            }
            if ((k == KeyEvent.VK_NUMPAD6) || (k == KeyEvent.VK_RIGHT)) {
                kf6++;
            }
            if ((k == KeyEvent.VK_NUMPAD2) || (k == KeyEvent.VK_DOWN)) {
                kf2++;
            }
            if ((k == KeyEvent.VK_SPACE) || (k == KeyEvent.VK_NUMPAD5)) {
                kfs++;
            }
        }
    };
}

/* */
