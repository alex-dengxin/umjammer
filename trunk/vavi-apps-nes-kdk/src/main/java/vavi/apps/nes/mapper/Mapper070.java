/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 70 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper070 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 70;
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
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        // Check for a Couple of Exceptions
        if (crc == 3148188645L) {
            patch = true; // Family Trainer - Manhattan Police
        }
        if (crc == 3717124343L || crc == 3508805237L || crc == 2427987139L) {
            patch = true; // Kamen Rider Club
        }
        if (patch)
            System.out.println("Patched");
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // set CPU bank pointers
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
    }

    boolean patch = false;

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        int chr_bank = data & 0x0F;
        int prg_bank = (data & 0x70) >> 4;
        setCPUBank8(prg_bank * 2 + 0);
        setCPUBankA(prg_bank * 2 + 1);
        setPPUBank0(chr_bank * 8 + 0);
        setPPUBank1(chr_bank * 8 + 1);
        setPPUBank2(chr_bank * 8 + 2);
        setPPUBank3(chr_bank * 8 + 3);
        setPPUBank4(chr_bank * 8 + 4);
        setPPUBank5(chr_bank * 8 + 5);
        setPPUBank6(chr_bank * 8 + 6);
        setPPUBank7(chr_bank * 8 + 7);
        if (patch) {
            if ((data & 0x80) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        } else {
            if ((data & 0x80) != 0) {
                setMirroring(1, 1, 1, 1);
            } else {
                setMirroring(0, 0, 0, 0);
            }
        }
    }
}
