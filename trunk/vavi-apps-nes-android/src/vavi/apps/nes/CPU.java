/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class for the CPU Controller required by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class CPU {

    /** */
    boolean useUndocumentedOpCodes = true;

    /**
     * The current NES Machine.
     */
    private NES nes;

    /**
     * Whether this CPU Controller can request Screen Drawing.
     */
    private boolean allowDrawScreen = true;

    /**
     * True if the CPU is active.
     */
    protected boolean cpuActive = false;

    /**
     * True if the CPU is paused.
     */
    protected boolean cpuPaused = false;

    /**
     * True if the CPU is running instructions.
     */
    protected boolean cpuRunning = false;

    /**
     * The current Cartridge ROM Image.
     */
    protected NESCart currentCart;

    /**
     * 6502 Register : The Accumulator (8 bit)
     */
    private int A;

    /**
     * 6502 Register : X (8 bits)
     */
    private int X;

    /**
     * 6502 Register : Y (8 bits)
     */
    private int Y;

    /**
     * 6502 Register : The Processor Status Register (8 bits)
     */
    private int P;

    /**
     * 6502 Register : The Stack Index Register (8 bits)
     */
    private int S;

    /**
     * 6502 Register : The Program Counter Register (16 bits)
     */
    private int PC;

    /**
     * The number of CPU Cycles until the next Horizontal Blank.
     */
    private float cyclesPending;

    /**
     * The number of CPU Cycles between Horizontal Blanks.
     */
    public float CYCLES_PER_LINE = 116.0f;

    /**
     * True if a Reset request has been issued.
     */
    private boolean resetRequest = false;

    /**
     * True if a stop request has been issued.
     */
    private boolean stopRequest = false;

    /**
     * True if a stop request has been received.
     */
    // private boolean stopRequestFulfilled = false;
    /**
     * Halt Instruction has been fetched.
     */
    private boolean halted = false;

    /**
     * Debug Mode
     */
    private boolean debug = false;

    /**
     * Debug Mode Latch
     */
    private boolean debugLatch = false;

    /**
     * Debug Mode Latch
     */
    private boolean debugToInterrupt = false;

    /**
     * Debug CPU Instructions
     */
    private int debugInstructions = 0;

    /**
     * Create a new NES CPU Controller.
     * 
     * @param Nes The current NES Machine.
     * @param Gui The current Graphical User Interface.
     */
    public CPU(NES nes) {
        this.nes = nes;
    }

    /**
     * The main CPU run Thread.
     */
    public final void loop() {
        // Request an Internal Reset
        intReset();
        // Start Counter for number of Screens Displayed
        int counter = 1;
        // Turn off the Stop Request since we are just Starting
        stopRequest = false;
        // stopRequestFulfilled = false;
        // Check every 5 Frames
        final int everyX = 5;
        // State Ideal Frame Rate
        final int idealFrameRate = 60;
        // Calculate Wait Period
        final int waitPeriod = (1000 / idealFrameRate) * everyX;
        // Declare Time for Frame Stop
        long timeFrameStop = System.currentTimeMillis() + waitPeriod;
        // State Frame Counter
        int everyY = 1;
        // Start Emulating
        while (!stopRequest) {
            // Check if new Calc Needed
            if (everyY > everyX) {
                // Suggest Minimum Time before Frame is Finished
                timeFrameStop = System.currentTimeMillis() + waitPeriod;
                // Reset the EveryY
                everyY = 1;
            }
            // Emulate until a Vertical Blank occurs
            emulateFrame();
            // Check for a Reset Request
            if (resetRequest)
                reset();
            // Loop until at Least the Minimum Frame Time has Expired
            if (everyY == everyX)
                while (timeFrameStop > System.currentTimeMillis())
                    Thread.yield();
            // After 32 Frames the PPU is stable so turn off Loading Screen
            if (counter != 0) {
                if (++counter > 32) {
                    nes.gui.showLoadingScreen(false);
                    counter = 0;
                }
            }
            // Increment Frame
            everyY++;
        }
        // Signal that Stop Requested has been Received
        // stopRequestFulfilled = true;
    }

    /**
     * Method to Eat up CPU Cycles.
     */
    public final void eatCycles(int cycles) {
        cyclesPending -= cycles;
    }

    /**
     * Load a Cartridge ROM for the CPU.
     * 
     * @param fileName The filename of the ROM image.
     * @throws NESCafeException If ROM File is not suitable for NESCafe.
     */
    public final void cpuLoadRom(String fileName) throws NESCafeException {
        // Load Cart into NESCart Object
        currentCart = new NESCart(nes);
        boolean fail = currentCart.loadRom(fileName);
        // Check if an Error Occurred
        if (fail) {
            // If no Error can be Identified then use this Description
            String errString = "An Error occurred when opening the ROM Image.";
            // Try and Identify the Error more Precisely
            switch (currentCart.getErrorCode()) {
            case NESCart.ERROR_IO:
                errString = "The selected ROM file could not be read. " + fileName;
                break;
            case NESCart.ERROR_FILE_FORMAT:
                errString = "The selected game is not a valid iNES Rom." + fileName;
                break;
            case NESCart.ERROR_UNSUPPORTED_MAPPER:
                String mapName = "Mapper '" + currentCart.getMapperName() + "'";
                errString = mapName + " is not currently supported.";
                break;
            case NESCart.ERROR_MISSING_PROGRAM_ROM:
                errString = "The selected ROM file contains no Program ROM";
                break;
            case NESCart.ERROR_ZIP_NO_ENTRIES:
                errString = "The selected ZIP file contains no Nintendo ROMs";
                break;
            case NESCart.ERROR_ZIP_INVALID:
                errString = "The selected ZIP file is corrupt. Please download it again.";
                break;
            case NESCart.ERROR_GZIP_INVALID:
                errString = "The selected GZIP file is corrupt. Please download it again.";
                break;
            }
            // Get Rid of the Cart Object
            currentCart = null;
            throw new NESCafeException(errString);
        }
        // Close any Current ROM
        cpuStop();
        // Stop any Active Debug Instructions
        debugExit();
        // Load the Memory with the Cartridge Image
        nes.memory.init(currentCart, fileName);
        // Load Specific Memory Manager for Supported Mappers
        nes.mapper = currentCart.mapper;
        if (nes.mapper == null) {
            currentCart = null;
            String errString = "The Hardware could not be located for the Cartridge.";
            throw new NESCafeException(errString);
        }
        // Initialise the Memory Mapper and Force a Mapper Reset
        nes.ppu.latchMapper = false;
        nes.mapper.init(nes.memory);
        nes.mapper.setCRC(currentCart.crc32);
        // Display the Mapper Number in DEBUG
        System.out.println("Mapper: " + currentCart.getMapperNumber());
        // Inform user that the Memory and its Mapper have been Loaded and
        // Initialised
        if (currentCart.getMapperNumber() > 0) {
            nes.gui.writeToScreen("Loading " + currentCart.getMapperName() + " Board...");
        } else {
            nes.gui.writeToScreen("Loading NES System...");
        }
    }

    /**
     * Run the CPU.
     */
    public final void cpuRun() {
        // Activates the CPU
        cpuActive = true;
        // Sets Running Flag
        cpuRunning = true;
        // Starts the Nintendo 6502 Machine
    }

    /**
     * Stop the current CPU.
     */
    public final void cpuStop() {
        // If the CPU is not Running Code then Return
        if (!cpuRunning)
            return;
        // Issue a Stop Request to the Processor
        stopProcessing();
        // Force a Closing
        cpuRunning = false;
    }

    /**
     * Halt the CPU
     */
    public final boolean isCPUHalted() {
        return halted;
    }

    /**
     * Request that the Processor performs a NMI
     */
    public final void cpuNMI() {
        NMI();
    }

    /**
     * Ask the TV Controller to draw the Screen.
     * 
     * @param force True to force a draw.
     */
    public final synchronized void drawScreen(boolean force) {
        // Check if the CPU is allowed to make this Request
        if (!allowDrawScreen)
            return;
        // Ask TV Controller to Draw whatever is in its Buffer
        nes.gui.drawScreen(force);
    }

    /**
     * Enable or Disable the CPU Controller to issue drawing requests.
     * 
     * @param allow True to allow the CPU Controller to issue drawing request.
     */
    public final synchronized void enableDrawScreen(boolean allow) {
        allowDrawScreen = allow;
    }

    /**
     * Determines whether a CPU is paused.
     * 
     * @return True if CPU is currently paused.
     */
    public final boolean getCPUPause() {
        return cpuPaused;
    }

    /**
     * Determines if the CPU is running.
     * 
     * @return Return true if the CPU is running normally.
     */
    public final boolean isCPURunning() {
        // Make checks to see if CPU is running normally
        return (cpuActive & cpuRunning & !cpuPaused);
    }

    /**
     * Activates and deactivates the CPU.
     * 
     * @param active True to active the CPU, else False.
     */
    public final void setCpuActive(boolean active) {
        // Deactiving a CPU will not allow it to run or display.
        cpuActive = active;
    }

    /**
     * Pauses a CPU.
     * 
     * @param pause True to pause the CPU and false to resume.
     */
    public final void setCPUPause(boolean pause) {
        // Pause the CPU Controller
        cpuPaused = pause;
    }

    /**
     * Clears the CPU from Debug Mode.
     */
    public final void debugExit() {
        if (getDebug()) {
            debugEnterToggle();
        }
    }

    /**
     * Sets the CPU into Debug Mode.
     */
    public final void debugEnterToggle() {
        // Check Current Mode and Toggle
        if (getDebug()) {
            // Debug Exit
            setDebug(false);
            setDebugInstructions(0);
            nes.gui.fireViewEvent(new NES.View.ViewEvent(this, "doDisableDebug"));
        } else {
            // Debug Enter
            setDebugToInterrupt(false);
            setDebugInstructions(1);
            setDebug(true);
            nes.gui.fireViewEvent(new NES.View.ViewEvent(this, "doEnableDebug"));
        }
    }

    /**
     * Debug to Interrupt
     */
    public final void debugToInterrupt(boolean val) {
        // Check for a Valid Processor
        if (!getDebug())
            return;
        // Set FetchDecode Enable on Processor
        setDebugToInterrupt(val);
        if (val)
            nes.gui.writeToScreen("Seeking for next Interrupt...");
    }

    /**
     * Removes the CPU from Debug Mode.
     */
    public final void debugStep(int instructions) {
        // Check for a Valid Processor
        if (!getDebug())
            return;
        // Debug Step
        if (getDebugInstructions() == 0) {
            setDebugInstructions(instructions);
            if (instructions == 1) {
                nes.gui.writeToScreen("Debug Step...");
            } else if (instructions > 1) {
                nes.gui.writeToScreen("Debug Step " + instructions + " instructions...");
            }
        }
    }

    /**
     * Loads the State of the CPU Controller from an InputStream.
     */
    public final void loadState(InputStream is) throws IOException, NESCafeException {
        // Read the Stored CRC
        int crc = 0;
        crc = (is.read() & 0xFF);
        crc |= (is.read() & 0xFF) << 8;
        crc |= (is.read() & 0xFF) << 16;
        crc |= (is.read() & 0xFF) << 24;
        crc &= 0xFFFFFFFF;
        // Read the Actual CRC from the Cartridge
        int actualCRC = (int) (currentCart.crc32 & 0xFFFFFFFF);
        if (actualCRC != crc) {
            throw new NESCafeException("Saved-state belongs to a different ROM");
        }
        // Read Registers
        A = is.read() & 0xFF;
        X = is.read() & 0xFF;
        Y = is.read() & 0xFF;
        P = is.read() & 0xFF;
        S = is.read() & 0xFF;
        // Read Program Counter
        PC = (is.read() & 0xFF);
        PC |= (is.read() & 0xFF) << 8;
        // Read Emulation Information
        cyclesPending = is.read() & 0xFF;
        halted = (is.read() == 0xFF);
    }

    /**
     * Saves the State of the CPU Controller to a FileOutputStream.
     */
    public final void saveState(OutputStream os) throws IOException {
        // Record CRC32
        int crc = (int) (currentCart.crc32 & 0xFFFFFFFF);
        os.write((crc >> 0) & 0xFF);
        os.write((crc >> 8) & 0xFF);
        os.write((crc >> 16) & 0xFF);
        os.write((crc >> 24) & 0xFF);
        // Write Registers
        os.write(A & 0xFF);
        os.write(X & 0xFF);
        os.write(Y & 0xFF);
        os.write(P & 0xFF);
        os.write(S & 0xFF);
        // Write Program Counter
        os.write((PC >> 0) & 0xFF);
        os.write((PC >> 8) & 0xFF);
        // Write Emulation Information
        os.write((int) cyclesPending & 0xFF);
        os.write(halted ? 0xFF : 0x00);
    }

    //
    // Main Control Functions
    //

    /**
     * Clears the Display.
     */
    private final void deleteScreen() {
        nes.gui.deleteDisplay();
    }

    /**
     * Emulate a Frame.
     */
    private final void emulateFrame() {
        // Check if Halted
        if (halted) {
            drawScreen(false);
            return;
        }
        // Start PPU Frame
        nes.ppu.startFrame();
        // Lines 0-239
        for (int i = 0; i < 240; i++) {
            emulateCPUCycles(CYCLES_PER_LINE);
            if (nes.mapper.syncH(i) != 0)
                IRQ();
            nes.ppu.drawScanLine();
        }
        // Close PPU Frame
        nes.ppu.endFrame();
        // Frame IRQ
        if ((nes.frameIRQEnabled & 0xC0) == 0) {
            IRQ();
        }
        // Lines 240-261
        for (int i = 240; i <= 261; i++) {
            // End of Virtual Blank
            if (i == 261) {
                nes.ppu.endVBlank();
            }
            // Start of Virtual Blank
            if (i == 241) {
                nes.ppu.startVBlank();
                nes.mapper.syncV();
                emulateCPUCycles(1);
                if (nes.ppu.nmiEnabled())
                    NMI();
                emulateCPUCycles(CYCLES_PER_LINE - 1);
                if (nes.mapper.syncH(i) != 0)
                    IRQ();
                continue;
            }
            emulateCPUCycles(CYCLES_PER_LINE);
            if (nes.mapper.syncH(i) != 0)
                IRQ();
        }
        // Drawn the Screen
        drawScreen(false);
        // Check if Save or Load State Requests have been issued
        if (nes.gui.isSaveStateRequest())
            nes.stateSave();
        if (nes.gui.isLoadStateRequest())
            nes.stateLoad();
        // Wait while NESCafe is not Active or Paused
        waitWhileNotActive();
        waitWhilePaused();
    }

    /**
     * Perform a Non Maskable Interrupt.
     */
    public final void NMI() {
        if (debug) {
            debugToInterrupt = false;
            debugInstructions = 1;
            debugPrint("\n[NMI]");
            nes.gui.writeToScreen("");
        }
        pushWord(PC);
        push(P & 0xEF); // CLEAR BRK
        PC = readWord(0xFFFA);
        cyclesPending += 7;
    }

    /**
     * Debug Event Happened
     */
    public final void debugEventHappened(String event) {
        if (debug) {
            debugToInterrupt = false;
            debugInstructions = 1;
            debugPrint("\n[" + event + "]");
            nes.gui.writeToScreen("");
        }
    }

    /**
     * Perform a IRQ/BRK Interrupt.
     */
    public final void IRQ() {
        if ((P & 0x4) == 0x00) {
            if (debug) {
                debugToInterrupt = false;
                debugInstructions = 1;
                debugPrint("\n[IRQ]");
                nes.gui.writeToScreen("");
            }
            pushWord(PC);
            push(P & 0xEF); // CLEAR BRK
            PC = readWord(0xFFFE);
            P |= 0x04;
            cyclesPending += 7;
        }
    }

    /**
     * Emulate until the next Horizontal Blank is encountered.
     */
    public final void emulateCPUCycles(float cycles) {
        // Declare Deficit Cycles
        cyclesPending += cycles;
        // Loop until a Horizontal Blank is encountered
        while (cyclesPending > 0) {
            // Fetch and Execute the Next Instruction
            if (!halted)
                instructionFetchExecute();
            else
                cyclesPending--;
            // Check for a Stop Request
            if (stopRequest)
                return;
        }
    }

    /**
     * Halt the CPU
     */
    public final void haltCPU() {
        halted = true;
    }

    /**
     * Fetch and Execute the next Instruction.
     */
    private final void instructionFetchExecute() {
        // Sanity Check
        if (halted)
            return;
        // Display Debug Header Information
        debug = debugLatch;
        if (debug) {
            // Loop While Waiting
            while (debugInstructions <= 0 && debugLatch && !debugToInterrupt) {
                try {
                    drawScreen(true);
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
            debug = debugLatch;
            // Check Debug Still Enabled
            if (debug) {
                // Check for a Reset Request
                if (resetRequest)
                    reset();
                // Decrement Instruction Count
                debugInstructions--;
                // Print Header
                debugPrint("\n" + Utils.hex(PC, 4) + " A=" + Utils.hex(A, 2) + " X=" + Utils.hex(X, 2) + " Y=" + Utils.hex(Y, 2) + " S=" + Utils.hex(S, 2) + " P=" + Utils.binary(P, 8) + " ");
            }
        }
        // Fetch the Next Instruction Code
        int instCode = read(PC++);
        // Declare Variables for Handling Addresses and Values
        int address;
        int writeVal;
        // Check if an Instruction Code can be Identified
        switch (instCode) {
        case 0x00: // BRK
            if (debug)
                debugPrint("BRK ");
            address = PC + 1;
            pushWord(address);
            push(P | 0x10);
            PC = readWord(0xFFFE);
            P |= 0x04;
            P |= 0x10;
            break;
        case 0xA9: // LDA #aa
            if (debug)
                debugPrint("LDA ");
            A = byImmediate();
            setStatusFlags(A);
            break;
        case 0xA5: // LDA Zero Page
            if (debug)
                debugPrint("LDA ");
            A = read(byZeroPage());
            setStatusFlags(A);
            break;
        case 0xB5: // LDA $aa,X
            if (debug)
                debugPrint("LDA ");
            A = read(byZeroPageX());
            setStatusFlags(A);
            break;
        case 0xAD: // LDA $aaaa
            if (debug)
                debugPrint("LDA ");
            A = read(byAbsolute());
            setStatusFlags(A);
            break;
        case 0xBD: // LDA $aaaa,X
            if (debug)
                debugPrint("LDA ");
            A = read(byAbsoluteX());
            setStatusFlags(A);
            break;
        case 0xB9: // LDA $aaaa,Y
            if (debug)
                debugPrint("LDA ");
            A = read(byAbsoluteY());
            setStatusFlags(A);
            break;
        case 0xA1: // LDA ($aa,X)
            if (debug)
                debugPrint("LDA ");
            A = read(byIndirectX());
            setStatusFlags(A);
            break;
        case 0xB1: // LDA ($aa),Y
            if (debug)
                debugPrint("LDA ");
            A = read(byIndirectY());
            setStatusFlags(A);
            break;
        case 0xA2: // LDX #aa
            if (debug)
                debugPrint("LDX ");
            X = byImmediate();
            setStatusFlags(X);
            break;
        case 0xA6: // LDX $aa
            if (debug)
                debugPrint("LDX ");
            X = read(byZeroPage());
            setStatusFlags(X);
            break;
        case 0xB6: // LDX $aa,Y
            if (debug)
                debugPrint("LDX ");
            X = read(byZeroPageY());
            setStatusFlags(X);
            break;
        case 0xAE: // LDX $aaaa
            if (debug)
                debugPrint("LDX ");
            X = read(byAbsolute());
            setStatusFlags(X);
            break;
        case 0xBE: // LDX $aaaa,Y
            if (debug)
                debugPrint("LDX ");
            X = read(byAbsoluteY());
            setStatusFlags(X);
            break;
        case 0xA0: // LDY #aa
            if (debug)
                debugPrint("LDY ");
            Y = byImmediate();
            setStatusFlags(Y);
            break;
        case 0xA4: // LDY $aa
            if (debug)
                debugPrint("LDY ");
            Y = read(byZeroPage());
            setStatusFlags(Y);
            break;
        case 0xB4: // LDY $aa,X
            if (debug)
                debugPrint("LDY ");
            Y = read(byZeroPageX());
            setStatusFlags(Y);
            break;
        case 0xAC: // LDY $aaaa
            if (debug)
                debugPrint("LDY ");
            Y = read(byAbsolute());
            setStatusFlags(Y);
            break;
        case 0xBC: // LDY $aaaa,x
            if (debug)
                debugPrint("LDY ");
            Y = read(byAbsoluteX());
            setStatusFlags(Y);
            break;
        case 0xA7: // LAX $aa
            if (debug)
                debugPrint("LAX ");
            if (useUndocumentedOpCodes) {
                A = read(byZeroPage());
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAX $aa");
                halted = true;
                PC--;
            }
            break;
        case 0xB7: // LAX $aa,Y
            if (debug)
                debugPrint("LAX ");
            if (useUndocumentedOpCodes) {
                A = read(byZeroPageY());
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAX $aa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0xAF: // LAX $aaaa
            if (debug)
                debugPrint("LAX ");
            if (useUndocumentedOpCodes) {
                A = read(byAbsolute());
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAX $aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0xBF: // LAX $aaaa,Y
            if (debug)
                debugPrint("LAX ");
            if (useUndocumentedOpCodes) {
                A = read(byAbsoluteY());
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAX $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0xA3: // LAX ($aa,X)
            if (debug)
                debugPrint("LAX ");
            if (useUndocumentedOpCodes) {
                A = read(byIndirectX());
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAX ($aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0xB3: // LAX ($aa),Y
            if (debug)
                debugPrint("LAX ");
            if (useUndocumentedOpCodes) {
                A = read(byIndirectY());
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAX ($aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0x85: // STA $aa
            if (debug)
                debugPrint("STA ");
            address = byZeroPage();
            write(address, A);
            break;
        case 0x95: // STA $aa,X
            if (debug)
                debugPrint("STA ");
            address = byZeroPageX();
            write(address, A);
            break;
        case 0x8D: // STA $aaaa
            if (debug)
                debugPrint("STA ");
            address = byAbsolute();
            write(address, A);
            break;
        case 0x9D: // STA $aaaa,X
            if (debug)
                debugPrint("STA ");
            address = byAbsoluteX();
            write(address, A);
            break;
        case 0x99: // STA $aaaa,Y
            if (debug)
                debugPrint("STA ");
            address = byAbsoluteY();
            write(address, A);
            break;
        case 0x81: // STA ($aa,X)
            if (debug)
                debugPrint("STA ");
            address = byIndirectX();
            write(address, A);
            break;
        case 0x91: // STA ($aa),Y
            if (debug)
                debugPrint("STA ");
            address = byIndirectY();
            write(address, A);
            break;
        case 0x86: // STX $aa
            if (debug)
                debugPrint("STX ");
            address = byZeroPage();
            write(address, X);
            break;
        case 0x96: // STX $aa,Y
            if (debug)
                debugPrint("STX ");
            address = byZeroPageY();
            write(address, X);
            break;
        case 0x8E: // STX $aaaa
            if (debug)
                debugPrint("STX ");
            address = byAbsolute();
            write(address, X);
            break;
        case 0x84: // STY $aa
            if (debug)
                debugPrint("STY ");
            address = byZeroPage();
            write(address, Y);
            break;
        case 0x94: // STY $aa,X
            if (debug)
                debugPrint("STY ");
            address = byZeroPageX();
            write(address, Y);
            break;
        case 0x8C: // STY $aaaa
            if (debug)
                debugPrint("STY ");
            address = byAbsolute();
            write(address, Y);
            break;
        case 0xAA: // TAX
            if (debug)
                debugPrint("TAX ");
            X = A;
            setStatusFlags(X);
            break;
        case 0xA8: // TAY
            if (debug)
                debugPrint("TAY ");
            Y = A;
            setStatusFlags(Y);
            break;
        case 0xBA: // TSX
            if (debug)
                debugPrint("TSX ");
            X = S & 0xFF;
            setStatusFlags(X);
            break;
        case 0x8A: // TXA
            if (debug)
                debugPrint("TXA ");
            A = X;
            setStatusFlags(A);
            break;
        case 0x9A: // TXS
            if (debug)
                debugPrint("TXS ");
            S = X & 0XFF;
            break;
        case 0x98: // TYA
            if (debug)
                debugPrint("TYA ");
            A = Y;
            setStatusFlags(A);
            break;
        case 0x09: // ORA #aa
            if (debug)
                debugPrint("ORA ");
            A |= byImmediate();
            setStatusFlags(A);
            break;
        case 0x05: // ORA $aa
            if (debug)
                debugPrint("ORA ");
            address = byZeroPage();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x15: // ORA $aa,X
            if (debug)
                debugPrint("ORA ");
            address = byZeroPageX();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x0D: // ORA $aaaa
            if (debug)
                debugPrint("ORA ");
            address = byAbsolute();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x1D: // ORA $aaaa,X
            if (debug)
                debugPrint("ORA ");
            address = byAbsoluteX();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x19: // ORA $aaaa,Y
            if (debug)
                debugPrint("ORA ");
            address = byAbsoluteY();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x01: // ORA ($aa,X)
            if (debug)
                debugPrint("ORA ");
            address = byIndirectX();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x11: // ORA ($aa),Y
            if (debug)
                debugPrint("ORA ");
            address = byIndirectY();
            A |= read(address);
            setStatusFlags(A);
            break;
        case 0x29: // AND #aa
            if (debug)
                debugPrint("AND ");
            A &= byImmediate();
            setStatusFlags(A);
            break;
        case 0x25: // AND $aa
            if (debug)
                debugPrint("AND ");
            address = byZeroPage();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x35: // AND $aa,X
            if (debug)
                debugPrint("AND ");
            address = byZeroPageX();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x2D: // AND $aaaa
            if (debug)
                debugPrint("AND ");
            address = byAbsolute();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x3D: // AND $aaaa,X
            if (debug)
                debugPrint("AND ");
            address = byAbsoluteX();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x39: // AND $aaaa,Y
            if (debug)
                debugPrint("AND ");
            address = byAbsoluteY();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x21: // AND ($aa,X)
            if (debug)
                debugPrint("AND ");
            address = byIndirectX();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x31: // AND ($aa),Y
            if (debug)
                debugPrint("AND ");
            address = byIndirectY();
            A &= read(address);
            setStatusFlags(A);
            break;
        case 0x49: // EOR #aa
            if (debug)
                debugPrint("EOR ");
            A ^= byImmediate();
            setStatusFlags(A);
            break;
        case 0x45: // EOR $aa
            if (debug)
                debugPrint("EOR ");
            address = byZeroPage();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x55: // EOR $aa,X
            if (debug)
                debugPrint("EOR ");
            address = byZeroPageX();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x4D: // EOR $aaaa
            if (debug)
                debugPrint("EOR ");
            address = byAbsolute();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x5D: // EOR $aaaa,X
            if (debug)
                debugPrint("EOR ");
            address = byAbsoluteX();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x59: // EOR $aaaa,Y
            if (debug)
                debugPrint("EOR ");
            address = byAbsoluteY();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x41: // EOR ($aa,X)
            if (debug)
                debugPrint("EOR ");
            address = byIndirectX();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x51: // EOR ($aa),Y
            if (debug)
                debugPrint("EOR ");
            address = byIndirectY();
            A ^= read(address);
            setStatusFlags(A);
            break;
        case 0x24: // BIT $aa
            if (debug)
                debugPrint("BIT ");
            operateBit(read(byZeroPage()));
            break;
        case 0x2C: // BIT $aaaa
            if (debug)
                debugPrint("BIT ");
            operateBit(read(byAbsolute()));
            break;
        case 0x07: // ASO $aa
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byZeroPage();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO $aa");
                halted = true;
                PC--;
            }
            break;
        case 0x0A: // ASL A
            if (debug)
                debugPrint("ASL ");
            A = ASL(A);
            break;
        case 0x06: // ASL $aa
            if (debug)
                debugPrint("ASL ");
            address = byZeroPage();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ASL(writeVal);
            write(address, writeVal);
            break;
        case 0x16: // ASL $aa,X
            if (debug)
                debugPrint("ASL ");
            address = byZeroPageX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ASL(writeVal);
            write(address, writeVal);
            break;
        case 0x0E: // ASL $aaaa
            if (debug)
                debugPrint("ASL ");
            address = byAbsolute();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ASL(writeVal);
            write(address, writeVal);
            break;
        case 0x1E: // ASL $aaaa,X
            if (debug)
                debugPrint("ASL ");
            address = byAbsoluteX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ASL(writeVal);
            write(address, writeVal);
            break;
        case 0x17: // ASO $aa,X
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byZeroPageX();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO $aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x0F: // ASO $aaaa
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byAbsolute();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO $aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0x1F: // ASO $aaaa,X
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteX();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO $aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x1B: // ASO $aaaa,Y
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x03: // ASO ($aa,X)
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byIndirectX();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO ($aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0x13: // ASO ($aa),Y
            if (debug)
                debugPrint("ASO ");
            if (useUndocumentedOpCodes) {
                address = byIndirectY();
                writeVal = ASL(read(address));
                write(address, writeVal);
                A |= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("ASO ($aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0x4A: // LSR A
            if (debug)
                debugPrint("LSR A ");
            A = LSR(A);
            break;
        case 0x46: // LSR $aa
            if (debug)
                debugPrint("LSR ");
            address = byZeroPage();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = LSR(writeVal);
            write(address, writeVal);
            break;
        case 0x56: // LSR $aa,X
            if (debug)
                debugPrint("LSR ");
            address = byZeroPageX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = LSR(writeVal);
            write(address, writeVal);
            break;
        case 0x4E: // LSR $aaaa
            if (debug)
                debugPrint("LSR ");
            address = byAbsolute();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = LSR(writeVal);
            write(address, writeVal);
            break;
        case 0x5E: // LSR $aaaa,X
            if (debug)
                debugPrint("LSR ");
            address = byAbsoluteX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = LSR(writeVal);
            write(address, writeVal);
            break;
        case 0x47: // LSE $aa
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byZeroPage();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE $aa");
                halted = true;
                PC--;
            }
            break;
        case 0x57: // LSE $aa,X
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byZeroPageX();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE $aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x4F: // LSE $aaaa
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byAbsolute();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE $aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0x5F: // LSE $aaaa,X
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteX();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE $aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x5B: // LSE $aaaa,Y
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x43: // LSE ($aa,X)
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byIndirectX();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE ($aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0x53: // LSE ($aa),Y
            if (debug)
                debugPrint("LSE ");
            if (useUndocumentedOpCodes) {
                address = byIndirectY();
                writeVal = LSR(read(address));
                write(address, writeVal);
                A ^= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LSE ($aa,Y)");
                halted = true;
                PC--;
            }
            break;
        case 0x2A: // ROL A
            if (debug)
                debugPrint("ROL A ");
            A = ROL(A);
            break;
        case 0x26: // ROL $aa (RWMW)
            if (debug)
                debugPrint("ROL ");
            address = byZeroPage();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROL(writeVal);
            write(address, writeVal);
            break;
        case 0x36: // ROL $aa,X (RWMW)
            if (debug)
                debugPrint("ROL ");
            address = byZeroPageX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROL(writeVal);
            write(address, writeVal);
            break;
        case 0x2E: // ROL $aaaa (RWMW)
            if (debug)
                debugPrint("ROL ");
            address = byAbsolute();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROL(writeVal);
            write(address, writeVal);
            break;
        case 0x3E: // ROL $aaaa,X (RWMW)
            if (debug)
                debugPrint("ROL ");
            address = byAbsoluteX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROL(writeVal);
            write(address, writeVal);
            break;
        case 0x27: // RLA $aa
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byZeroPage();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA $aa");
                halted = true;
                PC--;
            }
            break;
        case 0x37: // RLA $aa,X
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byZeroPageX();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA $aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x2F: // RLA $aaaa
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byAbsolute();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA $aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0x3F: // RLA $aaaa,X
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteX();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA $aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x3B: // RLA $aaaa,Y
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x23: // RLA ($aa,X)
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byIndirectX();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA ($aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0x33: // RLA ($aa),Y
            if (debug)
                debugPrint("RLA ");
            if (useUndocumentedOpCodes) {
                address = byIndirectY();
                writeVal = ROL(read(address));
                write(address, writeVal);
                A &= writeVal;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("RLA ($aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0x6A: // ROR A
            if (debug)
                debugPrint("ROR A ");
            A = ROR(A);
            break;
        case 0x66: // ROR $aa
            if (debug)
                debugPrint("ROR ");
            address = byZeroPage();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROR(writeVal);
            write(address, writeVal);
            break;
        case 0x67: // RRA $aa
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byZeroPage();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA $aa");
                halted = true;
                PC--;
            }
            break;
        case 0x76: // ROR $aa,X
            if (debug)
                debugPrint("ROR ");
            address = byZeroPageX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROR(writeVal);
            write(address, writeVal);
            break;
        case 0x77: // RRA $aa,X
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byZeroPageX();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA $aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x6E: // ROR $aaaa
            if (debug)
                debugPrint("ROR ");
            address = byAbsolute();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROR(writeVal);
            write(address, writeVal);
            break;
        case 0x6F: // RRA $aaaa
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byAbsolute();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA $aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0x7E: // ROR $aaaa,X
            if (debug)
                debugPrint("ROR ");
            address = byAbsoluteX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = ROR(writeVal);
            write(address, writeVal);
            break;
        case 0x7F: // RRA $aaaa,X
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteX();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA $aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x7B: // RRA $aaaa,Y
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x63: // RRA ($aa,X)
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byIndirectX();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA ($aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0x73: // RRA ($aa),Y
            if (debug)
                debugPrint("RRA ");
            if (useUndocumentedOpCodes) {
                address = byIndirectY();
                writeVal = ROR(read(address));
                write(address, writeVal);
                operateAdd(address);
            } else {
                usedUndocumentedCode("RRA ($aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0x90: // BCC
            if (debug)
                debugPrint("BCC ");
            branch(0x01, false);
            break;
        case 0xB0: // BCS
            if (debug)
                debugPrint("BCS ");
            branch(0x01, true);
            break;
        case 0xD0: // BNE
            if (debug)
                debugPrint("BNE ");
            branch(0x02, false);
            break;
        case 0xF0: // BEQ
            if (debug)
                debugPrint("BEQ ");
            branch(0x02, true);
            break;
        case 0x10: // BPL
            if (debug)
                debugPrint("BPL ");
            branch(0x80, false);
            break;
        case 0x30: // BMI
            if (debug)
                debugPrint("BMI ");
            branch(0x80, true);
            break;
        case 0x50: // BVC
            if (debug)
                debugPrint("BVC ");
            branch(0x40, false);
            break;
        case 0x70: // BVS
            if (debug)
                debugPrint("BVS ");
            branch(0x40, true);
            break;
        case 0x4C: // JMP $aaaa
            if (debug)
                debugPrint("JMP ");
            PC = byAbsolute();
            break;
        case 0x6C: // JMP ($aaaa)
            if (debug)
                debugPrint("JMP (");
            address = byAbsolute();
            if (debug)
                debugPrint(")");
            if ((address & 0x00FF) == 0xFF)
                PC = (read(address & 0xFF00) << 8) | read(address);
            else
                PC = readWord(address);
            break;
        case 0x20: // JSR $aaaa
            if (debug)
                debugPrint("JSR ");
            address = PC + 1;
            pushWord(address);
            PC = byAbsolute();
            break;
        case 0x60: // RTS
            if (debug)
                debugPrint("RTS ");
            PC = popWord() + 1;
            break;
        case 0x40: // RTI
            if (debug)
                debugPrint("RTI ");
            P = pop();
            PC = popWord();
            break;
        case 0x48: // PHA
            if (debug)
                debugPrint("PHA ");
            push(A);
            break;
        case 0x08: // PHP
            if (debug)
                debugPrint("PHP ");
            push(P | 0x10); // SET BRK
            break;
        case 0x68: // PLA
            if (debug)
                debugPrint("PLA ");
            A = pop();
            setStatusFlags(A);
            break;
        case 0x28: // PLP
            if (debug)
                debugPrint("PLP ");
            P = pop();
            break;
        case 0x18: // CLC
            if (debug)
                debugPrint("CLC ");
            P &= 0xfe;
            break;
        case 0xD8: // CLD
            if (debug)
                debugPrint("CLD ");
            P &= 0xf7;
            break;
        case 0x58: // CLI
            if (debug)
                debugPrint("CLI ");
            P &= 0xfb;
            break;
        case 0xB8: // CLV
            if (debug)
                debugPrint("CLV ");
            P &= 0xbf;
            break;
        case 0x38: // SEC
            if (debug)
                debugPrint("SEC ");
            P |= 0x1;
            break;
        case 0xF8: // SED
            if (debug)
                debugPrint("SED ");
            P |= 0x8;
            break;
        case 0x78: // SEI
            if (debug)
                debugPrint("SEI ");
            P |= 0x4;
            break;
        case 0xE6: // INC $aa (RWMW)
            if (debug)
                debugPrint("INC ");
            address = byZeroPage();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = increment(writeVal);
            write(address, writeVal);
            break;
        case 0xF6: // INC $aa,X (RWMW)
            if (debug)
                debugPrint("INC ");
            address = byZeroPageX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = increment(read(address));
            write(address, writeVal);
            break;
        case 0xEE: // INC $aaaa (RWMW)
            if (debug)
                debugPrint("INC ");
            address = byAbsolute();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = increment(read(address));
            write(address, writeVal);
            break;
        case 0xFE: // INC $aaaa,X (RWMW)
            if (debug)
                debugPrint("INC ");
            address = byAbsoluteX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = increment(read(address));
            write(address, writeVal);
            break;
        case 0xE8: // INX
            if (debug)
                debugPrint("INX ");
            X++;
            X &= 0xff;
            setStatusFlags(X);
            break;
        case 0xC8: // INY
            if (debug)
                debugPrint("INY ");
            Y++;
            Y &= 0xff;
            setStatusFlags(Y);
            break;
        case 0xC6: // DEC $aa (RWMW)
            if (debug)
                debugPrint("DEC ");
            address = byZeroPage();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = decrement(read(address));
            write(address, writeVal);
            break;
        case 0xD6: // DEC $aa,X (RWMW)
            if (debug)
                debugPrint("DEC ");
            address = byZeroPageX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = decrement(read(address));
            write(address, writeVal);
            break;
        case 0xCE: // DEC $aaaa (RWMW)
            if (debug)
                debugPrint("DEC ");
            address = byAbsolute();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = decrement(read(address));
            write(address, writeVal);
            break;
        case 0xDE: // DEC $aaaa,X (RWMW)
            if (debug)
                debugPrint("DEC ");
            address = byAbsoluteX();
            writeVal = read(address);
            write(address, writeVal);
            writeVal = decrement(read(address));
            write(address, writeVal);
            break;
        case 0xCA: // DEX
            if (debug)
                debugPrint("DEX ");
            X--;
            X &= 0xff;
            setStatusFlags(X);
            break;
        case 0x88: // DEY
            if (debug)
                debugPrint("DEY ");
            Y--;
            Y &= 0xff;
            setStatusFlags(Y);
            break;
        case 0x69: // ADC #aa
            if (debug)
                debugPrint("ADC ");
            operateAdd(byImmediate());
            break;
        case 0x65: // ADC $aa
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byZeroPage()));
            break;
        case 0x75: // ADC $aa,X
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byZeroPageX()));
            break;
        case 0x6D: // ADC $aaaa
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byAbsolute()));
            break;
        case 0x7D: // ADC $aaaa,X
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byAbsoluteX()));
            break;
        case 0x79: // ADC $aaaa,Y
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byAbsoluteY()));
            break;
        case 0x61: // ADC ($aa,X)
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byIndirectX()));
            break;
        case 0x71: // ADC ($aa),Y
            if (debug)
                debugPrint("ADC ");
            operateAdd(read(byIndirectY()));
            break;
        case 0xEB: // SBC #aa
        case 0xE9: // SBC #aa
            if (debug)
                debugPrint("SBC ");
            operateSub(byImmediate());
            break;
        case 0xE5: // SBC $aa
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byZeroPage()));
            break;
        case 0xF5: // SBC $aa,X
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byZeroPageX()));
            break;
        case 0xED: // SBC $aaaa
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byAbsolute()));
            break;
        case 0xFD: // SBC $aaaa,X
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byAbsoluteX()));
            break;
        case 0xF9: // SBC $aaaa,Y
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byAbsoluteY()));
            break;
        case 0xE1: // SBC ($aa,X)
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byIndirectX()));
            break;
        case 0xF1: // SBC ($aa),Y
            if (debug)
                debugPrint("SBC ");
            operateSub(read(byIndirectY()));
            break;
        case 0xEF: // UNDOCUMENTED : INS aaaa
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byAbsolute()));
            } else {
                usedUndocumentedCode("INS aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0xFF: // UNDOCUMENTED : INS aaaa,X
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byAbsoluteX()));
            } else {
                usedUndocumentedCode("INS aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0xFB: // UNDOCUMENTED : INS aaaa,Y
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byAbsoluteY()));
            } else {
                usedUndocumentedCode("INS aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0xE7: // UNDOCUMENTED : INS aa
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byZeroPage()));
            } else {
                usedUndocumentedCode("INS aa");
                halted = true;
                PC--;
            }
            break;
        case 0xF7: // UNDOCUMENTED : INS aa,X
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byZeroPageX()));
            } else {
                usedUndocumentedCode("INS aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0xE3: // UNDOCUMENTED : INS (aa,X)
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byIndirectX()));
            } else {
                usedUndocumentedCode("INS (aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0xF3: // UNDOCUMENTED : INS (aa),Y
            if (debug)
                debugPrint("INS ");
            if (useUndocumentedOpCodes) {
                operateCmp(A, increment(byIndirectY()));
            } else {
                usedUndocumentedCode("INS (aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0xC9: // CMP #aa
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, byImmediate());
            break;
        case 0xC5: // CMP $aa
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byZeroPage()));
            break;
        case 0xD5: // CMP $aa,X
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byZeroPageX()));
            break;
        case 0xCD: // CMP $aaaa
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byAbsolute()));
            break;
        // UP TO HERE
        case 0xDD: // CMP $aaaa,X
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byAbsoluteX()));
            break;
        case 0xD9: // CMP $aaaa,Y
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byAbsoluteY()));
            break;
        case 0xC1: // CMP ($aa,X)
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byIndirectX()));
            break;
        case 0xD1: // CMP ($aa),Y
            if (debug)
                debugPrint("CMP ");
            operateCmp(A, read(byIndirectY()));
            break;
        case 0xE0: // CPX #aa
            if (debug)
                debugPrint("CPX ");
            operateCmp(X, byImmediate());
            break;
        case 0xE4: // CPX $aa
            if (debug)
                debugPrint("CPX ");
            operateCmp(X, read(byZeroPage()));
            break;
        case 0xEC: // CPX $aaaa
            if (debug)
                debugPrint("CPX ");
            operateCmp(X, read(byAbsolute()));
            break;
        case 0xC0: // CPY #aa
            if (debug)
                debugPrint("CPY ");
            operateCmp(Y, byImmediate());
            break;
        case 0xC4: // CPY $aa
            if (debug)
                debugPrint("CPY ");
            operateCmp(Y, read(byZeroPage()));
            break;
        case 0xCC: // CPY $aaaa
            if (debug)
                debugPrint("CPY ");
            operateCmp(Y, read(byAbsolute()));
            break;
        case 0x8F: // UNDOCUMENTED : AXS $aaaa
            if (debug)
                debugPrint("AXS ");
            if (useUndocumentedOpCodes) {
                address = byAbsolute();
                writeVal = A & X;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("AXS $aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0x87: // UNDOCUMENTED : AXS $aa
            if (debug)
                debugPrint("AXS ");
            if (useUndocumentedOpCodes) {
                address = byZeroPage();
                writeVal = A & X;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("AXS $aa");
                halted = true;
                PC--;
            }
            break;
        case 0x97: // UNDOCUMENTED : AXS $aa,Y
            if (debug)
                debugPrint("AXS ");
            if (useUndocumentedOpCodes) {
                address = byZeroPageY();
                writeVal = A & X;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("AXS $aa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x83: // UNDOCUMENTED : AXS ($aa,X)
            if (debug)
                debugPrint("AXS ");
            if (useUndocumentedOpCodes) {
                address = byIndirectX();
                writeVal = A & X;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("AXS $aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0xCB: // UNDOCUMENTED : SAX #aa
            if (debug)
                debugPrint("SAX ");
            if (useUndocumentedOpCodes) {
                X = (A & X) - byImmediate();
                P |= X < 0 ? 0 : 1;
                X &= 0xFF;
                setStatusFlags(X);
            } else {
                usedUndocumentedCode("SAX #aa");
                halted = true;
                PC--;
            }
            break;
        case 0xAB: // UNDOCUMENTED : OAL #aa
            if (debug)
                debugPrint("OAL ");
            if (useUndocumentedOpCodes) {
                A |= 0xEE;
                A &= byImmediate();
                X = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("OAL #aa");
                halted = true;
                PC--;
            }
            break;
        case 0x4B: // UNDOCUMENTED : ALR #aa
            if (debug)
                debugPrint("ALR ");
            if (useUndocumentedOpCodes) {
                writeVal = A & byImmediate();
                A = LSR(A);
            } else {
                usedUndocumentedCode("ALR #aa");
                halted = true;
                PC--;
            }
            break;
        case 0x6B: // UNDOCUMENTED : ARR #aa
            if (debug)
                debugPrint("ARR ");
            if (useUndocumentedOpCodes) {
                writeVal = A & byImmediate();
                A = ROR(A);
            } else {
                usedUndocumentedCode("ARR #aa");
                halted = true;
                PC--;
            }
            break;
        case 0x8B: // UNDOCUMENTED : XAA #aa
            if (debug)
                debugPrint("XAA ");
            if (useUndocumentedOpCodes) {
                A = X & byImmediate();
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("XAA #aa");
                halted = true;
                PC--;
            }
            break;
        case 0xCF: // UNDOCUMENTED : DCM aaaa
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byAbsolute();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM aaaa");
                halted = true;
                PC--;
            }
            break;
        case 0xDF: // UNDOCUMENTED : DCM aaaa,X
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteX();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0xDB: // UNDOCUMENTED : DCM $aaaa,Y
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0xC7: // UNDOCUMENTED : DCM $aa
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byZeroPage();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM $aa");
                halted = true;
                PC--;
            }
            break;
        case 0xD7: // UNDOCUMENTED : DCM $aa,X
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byZeroPageX();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM $aa,X");
                halted = true;
                PC--;
            }
            break;
        case 0xC3: // UNDOCUMENTED : DCM (aa,X)
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byIndirectX();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM (aa,X)");
                halted = true;
                PC--;
            }
            break;
        case 0xD3: // UNDOCUMENTED : DCM (aa),Y
            if (debug)
                debugPrint("DCM ");
            if (useUndocumentedOpCodes) {
                address = byIndirectY();
                writeVal = (read(address) - 1) & 0xFF;
                operateCmp(A, writeVal);
                write(address, writeVal);
            } else {
                usedUndocumentedCode("DCM (aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0xBB: // UNDOCUMENTED : LAS aaaa,Y
            if (debug)
                debugPrint("LAS ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                A = read(address) & S;
                X = A;
                S = A;
                setStatusFlags(A);
            } else {
                usedUndocumentedCode("LAS aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x0B: // UNDOCUMENTED : ANC #aa
        case 0x2B:
            if (debug)
                debugPrint("ANC ");
            if (useUndocumentedOpCodes) {
                address = byImmediate();
                writeVal = read(address) & A;
                setStatusFlags(writeVal);
                P |= (P & 0x80) >> 7;
            } else {
                usedUndocumentedCode("ANC #aa");
                halted = true;
                PC--;
            }
            break;
        case 0x9B: // UNDOCUMENTED : TAS $aaaa,Y
            if (debug)
                debugPrint("TAS ");
            if (useUndocumentedOpCodes) {
                S = X & A;
                address = byAbsoluteY();
                writeVal = ((address & 0xFF00) >> 8) + 1;
                writeVal &= S;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("TAS $aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x9C: // UNDOCUMENTED : SAY $aaaa,X
            if (debug)
                debugPrint("SAY ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteX();
                writeVal = ((address & 0xFF00) >> 8) + 1;
                writeVal &= Y;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("SAY $aaaa,X");
                halted = true;
                PC--;
            }
            break;
        case 0x9E: // UNDOCUMENTED : XAS aaaa,Y
            if (debug)
                debugPrint("XAS ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = ((address & 0xFF00) >> 8) + 1;
                writeVal &= X;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("XAS aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x9F: // UNDOCUMENTED : AXA aaaa,Y
            if (debug)
                debugPrint("AXA ");
            if (useUndocumentedOpCodes) {
                address = byAbsoluteY();
                writeVal = ((address & 0xFF00) >> 8) + 1;
                writeVal &= X;
                writeVal &= A;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("AXA aaaa,Y");
                halted = true;
                PC--;
            }
            break;
        case 0x93: // UNDOCUMENTED : AXA (aa),Y
            if (debug)
                debugPrint("AXA ");
            if (useUndocumentedOpCodes) {
                address = byIndirectY();
                writeVal = ((address & 0xFF00) >> 8) + 1;
                writeVal &= X;
                writeVal &= A;
                write(address, writeVal);
            } else {
                usedUndocumentedCode("AXA (aa),Y");
                halted = true;
                PC--;
            }
            break;
        case 0x80: // UNDOCUMENTED : SKB
        case 0x82:
        case 0x89:
        case 0xC2:
        case 0xE2:
        case 0x04:
        case 0x14:
        case 0x34:
        case 0x44:
        case 0x54:
        case 0x64:
        case 0x74:
        case 0xD4:
        case 0xF4:
            if (debug)
                debugPrint("SKB ");
            if (useUndocumentedOpCodes) {
                PC++;
            } else {
                usedUndocumentedCode("SKB");
                halted = true;
                PC--;
            }
            break;
        case 0x0C: // UNDOCUMENTED : SKW
        case 0x1C:
        case 0x3C:
        case 0x5C:
        case 0x7C:
        case 0xDC:
        case 0xFC:
            if (debug)
                debugPrint("SKW ");
            if (useUndocumentedOpCodes) {
                PC += 2;
            } else {
                usedUndocumentedCode("SKW");
                halted = true;
                PC--;
            }
            break;
        case 0x1A: // UNDOCUMENTED : NOP
        case 0x3A:
        case 0x5A:
        case 0x7A:
        case 0xDA:
        case 0xEA:
        case 0xFA:
            if (debug)
                debugPrint("NOP ");
            break;
        case 0x02: // UNDOCUMENTED : HLT
        case 0x12:
        case 0x22:
        case 0x32:
        case 0x42:
        case 0x52:
        case 0x62:
        case 0x72:
        case 0x92:
        case 0xB2:
        case 0xD2:
        case 0xF2:
            if (debug)
                debugPrint("HLT ");
            halted = true;
            PC--;
            break;
        default: // Unknown OpCode so Hang
            PC--;
            break;
        }
        // Decrement Cycles by number of Cycles in Instruction
        cyclesPending -= cycles[instCode];
    }

    /**
     * Reset the Processor
     */
    private final void reset() {
        // Turn off the Reset Request
        resetRequest = false;
        // Save the SaveRAM
        nes.memory.saveSaveRAM();
        // Reset the Memory Manager
        nes.memory.init(currentCart, nes.currentCartFileName);
        // Reset the Mapper
        nes.mapper.init(nes.memory);
        // Reset the Internal CPU
        intReset();
        // Inform User that Reset was Successful
        nes.gui.writeToScreen("Game Reset");
    }

    /**
     * Correct the CPU Cycles for a Couple of Odd Games.
     */
    public void correctCPUCycles() {
        // Check Mapper Type
        if (nes.mapper == null) {
            CYCLES_PER_LINE = 116.0f;
            return;
        }
        // Get CRC for Cartridge
        long crc = currentCart.crc32;
        // Print CRC
        System.out.println(crc);
        // Identify the Game by Mapper then CRC
        switch (nes.mapper.getMapperNumber()) {
        case 0x04: {
            if (crc == 0xA0B0B742l) {
                // Mario 3 (U)
                CYCLES_PER_LINE = 144.0f;
                return;
            }
        }
        case 0x07: {
            if (crc == 0x279710DCl) {
                // BattleToads (U)
                CYCLES_PER_LINE = 112.0f;
                return;
            }
        }
        default: {
            CYCLES_PER_LINE = 116.0f;
            return;
        }
        }
    }

    /**
     * Issue Reset Request.
     */
    public final void cpuReset() {
        resetRequest = true;
    }

    /**
     * Reset the internal CPU registers.
     */
    private final void intReset() {
        // Correct CPU Cycles for Odd Games
        correctCPUCycles();
        // Reset the CPU Registers
        A = 0x00;
        X = 0x00;
        Y = 0x00;
        P = 0x04;
        S = 0xFF;
        halted = false;
        // Read the Reset Vector for PC Address
        PC = readWord(0xFFFC);
        if (debug)
            debugPrint("\n[RESET]");
    }

    /**
     * Request that the current CPU stops Processing.
     */
    public final void stopProcessing() {
        // Place the Stop Request
        stopRequest = true;
        // Wait for the Stop Request to be Received
        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }
    }

    /**
     * Wait while CPU is not Active.
     */
    private final void waitWhileNotActive() {
        // Wait while CPU is not Active
        while (!cpuActive) {
            // Draw the Screen
            deleteScreen();
            // Check for the Stop Request whilst non Active
            if (stopRequest)
                return;
            // Check for Load and Save State Requests
            if (nes.gui.isLoadStateRequest())
                nes.stateLoad();
            if (nes.gui.isSaveStateRequest())
                nes.stateSave();
            // Sleep for 1/10th of a Second
            try {
                Thread.sleep(100);
            } catch (Exception controlPausedException) {
            }
        }
    }

    /**
     * Wait while CPU is paused.
     */
    private final void waitWhilePaused() {
        // Wait while CPU is Paused
        while (cpuPaused) {
            // Draw the Screen
            drawScreen(true);
            // Check for Stop Request
            if (stopRequest)
                return;
            // Check for Load and Save State Requests
            if (nes.gui.isSaveStateRequest())
                nes.stateSave();
            if (nes.gui.isLoadStateRequest())
                nes.stateLoad();
            // Sleep for 1/10th of a Second
            try {
                Thread.sleep(100);
            } catch (Exception controlPausedException) {
            }
        }
    }

    //
    // Memory IO Functions
    //

    /**
     * Read Byte from memory.
     * 
     * @param address Address in memory to read from.
     * @return value at the specified address.
     */
    private final int read(int addr) {
        return nes.memory.read(addr);
    }

    /**
     * Read Word from memory.
     * 
     * @param address Address in memory to read from.
     * @return value at the specified address.
     */
    private final int readWord(int address) {
        return nes.memory.readWord(address);
    }

    /**
     * Write Byte to memory.
     * 
     * @param address Address in memory to write to.
     * @param value value to write.
     */
    private final void write(int address, int value) {
        nes.memory.write(address, value);
    }

    /**
     * Write Word to memory.
     * 
     * @param address Address in memory to write to.
     * @param value Value to write.
     */
    protected final void writeWord(int address, int value) {
        nes.memory.writeWord(address, value);
    }

    //
    // Addressing Mode Functions
    //
    /**
     * Get value by Immediate Mode Addressing - #$00
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byImmediate() {
        int i = read(PC++);
        if (debug)
            debugPrint("#0x" + Utils.hex(i, 2));
        return i;
    }

    /**
     * Get value by Absolute Mode Addressing - $aaaa
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byAbsolute() {
        int address = readWord(PC);
        if (debug)
            debugPrint("$0x" + Utils.hex(address, 4));
        PC += 2;
        return address;
    }

    /**
     * Get value by Absolute Y Mode Addressing - $aaaa,Y
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byAbsoluteY() {
        int i = byAbsolute();
        if (debug)
            debugPrint(",Y");
        int j = i + Y;
        checkPageBoundaryCrossing(i, j);
        return j;
    }

    /**
     * Get value by Absolute X Mode Addressing - $aaaa,X
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byAbsoluteX() {
        int i = byAbsolute();
        if (debug)
            debugPrint(",X");
        int j = i + X;
        checkPageBoundaryCrossing(i, j);
        return j;
    }

    /**
     * Get value by Zero Page Mode Addressing - $aa
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byZeroPage() {
        int address = read(PC++);
        if (debug)
            debugPrint("$0x" + Utils.hex(address, 2));
        return address;
    }

    /**
     * Get value by Zero Page X Mode Addressing - $aa,X </P>
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byZeroPageX() {
        int address = read(PC++);
        if (debug)
            debugPrint("$0x" + Utils.hex(address, 2) + ",X");
        return (address + X) & 0xff;
    }

    /**
     * Get value by Zero Page Y Mode Addressing - $aa,Y
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byZeroPageY() {
        int address = read(PC++);
        if (debug)
            debugPrint("$0x" + Utils.hex(address, 2) + ",Y");
        return address + Y & 0xff;
    }

    /**
     * Get value by Indirect X Mode Addressing - ($aa,X)
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byIndirectX() {
        int address = read(PC++);
        if (debug)
            debugPrint("($0x" + Utils.hex(address, 2) + ",X)");
        address += X;
        address &= 0xFF;
        return readWord(address);
    }

    /**
     * Get value by Indirect Y Mode Addressing - ($aa),Y
     * 
     * @return The value by the specified addressing mode in relation to the
     *         current PC.
     */
    private final int byIndirectY() {
        int address = read(PC++);
        if (debug)
            debugPrint("($0x" + Utils.hex(address, 2) + "),Y");
        address = readWord(address);
        checkPageBoundaryCrossing(address, address + Y);
        return address + Y;
    }

    //
    // Utility Functions
    //
    /**
     * Decrement the number of cycles pending if over a page boundary.
     * 
     * @param address1 The first address.
     * @param address2 The second address.
     */
    private final void checkPageBoundaryCrossing(int address1, int address2) {
        if (((address2 ^ address1) & 0x100) != 0)
            cyclesPending--;
    }

    /**
     * Set the Zero and Negative Status Flags.
     * 
     * @param value The value used to determine the Status Flags.
     */
    private final void setStatusFlags(int value) {
        P &= 0x7D;
        P |= znTable[value];
    }

    /**
     * Perform Arithmetic Shift Left.
     * 
     * @param i The value used by the function.
     */
    private final int ASL(int i) {
        P &= 0x7C;
        P |= i >> 7;
        i <<= 1;
        i &= 0xFF;
        P |= znTable[i];
        return i;
    }

    /**
     * Perform Logical Shift Right.
     * 
     * @param i The value used by the function.
     */
    private final int LSR(int i) {
        P &= 0x7C;
        P |= i & 0x1;
        i >>= 1;
        P |= znTable[i];
        return i;
    }

    /**
     * Perform Rotate Left.
     * 
     * @param i The value used by the function.
     */
    private final int ROL(int i) {
        i <<= 1;
        i |= P & 0x1;
        P &= 0x7C;
        P |= i >> 8;
        i &= 0xFF;
        P |= znTable[i];
        return i;
    }

    /**
     * Perform Rotate Right.
     * 
     * @param i The value used by the function.
     */
    private final int ROR(int i) {
        int j = P & 0x1;
        P &= 0x7C;
        P |= i & 0x1;
        i >>= 1;
        i |= j << 7;
        P |= znTable[i];
        return i;
    }

    /**
     * Perform Incrementation.
     * 
     * @param i The value used by the function.
     */
    private final int increment(int i) {
        i = ++i & 0xff;
        setStatusFlags(i);
        return i;
    }

    /**
     * Perform Decrementation.
     * 
     * @param i The value used by the function.
     */
    private final int decrement(int i) {
        i = --i & 0xff;
        setStatusFlags(i);
        return i;
    }

    /**
     * Perform Add with Carry (no decimal mode on NES).
     * 
     * @param i The value used by the function.
     */
    private final void operateAdd(int i) {
        // Store Carry
        int k = P & 0x1;
        // Store Add Result
        int j = A + i + k;
        // Turn Off CZN
        P &= 0x3C;
        // Set Overflow (V)
        P |= (~(A ^ i) & (A ^ i) & 0x80) == 0 ? 0 : 0x40;
        // Set Carry (C)
        P |= j <= 255 ? 0 : 0x1;
        // Set A
        A = j & 0xFF;
        // Set ZN
        P |= znTable[A];
    }

    /**
     * Perform Subtract with Carry (no decimal mode on NES).
     * 
     * @param i The value used by the function.
     */
    private final void operateSub(int i) {
        // Store Carry
        int k = ~P & 0x1;
        // Store Subtract Result
        int j = A - i - k;
        // Turn Off CZN
        P &= 0x3C;
        // Set Overflow (V)
        P |= (~(A ^ i) & (A ^ i) & 0x80) == 0 ? 0 : 0x40;
        // Set Carry
        P |= j < 0 ? 0 : 0x1;
        // Set A
        A = j & 0xFF;
        // Set ZN in P
        P |= znTable[A];
    }

    /**
     * Perform Compare Function.
     * 
     * @param i The first value.
     * @param j The second value.
     */
    private final void operateCmp(int i, int j) {
        int k = i - j;
        P &= 0x7C;
        P |= k < 0 ? 0 : 0x1;
        P |= znTable[k & 0xff];
    }

    /**
     * Perform Bit Function.
     * 
     * @param i The value used by the function.
     */
    private final void operateBit(int i) {
        P &= 0x3D;
        P |= i & 0xc0;
        P |= (A & i) != 0 ? 0 : 0x2;
    }

    /**
     * Function for Handling Branches
     * 
     * @param flagNum The byte value to compare.
     * @param flagVal The expected truth value for a branch.
     */
    private final void branch(int flagNum, boolean flagVal) {
        int offset = (byte) read(PC++);
        if (debug)
            debugPrint("$0x" + Utils.hex((PC + offset) & 0xFFFF, 4));
        if (((P & flagNum) != 0) == flagVal) {
            checkPageBoundaryCrossing(PC + offset, PC);
            PC = PC + offset;
            cyclesPending--;
        }
    }

    /**
     * Push a value onto the Stack.
     * 
     * @param stackVal The value to push.
     */
    private final void push(int stackVal) {
        write(S + 256, stackVal);
        S--;
        S &= 0xff;
    }

    /**
     * Pop a value from the Stack.
     * 
     * @return The value on top of the Stack.
     */
    private final int pop() {
        S++;
        S &= 0xff;
        return read(S + 256);
    }

    /**
     * Push a Word onto the Stack.
     * 
     * @param stackVal The 16 bit word to push.
     */
    private final void pushWord(int stackVal) {
        push((stackVal >> 8) & 0xFF);
        push(stackVal & 0xFF);
    }

    /**
     * Pop a Word from the Stack.
     * 
     * @return The 16 bit word on top of the Stack.
     */
    private final int popWord() {
        return pop() + pop() * 256;
    }

    public int getPC() {
        return PC;
    }

    //
    // Save State Functions
    //
    /**
     * Called when an Undocumented OpCode is executed
     */
    private final void usedUndocumentedCode(String message) {
        // Delete Display and Send Message
        deleteScreen();
        nes.gui.writeToScreen("Undocumented OpCode: " + message);
    }

    /**
     * Array of CPU Cycles for each Machine Code Instruction
     */
    private static final int[] cycles = {
        7, // 0x00 BRK
        6, // 0x01 ORA (aa,X)
        2, // 0x02 HLT
        8, // 0x03 ASO (ab,X)
        3, // 0x04 SKB
        3, // 0x05 ORA aa
        5, // 0x06 ASL aa
        5, // 0x07 ASO aa
        3, // 0x08 PHP
        2, // 0x09 ORA #aa
        2, // 0x0A ASL A
        2, // 0x0B ANC #aa
        4, // 0x0C SKW
        4, // 0x0D ORA aaaa
        6, // 0x0E ASL aaaa
        6, // 0x0F ASO aaaa
        2, // 0x10 BPL
        5, // 0x11 ORA (aa),Y
        2, // 0x12 HLT
        8, // 0x13 ASO (ab),Y
        4, // 0x14 SKB
        4, // 0x15 ORA aa,X
        6, // 0x16 ASL aa,X
        6, // 0x17 ASO aa,X
        2, // 0x18 CLC
        4, // 0x19 ORA aaaa,Y
        2, // 0x1A NOP
        7, // 0x1B ASO aaaa,Y
        5, // 0x1C SKW
        5, // 0x1D ORA aaaa,X
        7, // 0x1E ASL aaaa,X
        7, // 0x1F ASO aaaa,X
        6, // 0x20 JSR aaaa
        6, // 0x21 AND (aa,X)
        2, // 0x22 HLT
        8, // 0x23 RLA (aa,X)
        3, // 0x24 BIT aa
        3, // 0x25 AND aa
        5, // 0x26 ROL aa
        5, // 0x27 RLA aa
        4, // 0x28 PLP
        2, // 0x29 AND #aa
        2, // 0x2A ROL A
        2, // 0x2B ANC #aa
        4, // 0x2C BIT aaaa
        4, // 0x2D AND aaaa
        6, // 0x2E ROL aaaa
        6, // 0x2F RLA aaaa
        2, // 0x30 BMI aa
        5, // 0x31 AND (aa),Y
        2, // 0x32 HLT
        8, // 0x33 RLA (ab),Y
        4, // 0x34 SKB
        4, // 0x35 AND aa,X
        6, // 0x36 ROL aa,X
        6, // 0x37 RLA aa,X
        2, // 0x38 SEC
        4, // 0x39 AND aaaa,Y
        2, // 0x3A NOP
        7, // 0x3B RLA aaaa,Y
        5, // 0x3C SKW
        5, // 0x3D AND aaaa,X
        7, // 0x3E ROL aaaa,X
        7, // 0x3F RLA aaaa,X
        6, // 0x40 EOR aaaa
        6, // 0x41 EOR (aa,X)
        2, // 0x42 HLT
        8, // 0x43 LSE (aa,X)
        3, // 0x44 SKB
        3, // 0x45 EOR aa
        5, // 0x46 LSR aa
        5, // 0x47 LSE aa
        3, // 0x48 PHA
        2, // 0x49 EOR #aa
        2, // 0x4A LST A
        2, // 0x4B ALR #aa
        3, // 0x4C JMP aaaa
        4, // 0x4D RTI
        6, // 0x4E LSE aaaa
        6, // 0x4F LSE aaaa
        2, // 0x50 BVC aa
        5, // 0x51 EOR (aa),Y
        2, // 0x52 HLT
        8, // 0x53 LSE (aa),Y
        4, // 0x54 SKB
        4, // 0x55 EOR aa,X
        6, // 0x56 LSR aa,X
        6, // 0x57 LSE aa,X
        2, // 0x58 CLI
        4, // 0x59 EOR aaaa,Y
        2, // 0x5A NOP
        7, // 0x5B LSE aaaa,Y
        5, // 0x5C SKW
        5, // 0x5D EOR aaaa,X
        7, // 0x5E LSR aaaa,X
        7, // 0x5F LSE aaaa,X
        4, // 0x60 RTS
        6, // 0x61 ADC (aa,X)
        2, // 0x62 HLT
        8, // 0x63 RRA (aa,X)
        3, // 0x64 SKB
        3, // 0x65 ADC aa
        5, // 0x66 ROR aa
        5, // 0x67 RRA aa
        4, // 0x68 PLA
        2, // 0x69 ADC #aa
        2, // 0x6A ROR A
        2, // 0x6B ARR #aa
        5, // 0x6C JMP (aa)
        4, // 0x6D ADC aaaa
        6, // 0x6E ROR aaaa
        6, // 0x6F RRA aaaa
        4, // 0x70 BVS aa
        5, // 0x71 ADC (aa),Y
        2, // 0x72 HLT
        8, // 0x73 RRA (aa),Y
        4, // 0x74 SKB
        4, // 0x75 ADC aa,X
        6, // 0x76 ROR aa,X
        6, // 0x77 RRA aa,X
        2, // 0x78 SEI
        4, // 0x79 ADC aaaa,Y
        2, // 0x7A NOP
        7, // 0x7B RRA aaaa,Y
        5, // 0x7C SKW
        5, // 0x7D ADC aaaa,X
        7, // 0x7E ROR aaaa,X
        7, // 0x7F RRA aaaa,X
        2, // 0x80 SKB
        6, // 0x81 STA (aa,X)
        2, // 0x82 SKB
        6, // 0x83 AXS (aa,X)
        3, // 0x84 STY aa
        3, // 0x85 STA aa
        3, // 0x86 STX aa
        3, // 0x87 AXS aa
        2, // 0x88 DEY
        2, // 0x89 SKB
        2, // 0x8A TXA
        2, // 0x8B XAA #aa
        4, // 0x8C STY aaaa
        4, // 0x8D STA aaaa
        4, // 0x8E STX aaaa
        4, // 0x8F AXS aaaa
        2, // 0x90 BCC aa
        6, // 0x91 STA (aa),Y
        2, // 0x92 HLT
        6, // 0x93 AXA (aa),Y
        4, // 0x94 STY aa,X
        4, // 0x95 STA aa,X
        4, // 0x96 STX aa,Y
        4, // 0x97 AXS aa,Y
        2, // 0x98 TYA
        5, // 0x99 STA aaaa,Y
        2, // 0x9A TXS
        5, // 0x9B TAS aaaa,Y
        5, // 0x9C SAY aaaa,X
        5, // 0x9D STA aaaa,X
        5, // 0x9E XAS aaaa,Y
        5, // 0x9F AXA aaaa,Y
        2, // 0xA0 LDY #aa
        6, // 0xA1 LDA (aa,X)
        2, // 0xA2 LDX #aa
        6, // 0xA3 LAX (aa,X)
        3, // 0xA4 LDY aa
        3, // 0xA5 LDA aa
        3, // 0xA6 LDX aa
        3, // 0xA7 LAX aa
        2, // 0xA8 TAY
        2, // 0xA9 LDA #aa
        2, // 0xAA TAX
        2, // 0xAB OAL #aa
        4, // 0xAC LDY aaaa
        4, // 0xAD LDA aaaa
        4, // 0xAE LDX aaaa
        4, // 0xAF LAX aaaa
        2, // 0xB0 BCS aa
        5, // 0xB1 LDA (aa),Y
        2, // 0xB2 HLT
        5, // 0xB3 LAX (aa),Y
        4, // 0xB4 LDY aa,X
        4, // 0xB5 LDA aa,X
        4, // 0xB6 LDX aa,Y
        4, // 0xB7 LAX aa,Y
        2, // 0xB8 CLV
        4, // 0xB9 LDA aaaa,Y
        2, // 0xBA TSX
        4, // 0xBB LAS aaaa,Y
        4, // 0xBC LDY aaaa,X
        4, // 0xBD LDA aaaa,X
        4, // 0xBE LDX aaaa,Y
        4, // 0xBF LAX aaaa,Y
        2, // 0xC0 CPY #aa
        6, // 0xC1 CMP (aa,X)
        2, // 0xC2 SKB
        8, // 0xC3 DCM (aa,X)
        3, // 0xC4 CPY aa
        3, // 0xC5 CMP aa
        5, // 0xC6 DEC aa
        5, // 0xC7 DCM aa
        2, // 0xC8 INY
        2, // 0xC9 CMP #aa
        2, // 0xCA DEX
        2, // 0xCB SAX #aa
        4, // 0xCC CPY aaaa
        4, // 0xCD CMP aaaa
        6, // 0xCE DEC aaaa
        6, // 0xCF DCM aaaa
        2, // 0xD0 BNE aa
        5, // 0xD1 CMP (aa),Y
        2, // 0xD2 HLT
        8, // 0xD3 DCM (aa),Y
        4, // 0xD4 SKB
        4, // 0xD5 CMP aa,X
        6, // 0xD6 DEC aa,X
        6, // 0xD7 DCM aa,X
        2, // 0xD8 CLD
        4, // 0xD9 CMP aaaa,Y
        2, // 0xDA NOP
        7, // 0xDB DCM aaaa,Y
        5, // 0xDC SKW
        5, // 0xDD CMP aaaa,X
        7, // 0xDE DEC aaaa,X
        7, // 0xDF DCM aaaa,X
        2, // 0xE0 CPX #aa
        6, // 0xD1 SBC (aa,X)
        2, // 0xE2 SKB
        8, // 0xE3 INS (aa,X)
        3, // 0xE4 CPX aa
        3, // 0xE5 SBC aa
        5, // 0xE6 INC aa
        5, // 0xE7 INS aa
        2, // 0xE8 INX
        2, // 0xE9 SBC #aa
        2, // 0xEA NOP
        2, // 0xEB SBC #aa
        4, // 0xEC CPX aaaa
        4, // 0xED SBC aaaa
        6, // 0xEE INC aaaa
        6, // 0xEF INS aaaa
        2, // 0xF0 BEQ aa
        5, // 0xF1 SBC (aa),Y
        2, // 0xF2 HLT
        8, // 0xF3 INS (aa),Y
        4, // 0xF4 SKB
        4, // 0xF5 SBC aa,X
        6, // 0xF6 INC aa,X
        6, // 0xF7 INS aa,X
        2, // 0xF8 SED
        4, // 0xF9 SBC aaaa,Y
        2, // 0xFA NOP
        7, // 0xFB INS aaaa,Y
        5, // 0xFC SKW
        5, // 0xFD SBC aaaa,X
        7, // 0xFE INC aaaa,X
        7 // 0xFF INS aaaa,X
    };

    /**
     * Array of Zero and Negative Flags for Speedy Lookup
     */
    private static final int[] znTable = {
        002, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000, 000,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
        128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128
    };

    /**
     * Get Debug Enabled
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Set Debug
     */
    public void setDebug(boolean debug) {
        this.debugLatch = debug;
    }

    /**
     * Set Debug to Interrupt
     */
    public void setDebugToInterrupt(boolean val) {
        this.debugToInterrupt = val;
    }

    /**
     * Get Debug Instructions
     */
    public int getDebugInstructions() {
        return debugInstructions;
    }

    /**
     * Set Debug Instructions
     */
    public void setDebugInstructions(int instructions) {
        this.debugInstructions = instructions;
    }

    /**
     * Debug Print
     */
    private void debugPrint(String text) {
        System.out.print(text);
    }

    /**
     * Split P down into Components
     */
    protected String breakdownP(int f) {
        String buffer = "{";
        buffer += ((f & 0x80) != 0) ? "N=1," : "N=0,";
        buffer += ((f & 0x40) != 0) ? "V=1," : "V=0,";
        buffer += ((f & 0x10) != 0) ? "B=1," : "B=0,";
        buffer += ((f & 0x08) != 0) ? "D=1," : "D=0,";
        buffer += ((f & 0x04) != 0) ? "I=1," : "I=0,";
        buffer += ((f & 0x02) != 0) ? "Z=1," : "Z=0,";
        buffer += ((f & 0x01) != 0) ? "C=1" : "C=0";
        buffer += "}";
        return buffer;
    }
}
