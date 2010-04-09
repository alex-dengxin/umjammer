/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper FF Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper255 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 255;
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

    public void accesslow(int addr, int data) {
        if (addr >= 0x5800) {
            regs[addr & 0x0003] = data & 0x0F;
        }
    }

    public int accesslowread(int addr) {
        if (addr >= 0x5800) {
            return regs[addr & 0x0003] & 0x0F;
        } else {
            return addr >> 8;
        }
    }

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        // Check Range
        if (addr < 0x8000)
            return;
        // Determine Program, Character and ROM Banks
        int prg_bank = ((addr & 0x0F80) >> 7);
        int chr_bank = (addr & 0x003F);
        int rom_bank = ((addr & 0x4000) >> 14);
        // Sort Mirroring
        if ((addr & 0x2000) != 0) {
            setMirroringHorizontal();
        } else {
            setMirroringVertical();
        }
        // Sort CPU
        if ((addr & 0x1000) != 0) {
            // 16K PRG_ROM
            if ((addr & 0x0040) != 0) {
                // Upper half
                setCPUBank8(0x80 * rom_bank + prg_bank * 4 + 2);
                setCPUBankA(0x80 * rom_bank + prg_bank * 4 + 3);
                setCPUBankC(0x80 * rom_bank + prg_bank * 4 + 2);
                setCPUBankE(0x80 * rom_bank + prg_bank * 4 + 3);
            } else {
                // Lower half
                setCPUBank8(0x80 * rom_bank + prg_bank * 4 + 0);
                setCPUBankA(0x80 * rom_bank + prg_bank * 4 + 1);
                setCPUBankC(0x80 * rom_bank + prg_bank * 4 + 0);
                setCPUBankE(0x80 * rom_bank + prg_bank * 4 + 1);
            }
        } else {
            // 32K PRG_ROM
            setCPUBank8(0x80 * rom_bank + prg_bank * 4 + 0);
            setCPUBankA(0x80 * rom_bank + prg_bank * 4 + 1);
            setCPUBankC(0x80 * rom_bank + prg_bank * 4 + 2);
            setCPUBankE(0x80 * rom_bank + prg_bank * 4 + 3);
        }
        // PPU Banks
        setPPUBank0(0x200 * rom_bank + chr_bank * 8 + 0);
        setPPUBank1(0x200 * rom_bank + chr_bank * 8 + 1);
        setPPUBank2(0x200 * rom_bank + chr_bank * 8 + 2);
        setPPUBank3(0x200 * rom_bank + chr_bank * 8 + 3);
        setPPUBank4(0x200 * rom_bank + chr_bank * 8 + 4);
        setPPUBank5(0x200 * rom_bank + chr_bank * 8 + 5);
        setPPUBank6(0x200 * rom_bank + chr_bank * 8 + 6);
        setPPUBank7(0x200 * rom_bank + chr_bank * 8 + 7);
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set CPU Banks
        setCPUBanks(0, 1, 2, 3);
        // Set PPU Banks
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        // Set Mirroring
        setMirroringVertical();
    }
}
