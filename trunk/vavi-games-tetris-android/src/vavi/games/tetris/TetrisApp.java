/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.tetris;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;


/**
 * TetrisApp.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080829 nsano initial version <br>
 */
public class TetrisApp extends Activity {

    /* */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        MyView view = new MyView(this);
        setContentView(view);
        new Thread(view).start();
    }

    /** */
    private class MyView extends View implements Tetris.View, Runnable {

        final int SCALE= 2;

        private Tetris game;

        /** */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                game.keyUp();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                game.keyLeft();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                game.keyRight();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                game.keyDown();
                break;
            case KeyEvent.KEYCODE_ENTER:
                game.keyDown(); // TODO ‚¬‚è‚¬‚è—Ž‚¿
                game.keyDown();
                game.keyDown();
                game.keyDown();
                game.keyDown();
                game.keyDown();
                game.keyDown();
                break;
            case KeyEvent.KEYCODE_SPACE:
                game.keyRotate();
                break;
            }    
            return super.onKeyDown(keyCode, event);
        }

        /** */
        private Bitmap image;

        /** */
        private Canvas canvas;

        /** */
        private Bitmap patterns;

        /** */
        MyView(Context context) {
            super(context);

            this.game = new Tetris();
            game.setView(this);

            setFocusable(true);

            // images
            Resources r = getContext().getResources();

            patterns = BitmapFactory.decodeResource(r, R.drawable.tetris);

            image = Bitmap.createBitmap(640, 400, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(image);
        }

        /* */
        public void run() {
            try {
                game.loop();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        /* */
        public void drawImage(int c, int l, int x, int y) {
            x <<= 4;
            y <<= 4;
            c <<= 4;
            l <<= 4;
            canvas.drawBitmap(patterns, new Rect(c, l, c + 16, l + 16), new Rect(x, y, x + 16, y + 16), null);
            repaint(x, y, 16, 16);
        }

        /** */
        private void repaint(int x, int y, int w, int h) {
            postInvalidate(x / SCALE, y / SCALE, (x + w) / SCALE, (y + h) / SCALE);
        }

        /** */
        public void repaint() {
            postInvalidate();
        }

        /* */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(image, new Rect(0, 0, 640, 400), new RectF(0, 0, 640 / SCALE, 400 / SCALE), null);
        }
    }
}

/* */
