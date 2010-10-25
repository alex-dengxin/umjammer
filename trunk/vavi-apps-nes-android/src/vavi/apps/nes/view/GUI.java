/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import vavi.apps.nes.NES;
import vavi.apps.nes.R;
import vavi.apps.nes.controller.GUIKeyListener;
import vavi.apps.nes.controller.GUIMouseListener;


/**
 * Class for the GUI required by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUI extends View implements NES.View {

    /**
     * The current NES Machine.
     */
    protected NES nes;

    /**
     * Cancel any current downloading of ROMs to the GUI.
     */
//    private boolean cancelDownload = false;

    /**
     * The Currently Downloaded File
     */
    private File downloadedFile = null;

    /**
     * The Graphics Object of the Canvas.
     */
    private Paint paint = new Paint();

    /**
     * Signals that the user has requested to Load State.
     */
    private boolean loadStateRequest = false;

    /**
     * Signals that the user has requested to Save State.
     */
    private boolean saveStateRequest = false;

    /**
     * The TV Controller used by the GUI.
     */
    private TVController tvController;

    /**
     * Whether the GUI is displaying a Please Wait screen.
     */
    boolean waitMode = false;

    /** */
    private GUIKeyListener keyListener;

    /** */
    private class MyCartDao implements NES.CartDao {
        @Override
        public InputStream getRomInputStream(String fileName) throws IOException {
            if (fileName == "") {
                return getContext().getResources().openRawResource(R.raw.smb_nes);
            } else {
                return new FileInputStream(fileName);
            }
        }
    }

    /**
     * Initialise and Display the GUI.
     * 
     * @param Nes The current NES Engine.
     */
    public GUI(NESCafe context) {
        super(context);
        // Set Pointers
        this.nes = context.nes;
        nes.setCartDao(new MyCartDao());
        // Add KeyListeners to Canvas and Frame
        keyListener = new GUIKeyListener(nes);
        GUIMouseListener touchListener = new GUIMouseListener(nes);
        // Add Key Listener to Canvas
        setOnTouchListener(touchListener);
        // Set Canvas Attribs and Add to Frame
//        setPreferredSize(new Dimension(256, 240));
        // Add Canvas to Screen
        setFocusable(true);

        tvController = new TVController(this);
        requestFocus();
    }

    /** */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
Log.d("GUI#onKeyDown", String.valueOf(keyCode));
        keyListener.keyPressed(keyCode);
        return super.onKeyDown(keyCode, event);
    }
    
    /** */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
Log.d("GUI#onKeyUp", String.valueOf(keyCode));
        keyListener.keyReleased(keyCode);
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Blank the current screen with Logo.
     */
    public final void showBlankDisplay() {
        if (tvController != null)
            tvController.blankScreen();
    }

    /**
     * Blank the current screen with Logo.
     */
    public final void showNESCafeLogo() {
        if (tvController != null)
            tvController.showNESCafeLogo();
    }

    /**
     * Blank the current screen with Black.
     */
    public final void deleteDisplay() {
        if (tvController != null)
            tvController.deleteDisplay();
    }

    /**
     * Returns the display width of the canvas.
     */
    public final int getDispWidth() {
        return 256 * 2;
    }

    /**
     * Returns the display height of the canvas.
     */
    public final int getDispHeight() {
        return 240 * 2;
    }

    /**
     * Displays the ROM Loading Screen.
     */
    public final void showLoadingScreen(boolean show) {
        waitMode = show;
        if (show)
            tvController.drawScreen(true);
    }

    /**
     * Writes a temporary message to the status bar.
     */
    public final void writeToScreen(final String message) {
        new Thread(new Runnable() {
            public void run() {
                sendMessage(message);
            }
        }).start();
    }

    /**
     * Writes a permanent message to the status bar.
     */
    public final void writeToTopBar(String message) {
        // Display Message in Top Status Bar
        tvController.setTopStatusBar(message);
    }

    public boolean isLoadStateRequest() {
        return loadStateRequest;
    }

    public boolean isSaveStateRequest() {
        return saveStateRequest;
    }

    public void setLoadStateRequest(boolean b) {
        loadStateRequest = b;
    }

    public void setSaveStateRequest(boolean b) {
        saveStateRequest = b;
    }

    public int getScanLine() {
        return tvController.scanLine;
    }

    public void drawScreen(boolean force) {
        tvController.drawScreen(force);
    }

    public void sendMessage(String message) {
        // Display the message in the current GUI
        if (tvController != null) {
            tvController.setStatusBar(message);
            tvController.drawBlankScreen();
        }
        // Sleep for 6 seconds
        try {
            Thread.sleep(6000);
        } catch (Exception e) {
        }
        // Remove the Message from Display
        if (tvController.getStatusBar().equals(message) && tvController != null) {
            tvController.setStatusBar("");
            tvController.drawBlankScreen();
        }
    }

    public boolean getBW() {
        return tvController.BW;
    }

    public float getHue() {
        return tvController.hue;
    }

    public float getTint() {
        return tvController.tint;
    }

    public double getTvFactor() {
        return tvController.tvFactor;
    }

    public int getLastX() {
        return tvController.guiLastX;
    }

    public int getLastY() {
        return tvController.guiLastY;
    }

    public void setPixels(int[] palEntries) {
        tvController.setPixels(palEntries);
    }

    public boolean setScanLineNum(int sl) {
        return tvController.setScanLineNum(sl);
    }

    public void decHue() {
        tvController.decHue();
    }

    public void decTint() {
        tvController.decTint();
    }

    public void incHue() {
        tvController.incHue();
    }

    public void incTint() {
        tvController.incTint();
    }

    public void setFrameSkip(int value) {
        tvController.setFrameSkip(value);
    }

    public void toggleBlackWhite() {
        tvController.toggleBlackWhite();
    }

    public void fireViewEvent(ViewEvent event) {
        String name = event.getName();
        if ("doReset".equals(name)) {
            doReset();
        } else if ("doClose".equals(name)) {
            doClose();
        } else if ("doExit".equals(name)) {
            doExit();
        } else if ("doOpen".equals(name)) {
            doOpen();
        } else if ("doDisableDebug".equals(name)) {
            doDisableDebug();
        } else if ("doEnableDebug".equals(name)) {
            doEnableDebug();
        } else if ("doPauseCart".equals(name)) {
            doPauseCart();
        } else if ("doToggleDebug".equals(name)) {
            doToggleDebug();
        } else if ("doResetCart".equals(name)) {
            doResetCart();
        }
    }

    void doResetCart() {
        nes.reset();
        if (nes.cpu.getCPUPause()) {
            nes.cpu.setCPUPause(false);
        }
    }

    private void doPauseCart() {
        boolean newState = !nes.cpu.getCPUPause();
        if (newState)
            writeToScreen("Pausing game...");
        else
            writeToScreen("Resuming game...");
        nes.cpu.setCPUPause(newState);
    }

    private void doToggleDebug() {
        // Toggle State
        nes.cpu.debugEnterToggle();
    }

    private void doDisableDebug() {
        writeToTopBar("");
        writeToScreen("Debug Mode Exited...");
    }

    private void doEnableDebug() {
        // Check if we Need to Display or Hide FPS
//            tvController.fpsHide();
        // Sort out the Interface
        writeToTopBar("Debug Mode Enabled");
        writeToScreen("");
    }

    private void doReset() {
//        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        nes.reset();
//        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    void doClose() {
//        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        nes.cartUnload();
        deleteDisplay();
//        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        writeToScreen("Rom successfully closed");
    }

    void doExit() {
//        setVisibe(false);
        nes.cartUnload();
        System.exit(0);
    }

    void doOpen() {
        openRomDialog();
    }

    /**
     * The current Graphical User Interface.
     */
    private String lastDirectory = System.getProperty("user.dir");

    /**
     * Open a ROM Image.
     */
    private final void openRomDialog() {
        // Pause the current Display
        nes.pauseSystem(true);
        // Set the Waiting Cursor
//        setCursor(new Cursor(Cursor.WAIT_CURSOR));
//        validate();
        // Perform a Garbage Collection
        System.gc();
        // Display the Open Dialog
//        FileDialog mainSelection = new FileDialog((Frame) null, "Open...", FileDialog.LOAD);
//        mainSelection.setFilenameFilter(new GUILoadRomFileFilter());
//        mainSelection.setFile("*.nes; *.nes.gz; *.zip");
//        mainSelection.setDirectory(lastDirectory);
//        mainSelection.setVisible(true);
//        // Check If User Cancelled
//        if (mainSelection.getFile() == null) {
////            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//            pauseSystem(false);
//            return;
//        }
        // Retrieve the Selected File
//        lastDirectory = mainSelection.getDirectory();
//        String openFilename = mainSelection.getDirectory() + mainSelection.getFile();
        File retFile = new File("openFilename");
        openRom(retFile);
    }

    private final void openRom(File retFile) {
        // Pause the current Display
        nes.pauseSystem(true);
        // Set the Waiting Cursor
//        setCursor(new Cursor(Cursor.WAIT_CURSOR));
//        validate();
        // Perform a Garbage Collection
        System.gc();
        // Attempt to Load File
        showLoadingScreen(true);
        try {
            nes.pauseSystem(true);
//            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            nes.cartLoad(retFile.toString());
            nes.pauseSystem(false);
            nes.lastOpenedCartFileName = retFile.toString();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // Cart didn't Load
            showLoadingScreen(false);
            nes.pauseSystem(false);
        }
        // Change the Cursor to Normal
//        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        System.gc();
    }
    
    /**
     * Show a Message Box.
     * 
     * @param message The message to display.
     */
    public void showDialog(String message) {
        try {
            // Create the Option Pane
            Log.e("NESCafe", message);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void repaint() {
        postInvalidate();
    }
   
    /**
     * Applet Main Paint Method
     */
    public void onDraw(Canvas screen) {
        super.onDraw(screen);
        try {
            // Declare Local Variables
            int h = 256;
            int w = 256;
            int x = 0;
            int y = 0;
            tvController.tvFactor = 1;
            // Get Display Dimensions
            h = getDispHeight();
            w = getDispWidth();
            // Adjust Factor
            if (w > 320 && h > 320) {
                double factorw = (w - 20) / 256.0;
                double factorh = (h - 20) / 240.0;
                if (factorw > factorh)
                    tvController.tvFactor = factorh;
                else
                    tvController.tvFactor = factorw;
            }
            // Check Factor Limit
            if (tvController.tvFactor < 1)
                tvController.tvFactor = 1;
            if (tvController.tvFactor > 2)
                tvController.tvFactor = 2;
            // Determine if Screen Size Changed
            boolean sizeChanged = ((h != tvController.guiLastH) || (w != tvController.guiLastW));
            if (sizeChanged) {
                // Determine how to Center the Screen on the GUI Panel
                x = w / 2 - (int) (tvController.tvFactor * TVController.screenWidth) / 2;
                y = h / 2 - (int) (tvController.tvFactor * TVController.screenHeight) / 2;
                tvController.guiLastX = x;
                tvController.guiLastY = y;
            } else {
                // Copy Old Settings
                x = tvController.guiLastX;
                y = tvController.guiLastY;
            }
            // Check for Image Buffer (for mixing and matching)
            if (tvController.imageBuffer == null || sizeChanged) {
//              imageBuffer = gui.createImage(w, h);
                tvController.imageBuffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                if (tvController.imageBuffer == null)
                    return;
            }
            // Create if we Need to Super-impose Something
            boolean superimpose = sizeChanged || tvController.statusBarOff || tvController.topstatusBarOff || (tvController.statusMessage != null && tvController.statusMessage != "") || (tvController.topstatusMessage != null && tvController.topstatusMessage != "");
            if (superimpose) {
                // Get Graphics Pointer
                Canvas g = new Canvas(tvController.imageBuffer);
                // Clean the Screen Borders
                if (tvController.cleanScreen == 0) {
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);
                    g.drawRect(0, 0, x, h, paint);
                    g.drawRect(x, 0, w - x, y, paint);
                    g.drawRect(w - x, 0, w, h, paint);
                    g.drawRect(x, h - y, w - x, h, paint);
                }
                // Blank Screen if Size Changed
                if (sizeChanged || tvController.statusBarOff || tvController.topstatusBarOff || tvController.img == null) {
                    // Set Black to Blank
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.FILL);
                    // Blank Top
                    if (tvController.statusBarOff) {
                        g.drawRect(0, h - 16, w, h, paint);
                        tvController.statusBarOff = false;
                    }
                    // Blank Bottom
                    if (tvController.topstatusBarOff) {
                        g.drawRect(0, 0, w, 16, paint);
                        tvController.topstatusBarOff = false;
                    }
                }
                // Draw Image
                if (tvController.img != null) {
                    if (tvController.tvFactor == 1) {
//                        if (!g.drawImage(tvController.img, x, y, Color.black, null))
                            g.drawBitmap(tvController.img, x, y, paint);
                    } else {
//                        if (!g.drawImage(tvController.img, x, y, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor), Color.black, null))
                            g.drawBitmap(tvController.img, new Rect(0, 0, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor)), new Rect(x, y, (int) (256 * tvController.tvFactor) + x, (int) (240 * tvController.tvFactor) + y), paint);
                    }
                }
                // Write Status Message onto Screen
                if (tvController.statusMessage != null && tvController.statusMessage != "") {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.rgb(128, 128, 255));
                    g.drawRect(0, h - 16, w - 1, h - 1, paint);
                    paint.setColor(Color.rgb(0, 0, 255));
                    g.drawRect(0, h - 16, w - 1, h - 1, paint);
//                    g.setFont(new Font("Helvetica", Font.PLAIN, 10));
                    paint.setColor(Color.rgb(0, 0, 128));
                    g.drawText(tvController.statusMessage, 0 + 5, h - 4, paint);
                }
                // Write Status Message onto Screen
                if (tvController.topstatusMessage != null && tvController.topstatusMessage != "") {
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.rgb(128, 128, 255));
                    g.drawRect(0, 0, w - 1, 15, paint);
                    paint.setColor(Color.rgb(0, 0, 255));
                    g.drawRect(0, 0, w - 1, 15, paint);
//                    g.setFont(new Font("Helvetica", Font.PLAIN, 10));
                    paint.setColor(Color.rgb(0, 0, 128));
                    g.drawText(tvController.topstatusMessage, 0 + 5, 12, paint);
                }
                // Dispose of Graphics Context
//                g.dispose();
                // Blitz the Screen
//                if (!screen.drawImage(tvController.imageBuffer, 0, 0, Color.black, null))
                    screen.drawBitmap(tvController.imageBuffer, 0, 0, paint);
            } else {
                // No Super-Imposing
                Canvas g = screen;
                if (tvController.img != null && g != null) {
                    if (tvController.tvFactor == 1) {
//                        if (!g.drawImage(tvController.img, x, y, Color.black, null))
                            g.drawBitmap(tvController.img, x, y, paint);
                    } else {
//                        if (!g.drawImage(tvController.img, x, y, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor), Color.black, null))
                            g.drawBitmap(tvController.img, new Rect(0, 0, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor)), new Rect(x, y, (int) (256 * tvController.tvFactor) + x, (int) (240 * tvController.tvFactor) + y), paint);
                    }
                    // Clean the Screen Borders
                    if (tvController.cleanScreen == 0) {
                        paint.setColor(Color.BLACK);
                        paint.setStyle(Paint.Style.FILL);
                        g.drawRect(0, 0, x, h, paint);
                        g.drawRect(x, 0, w - (2 * x), y, paint);
                        g.drawRect(w - x, 0, w, h, paint);
                        g.drawRect(x, h - y, w - x, h, paint);
                    }
                }
            }
            // Decrease cleanScreen
            tvController.cleanScreen--;
            if (tvController.cleanScreen < 0)
                tvController.cleanScreen = 120;
            // Record Last Width and Height When we Drawed
            tvController.guiLastH = h;
            tvController.guiLastW = w;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
