/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Mapper 73
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper073 extends Mapper {
    int irq_counter = 0;

    int irq_enabled = 0;

    public void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr) {
        case 0x8000: {
            irq_counter = (irq_counter & 0xFFF0) | (data & 0x0F);
        }
            break;
        case 0x9000: {
            irq_counter = (irq_counter & 0xFF0F) | ((data & 0x0F) << 4);
        }
            break;
        case 0xA000: {
            irq_counter = (irq_counter & 0xF0FF) | ((data & 0x0F) << 8);
        }
            break;
        case 0xB000: {
            irq_counter = (irq_counter & 0x0FFF) | ((data & 0x0F) << 12);
        }
            break;
        case 0xC000: {
            irq_enabled = data;
        }
            break;
        case 0xF000: {
            setCPUBank8(data * 2 + 0);
            setCPUBankA(data * 2 + 1);
        }
            break;
        }
    }

    /**
     * Determine the number of the Memory Mapper.
     * 
     * @return The number of the Memory Mapper.
     */
    public int getMapperNumber() {
        return 73;
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
     * Reset the Memory Mapper.
     */
    public void reset() {
        irq_counter = 0;
        irq_enabled = 0;
        // set CPU bank pointers
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if ((irq_enabled & 0x02) != 0) {
            if (irq_counter > 0xFFFF - 114) {
                irq_enabled = 0;
                return 3;
            } else {
                irq_counter += 114;
            }
        }
        return 0;
    }
}
