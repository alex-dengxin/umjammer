/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 251 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper251 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 251;
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
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        switch (addr & 0xE001) {
        case 0x6000:
            if (regs[9] != 0) {
                bregs[regs[10]++] = data;
                if (regs[10] == 4) {
                    regs[10] = 0;
                    banksync();
                }
            }
            break;
        case 0x8000:
            regs[8] = data;
            banksync();
            break;
        case 0x8001:
            regs[regs[8] & 7] = data;
            banksync();
            break;
        case 0xA001:
            if ((data & 0x80) != 0) {
                regs[9] = 1;
                regs[10] = 0;
            } else {
                regs[9] = 0;
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
        // Set Mirroring
        setMirroringVertical();
        // Blank Registers
        for (int i = 0; i < regs.length; i++)
            regs[i] = 0;
        for (int i = 0; i < bregs.length; i++)
            bregs[i] = 0;
    }

    private void banksync() {
        int[] chr = new int[6];
        int[] prg = new int[4];
        for (int i = 0; i < chr.length; i++) {
            chr[i] = (regs[i] | (bregs[1] << 4)) & ((bregs[2] << 4) | 0x0F);
        }
        if ((regs[8] & 0x80) != 0) {
            setPPUBanks(chr[2], chr[3], chr[4], chr[5], chr[0], chr[0] + 1, chr[1], chr[1] + 1);
        } else {
            setPPUBanks(chr[0], chr[0] + 1, chr[1], chr[1] + 1, chr[2], chr[3], chr[4], chr[5]);
        }
        prg[0] = (regs[6] & ((bregs[3] & 0x3F) ^ 0x3F)) | (bregs[1]);
        prg[1] = (regs[7] & ((bregs[3] & 0x3F) ^ 0x3F)) | (bregs[1]);
        prg[2] = prg[3] = ((bregs[3] & 0x3F) ^ 0x3F) | (bregs[1]);
        prg[2] &= (getNum8KRomBanks() - 1);
        if ((regs[8] & 0x40) != 0) {
            setCPUBanks(prg[2], prg[1], prg[0], prg[3]);
        } else {
            setCPUBanks(prg[0], prg[1], prg[2], prg[3]);
        }
    }

    int[] regs = new int[11];
    int[] bregs = new int[4];
}
