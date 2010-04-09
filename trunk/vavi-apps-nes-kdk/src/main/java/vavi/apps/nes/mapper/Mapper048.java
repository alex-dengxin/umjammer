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
 * Class for the Mapper 048 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper048 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 48;
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
     * Register to Store Function
     */
    private int regs[] = new int[1];

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
        case 0x8000: {
            if (regs[0] == 0) {
                if ((data & 0x40) != 0) {
                    setMirroringHorizontal();
                } else {
                    setMirroringVertical();
                }
            }
            setCPUBank8(data);
        }
            break;
        case 0x8001: {
            setCPUBankA(data);
        }
            break;
        case 0x8002: {
            setPPUBank0(data * 2 + 0);
            setPPUBank1(data * 2 + 1);
        }
            break;
        case 0x8003: {
            setPPUBank2(data * 2 + 0);
            setPPUBank3(data * 2 + 1);
        }
            break;
        case 0xA000: {
            setPPUBank4(data);
        }
            break;
        case 0xA001: {
            setPPUBank5(data);
        }
            break;
        case 0xA002: {
            setPPUBank6(data);
        }
            break;
        case 0xA003: {
            setPPUBank7(data);
        }
            break;
        case 0xC000: {
            irq_counter = data;
        }
            break;
        case 0xC001: {
            irq_enabled = (data & 0x01) != 0;
        }
            break;
        case 0xE000: {
            if ((data & 0x40) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
            regs[0] = 1;
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
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        // Set Interrupts
        irq_enabled = false;
        irq_counter = 0;
        regs[0] = 0;
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
                    if (irq_counter == 0xFF) {
                        irq_enabled = false;
                        return 3;
                    } else {
                        irq_counter++;
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
