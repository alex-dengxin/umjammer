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
 * Class for the Mapper 65 used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper065 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 65;
    }

    /**
     * Irq Counter
     */
    private int irq_counter = 0;

    /**
     * Whether the IRQ has been Set
     */
    private boolean irq_enabled = false;

    /**
     * IRQ Latch Value
     */
    private int irq_latch = 0;

    /**
     * Patch Values
     */
    private int patch = 0;

    private int patch2 = 0;

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
        // Check Range
        if (addr < 0x8000)
            return;
        // Perform Function
        switch (addr) {
        case 0x9000: {
            if (patch == 0) {
                if ((data & 0x40) != 0) {
                    setMirroringVertical();
                } else {
                    setMirroringHorizontal();
                }
            }
        }
            break;
        case 0x9001: {
            if (patch == 1) {
                if ((data & 0x80) != 0) {
                    setMirroringHorizontal();
                } else {
                    setMirroringVertical();
                }
            }
        }
            break;
        case 0x9003: {
            if (patch == 0)
                irq_enabled = (data & 0x80) != 0;
        }
            break;
        case 0x9004: {
            if (patch == 0)
                irq_counter = irq_latch;
        }
            break;
        case 0x9005: {
            if (patch == 1) {
                irq_counter = data << 1;
                irq_enabled = (data != 0);
            } else {
                irq_latch = (irq_latch & 0x00FF) | (data << 8);
            }
        }
            break;
        case 0x9006: {
            if (patch == 1) {
                irq_enabled = true;
            } else {
                irq_latch = (irq_latch & 0xFF00) | data;
            }
        }
            break;
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
            setPPUBank6(data);
        }
            break;
        case 0xB007: {
            setPPUBank7(data);
        }
            break;
        case 0x8000: {
            setCPUBank8(data);
        }
            break;
        case 0xA000: {
            setCPUBankA(data);
        }
            break;
        case 0xC000: {
            setCPUBankC(data);
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
        // Set Interrupts
        irq_enabled = false;
        irq_counter = 0;
    }

    /**
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        // Reset Patches
        patch = 0;
        patch2 = 0;
        System.out.println("CRC: " + crc);
        // Kaiketsu Yanchamaru 3 - Taiketsu! Zouringen
        if (crc == 2659812229l) {
            patch = 1;
        }
        // X 2
        if (crc == 0) {
            patch2 = 1;
        }
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if (irq_enabled) {
            if (patch == 1) {
                if (irq_counter == 0) {
                    irq_enabled = false;
                    return 3;
                } else {
                    irq_counter--;
                }
            } else {
                if (irq_counter <= ((patch2 == 1) ? 111 : 113)) {
                    irq_enabled = false;
                    irq_counter = 0xFFFF;
                    return 3;
                } else {
                    irq_counter -= ((patch2 == 1) ? 111 : 113);
                }
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
    }
}
