/*
 * This file is part of J2ME VNC.
 *
 * Copyright (c) 2003 Michael Lloyd Lee
 */

package tk.wetnet.j2me.vnc;

import com.nttdocomo.ui.AudioPresenter;
import com.nttdocomo.ui.Button;
import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Component;
import com.nttdocomo.ui.ComponentListener;
import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.ImageLabel;
import com.nttdocomo.ui.KeyListener;
import com.nttdocomo.ui.Label;
import com.nttdocomo.ui.ListBox;
import com.nttdocomo.ui.Panel;
import com.nttdocomo.ui.TextBox;
import com.nttdocomo.util.Timer;
import com.nttdocomo.util.TimerListener;

import tk.wetnet.util.Queue;
import tk.wetnet.vnc.DrawToable;

import vavi.microedition.rms.RecordEnumeration;
import vavi.microedition.rms.RecordStore;


/**
 * VNCCanvas.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040911 nsano initial version <br>
 */
public class VNCCanvas extends Canvas
    implements ComponentListener, KeyListener, DrawToable, Runnable {

    /** */
    VNC midlet;

    /** */
    private AudioPresenter ap = AudioPresenter.getAudioPresenter();

    /** */
    private class Timeout implements TimerListener {
        public void timerExpired(Timer source) {
            nextLetter();
            canvas.repaint();
//            serviceRepaints();
        }
    }

    /** */
    private com.nttdocomo.util.Timer timer = new com.nttdocomo.util.Timer();
    /** */
    private Timeout timeout = null;
    /** */
    private boolean activeRefresh = false;
    /** */
    private Image redraw = null;
    /** */
    private Image mouse = null;
    /** */
    private Image sms = null;

    /** */
    protected void tick() {
        if (activeRefresh && !update) {
            try {
                midlet.rfb.requestUpdate(offx, offy, getWidth(), getHeight(), true);
            } catch (Throwable t) {
                error("Active Refresh: " + t.toString());
            }
        }
    }

    /** */
    private int scrollby = (getWidth() / 5);
    /** */
    private int scrollbyY = (getHeight() / 5);
    /** */
    private Image tmpImg;
    /** */
    private Image buffer;
    /** */
    private Image zoomOut;
    /** */
    private Graphics g = buffer.getGraphics();
    /** */
    private Graphics gzoomout = zoomOut.getGraphics();
    /** */
    private int offx;
    /** */
    private int offy;
    /** */
    private int mouseMoveBy = 1;
    /** */
    private int viewMode = NORMAL;
    /** */
    private static final int NORMAL = 0;
    /** */
    private static final int ZOOM_OUT = 1;
    /** */
    private int numberKeyPadMode = QUICKEN;
    /** */
    private static final int QUICKEN = 0;
    /** */
    private static final int SMS_MODE = 1;
    /** */
    private static final int NUMBERITICAL_MODE = 2;
    /** */
    private int gameKeyMode = SCREEN_MOVE_MODE;
    /** */
    private static final int SCREEN_MOVE_MODE = 0;
    /** */
    private static final int MOUSE_MODE = 1;
    /** */
    private static final int ARROW_KEY_MODE = 2;
    /** */
    private Button modeCmd = new Button("#mode#");
    /** */
    private Button refreshCmd = new Button("#refresh#");
    /** */
    private Button moveMouseCmd = new Button("#call_mouse#");
    /** */
    private Button textCmd = new Button("#enter_text#");
    /** */
    private Button modeOKCmd = new Button("#ok#");
    /** */
    private Button modeOKCmd_sendString = new Button("#ok#");
    /** */
    private Button modeOKCmd_options = new Button("#ok#");
    /** */
    private Button exitCmd = new Button("#exit#");
    /** */
    private Button optionsCmd = new Button("#options#");
    /** */
    private Button cancelCmd = new Button("#cancel#");
    /** */
    private Button altCmd = new Button("#alt#");
    /** */
    private Button metaCmd = new Button("#meta#");
    /** */
    private Button ctrlCmd = new Button("#control#");
    /** */
    private Button caseCmd = new Button("#case#");
    /** */
    private Button enterCmd = new Button("#enter#");
    /** */
    private boolean alt = false;
    /** */
    private boolean meta = false;
    /** */
    private boolean ctrl = false;
    /** */
    private String[] viewModes = { "#normal#", "#full_screen#" };
    /** */
    private String[] gameKeyModes = {
        "#navigation#", "#mouse_mode#", "#arrow_keys#" };
    /** */
    private String[] numberKeyPadModes = {
        "#quicken#", "#sms_mode#", "#numlock#" };
    /** "#view#",, viewModes, null */
    private ListBox viewCh = new ListBox(ListBox.SINGLE_SELECT);
    /** "#game_keys#", , gameKeyModes, null */
    private ListBox gameKeysCh = new ListBox(ListBox.SINGLE_SELECT);
    /** "#number_keys#", , numberKeyPadModes, null */
    private ListBox numberKeyPadCh = new ListBox(ListBox.SINGLE_SELECT);
    /** */
    private TextBox sendStringBox =
        new TextBox("", 255, 1, TextBox.DISPLAY_ANY);
    /** */
    private Panel options = new Panel();
    /** */
//    private Gauge scrollX = new Gauge("#scroll_amount#", true, 4, 0);
    /** */
//    private Gauge scrollY = new Gauge( "#scrolly#", true, 4, 0 );

    /** */
//    private Gauge mouseMove = new Gauge("#mouse_move#", true, 20, 1);
    /** */
    private ListBox aR = new ListBox(ListBox.CHECK_BOX | ListBox.MULTIPLE_SELECT);
    /** */
    private Panel modeForm = new Panel();
    /** */
    private Thread worker = new Thread(this);
    /** */
    private boolean running = true;
    /** */
    private boolean localCursor = false;
    /** */
    private int requests = 0;

    /** */
    private Panel sendStringForm = new Panel();

    /** */
    RecordStore rs;

    Panel main = new Panel();
    
    /** */
    VNCCanvas() {
    }

    /** */
    VNCCanvas(VNC m) {
        midlet = m;
        main.add(metaCmd);
        main.add(ctrlCmd);
        main.add(altCmd);
        main.add(modeCmd);
        main.add(moveMouseCmd);
        main.add(refreshCmd);
        main.add(textCmd);
        main.add(exitCmd);
        main.add(optionsCmd);
        main.add(enterCmd);
        main.add(m.log);

        greyOut(g, 0, 0, getWidth(), getHeight());
        greyOut(gzoomout, 0, 0, getWidth(), getHeight());

        aR.append("#activerefresh#");
        aR.append("#local_cursor#");

//  	options.add(scrollX);
//  	options.add(scrollY);
        options.add(moveMouseCmd);
        options.add(aR);
        options.add(modeOKCmd_options);
        options.add(cancelCmd);

        sendStringForm.add(sendStringBox);
        sendStringForm.add(modeOKCmd_sendString);
        sendStringForm.add(enterCmd);
        sendStringForm.add(cancelCmd);
        sendStringForm.setKeyListener(this);

        main.setComponentListener(this);

        modeForm.add(viewCh);
        modeForm.add(gameKeysCh);
        modeForm.add(numberKeyPadCh);
        modeForm.add(modeOKCmd);
        modeForm.add(cancelCmd);
        modeForm.setKeyListener(this);

        options.setKeyListener(this);

        for (int i = 0; i < 5; i++) {
            eventCashe.push(new Event());
        }

        try {
            // "/redraw.png"
            redraw = Image.createImage(0, 0, null, 0);

            // "/mouse.png"
            mouse = Image.createImage(0, 0, null, 0);
            // "/key.png"
            sms = Image.createImage(0, 0, null, 0);
        } catch (Throwable t) {
System.err.println("Create Image: key: " + t);
        }

        timer.setListener(new Ticker(this)); // , 2000, 1500

//VNC.log("hasRepeatEvents() " + hasRepeatEvents());

        try {
            rs = RecordStore.openRecordStore("cmds", true);

            if (rs.getNumRecords() == 0) {
                byte[] s = "Ctrl Alt Del|<\\c<\\a!\\d>\\a>\\c".getBytes();
                rs.addRecord(s, 0, s.length);
            }

            RecordEnumeration re = rs.enumerateRecords(null, null, false);

            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                String current = new String(rs.getRecord(id));
                String title = current;

                if (current.indexOf("|") > 0) {
                    title = current.substring(0, current.indexOf("|"));
                }

                Button cm = new Button(title);
                midlet.hostCommands.put(cm, new Integer(id));
                main.add(cm);
            }
        } catch (Exception t) {
System.err.println("VNC() : t " + t.toString());
t.printStackTrace();
        }

        worker.start();
    }

    /** */
    private int mouseX = 0;
    /** */
    private int mouseY = 0;

    /** */
    public void componentAction(Component source, int type, int param) {
VNC.log("key: " + source);

        if (source == refreshCmd) {
            try {
                if (viewMode == ZOOM_OUT) {
                    midlet.rfb.requestUpdate(0, 0, midlet.rfb.x, midlet.rfb.y, false);
                } else {
                    midlet.rfb.requestUpdate(offx, offy, getWidth(), getHeight(), false);
                }
            } catch (Throwable t) {
                error("cmd Refresh" + t);
            }

            canvas.repaint();
        } else if (source == optionsCmd) {
            Display.setCurrent(options);
        } else if (source == metaCmd) {
            meta = !meta;

            try {
                midlet.rfb.key(0xffe7, meta);
            } catch (Throwable t) {
                error(t.toString());
            }
        } else if (source == altCmd) {
            alt = !alt;

            try {
                midlet.rfb.key(0xffe9, alt);
            } catch (Throwable t) {
                error(t.toString());
            }
        } else if (source == ctrlCmd) {
            ctrl = !ctrl;

            try {
                midlet.rfb.key(0xffe3, ctrl);
            } catch (Throwable t) {
                error(t.toString());
            }
        } else if (source == exitCmd) {
            close();
            midlet.terminate();
        } else if (source == textCmd) {
            Display.setCurrent(sendStringForm);
        } else if (source == modeCmd) {
            Display.setCurrent(modeForm);
        } else if (source == cancelCmd) {
            // Reset settings.
            sendStringBox.setText((String) null);
            gameKeysCh.select(gameKeyMode);
            numberKeyPadCh.select(numberKeyPadMode);
            viewCh.select(viewMode);
            Display.setCurrent(this);
        } else if (source == caseCmd) {
            uppercase = !uppercase;
        } else if (source == enterCmd) {
            try {
                if (source == sendStringBox) {
                    midlet.rfb.sendString(sendStringBox.getText() + "\n");
                } else {
                    midlet.rfb.sendString("\n");
                }
            } catch (Throwable t) {
                error("Sending Enter: " + t.toString());
            }

            Display.setCurrent(this);
        } else if (source == modeOKCmd) {
            gameKeyMode = gameKeysCh.getSelectedIndex();
            numberKeyPadMode = numberKeyPadCh.getSelectedIndex();
            viewMode = viewCh.getSelectedIndex();
            
            if (numberKeyPadMode == SMS_MODE) {
                main.add(caseCmd);
            } else {
//                main.remove(caseCmd);
            }
            Display.setCurrent(this);
        } else if (source == modeOKCmd_sendString) {
            try {
                midlet.rfb.sendString(sendStringBox.getText());
            } catch (Throwable t) {
                error("modeOK: sendString: " + t);
            }
            Display.setCurrent(this);
        } else if (source == modeOKCmd_options) {
//                scrollby = (scrollX.getValue() + 1) * (getWidth() / 5);
//                scrollbyY = (scrollX.getValue() + 1) * (getHeight() / 5);
//                mouseMoveBy = mouseMove.getValue();
            
            if (mouseMoveBy == 0) {
                mouseMoveBy = 1;
            }
            
            activeRefresh = aR.isIndexSelected(0);
            localCursor = aR.isIndexSelected(1);

            Display.setCurrent(this);
        } else if (source == moveMouseCmd) {
            try {
                mouseX = offx + (getWidth() / 2);
                mouseY = offy + (getHeight() / 2);
                midlet.rfb.mouse(mouseX, mouseY, 0);
                midlet.rfb.requestUpdate(mouseX, mouseY, 32, 32, true);
            } catch (Throwable t) {
                error("cmd Refresh" + t);
            }
        } else if (source == midlet.log) {
            midlet.componentAction(midlet.log, type, param);
        } else if (source instanceof Button /* HostCommand */) {
            Button cmd = (Button) source;
            String tmp;

            try {
                int id = ((Integer) midlet.hostCommands.get(cmd)).intValue();
                tmp = new String(rs.getRecord(id));
            } catch (Throwable t) {
                tmp = "|";
            }

            try {
                stringToKeys(tmp.substring(tmp.indexOf("|") + 1, tmp.length()));
            } catch (Throwable t) {
                error("Custom Command: " + t);
            }
        }

        requests--;

        if (requests <= 0) {
            requests = 0;
        }

        canvas.repaint();
//        serviceRepaints();
    }

    /** */
    public void keyPressed(Panel p, int key) {
        Event e = null;

        requests++;
        canvas.repaint();

        if (eventCashe.isEmpty()) {
            e = new Event();
        } else {
            e = (Event) eventCashe.pop();
        }

        boolean notify = events.isEmpty();
        e.mode = Event.COMMAND_ACTION;
        e.panel = p;
        e.keyCode = key;

        events.push(e);

        if (notify) {
            try {
                worker.notifyAll();
            } catch (IllegalMonitorStateException i) {
System.err.println(i.toString());
            }
        }
    }

    /** */
    private boolean uppercase = true;
    /** */
    private byte lastPressed = -1;
    /** */
    private byte keyCounter = -1;
    /** */
    private char[][] keys = {
        { '0', ' ' },
        { '.', ',', '!', '1' },
        { 'A', 'B', 'C', '2' },
        { 'D', 'E', 'F', '3' },
        { 'G', 'H', 'I', '4' },
        { 'J', 'K', 'L', '5' },
        { 'M', 'N', 'O', '6' },
        { 'P', 'Q', 'R', 'S', '7' },
        { 'T', 'U', 'V', '8' },
        { 'W', 'X', 'Y', 'Z', '9' },
        { '#' },
        { '*' }
    };

    /** */
    protected void close() {
        try {
            rs.closeRecordStore();
        } catch (Throwable t) {
            error(t.toString());
        }

        try {
            running = false;
            worker.notify();

            if (meta) {
                try {
                    midlet.rfb.key(0xffe7, false);
                } catch (Throwable t) {
                    error(t.toString());
                }
            }

            if (alt) {
                try {
                    midlet.rfb.key(0xffe9, false);
                } catch (Throwable t) {
                    error(t.toString());
                }
            }

            if (ctrl) {
                try {
                    midlet.rfb.key(0xffe3, false);
                } catch (Throwable t) {
                    error(t.toString());
                }
            }

            midlet.rfb.running = false;
            midlet.rfb.sin.close();
            midlet.rfb.sout.close();
        } catch (Throwable t) {
            error(t.toString());
        }
    }

    /** */
    private void nextLetter() {
        try {
            if (uppercase) {
                midlet.rfb.key(keys[lastPressed][keyCounter], true);
                midlet.rfb.key(keys[lastPressed][keyCounter], false);
            } else {
                midlet.rfb.key(Character.toLowerCase(keys[lastPressed][keyCounter]),
                               true);
                midlet.rfb.key(Character.toLowerCase(keys[lastPressed][keyCounter]),
                               false);
            }
        } catch (Throwable tt) {
            error("keyPressed: tt: " + tt);
        }

        keyCounter = -1;
        lastPressed = -1;

        try {
            midlet.rfb.requestUpdate(offx, offy, getWidth(), getHeight(), true);
        } catch (Throwable t) {
            error("keyPressed: " + t);
        }
    }

    /** */
    private Queue events = new Queue();
    /** */
    private Queue eventCashe = new Queue();

    /** */
    public void run() {
        while (running) {
            while (!events.isEmpty() && running) {
                Event e = (Event) events.pop();

                switch (e.mode) {
                case Event.KEY_PRESSED:
                    kp(e.keyCode);

                    break;
                case Event.COMMAND_ACTION:
                    midlet.componentAction(e.button, e.keyCode, 0);
                    e.button = null;
                    e.panel = null;

                    break;
                }

                eventCashe.push(e);
            }

            try {
                synchronized (worker) {
                    worker.wait(1000);
                }
            } catch (InterruptedException e) {
System.err.println(e);
            } catch (IllegalMonitorStateException e) {
System.err.println(e);
            }
        }
    }

    /** */
    private class Event extends tk.wetnet.util.Queue.QueueEntry {
        private static final int KEY_PRESSED = 0;
        private static final int COMMAND_ACTION = 1;
        private int mode = KEY_PRESSED;
        private int keyCode = 0;
        private Button button = null;
        private Panel panel = null;
    }

    /** Todo: Add scroll mode, where it grays out screen, then adds
     * A small box to represent the current location, and you move that
     * then it refresh the screen to that location.
     * MOD: Fish eye mode?
     * WAS: keyPressed
     */
    public void keyReleased(Panel p, int keyCode) {
        Event e = null;

        requests++;
        canvas.repaint();

        if (eventCashe.isEmpty()) {
            e = new Event();
        } else {
            e = (Event) eventCashe.pop();
        }

        boolean notify = events.isEmpty();
        e.mode = Event.KEY_PRESSED;
        e.keyCode = keyCode;
        events.push(e);

        if (notify) {
            try {
                worker.notifyAll();
            } catch (IllegalMonitorStateException i) {
System.err.println(i.toString());
            }
        }
    }

    /** */
    public void keyRepeated(int keyCode) {
        if (gameKeyMode == MOUSE_MODE) {
            switch (getKeypadState(keyCode)) {
            case Display.KEY_DOWN:
                mouseY += mouseMoveBy;
                break;
            case Display.KEY_UP:
                mouseY -= mouseMoveBy;
                break;
            case Display.KEY_LEFT:
                mouseX -= mouseMoveBy;
                break;
            case Display.KEY_RIGHT:
                mouseX += mouseMoveBy;
                break;
            }

            if (mouseX < 0) {
                mouseX = 0;
            } else if (mouseX > midlet.rfb.x) {
                mouseX = midlet.rfb.x;
            }

            if (mouseY < 0) {
                mouseY = 0;
            } else if (mouseY > midlet.rfb.y) {
                mouseY = midlet.rfb.y;
            }
        }

        canvas.repaint();
//        serviceRepaints();
    }

    /** */
    private void kp(int keyCode) {
System.out.println(keyCode);
System.out.println((char) keyCode);

        // don't like. Rewrite.
        if (((keyCode > 0) &&
            !((keyCode == Display.KEY_1) || (keyCode == Display.KEY_2) ||
            (keyCode == Display.KEY_3) || (keyCode == Display.KEY_4) ||
            (keyCode == Display.KEY_5) || (keyCode == Display.KEY_6) ||
            (keyCode == Display.KEY_7) || (keyCode == Display.KEY_8) ||
            (keyCode == Display.KEY_9) || (keyCode == Display.KEY_0) ||
            (keyCode == Display.KEY_ASTERISK) || (keyCode == Display.KEY_POUND)) &&
            (getKeypadState(keyCode) <= 0)) ||
            ((numberKeyPadMode == NUMBERITICAL_MODE) &&
            ((keyCode == Display.KEY_1) || (keyCode == Display.KEY_2) ||
            (keyCode == Display.KEY_3) || (keyCode == Display.KEY_4) ||
            (keyCode == Display.KEY_5) || (keyCode == Display.KEY_6) ||
            (keyCode == Display.KEY_7) || (keyCode == Display.KEY_8) ||
            (keyCode == Display.KEY_9) || (keyCode == Display.KEY_0) ||
            (keyCode == Display.KEY_ASTERISK) || (keyCode == Display.KEY_POUND)))) {
            try {
                midlet.rfb.key(keyCode, true);
                midlet.rfb.key(keyCode, false);
                midlet.rfb.requestUpdate(offx, offy, getWidth(), getHeight(),
                                         true);
            } catch (Throwable t1) {
                error("keyPressed: t1: " + t1);
            }

            requests--;

            if (requests <= 0) {
                requests = 0;
            }

            canvas.repaint();
//            serviceRepaints();

            return;
        }

        /* I'm not happy with this if statement.
           It does not allow for phones double up on the keypad/arrow keys.
           I don't think many exist. But I need to allow for this. I believe
           a good refactoring is int order.
         */
        if ((keyCode == Display.KEY_1) || (keyCode == Display.KEY_2) ||
            (keyCode == Display.KEY_3) || (keyCode == Display.KEY_4) ||
            (keyCode == Display.KEY_5) || (keyCode == Display.KEY_6) ||
            (keyCode == Display.KEY_7) || (keyCode == Display.KEY_8) ||
            (keyCode == Display.KEY_9) || (keyCode == Display.KEY_0) ||
            (keyCode == Display.KEY_ASTERISK) || (keyCode == Display.KEY_POUND)) {
            switch (numberKeyPadMode) {
            case SMS_MODE:

                byte t = -1;

                if ((keyCode >= 48) && (keyCode <= 57)) {
                    t = (byte) (keyCode - 48);
                } else if (keyCode == Display.KEY_ASTERISK) {
                    t = 11;
                } else if (keyCode == Display.KEY_POUND) {
                    t = 10;
                }

                if (t == -1) {
                    return;
                }

                if ((t != lastPressed) && (lastPressed != -1)) {
                    try {
                        if (uppercase) {
                            midlet.rfb.key(keys[lastPressed][keyCounter], true);
                            midlet.rfb.key(keys[lastPressed][keyCounter], false);
                        } else {
                            midlet.rfb.key(Character.toLowerCase(keys[lastPressed][keyCounter]),
                                           true);
                            midlet.rfb.key(Character.toLowerCase(keys[lastPressed][keyCounter]),
                                           false);
                        }
                    } catch (Throwable tt) {
                        error("keyPressed: tt: " + tt);
                    }

                    keyCounter = -1;
                } else {
                    if (timeout != null) {
                        timer.stop();
                    }

                    timeout = new Timeout();
                    timer.setListener(timeout); // 2000
                    timer.start();
                }

                lastPressed = t;
                keyCounter++;

                if (keyCounter == keys[lastPressed].length) {
                    keyCounter = 0;
                }

                break;
            case QUICKEN:

                if (gameKeyMode == SCREEN_MOVE_MODE) {
System.out.println((char) keyCode);
                    offy = 0;
                    offx = 0;

                    switch (keyCode) {
                    case Display.KEY_1:
                        offy = midlet.rfb.y - getWidth();
                        break;
                    case Display.KEY_2:
                        offy = midlet.rfb.y - getWidth();
                        offx = (midlet.rfb.x / 2) - (getHeight() / 2);
                        break;
                    case Display.KEY_3:
                        offy = midlet.rfb.y - getWidth();
                        offx = midlet.rfb.x - getHeight();
                        break;
                    case Display.KEY_4:
                        offx = (midlet.rfb.x / 2) - (getHeight() / 2);
                        break;
                    case Display.KEY_5:
                        offy = (midlet.rfb.y / 2) - (getHeight() / 2);
                        offx = (midlet.rfb.x / 2) - (getWidth() / 2);
                        break;
                    case Display.KEY_6:
                        offx = midlet.rfb.x - getWidth();
                        offy = (midlet.rfb.y / 2) - (getHeight() / 2);
                        break;
                    case Display.KEY_7:
                        break;
                    case Display.KEY_8:
                        offx = (midlet.rfb.x / 2) - (getWidth() / 2);
                        break;
                    case Display.KEY_9:
                        offx = midlet.rfb.x - getWidth();
                        offy = midlet.rfb.y - getHeight();
                        break;
                    }

                    greyOut(g, offx, offy, getWidth(), getHeight());

                    try {
                        midlet.rfb.requestUpdate(offx, offy, getWidth(),
                                                 getHeight(), false);
                    } catch (Throwable t4) {
                        error("keyPressed: t4: " + t4);
                    }

                    canvas.repaint();
//                    serviceRepaints();
                } else if (gameKeyMode == MOUSE_MODE) {
                }

                break;
            }
        } else {
            int sb = 0;

            switch (gameKeyMode) {
            case SCREEN_MOVE_MODE:
                Image i = buffer;
                tmpImg.getGraphics().drawImage(i, 0, 0);

                switch (getKeypadState()) {
                case Display.KEY_LEFT:

                    if (viewMode == NORMAL) {
                        sb = scrollby;
                    } else if (viewMode == ZOOM_OUT) {
                        sb = getWidth();
                    }

                    if ((offx - sb) < 0) {
                        sb = offx;
                    }

                    offx -= sb;

                    g.drawImage(tmpImg, sb, 0);

                    greyOut(g, 0, 0, sb, getHeight());

                    try {
                        midlet.rfb.requestUpdate(offx, offy, sb, getHeight(),
                                                 false);
                    } catch (Throwable t4) {
                        error("keyPressed: t4: " + t4);
                    }

                    break;
                case Display.KEY_RIGHT:

                    if (viewMode == NORMAL) {
                        sb = scrollby;
                    } else if (viewMode == ZOOM_OUT) {
                        sb = getWidth();
                    }

                    if ((offx + sb) > (midlet.rfb.x - getWidth())) {
                        sb = (midlet.rfb.x - getWidth()) - offx;
                        System.out.println(sb);
                    }

                    offx += sb;
                    g.drawImage(tmpImg, -sb, 0);

                    greyOut(g, getWidth() - sb, 0, scrollby, getHeight());

                    try {
                        midlet.rfb.requestUpdate((offx + getWidth()) - sb,
                                                 offy, sb, getHeight(), false);
                    } catch (Throwable t5) {
                        error("keyPressed: t5: " + t5);
                    }

                    break;
                case Display.KEY_UP:

                    if (viewMode == NORMAL) {
                        sb = scrollbyY;
                    } else if (viewMode == ZOOM_OUT) {
                        sb = getHeight();
                    }

                    if ((offy - sb) < 0) {
                        sb = offy;
                    }

                    offy -= sb;
                    g.drawImage(tmpImg, 0, sb);

                    greyOut(g, 0, 0, getWidth(), sb);

                    try {
                        midlet.rfb.requestUpdate(offx, offy, getWidth(), sb,
                                                 false);
                    } catch (Throwable t6) {
                        error("keyPressed: t6: " + t6);
                    }

                    break;
                case Display.KEY_DOWN:

                    if (viewMode == NORMAL) {
                        sb = scrollbyY;
                    } else if (viewMode == ZOOM_OUT) {
                        sb = getHeight();
                    }

                    if ((offy + sb) > (midlet.rfb.y - getHeight())) {
                        System.out.println(sb);
                        sb = (midlet.rfb.y - getHeight()) - offy;
                        System.out.println(sb + " " + offy + " " +
                                           (midlet.rfb.y - getHeight()) + " " +
                                           (offy + sb));
                    }

                    offy += sb;
                    g.drawImage(tmpImg, 0, -sb);

                    greyOut(g, 0, getHeight() - sb, getWidth(), sb);

                    try {
                        midlet.rfb.requestUpdate(offx,
                                                 (offy + getHeight()) - sb,
                                                 getWidth(), sb, false);
                    } catch (Throwable t1) {
                        error("keyPressed: t1: " + t1);
                    }

                    break;
                }

                break;
            case MOUSE_MODE:
                int buts = 0;
                switch (getKeypadState()) {
                case Display.KEY_DOWN:
                    mouseY += mouseMoveBy;
                    break;
                case Display.KEY_UP:
                    mouseY -= mouseMoveBy;
                    break;
                case Display.KEY_LEFT:
                    mouseX -= mouseMoveBy;
                    break;
                case Display.KEY_RIGHT:
                    mouseX += mouseMoveBy;
                    break;
                case Display.KEY_SELECT:
                    buts = 1;
                    break;
                case Display.KEY_SOFT1:
                    buts = 2;
                    break;
                }

                if (mouseX < 0) {
                    mouseX = 0;
                } else if (mouseX > midlet.rfb.x) {
                    mouseX = midlet.rfb.x;
                }

                if (mouseY < 0) {
                    mouseY = 0;
                } else if (mouseY > midlet.rfb.y) {
                    mouseY = midlet.rfb.y;
                }

                if (!localCursor || (buts > 0)) {
VNC.log("mousex: " + mouseX + "mouseY" + mouseX);

                    try {
                        midlet.rfb.mouse(mouseX, mouseY, ((buts > 0) ? 1 : 0));

                        if (buts != 0) {
                            midlet.rfb.mouse(mouseX, mouseY, 0);

                            if (buts == 2) {
                                midlet.rfb.mouse(mouseX, mouseY, 1);
                                midlet.rfb.mouse(mouseX, mouseY, 0);
                            }
                        }

                        midlet.rfb.requestUpdate(offx, offy, getWidth(),
                                                 getHeight(), true);
                    } catch (Throwable t7) {
                        error("MouseMove: " + t7);
                    }
                }

                break;
            case ARROW_KEY_MODE:
                int key = -1;
                switch (getKeypadState()) {
                case Display.KEY_LEFT:
                    key = 0xff51;
                    break;
                case Display.KEY_UP:
                    key = 0xff52;
                    break;
                case Display.KEY_RIGHT:
                    key = 0xff53;
                    break;
                case Display.KEY_DOWN:
                    key = 0xff54;
                    break;
                case Display.KEY_SELECT:
                    key = 0xff0d;
                    break;
                }

                try {
                    midlet.rfb.key(key, true);
                    midlet.rfb.key(key, false);
                } catch (Throwable t1) {
                    error("keyPressed: t1: " + t1);
                }

                break;
            }
        }

        requests--;

        if (requests <= 0) {
            requests = 0;
        }

        canvas.repaint();
//        serviceRepaints();
    }

    /** */
    private void greyOut(Graphics gr, int x, int y, int w, int h) {
        g.setColor(Graphics.WHITE);
        g.fillRect(x, y, w, h);

        for (int i = w - 1; i > 0; i -= 2) {
            for (int f = h - 1; f > 0; f -= 2) {
                gr.setColor(Graphics.GRAY);
                gr.fillRect(x + i, y + f, 1, 1);
            }
        }
    }

    /** */
    private boolean update = false;

    /** */
    public void startUpdate() {
        update = true;
        canvas.repaint();
//        serviceRepaints();
    }

    /** */
    public void endUpdate() {
        update = false;
        canvas.repaint();
//        serviceRepaints();
    }

    /** */
    public void ringBell() {
        ap.play();
    }

    /** */
    public void copyRect(int x, int y, int w, int h, int srcx, int srcy) {
VNC.log("copyRect");

//  	if ((x > offx) && (x < offx + getWidth())
//              && (y > offy) && (y < offy + getHeight())) {
//              Image i = Image.createImage(w, h);
//              i.getGraphics().drawImage(buffer, x - offx, y - offy);
//              gr.drawImage(i, srcx, srcy);
//          }
    }

    /** */
    private int divx = 0;
    /** */
    private int divy = 0;

    /** */
    public void draw(int red, int green, int blue, int x, int y, int w, int h) {
        gzoomout.setColor((red << 16) | (green << 8) | blue);

        if ((w >= divx) || (h >= divy)) {
            gzoomout.fillRect(((x == 0) ? 0 : (x / divx)),
                              ((y == 0) ? 0 : (y / divy)),
                              ((w == 0) ? 1 : ((w / divx) + 1)),
                              ((h == 0) ? 1 : ((h / divy) + 1)));
        } else if (((x % divx) == 0) && ((y % divy) == 0)) {
            gzoomout.fillRect(((x == 0) ? 0 : (x / divx)),
                              ((y == 0) ? 0 : (y / divy)), 1, 1);
        }

        gzoomout.setColor((red << 16) | (green << 8) | blue);
        g.fillRect(x - offx, y - offy, w, h);
    }

    /** */
    protected void pointerPressed(int x, int y) {
        mouseX = offx + x;
        mouseY = offy + y;

        try {
            midlet.rfb.mouse(mouseX, mouseY, 1);
            midlet.rfb.mouse(mouseX, mouseY, 0);
            midlet.rfb.requestUpdate(mouseX, mouseY, 32, 32, true);
        } catch (Throwable t) {
            error("MouseMove: " + t);
        }
    }

    /** */
    private Canvas canvas = new Canvas() {
        public void paint(Graphics gr) {
            switch (viewMode) {
                case ZOOM_OUT:
                gr.drawImage(zoomOut, 0, 0);
                gr.setColor(Graphics.GREEN);

                gr.drawRect(((offx == 0) ? 0 : (offx / divx)),
                            ((offy == 0) ? 0 : (offy / divy)), getWidth() / divx,
                            getHeight() / divy);

                break;
                default:
                gr.drawImage(buffer, 0, 0);

                if (gameKeyMode == MOUSE_MODE) {
                    gr.setColor(Graphics.RED);
                    gr.drawRect(mouseX - offx - 1, mouseY - offy - 1, 2, 2);
                }

                break;
            }

            if (update || (requests > 0)) {
                gr.drawImage(redraw, getWidth(), getHeight()); // bottom, right
            }

            if (gameKeyMode == MOUSE_MODE) {
                gr.drawImage(mouse, getWidth(), getHeight() - 16); // bottom, right
            }

            if (numberKeyPadMode == SMS_MODE) {
                gr.drawImage(sms, getWidth() - 16, getHeight()); // bottom, right
            }

            if ((numberKeyPadMode == SMS_MODE) && (lastPressed != -1)) {
                gr.setColor(Graphics.BLACK);
                gr.fillRect(0, getHeight() - Font.getDefaultFont().getHeight(),
                            Font.getDefaultFont().stringWidth(String.valueOf(keys[lastPressed][keyCounter])),
                            Font.getDefaultFont().getHeight());
                gr.setColor(Graphics.WHITE);

                if (uppercase) {
                    gr.drawChars(keys[lastPressed], 0,
                                 getHeight() - Font.getDefaultFont().getHeight(), 0, keyCounter);
                } else { // TODO
                    gr.drawChars(keys[lastPressed],
                                 0, getHeight() - Font.getDefaultFont().getHeight(), 0, keyCounter);
                }
            }
        }
    };

    /** */
    public void error(String error) {
System.out.print(error);

        Panel form = new Panel();

        try {
            form.add(new ImageLabel(midlet.aboutImage));
        } catch (Throwable t) {
t.printStackTrace();
        }

        form.add(new Label(error));
        form.add(exitCmd);
        form.add(midlet.log);
        form.setComponentListener(this);
        Display.setCurrent(form);
    }

    /** */
    public void ready() {
        Display.setCurrent(this);
        divx = (midlet.rfb.x / getWidth()) + 1;
        divy = (midlet.rfb.y / getHeight()) + 1;

        if (divx < divy) {
            divx = divy;
        }

        if (divy < divx) {
            divy = divx;
        }

        try {
            midlet.rfb.requestUpdate(0, 0, getWidth(), getHeight(), false);
        } catch (Throwable t) {
            error("ready: " + t);
        }
    }

    /** */
    public void incConnectionStatus() {
        midlet.incConnectionStatus();
    }

    /**
     * ?move this too the About box (with the "hidden" options)
     * ?and on this side have a machine friendly, human unreadable format?
     * ? harder to then parse back for editing.
     *
     *
     * cmd (action)
     * i.e.
     * !h!e!l!l!e - sends hello.
     * <\c<\a!\d>\a>\c - ctrl alt delete.
     *
     *
     * letter combos:
     * x literal.
     * \d Delete
     * \a Alt
     * \m Meta
     * \c Control
     * \n newline
     * \\ \
     * \p |
     *
     *
     * cmds:
     * < key down
     * > key up
     * ! key down then up (ie press)
     *
     */
    private void stringToKeys(String s) throws Throwable {
        try {
            int len = s.length();

            for (int i = 0; i < len; i++) {
                if ((s.charAt(i) == '!') || (s.charAt(i) == '>') ||
                    (s.charAt(i) == '<')) {
                    char cmd = s.charAt(i);
                    i++;

                    int send = s.charAt(i);

                    if (s.charAt(i) == '\\') {
                        i++;

                        switch (s.charAt(i)) {
                        case '\\':
                            send = '\\';
                            break;
                        case 'n':
                            send = '\n';
                            break;
                        case 'p':
                            send = '|';
                            break;
                        case 'd':
                            send = 0xFFFF;
                            break;
                        case 'a':
                            send = 0xFFE9;
                            break;
                        case 'm':
                            send = 0xFFE7;
                            break;
                        case 'c':
                            send = 0xFFE3;
                            break;
                        // TODO the rest! :>
                        default:
                            send = s.charAt(i);
                        }
                    }

                    switch (cmd) {
                    case '!':
                        midlet.rfb.key(send, true);
                        midlet.rfb.key(send, false);

                        break;
                    case '>':
                        midlet.rfb.key(send, false);

                        break;
                    case '<':
                        midlet.rfb.key(send, true);

                        break;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            ap.play();
            Dialog dialog = new Dialog(Dialog.DIALOG_ERROR, "#index_out_title#");
            dialog.setText("#index_out_start#" + e.getMessage() + "#index_out_end#");
            dialog.show();
        }

        midlet.rfb.requestUpdate(offx, offy, getWidth(), getHeight(), true);
    }

    /** */
    public void paint(Graphics g) {
        // TODO Auto-generated method stub
        
    }
}

/* */
