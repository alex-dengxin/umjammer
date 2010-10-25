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
 * Class for the Mapper 105 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper105 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 105;
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
     * Initial Function State
     */
    private int init_state = 0;

    private int[] regs = new int[4];

    int bits = 0;

    int write_count = 0;

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
        setCPUBanks(0, 1, 2, 3);
        regs[0] = 0x0C;
        regs[1] = 0x00;
        regs[2] = 0x00;
        regs[3] = 0x10;
        bits = 0;
        write_count = 0;
        // IRQ Settings
        irq_enabled = false;
        irq_counter = 0;
        init_state = 0;
    }

    /**
     * Access the Mapper in Low Area.
     */
    public final void access(int addr, int data) {
        if (addr < 0x8000)
            return;
        int reg_num = (addr & 0x7FFF) >> 13;
        if ((data & 0x80) != 0) {
            bits = 0;
            write_count = 0;
            if (reg_num == 0) {
                regs[reg_num] |= 0x0C;
            }
        } else {
            bits |= (data & 1) << write_count++;
            if (write_count == 5) {
                regs[reg_num] = bits & 0x1F;
                bits = write_count = 0;
            }
        }
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
        switch (init_state) {
        case 0:
        case 1: {
            init_state++;
        }
            break;
        case 2: {
            if ((regs[1] & 0x08) != 0) {
                if ((regs[0] & 0x08) != 0) {
                    if ((regs[0] & 0x04) != 0) {
                        setCPUBank8((regs[3] & 0x07) * 2 + 16);
                        setCPUBankA((regs[3] & 0x07) * 2 + 17);
                        setCPUBankC(30);
                        setCPUBankE(31);
                    } else {
                        setCPUBank8(16);
                        setCPUBankA(17);
                        setCPUBankC((regs[3] & 0x07) * 2 + 16);
                        setCPUBankE((regs[3] & 0x07) * 2 + 17);
                    }
                } else {
                    setCPUBank8((regs[3] & 0x06) * 2 + 16);
                    setCPUBankA((regs[3] & 0x06) * 2 + 17);
                    setCPUBankC((regs[3] & 0x06) * 2 + 18);
                    setCPUBankE((regs[3] & 0x06) * 2 + 19);
                }
            } else {
                setCPUBank8((regs[1] & 0x06) * 2 + 0);
                setCPUBankA((regs[1] & 0x06) * 2 + 1);
                setCPUBankC((regs[1] & 0x06) * 2 + 2);
                setCPUBankE((regs[1] & 0x06) * 2 + 3);
            }
            if ((regs[1] & 0x10) != 0) {
                irq_counter = 0;
                irq_enabled = false;
            } else {
                irq_enabled = true;
            }
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
        if (scanline == 0) {
            if (irq_enabled) {
                irq_counter += 29781;
            }
            if (((irq_counter | 0x21FFFFFF) & 0x3E000000) == 0x3E000000) {
                return 3;
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
        init_state = (input.read() << 8) + input.read();
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Registers
        output.write((irq_counter & 0xFF00) >> 8);
        output.write(irq_counter & 0xFF);
        output.write(irq_enabled ? 0xFF : 0x00);
        output.write((init_state & 0xFF00) >> 8);
        output.write(init_state & 0xFF);
    }
}
