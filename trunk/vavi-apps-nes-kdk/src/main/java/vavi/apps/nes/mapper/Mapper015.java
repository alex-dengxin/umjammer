/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 15 Controller (100-in-1) used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper015 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 15;
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
        // Execute Function
        switch (addr) {
        case 0x8000: {
            if ((data & 0x80) != 0) {
                setCPUBank8((data & 0x3F) * 2 + 1);
                setCPUBankA((data & 0x3F) * 2 + 0);
                setCPUBankC((data & 0x3F) * 2 + 3);
                setCPUBankE((data & 0x3F) * 2 + 2);
            } else {
                setCPUBank8((data & 0x3F) * 2 + 0);
                setCPUBankA((data & 0x3F) * 2 + 1);
                setCPUBankC((data & 0x3F) * 2 + 2);
                setCPUBankE((data & 0x3F) * 2 + 3);
            }
            if ((data & 0x40) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
            break;
        case 0x8001: {
            if ((data & 0x80) != 0) {
                setCPUBankC((data & 0x3F) * 2 + 1);
                setCPUBankE((data & 0x3F) * 2 + 0);
            } else {
                setCPUBankC((data & 0x3F) * 2 + 0);
                setCPUBankE((data & 0x3F) * 2 + 1);
            }
        }
            break;
        case 0x8002: {
            if ((data & 0x80) != 0) {
                setCPUBank8((data & 0x3F) * 2 + 1);
                setCPUBankA((data & 0x3F) * 2 + 1);
                setCPUBankC((data & 0x3F) * 2 + 1);
                setCPUBankE((data & 0x3F) * 2 + 1);
            } else {
                setCPUBank8((data & 0x3F) * 2);
                setCPUBankA((data & 0x3F) * 2);
                setCPUBankC((data & 0x3F) * 2);
                setCPUBankE((data & 0x3F) * 2);
            }
        }
            break;
        case 0x8003: {
            if ((data & 0x80) != 0) {
                setCPUBankC((data & 0x3F) * 2 + 1);
                setCPUBankE((data & 0x3F) * 2 + 0);
            } else {
                setCPUBankC((data & 0x3F) * 2 + 0);
                setCPUBankE((data & 0x3F) * 2 + 1);
            }
            if ((data & 0x40) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
            break;
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set CPU Banks
        setCPUBanks(0, 1, 2, 3);
    }
}
