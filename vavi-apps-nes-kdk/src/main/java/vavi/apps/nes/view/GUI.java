/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JOptionPane;

import vavi.apps.nes.Messenger;
import vavi.apps.nes.NES;
import vavi.apps.nes.controller.GUIKeyListener;
import vavi.apps.nes.controller.GUIMenuMonitor;
import vavi.apps.nes.controller.GUIMouseListener;
import vavi.apps.nes.dao.GUILoadRomFileFilter;


/**
 * Class for the GUI required by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class GUI extends Frame implements NES.View {
    //
    // Main System Components
    //

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
     * The Canvas Object for Drawing.
     */
    private Canvas canvas = new Canvas();

    /**
     * The Graphics Object of the Canvas.
     */
    private Graphics graphics;

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

    //
    // Main Menu System
    //

    /**
     * The File Menu.
     */
    private Menu menuFile = new Menu("File");

    /**
     * The CPU Menu.
     */
    private Menu menuCPU = new Menu("Nes");

    /**
     * The View Menu.
     */
    private Menu menuView = new Menu("View");

    //
    // Sub Menu System
    //

    /**
     * File Open Menu item.
     */
    protected MenuItem fileMenu1 = new MenuItem("Load Rom...");

    /**
     * File Close Menu item.
     */
    protected MenuItem fileMenu2 = new MenuItem("Close Rom");

    /**
     * Exit NESCafe Menu item.
     */
    protected MenuItem fileMenu3 = new MenuItem("Exit NESCafe");

    /**
     * Reset Rom Menu item.
     */
    protected MenuItem cpuMenu1 = new MenuItem("Reset ROM");

    /**
     * Pause Rom Menu item.
     */
    protected CheckboxMenuItem cpuMenu2 = new CheckboxMenuItem("Pause ROM", false);

    /**
     * For allowing the Undocumented OpCodes.
     */
    protected CheckboxMenuItem cpuMenu5 = new CheckboxMenuItem("Allow Undocumented OpCodes", false);

    /**
     * Controller 1 Joypad Menu
     */
    protected CheckboxMenuItem cpuMenu6 = new CheckboxMenuItem("Enable Light Gun", false);

    //
    // Class Methods
    //

    /**
     * Create a new Graphical User Interface.
     */
    public GUI() {
    }

    /**
     * Create the Menu Bar.
     */
    private final MenuBar createMenu() {
        // Create the Menu Monitor
        GUIMenuMonitor menuMonitor = new GUIMenuMonitor(this);
        // Add the Menu Monitor to the File Menu Items
        fileMenu1.addActionListener(menuMonitor);
        fileMenu2.addActionListener(menuMonitor);
        fileMenu3.addActionListener(menuMonitor);
        // Add the Menu Monitor to the CPU Menu Items
        cpuMenu1.addActionListener(menuMonitor);
        // Set Command Names for Menu Items
        fileMenu1.setActionCommand("OPEN");
        fileMenu2.setActionCommand("CLOSE");
        fileMenu3.setActionCommand("EXIT");
        cpuMenu1.setActionCommand("RESET");
        // Construct File Menu
        menuFile.add(fileMenu1);
        menuFile.add(fileMenu2);
        menuFile.addSeparator();
        menuFile.addSeparator();
        menuFile.addSeparator();
        menuFile.add(fileMenu3);
        // Construct CPU Menu
        menuCPU.add(cpuMenu1);
        menuCPU.add(cpuMenu2);
        menuCPU.addSeparator();
        menuCPU.add(cpuMenu6);
        menuCPU.addSeparator();
        menuCPU.add(cpuMenu5);
        menuCPU.addSeparator();
        // Construct View Menu
        menuView.addSeparator();
        // Create the Menu Bar and Return It
        MenuBar mb = new MenuBar();
        mb.add(menuFile);
        mb.add(menuCPU);
        mb.add(menuView);
        return mb;
    }

    /**
     * Initialise and Display the GUI.
     * 
     * @param Nes The current NES Engine.
     */
    public final void init(NES nes) {
        // Set Pointers
        this.nes = nes;
        // Create the Layout
        setLayout(new BorderLayout());
        // Set up the Frame
        setResizable(true);
        setTitle("NESCafe Emulator v" + NESCafe.version);
        setBackground(Color.black);
        setForeground(Color.black);
        // Add KeyListeners to Canvas and Frame
        GUIKeyListener keylistener = new GUIKeyListener(nes, this);
        GUIMouseListener mouselistener = new GUIMouseListener(this);
        // Add Key Listener to Canvas
        canvas.addKeyListener(keylistener);
        canvas.addMouseListener(mouselistener);
        canvas.addMouseMotionListener(mouselistener);
        // Set Canvas Attribs and Add to Frame
        canvas.setSize(270, 260);
        canvas.setBackground(Color.black);
        // Add Canvas to Screen
        addKeyListener(keylistener);
        add(canvas, BorderLayout.CENTER);
        // Return Screen Size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = 320;
        final int h = 280;
        // Position the Frame at the Center of the Screen
        setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        // Set the Correct Menu
        showMenuNoRom();
        // Show the Frame
        pack();
        setVisible(true);
        // Set Pointer to Graphics Context
        graphics = canvas.getGraphics();
        graphics.setFont(new Font("Helvetica", Font.PLAIN, 10));
        // Create a new TV Controller
        tvController = new TVController(nes, this);
        writeToScreen("Welcome to NESCafe " + NESCafe.version);
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
     * Configure Menu for when no Rom is loaded.
     */
    public final void showMenuNoRom() {
        // Enable and Disable File Menu Items
        fileMenu1.setEnabled(true);
        fileMenu2.setEnabled(false);
        fileMenu3.setEnabled(true);
        // Enable and Disable CPU Menu Items
        cpuMenu1.setEnabled(false);
        cpuMenu2.setEnabled(false);
    }

    /**
     * Configure Menu for when a Rom is loaded.
     */
    public final void showMenuRom() {
        // Enable and Disable File Menu Items
        fileMenu1.setEnabled(true);
        fileMenu2.setEnabled(true);
        fileMenu3.setEnabled(true);
        // Enable and Disable CPU Menu Items
        cpuMenu1.setEnabled(true);
        cpuMenu2.setEnabled(true);
    }

    // 
    // Basic Display Information Gathering Functions
    //

    /**
     * Returns the graphics context of the canvas.
     */
    public final Graphics connect() {
        return graphics;
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
        return canvas.getWidth();
    }

    /**
     * Returns the display height of the canvas.
     */
    public final int getDispHeight() {
        return canvas.getHeight();
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
    public final void writeToScreen(String message) {
        Messenger note = new Messenger(this, message);
        note.start();
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

    public int getGuiLastX() {
        return tvController.guiLastX;
    }

    public int getGuiLastY() {
        return tvController.guiLastY;
    }

    public double getTVFactor() {
        return tvController.tvFactor;
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
            if (cpuMenu2 != null) {
                cpuMenu2.setState(false);
            }
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
        // Toggle Menu
        if (cpuMenu2 != null) {
            tvController.imageCounter = tvController.imageLatchCounter;
            cpuMenu2.setState(nes.cpu.getCPUPause());
        }
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
        double x = (x_ - tvController.guiLastX) / factor;
        double y = (y_ - tvController.guiLastY) / factor;
        // Determine the new X Location of the Mouse
        int newX = (int) x;
        if (newX < 0)
            newX = 0;
        if (newX > 255)
            newX = 255;
        nes.memory.zapperX = newX;
        // Determine the new Y Location of the Mouse
        int newY = (int) y;
        if (newY < 0)
            newY = 0;
        if (newY > 239)
            newY = 239;
        nes.memory.zapperY = newY;
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
        if (nes.cpu.getCPUPause()) {
            cpuMenu2.setState(false);
            nes.cpu.setCPUPause(cpuMenu2.getState());
        }
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
        FileDialog mainSelection = new FileDialog(this, "Open...", FileDialog.LOAD);
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
}
