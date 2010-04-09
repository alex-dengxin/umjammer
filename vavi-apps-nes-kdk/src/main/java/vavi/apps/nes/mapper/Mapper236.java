/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 236
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper236 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 236;
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
        if (0x8000 <= addr && addr <= 0xbfff) {
            bank = ((addr & 0x03) << 4) | (bank & 0x07);
        } else {
            bank = (addr & 0x07) | (bank & 0x30);
            mode = addr & 0x30;
        }
        if ((addr & 0x20) != 0) {
            setMirroringHorizontal();
        } else {
            setMirroringVertical();
        }
        switch (mode) {
        case 0x00: {
            bank |= 0x08;
            setCPUBank8(bank * 2 + 0);
            setCPUBankA(bank * 2 + 1);
            setCPUBankC((bank | 0x07) * 2 + 0);
            setCPUBankE((bank | 0x07) * 2 + 1);
        }
            break;
        case 0x10: {
            bank &= 0x37;
            setCPUBank8(bank * 2 + 0);
            setCPUBankA(bank * 2 + 1);
            setCPUBankC((bank | 0x07) * 2 + 0);
            setCPUBankE((bank | 0x07) * 2 + 1);
        }
            break;
        case 0x20: {
            bank |= 0x08;
            setCPUBank8((bank & 0xfe) * 2 + 0);
            setCPUBankA((bank & 0xfe) * 2 + 1);
            setCPUBankC((bank & 0xfe) * 2 + 2);
            setCPUBankE((bank & 0xfe) * 2 + 3);
        }
            break;
        case 0x30: {
            bank |= 0x08;
            setCPUBank8(bank * 2 + 0);
            setCPUBankA(bank * 2 + 1);
            setCPUBankC(bank * 2 + 0);
            setCPUBankE(bank * 2 + 1);
        }
            break;
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // set CPU bank pointers
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        bank = 0;
        mode = 0;
    }

    int bank = 0;
    int mode = 0;
}
