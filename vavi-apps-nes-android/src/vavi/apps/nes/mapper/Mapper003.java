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
 * Class for the Mapper 003 Controller used by NESCafe.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper003 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 3;
    }

    /**
     * Records Last Byte Written
     */
    int lastByteWritten = 0;

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
    public final void access(int address, int value) {
        // Check Mapper Write is Within Range
        if (address < 0x8000)
            return;
        // Record Last Write
        lastByteWritten = value;
        // Wrap PPU Banks
        int max = (getNum1KVROMBanks() >> 1) - 1;
        int base = (value & max) << 3;
        // Set PPU Banks
        setPPUBanks(base + 0, base + 1, base + 2, base + 3, base + 4, base + 5, base + 6, base + 7);
    }

    /**
     * Reset the Mapper.
     */
    public final void reset() {
        // Set Program ROM Banks
        if (getNum8KRomBanks() > 2) {
            setCPUBanks(0, 1, 2, 3);
        } else {
            setCPUBanks(0, 1, 0, 1);
        }
        // Set VROM Banks
        setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
    }

    /**
     * Loads the State of the Memory Mapper from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        lastByteWritten = input.read() & 0xFF;
        access(0x8000, lastByteWritten);
    }

    /**
     * Saves the State of the Memory Mapper to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        output.write(lastByteWritten & 0xFF);
    }
}
