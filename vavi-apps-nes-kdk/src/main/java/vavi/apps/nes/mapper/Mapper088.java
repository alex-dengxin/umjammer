/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 88
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper088 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 88;
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

    int regs[] = new int[1];

    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr) {
        case 0x8000: {
            regs[0] = data;
        }
            break;
        case 0x8001: {
            switch (regs[0] & 0x07) {
            case 0x00: {
                setPPUBank0(data & 0xFE);
                setPPUBank1((data & 0xFE) + 1);
            }
                break;
            case 0x01: {
                setPPUBank2(data & 0xFE);
                setPPUBank3((data & 0xFE) + 1);
            }
                break;
            case 0x02: {
                setPPUBank4(data | 0x40);
            }
                break;
            case 0x03: {
                setPPUBank5(data | 0x40);
            }
                break;
            case 0x04: {
                setPPUBank6(data | 0x40);
            }
                break;
            case 0x05: {
                setPPUBank7(data | 0x40);
            }
                break;
            case 0x06: {
                setCPUBank8(data);
            }
                break;
            case 0x07: {
                setCPUBankA(data);
            }
                break;
            }
        }
            break;
        case 0xC000: {
            if ((data & 0x40) != 0) {
                setMirroring(1, 1, 1, 1);
            } else {
                setMirroring(0, 0, 0, 0);
            }
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
        // Set the Video ROM
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
    }
}
