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
 * Class for the Mapper 47 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper047 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 47;
    }

    /**
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        System.out.println("Mapper047: " + crc);
        patch = 0;
        // Super Mario Bros, Tetris, Nintendo World Cup
        if (crc == 2055219138l) {
            patch = 1;
        }
    }

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

    /**
     * The current Interrupt Counter Value
     */
    private int irq_counter = 0;

    /**
     * Whether the Interrupt Counter is enabled
     */
    private boolean irq_enabled = false;

    /**
     * Interrupt Latch Value
     */
    private int irq_latch = 0;

    /**
     * The Registers for the MMC
     */
    private int[] regs = new int[8];

    /**
     * Program ROM Pointer for Bank 0
     */
    private int prg0 = 0;

    /**
     * Program ROM Pointer for Bank 1
     */
    private int prg1 = 1;

    /**
     * VROM Pointer for Bank 0 (and 1)
     */
    private int chr01 = 0;

    /**
     * VROM Pointer for Bank 2 (and 3)
     */
    private int chr23 = 0;

    /**
     * VROM Pointer for Bank 4
     */
    private int chr4 = 0;

    /**
     * VROM Pointer for Bank 5
     */
    private int chr5 = 0;

    /**
     * VROM Pointer for Bank 6
     */
    private int chr6 = 0;

    /**
     * VROM Pointer for Bank 7
     */
    private int chr7 = 0;

    /**
     * ROM Bank
     */
    private int rom_bank = 0;

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Clear the Registers
        for (int i = 0; i < 8; i++)
            regs[i] = 0;
        // Switch Banks 8-B to First 16k of Program ROM
        rom_bank = 0;
        prg0 = 0;
        prg1 = 1;
        MMC3_set_CPU_banks();
        // Set VROM Banks
        if (getNum1KVROMBanks() > 0) {
            chr01 = 0;
            chr23 = 2;
            chr4 = 4;
            chr5 = 5;
            chr6 = 6;
            chr7 = 7;
            MMC3_set_PPU_banks();
        } else {
            chr01 = chr23 = chr4 = chr5 = chr6 = chr7 = 0;
        }
        // Reset IRQ Status
        irq_enabled = false;
        irq_counter = 0;
        irq_latch = 0;
    }

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
        // Check Address within Range
        if (addr >= 0x6000 && addr < 0x8000) {
            if (addr == 0x6000) {
                if (patch == 1) {
                    rom_bank = (data & 0x06) >> 1;
                } else {
                    rom_bank = (data & 0x01) << 1;
                }
                MMC3_set_CPU_banks();
                MMC3_set_PPU_banks();
            }
            return;
        }
        // Determine the Function
        switch (addr & 0xE001) {
        // Command Register
        case 0x8000: {
            regs[0] = data;
            MMC3_set_PPU_banks();
            MMC3_set_CPU_banks();
        }
            break;
        // Activate Register
        case 0x8001: {
            regs[1] = data;
            int bank_num = regs[1];
            switch (regs[0] & 0x07) {
            case 0x00: {
                if (getNum1KVROMBanks() > 0) {
                    bank_num &= 0xfe;
                    chr01 = bank_num;
                    MMC3_set_PPU_banks();
                }
            }
                break;
            case 0x01: {
                if (getNum1KVROMBanks() > 0) {
                    bank_num &= 0xfe;
                    chr23 = bank_num;
                    MMC3_set_PPU_banks();
                }
            }
                break;
            case 0x02: {
                if (getNum1KVROMBanks() > 0) {
                    chr4 = bank_num;
                    MMC3_set_PPU_banks();
                }
            }
                break;
            case 0x03: {
                if (getNum1KVROMBanks() > 0) {
                    chr5 = bank_num;
                    MMC3_set_PPU_banks();
                }
            }
                break;
            case 0x04: {
                if (getNum1KVROMBanks() > 0) {
                    chr6 = bank_num;
                    MMC3_set_PPU_banks();
                }
            }
                break;
            case 0x05: {
                if (getNum1KVROMBanks() > 0) {
                    chr7 = bank_num;
                    MMC3_set_PPU_banks();
                }
            }
                break;
            case 0x06: {
                prg0 = bank_num;
                MMC3_set_CPU_banks();
            }
                break;
            case 0x07: {
                prg1 = bank_num;
                MMC3_set_CPU_banks();
            }
                break;
            }
        }
            break;
        // Handle Mirroring
        case 0xA000: {
            regs[2] = data;
            if (!mm.ppu.mirrorFourScreen) {
                if ((data & 0x01) != 0) {
                    setMirroringHorizontal();
                } else {
                    setMirroringVertical();
                }
            }
        }
            break;
        case 0xA001: {
            regs[3] = data;
        }
            break;
        // Store IRQ Counter
        case 0xC000: {
            regs[4] = data;
            irq_counter = regs[4];
        }
            break;
        // Store IRQ Counter
        case 0xC001: {
            regs[5] = data;
            irq_latch = regs[5];
        }
            break;
        // Disable IRQ
        case 0xE000: {
            regs[6] = data;
            irq_enabled = false;
        }
            break;
        // Enable IRQ
        case 0xE001: {
            regs[7] = data;
            irq_enabled = true;
        }
            break;
        }
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     */
    public final int syncH(int scanline) {
        // Check if IRQ Enabled
        if (irq_enabled) {
            // Check for Visible Scanline
            if ((scanline >= 0) && (scanline < 240)) {
                // Check if Background or Sprites Enabled
                if ((mm.nes.ppu.REG_2001 & 0x18) != 00) {
                    // Decrement IRQ Counter
                    irq_counter--;
                    // Check if Counter Down to Zero
                    if (irq_counter < 0) {
                        // Set Counter to Latch and Fire Interupt
                        irq_counter = irq_latch;
                        return 3;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Set the MMC3 CPU Banks
     */
    private void MMC3_set_CPU_banks() {
        if (prg_swap()) {
            setCPUBank8(rom_bank * 8 + ((patch == 1 && rom_bank != 2) ? 6 : 14));
            setCPUBankA(rom_bank * 8 + prg1);
            setCPUBankC(rom_bank * 8 + prg0);
            setCPUBankE(rom_bank * 8 + ((patch == 1 && rom_bank != 2) ? 7 : 15));
        } else {
            setCPUBank8(rom_bank * 8 + prg0);
            setCPUBankA(rom_bank * 8 + prg1);
            setCPUBankC(rom_bank * 8 + ((patch == 1 && rom_bank != 2) ? 6 : 14));
            setCPUBankE(rom_bank * 8 + ((patch == 1 && rom_bank != 2) ? 7 : 15));
        }
    }

    /**
     * Set the MMC3 PPU Banks
     */
    private void MMC3_set_PPU_banks() {
        // Check if VROM Banks Exist and Map Them
        if (getNum1KVROMBanks() != 0) {
            // Check if Swap Low and High Character ROM
            if (chr_swap()) {
                setPPUBank0((rom_bank & 0x02) * 64 + chr4);
                setPPUBank1((rom_bank & 0x02) * 64 + chr5);
                setPPUBank2((rom_bank & 0x02) * 64 + chr6);
                setPPUBank3((rom_bank & 0x02) * 64 + chr7);
                setPPUBank4((rom_bank & 0x02) * 64 + chr01 + 0);
                setPPUBank5((rom_bank & 0x02) * 64 + chr01 + 1);
                setPPUBank6((rom_bank & 0x02) * 64 + chr23 + 0);
                setPPUBank7((rom_bank & 0x02) * 64 + chr23 + 1);
            } else {
                setPPUBank0((rom_bank & 0x02) * 64 + chr01 + 0);
                setPPUBank1((rom_bank & 0x02) * 64 + chr01 + 1);
                setPPUBank2((rom_bank & 0x02) * 64 + chr23 + 0);
                setPPUBank3((rom_bank & 0x02) * 64 + chr23 + 1);
                setPPUBank4((rom_bank & 0x02) * 64 + chr4);
                setPPUBank5((rom_bank & 0x02) * 64 + chr5);
                setPPUBank6((rom_bank & 0x02) * 64 + chr6);
                setPPUBank7((rom_bank & 0x02) * 64 + chr7);
            }
        }
    }

    /**
     * Determine if Character ROM is Swapped
     */
    private boolean chr_swap() {
        return (regs[0] & 0x80) != 0;
    }

    /**
     * Determine if Program ROM is Swapped
     */
    private boolean prg_swap() {
        return (regs[0] & 0x40) != 0;
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load MMC3 Information
        irq_counter = input.read() & 0xFF;
        irq_enabled = (input.read() == 0xFF);
        irq_latch = input.read() & 0xFF;
        for (int i = 0; i < regs.length; i++)
            regs[i] = input.read() & 0xFF;
        prg0 = input.read() & 0xFF;
        prg1 = input.read() & 0xFF;
        chr01 = input.read() & 0xFF;
        chr23 = input.read() & 0xFF;
        chr4 = input.read() & 0xFF;
        chr5 = input.read() & 0xFF;
        chr6 = input.read() & 0xFF;
        chr7 = input.read() & 0xFF;
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save MMC3 Information
        output.write(irq_counter & 0xFF);
        output.write(irq_enabled ? 0xFF : 0x00);
        output.write(irq_latch & 0xFF);
        for (int i = 0; i < regs.length; i++)
            output.write(regs[i] & 0xFF);
        output.write(prg0 & 0xFF);
        output.write(prg1 & 0xFF);
        output.write(chr01 & 0xFF);
        output.write(chr23 & 0xFF);
        output.write(chr4 & 0xFF);
        output.write(chr5 & 0xFF);
        output.write(chr6 & 0xFF);
        output.write(chr7 & 0xFF);
    }
}
