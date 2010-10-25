/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 41 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper041 extends Mapper {
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
    int regs[] = new int[1];

    public final void access(int addr, int data) {
        if (addr >= 0x6000 && addr < 0x6800) {
            regs[0] = (addr & 0xFF);
            setCPUBank8((regs[0] & 0x07) * 4 + 0);
            setCPUBankA((regs[0] & 0x07) * 4 + 1);
            setCPUBankC((regs[0] & 0x07) * 4 + 2);
            setCPUBankE((regs[0] & 0x07) * 4 + 3);
            if ((regs[0] & 0x20) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
        if (addr >= 0x8000) {
            if ((regs[0] & 0x04) != 0) {
                int chr_bank = ((regs[0] & 0x18) >> 1) | (data & 0x03);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
    }

    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 41;
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Initialise Memory
        setCPUBanks(0, 1, 2, 3);
        // Set PPU Banks
        if (getNum1KVROMBanks() > 0)
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }
}
