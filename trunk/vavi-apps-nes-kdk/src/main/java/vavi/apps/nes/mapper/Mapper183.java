/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 183 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper183 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 183;
    }

    /**
     * Irq Counter
     */
    private int irq_counter = 0;

    /**
     * Whether the IRQ has been Set
     */
    private int irq_enabled = 0;

    /**
     * Register to Store Function
     */
    private int[] regs = new int[8];

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
        // Set CPU Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set PPU Banks
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        // Clear Registers
        for (int i = 0; i < regs.length; i++)
            regs[i] = i;
        // Set Interrupts
        irq_enabled = 0;
        irq_counter = 0;
    }

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        // Check Range
        if (addr < 0x8000)
            return;
        // Perform Function
        switch (addr) {
        case 0x8800: {
            setCPUBank8(data);
        }
            break;
        case 0xA800: {
            setCPUBankA(data);
        }
            break;
        case 0xA000: {
            setCPUBankC(data);
        }
            break;
        case 0xB000: {
            regs[0] = (regs[0] & 0xf0) | (data & 0x0f);
            setPPUBank0(regs[0]);
        }
            break;
        case 0xB004: {
            regs[0] = (regs[0] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank0(regs[0]);
        }
            break;
        case 0xB008: {
            regs[1] = (regs[1] & 0xf0) | (data & 0x0f);
            setPPUBank1(regs[1]);
        }
            break;
        case 0xB00C: {
            regs[1] = (regs[1] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank1(regs[1]);
        }
            break;
        case 0xC000: {
            regs[2] = (regs[2] & 0xf0) | (data & 0x0f);
            setPPUBank2(regs[2]);
        }
            break;
        case 0xC004: {
            regs[2] = (regs[2] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank2(regs[2]);
        }
            break;
        case 0xC008: {
            regs[3] = (regs[3] & 0xf0) | (data & 0x0f);
            setPPUBank3(regs[3]);
        }
            break;
        case 0xC00C: {
            regs[3] = (regs[3] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank3(regs[3]);
        }
            break;
        case 0xD000: {
            regs[4] = (regs[4] & 0xf0) | (data & 0x0f);
            setPPUBank4(regs[4]);
        }
            break;
        case 0xD004: {
            regs[4] = (regs[4] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank4(regs[4]);
        }
            break;
        case 0xD008: {
            regs[5] = (regs[5] & 0xf0) | (data & 0x0f);
            setPPUBank5(regs[5]);
        }
            break;
        case 0xD00C: {
            regs[5] = (regs[5] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank5(regs[5]);
        }
            break;
        case 0xE000: {
            regs[6] = (regs[6] & 0xf0) | (data & 0x0f);
            setPPUBank6(regs[6]);
        }
            break;
        case 0xE004: {
            regs[6] = (regs[6] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank6(regs[6]);
        }
            break;
        case 0xE008: {
            regs[7] = (regs[7] & 0xf0) | (data & 0x0f);
            setPPUBank7(regs[7]);
        }
            break;
        case 0xE00C: {
            regs[7] = (regs[7] & 0x0f) | ((data & 0x0f) << 4);
            setPPUBank7(regs[7]);
        }
            break;
        case 0x9008: {
            if (data == 1) {
                for (int i = 0; i < regs.length; i++) {
                    regs[i] = i;
                }
                setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
            }
        }
            break;
        case 0x9800: {
            if (data == 0) {
                setMirroringVertical();
            } else if (data == 1) {
                setMirroringHorizontal();
            } else if (data == 2) {
                setMirroring(0, 0, 0, 0);
            } else if (data == 3) {
                setMirroring(1, 1, 1, 1);
            }
        }
            break;
        case 0xF000: {
            irq_counter = (irq_counter & 0xFF00) | data;
        }
            break;
        case 0xF004: {
            irq_counter = (irq_counter & 0x00FF) | (data << 8);
        }
            break;
        case 0xF008: {
            irq_enabled = data;
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
        if ((irq_enabled & 0x02) != 0) {
            if (irq_counter <= 113) {
                irq_counter = 0;
                return 3;
            } else {
                irq_counter -= 113;
            }
        }
        return 0;
    }
}
