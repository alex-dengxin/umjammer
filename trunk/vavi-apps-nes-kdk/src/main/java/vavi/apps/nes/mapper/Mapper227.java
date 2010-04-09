/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper E3 Controller (1200-in-1) used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper227 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 227;
    }

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
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        // Check Range
        if (addr < 0x8000)
            return;
        // Determine Program ROM Bank
        int prg_bank = ((addr & 0x0100) >> 4) | ((addr & 0x0078) >> 3);
        // Perform Function
        if ((addr & 0x0001) != 0) {
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 2);
            setCPUBankE(prg_bank * 4 + 3);
        } else {
            if ((addr & 0x0004) != 0) {
                setCPUBank8(prg_bank * 4 + 2);
                setCPUBankA(prg_bank * 4 + 3);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
            } else {
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 0);
                setCPUBankE(prg_bank * 4 + 1);
            }
        }
        if ((addr & 0x0080) == 0) {
            if ((addr & 0x0200) != 0) {
                setCPUBankC((prg_bank & 0x1C) * 4 + 14);
                setCPUBankE((prg_bank & 0x1C) * 4 + 15);
            } else {
                setCPUBankC((prg_bank & 0x1C) * 4 + 0);
                setCPUBankE((prg_bank & 0x1C) * 4 + 1);
            }
        }
        if ((addr & 0x0002) != 0) {
            setMirroringHorizontal();
        } else {
            setMirroringVertical();
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set CPU Banks
        setCPUBanks(0, 1, 0, 1);
        // Set PPU Banks
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }
}
