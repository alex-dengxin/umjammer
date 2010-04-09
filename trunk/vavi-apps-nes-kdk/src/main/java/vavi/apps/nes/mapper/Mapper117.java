/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 117
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper117 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 117;
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
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set the Video ROM
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
        irq_line = 0;
        irq_enabled1 = 0;
        irq_enabled2 = 1;
    }

    int irq_line = 0;

    int irq_enabled1 = 0;

    int irq_enabled2 = 1;

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr) {
        case 0x8000: {
            setCPUBank8(data);
        }
            break;
        case 0x8001: {
            setCPUBankA(data);
        }
            break;
        case 0x8002: {
            setCPUBankC(data);
        }
            break;
        case 0xA000: {
            setPPUBank0(data);
        }
            break;
        case 0xA001: {
            setPPUBank1(data);
        }
            break;
        case 0xA002: {
            setPPUBank2(data);
        }
            break;
        case 0xA003: {
            setPPUBank3(data);
        }
            break;
        case 0xA004: {
            setPPUBank4(data);
        }
            break;
        case 0xA005: {
            setPPUBank5(data);
        }
            break;
        case 0xA006: {
            setPPUBank6(data);
        }
            break;
        case 0xA007: {
            setPPUBank7(data);
        }
            break;
        case 0xA008:
        case 0xA009:
        case 0xA00a:
        case 0xA00b:
        case 0xA00c:
        case 0xA00d:
        case 0xA00e:
        case 0xA00f:
            break;
        case 0xC001:
        case 0xC002:
        case 0xC003: {
            irq_enabled1 = data;
            irq_line = data;
        }
            break;
        case 0xE000: {
            irq_enabled2 = data & 1;
        }
            break;
        }
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if (irq_enabled1 != 0 && irq_enabled2 != 0 && irq_line == scanline) {
            irq_enabled1 = 0;
            return 3;
        }
        return 0;
    }
}
