/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 76 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper076 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 76;
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
        // Set the PPU Banks
        if (getNum1KVROMBanks() >= 8) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
    }

    int regs[] = new int[1];

    /**
     * Access the Mapper
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        if (addr == 0x8000) {
            regs[0] = data;
        } else if (addr == 0x8001) {
            switch (regs[0] & 0x07) {
            case 0x02: {
                setPPUBank0(data * 2 + 0);
                setPPUBank1(data * 2 + 1);
            }
                break;
            case 0x03: {
                setPPUBank2(data * 2 + 0);
                setPPUBank3(data * 2 + 1);
            }
                break;
            case 0x04: {
                setPPUBank4(data * 2 + 0);
                setPPUBank5(data * 2 + 1);
            }
                break;
            case 0x05: {
                setPPUBank6(data * 2 + 0);
                setPPUBank7(data * 2 + 1);
            }
                break;
            case 0x06: {
                setCPUBank8(data);
            }
                break;
            case 0x07: {
                setCPUBankA(data);
            }
                break;
            }
        }
    }
}
