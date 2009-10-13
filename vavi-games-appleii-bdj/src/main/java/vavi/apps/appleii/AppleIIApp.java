/*
 * Copyright (c) 2008 by umjammer, All rights reserved.
 *
 * Programmed by umjammer
 * 
 * Released under the GPL
 */

package vavi.apps.appleii;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;


/**
 * AppleIIApp.
 * 
 * @author umjammer
 * @version 0.00 080912 umjammer initial version <br>
 */
public class AppleIIApp implements Xlet {

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

            gui.setSize(1920, 1080);
            scene.add(gui, BorderLayout.CENTER);
        } catch (Throwable e) {
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
    private class MyView extends Container implements AppleIIGo.View, Runnable {

        private static final int MODE_INIT = 0;
        private static final int MODE_NORMAL = 1;
        private static final int MODE_VIRTUAL_KEYBOARD = 2;
        private static final int MODE_DIRECT = 3;
        private static final int MODE_DISK1 = 4;
        private static final int MODE_DISK2 = 5;

        private int mode = MODE_INIT;

        private AppleIIGo game;

        private KeyBoard keyBoard;
        private NormalVC normalVC;
        private DirectVC directVC;
        private DiskVC[] diskVCs = new DiskVC[2];
        private GameVC gameVC;

        /**
         * <pre> 
         *  1
         *  2
         *  3
         *  4
         *  5   toggleStatMode
         *  6   toggleStepMode
         *  7   stepInstructions 1
         *  8   stepInstructions 128
         *  9   setVolume true
         *  0   setVolume false
         *  B   mode change
         *  R
         *  G   restart
         *  Y   reset
         *  O   pad button right
         *  #   pad button left   
         *  U   pad up
         *  L   pad left
         *  R   pad right
         *  D   pad down
         * </pre> 
         */
        class NormalVC {

            /** O, # */
            long lastPressed[] = new long[2];

            NormalVC() {
                // xletview doesn't have key release capability!
                if (!releasable) {
                    new Thread(new Runnable() {
                        public void run() {
System.err.println("RELEASE THREAD: start");
                            try {
                                while (true) {
                                    Thread.sleep(100);
                                    if (System.currentTimeMillis() - lastPressed[0] > 100) {
                                        game.setButton(0, false);
                                    }
                                    if (System.currentTimeMillis() - lastPressed[1] > 100) {
                                        game.setButton(1, false);
                                    }
                                }
                            } catch (Throwable t) {
t.printStackTrace(System.err);
debug(t);
                            }
                        }
                    }).start();
                }
            }

            /* */
            void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
System.err.println("NORMAL: " + keyCode);
                switch(keyCode) {
                case KeyEvent.VK_1:     // 1
                    // user mapped ?
                    break;
                case KeyEvent.VK_2:     // 2
                    // user mapped ?
                    break;
                case KeyEvent.VK_3:     // 3
                    // user mapped ?
                    break;
                case KeyEvent.VK_4:     // 4
                    // user mapped ?
                    break;
                case KeyEvent.VK_5:     // 5
                    if (game.isCpuDebugEnabled()) {
                        game.toggleStatMode();
                    }
                    break;
                case KeyEvent.VK_6:     // 6
                    if (game.isCpuDebugEnabled()) {
                        game.toggleStepMode();
                    }
                    break;
                case KeyEvent.VK_7:     // 7
                    if (game.isCpuDebugEnabled()) {
                        game.stepInstructions(1);
                    }
                    break;
                case KeyEvent.VK_8:     // 8
                    if (game.isCpuDebugEnabled()) {
                        game.stepInstructions(128);
                    }
                    break;
                case KeyEvent.VK_9:     // 9
                    game.setVolume(true);
                    break;
                case KeyEvent.VK_0:     // 0
                    game.setVolume(false);
                    break;
                case 403:               // B
                    mode = MODE_VIRTUAL_KEYBOARD;
game.setPaddle(0, Paddle.PADDLE_CENTER);
game.setPaddle(1, Paddle.PADDLE_CENTER);
System.err.println("mode: -> MODE_VIRTUAL_KEYBOARD");
                    break;
                case 404:               // R
                    // user mapped ?
                    break;
                case 405:               // G
stat.clear();
                    game.restart();
                    break;
                case 406:               // Y
stat.clear();
                    game.reset();
                    break;
                case KeyEvent.VK_ENTER: // O
case KeyEvent.VK_X: // for emulator
                    game.setButton(0, true);
                    if (!releasable) {
                        lastPressed[0] = System.currentTimeMillis();
                    }
                    break;
                case 461:               // #
case KeyEvent.VK_Z: // for emulator
                    game.setButton(1, true);
                    if (!releasable) {
                        lastPressed[1] = System.currentTimeMillis();
                    }
                    break;
                case KeyEvent.VK_LEFT:  // L
                    game.setPaddle(0, Paddle.PADDLE_LOW);
                    break;
                case KeyEvent.VK_UP:    // U
                    game.setPaddle(1, Paddle.PADDLE_LOW);
                    break;
                case KeyEvent.VK_DOWN:  // D
                    game.setPaddle(1, Paddle.PADDLE_HIGH);
                    break;
                case KeyEvent.VK_RIGHT: // R
                    game.setPaddle(0, Paddle.PADDLE_HIGH);
                    break;
                }
            }

            /* */
            void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
//debug("KEY RELEASED: " + e.getKeyCode());
                if (releasable) {
                    switch(keyCode) {
                    case KeyEvent.VK_ENTER: // O
case KeyEvent.VK_X: // for emulator
                        game.setButton(0, false);
                        break;
                    case 461:               // #
case KeyEvent.VK_Z: // for emulator
                        game.setButton(1, false);
                        break;
                    case KeyEvent.VK_LEFT:  // L
                        game.setPaddle(0, Paddle.PADDLE_CENTER);
                        break;
                    case KeyEvent.VK_UP:    // U
                        game.setPaddle(1, Paddle.PADDLE_CENTER);
                        break;
                    case KeyEvent.VK_DOWN:  // D
                        game.setPaddle(1, Paddle.PADDLE_CENTER);
                        break;
                    case KeyEvent.VK_RIGHT: // R
                        game.setPaddle(0, Paddle.PADDLE_CENTER);
                        break;
                    }
                }
            }

            final int Y = 384;
            /* */
            public void paint(Graphics g) {
                g.setColor(Color.black);
                g.fillRect(0, Y, 560, 540);
                if (debug) {
                    g.setColor(Color.white);
                    g.drawString(" Normal Mode: releasable: " + releasable, 0, Y + 36);
                    g.drawString("   1: " + game.getDiskDriveResource(0), 0, Y + 36 * 2);
                    g.drawString("   2: " + game.getDiskDriveResource(1), 0, Y + 36 * 3);
                }
            }
        }

        /**
         * <pre>
         *  B   mode change
         *  G   restart
         *  O   insert
         *  #   release
         *  U   disk previous
         *  D   disk next
         * </pre>
         */
        class DiskVC {
            Image image;
            int drive;
            File[] files;
            /** has createImage */
            DiskVC(int drive) {
                this.drive = drive;
                this.image = createImage("/disk.png");
                String root = System.getProperty("bluray.vfs.root", "../tmp");
                File file = new File(root);
try {
                files = file.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".dsk") ||
                               name.toLowerCase().endsWith(".nib");
                    }
                });
} catch (Throwable t) { // for xletview bug
 t.printStackTrace(System.err);
                files = new File[] {
                    new File(file, "Lode Runner.dsk"), 
                    new File(file, "Mystery House.dsk"), 
                    new File(file, "Tetris II.dsk"), 
                    new File(file, "tzone1a.nib"), 
                    new File(file, "tzone1b.nib"), 
                    new File(file, "tzone2c.nib"), 
                    new File(file, "tzone2d.nib"), 
                    new File(file, "tzone3e.nib"), 
                    new File(file, "tzone3f.nib"), 
                    new File(file, "tzone4g.nib"), 
                    new File(file, "tzone4h.nib"), 
                    new File(file, "tzone5i.nib"), 
                    new File(file, "tzone5j.nib"), 
                    new File(file, "tzone6k.nib"), 
                    new File(file, "tzone6l.nib"),
                };
}
            }
            int fileIndex;
            void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
System.err.println("DISK[" + drive + "]: " + keyCode);
                switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (fileIndex > 0) {
                        fileIndex--;
                    } else {
                        fileIndex = files.length - 1;
                    }
                    name = files[fileIndex].getName();
                    selectionMode = MODE_SELECTING;
                    break;
                case KeyEvent.VK_DOWN:
                    if (fileIndex < files.length - 1) {
                        fileIndex++;
                    } else {
                        fileIndex = 0;
                    }
                    name = files[fileIndex].getName();
                    selectionMode = MODE_SELECTING;
                    break;
                case KeyEvent.VK_ENTER:
                    name = files[fileIndex].getName();
                    game.mountDisk(drive, name);
                    selectionMode = MODE_SELECTED;
                    break;
                case 461:               // #
case KeyEvent.VK_Z: // for emulator
                    name = null;
                    game.mountDisk(drive, null);
                    selectionMode = MODE_SELECTED;
                    break;
                case 403:               // B
                    if (drive == 0) {
                        mode = MODE_DISK2;
                        diskVCs[1].init();
System.err.println("mode: -> DISK2");
                    } else {
                        if (debug) {
                            mode = MODE_DIRECT;
System.err.println("mode: -> DIRECT");
                        } else {
                            mode = MODE_NORMAL;
System.err.println("mode: -> NORMAL");
                        }
                    }
                    break;
                case 405:               // G
stat.clear();
                    game.restart();
                    break;
                case 406:               // Y
stat.clear();
                    game.reset();
                }
            }
            String name;
            final int MODE_SELECTING = 1;
            final int MODE_SELECTED = 0;
            int selectionMode;
            void init() {
                name = game.getDiskDriveResource(drive);
System.err.println("DRIVE[" + drive + "]: " + name);
                selectionMode = MODE_SELECTED;
            }
            final int Y = 384;
            /* */
            public void paint(Graphics g) {
                g.setColor(Color.black);
                g.fillRect(0, Y, 560, 540);
                g.drawImage(image, 0, Y, MyView.this);
                switch (selectionMode) {
                case MODE_SELECTED:
                    g.setColor(Color.blue);
                    break;
                case MODE_SELECTING:
                    g.setColor(Color.red);
                    break;
                }
                String label = (drive + 1) + ": "  + (name == null || "".equals(name) ? "NO DISK" : name.substring(0, name.length() - 4));
                g.drawString(label, 16, Y + 36);
            }
        }

        /** for emulator */
        class DirectVC {
            void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
System.err.println("DIRECT: " + keyCode);
                switch(keyCode) {
                case KeyEvent.VK_LEFT:
                    game.setKeyLatch(8);
                    break;
                case KeyEvent.VK_RIGHT:
                    game.setKeyLatch(21);
                    break;
                case KeyEvent.VK_UP:
                    game.setKeyLatch(11);
                    break;
                case KeyEvent.VK_DOWN:
                    game.setKeyLatch(10);
                    break;
                case KeyEvent.VK_ESCAPE:
                    game.setKeyLatch(27);
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    game.setKeyLatch(127);
                    break;
                case KeyEvent.VK_ENTER:
                    game.setKeyLatch(13);
                    break;
                case 403:               // B
                    mode = MODE_NORMAL;
game.setPaddle(0, Paddle.PADDLE_CENTER);
game.setPaddle(1, Paddle.PADDLE_CENTER);
System.err.println("mode: -> NORMAL");
                    break;
                case 405:               // G
stat.clear();
                    game.restart();
                    break;
                case 406:               // Y
stat.clear();
                    game.reset();
                default:
                    game.setKeyLatch(keyCode);
                    break;
                }
            }
            
            void mousePressed(MouseEvent e) {
                int modifiers = e.getModifiers();
                
                if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
                    game.setButton(0, true);
                }
                if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
                    game.setButton(1, true);
                }
            }

            void mouseReleased(MouseEvent e) {
                int modifiers = e.getModifiers();
                
                if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
                    game.setButton(0, false);
                }
                if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
                    game.setButton(1, false);
                }
            }

            void mouseDragged(MouseEvent e) {
                mouseMoved(e);
            }

            void mouseMoved(MouseEvent e) {
                game.setPaddlePos(e.getX(), e.getY());
            }

            final int Y = 384;
            /* */
            public void paint(Graphics g) {
                g.setColor(Color.black);
                g.fillRect(0, Y, 560, 540);
                g.setColor(Color.white);
                g.drawString(" Direct Mode", 0, Y + 36);
            }
        }

        /** */
        private KeyListener keyListener = new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                switch (mode) {
                case MODE_VIRTUAL_KEYBOARD:
                    keyBoard.keyPressed(e);
                    break;
                case MODE_NORMAL:
                    normalVC.keyPressed(e);
                    break;
                case MODE_DIRECT:
                    directVC.keyPressed(e);
                    break;
                case MODE_DISK1:
                    diskVCs[0].keyPressed(e);
                    break;
                case MODE_DISK2:
                    diskVCs[1].keyPressed(e);
                    break;
                }
            }

            public void keyReleased(KeyEvent e) {
                switch (mode) {
                case MODE_NORMAL:
                    normalVC.keyReleased(e);
                    break;
                }
            }
        };

        /** */
        private MouseListener mouseListener = new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                switch (mode) {
                case MODE_VIRTUAL_KEYBOARD:
                    keyBoard.mousePressed(e);
                    break;
                case MODE_DIRECT:
                    directVC.mousePressed(e);
                    break;
                }
            }

            public void mouseReleased(MouseEvent e) {
                switch (mode) {
                case MODE_VIRTUAL_KEYBOARD:
                    keyBoard.mouseReleased(e);
                    break;
                case MODE_DIRECT:
                    directVC.mouseReleased(e);
                    break;
                }
            }
        };

        /** */
        private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                switch (mode) {
                case MODE_NORMAL:
                    break;
                case MODE_DIRECT:
                    directVC.mouseDragged(e);
                    break;
                }
            }

            public void mouseMoved(MouseEvent e) {
                switch (mode) {
                case MODE_NORMAL:
                    break;
                case MODE_DIRECT:
                    directVC.mouseMoved(e);
                    break;
                }
            }
        };

        /** */
        private boolean notified;

        /* */
        public void addNotify() {
            super.addNotify();
            notified = true;
        }

List stat = new ArrayList();
public void debug(String s) {
 stat.add(s);
}
public void debug(Throwable t) {
 t.printStackTrace(System.err);
 ByteArrayOutputStream baos = new ByteArrayOutputStream();
 PrintStream ps = new PrintStream(baos);
 t.printStackTrace(ps);
 String stackTraces = baos.toString();
 StringTokenizer st = new StringTokenizer(stackTraces, "\n");
 while (st.hasMoreTokens()) {
  stat.add(st.nextToken());
 }
}

        /** */
        private boolean debug;

        /** */
        private boolean releasable;

        /** */
        MyView() throws IOException {
            this.game = new AppleIIGo();
            game.setView(this);
            AppleIIGo.Dao dao = new MyDao();
            game.setDao(dao);
            debug = "true".equals(dao.getParameter("debugMode"));
System.err.println("DEBUG: " + debug);
            releasable = "YES".equals(System.getProperty("bluray.rccapability.holdandrelease")); // "bluray.rccapability.release"
System.err.println("RELEASABLE: " + releasable);

            addKeyListener(keyListener);
            addMouseListener(mouseListener);
            addMouseMotionListener(mouseMotionListener);
        }

        /** */
        public void run() {
            while (!notified) {
                Thread.yield();
            }
try {
            game.init();

            this.keyBoard = new KeyBoard();
            this.directVC = new DirectVC();
            this.normalVC = new NormalVC();
            this.diskVCs[0] = new DiskVC(0);
            this.diskVCs[1] = new DiskVC(1);
            this.gameVC = new GameVC();
    
            mode = MODE_NORMAL;
System.err.println("mode: -> MODE_NORMAL");

            game.start();
} catch (Throwable t) {
 debug(t);
 repaint();
}
        }

        /**
         * <pre> 
         *  B   mode change
         *  G   restart
         *  O   key select
         *  U   key up
         *  L   key left
         *  R   key right
         *  D   key down
         * </pre> 
         */
        class KeyBoard {
            Image image;
            int[][] bounds = {
                { 6, 14, 22, 14 }, 
                { 31, 14, 22, 14 },
                { 56, 14, 22, 14 },
                { 82, 14, 22, 14 },
                { 107, 14, 22, 14 },
                { 133, 14, 22, 14 },
                { 159, 14, 22, 14 },
                { 184, 14, 22, 14 },
                { 209, 14, 22, 14 },
                { 235, 14, 22, 14 },
                { 260, 14, 22, 14 },
                { 286, 14, 22, 14 },
                { 311, 14, 22, 14 },
                { 337, 14, 22, 14 },

                { 6, 31, 22, 20 },
                { 30, 31, 22, 20 },
                { 55, 31, 22, 20 },
                { 79, 31, 22, 20 },
                { 104, 31, 22, 20 },
                { 129, 31, 22, 20 },
                { 153, 31, 22, 20 },
                { 177, 31, 22, 20 },
                { 202, 31, 22, 20 },
                { 226, 31, 22, 20 },
                { 251, 31, 22, 20 },
                { 276, 31, 22, 20 },
                { 300, 31, 22, 20 },
                { 325, 31, 34, 20 },

                { 6, 55, 34, 20 },
                { 42, 55, 22, 20 },
                { 67, 55, 22, 20 },
                { 91, 55, 22, 20 },
                { 115, 55, 22, 20 },
                { 140, 55, 22, 20 },
                { 165, 55, 22, 20 },
                { 189, 55, 22, 20 },
                { 214, 55, 22, 20 },
                { 239, 55, 22, 20 },
                { 263, 55, 22, 20 },
                { 288, 55, 22, 20 },
                { 312, 55, 22, 20 },
                { 337, 55, 22, 20 },

                { 6, 79, 39, 20 },
                { 48, 79, 22, 20 },
                { 73, 79, 22, 20 },
                { 97, 79, 22, 20 },
                { 122, 79, 22, 20 },
                { 147, 79, 22, 20 },
                { 171, 79, 22, 20 },
                { 196, 79, 22, 20 },
                { 221, 79, 22, 20 },
                { 245, 79, 22, 20 },
                { 270, 79, 22, 20 },
                { 294, 79, 22, 20 },
                { 319, 79, 39, 20 },

                { 6, 102, 52, 20 },
                { 60, 102, 22, 20 },
                { 85, 102, 22, 20 },
                { 110, 102, 22, 20 },
                { 134, 102, 22, 20 },
                { 159, 102, 22, 20 },
                { 184, 102, 22, 20 },
                { 208, 102, 22, 20 },
                { 233, 102, 22, 20 },
                { 258, 102, 22, 20 },
                { 282, 102, 22, 20 },
                { 307, 102, 52, 20 },

                { 6, 126, 21, 24 },
                { 30, 126, 21, 24 },
                { 55, 126, 21, 24 },
                { 79, 126, 28, 24 },
                { 110, 126, 121, 24 },
                { 233, 126, 28, 24 },
                { 263, 126, 21, 24 },

                { 288, 138, 22, 12 },
                { 313, 126, 22, 12 },
                { 313, 138, 22, 12 },
                { 337, 138, 22, 12 },
            };
            /** l, r, u, d */
            int[][] navigation = {
                { 13, 1, 67, 14 }, 
                { 0, 2, 68, 15 },
                { 1, 3, 69, 16 },
                { 2, 4, 70, 17 },
                { 3, 5, 71, 18 },
                { 4, 6, 71, 19 },
                { 5, 7, 71, 20 },
                { 6, 8, 71, 21 },
                { 7, 9, 71, 22 },
                { 8, 10, 72, 23 },
                { 9, 11, 73, 24 },
                { 10, 12, 74, 25 },
                { 11, 13, 76, 26 },
                { 12, 0, 77, 27 },

                { 27, 15, 0, 28 },
                { 14, 16, 1, 29 },
                { 15, 17, 2, 30 },
                { 16, 18, 3, 31 },
                { 17, 19, 4, 32 },
                { 18, 20, 5, 33 },
                { 19, 21, 6, 34 },
                { 20, 22, 7, 35 },
                { 21, 23, 8, 36 },
                { 22, 24, 9, 37 },
                { 23, 25, 10, 38 },
                { 24, 26, 11, 39 },
                { 25, 27, 12, 40 },
                { 26, 14, 13, 41 },

                { 41, 29, 14, 42 },
                { 28, 30, 15, 43 },
                { 29, 31, 16, 44 },
                { 30, 32, 17, 45 },
                { 31, 33, 18, 46 },
                { 32, 34, 19, 47 },
                { 33, 35, 20, 48 },
                { 34, 36, 21, 49 },
                { 35, 37, 22, 50 },
                { 36, 38, 23, 51 },
                { 37, 39, 24, 52 },
                { 38, 40, 25, 53 },
                { 39, 41, 26, 54 },
                { 40, 28, 27, 54 },

                { 54, 43, 28, 55 },
                { 42, 44, 29, 56 },
                { 43, 45, 30, 57 },
                { 44, 46, 31, 58 },
                { 45, 47, 32, 59 },
                { 46, 48, 33, 60 },
                { 47, 49, 34, 61 },
                { 48, 50, 35, 62 },
                { 49, 51, 36, 63 },
                { 50, 52, 37, 64 },
                { 51, 53, 38, 65 },
                { 52, 54, 39, 66 },
                { 53, 42, 40, 66 },

                { 66, 56, 42, 67 },
                { 55, 57, 43, 69 },
                { 56, 58, 44, 70 },
                { 57, 59, 45, 71 },
                { 58, 60, 46, 71 },
                { 59, 61, 47, 71 },
                { 60, 62, 48, 71 },
                { 61, 63, 49, 71 },
                { 62, 64, 50, 72 },
                { 63, 65, 51, 73 },
                { 64, 66, 52, 74 },
                { 65, 67, 53, 75 },

                { 77, 68, 55, 0 },
                { 67, 69, 55, 1 },
                { 68, 70, 56, 2 },
                { 69, 71, 57, 3 },
                { 70, 72, 60, 6 },
                { 71, 73, 63, 9 },
                { 72, 74, 64, 10 },

                { 73, 75, 65, 11 },
                { 74, 77, 66, 76 },
                { 74, 77, 75, 12 },
                { 76, 67, 66, 13 },
            };
            /** has createImage */
            KeyBoard() {
                this.image = createImage("/keyboard.png");
            }
            final int Y = 384;
            /* */
            public void paint(Graphics g) {
                g.drawImage(image, 0, Y, MyView.this);
                if (shiftIndex == 1) {
                    g.setColor(Color.blue);
                    g.drawRect(bounds[55][0], bounds[55][1] + Y, bounds[55][2], bounds[55][3]);
                    g.setColor(Color.blue);
                    g.drawRect(bounds[66][0], bounds[66][1] + Y, bounds[66][2], bounds[66][3]);
                }
                g.setColor(Color.red);
                g.drawRect(bounds[keyIndex][0], bounds[keyIndex][1] + Y, bounds[keyIndex][2], bounds[keyIndex][3]);
            }
            int shiftIndex = 0;
            final int KEY_SHIFT = -2;
            int keyIndex;
            /** normal, shift */
            int[][] keyDatum = {
                { 0x1b, 0x1b }, // esc
                { -1, -1 }, // f1
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 },
                { -1, -1 }, // f12
                { -1, -1 }, // ?

                { '`', '~' },
                { '1', '!' },
                { '2', '@' },
                { '3', '#' },
                { '4', '$' },
                { '5', '%' },
                { '6', '^' },
                { '7', '&' },
                { '8', '*' },
                { '9', '(' },
                { '0', ')' },
                { '-', '_' },
                { '=', '+' },
                { 0x08, 0x08 },   // delete

                { 0x09, 0x09 },   // tab
                { 'q', 'Q' },
                { 'w', 'W' },
                { 'e', 'E' },
                { 'r', 'R' },
                { 't', 'T' },
                { 'y', 'Y' },
                { 'u', 'U' },
                { 'i', 'I' },
                { 'o', 'O' },
                { 'p', 'P' },
                { '[', '{' },
                { ']', '}' },
                { '\\', '|' },

                { -1, -1 },   // caps
                { 'a', 'A' },
                { 's', 'S' },
                { 'd', 'D' },
                { 'f', 'F' },
                { 'g', 'G' },
                { 'h', 'H' },
                { 'j', 'J' },
                { 'k', 'K' },
                { 'l', 'L' },
                { ';', ':' },
                { '\'', '\"' },
                { 0x0d, 0x0d },   // enter

                { KEY_SHIFT, KEY_SHIFT },   // shift
                { 'z', 'Z' },
                { 'x', 'X' },
                { 'c', 'C' },
                { 'v', 'V' },
                { 'b', 'B' },
                { 'n', 'N' },
                { 'm', 'M' },
                { ',', '<' },
                { '.', '>' },
                { '/', '?' },
                { KEY_SHIFT, KEY_SHIFT },   // shift

                { -1, -1 },   // fn
                { -1, -1 },   // ctrl
                { -1, -1 },   // option
                { -1, -1 },   // command
                { 0x20, 0x20 },   // space
                { -1, -1 },   // command
                { -1, -1 },   // option
                { 8, 8 },   // left
                { 11, 11 },   // up
                { 10, 10 },   // down
                { 21, 21 },   // right
            };
            public void keyPressed(KeyEvent e) {
System.err.println("VK: " + e.getKeyCode());
                switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    keyIndex = navigation[keyIndex][0];
                    break;
                case KeyEvent.VK_RIGHT:
                    keyIndex = navigation[keyIndex][1];
                    break;
                case KeyEvent.VK_UP:
                    keyIndex = navigation[keyIndex][2];
                    break;
                case KeyEvent.VK_DOWN:
                    keyIndex = navigation[keyIndex][3];
                    break;
                case KeyEvent.VK_ENTER: // O
                    keyPressed();
                    break;
                case 461:               // #
                    // TODO toggle shift ? BS ?
                    break;
                case 403:               // B
                    mode = MODE_DISK1;
                    diskVCs[0].init();
System.err.println("mode: -> DISK1");
                    break;
                case 405:               // G
stat.clear();
                    game.restart();
                case 406:               // Y
stat.clear();
                    game.reset();
                    break;
                }
            }
            private void keyPressed() {
                int keyCode = keyDatum[keyIndex][shiftIndex];
                switch (keyCode) {
                case KEY_SHIFT:
                    shiftIndex = 1 - shiftIndex; 
System.err.println("VK: SHIFT: " + (shiftIndex == 1 ? "ON" : "OFF"));
                    break;
                default:
                    game.setKeyLatch(keyCode);
System.err.println("VK: SEND: " + keyCode);
                    break;
                }
            }
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                for (int i = 0; i < bounds.length; i++) {
                    if (x >= bounds[i][0] && x <= bounds[i][0] + bounds[i][2] &&
                        y >= bounds[i][1] + Y && y <= bounds[i][1] + Y + bounds[i][3]) {
                        keyIndex = i;
                        keyPressed();
                    }
                }
            }
            public void mouseReleased(MouseEvent e) {
            }
        }

        /* */
        public void paint(Graphics g) {
try {
            if (gameVC != null) {
                gameVC.paint(g);
                switch (mode) {
                case MODE_VIRTUAL_KEYBOARD:
                    keyBoard.paint(g);
                    break;
                case MODE_NORMAL:
                    normalVC.paint(g);
                    break;
                case MODE_DIRECT:
                    directVC.paint(g);
                    break;
                case MODE_DISK1:
                    diskVCs[0].paint(g);
                    break;
                case MODE_DISK2:
                    diskVCs[1].paint(g);
                    break;
                }
            }
} catch (Throwable t) {
 debug(t);
}
if (stat.size() > 0) {
 g.setColor(Color.red);
 for (int i = 0; i < stat.size(); i++) {
  g.drawString((String) stat.get(i), 0, 36 * i + 36);
 }
}
        }

        /** */
        class GameVC {
            int displayScaledSizeX;
            int displayScaledSizeY; 
            MemoryImageSource mis;
            Image displayImage;
            Image displayImagePaused;
            Image displayImageGlare;
            /** has createImage */
            GameVC() {
                this.mis = new MemoryImageSource(AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y, game.getDisplayImageBuffer(), 0, AppleDisplay.DISPLAY_SIZE_X);
                mis.setAnimated(true);
                mis.setFullBufferUpdates(true);
                this.displayImage = createImage(mis);

                // Load glare and pause images
                displayImageGlare = createImage("/Glare.png");
                displayImagePaused = createImage("/Paused.png");
            }
            /** */
            void paint(Graphics g) {
    
                mis.newPixels();

                g.drawImage(displayImage,
                    0, 0, displayScaledSizeX, displayScaledSizeY,
                    0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y,
                    MyView.this);

                if (game.isStatMode()) {
                    g.setColor(Color.black);
                    g.fillRect(displayScaledSizeX, 0, 512, 600); 
                    drawStatInfo(g);
                }
    
                if (game.isPaused()) {
                    g.drawImage(displayImagePaused,
                        0, 0, displayScaledSizeX, displayScaledSizeY,
                        0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y,
                        MyView.this);
                }
    
                if (game.isGlare()) {
                    g.drawImage(displayImageGlare,
                        0, 0, displayScaledSizeX, displayScaledSizeY,
                        0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y,
                        MyView.this);
                }
            }

            /**
             * Paint stat info
             */
            private void drawStatInfo(Graphics g) {
                StringBuffer statInfo = new StringBuffer();
                statInfo.append(game.getStatInfo()).append("\n");

                StringTokenizer lines = new StringTokenizer(statInfo.toString(), "\n");

                final int fontSize = 32;
                int drawPosY = fontSize;

                g.setColor(Color.white);

                while (lines.hasMoreTokens()) {
                    String line = lines.nextToken();
                    g.drawString(line, displayScaledSizeX, drawPosY);
                    drawPosY += fontSize;
                }
            }
        };

        private Image createImage(String name) {
try {
            MediaTracker mt = new MediaTracker(this);

            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(name));
            mt.addImage(image, 0);

            try { mt.waitForID(0); } catch (InterruptedException e) {}

            return image;
} catch (Throwable t) {
 debug(t);
 repaint();
            return null;
}
        }

        /* */
        public Object getCharSet(int[] buffer, int w, int h, int s) {
            Image charSetSource = createImage("/Character Set.png");
            BufferedImage bi = (BufferedImage) createImage(w, h);
            bi.getGraphics().drawImage(charSetSource, 0, 0, null);

            bi.getRGB(
                0, 0,
                w, h,
                buffer,
                0, s);

            return charSetSource;
        }

        /** @see #getCharSet(int[], int, int, int) */
        public void flushCharSet(Object resource) {
            ((Image) resource).flush();
        }

        public void setDisplayScaledSizeX(int w) {
            gameVC.displayScaledSizeX = w;
        }

        public void setDisplayScaledSizeY(int h) {
            gameVC.displayScaledSizeY = h;
        }
    }

    /** */
    class MyDao implements AppleIIGo.Dao {
        private String root;
        private Properties props = new Properties();

        MyDao() throws IOException {
            root = System.getProperty("bluray.vfs.root", "../tmp");
//System.err.println("root: " + root);
            props.load(new FileInputStream(new File(root, "appleii.properties")));
        }

        /* */
        public String getParameter(String parameter) {
            return props.getProperty(parameter);
        }

        /* */
        public void read(byte[] bytes, int offset, int length) {
            try {
                int l = 0;
                while (l < length) {
                    int r = is.read(bytes, offset + l, length - l);
                    if (r < 0) {
System.err.println("Illegal EOF: " + l + "/" + length);
                        break;
                    }
                    l += r;
                }
            } catch (IOException e) {
//e.printStackTrace(System.err);
                throw new IllegalStateException(e);
            }
        }

        private InputStream is = null;

        /**
         * Open input stream
         */
        public void openInputStream(String resource) {
            try {
//              URL url = getClass().getResource(resource);
//              is = url.openStream();
                this.is = new FileInputStream(new File(root, resource));

                if (resource.toLowerCase().endsWith(".gz")) {
                    this.is = new GZIPInputStream(is);
                }
            } catch (IOException e) {
//e.printStackTrace(System.err);
              throw new IllegalStateException(e);
            }
        }

        /**
         * Close input stream
         */
        public void closeInputStream() {
            try {
                is.close();
            } catch (IOException e) {
//e.printStackTrace(System.err);
              throw new IllegalStateException(e);
            }
        }

//        private OutputStream os;

        /**
         * Open output stream
         */
//        public void openOutputStream(String resource) throws IOException {
//            if (!(resource.substring(0, 6).equals("http://"))) {
//                this.os = new FileOutputStream(resource);
//            }
//        }
    }
}


/* */
