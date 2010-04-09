/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 75 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper075 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 75;
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
        if (getNum1KVROMBanks() >= 8) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
        regs[0] = 0;
        regs[1] = 1;
    }

    int regs[] = new int[2];

    /**
     * Access the Mapper
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr & 0xF000) {
        case 0x8000: {
            setCPUBank8(data);
        }
            break;
        case 0x9000: {
            if ((data & 0x01) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
            regs[0] = (regs[0] & 0x0F) | ((data & 0x02) << 3);
            setPPUBank0(regs[0] * 4 + 0);
            setPPUBank1(regs[0] * 4 + 1);
            setPPUBank2(regs[0] * 4 + 2);
            setPPUBank3(regs[0] * 4 + 3);
            regs[1] = (regs[1] & 0x0F) | ((data & 0x04) << 2);
            setPPUBank4(regs[1] * 4 + 0);
            setPPUBank5(regs[1] * 4 + 1);
            setPPUBank6(regs[1] * 4 + 2);
            setPPUBank7(regs[1] * 4 + 3);
        }
            break;
        case 0xA000: {
            setCPUBankA(data);
        }
            break;
        case 0xC000: {
            setCPUBankC(data);
        }
            break;
        case 0xE000: {
            regs[0] = (regs[0] & 0x10) | (data & 0x0F);
            setPPUBank0(regs[0] * 4 + 0);
            setPPUBank1(regs[0] * 4 + 1);
            setPPUBank2(regs[0] * 4 + 2);
            setPPUBank3(regs[0] * 4 + 3);
        }
            break;
        case 0xF000: {
            regs[1] = (regs[1] & 0x10) | (data & 0x0F);
            setPPUBank4(regs[1] * 4 + 0);
            setPPUBank5(regs[1] * 4 + 1);
            setPPUBank6(regs[1] * 4 + 2);
            setPPUBank7(regs[1] * 4 + 3);
        }
            break;
        }
    }
}
