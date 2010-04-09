/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 189 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper189 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 189;
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
    private boolean irq_enabled = false;

    /**
     * Whether the IRQ has been Set
     */
    private int[] regs = new int[1];

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
        // IRQ Settings
        irq_enabled = false;
        irq_latch = 0;
        irq_counter = 0;
        // Set VROM Banks (Standard)
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
    }

    public void accesslow(int addr, int value) {
        if (addr >= 0x4100 && addr <= 0x41FF) {
            int data = (value & 0x30) >> 4;
            setCPUBanks(data * 4, data * 4 + 1, data * 4 + 2, data * 4 + 3);
        }
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        switch (addr) {
        case 0x8000: {
            regs[0] = data;
        }
            break;
        case 0x8001: {
            switch (regs[0]) {
            case 0x40: {
                setPPUBank0(data + 0);
                setPPUBank1(data + 1);
            }
                break;
            case 0x41: {
                setPPUBank2(data + 0);
                setPPUBank3(data + 1);
            }
                break;
            case 0x42: {
                setPPUBank4(data);
            }
                break;
            case 0x43: {
                setPPUBank5(data);
            }
                break;
            case 0x44: {
                setPPUBank6(data);
            }
                break;
            case 0x45: {
                setPPUBank7(data);
            }
                break;
            case 0x46: {
                setPPUBank6(data);
            }
                break;
            case 0x47: {
                setPPUBank5(data);
            }
                break;
            }
        }
            break;
        case 0xA000: {
            if ((data & 0x01) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
            break;
        case 0xC000: {
            irq_counter = data;
        }
            break;
        case 0xC001: {
            irq_latch = data;
        }
            break;
        case 0xE000: {
            irq_enabled = false;
        }
            break;
        case 0xE001: {
            irq_enabled = true;
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
        if (irq_enabled) {
            if ((scanline >= 0) && (scanline <= 239)) {
                if ((mm.nes.ppu.REG_2001 & 0x18) != 00) {
                    if (0 != (--irq_counter)) {
                        irq_counter = irq_latch;
                        return 3;
                    }
                }
            }
        }
        return 0;
    }
}
