/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.tetris;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.amazon.kindle.kindlet.Kindlet;
import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.ui.KComponent;
import com.amazon.kindle.kindlet.ui.image.ImageUtil;


/**
 * TetrisApp. (KDK)
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/03/11 nsano initial version <br>
 */
public class TetrisApp implements Kindlet {

    private MyView view;

    private KindletContext context;

    /** */
    public void create(KindletContext context) {
        try {
            view = new MyView();
            this.context = context;
            this.context.getRootContainer().add(view);
        } catch (Exception e) {
e.printStackTrace(System.err);
        }
    }

    /** */
    public void start() {
        view.requestFocus(); // for key
        new Thread(view).start();
    }

    public void stop() {
    }

    public void destroy() {
    }

    class MyView extends KComponent implements Tetris.View, Runnable {

        private Tetris game;

        private Image patterns;
        private Image image;
        
        MyView() {
            game = new Tetris();
            game.setView(this);

            setSize(640, 400);
            addKeyListener(keyListener);

            image = ImageUtil.createCompatibleImage(640, 400, Transparency.OPAQUE);

            MediaTracker mt = new MediaTracker(this);
            patterns = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/tetris.gif"));
            mt.addImage(image, 0);
            try { mt.waitForID(0); } catch (InterruptedException e) {}
        }

        /** */
        private boolean notified = false;
        
        /* */
        public void addNotify() {
            super.addNotify();
            notified = true;
        }

        public void run() {
            while (!notified) {
                Thread.yield();
            }

            try {
                game.loop();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
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
            g.drawImage(image, 0, 0, this);
        }
    
        public void drawImage(int c, int l, int x, int y) {
            x <<= 4;
            y <<= 4;
            c <<= 4;
            l <<= 4;
            image.getGraphics().drawImage(patterns, x, y, x + 16, y + 16, c, l, c + 16, l + 16, this);
            repaint(x, y, 16, 16);
        }
    
        public void loopClip() {
        }
    
        public void stopClip() {
        }
    }
}

/* */
