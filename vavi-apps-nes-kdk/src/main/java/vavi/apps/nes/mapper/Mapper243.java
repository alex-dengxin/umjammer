/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 243 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper243 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 243;
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
        setCPUBanks(0, 1, 2, 3);
        // Set VROM Banks (Standard)
        if (getNum1KVROMBanks() > 32) {
            setPPUBanks(24, 25, 26, 27, 28, 29, 30, 31);
        }
        setMirroringHorizontal();
        regs[0] = 0;
        regs[1] = 0;
        regs[2] = 3;
        regs[3] = 0;
    }

    int regs[] = new int[4];

    /**
     * Access the Mapper in Low Area.
     */
    public final void accesslow(int addr, int data) {
        if ((addr & 0x4101) == 0x4100) {
            regs[0] = data;
        } else if ((addr & 0x4101) == 0x4101) {
            switch (regs[0] & 0x07) {
            case 0: {
                regs[1] = 0;
                regs[2] = 3;
            }
                break;
            case 4: {
                regs[2] = (regs[2] & 0x06) | (data & 0x01);
            }
                break;
            case 5: {
                regs[1] = data & 0x01;
            }
                break;
            case 6: {
                regs[2] = (regs[2] & 0x01) | ((data & 0x03) << 1);
            }
                break;
            case 7: {
                regs[3] = data & 1;
            }
                break;
            }
            setCPUBanks(regs[1] * 4 + 0, regs[1] * 4 + 1, regs[1] * 4 + 2, regs[1] * 4 + 3);
            setPPUBanks(regs[2] * 8 + 0, regs[2] * 8 + 1, regs[2] * 8 + 2, regs[2] * 8 + 3, regs[2] * 8 + 4, regs[2] * 8 + 5, regs[2] * 8 + 6, regs[2] * 8 + 7);
            if (regs[3] != 0) {
                setMirroringVertical();
            } else {
                setMirroringHorizontal();
            }
        }
    }

    public final void access(int addr, int data) {
        if (addr >= 0x8000)
            return;
        if ((addr & 0x4101) == 0x4100) {
            regs[0] = data;
        } else if ((addr & 0x4101) == 0x4101) {
            switch (regs[0] & 0x07) {
            case 0: {
                regs[1] = 0;
                regs[2] = 3;
            }
                break;
            case 4: {
                regs[2] = (regs[2] & 0x06) | (data & 0x01);
            }
                break;
            case 5: {
                regs[1] = data & 0x01;
            }
                break;
            case 6: {
                regs[2] = (regs[2] & 0x01) | ((data & 0x03) << 1);
            }
                break;
            case 7: {
                regs[3] = data & 1;
            }
                break;
            }
            setCPUBanks(regs[1] * 4 + 0, regs[1] * 4 + 1, regs[1] * 4 + 2, regs[1] * 4 + 3);
            setPPUBanks(regs[2] * 8 + 0, regs[2] * 8 + 1, regs[2] * 8 + 2, regs[2] * 8 + 3, regs[2] * 8 + 4, regs[2] * 8 + 5, regs[2] * 8 + 6, regs[2] * 8 + 7);
            if (regs[3] != 0) {
                setMirroringVertical();
            } else {
                setMirroringHorizontal();
            }
        }
    }
}
