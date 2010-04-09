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
public class Mapper226 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 226;
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

    int[] regs = new int[2];

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        // Check Range
        if (addr < 0x8000)
            return;
        if ((addr & 0x001) != 0) {
            regs[1] = data;
        } else {
            regs[0] = data;
        }
        if ((regs[0] & 0x40) != 0) {
            setMirroringVertical();
        } else {
            setMirroringHorizontal();
        }
        // Determine Program ROM Bank
        int prg_bank = ((regs[0] & 0x1E) >> 1) | ((regs[0] & 0x80) >> 3) | ((regs[1] & 0x01) << 5);
        if ((regs[0] & 0x20) != 0) {
            if ((regs[0] & 0x01) != 0) {
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
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
        regs[0] = 0;
        regs[1] = 0;
    }
}
