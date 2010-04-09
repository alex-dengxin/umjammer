/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 245 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper245 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 245;
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
        // Set Program ROM Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        regs[0] = 0;
        irq_counter = 0;
        irq_latch = 0;
        irq_enabled = 0;
    }

    int[] regs = new int[1];
    int irq_counter = 0;
    int irq_latch = 0;
    int irq_enabled = 0;

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr & 0xF007) {
        case 0x8000: {
            regs[0] = data;
        }
            break;
        case 0x8001: {
            switch (regs[0] & 7) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                break;
            case 6: {
                setCPUBank8(data);
            }
                break;
            case 7: {
                setCPUBankA(data);
            }
                break;
            }
        }
            break;
        case 0xA000:
            break;
        case 0xA001:
            irq_enabled = data;
            break;
        case 0xE000:
            irq_counter = (irq_counter & 0xff00) | data;
            break;
        case 0xE001:
            irq_counter = (irq_counter & 0xff) | (data << 8);
            break;
        }
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if (irq_enabled != 0) {
            if (scanline < 241) {
                irq_counter -= 114;
                if (irq_counter <= 0) {
                    irq_enabled = 0;
                    return 3;
                }
            }
        }
        return 0;
    }
}
