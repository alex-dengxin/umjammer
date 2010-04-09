/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class for the Memory Manager required by the NESCafe NES Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class MemoryManager {
    /**
     * The current NES Machine.
     */
    public NES nes;

    /**
     * The current Picture Processing Unit.
     */
    public PPU ppu;

    /**
     * The main memory.
     */
    public int[] memory = new int[0x0800];

    /**
     * The Program ROM.
     */
    public int[] programROM = null;

    /**
     * Save RAM.
     */
    public int[] saveRAM = new int[0x10000];

    /**
     * Extended RAM from the Mapper Hardware
     */
    public int[] mapper_extram = new int[0x10000];

    /**
     * Size of Extended RAM from Mapper
     */
    public int mapper_extramsize = 0;

    /**
     * Respect Save RAM Writes.
     */
    public boolean enableSaveRAM = true;

    /**
     * The Program ROM Bank addresses.
     */
    private int[] bank = new int[16]; // Each bank is 0x1000

    /**
     * True if Save RAM was loaded with the Cart.
     */
    private boolean saveRAMWasLoaded = false;

    /**
     * Variable for Zapper Trigger
     */
    public int zapperTrigger = 0;

    /**
     * Variable for Last Record Zapper X Position
     */
    public int zapperX = 0;

    /**
     * Variable for Last Record Zapper Y Position
     */
    public int zapperY = 0;

    /**
     * Is the NESCafe ROM Currently Loaded
     */
    private boolean nescaferomloaded = false;

    /**
     * Create a new Memory Manager.
     */
    public MemoryManager(NES nes) {
        // Grab References to the GUI and NES
        this.nes = nes;
        this.ppu = nes.ppu;
    }

    /**
     * Return the current scanline number for Mappers.
     */
    public final int getScanline() {
        return nes.gui.getScanLine();
    }

    /**
     * Initialise the Memory Manager.
     */
    public final void init(NESCart game, String fileName) {
        // Fetch the Program ROM
        programROM = game.getProgROM();
        // Initialise the PPU Memory
        nes.ppu.PPUInit(game.getCharROM(), game.getMirroring(), game.getFourScreenNT());
        // Clear the Main Memory
        for (int i = 0; i < memory.length; i++)
            memory[i] = 0;
        // Load the Trainer ROM if it Exists
        if (game.hasTrainer && game.trainerROM != null) {
            for (int i = 0; i < 512; i++)
                saveRAM[0x1000 + i] = game.trainerROM[i];
        } else {
            for (int i = 0; i < 512; i++)
                saveRAM[0x1000 + i] = 0;
        }
        // Reset Frame IRQ Status
        nes.frameIRQEnabled = 0xFF;
        nes.frameIRQDisenabled = 0;
        // Load Save RAM if it Exists
        if (game.hasSaveRAM) {
            try {
                // Assume Successful Load
                saveRAMWasLoaded = true;
                // Attempt to Load
                if (fileName != null)
                    loadSaveRAM(fileName);
            } catch (Exception e) {
            }
        } else {
            saveRAMWasLoaded = false;
        }
        // Determine if this is the NESCafe Demo ROM
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 8; i++)
            sb.append((char) programROM[i]);
        // Check for NESCafe Signature
        nescaferomloaded = sb.toString().equals("NESCafe ");
    }

    /**
     * Method to Load the Save RAM for the current Cartridge.
     */
    public final void loadSaveRAM(String cartName) throws Exception {
        // Attempt to Create Save Directory
        File saveramDirectory = new File("saveram");
        // Construct the Filename
        String fileName = Utils.getFileName(cartName) + ".sav";
        // Check if Exists and Load
        File saveRAMFile = new File(saveramDirectory + "/" + fileName);
        if (saveRAMFile.exists() && saveRAMFile.isFile()) {
            // Read File
            FileInputStream saveFile = new FileInputStream(saveRAMFile);
            // Load the Data into SaveRAM
            for (int i = 0; i < saveRAM.length; i++)
                saveRAM[i] = saveFile.read() & 0xFF;
            // Close the File
            saveFile.close();
        } else {
            // Clear the SaveRAM
            for (int i = 0; i < saveRAM.length; i++)
                saveRAM[i] = 0;
        }
    }

    /**
     * Method to Save the Save RAM from the current Cartridge.
     */
    public final void saveSaveRAM() {
        // Check that SaveRAM was Loaded Originally
        if (!saveRAMWasLoaded)
            return;
        try {
            // Attempt to Create Save Directory
            File saveramDirectory = new File("saveram");
            if (saveramDirectory.exists() && !saveramDirectory.isDirectory()) {
                saveramDirectory.delete();
            }
            if (!saveramDirectory.exists()) {
                saveramDirectory.mkdir();
            }
            // Construct the Filename
            String fileName = saveramDirectory + "/" + Utils.getFileName(nes.currentCartFileName) + ".sav";
            FileOutputStream saveFile = new FileOutputStream(fileName);
            // Write the SaveRAM to Disk
            for (int i = 0; i < saveRAM.length; i++)
                saveFile.write(saveRAM[i] & 0xFF);
            // Close the File
            saveFile.close();
        } catch (IOException e) {
        }
    }

    /**
     * Sets the offset in Program ROM of a Memory Bank.
     * 
     * @param bankNum The bank number to configure (0-15).
     * @param offsetInPRGROM The offset in Program ROM that the bank starts at.
     */
    public final void setBankStartAddress(int bankNum, int offsetInPRGROM) {
        offsetInPRGROM %= programROM.length;
        bank[bankNum % bank.length] = offsetInPRGROM;
    }

    /**
     * Read from Memory.
     * 
     * @return The value at the specified address.
     */
    public final int read(int addr) {
        if (addr < 0x2000) {
            // RAM Mirrored 4 Times
            return memory[addr & 0x7FF];
        } else if (addr < 0x4000) {
            // Input/Output
            return nes.ppu.read(addr & 0xE007);
        } else if (addr < 0x4016) {
            // SPR-RAM DMA
            if (addr == 0x4014) {
                return 0;
            } else if (addr == 0x4015 && ((nes.frameIRQEnabled & 0xC0) == 0)) {
                return 0x40;
            }
            // Read from Sound Chip
            return 0;
        } else if (addr < 0x4018) {
            // High I/O Regs
            if (addr == 0x4016) {
                // Joypad #1
                return nes.joyPad1.readJoyPadBit();
            } else if (addr == 0x4017) {
                // Joypad #2
                return nes.joyPad2.readJoyPadBit();
            }
            return 0;
        } else if (addr < 0x6000) {
            // Expansion ROM
            return nes.mapper.accesslowread(addr);
        } else if (addr < 0x8000) {
            // 0x6000 - 0x7FFF SaveRAM
            return saveRAM[addr - 0x6000];
        } else {
            // Check if Game Genie is Enabled
            // Read the Memory Address
            try {
                // Get Offset
                int c = bank[((addr & 0xF000) >> 12)];
                // Return Memory
                return programROM[c + (addr & 0x0FFF)];
            } catch (Exception e) {
                return 0;
            }
        }
    }

    /**
     * Output File Used for Developer Debugging of Memory
     */
    private FileOutputStream outputDebug = null;

    /**
     * Method used for Developer Debugging
     */
    public void debugOutput(String line) {
        try {
            if (outputDebug != null)
                outputDebug.write(line.getBytes());
        } catch (Exception e) {
        }
    }

    /**
     * Method used for Developer Debugging
     */
    public void debugDump() {
        for (int i = 0; i < 0x3FF; i++)
            debugOutput("0x" + Utils.hex(i, 4) + "," + memory[i] + ",\r\n");
    }

    /**
     * Method used for Developer Debugging
     */
    public void debugOpen() {
        try {
            outputDebug = new FileOutputStream(new File("debug.csv"));
        } catch (Exception e) {
            outputDebug = null;
        }
    }

    /**
     * Method used for Developer Debugging
     */
    public void debugClose() {
        try {
            outputDebug.close();
            outputDebug = null;
        } catch (Exception e) {
        }
    }

    /**
     * Write to Memory.
     */
    public final void write(int addr, int value) {
        if (addr < 0x2000) {
            // 0x0000 - 0x1FFF RAM
            memory[addr & 0x7FF] = value;
            return;
        } else if (addr < 0x4000) {
            // Low IO Registers
            nes.ppu.write(addr & 0xE007, value);
            return;
        } else if (addr < 0x4018) {
            // High IO Registers
            nes.mapper.accesslow(addr, value);
            switch (addr) {
            case 0x4000:
            case 0x4001:
            case 0x4002:
            case 0x4003:
            case 0x4004:
            case 0x4005:
            case 0x4006:
            case 0x4007:
            case 0x4008:
            case 0x4009:
            case 0x400A:
            case 0x400B:
            case 0x400C:
            case 0x400D:
            case 0x400E:
            case 0x400F:
            case 0x4010:
            case 0x4011:
            case 0x4012:
            case 0x4013:
                return;
            case 0x4014: // Sprite DMA Register
                int source[] = memory;
                int k = value << 8;
                switch (k & 0xF000) {
                case 0x8000: // DMA Transfer from Program ROM
                    source = programROM;
                    k = bank[(k >> 12) + (k & 0x0FFF)];
                    break;
                case 0x6000: // DMA Transfer from SaveRAM
                case 0x7000:
                    System.out.println("Sprite DMA Attempted from SaveRAM");
                    return;
                case 0x5000: // DMA Transfer from Expansion RAM
                    System.out.println("Sprite DMA Attempted from Expansion RAM");
                    return;
                case 0x2000: // DMA Transfer from Registers
                case 0x3000:
                case 0x4000:
                    return;
                case 0x0000: // DMA Transfer from RAM
                case 0x1000:
                    source = memory;
                    k &= 0x7FF;
                    break;
                }
                // Perform the DMA Transfer
                for (int i = 0; i < 256; i++) {
                    nes.ppu.spriteMemory[i] = source[k] & 0xFF;
                    k++;
                }
                // Burn Some Cycles
                nes.cpu.eatCycles(514);
                return;
            case 0x4015:
                return;
            case 0x4016: // Joypad #1
                if ((value & 0x1) == 0)
                    nes.joyPad1.resetJoyPad();
                return;
            case 0x4017: // Joypad #2
                if ((value & 0x1) == 0)
                    nes.joyPad2.resetJoyPad();
                if (nes.frameIRQDisenabled == 0) {
                    nes.frameIRQEnabled = value;
                }
                return;
            }
            return;
        } else if (addr < 0x6000) {
            // Expansion ROM and Low Mapper Write Region
            nes.mapper.accesslow(addr, value);
        } else if (addr < 0x8000) {
            // Save RAM
            nes.mapper.access(addr, value);
            if (enableSaveRAM)
                saveRAM[addr - 0x6000] = value;
            return;
        } else {
            nes.mapper.access(addr, value);
        }
    }

    /**
     * Read a 16 bit Word from Memory.
     */
    public final int readWord(int address) {
        return read(address) | (read(address + 1) << 8);
    }

    /**
     * Write a 16 bit Word to Memory.
     */
    public final void writeWord(int address, int value) {
        write(address, value & 0xFF);
        write(address + 1, value >> 8);
    }

    /**
     * Returns the Program ROM.
     */
    public final int[] getProgramROM() {
        return programROM;
    }

    /**
     * Loads the State of the Memory from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load Memory
        for (int i = 0; i < memory.length; i++)
            memory[i] = input.read() & 0xFF;
        // Load SaveRAM
        for (int i = 0; i < saveRAM.length; i++)
            saveRAM[i] = input.read() & 0xFF;
        // Load Bank Addresses
        for (int i = 0; i < bank.length; i++) {
            bank[i] = (input.read() & 0xFF) << 0x00;
            bank[i] |= (input.read() & 0xFF) << 0x08;
            bank[i] |= (input.read() & 0xFF) << 0x10;
            bank[i] |= (input.read() & 0xFF) << 0x18;
        }
        // Load Emulation Information
        enableSaveRAM = (input.read() == 0xFF);
        saveRAMWasLoaded = (input.read() == 0xFF);
    }

    /**
     * Saves the State of the Memory to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Memory
        for (int i = 0; i < memory.length; i++)
            output.write(memory[i] & 0xFF);
        // Save SaveRAM
        for (int i = 0; i < saveRAM.length; i++)
            output.write(saveRAM[i] & 0xFF);
        // Save Bank Addresses
        for (int i = 0; i < bank.length; i++) {
            output.write((bank[i] >> 0x00) & 0xFF);
            output.write((bank[i] >> 0x08) & 0xFF);
            output.write((bank[i] >> 0x10) & 0xFF);
            output.write((bank[i] >> 0x18) & 0xFF);
        }
        // Emulation Information
        output.write(enableSaveRAM ? 0xFF : 0x00);
        output.write(saveRAMWasLoaded ? 0xFF : 0x00);
    }

    /**
     * Check for NESCafe ROM
     */
    public boolean isNESCafeROM() {
        // Return whether NESCafe ROM Loaded - Determined on Init
        return nescaferomloaded;
    }
}
