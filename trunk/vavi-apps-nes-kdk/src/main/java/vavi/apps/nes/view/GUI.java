/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Transparency;
import java.io.File;

import javax.swing.JOptionPane;

import com.amazon.kindle.kindlet.ui.KComponent;
import com.amazon.kindle.kindlet.ui.image.ImageUtil;

import vavi.apps.nes.NES;
import vavi.apps.nes.controller.GUIKeyListener;
import vavi.apps.nes.controller.GUIMouseListener;
import vavi.apps.nes.dao.GUILoadRomFileFilter;


/**
 * Class for the GUI required by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUI extends KComponent implements NES.View {

    /**
     * The current NES Machine.
     */
    protected NES nes;

    /**
     * Cancel any current downloading of ROMs to the GUI.
     */
    public boolean cancelDownload = false;

    /**
     * The Currently Downloaded File
     */
    public File downloadedFile = null;

    /**
     * The Graphics Object of the Canvas.
     */
//    private Graphics graphics;

    /**
     * Signals that the user has requested to Load State.
     */
    public boolean loadStateRequest = false;

    /**
     * Signals that the user has requested to Save State.
     */
    public boolean saveStateRequest = false;

    /**
     * The TV Controller used by the GUI.
     */
    protected TVController tvController;

    /**
     * Whether the GUI is displaying a Please Wait screen.
     */
    public boolean waitMode = false;

    /**
     * Initialise and Display the GUI.
     * 
     * @param Nes The current NES Engine.
     */
    public GUI(NES nes) {
        // Set Pointers
        this.nes = nes;
        // Create the Layout
        setLayout(new BorderLayout());
        // Add KeyListeners to Canvas and Frame
        GUIKeyListener keylistener = new GUIKeyListener(nes, this);
        GUIMouseListener mouselistener = new GUIMouseListener(this);
        // Add Key Listener to Canvas
        addKeyListener(keylistener);
        addMouseListener(mouselistener);
        addMouseMotionListener(mouselistener);
        // Set Canvas Attribs and Add to Frame
        setPreferredSize(new Dimension(256, 240));
        // Add Canvas to Screen
        requestFocus();
    }

    public void addNotify() {
        super.addNotify();

        // Set Pointer to Graphics Context
//        graphics = getGraphics().create();
//        graphics.setFont(new Font("Helvetica", Font.PLAIN, 10));
        // Create a new TV Controller
        tvController = new TVController(nes, this);
    }
    
    /**
     * Pauses display and NES Machine.
     */
    public final void pauseSystem(boolean pause) {
        // Checks for Cartridge
        if (!nes.isCartRunning())
            return;
        // Enable/Disable the CPU drawing to the Screen
        nes.cpu.enableDrawScreen(!pause);
        // Enable/Disable the CPU Running
        nes.cpu.setCpuActive(!pause);
        // Blank the Screen
        if (pause)
            showBlankDisplay();
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

    public void onZapper(int x, int y) {
        if (nes != null && nes.memory != null) {
            if (nes.controllerZapperEnabled) {
                calcZapperPos(x, y);
                nes.memory.zapperTrigger = 0x10;
            }
        }
    }

    public void offZapper(int x, int y) {
        if (nes != null && nes.memory != null) {
            if (nes.controllerZapperEnabled) {
                calcZapperPos(x, y);
                nes.memory.zapperTrigger = 0x00;
            }
        }
    }

    /**
     * Calculate Zapper Position
     */
    private void calcZapperPos(int x_, int y_) {
        // Calculate the Offsets
        double factor = tvController.tvFactor;
        nes.setZapperPos((x_ - tvController.guiLastX) / factor, (y_ - tvController.guiLastY) / factor);
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
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        nes.reset();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    void doClose() {
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        nes.cartUnload();
        deleteDisplay();
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        writeToScreen("Rom successfully closed");
    }

    void doExit() {
        setVisible(false);
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
        pauseSystem(true);
        // Set the Waiting Cursor
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        validate();
        // Perform a Garbage Collection
        System.gc();
        // Display the Open Dialog
        FileDialog mainSelection = new FileDialog((Frame) null, "Open...", FileDialog.LOAD);
        mainSelection.setFilenameFilter(new GUILoadRomFileFilter());
        mainSelection.setFile("*.nes; *.nes.gz; *.zip");
        mainSelection.setDirectory(lastDirectory);
        mainSelection.setVisible(true);
        // Check If User Cancelled
        if (mainSelection.getFile() == null) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            pauseSystem(false);
            return;
        }
        // Retrieve the Selected File
        lastDirectory = mainSelection.getDirectory();
        String openFilename = mainSelection.getDirectory() + mainSelection.getFile();
        File retFile = new File(openFilename);
        openRom(retFile);
    }

    private final void openRom(File retFile) {
        // Pause the current Display
        pauseSystem(true);
        // Set the Waiting Cursor
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        validate();
        // Perform a Garbage Collection
        System.gc();
        // Attempt to Load File
        showLoadingScreen(true);
        try {
            pauseSystem(true);
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            nes.cartLoad(retFile.toString());
            pauseSystem(false);
            nes.lastOpenedCartFileName = retFile.toString();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // Cart didn't Load
            showLoadingScreen(false);
            pauseSystem(false);
        }
        // Change the Cursor to Normal
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
            JOptionPane.showMessageDialog(null, message, "NESCafe Emulator", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 
     * Applet Main Paint Method
     * 
     */
    public void paint(Graphics screen) {
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
                tvController.imageBuffer = ImageUtil.createCompatibleImage(w, h, Transparency.OPAQUE);
                if (tvController.imageBuffer == null)
                    return;
            }
            // Create if we Need to Super-impose Something
            boolean superimpose = sizeChanged || tvController.statusBarOff || tvController.topstatusBarOff || (tvController.statusMessage != null && tvController.statusMessage != "") || (tvController.topstatusMessage != null && tvController.topstatusMessage != "");
            if (superimpose) {
                // Get Graphics Pointer
                Graphics g = tvController.imageBuffer.getGraphics();
                // Clean the Screen Borders
                if (tvController.cleanScreen == 0) {
                    g.setColor(Color.black);
                    g.fillRect(0, 0, x, h);
                    g.fillRect(x, 0, w - (2 * x), y);
                    g.fillRect(w - x, 0, x, h);
                    g.fillRect(x, h - y, w - (2 * x), y);
                }
                // Blank Screen if Size Changed
                if (sizeChanged || tvController.statusBarOff || tvController.topstatusBarOff || tvController.img == null) {
                    // Set Black to Blank
                    g.setColor(Color.black);
                    // Blank Top
                    if (tvController.statusBarOff) {
                        g.fillRect(0, h - 16, w, 16);
                        tvController.statusBarOff = false;
                    }
                    // Blank Bottom
                    if (tvController.topstatusBarOff) {
                        g.fillRect(0, 0, w, 16);
                        tvController.topstatusBarOff = false;
                    }
                }
                // Draw Image
                if (tvController.img != null) {
                    if (tvController.tvFactor == 1) {
//                        if (!g.drawImage(tvController.img, x, y, Color.black, null))
                            g.drawImage(tvController.img, x, y, Color.black, null);
                    } else {
//                        if (!g.drawImage(tvController.img, x, y, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor), Color.black, null))
                            g.drawImage(tvController.img, x, y, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor), Color.black, null);
                    }
                }
                // Write Status Message onto Screen
                if (tvController.statusMessage != null && tvController.statusMessage != "") {
                    g.setColor(new Color(128, 128, 255));
                    g.fillRect(0, h - 16, w - 1, 15);
                    g.setColor(new Color(0, 0, 255));
                    g.drawRect(0, h - 16, w - 1, 15);
                    g.setFont(new Font("Helvetica", Font.PLAIN, 10));
                    g.setColor(new Color(0, 0, 128));
                    g.drawString(tvController.statusMessage, 0 + 5, h - 4);
                }
                // Write Status Message onto Screen
                if (tvController.topstatusMessage != null && tvController.topstatusMessage != "") {
                    g.setColor(new Color(128, 128, 255));
                    g.fillRect(0, 0, w - 1, 15);
                    g.setColor(new Color(0, 0, 255));
                    g.drawRect(0, 0, w - 1, 15);
                    g.setFont(new Font("Helvetica", Font.PLAIN, 10));
                    g.setColor(new Color(0, 0, 128));
                    g.drawString(tvController.topstatusMessage, 0 + 5, 12);
                }
                // Dispose of Graphics Context
                g.dispose();
                // Blitz the Screen
//                if (!screen.drawImage(tvController.imageBuffer, 0, 0, Color.black, null))
                    screen.drawImage(tvController.imageBuffer, 0, 0, Color.black, null);
            } else {
                // No Super-Imposing
                Graphics g = screen;
                if (tvController.img != null && g != null) {
                    if (tvController.tvFactor == 1) {
//                        if (!g.drawImage(tvController.img, x, y, Color.black, null))
                            g.drawImage(tvController.img, x, y, Color.black, null);
                    } else {
//                        if (!g.drawImage(tvController.img, x, y, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor), Color.black, null))
                            g.drawImage(tvController.img, x, y, (int) (256 * tvController.tvFactor), (int) (240 * tvController.tvFactor), Color.black, null);
                    }
                    // Clean the Screen Borders
                    if (tvController.cleanScreen == 0) {
                        g.setColor(Color.black);
                        g.fillRect(0, 0, x, h);
                        g.fillRect(x, 0, w - (2 * x), y);
                        g.fillRect(w - x, 0, x, h);
                        g.fillRect(x, h - y, w - (2 * x), y);
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
        super.paint(screen);
    }

    /**
     * 
     * Applet Update Method with No Blanking
     * 
     */
    public void update(Graphics g) {
        paint(g);
    }
}
