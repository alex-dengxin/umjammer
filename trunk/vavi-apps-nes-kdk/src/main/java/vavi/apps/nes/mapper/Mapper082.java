/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 82 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper082 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 82;
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

    int regs[] = new int[1];

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
        setMirroringVertical();
        regs[0] = 0;
    }

    /**
     * Access the Mapper
     */
    public final void access(int addr, int data) {
        switch (addr) {
        case 0x7EF0: {
            if (regs[0] != 0) {
                setPPUBank4((data & 0xFE) + 0);
                setPPUBank5((data & 0xFE) + 1);
            } else {
                setPPUBank0((data & 0xFE) + 0);
                setPPUBank1((data & 0xFE) + 1);
            }
        }
            break;
        case 0x7EF1: {
            if (regs[0] != 0) {
                setPPUBank6((data & 0xFE) + 0);
                setPPUBank7((data & 0xFE) + 1);
            } else {
                setPPUBank2((data & 0xFE) + 0);
                setPPUBank3((data & 0xFE) + 1);
            }
        }
            break;
        case 0x7EF2: {
            if (regs[0] == 0) {
                setPPUBank4(data);
            } else {
                setPPUBank0(data);
            }
        }
            break;
        case 0x7EF3: {
            if (regs[0] == 0) {
                setPPUBank5(data);
            } else {
                setPPUBank1(data);
            }
        }
            break;
        case 0x7EF4: {
            if (regs[0] == 0) {
                setPPUBank6(data);
            } else {
                setPPUBank2(data);
            }
        }
            break;
        case 0x7EF5: {
            if (regs[0] == 0) {
                setPPUBank7(data);
            } else {
                setPPUBank3(data);
            }
        }
            break;
        case 0x7EF6: {
            regs[0] = data & 0x02;
            if ((data & 0x01) != 0) {
                setMirroringVertical();
            } else {
                setMirroringHorizontal();
            }
        }
            break;
        case 0x7EFA: {
            setCPUBank8(data >> 2);
        }
            break;
        case 0x7EFB: {
            setCPUBankA(data >> 2);
        }
            break;
        case 0x7EFC: {
            setCPUBankC(data >> 2);
        }
            break;
        }
    }
}
