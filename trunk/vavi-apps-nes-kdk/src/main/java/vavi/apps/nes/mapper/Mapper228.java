/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for Mapper 228 used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper228 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 228;
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
        int prg_bank = (addr & 0x0780) >> 7;
        int chr_bank = ((addr & 0x000F) << 2) | (data & 0x03);
        switch ((addr & 0x1800) >> 11) {
        case 1: {
            prg_bank |= 0x10;
        }
            break;
        case 3: {
            prg_bank |= 0x20;
        }
            break;
        }
        if ((addr & 0x0020) != 0) {
            prg_bank = prg_bank << 1;
            if ((addr & 0x0040) != 0) {
                prg_bank++;
            }
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 0);
            setCPUBankE(prg_bank * 4 + 1);
        } else {
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 2);
            setCPUBankE(prg_bank * 4 + 3);
        }
        setPPUBank0(chr_bank * 8 + 0);
        setPPUBank1(chr_bank * 8 + 1);
        setPPUBank2(chr_bank * 8 + 2);
        setPPUBank3(chr_bank * 8 + 3);
        setPPUBank4(chr_bank * 8 + 4);
        setPPUBank5(chr_bank * 8 + 5);
        setPPUBank6(chr_bank * 8 + 6);
        setPPUBank7(chr_bank * 8 + 7);
        if ((addr & 0x2000) != 0) {
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
        setCPUBanks(0, 1, 2, 3);
        // Set PPU Banks
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }
}
