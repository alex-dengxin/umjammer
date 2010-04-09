/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 23 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper023 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 23;
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

    int patch = 0xFFFF;

    /**
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        patch = 0xFFFF;
        if (crc == 3254515289l) {
            // Akumajou Special - Boku Dracula Kun
            patch = 0xF00C;
            System.out.println("M023:" + crc);
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set Program ROM Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
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
        // regs[8] ... $9008 swap
        switch (addr & patch) {
        case 0x8000:
        case 0x8004:
        case 0x8008:
        case 0x800C: {
            if ((regs[8]) != 0) {
                setCPUBankC(data);
            } else {
                setCPUBank8(data);
            }
        }
            break;
        case 0x9000: {
            if (data != 0xFF) {
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
        }
            break;
        case 0x9008: {
            regs[8] = data & 0x02;
        }
            break;
        case 0xA000:
        case 0xA004:
        case 0xA008:
        case 0xA00C: {
            setCPUBankA(data);
        }
            break;
        case 0xB000: {
            regs[0] = (regs[0] & 0xF0) | (data & 0x0F);
            setPPUBank0(regs[0]);
        }
            break;
        case 0xB001:
        case 0xB004: {
            regs[0] = (regs[0] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank0(regs[0]);
        }
            break;
        case 0xB002:
        case 0xB008: {
            regs[1] = (regs[1] & 0xF0) | (data & 0x0F);
            setPPUBank1(regs[1]);
        }
            break;
        case 0xB003:
        case 0xB00C: {
            regs[1] = (regs[1] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank1(regs[1]);
        }
            break;
        case 0xC000: {
            regs[2] = (regs[2] & 0xF0) | (data & 0x0F);
            setPPUBank2(regs[2]);
        }
            break;
        case 0xC001:
        case 0xC004: {
            regs[2] = (regs[2] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank2(regs[2]);
        }
            break;
        case 0xC002:
        case 0xC008: {
            regs[3] = (regs[3] & 0xF0) | (data & 0x0F);
            setPPUBank3(regs[3]);
        }
            break;
        case 0xC003:
        case 0xC00C: {
            regs[3] = (regs[3] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank3(regs[3]);
        }
            break;
        case 0xD000: {
            regs[4] = (regs[4] & 0xF0) | (data & 0x0F);
            setPPUBank4(regs[4]);
        }
            break;
        case 0xD001:
        case 0xD004: {
            regs[4] = (regs[4] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank4(regs[4]);
        }
            break;
        case 0xD002:
        case 0xD008: {
            regs[5] = (regs[5] & 0xF0) | (data & 0x0F);
            setPPUBank5(regs[5]);
        }
            break;
        case 0xD003:
        case 0xD00C: {
            regs[5] = (regs[5] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank5(regs[5]);
        }
            break;
        case 0xE000: {
            regs[6] = (regs[6] & 0xF0) | (data & 0x0F);
            setPPUBank6(regs[6]);
        }
            break;
        case 0xE001:
        case 0xE004: {
            regs[6] = (regs[6] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank6(regs[6]);
        }
            break;
        case 0xE002:
        case 0xE008: {
            regs[7] = (regs[7] & 0xF0) | (data & 0x0F);
            setPPUBank7(regs[7]);
        }
            break;
        case 0xE003:
        case 0xE00C: {
            regs[7] = (regs[7] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank7(regs[7]);
        }
            break;
        case 0xF000: {
            irq_latch = (irq_latch & 0xF0) | (data & 0x0F);
        }
            break;
        case 0xF004: {
            irq_latch = (irq_latch & 0x0F) | ((data & 0x0F) << 4);
        }
            break;
        case 0xF008: {
            irq_enabled = data & 0x03;
            if ((irq_enabled & 0x02) != 0) {
                irq_counter = irq_latch;
            }
        }
            break;
        case 0xF00C: {
            irq_enabled = (irq_enabled & 0x01) * 3;
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
            if (irq_counter == 0xFF) {
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
