/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 5 (MMC5) Controller used by NESCafe.
 * 
 *        NOT FULLY IMPLEMENTED
 *
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper005 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 5;
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
     * WRAM Size
     */
    int wram_size = 0;

    /**
     * Registers
     */
    int chr_reg[][] = new int[8][2];

    /**
     * WRAM
     */
    int[] wram;

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        wram_size = 1;
        wram = mm.mapper_extram;
        mm.mapper_extramsize = 0x10000;
        // Set SaveRAM
        for (int i = 0; i < 0x10000; i++) {
            wram[i] = mm.saveRAM[i];
        }
        MMC5_set_WRAM_bank(3, 0);
        setCPUBanks(getNum8KRomBanks() - 1, getNum8KRomBanks() - 1, getNum8KRomBanks() - 1, getNum8KRomBanks() - 1);
        // Set PPU Banks
        if (getNum1KVROMBanks() > 0)
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        for (int i = 0; i < 8; i++) {
            chr_reg[i][0] = i;
            chr_reg[i][1] = (i & 0x03) + 4;
        }
        wb[3] = 0;
        wb[4] = wb[5] = wb[6] = 8;
        prg_size = 3;
        wram_protect0 = 0x02;
        wram_protect1 = 0x01;
        chr_size = 3;
        gfx_mode = 0;
        irq_enabled = 0;
        irq_status = 0;
        irq_line = 0;
        split_control = 0;
        split_bank = 0;
    }

    int wb[] = new int[7];

    int prg_size = 0;

    int wram_protect0 = 0;

    int wram_protect1 = 0;

    int chr_size = 0;

    int gfx_mode = 0;

    int irq_enabled = 0;

    int irq_status = 0;

    int irq_line = 0;

    int split_control = 0;

    int split_bank = 0;

    // Need to implement read from low range!!
    public void accesslow(int addr, int data) {
        switch (addr) {
        case 0x5100: {
            prg_size = data & 0x03;
        }
            break;
        case 0x5101: {
            chr_size = data & 0x03;
        }
            break;
        case 0x5102: {
            wram_protect0 = data & 0x03;
        }
            break;
        case 0x5103: {
            wram_protect1 = data & 0x03;
        }
            break;
        case 0x5104: {
            gfx_mode = data & 0x03;
        }
            break;
        case 0x5105: {
            setPPUBank8(data & 0x03);
            data >>= 2;
            setPPUBank9(data & 0x03);
            data >>= 2;
            setPPUBankA(data & 0x03);
            data >>= 2;
            setPPUBankB(data & 0x03);
            data >>= 2;
        }
            break;
        case 0x5106: {
            // Write to 0xC00 in Nametable
            System.out.println("5106");
            for (int i = 0; i < 0x3C0; i++) {
                mm.ppu.ppuMemory[0x2000 + 0xC00 + i] = data;
            }
        }
            break;
        case 0x5107: {
            data &= 0x03;
            data = data | (data << 2) | (data << 4) | (data << 6);
            System.out.println("5107");
            for (int i = 0; i < 0x3C0; i++) {
                mm.ppu.ppuMemory[0x2000 + 0xC00 + i] = data;
            }
        }
            break;
        case 0x5113: {
            MMC5_set_WRAM_bank(3, data & 0x07);
        }
            break;
        case 0x5114:
        case 0x5115:
        case 0x5116:
        case 0x5117: {
            MMC5_set_CPU_bank(addr & 0x07, data);
        }
            break;
        case 0x5120:
        case 0x5121:
        case 0x5122:
        case 0x5123:
        case 0x5124:
        case 0x5125:
        case 0x5126:
        case 0x5127: {
            chr_reg[addr & 0x07][0] = data;
            sync_Chr_banks(0);
        }
            break;
        case 0x5128:
        case 0x5129:
        case 0x512A:
        case 0x512B: {
            chr_reg[(addr & 0x03) + 0][1] = data;
            chr_reg[(addr & 0x03) + 4][1] = data;
        }
            break;
        case 0x5200: {
            split_control = data;
        }
            break;
        case 0x5201: {
            // split_scroll = data;
        }
            break;
        case 0x5202: {
            split_bank = data & 0x3F;
        }
            break;
        case 0x5203: {
            irq_line = data;
        }
            break;
        case 0x5204: {
            irq_enabled = data;
        }
            break;
        case 0x5205: {
            value0 = data;
        }
            break;
        case 0x5206: {
            value1 = data;
        }
            break;
        default: {
            if (addr >= 0x5000 && addr <= 0x5015) {
                System.out.println("M005: APU ExWrite Not Implemented");
            } else if (addr >= 0x5C00 && addr <= 0x5FFF) {
                if (gfx_mode != 3) {
                    mm.ppu.ppuMemory[0x2000 + 0x800 + (addr & 0x3FF)] = data;
                }
            }
        }
            break;
        }
    }

    int value0 = 0;

    int value1 = 0;

    public void access(int addr, int data) {
        if (wram_protect0 == 0x02 && wram_protect1 == 0x01) {
            if (addr >= 0x8000 && addr <= 0x9FFF) {
                if (wb[4] != 8) {
                    wram[wb[4] * 0x2000 + (addr & 0x1FFF)] = data;
                    mm.saveRAM[wb[4] * 0x2000 + (addr & 0x1FFF)] = data;
                }
            } else if (addr >= 0xA000 && addr <= 0xBFFF) {
                if (wb[5] != 8) {
                    wram[wb[5] * 0x2000 + (addr & 0x1FFF)] = data;
                    mm.saveRAM[wb[5] * 0x2000 + (addr & 0x1FFF)] = data;
                }
            } else if (addr >= 0xC000 && addr <= 0xDFFF) {
                if (wb[6] != 8) {
                    wram[wb[6] * 0x2000 + (addr & 0x1FFF)] = data;
                    mm.saveRAM[wb[6] * 0x2000 + (addr & 0x1FFF)] = data;
                }
            }
        }
    }

    public int syncH(int scanline) {
        if (scanline <= 240) {
            if (scanline == irq_line) {
                if ((mm.nes.ppu.REG_2001 & 0x18) != 00) {
                    irq_status |= 0x80;
                }
            }
            if ((irq_status & 0x80) != 0 && (irq_enabled & 0x80) != 0) {
                return 3;
            }
        } else {
            irq_status |= 0x40;
        }
        return 0;
    }

    private void MMC5_set_CPU_bank(int page, int bank) {
        if ((bank & 0x80) != 0) {
            if (prg_size == 0) {
                if (page == 7) {
                    setCPUBank8((bank & 0x7C) + 0);
                    setCPUBankA((bank & 0x7C) + 1);
                    setCPUBankC((bank & 0x7C) + 2);
                    setCPUBankE((bank & 0x7C) + 3);
                    wb[4] = wb[5] = wb[6] = 8;
                }
            }
            if (prg_size == 1) {
                if (page == 5) {
                    setCPUBank8((bank & 0x7E) + 0);
                    setCPUBankA((bank & 0x7E) + 1);
                    wb[4] = wb[5] = 8;
                }
                if (page == 7) {
                    setCPUBankC((bank & 0x7E) + 0);
                    setCPUBankE((bank & 0x7E) + 1);
                    wb[6] = 8;
                }
            }
            if (prg_size == 2) {
                if (page == 5) {
                    setCPUBank8((bank & 0x7E) + 0);
                    setCPUBankA((bank & 0x7E) + 1);
                    wb[4] = wb[5] = 8;
                }
                if (page == 6) {
                    setCPUBankC(bank & 0x7F);
                    wb[6] = 8;
                }
                if (page == 7) {
                    setCPUBankE(bank & 0x7F);
                }
            }
            if (prg_size == 3) {
                if (page == 4) {
                    setCPUBank8(bank & 0x7F);
                    wb[4] = 8;
                }
                if (page == 5) {
                    setCPUBankA(bank & 0x7F);
                    wb[5] = 8;
                }
                if (page == 6) {
                    setCPUBankC(bank & 0x7F);
                    wb[6] = 8;
                }
                if (page == 7) {
                    setCPUBankE(bank & 0x7F);
                }
            }
        } else {
            if (prg_size == 1) {
                if (page == 5) {
                    MMC5_set_WRAM_bank(4, (bank & 0x06) + 0);
                    MMC5_set_WRAM_bank(5, (bank & 0x06) + 1);
                }
            }
            if (prg_size == 2) {
                if (page == 5) {
                    MMC5_set_WRAM_bank(4, (bank & 0x06) + 0);
                    MMC5_set_WRAM_bank(5, (bank & 0x06) + 1);
                }
                if (page == 6) {
                    MMC5_set_WRAM_bank(6, bank & 0x07);
                }
            }
            if (prg_size == 3) {
                if (page == 4) {
                    MMC5_set_WRAM_bank(4, bank & 0x07);
                }
                if (page == 5) {
                    MMC5_set_WRAM_bank(5, bank & 0x07);
                }
                if (page == 6) {
                    MMC5_set_WRAM_bank(6, bank & 0x07);
                }
            }
        }
    }

    private void MMC5_set_WRAM_bank(int page, int bank) {
        if (bank != 8) {
            if (wram_size == 1)
                bank = (bank > 3) ? 8 : 0;
            if (wram_size == 2)
                bank = (bank > 3) ? 1 : 0;
            if (wram_size == 3)
                bank = (bank > 3) ? 8 : bank;
            if (wram_size == 4)
                bank = (bank > 3) ? 4 : bank;
        }
        wb[page] = bank;
        if (bank != 8) {
            System.out.println("M005: NOT IMPLEMENTED");
        }
    }

    public int PPU_Latch_RenderScreen(int mode, int addr) {
        int ex_pal = 0;
        if (gfx_mode == 1 && mode == 1) {
            // ex gfx mode
            System.out.println("M005: Not implemented");
        } else {
            // normal
            sync_Chr_banks(mode);
        }
        return ex_pal;
    }

    private void sync_Chr_banks(int mode) {
        if (chr_size == 0) {
            setPPUBank0(chr_reg[7][mode] * 8 + 0);
            setPPUBank1(chr_reg[7][mode] * 8 + 1);
            setPPUBank2(chr_reg[7][mode] * 8 + 2);
            setPPUBank3(chr_reg[7][mode] * 8 + 3);
            setPPUBank4(chr_reg[7][mode] * 8 + 4);
            setPPUBank5(chr_reg[7][mode] * 8 + 5);
            setPPUBank6(chr_reg[7][mode] * 8 + 6);
            setPPUBank7(chr_reg[7][mode] * 8 + 7);
        } else if (chr_size == 1) {
            setPPUBank0(chr_reg[3][mode] * 4 + 0);
            setPPUBank1(chr_reg[3][mode] * 4 + 1);
            setPPUBank2(chr_reg[3][mode] * 4 + 2);
            setPPUBank3(chr_reg[3][mode] * 4 + 3);
            setPPUBank4(chr_reg[7][mode] * 4 + 0);
            setPPUBank5(chr_reg[7][mode] * 4 + 1);
            setPPUBank6(chr_reg[7][mode] * 4 + 2);
            setPPUBank7(chr_reg[7][mode] * 4 + 3);
        } else if (chr_size == 2) {
            setPPUBank0(chr_reg[1][mode] * 2 + 0);
            setPPUBank1(chr_reg[1][mode] * 2 + 1);
            setPPUBank2(chr_reg[3][mode] * 2 + 0);
            setPPUBank3(chr_reg[3][mode] * 2 + 1);
            setPPUBank4(chr_reg[5][mode] * 2 + 0);
            setPPUBank5(chr_reg[5][mode] * 2 + 1);
            setPPUBank6(chr_reg[7][mode] * 2 + 0);
            setPPUBank7(chr_reg[7][mode] * 2 + 1);
        } else {
            setPPUBank0(chr_reg[0][mode]);
            setPPUBank1(chr_reg[1][mode]);
            setPPUBank2(chr_reg[2][mode]);
            setPPUBank3(chr_reg[3][mode]);
            setPPUBank4(chr_reg[4][mode]);
            setPPUBank5(chr_reg[5][mode]);
            setPPUBank6(chr_reg[6][mode]);
            setPPUBank7(chr_reg[7][mode]);
        }
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
    }
}
