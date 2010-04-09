/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 113
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper113 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 113;
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
     * Reset the Mapper.
     */
    public final void reset() {
        // set CPU bank pointers
        setCPUBanks(0, 1, 2, 3);
        // Set the Video ROM
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
    }

    /**
     * Access the Low Region of the Mapper.
     */
    public final void accesslow(int addr, int data) {
        switch (addr) {
        case 0x4100:
        case 0x4111:
        case 0x4120:
        case 0x4900: {
            int prg_bank = 0;
            int chr_bank = 0;
            prg_bank = data >> 3;
            if (getNum8KRomBanks() <= 8 && getNum1KVROMBanks() == 8 * 16) {
                chr_bank = ((data >> 3) & 0x08) + (data & 0x07);
            } else {
                chr_bank = data & 0x07;
            }
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 2);
            setCPUBankE(prg_bank * 4 + 3);
            setPPUBank0(chr_bank * 8 + 0);
            setPPUBank1(chr_bank * 8 + 1);
            setPPUBank2(chr_bank * 8 + 2);
            setPPUBank3(chr_bank * 8 + 3);
            setPPUBank4(chr_bank * 8 + 4);
            setPPUBank5(chr_bank * 8 + 5);
            setPPUBank6(chr_bank * 8 + 6);
            setPPUBank7(chr_bank * 8 + 7);
        }
            break;
        }
    }

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        switch (addr) {
        case 0x8008:
        case 0x8009: {
            int prg_bank = 0;
            int chr_bank = 0;
            prg_bank = data >> 3;
            if (getNum8KRomBanks() <= 8 && getNum1KVROMBanks() == 8 * 16) {
                chr_bank = ((data >> 3) & 0x08) + (data & 0x07);
            } else {
                chr_bank = data & 0x07;
            }
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 2);
            setCPUBankE(prg_bank * 4 + 3);
            setPPUBank0(chr_bank * 8 + 0);
            setPPUBank1(chr_bank * 8 + 1);
            setPPUBank2(chr_bank * 8 + 2);
            setPPUBank3(chr_bank * 8 + 3);
            setPPUBank4(chr_bank * 8 + 4);
            setPPUBank5(chr_bank * 8 + 5);
            setPPUBank6(chr_bank * 8 + 6);
            setPPUBank7(chr_bank * 8 + 7);
        }
            break;
        }
    }
}
