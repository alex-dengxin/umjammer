/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.mapper;

import vavi.apps.nes.Mapper;
import vavi.apps.nes.MemoryManager;


/**
 * Mapper Handlers Handles Collection of Simple Memory Mappers
 * 
 * @author : David de Niese
 * @version : 0.56
 */
public class MapperCollection extends Mapper {
    private int mapperNumber = 0;

    public int getMapperNumber() {
        return mapperNumber;
    }

    public MapperCollection(int mapperNumber) {
        System.out.println("Using MapperCollection (" + mapperNumber + ")");
        switch (mapperNumber) {
        case 2:
        case 7:
        case 8:
        case 11:
        case 34:
        case 58:
        case 60:
        case 62:
        case 66:
        case 71:
        case 72:
        case 77:
        case 78:
        case 79:
        case 86:
        case 87:
        case 89:
        case 92:
        case 93:
        case 94:
        case 97:
        case 99:
        case 101:
        case 140:
        case 151:
        case 180:
        case 181:
        case 184:
        case 185:
        case 222:
        case 229:
        case 231:
        case 232:
        case 233:
        case 240:
        case 242:
        case 244:
        case 246:
            break;
        default:
            System.out.println("FATAL: Incorrect Mapper for MapperHandler " + mapperNumber);
            break;
        }
        this.mapperNumber = mapperNumber;
    }

    public final void accesslow(int addr, int data) {
        switch (mapperNumber) {
        case 79: {
            if ((addr & 0x0100) != 0) {
                int prg_bank = (data & 0x08) >> 3;
                int chr_bank = data & 0x07;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        case 99: {
            if (addr == 0x4016) {
                if ((data & 0x04) != 0) {
                    setPPUBanks(8, 9, 10, 11, 12, 13, 14, 15);
                } else {
                    setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
                }
            }
        }
            break;
        case 181: {
            if (addr == 0x4120) {
                int prg_bank = (data & 0x08) >> 3;
                int chr_bank = data & 0x07;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        case 240: {
            if (addr >= 0x4100 && addr <= 0x4FFF) {
                int prg_bank = data >> 4;
                int chr_bank = data & 0x0F;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        }
    }

    public void access(int addr, int data) {
        switch (mapperNumber) {
        case 2: {
            if (addr < 0x8000)
                return;
            int romBankCount = getNum8KRomBanks();
            data &= romBankCount - 1;
            setCPUBanks(data * 2, (data * 2) + 1, romBankCount - 2, romBankCount - 1);
        }
            break;
        case 7: {
            if (addr < 0x8000)
                return;
            int bank = (data & 0x07) << 2;
            setCPUBanks(bank + 0, bank + 1, bank + 2, bank + 3);
            if ((data & 0x10) != 0) {
                setMirroringOneScreenHigh();
            } else {
                setMirroringOneScreenLow();
            }
        }
            break;
        case 8: {
            if (addr < 0x8000)
                return;
            int prg_bank = (data & 0xF8) >> 3;
            int chr_bank = data & 0x07;
            setCPUBank8(prg_bank * 2 + 0);
            setCPUBankA(prg_bank * 2 + 1);
            setPPUBank0(chr_bank * 8 + 0);
            setPPUBank1(chr_bank * 8 + 1);
            setPPUBank2(chr_bank * 8 + 2);
            setPPUBank3(chr_bank * 8 + 3);
            setPPUBank4(chr_bank * 8 + 4);
            setPPUBank5(chr_bank * 8 + 5);
            setPPUBank6(chr_bank * 8 + 6);
            setPPUBank7(chr_bank * 8 + 7);
        }
            break;
        case 11: {
            // Check Range
            if (addr < 0x8000)
                return;
            // Determine Program RAM Bank
            int prgBank = data & 0x01;
            int chrBank = (data & 0x70) >> 4;
            // Set Program ROM Banks
            setCPUBank8(prgBank * 4 + 0);
            setCPUBankA(prgBank * 4 + 1);
            setCPUBankC(prgBank * 4 + 2);
            setCPUBankE(prgBank * 4 + 3);
            // Set Program ROM Banks
            setPPUBank0(chrBank * 8 + 0);
            setPPUBank1(chrBank * 8 + 1);
            setPPUBank2(chrBank * 8 + 2);
            setPPUBank3(chrBank * 8 + 3);
            setPPUBank4(chrBank * 8 + 4);
            setPPUBank5(chrBank * 8 + 5);
            setPPUBank6(chrBank * 8 + 6);
            setPPUBank7(chrBank * 8 + 7);
        }
            break;
        case 34: {
            if (addr < 0x8000) {
                switch (addr) {
                case 0x7FFD: {
                    setCPUBanks(data * 4, data * 4 + 1, data * 4 + 2, data * 4 + 3);
                }
                    break;
                case 0x7FFE: {
                    setPPUBank0(data * 4 + 0);
                    setPPUBank1(data * 4 + 1);
                    setPPUBank2(data * 4 + 2);
                    setPPUBank3(data * 4 + 3);
                }
                    break;
                case 0x7FFF: {
                    setPPUBank4(data * 4 + 0);
                    setPPUBank5(data * 4 + 1);
                    setPPUBank6(data * 4 + 2);
                    setPPUBank7(data * 4 + 3);
                }
                    break;
                }
                return;
            } else {
                setCPUBanks(data * 4, data * 4 + 1, data * 4 + 2, data * 4 + 3);
            }
        }
            break;
        case 58: {
            if (addr < 0x8000)
                return;
            if ((addr & 0x40) != 0) {
                setCPUBank8(2 * (addr & 0x07) + 0);
                setCPUBankA(2 * (addr & 0x07) + 1);
                setCPUBankC(2 * (addr & 0x07) + 0);
                setCPUBankE(2 * (addr & 0x07) + 1);
            } else {
                setCPUBank8(4 * ((addr & 0x06) >> 1) + 0);
                setCPUBankA(4 * ((addr & 0x06) >> 1) + 1);
                setCPUBankC(4 * ((addr & 0x06) >> 1) + 2);
                setCPUBankE(4 * ((addr & 0x06) >> 1) + 3);
            }
            setPPUBank0(8 * ((addr & 0x38) >> 3) + 0);
            setPPUBank1(8 * ((addr & 0x38) >> 3) + 1);
            setPPUBank2(8 * ((addr & 0x38) >> 3) + 2);
            setPPUBank3(8 * ((addr & 0x38) >> 3) + 3);
            setPPUBank4(8 * ((addr & 0x38) >> 3) + 4);
            setPPUBank5(8 * ((addr & 0x38) >> 3) + 5);
            setPPUBank6(8 * ((addr & 0x38) >> 3) + 6);
            setPPUBank7(8 * ((addr & 0x38) >> 3) + 7);
            if ((data & 0x02) != 0) {
                setMirroringVertical();
            } else {
                setMirroringHorizontal();
            }
        }
            break;
        case 60: {
            if (addr < 0x8000)
                return;
            if ((addr & 0x80) != 0) {
                setCPUBank8(2 * ((addr & 0x70) >> 4) + 0);
                setCPUBankA(2 * ((addr & 0x70) >> 4) + 1);
                setCPUBankC(2 * ((addr & 0x70) >> 4) + 0);
                setCPUBankE(2 * ((addr & 0x70) >> 4) + 1);
            } else {
                setCPUBank8(4 * ((addr & 0x70) >> 5) + 0);
                setCPUBankA(4 * ((addr & 0x70) >> 5) + 1);
                setCPUBankC(4 * ((addr & 0x70) >> 5) + 2);
                setCPUBankE(4 * ((addr & 0x70) >> 5) + 3);
            }
            setPPUBank0(8 * (addr & 0x07) + 0);
            setPPUBank1(8 * (addr & 0x07) + 1);
            setPPUBank2(8 * (addr & 0x07) + 2);
            setPPUBank3(8 * (addr & 0x07) + 3);
            setPPUBank4(8 * (addr & 0x07) + 4);
            setPPUBank5(8 * (addr & 0x07) + 5);
            setPPUBank6(8 * (addr & 0x07) + 6);
            setPPUBank7(8 * (addr & 0x07) + 7);
            if ((data & 0x08) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
            break;
        case 62: {
            if (addr < 0x8000)
                return;
            switch (addr & 0xFF00) {
            case 0x8100:
                setCPUBank8(data);
                setCPUBankA(data + 1);
                break;
            case 0x8500:
                setCPUBank8(data);
                break;
            case 0x8700:
                setCPUBankA(data);
                break;
            default:
                setPPUBank0(data);
                setPPUBank1(data + 1);
                setPPUBank2(data + 2);
                setPPUBank3(data + 3);
                setPPUBank4(data + 4);
                setPPUBank5(data + 5);
                setPPUBank6(data + 6);
                setPPUBank7(data + 7);
                break;
            }
        }
            break;
        case 66: {
            if (addr >= 0x6000) {
                int chr_bank = data & 0x0F;
                int prg_bank = (data & 0xF0) >> 4;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        case 71: {
            if (addr >= 0x6000 && addr < 0x8000) {
                setCPUBank8(data * 2 + 0);
                setCPUBankA(data * 2 + 1);
            }
            if (addr >= 0x8000) {
                switch (addr & 0xF000) {
                case 0x9000: {
                    if ((data & 0x10) != 0) {
                        setMirroring(1, 1, 1, 1);
                    } else {
                        setMirroring(0, 0, 0, 0);
                    }
                }
                    break;
                case 0xC000:
                case 0xD000:
                case 0xE000:
                case 0xF000: {
                    setCPUBank8(data * 2 + 0);
                    setCPUBankA(data * 2 + 1);
                }
                    break;
                }
            }
        }
            break;
        case 72: {
            if (addr < 0x8000)
                return;
            int bank = data & 0x0f;
            if ((data & 0x80) != 0) {
                setCPUBanks(bank * 2, bank * 2 + 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
            }
            if ((data & 0x40) != 0) {
                setPPUBanks(bank * 8, bank * 8 + 1, bank * 8 + 2, bank * 8 + 3, bank * 8 + 4, bank * 8 + 5, bank * 8 + 6, bank * 8 + 7);
            }
        }
            break;
        case 77: {
            if (addr < 0x8000)
                return;
            int prg_bank = data & 0x07;
            int chr_bank = (data & 0xF0) >> 4;
            setCPUBank8(prg_bank * 4 + 0);
            setCPUBankA(prg_bank * 4 + 1);
            setCPUBankC(prg_bank * 4 + 2);
            setCPUBankE(prg_bank * 4 + 3);
            setPPUBank0(chr_bank * 2 + 0);
            setPPUBank1(chr_bank * 2 + 1);
        }
            break;
        case 78: {
            if (addr < 0x8000)
                return;
            int prg_bank = data & 0x0F;
            int chr_bank = (data & 0xF0) >> 4;
            setCPUBank8(prg_bank * 2 + 0);
            setCPUBankA(prg_bank * 2 + 1);
            setPPUBank0(chr_bank * 8 + 0);
            setPPUBank1(chr_bank * 8 + 1);
            setPPUBank2(chr_bank * 8 + 2);
            setPPUBank3(chr_bank * 8 + 3);
            setPPUBank4(chr_bank * 8 + 4);
            setPPUBank5(chr_bank * 8 + 5);
            setPPUBank6(chr_bank * 8 + 6);
            setPPUBank7(chr_bank * 8 + 7);
            if ((addr & 0xFE00) != 0xFE00) {
                if ((data & 0x08) != 0) {
                    setMirroring(1, 1, 1, 1);
                } else {
                    setMirroring(0, 0, 0, 0);
                }
            }
        }
            break;
        case 86: {
            if (addr == 0x6000) {
                int chr_bank = data & 0x03 | (data & 0x40) >> 4;
                int prg_bank = (data & 0x30) >> 4;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        case 87: {
            if (addr == 0x6000) {
                int chr_bank = (data & 0x02) >> 1;
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        case 89: {
            if (addr < 0x8000)
                return;
            if ((addr & 0xFF00) == 0xC000) {
                int prg_bank = (data & 0x70) >> 4;
                int chr_bank = ((data & 0x80) >> 4) | (data & 0x07);
                setCPUBank8(prg_bank * 2 + 0);
                setCPUBankA(prg_bank * 2 + 1);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
                if ((data & 0x08) != 0) {
                    setMirroring(1, 1, 1, 1);
                } else {
                    setMirroring(0, 0, 0, 0);
                }
            }
        }
            break;
        case 92: {
            if (addr < 0x8000)
                return;
            data = addr & 0xff;
            int c_bank = (data & 0x0f) << 1;
            int p_bank = data & 0x0f;
            if (addr >= 0x9000) {
                // Moero!! ProSoccer
                if ((data & 0xf0) == 0xd0) {
                    setCPUBanks(0, 1, c_bank, c_bank + 1);
                } else if ((data & 0xf0) == 0xe0) {
                    setPPUBanks(p_bank * 8, p_bank * 8 + 1, p_bank * 8 + 2, p_bank * 8 + 3, p_bank * 8 + 4, p_bank * 8 + 5, p_bank * 8 + 6, p_bank * 8 + 7);
                }
            } else {
                // Moero!! Proyakyuu '88 ketteiban
                if ((data & 0xf0) == 0xb0) {
                    setCPUBanks(0, 1, c_bank, c_bank + 1);
                } else if ((data & 0xf0) == 0x70) {
                    setPPUBanks(p_bank * 8, p_bank * 8 + 1, p_bank * 8 + 2, p_bank * 8 + 3, p_bank * 8 + 4, p_bank * 8 + 5, p_bank * 8 + 6, p_bank * 8 + 7);
                }
            }
        }
            break;
        case 93: {
            if (addr == 0x6000) {
                setCPUBank8(data * 2 + 0);
                setCPUBankA(data * 2 + 1);
            }
        }
            break;
        case 94: {
            if ((addr & 0xFFF0) == 0xFF00) {
                int bank = (data & 0x1C) >> 2;
                setCPUBank8(bank * 2 + 0);
                setCPUBankA(bank * 2 + 1);
            }
        }
            break;
        case 97: {
            if (addr >= 0x8000 && addr < 0xC000) {
                int prg_bank = data & 0x0F;
                setCPUBankC(prg_bank * 2 + 0);
                setCPUBankE(prg_bank * 2 + 1);
                if ((data & 0x80) == 0) {
                    setMirroringHorizontal();
                } else {
                    setMirroringVertical();
                }
            }
        }
            break;
        case 101: {
            if (addr >= 0x6000 && addr < 0x8000) {
                data &= 0x03;
                setPPUBank0(data * 8 + 0);
                setPPUBank1(data * 8 + 1);
                setPPUBank2(data * 8 + 2);
                setPPUBank3(data * 8 + 3);
                setPPUBank4(data * 8 + 4);
                setPPUBank5(data * 8 + 5);
                setPPUBank6(data * 8 + 6);
                setPPUBank7(data * 8 + 7);
            }
            if (addr >= 0x8000) {
                data &= 0x03;
                setPPUBank0(data * 8 + 0);
                setPPUBank1(data * 8 + 1);
                setPPUBank2(data * 8 + 2);
                setPPUBank3(data * 8 + 3);
                setPPUBank4(data * 8 + 4);
                setPPUBank5(data * 8 + 5);
                setPPUBank6(data * 8 + 6);
                setPPUBank7(data * 8 + 7);
            }
        }
            break;
        case 140: {
            if (addr >= 0x6000 && addr < 0x8000) {
                int prg_bank = (data & 0xF0) >> 4;
                int chr_bank = data & 0x0F;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            }
        }
            break;
        case 151: {
            if (addr < 0x8000)
                return;
            switch (addr & 0xF000) {
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
            case 0xE000: {
                setPPUBank0(data * 4 + 0);
                setPPUBank1(data * 4 + 1);
                setPPUBank2(data * 4 + 2);
                setPPUBank3(data * 4 + 3);
            }
                break;
            case 0xF000: {
                setPPUBank4(data * 4 + 0);
                setPPUBank5(data * 4 + 1);
                setPPUBank6(data * 4 + 2);
                setPPUBank7(data * 4 + 3);
            }
                break;
            }
        }
            break;
        case 180: {
            setCPUBankC((data & 0x07) * 2 + 0);
            setCPUBankE((data & 0x07) * 2 + 1);
        }
            break;
        case 184: {
            // Save RAM Writes only
            if (addr >= 0x6000 && addr < 0x8000) {
                int h = (data & 0x20) >> 2;
                int l = ((data & 2) << 2) | (data & 4);
                setPPUBanks(l, l + 1, l + 2, l + 3, h, h + 1, h + 2, h + 3);
            }
        }
            break;
        case 185: {
            if (addr < 0x8000)
                return;
            if ((patch185 == 0 && ((data & 0x03) != 0)) || (patch185 == 1 && data == 0x21)) {
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
            } else {
                for (int i = 0; i < 8192; i++) {
                    mm.nes.ppu.ppuMemory[i] = 0xFF;
                }
            }
        }
            break;
        case 222: {
            if (addr < 0x8000)
                return;
            switch (addr & 0xf003) {
            case 0x8000:
                setCPUBank8(data);
                break;
            case 0xA000:
                setCPUBankA(data);
                break;
            case 0xB000:
                setPPUBank0(data);
                break;
            case 0xB002:
                setPPUBank1(data);
                break;
            case 0xC000:
                setPPUBank2(data);
                break;
            case 0xC002:
                setPPUBank3(data);
                break;
            case 0xD000:
                setPPUBank4(data);
                break;
            case 0xD002:
                setPPUBank5(data);
                break;
            case 0xE000:
                setPPUBank6(data);
                break;
            case 0xE002:
                setPPUBank7(data);
                break;
            }
        }
            break;
        case 229: {
            if (addr < 0x8000)
                return;
            addr &= 0x0FFF;
            if ((addr & 0x0020) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
            if ((addr & 0x001E) != 0) {
                int prg_bank = addr & 0x001F;
                int chr_bank = addr & 0x0FFF;
                setCPUBank8(prg_bank * 2 + 0);
                setCPUBankA(prg_bank * 2 + 1);
                setCPUBankC(prg_bank * 2 + 0);
                setCPUBankE(prg_bank * 2 + 1);
                setPPUBank0(chr_bank * 8 + 0);
                setPPUBank1(chr_bank * 8 + 1);
                setPPUBank2(chr_bank * 8 + 2);
                setPPUBank3(chr_bank * 8 + 3);
                setPPUBank4(chr_bank * 8 + 4);
                setPPUBank5(chr_bank * 8 + 5);
                setPPUBank6(chr_bank * 8 + 6);
                setPPUBank7(chr_bank * 8 + 7);
            } else {
                setCPUBanks(0, 1, 2, 3);
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
            }
        }
            break;
        case 231: {
            if (addr < 0x8000)
                return;
            if ((addr & 0x0020) != 0) {
                int prg_bank = (addr >> 1) & 0x0F;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
            } else {
                int prg_bank = addr & 0x001E;
                setCPUBank8(prg_bank * 2 + 0);
                setCPUBankA(prg_bank * 2 + 1);
                setCPUBankC(prg_bank * 2 + 0);
                setCPUBankE(prg_bank * 2 + 1);
            }
            if ((addr & 0x0080) != 0) {
                setMirroringHorizontal();
            } else {
                setMirroringVertical();
            }
        }
            break;
        case 232: {
            if (addr < 0x8000)
                return;
            if (addr == 0x9000) {
                regs232[0] = (data & 0x18) >> 1;
            } else if (0xA000 <= addr && addr <= 0xFFFF) {
                regs232[1] = data & 0x03;
            }
            setCPUBank8((regs232[0] | regs232[1]) * 2 + 0);
            setCPUBankA((regs232[0] | regs232[1]) * 2 + 1);
            setCPUBankC((regs232[0] | 0x03) * 2 + 0);
            setCPUBankE((regs232[0] | 0x03) * 2 + 1);
        }
            break;
        case 233: {
            if (addr < 0x8000)
                return;
            if ((data & 0x20) != 0) {
                int prg_bank = data & 0x1F;
                setCPUBank8(prg_bank * 2 + 0);
                setCPUBankA(prg_bank * 2 + 1);
                setCPUBankC(prg_bank * 2 + 0);
                setCPUBankE(prg_bank * 2 + 1);
            } else {
                int prg_bank = (data & 0x1E) >> 1;
                setCPUBank8(prg_bank * 4 + 0);
                setCPUBankA(prg_bank * 4 + 1);
                setCPUBankC(prg_bank * 4 + 2);
                setCPUBankE(prg_bank * 4 + 3);
            }
            if ((data & 0xC0) == 0x00) {
                setMirroring(0, 0, 0, 1);
            } else if ((data & 0xC0) == 0x40) {
                setMirroringVertical();
            } else if ((data & 0xC0) == 0x80) {
                setMirroringHorizontal();
            } else {
                setMirroring(1, 1, 1, 1);
            }
        }
            break;
        case 242: {
            if ((addr & 0x0001) != 0) {
                setCPUBank8(((addr & 0x78) >> 1) + 0);
                setCPUBankA(((addr & 0x78) >> 1) + 1);
                setCPUBankC(((addr & 0x78) >> 1) + 2);
                setCPUBankE(((addr & 0x78) >> 1) + 3);
            }
        }
            break;
        case 244: {
            if (addr < 0x8000)
                return;
            if (data < 4) {
                data <<= 2;
                setCPUBanks(data, data + 1, data + 2, data + 3);
            } else if (data >= 8 && data <= 0x0f) {
                data -= 8;
                data <<= 3;
                setPPUBanks(data, data + 1, data + 2, data + 3, data + 4, data + 5, data + 6, data + 7);
            }
        }
            break;
        case 246: {
            if (addr >= 0x6000 & addr < 0x8000) {
                switch (addr) {
                case 0x6000: {
                    setCPUBank8(data);
                }
                    break;
                case 0x6001: {
                    setCPUBankA(data);
                }
                    break;
                case 0x6002: {
                    setCPUBankC(data);
                }
                    break;
                case 0x6003: {
                    setCPUBankE(data);
                }
                    break;
                case 0x6004: {
                    setPPUBank0(data * 2 + 0);
                    setPPUBank1(data * 2 + 1);
                }
                    break;
                case 0x6005: {
                    setPPUBank2(data * 2 + 0);
                    setPPUBank3(data * 2 + 1);
                }
                    break;
                case 0x6006: {
                    setPPUBank4(data * 2 + 0);
                    setPPUBank5(data * 2 + 1);
                }
                    break;
                case 0x6007: {
                    setPPUBank6(data * 2 + 0);
                    setPPUBank7(data * 2 + 1);
                }
                    break;
                }
            }
        }
            break;
        }
    }

    /**
     * Reset the Memory Mapper.
     */
    public void reset() {
        switch (mapperNumber) {
        case 7:
        case 185:
        case 233: {
            setCPUBanks(0, 1, 2, 3);
        }
            break;
        case 58: {
            setCPUBanks(0, 1, 0, 1);
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 66: {
            setCPUBanks(0, 1, 2, 3);
            setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 71:
        case 246: {
            setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
        }
            break;
        case 2:
        case 34:
        case 72:
        case 78:
        case 89:
        case 93:
        case 94:
        case 151:
        case 184:
        case 232:
        case 240: {
            setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
            if (getNum1KVROMBanks() > 0)
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 92:
        case 222: {
            setCPUBanks(0, 1, getNum8KRomBanks() - 2, getNum8KRomBanks() - 1);
            if (getNum1KVROMBanks() >= 8)
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 97: {
            setCPUBanks(getNum8KRomBanks() - 2, getNum8KRomBanks() - 1, 0, 1);
            if (getNum1KVROMBanks() > 0)
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 99: {
            if (getNum8KRomBanks() > 0) {
                setCPUBanks(0, 1, 2, 3);
            } else if (getNum8KRomBanks() > 1) {
                setCPUBanks(0, 1, 0, 1);
            } else {
                setCPUBanks(0, 0, 0, 0);
            }
            if (getNum1KVROMBanks() > 0)
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 8:
        case 11:
        case 77:
        case 79:
        case 86:
        case 87:
        case 101:
        case 140:
        case 180:
        case 181:
        case 231:
        case 242:
        case 244: {
            setCPUBanks(0, 1, 2, 3);
            if (getNum1KVROMBanks() > 0)
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
        }
            break;
        case 62:
        case 229: {
            setCPUBanks(0, 1, 2, 3);
            if (getNum1KVROMBanks() >= 8) {
                setPPUBanks(0, 1, 2, 3, 4, 5, 6, 7);
            }
        }
            break;
        default: {
            System.out.println("FATAL: No Reset Routine for Mapper " + mapperNumber);
            System.exit(0);
        }
        }
        // Special Processing
        switch (mapperNumber) {
        case 11: {
            setMirroringVertical();
        }
            break;
        case 222: {
            setMirroringHorizontal();
        }
            break;
        case 232: {
            regs232[0] = 0x0C;
            regs232[1] = 0x00;
        }
            break;
        }
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

    int patch185 = 0;

    int[] regs232 = new int[2];

    public void setCRC(long crc) {
        switch (mapperNumber) {
        case 66: {
            if (crc == 292343108l) {
                System.out.println("Paris-Dakar Rally Special CRC Found :" + crc);
                mm.nes.frameIRQDisenabled = 1;
            }
        }
            break;
        case 185: {
            if (crc == 3473710487l) {
                // Spy v Spy J
                patch185 = 1;
                System.out.println("Spy V Spy J");
            }
        }
            break;
        }
    }
}
