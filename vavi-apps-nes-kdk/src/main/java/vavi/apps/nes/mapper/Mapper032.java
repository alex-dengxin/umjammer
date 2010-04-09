/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 32 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper032 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 32;
    }

    int regs[] = new int[1];

    int patch = 0;

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

    public void setCRC(long crc) {
        if (crc == 0x243A8735) {
            // Major League Baseball
            patch = 1;
        }
        if (crc == 674943524l) {
            // Ai Sensei no Oshiete - Watashi no Hoshi
            setCPUBanks(30, 31, 30, 31);
            System.out.println("Ai Sensei no Oshiete - Watashi no Hoshi CRC:" + crc);
        }
    }

    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        switch (addr & 0xF000) {
        case 0x8000: {
            if ((regs[0] & 0x02) != 0) {
                setCPUBankC(data);
            } else {
                setCPUBank8(data);
            }
        }
            break;
        case 0x9000: {
            if ((data & 0x01) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
            regs[0] = data;
        }
            break;
        case 0xA000: {
            setCPUBankA(data);
        }
            break;
        }
        switch (addr & 0xF007) {
        case 0xB000: {
            setPPUBank0(data);
        }
            break;
        case 0xB001: {
            setPPUBank1(data);
        }
            break;
        case 0xB002: {
            setPPUBank2(data);
        }
            break;
        case 0xB003: {
            setPPUBank3(data);
        }
            break;
        case 0xB004: {
            setPPUBank4(data);
        }
            break;
        case 0xB005: {
            setPPUBank5(data);
        }
            break;
        case 0xB006: {
            if ((patch == 1) && ((data & 0x40) != 0)) {
                setMirroring(0, 0, 0, 1);
            }
            setPPUBank6(data);
        }
            break;
        case 0xB007: {
            if ((patch == 1) && ((data & 0x40) != 0)) {
                setMirroring(0, 0, 0, 0);
            }
            setPPUBank7(data);
        }
            break;
        }
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set CPU Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set PPU Banks
        if (getNum1KVROMBanks() > 0)
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        // Set Registers
        for (int i = 0; i < regs.length; i++) {
            regs[i] = 0;
        }
        // Check for Patches
        if (patch == 1) {
            setMirroring(0, 0, 0, 0);
        }
    }
}
