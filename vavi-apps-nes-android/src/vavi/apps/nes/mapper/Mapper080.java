/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Class for the Mapper 80 Controller
 * 
 * @author David de Niese
 * @version 0.56f
 */
public class Mapper080 extends Mapper {
    /**
     * Determine the number of the Memory Mapper.
     */
    public final int getMapperNumber() {
        return 80;
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
        // set CPU bank pointers
        setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        // Set the Video ROM
        if (getNum1KVROMBanks() > 0) {
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
        patch = false;
    }

    /**
     * Provide Mapper with CRC32 for Cartridge
     */
    public void setCRC(long crc) {
        // Fudou Myouou Den (J)
        if (crc == 1987637717l) {
            // for Hudoumyouou Den
            patch = true;
            System.out.println("Found Fudou Myouou Den");
        }
    }

    boolean patch = false;

    /**
     * Access the Mapper
     */
    public final void access(int addr, int data) {
        if (addr >= 0x6000 && addr < 0x8000) {
            switch (addr) {
            case 0x7EF0: {
                setPPUBank0(data & 0x7F);
                setPPUBank1((data & 0x7F) + 1);
                if (patch) {
                    if ((data & 0x80) != 0) {
                        setVRAMBank(0x8, 1);
                        setVRAMBank(0x9, 1);
                    } else {
                        setVRAMBank(0x8, 0);
                        setVRAMBank(0x9, 0);
                    }
                }
            }
                break;
            case 0x7EF1: {
                setPPUBank2(data & 0x7F);
                setPPUBank3((data & 0x7F) + 1);
                if (patch) {
                    if ((data & 0x80) != 0) {
                        setVRAMBank(0xA, 1);
                        setVRAMBank(0xB, 1);
                    } else {
                        setVRAMBank(0xA, 0);
                        setVRAMBank(0xB, 0);
                    }
                }
            }
                break;
            case 0x7EF2: {
                setPPUBank4(data);
            }
                break;
            case 0x7EF3: {
                setPPUBank5(data);
            }
                break;
            case 0x7EF4: {
                setPPUBank6(data);
            }
                break;
            case 0x7EF5: {
                setPPUBank7(data);
            }
                break;
            case 0x7EF6: {
                if ((data & 0x01) != 0) {
                    setMirroringVertical();
                } else {
                    setMirroringHorizontal();
                }
            }
                break;
            case 0x7EFA:
            case 0x7EFB: {
                setCPUBank8(data);
            }
                break;
            case 0x7EFC:
            case 0x7EFD: {
                setCPUBankA(data);
            }
                break;
            case 0x7EFE:
            case 0x7EFF: {
                setCPUBankC(data);
            }
                break;
            }
        }
    }
}
