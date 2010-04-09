/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 50 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper050 extends Mapper {
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
    private boolean irq_enabled = false;

    /**
     * Access the Mapper.
     */
    public final void accesslow(int addr, int data) {
        if ((addr & 0xE060) == 0x4020) {
            if ((addr & 0x0100) != 0) {
                irq_enabled = (data & 0x01) != 0;
            } else {
                setCPUBankC((data & 0x08) | ((data & 0x01) << 2) | ((data & 0x06) >> 1));
            }
        }
    }

    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 50;
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Reset the IRQ Status
        irq_enabled = false;
        // Copy BankC of Program ROM to Save RAM
        for (int i = 0; i < 0x2000; i++)
            mm.saveRAM[i] = mm.programROM[0x1E000 + i];
        // Initialise Memory
        setCPUBanks(8, 9, 0, 11);
        // Set PPU Banks
        if (getNum1KVROMBanks() > 0)
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     */
    public final int syncH(int scanline) {
        // Perform IRQ Check
        if (irq_enabled) {
            if (scanline == 21)
                return 3;
        }
        // No IRQ Triggered
        return 0;
    }
}
