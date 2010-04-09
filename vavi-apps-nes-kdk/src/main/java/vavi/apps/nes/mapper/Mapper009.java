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
 * Class for the Mapper 009 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.55f
 */
public class Mapper009 extends Mapper implements Serializable {
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
        setCPUBanks(0, num8kROMBanks - 3, num8kROMBanks - 2, num8kROMBanks - 1);
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
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 9;
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
        case 0xA000: // Select 8K ROM bank at $8000 - $9000
            setCPUBank8(value);
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
    /**************************************************************************
     * Mr Dream and Mike Tyson Punch-Out : Memory Map
     ************************************************************************** 
     * 0001 : Opponent 0006 : Round Number 000D : Current CPU Bank in 0x8000
     * (written to 0xAFFF) 008F : TKO if equal to 3 00E0 : Temporary Variable
     * (Used during Health Routine) 00E7 : Temporary Variable (Number of Hearts
     * to Decrease) 0120 : Start of Password 0129 : End of Password 0170 : MSB
     * of number of wins (BCD format) 0171 : LSB of number of wins 0172 : MSB of
     * number of loses 0173 : LSB of number of loses 0174 : MSB of number of KOs
     * 0175 : LSB of number of KOs 0302 : Minutes 0304 : Ten Second Units 0305 :
     * Seconds 0306 : Milliseconds (wrapped at 100) 0307 : Milliseconds LSB 0308
     * : Milliseconds Increment for Round MSB (R1:04, R2:05, R3:05) 0309 :
     * Milliseconds Incrememt for Round LSB (R1:F9, R2:6D, R3:F8) 0321 : New
     * Hearts MSB (gets blanked after copied across to 0323) 0322 : New Hearts
     * LSB (gets blanked after copied across to 0324) 0323 : Hearts MSB (BCD
     * format) 0324 : Hearts LSB 0325 : Redraw Hearts if Negative (#$80) 0326 :
     * Hearts MSB (ASCII $40+0323) 0327 : Hearts LSB (ASCII $40+0324) 0341 :
     * Star Increase Flag (00:no change, 01:Increase, FF:Decrease) 0342 : Stars
     * 034A : Star ASCII Display (40+0342) 0391 : Mac Energy (for setting
     * energy) 0392 : Mac Energy (recognised by system as new amount) 0393 : Mac
     * Energy (current actual amount) 0398 : Enemy Boxer Energy (new amount set)
     * 0399 : Enemy Boxer Energy (recognised as new amount by system) 039A :
     * Enemy Boxer Energy (current actual amount) 03C1 : Incremented by x every
     * time Mac is knocked down (keep 0 for infinite knock-downs) 03D0 : Number
     * of Times Mac Down 03D1 : Number of Times Opponent Down 03DD : Number of
     * Times Mac Down in Round 1 03DE : Number of Times Mac Down in Round 2 03DF
     * : Number of Times Mac Down in Round 3 03E0 : Set to 0x80 to signal new
     * Score Addition 03E1 : Score to Add Position 6 03E2 : Score to Add
     * Position 5 03E3 : Score to Add Position 4 03E4 : Score to Add Position 3
     * 03E5 : Score to Add Position 2 03E6 : Score to Add Position 1 03E7 ? 03E8
     * : Score Position 6 03E9 : Score Position 5 03EA : Score Position 4 03EB :
     * Score Position 3 03EC : Score Position 2 03ED : Score Position 1
     */
}
