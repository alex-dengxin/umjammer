/*
 * AppleIIGo
 * Disk II Emulator
 * (C) 2006 by Marc S. Ressl(ressl@lonetree.com)
 * Released under the GPL
 * Based on work by Doug Kwan
 */
 
package vavi.apps.appleii;


public class DiskII extends Peripheral {
	// ROM (with boot wait cycle optimization)
	private static final int[] rom = {
        0xa2, 0x20, 0xa0, 0x00, 0xa2, 0x03, 0x86, 0x3c, 0x8a, 0x0a, 0x24, 0x3c, 0xf0, 0x10, 0x05, 0x3c,
        0x49, 0xff, 0x29, 0x7e, 0xb0, 0x08, 0x4a, 0xd0, 0xfb, 0x98, 0x9d, 0x56, 0x03, 0xc8, 0xe8, 0x10,
		0xe5, 0x20, 0x58, 0xff, 0xba, 0xbd, 0x00, 0x01, 0x0a, 0x0a, 0x0a, 0x0a, 0x85, 0x2b, 0xaa, 0xbd,
		0x8e, 0xc0, 0xbd, 0x8c, 0xc0, 0xbd, 0x8a, 0xc0, 0xbd, 0x89, 0xc0, 0xa0, 0x50, 0xbd, 0x80, 0xc0,
		0x98, 0x29, 0x03, 0x0a, 0x05, 0x2b, 0xaa, 0xbd, 0x81, 0xc0, 0xa9, 0x56, 0xa9, 0x00, 0xea, 0x88,
		0x10, 0xeb, 0x85, 0x26, 0x85, 0x3d, 0x85, 0x41, 0xa9, 0x08, 0x85, 0x27, 0x18, 0x08, 0xbd, 0x8c,
		0xc0, 0x10, 0xfb, 0x49, 0xd5, 0xd0, 0xf7, 0xbd, 0x8c, 0xc0, 0x10, 0xfb, 0xc9, 0xaa, 0xd0, 0xf3,
		0xea, 0xbd, 0x8c, 0xc0, 0x10, 0xfb, 0xc9, 0x96, 0xf0, 0x09, 0x28, 0x90, 0xdf, 0x49, 0xad, 0xf0,
		0x25, 0xd0, 0xd9, 0xa0, 0x03, 0x85, 0x40, 0xbd, 0x8c, 0xc0, 0x10, 0xfb, 0x2a, 0x85, 0x3c, 0xbd,
		0x8c, 0xc0, 0x10, 0xfb, 0x25, 0x3c, 0x88, 0xd0, 0xec, 0x28, 0xc5, 0x3d, 0xd0, 0xbe, 0xa5, 0x40,
		0xc5, 0x41, 0xd0, 0xb8, 0xb0, 0xb7, 0xa0, 0x56, 0x84, 0x3c, 0xbc, 0x8c, 0xc0, 0x10, 0xfb, 0x59,
		0xd6, 0x02, 0xa4, 0x3c, 0x88, 0x99, 0x00, 0x03, 0xd0, 0xee, 0x84, 0x3c, 0xbc, 0x8c, 0xc0, 0x10,
		0xfb, 0x59, 0xd6, 0x02, 0xa4, 0x3c, 0x91, 0x26, 0xc8, 0xd0, 0xef, 0xbc, 0x8c, 0xc0, 0x10, 0xfb,
		0x59, 0xd6, 0x02, 0xd0, 0x87, 0xa0, 0x00, 0xa2, 0x56, 0xca, 0x30, 0xfb, 0xb1, 0x26, 0x5e, 0x00,
		0x03, 0x2a, 0x5e, 0x00, 0x03, 0x2a, 0x91, 0x26, 0xc8, 0xd0, 0xee, 0xe6, 0x27, 0xe6, 0x3d, 0xa5,
		0x3d, 0xcd, 0x00, 0x08, 0xa6, 0x2b, 0x90, 0xdb, 0x4c, 0x01, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00,
	};	

	// Constants
	private static final int NUM_DRIVES = 2;
	private static final int DOS_NUM_SECTORS = 16;
	private static final int DOS_NUM_TRACKS = 35;
	private static final int DOS_TRACK_BYTES = 256 * DOS_NUM_SECTORS;
	private static final int RAW_TRACK_BYTES = 6656; // TODO 6250 ???
	
	// Disk II direct access variables
	private int drive = 0;
	private boolean isMotorOn = false;

	private byte[][][] disk = new byte[NUM_DRIVES][DOS_NUM_TRACKS][];
	private boolean[] isWriteProtected = new boolean[NUM_DRIVES];

	private int currPhysTrack;
	private int currNibble;

	// Caches
	private int[] driveCurrPhysTrack = new int[NUM_DRIVES];
	private byte[] realTrack;
	
	/*
	 * Disk II emulation:
	 *
	 * C0xD, C0xE -> Read write protect
	 * C0xE, C0xC -> Read data from disk
	 * Write data to disk -> C0xF, C0xC
	 * Write data to disk -> C0xD, C0xC
	 *
	 * We use 'fast mode', i.e. no 65(C)02 clock reference
	 * We use simplified track handling (only adjacent phases)
	 */

	// Internal registers
	private int latchAddress;
	private int latchData;
	private boolean writeMode;
	
	// GCR encoding and decoding tables
	private static final int[] gcrEncodingTable = {
		0x96, 0x97, 0x9a, 0x9b, 0x9d, 0x9e, 0x9f, 0xa6,
		0xa7, 0xab, 0xac, 0xad, 0xae, 0xaf, 0xb2, 0xb3,
		0xb4, 0xb5, 0xb6, 0xb7, 0xb9, 0xba, 0xbb, 0xbc,
		0xbd, 0xbe, 0xbf, 0xcb, 0xcd, 0xce, 0xcf, 0xd3,
		0xd6, 0xd7, 0xd9, 0xda, 0xdb, 0xdc, 0xdd, 0xde,
		0xdf, 0xe5, 0xe6, 0xe7, 0xe9, 0xea, 0xeb, 0xec,
		0xed, 0xee, 0xef, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6,
		0xf7, 0xf9, 0xfa, 0xfb, 0xfc, 0xfd, 0xfe, 0xff,
	};
//	private int[] gcrDecodingTable = new int[256];
	private int[] gcrSwapBit = {0, 2, 1, 3};
	private int[] gcrBuffer = new int[256];
	private int[] gcrBuffer2 = new int[86];
	
	// Physical sector to DOS 3.3 logical sector table
	private static final int[] gcrLogicalSector = {
		0x0, 0x7, 0xe, 0x6, 0xd, 0x5, 0xc, 0x4,
		0xb, 0x3, 0xa, 0x2, 0x9, 0x1, 0x8, 0xf
	};
	
	// Temporary variables for conversion
	private byte[] gcrNibbles = new byte[RAW_TRACK_BYTES];
	private int gcrNibblesPos;
	
	/**
	 * Constructor
	 */
	public DiskII() {
	    readDisk(null, 0, null, 254, false);
		readDisk(null, 1, null, 254, false);
	}

    /**
     * I/O read
     * 
     * @param address Address
     */
	public int ioRead(int address) {
		int phase;
		
		switch (address & 0xf) {
			case 0x0:
			case 0x2:			
			case 0x4:
			case 0x6:
				// Q0, Q1, Q2, Q3 off
				break;
			case 0x1:
				// Q0 on
				phase = currPhysTrack & 3;
				if (phase == 1) {
					if (currPhysTrack > 0) {
						currPhysTrack--;
					}
				} else if (phase == 3) {
					if (currPhysTrack < ((2 * DOS_NUM_TRACKS) - 1)) {
						currPhysTrack++;
					}
				}
				realTrack = disk[drive][currPhysTrack >> 1];
				break;
			case 0x3:
				// Q1 on
				phase = currPhysTrack & 3;
				if (phase == 2) {
					if (currPhysTrack > 0) {
						currPhysTrack--;
					}
				} else if (phase == 0) {
					if (currPhysTrack < ((2 * DOS_NUM_TRACKS) - 1)) {
						currPhysTrack++;
					}
				}
				realTrack = disk[drive][currPhysTrack >> 1];
				break;
			case 0x5:
				// Q2 on
				phase = currPhysTrack & 3;
				if (phase == 3) {
					if (currPhysTrack > 0) {
						currPhysTrack--;
					}
				} else if (phase == 1) {
					if (currPhysTrack < ((2 * DOS_NUM_TRACKS) - 1)) {
						currPhysTrack++;
					}
				}
				realTrack = disk[drive][currPhysTrack >> 1];
				break;
			case 0x7:
				// Q3 on
				phase = currPhysTrack & 3;
				if (phase == 0) {
					if (currPhysTrack > 0) {
						currPhysTrack--;
					}
				} else if (phase == 2) {
					if (currPhysTrack < ((2 * DOS_NUM_TRACKS) - 1)) {
						currPhysTrack++;
					}
				}
				realTrack = disk[drive][currPhysTrack >> 1];
				break;
			case 0x8:
				// Motor off
				isMotorOn = false;
				break;
			case 0x9:
				// Motor on
				isMotorOn = true;
				break;
			case 0xa:
				// Drive 1
				driveCurrPhysTrack[drive] = currPhysTrack;
				drive = 0;
				currPhysTrack = driveCurrPhysTrack[drive];

				realTrack = disk[drive][currPhysTrack >> 1];
				break;
			case 0xb:
				// Drive 2
				driveCurrPhysTrack[drive] = currPhysTrack;
				drive = 1;
				currPhysTrack = driveCurrPhysTrack[drive];

				realTrack = disk[drive][currPhysTrack >> 1];
				break;
			case 0xc:
				return ioLatchC();
			case 0xd:
				ioLatchD(0xff);
				break;
			case 0xe:
				return ioLatchE();
			case 0xf:
				ioLatchF(0xff);
				break;
		}
		
		return rand.nextInt(256);
    }

    /**
     * I/O write
     * 
     * @param address Address
     */
	public void ioWrite(int address, int value) {
		switch (address & 0xf) {
		case 0x0:
		case 0x1:
		case 0x2:			
		case 0x3:
		case 0x4:
		case 0x5:
		case 0x6:
		case 0x7:
		case 0x8:
		case 0x9:
		case 0xa:
		case 0xb:
			ioRead(address);
			break;
		case 0xc:
			ioLatchC();
			break;
		case 0xd:
			ioLatchD(value);
			break;
		case 0xe:
			ioLatchE();
			break;
		case 0xf:
			ioLatchF(value);
			break;
		}
    }

    /**
     * Memory read
     * 
     * @param address Address
     */
    public int memoryRead(int address) {
		return rom[address & 0xff];
    }

	/**
	 * Reset peripheral
	 */
	public void reset() {
		ioRead(0x8);
	}

    /**
     * Loads a disk
     * 
     * @param resource filename
     * @param drive Disk II drive
     * @throws IllegalStateException
     */
	public void readDisk(AppleIIGo.Dao dao, int drive, String resource, int volume, boolean isWriteProtected) {
		byte[] track = new byte[RAW_TRACK_BYTES];

		boolean isNib = false;
        if (resource != null) {
            dao.openInputStream(resource);
            if (resource.toLowerCase().endsWith(".nib")) {
System.err.println("DRIVE[" + drive + "]: NIB");
                isNib = true;
            } else {
System.err.println("DRIVE[" + drive + "]: DSK");
            }
        }
		for (int trackNum = 0; trackNum < DOS_NUM_TRACKS; trackNum++) {
			disk[drive][trackNum] = new byte[RAW_TRACK_BYTES];

			if (resource != null) {
			    if (isNib) {
	                dao.read(disk[drive][trackNum], 0, RAW_TRACK_BYTES);
			    } else {
			    dao.read(track, 0, DOS_TRACK_BYTES);
				trackToNibbles(track, disk[drive][trackNum], volume, trackNum);
			}
		}
		}
        if (resource != null) {
            dao.closeInputStream();
        }

		this.realTrack = disk[drive][currPhysTrack >> 1];
		this.isWriteProtected[drive] = isWriteProtected;
	}

    /**
     * Writes a disk
     * 
     * @param resource filename
     * @param drive Disk II drive
     */
	public void writeDisk(int drive, String resource) {
	}

	/**
 	 * Motor on indicator
	 */
	public boolean isMotorOn() {
		return isMotorOn;
	}

	/**
	 * I/O read Latch C
	 *
	 * @param	address	Address
	 */
	private int ioLatchC() {
		if (writeMode) {
			// Write data: C0xD, C0xC
			realTrack[currNibble] = (byte) latchData;
		} else {
			// Read data: C0xE, C0xC
			latchData = (realTrack[currNibble] & 0xff);
		}

		currNibble++;
		if (currNibble >= RAW_TRACK_BYTES) {
			currNibble = 0;
		}

		latchAddress = 0xc;
		return latchData;
	}
	
	/**
	 * I/O write Latch D
	 *
	 * @param	address	Address
	 */
	private void ioLatchD(int value) {
		// Prepare write
		writeMode = true;
		latchData = value;
		latchAddress = 0xd;
	}
	
	/**
	 * I/O read Latch E
	 *
	 * @param	address	Address
	 */
	private int ioLatchE() {
		// Read write-protect: C0xD, C0xE
		if (latchAddress == 0xd) {
			latchAddress = 0xe;
			return isWriteProtected[drive] ? 0x80 : 0x00;
		}

		writeMode = false;
		latchAddress = 0xe;
		return 0x3c;
	}

	/**
	 * I/O write Latch F
	 *
	 * @param	address	Address
	 */
	private void ioLatchF(int value) {
		// Prepare write
		writeMode = true;
		latchData = value;
		latchAddress = 0xf;
	}

	/*
	 * TRACK CONVERSION ROUTINES
	 */

	/**
 	 * Writes a nibble
	 *
	 * @param	value		Value
	 */
	private final void gcrWriteNibble(int value) {
		gcrNibbles[gcrNibblesPos] = (byte) value;
		gcrNibblesPos++;
	}

	/**
 	 * Writes sync bits
	 *
	 * @param	length		Number of bits
	 */
	private final void writeSync(int length) {
		while(length > 0) {
			length--;
			gcrWriteNibble(0xff);
		}
	}

	/**
	 * Write an FM encoded value, used in writing address fields 
	 *
	 * @param	value		Value
	 */
	private final void encode44(int value) {
		gcrWriteNibble((value >> 1) | 0xaa);
		gcrWriteNibble(value | 0xaa);
	}

    /**
     * Encode in 6:2
     * 
     * @param track Sectorized track data
     * @param offset Offset in this data
     */
	private void encode62(byte[] track, int offset) {
		// 86 * 3 = 258, so the first two byte are encoded twice
		gcrBuffer2[0] = gcrSwapBit[track[offset + 1] & 0x03];
		gcrBuffer2[1] = gcrSwapBit[track[offset] & 0x03];

		// Save higher 6 bits in gcrBuffer and lower 2 bits in gcrBuffer2
		for(int i = 255, j = 2; i >= 0; i--, j = j == 85 ? 0: j + 1) {
		   gcrBuffer2[j] = ((gcrBuffer2[j] << 2) | gcrSwapBit[track[offset + i] & 0x03]);
		   gcrBuffer[i] = (track[offset + i] & 0xff)  >> 2;
		}
		 
		// Clear off higher 2 bits of GCR_buffer2 set in the last call
		for(int i = 0; i < 86; i++) {
		   gcrBuffer2[i] &= 0x3f;
		}
	}

    /**
     * Write address field
     * 
     * @param track Sectorized track data
     * @param offset Offset in this data
     */
	private final void writeAddressField(int volumeNum, int trackNum, int sectorNum) {
		// Write address mark
		gcrWriteNibble(0xd5);
		gcrWriteNibble(0xaa);
		gcrWriteNibble(0x96);
		 
		// Write volume, trackNum, sector & checksum
		encode44(volumeNum);
		encode44(trackNum);
		encode44(sectorNum);
		encode44(volumeNum ^ trackNum ^ sectorNum);
		 
		// Write epilogue
		gcrWriteNibble(0xde);
		gcrWriteNibble(0xaa);
		gcrWriteNibble(0xeb);
	}
	
	/**
 	 * Write data field
	 */
	private void writeDataField() {
		int last = 0;
		int checksum;

		// Write prologue
		gcrWriteNibble(0xd5);
		gcrWriteNibble(0xaa);
		gcrWriteNibble(0xad);

		// Write GCR encoded data
		for (int i = 0x55; i >= 0; i--) {
			checksum = last ^ gcrBuffer2[i];
			gcrWriteNibble(gcrEncodingTable[checksum]);
			last = gcrBuffer2[i];
		}
		for (int i = 0; i < 256; i++) {
			checksum = last ^ gcrBuffer[i];
			gcrWriteNibble(gcrEncodingTable[checksum]);
			last = gcrBuffer[i];
		}

		// Write checksum
		gcrWriteNibble(gcrEncodingTable[last]);

		// Write epilogue
		gcrWriteNibble(0xde);
		gcrWriteNibble(0xaa);
		gcrWriteNibble(0xeb);
	}

	/**
 	 * Converts a track to nibbles
	 */
	private void trackToNibbles(byte[] track, byte[] nibbles, int volumeNum, int trackNum) {
		this.gcrNibbles = nibbles;
		gcrNibblesPos = 0;

		for (int sectorNum = 0; sectorNum < DOS_NUM_SECTORS; sectorNum++) {
			encode62(track, gcrLogicalSector[sectorNum] << 8);
			writeSync(12);
			writeAddressField(volumeNum, trackNum, sectorNum);
			writeSync(8);
			writeDataField();
		}
		writeSync(RAW_TRACK_BYTES - gcrNibblesPos);
	}
}
