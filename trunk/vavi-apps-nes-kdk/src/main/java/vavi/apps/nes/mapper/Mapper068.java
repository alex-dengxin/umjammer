/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 68 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper068 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 68;
    }

    int regs[] = new int[4];

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

    public final void access(int addr, int data) {
        switch (addr & 0xF000) {
        case 0x8000: {
            setPPUBank0(data * 2 + 0);
            setPPUBank1(data * 2 + 1);
        }
            break;
        case 0x9000: {
            setPPUBank2(data * 2 + 0);
            setPPUBank3(data * 2 + 1);
        }
            break;
        case 0xA000: {
            setPPUBank4(data * 2 + 0);
            setPPUBank5(data * 2 + 1);
        }
            break;
        case 0xB000: {
            setPPUBank6(data * 2 + 0);
            setPPUBank7(data * 2 + 1);
        }
            break;
        case 0xC000: {
            regs[2] = data;
            SyncMirror();
        }
            break;
        case 0xD000: {
            regs[3] = data;
            SyncMirror();
        }
            break;
        case 0xE000: {
            regs[0] = (data & 0x10) >> 4;
            regs[1] = data & 0x03;
            SyncMirror();
        }
            break;
        case 0xF000: {
            setCPUBank8(data * 2);
            setCPUBankA(data * 2 + 1);
        }
            break;
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        regs[0] = 0;
        regs[1] = 0;
        regs[2] = 0;
        regs[3] = 0;
    }

    /**
     * Synchronise the Mirrors
     */
    private void SyncMirror() {
        if (regs[0] != 0) {
            if (regs[1] == 0) {
                setPPUBank8(regs[2] + 0x80); // + 0x20000
                setPPUBank9(regs[3] + 0x80);
                setPPUBankA(regs[2] + 0x80);
                setPPUBankB(regs[3] + 0x80);
            } else if (regs[1] == 1) {
                setPPUBank8(regs[2] + 0x80);
                setPPUBank9(regs[2] + 0x80);
                setPPUBankA(regs[3] + 0x80);
                setPPUBankB(regs[3] + 0x80);
            } else if (regs[1] == 2) {
                setPPUBank8(regs[2] + 0x80);
                setPPUBank9(regs[2] + 0x80);
                setPPUBankA(regs[2] + 0x80);
                setPPUBankB(regs[2] + 0x80);
            } else if (regs[1] == 3) {
                setPPUBank8(regs[3] + 0x80);
                setPPUBank9(regs[3] + 0x80);
                setPPUBankA(regs[3] + 0x80);
                setPPUBankB(regs[3] + 0x80);
            }
        } else {
            if (regs[1] == 0) {
                setMirroringVertical();
            } else if (regs[1] == 1) {
                setMirroringHorizontal();
            } else if (regs[1] == 2) {
                setMirroring(0, 0, 0, 0);
            } else if (regs[1] == 3) {
                setMirroring(1, 1, 1, 1);
            }
        }
    }
}
