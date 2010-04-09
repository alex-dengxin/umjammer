/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for Mapper E2
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper225 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 225;
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
        int prg_bank = (addr & 0x0F80) >> 7;
        int chr_bank = addr & 0x003F;
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
        if ((addr & 0x1000) != 0) {
            // 16KBbank
            if ((addr & 0x0040) != 0) {
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
        } else {
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 2);
            setCPUBankE(prg_bank * 4 + 3);
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set CPU Banks
        setCPUBanks(0, 1, 2, 3);
        // Set VROM Banks
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
    }
}
