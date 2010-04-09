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
 * Class for the Mapper 91 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper091 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 91;
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
        setCPUBanks(getNum8KRomBanks() - 2, getNum8KRomBanks() - 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // IRQ Settings
        irq_enabled = false;
        irq_counter = 0;
        // Set VROM Banks (Standard)
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        switch (addr & 0xF007) {
        case 0x6000: {
            setPPUBank0(data * 2 + 0);
            setPPUBank1(data * 2 + 1);
        }
            break;
        case 0x6001: {
            setPPUBank2(data * 2 + 0);
            setPPUBank3(data * 2 + 1);
        }
            break;
        case 0x6002: {
            setPPUBank4(data * 2 + 0);
            setPPUBank5(data * 2 + 1);
        }
            break;
        case 0x6003: {
            setPPUBank6(data * 2 + 0);
            setPPUBank7(data * 2 + 1);
        }
            break;
        case 0x7000: {
            System.out.println("7000, date:" + data);
            setCPUBank8(data);
        }
            break;
        case 0x7001: {
            System.out.println("7001, date:" + data);
            setCPUBankA(data);
        }
            break;
        case 0x7002: {
            System.out.println("7002, date:" + data);
            irq_counter = data;
        }
            break;
        case 0x7003: {
            System.out.println("7003, date:" + data);
            irq_enabled = (data != 0);
        }
            break;
        case 0x7006:
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
            if (0 <= scanline && scanline <= 240) {
                if ((mm.nes.ppu.REG_2001 & 0x18) != 0) {
                    if (irq_counter-- == 0) {
                        System.out.println("Trigger");
                        irq_enabled = false;
                        return 3;
                    }
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
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Registers
        output.write((irq_counter & 0xFF00) >> 8);
        output.write(irq_counter & 0xFF);
        output.write(irq_enabled ? 0xFF : 0x00);
    }
}
