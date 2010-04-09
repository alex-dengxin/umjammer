/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 21 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper021 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 21;
    }

    /**
     * Irq Counter
     */
    private int irq_counter = 0;

    /**
     * Irq Latch
     */
    private int irq_latch = 0;

    /**
     * Whether the IRQ has been Set
     */
    private int irq_enabled = 0;

    /**
     * Whether the IRQ has been Set
     */
    private int regs[] = new int[9];

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
        regs[1] = 1;
        regs[2] = 2;
        regs[3] = 3;
        regs[4] = 4;
        regs[5] = 5;
        regs[6] = 6;
        regs[7] = 7;
        regs[8] = 0;
        // IRQ Settings
        irq_enabled = 0;
        irq_latch = 0;
        irq_counter = 0;
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        // Check Range
        if (addr < 0x8000)
            return;
        // regs[0] ... 1K VROM bank at PPU $0000
        // regs[1] ... 1K VROM bank at PPU $0400
        // regs[2] ... 1K VROM bank at PPU $0800
        // regs[3] ... 1K VROM bank at PPU $0C00
        // regs[4] ... 1K VROM bank at PPU $1000
        // regs[5] ... 1K VROM bank at PPU $1400
        // regs[6] ... 1K VROM bank at PPU $1800
        // regs[7] ... 1K VROM bank at PPU $1C00
        // regs[8] ... $8000 Switching Mode
        switch (addr & 0xF0CF) {
        case 0x8000: {
            if ((regs[8] & 0x02) != 0) {
                setCPUBankC(data);
            } else {
                setCPUBank8(data);
            }
        }
            break;
        case 0xA000: {
            setCPUBankA(data);
        }
            break;
        case 0x9000: {
            data &= 0x03;
            if (data == 0) {
                setMirroringVertical();
            } else if (data == 1) {
                setMirroringHorizontal();
            } else if (data == 2) {
                setMirroring(0, 0, 0, 0);
            } else {
                setMirroring(1, 1, 1, 1);
            }
        }
            break;
        case 0x9002:
        case 0x9080: {
            regs[8] = data;
        }
            break;
        case 0xB000: {
            regs[0] = (regs[0] & 0xF0) | (data & 0x0F);
            setPPUBank0(regs[0]);
        }
            break;
        case 0xB002:
        case 0xB040: {
            regs[0] = (regs[0] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank0(regs[0]);
        }
            break;
        case 0xB001:
        case 0xB004:
        case 0xB080: {
            regs[1] = (regs[1] & 0xF0) | (data & 0x0F);
            setPPUBank1(regs[1]);
        }
            break;
        case 0xB003:
        case 0xB006:
        case 0xB0C0: {
            regs[1] = (regs[1] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank1(regs[1]);
        }
            break;
        case 0xC000: {
            regs[2] = (regs[2] & 0xF0) | (data & 0x0F);
            setPPUBank2(regs[2]);
        }
            break;
        case 0xC002:
        case 0xC040: {
            regs[2] = (regs[2] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank2(regs[2]);
        }
            break;
        case 0xC001:
        case 0xC004:
        case 0xC080: {
            regs[3] = (regs[3] & 0xF0) | (data & 0x0F);
            setPPUBank3(regs[3]);
        }
            break;
        case 0xC003:
        case 0xC006:
        case 0xC0C0: {
            regs[3] = (regs[3] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank3(regs[3]);
        }
            break;
        case 0xD000: {
            regs[4] = (regs[4] & 0xF0) | (data & 0x0F);
            setPPUBank4(regs[4]);
        }
            break;
        case 0xD002:
        case 0xD040: {
            regs[4] = (regs[4] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank4(regs[4]);
        }
            break;
        case 0xD001:
        case 0xD004:
        case 0xD080: {
            regs[5] = (regs[5] & 0xF0) | (data & 0x0F);
            setPPUBank5(regs[5]);
        }
            break;
        case 0xD003:
        case 0xD006:
        case 0xD0C0: {
            regs[5] = (regs[5] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank5(regs[5]);
        }
            break;
        case 0xE000: {
            regs[6] = (regs[6] & 0xF0) | (data & 0x0F);
            setPPUBank6(regs[6]);
        }
            break;
        case 0xE002:
        case 0xE040: {
            regs[6] = (regs[6] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank6(regs[6]);
        }
            break;
        case 0xE001:
        case 0xE004:
        case 0xE080: {
            regs[7] = (regs[7] & 0xF0) | (data & 0x0F);
            setPPUBank7(regs[7]);
        }
            break;
        case 0xE003:
        case 0xE006:
        case 0xE0C0: {
            regs[7] = (regs[7] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank7(regs[7]);
        }
            break;
        case 0xF000: {
            irq_latch = (irq_latch & 0xF0) | (data & 0x0F);
        }
            break;
        case 0xF002:
        case 0xF040: {
            irq_latch = (irq_latch & 0x0F) | ((data & 0x0F) << 4);
        }
            break;
        case 0xF003:
        case 0xF0C0: {
            irq_enabled = (irq_enabled & 0x01) * 3;
        }
            break;
        case 0xF004:
        case 0xF080: {
            irq_enabled = data & 0x03;
            if ((irq_enabled & 0x02) != 0) {
                irq_counter = irq_latch;
            }
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
            if (irq_counter == 0) {
                irq_counter = irq_latch;
                irq_enabled = (irq_enabled & 0x01) * 3;
                return 3;
            } else {
                irq_counter++;
            }
        }
        return 0;
    }
}
