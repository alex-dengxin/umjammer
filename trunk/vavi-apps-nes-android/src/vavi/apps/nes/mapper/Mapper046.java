/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 46 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper046 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 46;
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

    int regs[] = new int[4];

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        regs[0] = 0;
        regs[1] = 0;
        regs[2] = 0;
        regs[3] = 0;
        set_rom_banks();
        setMirroringVertical();
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        if (addr >= 0x6000 && addr < 0x8000) {
            regs[0] = data & 0x0F;
            regs[1] = (data & 0xF0) >> 4;
            set_rom_banks();
        } else if (addr >= 0x8000) {
            regs[2] = data & 0x01;
            regs[3] = (data & 0x70) >> 4;
            set_rom_banks();
        }
    }

    private void set_rom_banks() {
        setCPUBank8(regs[0] * 8 + regs[2] * 4 + 0);
        setCPUBankA(regs[0] * 8 + regs[2] * 4 + 1);
        setCPUBankC(regs[0] * 8 + regs[2] * 4 + 2);
        setCPUBankE(regs[0] * 8 + regs[2] * 4 + 3);
        setPPUBank0(regs[1] * 64 + regs[3] * 8 + 0);
        setPPUBank1(regs[1] * 64 + regs[3] * 8 + 1);
        setPPUBank2(regs[1] * 64 + regs[3] * 8 + 2);
        setPPUBank3(regs[1] * 64 + regs[3] * 8 + 3);
        setPPUBank4(regs[1] * 64 + regs[3] * 8 + 4);
        setPPUBank5(regs[1] * 64 + regs[3] * 8 + 5);
        setPPUBank6(regs[1] * 64 + regs[3] * 8 + 6);
        setPPUBank7(regs[1] * 64 + regs[3] * 8 + 7);
    }
}
