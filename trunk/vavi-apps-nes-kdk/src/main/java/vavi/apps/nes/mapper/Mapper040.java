/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 40 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper040 extends Mapper {
    /**
     * Method to initialise the Memory Mapper.
     * 
     * @param mm Memory Manager to initialise the Mapper with.
     */
    public void init(MemoryManager mm) {
        // Assign Local Pointer for Memory Manager Object
        this.mm = mm;
        // Cause a Reset
        reset();
    }

    /**
     * Whether the Mapper IRQ is Enabled.
     */
    private boolean irqEnabled = false;

    /**
     * The number of scanlines until the IRQ is triggered.
     */
    private int linesToIrq = 0;

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int value) {
        // Check Within Range
        if (addr < 0x8000)
            return;
        // Check for Mapper Function
        switch (addr & 0xE000) {
        case 0x8000:
            // Disable the IRQ
            irqEnabled = false;
            linesToIrq = 0;
            break;
        case 0xA000:
            // Enable the IRQ Counter and Set IRQ for 36 Scanlines
            irqEnabled = true;
            linesToIrq = 37; // used to be 36
            break;
        case 0xC000:
            // This Memory Area is not Mapped
            break;
        case 0xE000:
            // Switch Bank C
            setCPUBankC(value & 0x07);
            break;
        }
    }

    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 40;
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Reset the IRQ Status
        irqEnabled = false;
        linesToIrq = 0;
        // Copy BankC of Program ROM to Save RAM
        for (int i = 0; i < 0x2000; i++)
            mm.saveRAM[i] = mm.programROM[0xC000 + i];
        // Initialise Memory
        setCPUBanks(4, 5, 0, 7);
        // Set PPU Banks
        if (getNum1KVROMBanks() > 0)
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     */
    public final int syncH(int scanline) {
        // Perform IRQ Check
        if (irqEnabled) {
            if ((linesToIrq--) <= 0)
                return 3;
        }
        // No IRQ Triggered
        return 0;
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load Mapper Information
        irqEnabled = (input.read() == 0xFF);
        linesToIrq = (input.read() & 0xFF);
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Mapper Information
        output.write(irqEnabled ? 0xFF : 0x00);
        output.write(linesToIrq & 0xFF);
    }
}
