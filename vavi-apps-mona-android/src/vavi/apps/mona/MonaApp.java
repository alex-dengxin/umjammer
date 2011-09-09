package vavi.apps.mona;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import vavi.util.mona.BbsBoard;
import vavi.util.mona.BbsData;
import vavi.util.mona.BbsThread;
import vavi.util.mona.Mona;
import vavi.util.mona.Mona.MonaEvent;
import vavi.util.mona.Mona.MonaListener;


public class MonaApp extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gui = new MyView(this);
        setContentView(gui);

        Thread thread = new Thread(gui);
        thread.start();
    }

    /** */
    private MyView gui;

    /** */
    private class MyView extends View implements Runnable {

        private GestureDetector gestureDetector; 

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        /** */
        private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                switch (mode) {
                case MODE_BOARD:
                    boardVC.keyPressed(KeyEvent.KEYCODE_ENTER);
                    return true;
                case MODE_THREAD:
                    threadVC.keyPressed(KeyEvent.KEYCODE_ENTER);
                    return true;
                case MODE_VIEW:
                    viewVC.keyPressed(KeyEvent.KEYCODE_ENTER);
                    return true;
                }
                return super.onDown(event);
            }

            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
                switch (mode) {
                case MODE_BOARD:
                    boardVC.keyPressed(KeyEvent.KEYCODE_DPAD_DOWN);
                    return true;
                case MODE_THREAD:
                    threadVC.keyPressed(KeyEvent.KEYCODE_DPAD_DOWN);
                    return true;
                case MODE_VIEW:
                    viewVC.keyPressed(KeyEvent.KEYCODE_DPAD_DOWN);
                    return true;
                }
                return super.onFling(event1, event2, velocityX, velocityY);
            }
        };

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            switch (mode) {
            case MODE_BOARD:
                boardVC.keyPressed(keyCode);
                break;
            case MODE_THREAD:
                threadVC.keyPressed(keyCode);
                break;
            case MODE_VIEW:
                viewVC.keyPressed(keyCode);
                break;
            }
            postInvalidate();

            return super.onKeyUp(keyCode, event);
        }

        /** */
        class BoardVC {

            /** model */
            private List<BbsBoard> boards;

            void setModel(List<BbsBoard> boards) {
                this.boards = boards;
                line = 0;
                page = 0;
            }

            private static final int LINES = 10;

            private int line = 0;
            private int page = 0;

            int getLineCount() {
                return Math.min(LINES, boards.size() - page * LINES);
            }
            
            int getPageCount() {
                return (boards.size() + LINES - 1) / LINES;
            }

            int getCurrentIndex() {
                return page * LINES + line;
            }

            int getIndex(int i) {
                return page * LINES + i;
            }

            void keyPressed(int keyCode) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    line--;
                    if (line < 0) {
                        line = getLineCount() - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    page--;
                    if (page < 0) {
                        page = getPageCount() - 1;
                    }
                    if (line > getLineCount() - 1) {
                        line = getLineCount() - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    page++;
                    if (page >= getPageCount()) {
                        page = 0;
                    }
                    if (line > getLineCount() - 1) {
                        line = getLineCount() - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    line++;
                    if (line >= getLineCount()) {
                        line = 0;
                    }
                    break;
                case KeyEvent.KEYCODE_ENTER: // ○
                    try {
                        String boardName = boards.get(getCurrentIndex()).getName();
                        BbsBoard board = mona.getBoardByName(boardName);
                        mona.setTargetBoard(board);
                        threadVC.setModel(board.getThreads());
                        mode = MODE_THREAD;
System.err.println("MODE: THREAD");
                    } catch (Exception e) {
e.printStackTrace(System.err);
status = "5:" + e;                        
                    }
                    break;
                case 461:               // □
                case KeyEvent.KEYCODE_SPACE:
                case KeyEvent.KEYCODE_BACK:
                    break;
                }    
//System.err.println("boardLine: " + boardLine + "/" + (getLineCount() - 1) + ", " + boardPage + "/" + (getPageCount() - 1));
            }

            /** */
            void paint(Canvas g) {
                paint.setColor(Color.BLACK);
                paint.setStyle(Style.FILL);
                g.drawRect(0, 0, getWidth(), getHeight(), paint);

                paint.setColor(Color.YELLOW);
                String boardMode = "● 板選択 " + (page + 1) + "/" + getPageCount();
                g.drawText(boardMode, fontSize, fontSize * 2, paint); 

                for (int i = 0; i < getLineCount(); i++) {
                    String cursor;
                    if (line == i) {
                        paint.setColor(Color.BLUE);
                        cursor = "＞";
                    } else {
                        paint.setColor(Color.WHITE);
                        cursor = "　";
                    }
                    BbsBoard board = boards.get(getIndex(i));
                    String boardName = cursor + board.getName();
                    g.drawText(boardName, fontSize, i * fontSize * 2 + fontSize * 4, paint); 
                }
            }
        }

        class ThreadVC {

            /** model */
            private List<BbsThread> threads;

            private static final int LINES = 10;

            private int line = 0;
            private int page = 0;

            int getLineCount() {
                return Math.min(LINES, threads.size() - page * LINES);
            }
            
            int getPageCount() {
                return (threads.size() + LINES - 1) / LINES;
            }

            int getCurrentIndex() {
                return page * LINES + line;
            }

            int getIndex(int i) {
                return page * LINES + i;
            }

            void setModel(List<BbsThread> threads) {
                this.threads = threads;
                line = 0;
                page = 0;
            }

            void keyPressed(int keyCode) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    line--;
                    if (line < 0) {
                        line = getLineCount() - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    page--;
                    if (page < 0) {
                        page = getPageCount() - 1;
                    }
                    if (line > getLineCount() - 1) {
                        line = getLineCount() - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    page++;
                    if (page >= getPageCount()) {
                        page = 0;
                    }
                    if (line > getLineCount() - 1) {
                        line = getLineCount() - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    line++;
                    if (line >= getLineCount()) {
                        line = 0;
                    }
                    break;
                case KeyEvent.KEYCODE_ENTER: // ○
                    try {
                        BbsThread thread = threads.get(getCurrentIndex());
                        mona.setTargetThread(thread);
                        mona.startLive();
    
                        Thread viewThread = new Thread(viewVC);
                        viewThread.start();
    
                        mode = MODE_VIEW;
System.err.println("MODE: VIEW");
                    } catch (Exception e) {
status = "4:" + e;
                    }
                    break;
                case KeyEvent.KEYCODE_SPACE:
                case KeyEvent.KEYCODE_BACK:
                    mode = MODE_BOARD;
System.err.println("MODE: BOARD");
                    break;
                }    
//System.err.println("threadLine: " + threadLine + "/" + (getLineCount() - 1) + ", " + threadPage + "/" + (getPageCount() - 1));
            }

            void paint(Canvas g) {
                paint.setColor(Color.BLACK);
                paint.setStyle(Style.FILL);
                g.drawRect(0, 0, getWidth(), getHeight(), paint);

                paint.setColor(Color.YELLOW);
                String threadMode = "● スレ選択 " + (page + 1) + "/" + getPageCount();
                g.drawText(threadMode, fontSize, fontSize * 2, paint); 

                for (int i = 0; i < getLineCount(); i++) {
                    String cursor;
                    if (line == i) {
                        paint.setColor(Color.BLUE);
                        cursor = "＞";
                    } else {
                        paint.setColor(Color.WHITE);
                        cursor = "　";
                    }
                    BbsThread thread = threads.get(getIndex(i));
//                    String dateFormat = "yyyy/MM/dd HH:mm:ss";
                    String threadName = cursor + thread.getTitle() + " (" + thread.getResponses() + ")" /* + "         " + new SimpleDateFormat(dateFormat).format(new Date(thread.sinse)) */;
                    g.drawText(threadName, fontSize, i * fontSize * 2 + fontSize * 4, paint); 
                }
            }
        }

        /** */
        class ViewVC implements MonaListener, Runnable {

            private boolean viewLoop;

            void keyPressed(int keyCode) {
                switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    // TODO back scroll とか
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (wait <= 950) {
                        wait += 50;
                    }
//System.err.println("wait: " + wait);
status = "wait: " + wait;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (wait > 50) {
                        wait -= 50;
                    }
//System.err.println("wait: " + wait);
status = "wait: " + wait;
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    break;
                case KeyEvent.KEYCODE_ENTER: // ○
                    break;
                case KeyEvent.KEYCODE_SPACE:
                case KeyEvent.KEYCODE_BACK:
                    try {
                        if (!mona.isLiveStopped()) {
                            mona.stopLive();
                            // TODO find next thread
                        }
                        threadVC.setModel(mona.getThreads());
                        viewLoop = false;
                        mode = MODE_THREAD;
System.err.println("MODE: THREAD");
                    } catch (Exception e) {
e.printStackTrace(System.err);
status = "3:" + e;                        
                    }
                    break;
                }    
            }

            /** model */
            private List<String> lines = new ArrayList<String>();

            /* */
            public void whenThreadUpdated(MonaEvent event) {
                List<BbsData> data = event.getBbsDatum();
                for (int i = 0; i < data.size(); i++) {
                    BbsData datum = data.get(i);
//System.err.println(data.toStringAsFormated() + "\n");
                    StringTokenizer lines = new StringTokenizer(datum.toStringAsFormated(), "\n");
                    while (lines.hasMoreTokens()) {
                        String line = lines.nextToken();
                        this.lines.add(line);
                    }
                }
            }

            /* */
            public void whenMessageUpdated(MonaEvent event) {
                status = event.getMessage();
            }

            /* */
            public void whenThreadEnd(MonaEvent event) {
            }

            /* */
            public void debug(Throwable t) {
                throwable = t;
            }

            /* */
            public void debug(String message) {
                status = message;
            }

            /** off screen image */
            private Bitmap image;
            /** Canvas for off screen image */
            private Canvas graphics;

            /** */
            int currentLine = 0;
            /** */
            long wait = 500;

            /* */
            public void run() {
System.err.println("SCROLL: start"/* + getHeight() / (fontSize * 2) */);
                viewLoop = true;
                lines.clear();
                int page = getHeight() / (fontSize * 2);

                paint.setColor(Color.BLACK);
                paint.setStyle(Style.FILL);
                graphics.drawRect(0, 0, getWidth(), getHeight(), paint);
                postInvalidate();

                while (viewLoop) {
                    try {
    
                        while (viewLoop && lines.size() > 0) {
                            String line = lines.get(0);
                            lines.remove(0);

                            if (currentLine < page - 1) {
                                currentLine++;
                            } else {
                                graphics.drawBitmap(image, new Rect(0, 0, getWidth(), getHeight()), new Rect(0, -fontSize * 2, getWidth(), -fontSize * 2 + getHeight()), null);
//Log.d("AndroidMona", new Rect(0, 0, getWidth(), getHeight()) + ", " + new Rect(0, -fontSize * 2, getWidth(), -fontSize * 2 + getHeight()) + ", " + getWidth() + "x" + getHeight());
                            }
                            paint.setColor(Color.BLACK);
                            paint.setStyle(Style.FILL);
                            graphics.drawRect(0, currentLine * fontSize * 2, getWidth(), (currentLine + 1) * fontSize * 2, paint);
                            if (line.charAt(0) != ' ') {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.WHITE);
                            }
                            graphics.drawText(line, 0, currentLine * fontSize * 2, paint);
                            if (lines.size() < 100) {
                                postInvalidate();
                                Thread.sleep(wait);
                            }
                        }

                        Thread.sleep(1000);
                    } catch (Exception e) {
e.printStackTrace(System.err);
status = "1:" + e;                        
                    }
                }
System.err.println("SCROLL: stop");
            }

            /** */
            void paint(Canvas g) {
                g.drawBitmap(image, 0, 0, null);
            }

            /** */
            void initImage(int w, int h) {
Log.d("AndroidMona", "bitmap: " + w + "x" + h);
                this.image = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                this.graphics = new Canvas(image);
            }
        }

        /** engine */
        private Mona mona;

        // models

        private static final int MODE_INIT = 0;
        private static final int MODE_BOARD = 1;
        private static final int MODE_THREAD = 2;
        private static final int MODE_VIEW = 3;

        /** */
        private int mode = MODE_INIT;

        // view/controllers

        /** */
        private BoardVC boardVC;
        /** */
        private ThreadVC threadVC;
        /** */
        private ViewVC viewVC;

        // view attributes

        /** */
        private int fontSize = 12;

        /** */
        private Paint paint = new Paint();

        /** */
        private String status = "";
        Throwable throwable;

        /** */
        MyView(Context context) {
            super(context);

            setFocusable(true);

            gestureDetector = new GestureDetector(context, simpleOnGestureListener); 

            boardVC = new BoardVC();
            threadVC = new ThreadVC();
            viewVC = new ViewVC();
        }

        /* */
        public void run() {
            // 1.
            try {
                mona = new Mona();
                boardVC.setModel(mona.getBoardsByCategory("実況ch"));

                mona.addMonaListener(viewVC);
            } catch (Exception e) {
e.printStackTrace(System.err);
status = e.toString();
            }

            // 2.
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
Log.d("AndroidMona", "Display#orientation: " + display.getOrientation());
            if (display.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                viewVC.initImage(display.getHeight(), display.getWidth());
            } else {
                viewVC.initImage(display.getWidth(), display.getHeight());
            }

            // 3.
            mode = MODE_BOARD;
System.err.println("MODE: BOARD");
postInvalidate();
        }

        /* */
        public void onDraw(Canvas g) {
            super.onDraw(g);

            switch (mode) {
//case MODE_INIT:
// g.setColor(Color.pink);
// g.fillRect(0, 0, getWidth(), getHeight());
// g.setColor(Color.blue);
// g.drawString("BD-J Mona (c) 2008 VAVI", 36, 36); 
// g.drawString(status, 36, 60); 
// break;
            case MODE_BOARD:
                boardVC.paint(g);
                break;
            case MODE_THREAD:
                threadVC.paint(g);
                break;
            case MODE_VIEW:
                viewVC.paint(g);
                break;
            }

paint.setColor(Color.DKGRAY);
if (throwable != null) {
 try {
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  PrintStream ps = new PrintStream(baos);
  throwable.printStackTrace(ps);
  String stackTraces = baos.toString();
  StringTokenizer st = new StringTokenizer(stackTraces, "\n");
  int i = 0;
  paint.setColor(Color.RED);
  while (st.hasMoreTokens()) {
   g.drawText(st.nextToken(), 0, fontSize * i + 36, paint);
   i++;
  }
 } catch (Exception e) {
  String x = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L + "KB/" + Runtime.getRuntime().totalMemory() / 1024L + "KB" + ": 6: " + e;
  g.drawText(x, 0, getHeight() - 36, paint);
 }
 throwable = null;
} else {
 String x = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L + "KB/" + Runtime.getRuntime().totalMemory() / 1024L + "KB" + ": " + status;
 g.drawText(x, 0, getHeight() - 36, paint);
}
        }
    }
}