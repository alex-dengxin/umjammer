/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 33 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper033 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 33;
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
        // IRQ Settings
        irq_enabled = false;
        irq_counter = 0;
        // Set VROM Banks (Standard)
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        switch (addr) {
        case 0x8000: {
            if (patch) // old #33 games
            {
                if ((data & 0x40) != 0) {
                    setMirroringHorizontal();
                } else {
                    setMirroringVertical();
                }
                setCPUBank8(data & 0x1F);
            } else {
                setCPUBank8(data);
            }
        }
            break;
        case 0x8001: {
            if (patch) // old #33 games
            {
                setCPUBankA(data & 0x1F);
            } else {
                setCPUBankA(data);
            }
        }
            break;
        case 0x8002: {
            setPPUBank0(data * 2 + 0);
            setPPUBank1(data * 2 + 1);
        }
            break;
        case 0x8003: {
            setPPUBank2(data * 2 + 0);
            setPPUBank3(data * 2 + 1);
        }
            break;
        case 0xA000: {
            setPPUBank4(data);
        }
            break;
        case 0xA001: {
            setPPUBank5(data);
        }
            break;
        case 0xA002: {
            setPPUBank6(data);
        }
            break;
        case 0xA003: {
            setPPUBank7(data);
        }
            break;
        case 0xC000: {
            irq_counter = data;
        }
            break;
        case 0xC001:
        case 0xC002:
        case 0xE001:
        case 0xE002: {
            irq_enabled = (data != 0);
        }
            break;
        case 0xE000: {
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
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if (irq_enabled) {
            if ((mm.nes.ppu.REG_2001 & 0x18) != 00) {
                if (scanline >= 0 && scanline <= 239) {
                    if (irq_counter == 0) {
                        irq_counter = 0;
                        irq_enabled = false;
                        return 3;
                    } else {
                        irq_counter++;
                    }
                }
            }
        }
        return 0;
    }

    boolean patch = false;

    /**
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        patch = false;
        System.out.println("CRC: " + crc);
        if (crc == 2323450558l || crc == 929118424l || // Akira
            crc == 2918582006l || crc == 2690820465l || // Bakushou!! Jinsei
            // Gekijou
            crc == 2051635939l || // Don Doko Don
            crc == 3133804713l || // Golf Ko Open
            crc == 711285153l || // Operation Wolf
            crc == 2931293417l || // Power Blazer
            crc == 3642816991l) // Takeshi no Sengoku Fuuunji
        {
            patch = true;
            System.out.println("Patched");
        }
    }
}
