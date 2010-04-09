/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 22 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper022 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 22;
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
        // set CPU bank pointers
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set the Video ROM
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        // Check Range
        if (addr < 0x8000)
            return;
        switch (addr) {
        case 0x8000: {
            setCPUBank8(data);
        }
            break;
        case 0x9000: {
            data &= 0x03;
            if (data == 0) {
                setMirroringVertical();
            } else if (data == 1) {
                setMirroringHorizontal();
            } else if (data == 2) {
                setMirroring(1, 1, 1, 1);
            } else {
                setMirroring(0, 0, 0, 0);
            }
        }
            break;
        case 0xA000: {
            setCPUBankA(data);
        }
            break;
        case 0xB000: {
            setPPUBank0(data >> 1);
        }
            break;
        case 0xB001: {
            setPPUBank1(data >> 1);
        }
            break;
        case 0xC000: {
            setPPUBank2(data >> 1);
        }
            break;
        case 0xC001: {
            setPPUBank3(data >> 1);
        }
            break;
        case 0xD000: {
            setPPUBank4(data >> 1);
        }
            break;
        case 0xD001: {
            setPPUBank5(data >> 1);
        }
            break;
        case 0xE000: {
            setPPUBank6(data >> 1);
        }
            break;
        case 0xE001: {
            setPPUBank7(data >> 1);
        }
            break;
        }
    }
}
