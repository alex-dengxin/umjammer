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
 * Class for the Mapper 17 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper017 extends Mapper {
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
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 17;
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
     * Reset the Mapper.
     */
    public final void reset() {
        // Set Program ROM Banks
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set VROM Banks (Standard)
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
        // IRQ Settings
        irq_enabled = false;
        irq_latch = 0;
        irq_counter = 0;
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void accesslow(int addr, int data) {
        switch (addr) {
        case 0x42FE: {
            if ((data & 0x10) == 0) {
                setMirroring(0, 0, 0, 0);
            } else {
                setMirroring(1, 1, 1, 1);
            }
        }
            break;
        case 0x42FF: {
            if ((data & 0x10) == 0) {
                setMirroringVertical();
            } else {
                setMirroringHorizontal();
            }
        }
            break;
        case 0x4501: {
            irq_enabled = false;
        }
            break;
        case 0x4502: {
            irq_latch = (irq_latch & 0xFF00) | data;
        }
            break;
        case 0x4503: {
            irq_latch = (irq_latch & 0x00FF) | (data << 8);
            irq_counter = irq_latch;
            irq_enabled = true;
        }
            break;
        case 0x4504: {
            setCPUBank8(data);
        }
            break;
        case 0x4505: {
            setCPUBankA(data);
        }
            break;
        case 0x4506: {
            setCPUBankC(data);
        }
            break;
        case 0x4507: {
            setCPUBankE(data);
        }
            break;
        case 0x4510: {
            setPPUBank0(data);
        }
            break;
        case 0x4511: {
            setPPUBank1(data);
        }
            break;
        case 0x4512: {
            setPPUBank2(data);
        }
            break;
        case 0x4513: {
            setPPUBank3(data);
        }
            break;
        case 0x4514: {
            setPPUBank4(data);
        }
            break;
        case 0x4515: {
            setPPUBank5(data);
        }
            break;
        case 0x4516: {
            setPPUBank6(data);
        }
            break;
        case 0x4517: {
            setPPUBank7(data);
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
            if (irq_counter >= (0xFFFF - 113)) {
                irq_counter = 0;
                irq_enabled = false;
                return 3;
            } else {
                irq_counter += 113;
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
