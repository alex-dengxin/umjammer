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
 * Class for the Mapper 67 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper067 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 67;
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
     * Whether the IRQ has been Set
     */
    private int irq_latch = 0;

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set CPU Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set PPU Banks
        setPPUBank0(0);
        setPPUBank1(1);
        setPPUBank2(2);
        setPPUBank3(3);
        setPPUBank4(getNum8KRomBanks() - 4);
        setPPUBank5(getNum8KRomBanks() - 3);
        setPPUBank6(getNum8KRomBanks() - 2);
        setPPUBank7(getNum8KRomBanks() - 1);
        // Set Interrupts
        irq_enabled = false;
        irq_counter = 0;
        irq_latch = 0;
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
        // Check Range
        if (addr < 0x8000)
            return;
        // Perform Function
        switch (addr & 0xF800) {
        case 0x8800: {
            setPPUBank0(data * 2 + 0);
            setPPUBank1(data * 2 + 1);
        }
            break;
        case 0x9800: {
            setPPUBank2(data * 2 + 0);
            setPPUBank3(data * 2 + 1);
        }
            break;
        case 0xA800: {
            setPPUBank4(data * 2 + 0);
            setPPUBank5(data * 2 + 1);
        }
            break;
        case 0xB800: {
            setPPUBank6(data * 2 + 0);
            setPPUBank7(data * 2 + 1);
        }
            break;
        case 0xC800: {
            irq_counter = irq_latch;
            irq_latch = data;
        }
            break;
        case 0xD800: {
            irq_enabled = (data & 0x10) != 0;
        }
            break;
        case 0xE800: {
            data &= 0x03;
            if (data == 0) {
                setMirroringVertical();
            } else if (data == 1) {
                setMirroringHorizontal();
            } else if (data == 2) {
                setMirroring(0, 0, 0, 0);
            } else {
                setMirroring(1, 1, 1, 1);
            }
        }
            break;
        case 0xF800: {
            setCPUBank8(data * 2 + 0);
            setCPUBankA(data * 2 + 1);
        }
            break;
        }
    }

    /**
     * Syncronise the Memory Mapper Horizontally.
     * 
     * @return Returns non-zero if a Mapper-specific interrupt occurred.
     */
    public int syncH(int scanline) {
        if (irq_enabled) {
            if ((scanline >= 0) && (scanline <= 239)) {
                if ((mm.nes.ppu.REG_2001 & 0x18) != 00) {
                    if (--irq_counter == 0xF6) {
                        irq_counter = irq_latch;
                        return 3;
                    }
                    irq_counter &= 0xFF;
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
        irq_enabled = (input.read() == 0xFF);
        irq_latch = (input.read() << 8) + input.read();
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Registers
        output.write((irq_counter & 0xFF00) >> 8);
        output.write(irq_counter & 0xFF);
        output.write(irq_enabled ? 0xFF : 0x00);
        output.write((irq_latch & 0xFF00) >> 8);
        output.write(irq_latch & 0xFF);
    }
}
