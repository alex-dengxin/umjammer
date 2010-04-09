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
 * Class for the Mapper 1 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper001 extends Mapper {
    /**
     * The Last Write Address
     */
    int lastWriteAddr = 0;

    /**
     * The Number of Writes to this Mapper
     */
    int writeCount = 0;

    /**
     * Bits Array
     */
    int bits = 0;

    /**
     * Mapper Registers
     */
    int regs[] = new int[4];

    /**
     * The Bank Addresses
     */
    int MMC1_bank1 = 0;

    int MMC1_bank2 = 0;

    int MMC1_bank3 = 0;

    int MMC1_bank4 = 0;

    /**
     * MMC1 Size in K
     */
    int mmc1Size = 0;

    /**
     * MMC1 Program ROM Base Address
     */
    int mmc1base256k = 0;

    /**
     * MMC1 Program ROM Swapped
     */
    int mmc1Swap = 0;

    /**
     * MMC1 High
     */
    int mmc1Hi1 = 0;

    /**
     * MMC1 Low
     */
    int mmc1Hi2 = 0;

    /**
     * Constant for 1024K MMC1
     */
    static int MMC1_1024K = 1024;

    /**
     * Constant for 512K MMC1
     */
    static int MMC1_512K = 512;

    /**
     * Constant for <512K MMC1
     */
    static int MMC1_SMALL = 1;

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
        // Ensure Address is within Correct Range
        if (addr < 0x8000)
            return;
        // Reset Write Count if Register Changed
        if ((addr & 0x6000) != (lastWriteAddr & 0x6000)) {
            writeCount = 0;
            bits = 0x00;
        }
        // Record Last Register
        lastWriteAddr = addr;
        // Reset if Bit 7 Set
        if ((data & 0x80) != 0) {
            writeCount = 0;
            bits = 0x00;
            return;
        }
        // Set Bits
        if ((data & 0x01) != 0)
            bits |= (1 << writeCount);
        // Increment Write Count
        writeCount++;
        if (writeCount < 5)
            return;
        // Determine Register Number
        int reg_num = (addr & 0x7FFF) >> 13;
        // Write Bits to Register
        regs[reg_num] = bits;
        // Reset Write Count
        writeCount = 0;
        bits = 0x00;
        // Operate on Register
        switch (reg_num) {
        case 0: {
            // Set Mirroring
            if ((regs[0] & 0x02) != 0) {
                if ((regs[0] & 0x01) != 0) {
                    setMirroringHorizontal();
                } else {
                    setMirroringVertical();
                }
            } else {
                if ((regs[0] & 0x01) != 0) {
                    setMirroring(1, 1, 1, 1);
                } else {
                    setMirroring(0, 0, 0, 0);
                }
            }
        }
            break;
        case 1: {
            int bank_num = regs[1];
            if (mmc1Size == MMC1_1024K) {
                if ((regs[0] & 0x10) != 0) {
                    if ((mmc1Swap) != 0) {
                        mmc1base256k = (regs[1] & 0x10) >> 4;
                        if ((regs[0] & 0x08) != 0) {
                            mmc1base256k |= ((regs[2] & 0x10) >> 3);
                        }
                        MMC1_set_CPU_banks();
                        mmc1Swap = 0;
                    } else {
                        mmc1Swap = 1;
                    }
                } else {
                    mmc1base256k = ((regs[1] & 0x10) != 0) ? 3 : 0;
                    MMC1_set_CPU_banks();
                }
            } else if ((mmc1Size == MMC1_512K) && (getNum1KVROMBanks() == 0)) {
                mmc1base256k = (regs[1] & 0x10) >> 4;
                MMC1_set_CPU_banks();
            } else if (getNum1KVROMBanks() != 0) {
                // Set VROM Banks
                if ((regs[0] & 0x10) != 0) {
                    // Swap 4K
                    bank_num <<= 2;
                    setPPUBank0(bank_num + 0);
                    setPPUBank1(bank_num + 1);
                    setPPUBank2(bank_num + 2);
                    setPPUBank3(bank_num + 3);
                } else {
                    // Swap 8K
                    bank_num <<= 2;
                    setPPUBanks(bank_num + 0, bank_num + 1, bank_num + 2, bank_num + 3, bank_num + 4, bank_num + 5, bank_num + 6, bank_num + 7);
                }
            } else {
                if ((regs[0] & 0x10) != 0) {
                    bank_num <<= 2;
                    setVRAMBank(0, bank_num + 0);
                    setVRAMBank(1, bank_num + 1);
                    setVRAMBank(2, bank_num + 2);
                    setVRAMBank(3, bank_num + 3);
                }
            }
        }
            break;
        case 2: {
            int bank_num = regs[2];
            if ((mmc1Size == MMC1_1024K) && ((regs[0] & 0x08) != 0)) {
                if ((mmc1Swap) != 0) {
                    mmc1base256k = (regs[1] & 0x10) >> 4;
                    mmc1base256k |= ((regs[2] & 0x10) >> 3);
                    MMC1_set_CPU_banks();
                    mmc1Swap = 0;
                } else {
                    mmc1Swap = 1;
                }
            }
            if (getNum1KVROMBanks() == 0) {
                if ((regs[0] & 0x10) != 0) {
                    bank_num <<= 2;
                    setVRAMBank(4, bank_num + 0);
                    setVRAMBank(5, bank_num + 1);
                    setVRAMBank(6, bank_num + 2);
                    setVRAMBank(7, bank_num + 3);
                    break;
                }
            }
            if ((regs[0] & 0x10) != 0) {
                // Swap 4K
                bank_num <<= 2;
                setPPUBank4(bank_num + 0);
                setPPUBank5(bank_num + 1);
                setPPUBank6(bank_num + 2);
                setPPUBank7(bank_num + 3);
            }
        }
            break;
        case 3: {
            // Set Program ROM Bank
            int bank_num = regs[3];
            if ((regs[0] & 0x08) != 0) {
                // 16K of ROM
                bank_num <<= 1;
                if ((regs[0] & 0x04) != 0) {
                    // 16K of ROM at $8000
                    MMC1_bank1 = bank_num;
                    MMC1_bank2 = bank_num + 1;
                    MMC1_bank3 = mmc1Hi1;
                    MMC1_bank4 = mmc1Hi2;
                } else {
                    // 16K of ROM at $C000
                    if (mmc1Size == MMC1_SMALL) {
                        MMC1_bank1 = 0;
                        MMC1_bank2 = 1;
                        MMC1_bank3 = bank_num;
                        MMC1_bank4 = bank_num + 1;
                    }
                }
            } else {
                // 32K of ROM at $8000
                bank_num <<= 1;
                MMC1_bank1 = bank_num;
                MMC1_bank2 = bank_num + 1;
                if (mmc1Size == MMC1_SMALL) {
                    MMC1_bank3 = bank_num + 2;
                    MMC1_bank4 = bank_num + 3;
                }
            }
            MMC1_set_CPU_banks();
        }
            break;
        }
    }

    /**
     * Set CPU Banks for MMC1
     */
    private void MMC1_set_CPU_banks() {
        setCPUBanks((mmc1base256k << 5) + (MMC1_bank1 & ((256 / 8) - 1)), (mmc1base256k << 5) + (MMC1_bank2 & ((256 / 8) - 1)), (mmc1base256k << 5) + (MMC1_bank3 & ((256 / 8) - 1)), (mmc1base256k << 5) + (MMC1_bank4 & ((256 / 8) - 1)));
    }

    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 1;
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Reset the Write Counters
        writeCount = 0;
        bits = 0x00;
        // Reset the Registers
        regs[0] = 0x0C;
        regs[1] = 0x00;
        regs[2] = 0x00;
        regs[3] = 0x00;
        // Determine the Size in K of the Program ROM
        int size_in_K = getNum8KRomBanks() * 8;
        if (size_in_K == 1024) {
            mmc1Size = MMC1_1024K;
        } else if (size_in_K == 512) {
            mmc1Size = MMC1_512K;
        } else {
            mmc1Size = MMC1_SMALL;
        }
        // Select the First 256K
        mmc1base256k = 0;
        mmc1Swap = 0;
        // Map the High Pages
        if (mmc1Size == MMC1_SMALL) {
            // Set two High Pages to Last two Banks
            mmc1Hi1 = getNum8KRomBanks() - 2;
            mmc1Hi2 = getNum8KRomBanks() - 1;
        } else {
            // Set two High Pages to Last Two Banks of Current 256K Region
            mmc1Hi1 = (256 / 8) - 2;
            mmc1Hi2 = (256 / 8) - 1;
        }
        // Set CPU Banks
        MMC1_bank1 = 0;
        MMC1_bank2 = 1;
        MMC1_bank3 = mmc1Hi1;
        MMC1_bank4 = mmc1Hi2;
        MMC1_set_CPU_banks();
        // Set PPUMemory Bank Addresses
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        lastWriteAddr = (input.read() & 0xFF) << 8;
        lastWriteAddr |= (input.read() & 0xFF);
        writeCount = (input.read() & 0xFF);
        bits = (input.read() & 0xFF);
        for (int i = 0; i < regs.length; i++)
            regs[i] = (input.read() & 0xFF);
        MMC1_bank1 = (input.read() & 0xFF);
        MMC1_bank2 = (input.read() & 0xFF);
        MMC1_bank3 = (input.read() & 0xFF);
        MMC1_bank4 = (input.read() & 0xFF);
        mmc1Size = (input.read() & 0xFF);
        mmc1base256k = (input.read() & 0xFF);
        mmc1Swap = (input.read() & 0xFF);
        mmc1Hi1 = (input.read() & 0xFF);
        mmc1Hi2 = (input.read() & 0xFF);
        MMC1_set_CPU_banks();
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        output.write((lastWriteAddr >> 8) & 0xFF);
        output.write(lastWriteAddr & 0xFF);
        output.write(writeCount & 0xFF);
        output.write(bits & 0xFF);
        for (int i = 0; i < regs.length; i++)
            output.write(regs[i] & 0xFF);
        output.write(MMC1_bank1 & 0xFF);
        output.write(MMC1_bank2 & 0xFF);
        output.write(MMC1_bank3 & 0xFF);
        output.write(MMC1_bank4 & 0xFF);
        output.write(mmc1Size & 0xFF);
        output.write(mmc1base256k & 0xFF);
        output.write(mmc1Swap & 0xFF);
        output.write(mmc1Hi1 & 0xFF);
        output.write(mmc1Hi2 & 0xFF);
    }
}
