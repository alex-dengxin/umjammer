/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.tetris;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;


/**
 * TetrisApp.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080829 nsano initial version <br>
 */
public class TetrisApp implements Xlet {

    /** */
    private HScene scene;
    /** */
    private MyView gui;
    /** */
//    private XletContext context;

    /* */
    public void initXlet(XletContext context) {
//        this.context = context;

        this.scene = HSceneFactory.getInstance().getDefaultHScene();

        try {
            this.gui = new MyView();

            gui.setSize(640, 400);
            scene.add(gui, BorderLayout.CENTER);
        } catch (Exception e) {
e.printStackTrace(System.err);
        }

        scene.validate();
    }

    /* */
    public void startXlet() {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();

        new Thread(gui).start();
    }

    /* */
    public void pauseXlet() {
        gui.setVisible(false);
    }

    /* */
    public void destroyXlet(boolean unconditional) {
        scene.remove(gui);
        scene = null;
    }

    /** */
    private class MyView extends Container implements Tetris.View, Runnable {

        private Tetris game;

        /** */
        private KeyListener keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
System.err.println("k: " + keyCode);
                switch (keyCode) {
                case KeyEvent.VK_UP:
                    game.keyUp();
                    break;
                case KeyEvent.VK_LEFT:
                    game.keyLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    game.keyRight();
                    break;
                case KeyEvent.VK_DOWN:
                    game.keyDown();
                    break;
                case 461:               // Å†
                case 27:
                    game.keyDown(); // TODO Ç¨ÇËÇ¨ÇËóéÇø
                    game.keyDown();
                    game.keyDown();
                    game.keyDown();
                    game.keyDown();
                    game.keyDown();
                    game.keyDown();
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    game.keyRotate();
                    break;
                }    
            }
        };

        private Image image;
        
        private Graphics graphics;

        private Image patterns;

        /** */
        MyView() {
            this.game = new Tetris();
            game.setView(this);

            addKeyListener(keyListener);

            //
            MediaTracker mt = new MediaTracker(this);

            image = patterns = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/tetris.png"));
            mt.addImage(image, 0);

            try { mt.waitForID(0); } catch (InterruptedException e) {}
        }

        /** */
        private boolean notified;

        /* */
        public void addNotify() {
            super.addNotify();
            notified = true;
        }

        /* */
        public void run() {
            while (!notified) {
                Thread.yield();
            }

            image = createImage(640, 400);
            graphics = image.getGraphics();

            game.loop();
        }

        /* */
        public void drawImage(int c, int l, int x, int y) {
            x <<= 4;
            y <<= 4;
            c <<= 4;
            l <<= 4;
            graphics.drawImage(patterns, x, y, x + 16, y + 16, c, l, c + 16, l + 16, this);
            repaint(x, y, 16, 16);
        }

        /* */
        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, this);
        }
    }
}

/* */
