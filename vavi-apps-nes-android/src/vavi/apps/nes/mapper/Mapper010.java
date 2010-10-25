/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 010 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper010 extends Mapper implements Serializable {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 10;
    }

    /**
     * The Low Latch Select.
     */
    private int Latch0;

    /**
     * The High Latch Select.
     */
    private int Latch1;

    /**
     * The Low FD Latch Value.
     */
    private int Latch0FD;

    /**
     * The Low FE Latch Value.
     */
    private int Latch0FE;

    /**
     * The High FD Latch Value.
     */
    private int Latch1FD;

    /**
     * The High FE Latch Value.
     */
    private int Latch1FE;

    /**
     * Initialise the Mapper.
     */
    public final void init(MemoryManager MM) {
        // Assign Local Pointer for Memory Manager Object
        mm = MM;
        mm.ppu.latchMapper = true;
        // Cause a Reset
        reset();
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set Initial Latches
        Latch0FD = 0;
        Latch0FE = 4;
        Latch1FD = 0;
        Latch1FE = 0;
        // Switch First Bank into 0x8000 and last 24K into 0xA000-0xFFFF
        int num8kROMBanks = getNum8KRomBanks();
        setCPUBanks(0, 1, num8kROMBanks - 2, num8kROMBanks - 1);
        // Set Default Latch Values
        Latch0 = 0xFE;
        Latch1 = 0xFE;
        // Switch PPU Memory
        mm.ppu.setPPUBankStartAddress(0, Latch0FE * 0x1000 + 0x0000);
        mm.ppu.setPPUBankStartAddress(1, Latch0FE * 0x1000 + 0x0400);
        mm.ppu.setPPUBankStartAddress(2, Latch0FE * 0x1000 + 0x0800);
        mm.ppu.setPPUBankStartAddress(3, Latch0FE * 0x1000 + 0x0C00);
        mm.ppu.setPPUBankStartAddress(4, Latch1FE * 0x1000 + 0x0000);
        mm.ppu.setPPUBankStartAddress(5, Latch1FE * 0x1000 + 0x0400);
        mm.ppu.setPPUBankStartAddress(6, Latch1FE * 0x1000 + 0x0800);
        mm.ppu.setPPUBankStartAddress(7, Latch1FE * 0x1000 + 0x0C00);
    }

    /**
     * Access the Mapper.
     */
    public final void access(int address, int value) {
        // Check within Range
        if (address < 0x8000)
            return;
        // Ensure Value is Within Range
        value &= 0xFF;
        // Determine the Address and Function
        address &= 0xF000;
        // Perform Function on Mapper
        switch (address) {
        case 0xA000: // Select 16K ROM bank at $8000
            setCPUBank8(value * 2);
            setCPUBankA(value * 2 + 1);
            return;
        case 0xB000: // Latch 0FD Select
            Latch0FD = value;
            if (Latch0 == 0xFD) {
                mm.ppu.setPPUBankStartAddress(0, value * 0x1000 + 0x0000);
                mm.ppu.setPPUBankStartAddress(1, value * 0x1000 + 0x0400);
                mm.ppu.setPPUBankStartAddress(2, value * 0x1000 + 0x0800);
                mm.ppu.setPPUBankStartAddress(3, value * 0x1000 + 0x0C00);
            }
            return;
        case 0xC000: // Latch 0FE Select
            Latch0FE = value;
            if (Latch0 == 0xFE) {
                mm.ppu.setPPUBankStartAddress(0, value * 0x1000 + 0x0000);
                mm.ppu.setPPUBankStartAddress(1, value * 0x1000 + 0x0400);
                mm.ppu.setPPUBankStartAddress(2, value * 0x1000 + 0x0800);
                mm.ppu.setPPUBankStartAddress(3, value * 0x1000 + 0x0C00);
            }
            return;
        case 0xD000: // Latch 1FD Select
            Latch1FD = value;
            if (Latch1 == 0xFD) {
                mm.ppu.setPPUBankStartAddress(4, value * 0x1000 + 0x0000);
                mm.ppu.setPPUBankStartAddress(5, value * 0x1000 + 0x0400);
                mm.ppu.setPPUBankStartAddress(6, value * 0x1000 + 0x0800);
                mm.ppu.setPPUBankStartAddress(7, value * 0x1000 + 0x0C00);
            }
            return;
        case 0xE000: // Latch 1FE Select
            Latch1FE = value;
            if (Latch1 == 0xFE) {
                mm.ppu.setPPUBankStartAddress(4, value * 0x1000 + 0x0000);
                mm.ppu.setPPUBankStartAddress(5, value * 0x1000 + 0x0400);
                mm.ppu.setPPUBankStartAddress(6, value * 0x1000 + 0x0800);
                mm.ppu.setPPUBankStartAddress(7, value * 0x1000 + 0x0C00);
            }
            return;
        case 0xF000: // Mirroring Select
            if ((value & 0x01) != 0)
                setMirroringHorizontal();
            else
                setMirroringVertical();
            return;
        }
    }

    /**
     * Latch the Memory Mapper.
     */
    public final void latch(int address) {
        if ((address & 0x1FF0) == 0x0FD0 && Latch0 != 0xFD) {
            mm.ppu.setPPUBankStartAddress(0, Latch0FD * 0x1000 + 0x0000);
            mm.ppu.setPPUBankStartAddress(1, Latch0FD * 0x1000 + 0x0400);
            mm.ppu.setPPUBankStartAddress(2, Latch0FD * 0x1000 + 0x0800);
            mm.ppu.setPPUBankStartAddress(3, Latch0FD * 0x1000 + 0x0C00);
            Latch0 = 0xFD;
        } else if ((address & 0x1FF0) == 0x0FE0 && Latch0 != 0xFE) {
            mm.ppu.setPPUBankStartAddress(0, Latch0FE * 0x1000 + 0x0000);
            mm.ppu.setPPUBankStartAddress(1, Latch0FE * 0x1000 + 0x0400);
            mm.ppu.setPPUBankStartAddress(2, Latch0FE * 0x1000 + 0x0800);
            mm.ppu.setPPUBankStartAddress(3, Latch0FE * 0x1000 + 0x0C00);
            Latch0 = 0xFE;
        } else if ((address & 0x1FF0) == 0x1FD0 && Latch1 != 0xFD) {
            mm.ppu.setPPUBankStartAddress(4, Latch1FD * 0x1000 + 0x0000);
            mm.ppu.setPPUBankStartAddress(5, Latch1FD * 0x1000 + 0x0400);
            mm.ppu.setPPUBankStartAddress(6, Latch1FD * 0x1000 + 0x0800);
            mm.ppu.setPPUBankStartAddress(7, Latch1FD * 0x1000 + 0x0C00);
            Latch1 = 0xFD;
        } else if ((address & 0x1FF0) == 0x1FE0 && Latch1 != 0xFE) {
            mm.ppu.setPPUBankStartAddress(4, Latch1FE * 0x1000 + 0x0000);
            mm.ppu.setPPUBankStartAddress(5, Latch1FE * 0x1000 + 0x0400);
            mm.ppu.setPPUBankStartAddress(6, Latch1FE * 0x1000 + 0x0800);
            mm.ppu.setPPUBankStartAddress(7, Latch1FE * 0x1000 + 0x0C00);
            Latch1 = 0xFE;
        }
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load the Latch Values
        Latch0 = input.read() & 0xFF;
        Latch1 = input.read() & 0xFF;
        Latch0FD = input.read() & 0xFF;
        Latch0FE = input.read() & 0xFF;
        Latch1FD = input.read() & 0xFF;
        Latch1FE = input.read() & 0xFF;
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save the Latch Values
        output.write(Latch0 & 0xFF);
        output.write(Latch1 & 0xFF);
        output.write(Latch0FD & 0xFF);
        output.write(Latch0FE & 0xFF);
        output.write(Latch1FD & 0xFF);
        output.write(Latch1FE & 0xFF);
    }
}
