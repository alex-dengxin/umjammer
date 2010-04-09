/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 64 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper064 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 64;
    }

    /**
     * Irq Counter
     */
    private int irq_counter = 0;

    /**
     * Whether the IRQ has been Set
     */
    private boolean irq_enabled = false;

    /**
     * Whether the IRQ has been Set
     */
    private int irq_latch = 0;

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // set CPU bank pointers
        setCPUBank8(getNum8KRomBanks() - 1);
        setCPUBankA(getNum8KRomBanks() - 1);
        setCPUBankC(getNum8KRomBanks() - 1);
        setCPUBankE(getNum8KRomBanks() - 1);
        // set PPU bank pointers
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
        // Set Interrupts
        irq_latch = 0;
        irq_counter = 0;
        irq_enabled = false;
        regs[0] = 0;
        regs[1] = 0;
        regs[2] = 0;
    }

    int regs[] = new int[3];

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
        switch (addr & 0xF003) {
        case 0x8000: {
            regs[0] = data & 0x0F;
            regs[1] = data & 0x40;
            regs[2] = data & 0x80;
        }
            break;
        case 0x8001: {
            switch (regs[0]) {
            case 0x00: {
                if (regs[2] != 0) {
                    setPPUBank4(data + 0);
                    setPPUBank5(data + 1);
                } else {
                    setPPUBank0(data + 0);
                    setPPUBank1(data + 1);
                }
            }
                break;
            case 0x01: {
                if (regs[2] != 0) {
                    setPPUBank6(data + 0);
                    setPPUBank7(data + 1);
                } else {
                    setPPUBank2(data + 0);
                    setPPUBank3(data + 1);
                }
            }
                break;
            case 0x02: {
                if (regs[2] != 0) {
                    setPPUBank0(data);
                } else {
                    setPPUBank4(data);
                }
            }
                break;
            case 0x03: {
                if (regs[2] != 0) {
                    setPPUBank1(data);
                } else {
                    setPPUBank5(data);
                }
            }
                break;
            case 0x04: {
                if (regs[2] != 0) {
                    setPPUBank2(data);
                } else {
                    setPPUBank6(data);
                }
            }
                break;
            case 0x05: {
                if (regs[2] != 0) {
                    setPPUBank3(data);
                } else {
                    setPPUBank7(data);
                }
            }
                break;
            case 0x06: {
                if (regs[1] != 0) {
                    setCPUBankA(data);
                } else {
                    setCPUBank8(data);
                }
            }
                break;
            case 0x07: {
                if (regs[1] != 0) {
                    setCPUBankC(data);
                } else {
                    setCPUBankA(data);
                }
            }
                break;
            case 0x08: {
                setPPUBank1(data);
            }
                break;
            case 0x09: {
                setPPUBank3(data);
            }
                break;
            case 0x0F: {
                if (regs[1] != 0) {
                    setCPUBank8(data);
                } else {
                    setCPUBankC(data);
                }
            }
                break;
            }
        }
            break;
        case 0xA000: {
            if ((data & 0x01) == 0) {
                setMirroringVertical();
            } else {
                setMirroringHorizontal();
            }
        }
            break;
        case 0xC000: {
            irq_latch = data;
            irq_counter = irq_latch;
        }
            break;
        case 0xC001: {
            irq_counter = irq_latch;
        }
            break;
        case 0xE000: {
            irq_enabled = false;
            irq_counter = irq_latch;
        }
            break;
        case 0xE001: {
            irq_enabled = true;
            irq_counter = irq_latch;
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
                    if (--irq_counter == 0) {
                        irq_counter = irq_latch;
                        return 3;
                    }
                }
            }
        }
        return 0;
    }
}
