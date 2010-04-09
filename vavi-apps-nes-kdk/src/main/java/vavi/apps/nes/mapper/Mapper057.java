/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 57 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper057 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 57;
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
        setCPUBanks(0, 1, 0, 1);
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        regs[0] = 0;
    }

    int regs[] = new int[1];

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr) {
        case 0x8000:
        case 0x8001:
        case 0x8002:
        case 0x8003: {
            if ((data & 0x40) != 0) {
                setPPUBank0(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 0);
                setPPUBank1(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 1);
                setPPUBank2(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 2);
                setPPUBank3(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 3);
                setPPUBank4(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 4);
                setPPUBank5(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 5);
                setPPUBank6(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 6);
                setPPUBank7(8 * ((data & 0x03) + ((regs[0] & 0x10) >> 1) + (regs[0] & 0x07)) + 7);
            }
        }
            break;
        case 0x8800: {
            regs[0] = data;
            if ((data & 0x80) != 0) {
                setCPUBank8(4 * ((data & 0x40) >> 6) + 8 + 0);
                setCPUBankA(4 * ((data & 0x40) >> 6) + 8 + 1);
                setCPUBankC(4 * ((data & 0x40) >> 6) + 8 + 2);
                setCPUBankE(4 * ((data & 0x40) >> 6) + 8 + 3);
            } else {
                setCPUBank8(2 * ((data & 0x60) >> 5) + 0);
                setCPUBankA(2 * ((data & 0x60) >> 5) + 1);
                setCPUBankC(2 * ((data & 0x60) >> 5) + 0);
                setCPUBankE(2 * ((data & 0x60) >> 5) + 1);
            }
            setPPUBank0(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 0);
            setPPUBank1(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 1);
            setPPUBank2(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 2);
            setPPUBank3(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 3);
            setPPUBank4(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 4);
            setPPUBank5(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 5);
            setPPUBank6(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 6);
            setPPUBank7(8 * ((data & 0x07) + ((data & 0x10) >> 1)) + 7);
            if ((data & 0x08) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
            break;
        }
    }
}
