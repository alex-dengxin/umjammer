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
 * Class for the Mapper 18 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper018 extends Mapper {
    /**
     * Irq Counter
     */
    private int irq_counter = 0;

    /**
     * Irq Latch
     */
    private int irq_latch = 0;

    /**
     * Whether the IRQ has been Set
     */
    private boolean irq_enabled = false;

    /**
     * Whether the IRQ has been Set
     */
    private int regs[] = new int[11];

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
        // Set Program ROM Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        regs[0] = 0;
        regs[1] = 1;
        regs[2] = getNum8KRomBanks() - 2;
        regs[3] = getNum8KRomBanks() - 1;
        regs[4] = 0;
        regs[5] = 0;
        regs[6] = 0;
        regs[7] = 0;
        regs[8] = 0;
        regs[9] = 0;
        regs[10] = 0;
        // IRQ Settings
        irq_enabled = false;
        irq_latch = 0;
        irq_counter = 0;
        // Set VROM Banks (Standard)
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        } else {
            setVRAMBank(0, 0);
            setVRAMBank(0, 1);
            setVRAMBank(0, 2);
            setVRAMBank(0, 3);
            setVRAMBank(0, 4);
            setVRAMBank(0, 5);
            setVRAMBank(0, 6);
            setVRAMBank(0, 7);
        }
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        // regs[ 0] ... 8K PROM bank at CPU $8000
        // regs[ 1] ... 8K PROM bank at CPU $A000
        // regs[ 2] ... 8K PROM bank at CPU $C000
        // regs[ 3] ... 1K VROM bank at PPU $0000
        // regs[ 4] ... 1K VROM bank at PPU $0400
        // regs[ 5] ... 1K VROM bank at PPU $0800
        // regs[ 6] ... 1K VROM bank at PPU $0C00
        // regs[ 7] ... 1K VROM bank at PPU $1000
        // regs[ 8] ... 1K VROM bank at PPU $1400
        // regs[ 9] ... 1K VROM bank at PPU $1800
        // regs[10] ... 1K VROM bank at PPU $1C00
        // Check Range
        if (addr < 0x8000)
            return;
        // Do Function
        switch (addr) {
        case 0x8000: {
            regs[0] = (regs[0] & 0xF0) | (data & 0x0F);
            setCPUBank8(regs[0]);
        }
            break;
        case 0x8001: {
            regs[0] = (regs[0] & 0x0F) | ((data & 0x0F) << 4);
            setCPUBank8(regs[0]);
        }
            break;
        case 0x8002: {
            regs[1] = (regs[1] & 0xF0) | (data & 0x0F);
            setCPUBankA(regs[1]);
        }
            break;
        case 0x8003: {
            regs[1] = (regs[1] & 0x0F) | ((data & 0x0F) << 4);
            setCPUBankA(regs[1]);
        }
            break;
        case 0x9000: {
            regs[2] = (regs[2] & 0xF0) | (data & 0x0F);
            setCPUBankC(regs[2]);
        }
            break;
        case 0x9001: {
            regs[2] = (regs[2] & 0x0F) | ((data & 0x0F) << 4);
            setCPUBankC(regs[2]);
        }
            break;
        case 0xA000: {
            regs[3] = (regs[3] & 0xF0) | (data & 0x0F);
            setPPUBank0(regs[3]);
        }
            break;
        case 0xA001: {
            regs[3] = (regs[3] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank0(regs[3]);
        }
            break;
        case 0xA002: {
            regs[4] = (regs[4] & 0xF0) | (data & 0x0F);
            setPPUBank1(regs[4]);
        }
            break;
        case 0xA003: {
            regs[4] = (regs[4] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank1(regs[4]);
        }
            break;
        case 0xB000: {
            regs[5] = (regs[5] & 0xF0) | (data & 0x0F);
            setPPUBank2(regs[5]);
        }
            break;
        case 0xB001: {
            regs[5] = (regs[5] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank2(regs[5]);
        }
            break;
        case 0xB002: {
            regs[6] = (regs[6] & 0xF0) | (data & 0x0F);
            setPPUBank3(regs[6]);
        }
            break;
        case 0xB003: {
            regs[6] = (regs[6] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank3(regs[6]);
        }
            break;
        case 0xC000: {
            regs[7] = (regs[7] & 0xF0) | (data & 0x0F);
            setPPUBank4(regs[7]);
        }
            break;
        case 0xC001: {
            regs[7] = (regs[7] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank4(regs[7]);
        }
            break;
        case 0xC002: {
            regs[8] = (regs[8] & 0xF0) | (data & 0x0F);
            setPPUBank5(regs[8]);
        }
            break;
        case 0xC003: {
            regs[8] = (regs[8] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank5(regs[8]);
        }
            break;
        case 0xD000: {
            regs[9] = (regs[9] & 0xF0) | (data & 0x0F);
            setPPUBank6(regs[9]);
        }
            break;
        case 0xD001: {
            regs[9] = (regs[9] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank6(regs[9]);
        }
            break;
        case 0xD002: {
            regs[10] = (regs[10] & 0xF0) | (data & 0x0F);
            setPPUBank7(regs[10]);
        }
            break;
        case 0xD003: {
            regs[10] = (regs[10] & 0x0F) | ((data & 0x0F) << 4);
            setPPUBank7(regs[10]);
        }
            break;
        case 0xE000: {
            irq_latch = (irq_latch & 0xFFF0) | (data & 0x0F);
        }
            break;
        case 0xE001: {
            irq_latch = (irq_latch & 0xFF0F) | ((data & 0x0F) << 4);
        }
            break;
        case 0xE002: {
            irq_latch = (irq_latch & 0xF0FF) | ((data & 0x0F) << 8);
        }
            break;
        case 0xE003: {
            irq_latch = (irq_latch & 0x0FFF) | ((data & 0x0F) << 12);
        }
            break;
        case 0xF000: {
            irq_counter = irq_latch;
        }
            break;
        case 0xF001: {
            irq_enabled = (data & 0x01) != 0;
        }
            break;
        case 0xF002: {
            data &= 0x03;
            if (data == 0) {
                setMirroringHorizontal();
            } else if (data == 1) {
                setMirroringVertical();
            } else {
                setMirroringOneScreenLow();
            }
        }
            break;
        }
    }

    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 18;
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if (irq_enabled) {
            if (irq_counter <= 113) {
                irq_counter = (patch == 1) ? 114 : 0;
                irq_enabled = false;
                return 3;
            } else {
                irq_counter -= 113;
            }
        }
        return 0;
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load Registers
        irq_counter = (input.read() << 8) + input.read();
        irq_latch = (input.read() << 8) + input.read();
        irq_enabled = (input.read() == 0xFF);
        for (int i = 0; i < regs.length; i++)
            regs[i] = input.read();
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Registers
        output.write((irq_counter & 0xFF00) >> 8);
        output.write(irq_counter & 0xFF);
        output.write((irq_latch & 0xFF00) >> 8);
        output.write(irq_latch & 0xFF);
        output.write(irq_enabled ? 0xFF : 0x00);
        for (int i = 0; i < regs.length; i++)
            output.write(regs[i]);
    }

    int patch = 0;

    /**
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        patch = 0;
        if (crc == 253471719l) {
            // Jajamaru Gekimaden - Maboroshi no Kinmajou (J)
            patch = 1;
            System.out.println("Patched CRC:" + crc);
        }
    }
}
