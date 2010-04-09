/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;

import vavi.apps.nes.NES;
import vavi.apps.nes.dao.DDNBInputStream;


/**
 * Class for the TV Controller used by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
final class TVController implements Runnable, ImageObserver {
    /**
     * Number of Frames drawn since last FPS Count.
     */
    private int actualFrameNum = 0;

    /**
     * The current actual frame rate.
     */
    protected int frameRate = 0;

    /**
     * The current Frame Skip value.
     */
    protected int frameSkip = 0;

    /**
     * The current scanline number.
     */
    protected int scanLine = 0;

    /**
     * Screen Width.
     */
    protected static int screenWidth = 256;

    /**
     * Screen Height.
     */
    protected static int screenHeight = 240;

    /**
     * Whether FPS are currently being shown.
     */
    protected boolean showFPS = false;

    /**
     * The Screen Palette.
     */
    protected int[] palette;

    /**
     * Buffer of Colour Entries for Screen.
     */
    protected int[] videoBuffer = new int[TVController.screenWidth * TVController.screenHeight];

    /**
     * The Off-Screen Image.
     */
    private Image offScreenImage;

    /**
     * The Graphics Object to paint the Image onto.
     */
    private Graphics screen;

    /**
     * The current Graphical User Interface.
     */
    private GUI gui;

    /**
     * The current NES Engine.
     */
    private NES nes;

    /**
     * The NESCafe Standard Logo as an Image
     */
    private Image nescafeImage;

    /**
     * The "Please Wait" Image.
     */
    private Image waitImage;

    /**
     * The "Paused" Image.
     */
    private Image pauseImage;

    /**
     * The "Activate" Image.
     */
    private Image activateImage;

    /**
     * The Frame Counter Thread.
     */
    private Thread counter;

    /**
     * True if the current frame is being skipped.
     */
    private boolean skipThisFrame = false;

    /**
     * The number of frames skipped since last frame draw.
     */
    private int framesSkipped = 0;

    /**
     * Whether the Screen is in Black and White Mode.
     */
    protected boolean BW = false;

    /**
     * The Percentage Tint for the Palette
     */
    protected float tint = 128.0f;

    /**
     * The Percentage Hue for the Palette
     */
    protected float hue = 128.0f;

    /**
     * The GUI Width Last Time we Drawed
     */
    private int guiLastW = 0;

    /**
     * The GUI Height Last Time we Drawed
     */
    private int guiLastH = 0;

    /**
     * The GUI X Last Time we Drawed (for LightGun)
     */
    public int guiLastX = 0;

    /**
     * The GUI Y Last Time we Drawed (for LightGun)
     */
    public int guiLastY = 0;

    /**
     * The Display Factor (for LightGun)
     */
    public double tvFactor = 1;

    /**
     * Applet Graphics Context
     */
    private Graphics appletGraphics = null;

    /**
     * Image Counter Latch Value
     */
    public int imageLatchCounter = 20;

    /**
     * Counter to Allow Images to Flash
     */
    public int imageCounter = imageLatchCounter;

    /**
     * Counter to Allow Images to Flash
     */
    private Image imageBuffer = null;

    /**
     * The Message to Appear in the Status Bar
     */
    private String statusMessage = "";

    /**
     * Whether to remove State Bar message
     */
    private boolean statusBarOff = false;

    /**
     * The Message to Appear in the Top Status Bar
     */
    private String topstatusMessage = "";

    /**
     * Whether to remove Top State Bar message
     */
    private boolean topstatusBarOff = false;

    /**
     * Clean Screen Up when Zero
     */
    private int cleanScreen = 0;

    /**
     * Create a new TVController Object.
     * 
     * @param NES The current NES Machine.
     * @param GUI The current Graphical User Interface.
     */
    public TVController(NES nes, GUI gui) {
        // Declare References to Current NES and GUI
        this.nes = nes;
        this.gui = gui;
        // Connect to the Screen (controlled by the GUI)
        screen = gui.connect();
        // Create the Secondary Buffer (the Offscreen Image)
        int w = TVController.screenWidth;
        int h = TVController.screenHeight;
        offScreenImage = gui.createImage(new MemoryImageSource(w, h, videoBuffer, 0, 256));
        // Set the Palette
        nes.palette.calcPalette(tint, hue, BW, 0);
        palette = nes.palette.palette;
        // Start the Frames per Second Polling
        startFPSPolling();
        // Grab the Graphics
        try {
            nescafeImage = loadImage("/NESCafe.ddnb");
            nescafeImage.flush();
            waitImage = loadImage("/Wait.ddnb");
            waitImage.flush();
            pauseImage = loadImage("/Pause.ddnb");
            pauseImage.flush();
            activateImage = loadImage("/Activate.ddnb");
            activateImage.flush();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        // Clean Up
        System.gc();
    }

    /**
     * Load an Image
     */
    public final Image loadImage(String imgFile) throws Exception {
        // The New Method Reads Directly from JAR File
        Image img = loadImageFromJar(imgFile);
        // Ensure Image is Fully Downloaded
        MediaTracker tracker = new MediaTracker(gui);
        tracker.addImage(img, 0);
        tracker.waitForID(0);
        // Check for Errors
        if (tracker.isErrorID(0))
            return null;
        return img;
    }

    /**
     * Load Image from JAR File
     */
    public Image loadImageFromJar(String name) {
        // Declare Input Stream
        DDNBInputStream in;
        // Declare Read Buffer
        byte[] _array;
        int _array_size;
        // Grab Toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        // Attempt to Read Content
        try {
            // Grab InputStream to Resource
            in = new DDNBInputStream(getClass().getResourceAsStream(name));
            // Configure Input Buffer
            ByteArrayOutputStream bytes;
            bytes = new ByteArrayOutputStream();
            _array_size = 2048;
            _array = new byte[_array_size];
            // Read Data into Byte Array
            int rb;
            while ((rb = in.read(_array, 0, _array_size)) > -1) {
                bytes.write(_array, 0, rb);
            }
            bytes.close();
            _array = bytes.toByteArray();
            in.close();
            // Create Image
            Image image = toolkit.createImage(_array);
            return image;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * Blank the screen (show Waiting Screen).
     */
    public final void blankScreen() {
        // Check that the GUI is valid
        if (gui == null || nes.memory.isNESCafeROM())
            return;
        // Draw the Screen
        if (waitImage == null)
            return;
        waitImage.flush();
        drawImage(waitImage);
    }

    /**
     * Blank the NESCafe Logo
     */
    public final void showNESCafeLogo() {
        // Draw the Screen
        if (nescafeImage == null)
            return;
        nescafeImage.flush();
        drawImage(nescafeImage);
    }

    /**
     * Draw Blank Screen (useful for refreshing status when no ROM loaded)
     */
    public final void drawBlankScreen() {
        drawImage(null);
    }

    /**
     * Draw an Image File and Cater for Applet Mode>
     */
    private final void drawImage(Image img) {
        try {
            // Declare Local Variables
            int h = 256;
            int w = 256;
            int x = 0;
            int y = 0;
            tvFactor = 1;
            // Get Display Dimensions
            h = gui.getDispHeight();
            w = gui.getDispWidth();
            // Adjust Factor
            if (w > 320 && h > 320) {
                double factorw = (w - 20) / 256.0;
                double factorh = (h - 20) / 240.0;
                if (factorw > factorh)
                    tvFactor = factorh;
                else
                    tvFactor = factorw;
            }
            // Check Factor Limit
            if (tvFactor < 1)
                tvFactor = 1;
            if (tvFactor > 2)
                tvFactor = 2;
            // Determine if Screen Size Changed
            boolean sizeChanged = ((h != guiLastH) || (w != guiLastW));
            if (sizeChanged) {
                // Determine how to Center the Screen on the GUI Panel
                x = w / 2 - (int) (tvFactor * TVController.screenWidth) / 2;
                y = h / 2 - (int) (tvFactor * TVController.screenHeight) / 2;
                guiLastX = x;
                guiLastY = y;
            } else {
                // Copy Old Settings
                x = guiLastX;
                y = guiLastY;
            }
            // Check for Image Buffer (for mixing and matching)
            if (imageBuffer == null || sizeChanged) {
                imageBuffer = ((GUI) (nes.gui)).createImage(w, h);
                if (imageBuffer == null)
                    return;
            }
            // Create if we Need to Super-impose Something
            boolean superimpose = sizeChanged || statusBarOff || topstatusBarOff || (statusMessage != null && statusMessage != "") || (topstatusMessage != null && topstatusMessage != "");
            if (superimpose) {
                // Get Graphics Pointer
                Graphics g = imageBuffer.getGraphics();
                // Clean the Screen Borders
                if (cleanScreen == 0) {
                    g.setColor(Color.black);
                    g.fillRect(0, 0, x, h);
                    g.fillRect(x, 0, w - (2 * x), y);
                    g.fillRect(w - x, 0, x, h);
                    g.fillRect(x, h - y, w - (2 * x), y);
                }
                // Blank Screen if Size Changed
                if (sizeChanged || statusBarOff || topstatusBarOff || img == null) {
                    // Set Black to Blank
                    g.setColor(Color.black);
                    // Blank Top
                    if (statusBarOff) {
                        g.fillRect(0, h - 16, w, 16);
                        statusBarOff = false;
                    }
                    // Blank Bottom
                    if (topstatusBarOff) {
                        g.fillRect(0, 0, w, 16);
                        topstatusBarOff = false;
                    }
                }
                // Draw Image
                if (img != null) {
                    if (tvFactor == 1) {
                        if (!g.drawImage(img, x, y, Color.black, null))
                            g.drawImage(img, x, y, Color.black, this);
                    } else {
                        if (!g.drawImage(img, x, y, (int) (256 * tvFactor), (int) (240 * tvFactor), Color.black, null))
                            g.drawImage(img, x, y, (int) (256 * tvFactor), (int) (240 * tvFactor), Color.black, this);
                    }
                }
                // Write Status Message onto Screen
                if (statusMessage != null && statusMessage != "") {
                    g.setColor(new Color(128, 128, 255));
                    g.fillRect(0, h - 16, w - 1, 15);
                    g.setColor(new Color(0, 0, 255));
                    g.drawRect(0, h - 16, w - 1, 15);
                    g.setFont(new Font("Helvetica", Font.PLAIN, 10));
                    g.setColor(new Color(0, 0, 128));
                    g.drawString(statusMessage, 0 + 5, h - 4);
                }
                // Write Status Message onto Screen
                if (topstatusMessage != null && topstatusMessage != "") {
                    g.setColor(new Color(128, 128, 255));
                    g.fillRect(0, 0, w - 1, 15);
                    g.setColor(new Color(0, 0, 255));
                    g.drawRect(0, 0, w - 1, 15);
                    g.setFont(new Font("Helvetica", Font.PLAIN, 10));
                    g.setColor(new Color(0, 0, 128));
                    g.drawString(topstatusMessage, 0 + 5, 12);
                }
                // Dispose of Graphics Context
                g.dispose();
                // Blitz the Screen
                if (!screen.drawImage(imageBuffer, 0, 0, Color.black, null))
                    screen.drawImage(imageBuffer, 0, 0, Color.black, this);
            } else {
                // No Super-Imposing
                Graphics g = screen;
                if (img != null && g != null) {
                    if (tvFactor == 1) {
                        if (!g.drawImage(img, x, y, Color.black, null))
                            g.drawImage(img, x, y, Color.black, this);
                    } else {
                        if (!g.drawImage(img, x, y, (int) (256 * tvFactor), (int) (240 * tvFactor), Color.black, null))
                            g.drawImage(img, x, y, (int) (256 * tvFactor), (int) (240 * tvFactor), Color.black, this);
                    }
                    // Clean the Screen Borders
                    if (cleanScreen == 0) {
                        g.setColor(Color.black);
                        g.fillRect(0, 0, x, h);
                        g.fillRect(x, 0, w - (2 * x), y);
                        g.fillRect(w - x, 0, x, h);
                        g.fillRect(x, h - y, w - (2 * x), y);
                    }
                }
            }
            // Decrease cleanScreen
            cleanScreen--;
            if (cleanScreen < 0)
                cleanScreen = 120;
            // Record Last Width and Height When we Drawed
            guiLastH = h;
            guiLastW = w;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * Set Status Bar Message
     */
    public synchronized void setStatusBar(String message) {
        statusMessage = message;
        statusBarOff = true;
    }

    /**
     * Get Status Bar Message
     */
    public synchronized String getStatusBar() {
        return statusMessage;
    }

    /**
     * Set Top Status Bar Message
     */
    public synchronized void setTopStatusBar(String message) {
        topstatusMessage = message;
        topstatusBarOff = true;
    }

    /**
     * Get Top Status Bar Message
     */
    public synchronized String getTopStatusBar() {
        return topstatusMessage;
    }

    /**
     * Clear Pixel Information and Display Black Screen.
     */
    public final void deleteDisplay() {
        // Check that the GUI is valid
        // if (gui == null) return;
        // Delete the Data in the Video Buffer
        for (int i = 0; i < videoBuffer.length; i++)
            videoBuffer[i] = 0xFF000000;
        // Flush the Object to Prepare it for Display
        offScreenImage.flush();
        drawImage(offScreenImage);
    }

    /**
     * Blitz the Screen to Canvas.
     */
    private final void blitzScreen() {
        // Check for Applet Mode and No Focus
        // If in Paused Mode then Display Paused Screen
        if (nes.cpu.getCPUPause() && !nes.memory.isNESCafeROM()) {
            if (pauseImage == null) {
                return;
            }
            if (imageCounter-- > 5) {
                pauseImage.flush();
                drawImage(pauseImage);
            } else {
                nescafeImage.flush();
                drawImage(nescafeImage);
                if (imageCounter < 0)
                    imageCounter = imageLatchCounter;
            }
            return;
        }
        // If in WaitMode then Draw "Please Wait" Screen
        if (gui.waitMode && !nes.memory.isNESCafeROM()) {
            if (waitImage == null) {
                deleteDisplay();
            } else {
                waitImage.flush();
                drawImage(waitImage);
            }
            return;
        }
        // If no Cart is loaded then Blank the Screen
        if (!nes.isCartRunning()) {
            deleteDisplay();
            return;
        }
        // Check if Halted (such as in NESCafe Punchout Special Edition)
        if (nes.cpu.isCPUHalted()) {
            showNESCafeLogo();
            return;
        }
        // Draw the Screen from the Offscreen Buffer
        offScreenImage.flush();
        drawImage(offScreenImage);
    }

    /**
     * Draws the Screen.
     * 
     * @param force Forces a draw despite frame-skipping.
     */
    public final void drawScreen(boolean force) {
        if (!isSkipFrame() | force)
            blitzScreen();
        actualFrameNum++;
    }

    /**
     * Grab Applet Graphics Context
     * 
     * @param force Forces a draw despite frame-skipping.
     */
    public final void passAppletGraphics(Graphics g) {
        if (appletGraphics == null) {
            appletGraphics = g.create();
        }
    }

    /**
     * Hides Frame per Second count.
     */
    public final void fpsHide() {
        showFPS = false;
        gui.writeToTopBar("");
    }

    /**
     * Displays Frame per Second count.
     */
    public final void fpsShow() {
        showFPS = true;
        gui.writeToTopBar("Calculating FPS...");
    }

    /**
     * Set the Scanline Manually
     */
    public final boolean setScanLineNum(int sl) {
        // Set the Scanline
        scanLine = sl;
        // Check if we have reached a new Frame
        if (scanLine == 0) {
            // Assume we are going to Skip and increment Counter
            framesSkipped++;
            // Check if we should actually skip this Frame
            if (framesSkipped <= frameSkip) {
                skipThisFrame = true;
            } else {
                framesSkipped = 0;
                skipThisFrame = false;
            }
        }
        // Check for Palette Changes
        if (nes.palette.changed) {
            palette = nes.palette.palette;
            nes.palette.changed = false;
        }
        return skipThisFrame;
    }

    /**
     * Determine if the current frame is being skipped.
     * 
     * @return True if the frame is being skipped.
     */
    private final boolean isSkipFrame() {
        return skipThisFrame;
    }

    /**
     * The FPS Poller Thread.
     */
    public void run() {
        while (true) {
            // Reset the Frame Number to 0
            actualFrameNum = 0;
            // Sleep for 5 seconds
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            // Calculate the Frame Rate
            if (showFPS) {
                frameRate = (actualFrameNum) / 5;
                gui.writeToTopBar("Running at " + frameRate + " FPS");
            }
        }
    }

    /**
     * Sets the frame skip for the TV Controller.
     * 
     * @param value The number of frames to skip before drawing.
     */
    public final void setFrameSkip(int value) {
        frameSkip = value;
    }

    /**
     * Set a pixel on Screen.
     * 
     * @param x The x-coordinate on the current scanline.
     * @param palEntry The palette entry number.
     */
//    public final void setPixel(int x, int palEntry) {
//        // Set Pixel in Video Buffer
//        videoBuffer[(scanLine << 8) | x] = palette[palEntry % 0x40];
//    }

    /**
     * Sets an Array of Pixels representing the current Scanline.
     */
    public final void setPixels(int[] palEntries) {
        // Check Not First Line
        if (scanLine == 0)
            return;
        // Check Length
        if (palEntries.length < 256)
            return;
        // Set Pixels
        for (int x = 0; x < 256; x++) {
            videoBuffer[(scanLine << 8) | x] = palette[palEntries[x]];
        }
    }

    /**
     * Returns a Pixel at the given Point.
     */
    public final int getPixel(int x, int y) {
        // Determine the Offset
        int offset = (y << 8) | x;
        // Check Range
        if (offset >= videoBuffer.length)
            return 0;
        // Return the Pixel
        return videoBuffer[offset];
    }

    /**
     * Starts FPS Polling.
     */
    private final void startFPSPolling() {
        counter = new Thread(this);
        counter.start();
    }

    /**
     * Increment the Tint.
     */
    public final void incTint() {
        // Increment the Tint Value
        tint += 5;
        if (tint > 0xFF)
            tint = 0xFF;
        // Calculate the Palette
        nes.palette.calcPalette(tint, hue, BW, nes.memory.ppu.REG_2001);
        // Display the Selected Tint Value as a Percentage
        int percent = (int) ((tint / 256.0f) * 100);
        gui.writeToScreen("Tint = " + percent + "%");
    }

    /**
     * Decrement the Tint.
     */
    public final void decTint() {
        // Decrement the Tint Value
        tint -= 5;
        if (tint < 0)
            tint = 0x00;
        // Calculate the Palette
        nes.palette.calcPalette(tint, hue, BW, nes.memory.ppu.REG_2001);
        // Display the Selected Tint Value as a Percentage
        int percent = (int) ((tint / 256.0f) * 100);
        gui.writeToScreen("Tint = " + percent + "%");
    }

    /**
     * Increment the Hue.
     */
    public final void incHue() {
        // Increment the Hue Value
        hue += 5;
        if (hue > 0xFF)
            hue = 0xFF;
        // Calculate the Palette
        nes.palette.calcPalette(tint, hue, BW, nes.memory.ppu.REG_2001);
        // Display the Selected Hue Value as a Percentage
        int percent = (int) ((hue / 256.0f) * 100);
        gui.writeToScreen("Hue = " + percent + "%");
    }

    /**
     * Decrement the Hue.
     */
    public final void decHue() {
        // Decrement the Hue Value
        hue -= 5;
        if (hue < 0)
            hue = 0x00;
        // Calculate the Palette
        nes.palette.calcPalette(tint, hue, BW, nes.memory.ppu.REG_2001);
        // Display the Selected Hue Value as a Percentage
        int percent = (int) ((hue / 256.0f) * 100);
        gui.writeToScreen("Hue = " + percent + "%");
    }

    /**
     * Toggle Black and White Mode.
     */
    public final void toggleBlackWhite() {
        // Toggle Black and White Mode
        BW = !BW;
        // Calculate Palette
        nes.palette.calcPalette(tint, hue, BW, nes.memory.ppu.REG_2001);
        // Display Colour Mode
        gui.writeToScreen((BW ? "Black and White Mode" : "Colour Mode"));
    }

    /**
     * Image Observer Interface for Waiting for Dithering and Drawing of Images.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
        // Check if Image Content if Completely Ready Yet
        if ((flags & ALLBITS) != 0) {
            drawImage(img);
        }
        return (flags & (ALLBITS | ABORT | ERROR)) == 0;
    }
}
