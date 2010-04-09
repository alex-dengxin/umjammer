/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Class for the NES Machine used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class NES {

    public static interface View {
        class ViewEvent {
            String name;
            public ViewEvent(Object source, String name) {
                this.name = name;
            }
            public String getName() {
                return name;
            }
        }

        void fireViewEvent(ViewEvent event);

        int getScanLine();

        void showLoadingScreen(boolean b);

        void writeToScreen(String string);

        void setTitle(String string);
        
        boolean isLoadStateRequest();

        boolean isSaveStateRequest();

        void setSaveStateRequest(boolean b);

        void setLoadStateRequest(boolean b);

        void showMenuNoRom();

        void showMenuRom();

        void deleteDisplay();

        void drawScreen(boolean force);

        void sendMessage(String message);

        boolean getBW();

        float getHue();

        float getTint();

        void setPixels(int[] linePalettes);

        boolean setScanLineNum(int currentScanline);

        void setFrameSkip(int i);

        void toggleBlackWhite();

        void decTint();

        void incTint();

        void decHue();

        void incHue();

        void showDialog(String message);

        void onZapper(int x, int y);

        void offZapper(int x, int y);
    }

    // TODO
    public boolean controllerZapperEnabled;

    /**
     * Controller 1 Joypad.
     */
    public JoyPad joyPad1;

    /**
     * Controller 2 Joypad.
     */
    public JoyPad joyPad2;

    /**
     * The Central Processing Unit (CPU).
     */
    public CPU cpu;

    /**
     * The Graphical User Interface (GUI).
     */
    public NES.View gui;

    /**
     * The Memory Mapper.
     */
    protected Mapper mapper;

    /**
     * The Memory Manager.
     */
    public MemoryManager memory;

    /**
     * The Picture Processing Unit (PPU).
     */
    public PPU ppu;

    /**
     * The current Palette.
     */
    public Palette palette = new Palette();

    /**
     * Whether or not a Cartridge is currently loaded.
     */
    private boolean noCartLoaded = true;

    /**
     * The Filename of the current Cartridge.
     */
    protected String currentCartFileName = "";

    /**
     * The Filename of the last Opened Cartridge.
     */
    public String lastOpenedCartFileName = ".";

    /**
     * Last Recorded Error Message
     */
    protected String errorMessage = "";

    /**
     * Frame IRQ
     */
    public int frameIRQEnabled = 0xFF;

    /**
     * Frame IRQ Disenable
     */
    public int frameIRQDisenabled = 0;

    /**
     * Register Debugger
     */
    public boolean regdebug = false;

    /**
     * Create a new Nintendo Machine.
     */
    public NES() {
        noCartLoaded = true;
        currentCartFileName = "";
    }

    /**
     * Initialise the Nintendo Machine.
     * 
     * @param theGUI The Graphical User Interface in use.
     */
    public final void init(NES.View gui) {
        // Grab Reference to the Current Graphical User Interface
        this.gui = gui;
        // Create Controller 1
        joyPad1 = new JoyPad(JoyPad.JOYPAD_1);
        // Create Controller 2
        joyPad2 = new JoyPad(JoyPad.JOYPAD_2);
        // Create a CPU
        cpu = new CPU(this, gui);
        // Create a PPU
        ppu = new PPU(this, gui);
        // Create a Memory Manager
        memory = new MemoryManager(this, gui);
    }

    /**
     * Load a cartridge and start it running.
     * 
     * @param fileName The filename of the cartridge to load.
     * @return True if the Cartridge was successfully loaded.
     */
    public final boolean cartLoad(File fileName) {
        if (fileName == null || fileName.equals(""))
            return cartLoadDefault();
        return cartLoad(fileName.toString());
    }

    /**
     * Load default cartridge and start it running.
     */
    public final boolean cartLoadDefault() {
        return cartLoad("");
    }

    /**
     * Load a cartridge and start it running.
     * 
     * @param fileName The filename of the cartridge to load.
     * @return True if the Cartridge was successfully loaded.
     */
    public final boolean cartLoad(String fileName) {
        // Clear the Screen
        gui.deleteDisplay();
        try {
            // Load the ROM from the File
            cpu.cpuLoadRom(fileName);
        } catch (NESCafeException e) {
            // Record the Error Message
            errorMessage = e.getMessage();
            // Show the Error Message and Return
            gui.showDialog(errorMessage);
            return false;
        }
        // Stop the Currently Loaded ROM
        cpu.cpuStop();
        noCartLoaded = true;
        gui.showMenuNoRom();
        // Run the new ROM
        cpu.cpuRun();
        Thread bob = new Thread(new Runnable() {
            public void run() {
                cpu.loop();
            }
        });
        bob.start();

        noCartLoaded = false;
        gui.showMenuRom();
        // Return Signal
        currentCartFileName = fileName;
        return true;
    }

    /**
     * Unload the currently running cartridge.
     */
    public final void cartUnload() {
        // Check if a Cart is Loaded
        if (noCartLoaded)
            return;
        // Stop the Current CPU Processing
        cpu.cpuStop();
        // Save SaveRAM
        memory.saveSaveRAM();
        // Show No ROM Menu
        gui.showMenuNoRom();
        // Turn Off Debug
        cpu.debugExit();
        // Clean Up the Resources
        noCartLoaded = true;
        currentCartFileName = "";
        System.gc();
    }

    /**
     * Determine whether or not a cartridge is currently running.
     * 
     * @return True if a Cartridge is currently running, else False.
     */
    public final boolean isCartRunning() {
        return !noCartLoaded;
    }

    /**
     * Determine the name of the currently loaded cartridge.
     * 
     * @return The filename of the currently loaded cartridge.
     */
    public final String getCartFileName() {
        return currentCartFileName;
    }

    /**
     * Reset the current Processor.
     */
    public final void reset() {
        cpu.cpuReset();
    }

    /**
     * Loads state of the NES from the last committed state for the Cart. This
     * method is called by the Processor to avoid corruption.
     */
    public final void stateLoad() {
        // Turn off the Request Submitted by the User
        gui.setLoadStateRequest(false);
        // Declare FileInputStream Object
        InputStream input;
        // Determine the Filename of the State File
        try {
            // Determine the Original filename of the Cartridge (without
            // extension)
            String fileName = Utils.getFileName(currentCartFileName);
            // Attempt to Open Save Directory
            File saveDirectory = new File("savestates");
            // Attempt to Open an InputStream from the File
            File inputFile = new File(saveDirectory + "/NESCafe - " + fileName + ".nss");
            input = new GZIPInputStream(new FileInputStream(inputFile));
        } catch (java.io.FileNotFoundException e) {
            // Inform User that no Committed State was Found
            gui.showDialog("You have not committed a State File for this game.");
            return;
        } catch (Exception e) {
            // Inform User of Difficulties when Opening File
            gui.showDialog("A Committed State could not be loaded...");
            return;
        }
        try {
            // Check the Signature
            byte[] signature = new byte[7];
            if (input.read(signature, 0, 7) != 7 || !(new String(signature)).equals("NESCafe")) {
                throw new NESCafeException("Invalid Save-State File Format.");
            }
            // Check Version Number
            if (input.read() > 1) {
                throw new NESCafeException("File Format is newer than supported by this version of NESCafe.");
            }
            // Load the CPU State
            cpu.stateLoad(input);
            // Load the Memory Manager State
            memory.stateLoad(input);
            // Load the PPU State
            ppu.stateLoad(input);
            // Check that the Mappers Match
            input.read();
            // Load the Mapper State
            mapper.stateLoad(input);
            // Load the JoyPad State
            joyPad1.stateLoad(input);
            // Close the InputStream
            input.close();
        } catch (NESCafeException e) {
            gui.showDialog("The NESCafe Saved-State is invalid: " + e.getMessage());
            return;
        } catch (IOException e) {
            gui.showDialog("The NESCafe Saved-State file could not be read.");
            return;
        }
        // Un Pause the CPU (unless in Applet Mode)
        cpu.setCPUPause(false);
        // Inform the User that the State Loaded Correctly
        gui.writeToScreen("State Successfully Loaded...");
    }

    /**
     * Saves state of the NES to disk. This method is called by the Processor to
     * avoid corruption.
     */
    public final void stateSave() {
        // Turn off User Request
        gui.setSaveStateRequest(false);
        // Determine the Original filename of the Cartridge (without extension)
        String fileName = Utils.getFileName(currentCartFileName);
        // Declare FileInputStream Object
        java.io.OutputStream output;
        try {
            // Attempt to Create Save Directory
            File saveDirectory = new File("savestates");
            if (saveDirectory.exists() && !saveDirectory.isDirectory()) {
                saveDirectory.delete();
            }
            if (!saveDirectory.exists()) {
                saveDirectory.mkdir();
            }
            // Attempt to Open an OutputStream to the File
            File outputFile = new File(saveDirectory + "/NESCafe - " + fileName + ".nss");
            output = new GZIPOutputStream(new FileOutputStream(outputFile));
        } catch (Exception e) {
            // Inform User of Difficulties when Saving File
            gui.showDialog("A Committed State could not be saved.");
            return;
        }
        try {
            // Write 7-byte Signature
            output.write("NESCafe".getBytes());
            // Write Save State Version
            output.write(1);
            // Save CPU State
            cpu.stateSave(output);
            // Save Memory State
            memory.stateSave(output);
            // Save PPU State
            ppu.stateSave(output);
            // Write Mapper Number
            output.write(mapper.getMapperNumber() & 0xFF);
            // Save Mapper State
            mapper.stateSave(output);
            // Save JoyPad State
            joyPad1.stateSave(output);
            joyPad2.stateSave(output);
            // Close the File
            output.close();
        } catch (IOException e) {
            gui.showDialog("The NESCafe Saved-State File could not be Saved.");
            return;
        }
        // Inform User of Progress
        gui.writeToScreen("State Successfully Saved...");
    }
}
