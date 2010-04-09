/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 122
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper122 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 122;
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
        setCPUBanks(0, 1, 2, 3);
    }

    public void setCRC(long crc) {
        System.out.println("Mapper7A: crc=" + crc);
        if (crc == 0xF808AF60) {
            // AtlantisNoNazo
            patch = 1;
            System.out.println("Atlantis");
        }
    }

    private int patch = 0;

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        if (addr >= 0x6000 && addr < 0x8000) {
            if (patch == 1) {
                int h = (data & 0x20) >> 2;
                int l = ((data & 2) << 2) | (data & 4);
                setPPUBanks(l, l + 1, l + 2, l + 3, h, h + 1, h + 2, h + 3);
                return;
            }
            if (addr == 0x6000) {
                int chr_bank0 = data & 0x07;
                int chr_bank1 = (data & 0x70) >> 4;
                setPPUBank0(chr_bank0 * 4 + 0);
                setPPUBank0(chr_bank0 * 4 + 1);
                setPPUBank0(chr_bank0 * 4 + 2);
                setPPUBank0(chr_bank0 * 4 + 3);
                setPPUBank0(chr_bank1 * 4 + 0);
                setPPUBank0(chr_bank1 * 4 + 1);
                setPPUBank0(chr_bank1 * 4 + 2);
                setPPUBank0(chr_bank1 * 4 + 3);
            }
        }
    }
}
