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
 * Class for the Mapper 119 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper119 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 119;
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
    private int chr23 = 2;

    /**
     * VROM Pointer for Bank 4
     */
    private int chr4 = 4;

    /**
     * VROM Pointer for Bank 5
     */
    private int chr5 = 5;

    /**
     * VROM Pointer for Bank 6
     */
    private int chr6 = 6;

    /**
     * VROM Pointer for Bank 7
     */
    private int chr7 = 7;

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Clear the Registers
        for (int i = 0; i < 8; i++)
            regs[i] = 0;
        // Switch Banks 8-B to First 16k of Program ROM
        prg0 = 0;
        prg1 = 1;
        MMC3_set_CPU_banks();
        // Set VROM Banks
        chr01 = 0;
        chr23 = 2;
        chr4 = 4;
        chr5 = 5;
        chr6 = 6;
        chr7 = 7;
        MMC3_set_PPU_banks();
        // Reset IRQ Status
        irq_enabled = false;
        irq_counter = 0;
        irq_latch = 0;
        // Set VRAM Write Protect
        mm.nes.ppu.vram_write_protect = false;
    }

    /**
     * Access the Mapper.
     */
    public final void access(int addr, int data) {
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
        // Map Program ROM
        if (prg_swap()) {
            setCPUBanks(getNum8KRomBanks() - 2, prg1, prg0, getNum8KRomBanks() - 1);
        } else {
            setCPUBanks(prg0, prg1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        }
    }

    /**
     * Set the MMC3 PPU Banks
     */
    private void MMC3_set_PPU_banks() {
        int chr_bank[] = new int[8];
        if (chr_swap()) {
            chr_bank[0] = chr4;
            chr_bank[1] = chr5;
            chr_bank[2] = chr6;
            chr_bank[3] = chr7;
            chr_bank[4] = chr01 + 0;
            chr_bank[5] = chr01 + 1;
            chr_bank[6] = chr23 + 0;
            chr_bank[7] = chr23 + 1;
        } else {
            chr_bank[0] = chr01 + 0;
            chr_bank[1] = chr01 + 1;
            chr_bank[2] = chr23 + 0;
            chr_bank[3] = chr23 + 1;
            chr_bank[4] = chr4;
            chr_bank[5] = chr5;
            chr_bank[6] = chr6;
            chr_bank[7] = chr7;
        }
        if ((chr_bank[0] & 0x40) != 0)
            setVRAMBank(0, chr_bank[0] & 0x07);
        else
            setPPUBank0(chr_bank[0]);
        if ((chr_bank[1] & 0x40) != 0)
            setVRAMBank(1, chr_bank[1] & 0x07);
        else
            setPPUBank1(chr_bank[1]);
        if ((chr_bank[2] & 0x40) != 0)
            setVRAMBank(2, chr_bank[2] & 0x07);
        else
            setPPUBank2(chr_bank[2]);
        if ((chr_bank[3] & 0x40) != 0)
            setVRAMBank(3, chr_bank[3] & 0x07);
        else
            setPPUBank3(chr_bank[3]);
        if ((chr_bank[4] & 0x40) != 0)
            setVRAMBank(4, chr_bank[4] & 0x07);
        else
            setPPUBank4(chr_bank[4]);
        if ((chr_bank[5] & 0x40) != 0)
            setVRAMBank(5, chr_bank[5] & 0x07);
        else
            setPPUBank5(chr_bank[5]);
        if ((chr_bank[6] & 0x40) != 0)
            setVRAMBank(6, chr_bank[6] & 0x07);
        else
            setPPUBank6(chr_bank[6]);
        if ((chr_bank[7] & 0x40) != 0)
            setVRAMBank(7, chr_bank[7] & 0x07);
        else
            setPPUBank7(chr_bank[7]);
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
