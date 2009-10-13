/*
 * Copyright (c) 2008 by umjammer, All rights reserved.
 *
 * Programmed by umjammer
 * 
 * Released under the GPL
 */

package vavi.apps.appleii;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;


/**
 * AppleIIApp.
 * 
 * @author umjammer
 * @version 0.00 080912 umjammer initial version <br>
 */
public class AppleIIApp extends Activity {

    /* */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
try {
//Debug.startMethodTracing("appleii");
        MyView view = new MyView(this);
        setContentView(view);
        new Thread(view).start();
} catch (Throwable t) {
 t.printStackTrace(System.err);
}
    }

    /* */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//Debug.stopMethodTracing();
    }

    /** */
    private class MyView extends View implements AppleIIGo.View, Runnable {

        private static final int MODE_INIT = 0;
        private static final int MODE_NORMAL = 1;
        private static final int MODE_DIRECT = 3;
        private static final int MODE_DISK1 = 4;
        private static final int MODE_DISK2 = 5;

        private int mode = MODE_INIT;

        private AppleIIGo game;

        private NormalVC normalVC;
        private DirectVC directVC;
        private DiskVC[] diskVCs = new DiskVC[2];
        private GameVC gameVC;

        /** */
        private Paint paint = new Paint();

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

            /* */
            void keyPressed(int keyCode) {
System.err.println("NORMAL: " + keyCode);
                switch(keyCode) {
                case KeyEvent.KEYCODE_1:     // 1
                    // user mapped ?
                    break;
                case KeyEvent.KEYCODE_2:     // 2
                    // user mapped ?
                    break;
                case KeyEvent.KEYCODE_3:     // 3
                    // user mapped ?
                    break;
                case KeyEvent.KEYCODE_4:     // 4
                    // user mapped ?
                    break;
                case KeyEvent.KEYCODE_5:     // 5
                    if (game.isCpuDebugEnabled()) {
                        game.toggleStatMode();
                    }
                    break;
                case KeyEvent.KEYCODE_6:     // 6
                    if (game.isCpuDebugEnabled()) {
                        game.toggleStepMode();
                    }
                    break;
                case KeyEvent.KEYCODE_7:     // 7
                    if (game.isCpuDebugEnabled()) {
                        game.stepInstructions(1);
                    }
                    break;
                case KeyEvent.KEYCODE_8:     // 8
                    if (game.isCpuDebugEnabled()) {
                        game.stepInstructions(128);
                    }
                    break;
                case KeyEvent.KEYCODE_9:     // 9
                    game.setVolume(true);
                    break;
                case KeyEvent.KEYCODE_0:     // 0
                    game.setVolume(false);
                    break;
                case 403:               // B
                    mode = MODE_DISK1;
System.err.println("mode: -> MODE_VIRTUAL_KEYBOARD");
                    break;
                case 404:               // R
                    // user mapped ?
                    break;
                case 405:               // G
                    game.restart();
                    break;
                case 406:               // Y
                    game.reset();
                    break;
                case KeyEvent.KEYCODE_ENTER: // O
case KeyEvent.KEYCODE_X: // for emulator
                    game.setButton(0, true);
                    break;
                case 461:               // #
case KeyEvent.KEYCODE_Z: // for emulator
                    game.setButton(1, true);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:  // L
                    game.setPaddle(0, Paddle.PADDLE_LOW);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:    // U
                    game.setPaddle(1, Paddle.PADDLE_LOW);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:  // D
                    game.setPaddle(1, Paddle.PADDLE_HIGH);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT: // R
                    game.setPaddle(0, Paddle.PADDLE_HIGH);
                    break;
                }
            }

            /* */
            void keyReleased(int keyCode) {
debug("KEY RELEASED: " + keyCode);
                switch(keyCode) {
                case KeyEvent.KEYCODE_ENTER: // O
case KeyEvent.KEYCODE_X: // for emulator
                    game.setButton(0, false);
                    break;
                case 461:               // #
case KeyEvent.KEYCODE_Z: // for emulator
                    game.setButton(1, false);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:  // L
                    game.setPaddle(0, Paddle.PADDLE_CENTER);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:    // U
                    game.setPaddle(1, Paddle.PADDLE_CENTER);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:  // D
                    game.setPaddle(1, Paddle.PADDLE_CENTER);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT: // R
                    game.setPaddle(0, Paddle.PADDLE_CENTER);
                    break;
                }
            }

            final int Y = 384;
            /* */
            public void paint(Canvas g) {
                paint.setStyle(Style.FILL);
                paint.setARGB(0xff, 0, 0, 0);
                g.drawRect(new Rect(0, Y, 560, 540), paint);
                if (debug) {
                    paint.setARGB(0xff, 0xff, 0xff, 0xff);
                    g.drawText(" Normal Mode: ", 0, Y + 36, paint);
//                    g.drawString("   1: " + game.getDiskDriveResource(0), 0, Y + 36 * 2);
//                    g.drawString("   2: " + game.getDiskDriveResource(1), 0, Y + 36 * 3);
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
            Bitmap image;
            int drive;
            File[] files;
            DiskVC(int drive) {
                this.drive = drive;
                this.image = createImage(R.drawable.disk);
                String root = System.getProperty("bluray.vfs.root", "../tmp");
                File file = new File(root);
                files = file.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".dsk") ||
                               name.toLowerCase().endsWith(".nib");
                    }
                });
            }
            int fileIndex;
            void keyPressed(int keyCode) {
System.err.println("DISK[" + drive + "]: " + keyCode);
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (fileIndex > 0) {
                        fileIndex--;
                    } else {
                        fileIndex = files.length - 1;
                    }
                    name = files[fileIndex].getName();
                    selectionMode = MODE_SELECTING;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (fileIndex < files.length - 1) {
                        fileIndex++;
                    } else {
                        fileIndex = 0;
                    }
                    name = files[fileIndex].getName();
                    selectionMode = MODE_SELECTING;
                    break;
                case KeyEvent.KEYCODE_ENTER:
                    name = files[fileIndex].getName();
//                    game.mountDisk(drive, name);
                    selectionMode = MODE_SELECTED;
                    break;
                case 461:               // #
case KeyEvent.KEYCODE_Z: // for emulator
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
                    game.restart();
                    break;
                case 406:               // Y
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
            public void paint(Canvas g) {
                paint.setStyle(Style.FILL);
                paint.setARGB(0xff, 0, 0, 0);
                g.drawRect(new Rect(0, Y, 560, 540), paint);
                g.drawBitmap(image, 0, Y, null);
                switch (selectionMode) {
                case MODE_SELECTED:
                    paint.setARGB(0xff, 0, 0, 0xff);
                    break;
                case MODE_SELECTING:
                    paint.setARGB(0xff, 0xff, 0, 0);
                    break;
                }
                String label = (drive + 1) + ": "  + (name == null || "".equals(name) ? "NO DISK" : name.substring(0, name.length() - 4));
                g.drawText(label, 16, Y + 36, paint);
            }
        }

        /** for emulator */
        class DirectVC {
            void keyPressed(int keyCode) {
System.err.println("DIRECT: " + keyCode);
                switch(keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    game.setKeyLatch(8);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    game.setKeyLatch(21);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    game.setKeyLatch(11);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    game.setKeyLatch(10);
                    break;
                case KeyEvent.KEYCODE_CLEAR:
                    game.setKeyLatch(27);
                    break;
                case KeyEvent.KEYCODE_BACK:
                    game.setKeyLatch(127);
                    break;
                case KeyEvent.KEYCODE_ENTER:
                    game.setKeyLatch(13);
                    break;
                case 403:               // B
                    mode = MODE_NORMAL;
System.err.println("mode: -> NORMAL");
                    break;
                case 405:               // G
                    game.restart();
                    break;
                case 406:               // Y
                    game.reset();
                default:
                    game.setKeyLatch(keyCode);
                    break;
                }
            }
            
            void mousePressed(MotionEvent e) {
                int modifiers = e.getMetaState();
                
                if (modifiers == KeyEvent.KEYCODE_ALT_RIGHT) {
                    game.setButton(0, true);
                }
                if (modifiers == KeyEvent.KEYCODE_ALT_LEFT) {
                    game.setButton(1, true);
                }
            }

            void mouseReleased(MotionEvent e) {
                int modifiers = e.getMetaState();
                
                if (modifiers == KeyEvent.KEYCODE_ALT_RIGHT) {
                    game.setButton(0, false);
                }
                if (modifiers == KeyEvent.KEYCODE_ALT_LEFT) {
                    game.setButton(1, false);
                }
            }

            void mouseDragged(MotionEvent e) {
                mouseMoved(e);
            }

            void mouseMoved(MotionEvent e) {
                game.setPaddlePos((int) e.getX(), (int) e.getY());
            }

            final int Y = 384;
            /* */
            public void paint(Canvas g) {
                paint.setStyle(Style.FILL);
                paint.setARGB(0xff, 0, 0, 0);
                g.drawRect(new Rect(0, Y, 560, 540), paint);
                paint.setARGB(0xff, 0xff, 0xff, 0xff);
                g.drawText(" Direct Mode", 0, Y + 36, paint);
            }
        }

        /* */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (mode) {
            case MODE_NORMAL:
                normalVC.keyPressed(keyCode);
                break;
            case MODE_DIRECT:
                directVC.keyPressed(keyCode);
                break;
            case MODE_DISK1:
                diskVCs[0].keyPressed(keyCode);
                break;
            case MODE_DISK2:
                diskVCs[1].keyPressed(keyCode);
                break;
            }
            return super.onKeyDown(keyCode, event);
        }

        /* */
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            switch (mode) {
            case MODE_NORMAL:
                normalVC.keyReleased(keyCode);
                break;
            }
            return super.onKeyUp(keyCode, event);
        }

        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            
            switch (action) {
            case MotionEvent.ACTION_DOWN: 
                switch (mode) {
                case MODE_DIRECT:
                    directVC.mousePressed(event);
                    break;
                }
                break;
            case MotionEvent.ACTION_UP: 
                switch (mode) {
                case MODE_DIRECT:
                    directVC.mouseReleased(event);
                    break;
                }
                break;
            case MotionEvent.ACTION_MOVE: 
                switch (mode) {
                case MODE_DIRECT:
                    directVC.mouseMoved(event);
                    break;
                }
                break;
            }
            return super.onTouchEvent(event);
        }

public void debug(String s) {
 System.err.println(s);
}
public void debug(Throwable t) {
 t.printStackTrace(System.err);
}

        /** */
        private boolean debug;

        MyView(Context context) throws IOException {
            super(context);
            this.game = new AppleIIGo();
            game.setView(this);
            AppleIIGo.Dao dao = new MyDao();
            game.setDao(dao);
            debug = "true".equals(dao.getParameter("debugMode"));
System.err.println("DEBUG: " + debug);

            setFocusable(true);
        }

        /** */
        public void run() {
try {
            game.init();

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
}
        }

        class GameVC {
            int displayScaledSizeX;
            int displayScaledSizeY; 
            Bitmap displayImage;
            Bitmap displayImagePaused;
            Bitmap displayImageGlare;
            GameVC() {
                // Load glare and pause images
                displayImage = Bitmap.createBitmap(AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y, Bitmap.Config.ARGB_8888);;
                displayImageGlare = createImage(R.drawable.glare);
                displayImagePaused = createImage(R.drawable.paused);
            }

            /** */
            void paint(Canvas g) {
    
                if (displayImage != null) {
                    displayImage.setPixels(game.getDisplayImageBuffer(), 0, AppleDisplay.DISPLAY_SIZE_X, 0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y);

                    g.drawBitmap(displayImage,
                        new Rect(0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y),
                        new Rect(0, 0, displayScaledSizeX, displayScaledSizeY),
                        null);
                }
    
                if (game.isStatMode()) {
                    paint.setStyle(Style.FILL);
                    paint.setARGB(0xff, 0, 0, 0);
                    g.drawRect(new Rect(displayScaledSizeX, 0, displayScaledSizeX + 640, 480), paint);
                    drawStatInfo(g);
                }
    
                if ((displayImagePaused != null) && game.isPaused()) {
                    g.drawBitmap(displayImagePaused,
                                 new Rect(0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y),
                                 new Rect(0, 0, displayScaledSizeX, displayScaledSizeY),
                                 null);
                }
    
                if (game.isGlare() && (displayImageGlare != null)) {
                    g.drawBitmap(displayImageGlare,
                                 new Rect(0, 0, AppleDisplay.DISPLAY_SIZE_X, AppleDisplay.DISPLAY_SIZE_Y),
                                 new Rect(0, 0, displayScaledSizeX, displayScaledSizeY),
                                 null);
                }
            }

            /**
             * Paint stat info
             */
            private void drawStatInfo(Canvas g) {
                StringBuffer statInfo = new StringBuffer();
                statInfo.append(game.getStatInfo()).append("\n");

                StringTokenizer lines = new StringTokenizer(statInfo.toString(), "\n");

                final int fontSize = 32;
                int drawPosY = fontSize;

                paint.setARGB(0xff, 0xff, 0xff, 0xff);

                while (lines.hasMoreTokens()) {
                    String line = lines.nextToken();
                    g.drawText(line, displayScaledSizeX, drawPosY, paint);
                    drawPosY += fontSize;
                }
            }
        };

        /** */
        public void repaint() {
            postInvalidate();
        }

        /* */
        @Override
        protected void onDraw(Canvas g) {
            super.onDraw(g);
            if (gameVC == null) {
                return;
            }
//System.err.println("here");
            gameVC.paint(g);
            switch (mode) {
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

        private Bitmap createImage(int id) {
            Resources r = getContext().getResources();
            return BitmapFactory.decodeResource(r, id);
        }

        public void getCharSet(int[] buffer, int w, int h, int s) {
            Bitmap charSetSource = createImage(R.drawable.character_set);
            charSetSource.getPixels(
                buffer,
                0, s,
                0, 0,
                w, h);
        }

        public void setDisplayScaledSizeX(int w) {
            gameVC.displayScaledSizeX = w;
        }

        public void setDisplayScaledSizeY(int h) {
            gameVC.displayScaledSizeY = h;
        }

        class MyDao implements AppleIIGo.Dao {

            Properties props = new Properties();

            MyDao() throws IOException {
                props.load(new FileInputStream(new File("/sdcard", "appleii.properties")));
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
                    throw new IllegalStateException(e);
                }
            }

            private InputStream is = null;

            /**
             * Open input stream
             */
            public void openInputStream(String resource) {
                try {
                    this.is = new FileInputStream(new File("/sdcard", resource));

                    if (resource.toLowerCase().endsWith(".gz")) {
                        this.is = new GZIPInputStream(is);
                    }
                } catch (Exception e) {
                  throw new IllegalStateException(e);
                }
            }

            /**
             * Open input stream
             */
            public void closeInputStream() {
                try {
                    is.close();
                } catch (IOException e) {
                  throw new IllegalStateException(e);
                }
            }

//            private OutputStream os;

            /**
             * Open output stream
             */
//            public void openOutputStream(String resource) throws IOException {
//                if (!(resource.substring(0, 6).equals("http://"))) {
//                    this.os = new FileOutputStream(resource);
//                }
//            }
        }
    }
}


/* */
