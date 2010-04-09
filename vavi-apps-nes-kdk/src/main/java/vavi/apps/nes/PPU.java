/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Class for the Picture Processing Unit required by the NESCafe Emulator.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class PPU {
    /**
     * Whether VRAM is Write Protected.
     */
    public boolean vram_write_protect = false;

    /**
     * The current Graphical User Interface.
     */
    private NES.View gui;

    /**
     * The current NES Engine.
     */
    private NES nes;

    /**
     * Declare Buffer for pixels on current line.
     */
    private int[] linePalettes = new int[34 * 8];

    /**
     * The Palette Memory (external to the PPU Memory)
     */
    public int[] paletteMemory = new int[0x20];

    /**
     * Array declaring if Sprites or Background have already been Plotted.
     */
    private int[] solidBGPixel = new int[35 * 8];

    /**
     * PPU Register Control Register 2000
     */
    protected int REG_2000 = 0x00;

    /**
     * PPU Register Control Register 2001
     */
    public int REG_2001 = 0x00;

    /**
     * PPU Register Status Register 2002
     */
    protected int REG_2002 = 0x00;

    /**
     * PPU Background Pattern Table Address
     */
    protected int bgPatternTableAddress = 0x0000;

    /**
     * PPU Sprite Pattern Table Address
     */
    private int spPatternTableAddress = 0x1000;

    /**
     * Loopys PPU Refresh Register
     */
    public int loopyV = 0;

    /**
     * Loopys PPU Temp Refresh Register
     */
    public int loopyT = 0;

    /**
     * Loopys X Fine Scroll Register
     */
    public int loopyX = 0;

    /**
     * The PPU Read Latch for Register 2007
     */
    private int ppuReadLatch2007 = 0x00;

    /**
     * The general PPU Read Latch
     */
    private int ppuLatch = 0x00;

    /**
     * Whether the current Frame is being Skipped
     */
    private boolean skipIt = false;

    /**
     * Whether the Sprites are currently visible
     */
    public boolean showSprites = true;

    /**
     * Whether the Background is currently visible
     */
    public boolean showBackground = true;

    /**
     * Sprite Memory
     */
    public int spriteMemory[] = new int[256];

    /**
     * Current index into Sprite Memory
     */
    protected int spriteMemoryAddress = 0;

    //
    // PPU Memory
    //

    /**
     * Array for the 16K of PPU Memory.
     */
    public int[] ppuMemory = new int[0x4000];

    /**
     * Determines PPU Addressing Mode.
     */
    private boolean ppuAddressMode = false;

    /**
     * Offsets in VROM of the 12 current 1k Banks that are mapped into
     * 0x0000-0x2FFF.
     */
    private int[] ppuBank = new int[12];

    /**
     * Determines if Four Screen Name Tables are currently used.
     */
    public boolean mirrorFourScreen = false;

    /**
     * Determines if Cart is mirrored Horizontally.
     */
    protected boolean mirrorHorizontal = false;

    /**
     * Determines if Cart is mirrored Vertically.
     */
    protected boolean mirrorVertical = false;

    /**
     * PPU Address Increment.
     */
    private int ppuAddressIncrement = 1;

    /**
     * Array for the VROM from the Cart.
     */
    protected int[] ppuVROM;

    /**
     * The current Scan line.
     */
    int currentScanline = 0;

    /**
     * Whether the PPU should latch the Mapper
     */
    public boolean latchMapper = false;

    /**
     * Create a new Picture Processing Unit.
     */
    public PPU(NES nes, NES.View gui) {
        // Set Pointers to NES and GUI
        this.gui = gui;
        this.nes = nes;
    }

    /**
     * Blitz the Background on the specified scanline to internal buffer.
     */
    private final void backgroundBlitzer() {
        // Determine the X and Y Coordinates of the Current Offsets
        int tileX = (loopyV & 0x001F);
        int tileY = (loopyV & 0x03E0) >> 5;
        // Determine the Name Table Address
        int nameAddr = 0x2000 + (loopyV & 0x0FFF);
        // Determine the Attribute Table Address
        int attribAddr = 0x2000 + (loopyV & 0x0C00) + 0x03C0 + ((tileY & 0xFFFC) << 1) + (tileX >> 2);
        // Determine the Attribute Bits
        int attribBits = 0;
        if ((tileY & 0x0002) == 0) {
            if ((tileX & 0x0002) == 0)
                attribBits = (ppuRead(attribAddr) & 0x03) << 2;
            else
                attribBits = (ppuRead(attribAddr) & 0x0C);
        } else {
            if ((tileX & 0x0002) == 0)
                attribBits = (ppuRead(attribAddr) & 0x30) >> 2;
            else
                attribBits = (ppuRead(attribAddr) & 0xC0) >> 4;
        }
        // Declare Addresses for the Pattern Table Address and there Low and
        // High Values
        int patternAddr;
        int patternValueLo;
        int patternValueHi;
        // Calculate the X Offset into the Line
        int p = -loopyX;
        int solid = -loopyX;
        // Draw 33 Tiles on the Current Line
        int MMC5_pal = 0;
        for (int i = 0; i < 33; i++) {
            // MMC5 Palette Support
            MMC5_pal = nes.mapper.PPU_Latch_RenderScreen(1, nameAddr & 0x03FF);
            if (MMC5_pal != 0) {
                attribBits = MMC5_pal & 0x0C;
            }
            // Grab Pattern Table Addresses
            patternAddr = bgPatternTableAddress + (ppuRead(nameAddr) << 4) + ((loopyV & 0x7000) >> 12);
            patternValueLo = ppuRead(patternAddr);
            patternValueHi = ppuRead(patternAddr + 8);
            // Latch Mapper on those Two Addresses
            if (latchMapper)
                nes.mapper.latch(patternAddr);
            // Draw the Current Tile Data
            for (int patternMask = 0x80; patternMask > 0; patternMask >>= 1) {
                // Grab the 2 Upper Bits of Colour
                int col = attribBits;
                // Grab the 2 Lower Bits of Colour
                if ((patternValueLo & patternMask) != 0)
                    col |= 0x01;
                if ((patternValueHi & patternMask) != 0)
                    col |= 0x02;
                // If Not Transparent then Draw and Mark as Drawn
                if ((col & 0x03) != 0) {
                    if (solid > 0)
                        solidBGPixel[solid] = 0x01;
                    if (showBackground && p > 0)
                        linePalettes[p] = col;
                } else {
                    if (solid > 0)
                        solidBGPixel[solid] = 0x00;
                    if (showBackground && p > 0)
                        linePalettes[p] = 0;
                }
                solid++;
                p++;
            }
            // Increment the Tile X Index and the Name Table Address
            tileX++;
            nameAddr++;
            // Check if we Crossed a Dual-Tile Boundary
            if ((tileX & 0x0001) == 0) {
                // Check if we Crossed a Quad-Tile Boundary
                if ((tileX & 0x0003) == 0) {
                    // Check if we Crossed a Name Table Boundary
                    if ((tileX & 0x001F) == 0) {
                        // Switch Name Tables
                        nameAddr ^= 0x0400;
                        attribAddr ^= 0x0400;
                        nameAddr -= 0x0020;
                        attribAddr -= 0x0008;
                        tileX -= 0x0020;
                    }
                    attribAddr++;
                }
                if ((tileY & 0x0002) == 0)
                    if ((tileX & 0x0002) == 0)
                        attribBits = (ppuRead(attribAddr) & 0x03) << 2;
                    else
                        attribBits = (ppuRead(attribAddr) & 0x0C);
                else if ((tileX & 0x0002) == 0)
                    attribBits = (ppuRead(attribAddr) & 0x30) >> 2;
                else
                    attribBits = (ppuRead(attribAddr) & 0xC0) >> 4;
            } // Dual-Tile Boundary Crossed
        } // Tiles Complete
        // Check for Clip and Mark as Not Painted (Entry 64)
        if ((REG_2001 & 0x02) == 0) {
            for (int i = 0; i < 8; i++) {
                linePalettes[i] = 64;
                solidBGPixel[i] = 0;
            }
        }
    }

    /**
     * Blitz the Sprites on the specified scanline to internal buffer.
     */
    private final void spriteBlitzer(int lineNum) {
        // Declare Sprite Coordinates
        int sprX;
        int sprY;
        // Declare In-Sprite Pixel Coordinates
        int x;
        int y;
        // Declare Start and End X Coordinates on Current Line of Sprite
        int xStart;
        int xEnd;
        // State that no Sprites are Currently on this Scanline
        int spritesInScanLine = 0;
        // Determine the Height of the Sprites (16 or 8 pixels)
        int sprHeight = ((REG_2000 & 0x20) != 0) ? 16 : 8;
        // Assume Less than 8 Sprites on Line Until Told Otherwise
        REG_2002 &= 0xDF;
        // Loop through each of the 64 Possible Sprites
        nes.mapper.PPU_Latch_RenderScreen(0, 0);
        for (int s = 0; s < 64; s++) {
            // Determine the Lines the Sprite Crosses
            sprY = spriteMemory[s * 4] + 1;
            int lineOfSprite = lineNum - sprY;
            // Check for Intersection with Current Line
            if (lineOfSprite < 0 || lineOfSprite >= sprHeight)
                continue;
            // Report on Number of Sprites on Scanline (Shouldn't Draw > 8 but
            // will)
            if (++spritesInScanLine > 8) {
                REG_2002 |= 0x20;
            }
            // Grab the Immediate Attributes of the Sprite
            int tileIndex = spriteMemory[s * 4 + 1];
            int sprAttribs = spriteMemory[s * 4 + 2];
            sprX = spriteMemory[s * 4 + 3];
            // Grab the Extended Attributes of the Sprite
            boolean sprVFlip = (sprAttribs & 0x80) != 0;
            boolean sprHFlip = (sprAttribs & 0x40) != 0;
            boolean sprBGPriority = (sprAttribs & 0x20) != 0;
            // Assume That we are Drawing All 8 Horizontal Pixels of the Sprite
            xStart = 0;
            xEnd = 8;
            // Clip the Sprite if it Goes off to the Right
            if ((sprX + 7) > 255)
                xEnd -= ((sprX + 7) - 255);
            // Determine the Y Coordinate within the Sprite
            y = lineNum - sprY;
            // Calc offsets into Line Buffer and Solid Background Buffer
            int p = sprX + xStart;
            int solid = sprX + xStart;
            // Determine Direction to Read Sprite Data based on Horizontal
            // Flipping
            int incX = 1;
            // Check if Horizontally Flipped
            if (sprHFlip) {
                incX = -1;
                xStart = 7 - xStart;
                xEnd = 7 - xEnd;
            }
            // Check if Vertically Flipped
            if (sprVFlip)
                y = (sprHeight - 1) - y;
            // Latch Mapper with Tile Address (MMC2 Punchout needs this)
            if (latchMapper)
                nes.mapper.latch(tileIndex << 4);
            // Loop Through Line in Sprite
            for (x = xStart; x != xEnd; x += incX) {
                // Declare Variables for Colour and Tile Information
                int col = 0x00;
                int tileAddr;
                int tileMask;
                // Don't Draw if a Higher Priority Sprite has Already Drawn
                if ((solidBGPixel[solid] & 2) == 0) {
                    // Calculate the Tile Address
                    if (sprHeight == 16) {
                        tileAddr = tileIndex << 4;
                        if ((tileIndex & 0x01) != 0) {
                            tileAddr += 0x1000;
                            if (y < 8)
                                tileAddr -= 16;
                        } else {
                            if (y >= 8)
                                tileAddr += 16;
                        }
                        tileAddr += y & 0x07;
                        tileMask = (0x80 >> (x & 0x07));
                    } else {
                        tileAddr = tileIndex << 4;
                        tileAddr += y & 0x07;
                        tileAddr += spPatternTableAddress;
                        tileMask = (0x80 >> (x & 0x07));
                    }
                    // Determine the Two Lower Bits of Colour for Pixel
                    if ((ppuRead(tileAddr) & tileMask) != 0)
                        col |= 0x01;
                    tileAddr += 8;
                    if ((ppuRead(tileAddr) & tileMask) != 0)
                        col |= 0x02;
                    // Determine the Two Higher Bits of Colour for Pixel
                    if ((sprAttribs & 0x02) != 0)
                        col |= 0x08;
                    if ((sprAttribs & 0x01) != 0)
                        col |= 0x04;
                    // Check if Sprite Not Transparent
                    if ((col & 0x03) != 0) {
                        // Check if Sprite 0 was Hit and Background Written
                        if (s == 0 && solidBGPixel[solid] == 1) {
                            REG_2002 |= 0x40;
                            nes.cpu.debugEventHappened("SPR");
                            // if ((REG_2000 & 0x40) != 0) nes.cpu.cpuNMI();
                        }
                        // Check if Background Has Priority over Sprite
                        if ((REG_2001 & 0x04) != 0 || (p >= 8 && p < 248)) {
                            if (sprBGPriority) {
                                // Sprite Written
                                solidBGPixel[solid] |= 0x2;
                                // Actually Draw it if No Background is Drawn
                                // Over
                                if ((solidBGPixel[solid] & 1) == 0 && showSprites)
                                    linePalettes[p] = 16 + col;
                            } else {
                                if (showSprites)
                                    linePalettes[p] = 16 + col;
                                solidBGPixel[solid] |= 0x2; // Sprite Written
                            }
                        }
                    }
                }
                // Increment Line Buffer Pointer
                p++;
                solid++;
            } // End of Loop Through Sprite X Coordinates
        }
        return;
    }

    /**
     * Start a Vertical Blank.
     */
    public final void startVBlank() {
        REG_2002 |= 0x80;
        nes.cpu.debugEventHappened("VBLIN");
    }

    /**
     * End a Vertical Blank.
     */
    public final void endVBlank() {
        // Reset Vblank and Clear Sprite Hit
        REG_2002 &= 0x3F;
        nes.cpu.debugEventHappened("VBLOUT");
    }

    /**
     * End a Frame.
     */
    public final void endFrame() {
    }

    /**
     * Check if NMI is enabled.
     */
    public final boolean nmiEnabled() {
        return (REG_2000 & 0x80) != 0;
    }

    /**
     * Draw the next Scanline.
     */
    public final void drawScanLine() {
        // Clear the Line Buffer
        for (int i = 0; i < linePalettes.length; i++)
            linePalettes[i] = 64;
        boolean skipIt = false;
        if ((REG_2001 & 0x18) != 0x00) {
            loopyScanlineStart();
            skipIt = renderLine();
            loopyScanlineEnd();
        } else {
            skipIt = renderLine();
        }
        currentScanline++;
        // If Not a Skipped Frame then Send Pixels to TV Controller
        if (!skipIt) {
            // Convert Buffered NES 4-bit Colours into 32-Bit Palette Entries
            for (int x = 0; x < linePalettes.length; x++) {
                if (linePalettes[x] != 64)
                    linePalettes[x] = paletteMemory[linePalettes[x]] & 63;
            }
            // Draw to Screen (using TV Controller)
            gui.setPixels(linePalettes);
        }
    }

    /**
     * Prepare the Refresh Data Address for the start of a new Frame. This
     * information was provided in a document called
     * "The Skinny on NES Scrolling" by Loopy on the NESDEV eGroup.
     */
    public final void startFrame() {
        // Set the Scanline Number
        currentScanline = 0;
        // Check either Background or Sprites are Displayed
        if ((REG_2001 & 0x18) != 0x00)
            loopyV = loopyT;
    }

    /**
     * Prepare the Refresh Data Address for the end of the scanline. This
     * information was provided in a document called
     * "The Skinny on NES Scrolling" by Loopy on the NESDEV eGroup.
     */
    private final void loopyScanlineEnd() {
        // Check if Subtile Y Offset is 7
        if ((loopyV & 0x7000) == 0x7000) {
            // Set SubTile Y Offset = 0
            loopyV &= 0x8FFF;
            // Check if Name Table Line = 29
            if ((loopyV & 0x03E0) == 0x03A0) {
                // Switch Name Tables
                loopyV ^= 0x0800;
                // Name Table Line 0
                loopyV &= 0xFC1F;
            } else {
                // Check if Line = 31
                if ((loopyV & 0x03E0) == 0x03E0) {
                    // Name Table Line = 0
                    loopyV &= 0xFC1F;
                } else {
                    // Increment the Table Line Number
                    loopyV += 0x0020;
                }
            }
        } else {
            // Next Subtile Y Offset
            loopyV += 0x1000;
        }
    }

    /**
     * Prepare the Refresh Data Address for the start of a new Scanline. This
     * information was provided in a document called
     * "The Skinny on NES Scrolling" by Loopy on the NESDEV eGroup.
     */
    private final void loopyScanlineStart() {
        loopyV &= 0xFBE0;
        loopyV |= loopyT & 0x041F;
    }

    /**
     * Read from a specified register on the PPU.
     */
    public final int read(int address) {
        // Determine the Address
        switch (address & 0x7) {
        case 0x2: // PPU Status Register $2002
            // Reset the PPU Addressing Mode Toggle
            reset2005Toggle();
            // Clear vBlank Flag
            int j = REG_2002;
            REG_2002 &= 0x7F;
            // Return Bits 7-4 of Unmodified Register 2002 with Bits 3-0 of the
            // Latch
            return (j & 0xE0) | (ppuLatch & 0x1F);
        case 0x4: // SPR_RAM I/O Register (RW)
            spriteMemoryAddress++;
            spriteMemoryAddress &= 0xFF;
            return spriteMemory[spriteMemoryAddress];
        case 0x7: // VRAM I/O Register $2007
            // Return the PPU Latch Value and Read a new Value from PPU Memory
            // into the Latch
            ppuLatch = ppuReadLatch2007;
            ppuReadLatch2007 = ppuRead();
            return ppuLatch;
        default: // Return PPU Latch
            return ppuLatch & 0xFF;
        }
    }

    /**
     * Render the specified line to the TV Controller.
     */
    private final boolean renderLine() {
        // Set the current Scanline
        skipIt = gui.setScanLineNum(currentScanline);
        // Clear Solid BG Buffer
        for (int i = 0; i < solidBGPixel.length; i++)
            solidBGPixel[i] = 0;
        // Buffer the Background
        if ((REG_2001 & 0x08) != 0x00) {
            backgroundBlitzer();
        }
        // Buffer the Sprites
        if ((REG_2001 & 0x10) != 0x00) {
            spriteBlitzer(currentScanline);
        }
        return skipIt;
    }

    /**
     * Loads the State of the PPU from an InputStream.
     */
    public final void stateLoad(InputStream input) throws IOException {
        // Load Palette Memory
        for (int i = 0; i < paletteMemory.length; i++)
            paletteMemory[i] = input.read() & 0xFF;
        // Load Registers
        REG_2000 = input.read() & 0xFF;
        REG_2001 = input.read() & 0xFF;
        REG_2002 = input.read() & 0xFF;
        // Load Pattern Tables Addresses
        bgPatternTableAddress = (input.read() & 0xFF) << 0x00;
        bgPatternTableAddress |= (input.read() & 0xFF) << 0x08;
        spPatternTableAddress = (input.read() & 0xFF) << 0x00;
        spPatternTableAddress |= (input.read() & 0xFF) << 0x08;
        // Load Loopy Registers
        loopyV = (input.read() & 0xFF) << 0x00;
        loopyV |= (input.read() & 0xFF) << 0x08;
        loopyV |= (input.read() & 0xFF) << 0x10;
        loopyV |= (input.read() & 0xFF) << 0x18;
        loopyT = (input.read() & 0xFF) << 0x00;
        loopyT |= (input.read() & 0xFF) << 0x08;
        loopyT |= (input.read() & 0xFF) << 0x10;
        loopyT |= (input.read() & 0xFF) << 0x18;
        loopyX = input.read() & 0xFF;
        ppuReadLatch2007 = input.read() & 0xFF;
        ppuLatch = input.read() & 0xFF;
        // Load Bank Addresses
        for (int i = 0; i < ppuBank.length; i++) {
            ppuBank[i] = (input.read() & 0xFF) << 0x00;
            ppuBank[i] |= (input.read() & 0xFF) << 0x08;
            ppuBank[i] |= (input.read() & 0xFF) << 0x10;
            ppuBank[i] |= (input.read() & 0xFF) << 0x18;
        }
        // Load Memory
        for (int i = 0; i < ppuMemory.length; i++)
            ppuMemory[i] = input.read() & 0xFF;
        // Load PPU Information
        ppuAddressMode = (input.read() == 0xFF);
        mirrorFourScreen = (input.read() == 0xFF);
        mirrorHorizontal = (input.read() == 0xFF);
        mirrorVertical = (input.read() == 0xFF);
        ppuAddressIncrement = input.read() & 0xFF;
        // Load the Sprite Memory
        for (int i = 0; i < spriteMemory.length; i++)
            spriteMemory[i] = input.read() & 0xFF;
        spriteMemoryAddress = input.read() & 0xFF;
    }

    /**
     * Saves the State of the PPU to a FileOutputStream.
     */
    public final void stateSave(OutputStream output) throws IOException {
        // Save Palette Memory
        for (int i = 0; i < paletteMemory.length; i++)
            output.write(paletteMemory[i] & 0xFF);
        // Save Registers
        output.write(REG_2000 & 0xFF);
        output.write(REG_2001 & 0xFF);
        output.write(REG_2002 & 0xFF);
        // Save Pattern Table Addresses
        output.write((bgPatternTableAddress >> 0x00) & 0xFF);
        output.write((bgPatternTableAddress >> 0x08) & 0xFF);
        output.write((spPatternTableAddress >> 0x00) & 0xFF);
        output.write((spPatternTableAddress >> 0x08) & 0xFF);
        // Save Loopy Registers
        output.write((loopyV >> 0x00) & 0xFF);
        output.write((loopyV >> 0x08) & 0xFF);
        output.write((loopyV >> 0x10) & 0xFF);
        output.write((loopyV >> 0x18) & 0xFF);
        output.write((loopyT >> 0x00) & 0xFF);
        output.write((loopyT >> 0x08) & 0xFF);
        output.write((loopyT >> 0x10) & 0xFF);
        output.write((loopyT >> 0x18) & 0xFF);
        output.write(loopyX & 0xFF);
        output.write(ppuReadLatch2007 & 0xFF);
        output.write(ppuLatch & 0xFF);
        // Save Bank Addresses
        for (int i = 0; i < ppuBank.length; i++) {
            output.write((ppuBank[i] >> 0x00) & 0xFF);
            output.write((ppuBank[i] >> 0x08) & 0xFF);
            output.write((ppuBank[i] >> 0x10) & 0xFF);
            output.write((ppuBank[i] >> 0x18) & 0xFF);
        }
        // Save Memory
        for (int i = 0; i < ppuMemory.length; i++)
            output.write(ppuMemory[i] & 0xFF);
        // Save PPU Information
        output.write(ppuAddressMode ? 0xFF : 0x00);
        output.write(mirrorFourScreen ? 0xFF : 0x00);
        output.write(mirrorHorizontal ? 0xFF : 0x00);
        output.write(mirrorVertical ? 0xFF : 0x00);
        output.write(ppuAddressIncrement & 0xFF);
        // Save the Sprite Memory
        for (int i = 0; i < spriteMemory.length; i++)
            output.write(spriteMemory[i] & 0xFF);
        output.write(spriteMemoryAddress & 0xFF);
    }

    /**
     * Write to a specified register on the PPU.
     */
    public void write(int address, int value) {
        // Set the PPU Latch
        ppuLatch = value & 0xFF;
        // Calculate the Address (8 Registers Mirrored)
        address = (address & 0x7) + 0x2000;
        // Determine the Address being Written to
        switch (address & 7) {
        case 0x0: // PPU Control Register #1
            // Set the Register Value
            REG_2000 = value;
            // Set the Background and Sprite Pattern Table Addresses
            bgPatternTableAddress = ((value & 0x10) != 0) ? 0x1000 : 0x0000;
            spPatternTableAddress = ((value & 0x08) != 0) ? 0x1000 : 0x0000;
            // Set the Address Increment Value for the PPU
            ppuAddressIncrement = ((value & 0x4) != 0) ? 32 : 1;
            // Change the Temporary Refresh Address for the PPU
            loopyT &= 0xF3FF;
            loopyT |= (value & 3) << 10;
            return;
        case 0x1: // PPU Control Register #2
            if ((REG_2001 & 0xE0) != (value & 0xE0)) {
                // Colour Emphiase Changed so Recalc
                nes.palette.calcPalette(gui.getTint(), gui.getHue(), gui.getBW(), value);
            }
            REG_2001 = value;
            return;
        case 0x2: // Status Register (Cannot be Written to)
            return;
        case 0x3: // SPR-RAM Address Register $2003
            spriteMemoryAddress = value;
            return;
        case 0x4: // SPR-RAM I/O Register $2004
            spriteMemory[spriteMemoryAddress] = (value & 0xFF);
            spriteMemoryAddress++;
            spriteMemoryAddress &= 0xFF;
            return;
        case 0x5: // VRAM Address Register #1
            set2005(value);
            return;
        case 0x6: // VRAM Address Register #2
            set2006(value);
            return;
        case 0x7: // VRAM I/O Register $2007
            ppuWrite(value);
            return;
        }
    }

    //
    // PPUMemory Functions
    //

    /**
     * Initialises the PPU Memory.
     * 
     * @param vrom The Cartridges VROM.
     * @param verticalMirroring True if Cartridge uses Vertical mirroring.
     * @param fourScreenMirror True if Cartridge uses Four Screen Mirroring.
     */
    public final void PPUInit(int vrom[], boolean verticalMirroring, boolean fourScreenMirror) {
        // Reset the PPU
        REG_2000 = 0x00;
        REG_2001 = 0x00;
        REG_2002 = 0x00;
        // Clear the PPU Memory
        for (int i = 0; i < ppuMemory.length; i++)
            ppuMemory[i] = 0;
        // Initialise the 1K PPU Memory Banks at Correct Offsets into VROM
        ppuBank[0x0] = 0x0000;
        ppuBank[0x1] = 0x0400;
        ppuBank[0x2] = 0x0800;
        ppuBank[0x3] = 0x0C00;
        ppuBank[0x4] = 0x1000;
        ppuBank[0x5] = 0x1400;
        ppuBank[0x6] = 0x1800;
        ppuBank[0x7] = 0x1C00;
        ppuBank[0x8] = 0x2000;
        ppuBank[0x9] = 0x2000;
        ppuBank[0xA] = 0x2000;
        ppuBank[0xB] = 0x2000;
        // Set the FourScreen Name Table Mirroring Method
        mirrorFourScreen = fourScreenMirror;
        mirrorVertical = verticalMirroring;
        mirrorHorizontal = !verticalMirroring;
        // Configure the Mirroring Method
        setMirror();
        // Set the Cartridge VROM
        ppuVROM = vrom;
        // Clear Sprite RAM
        spriteMemoryAddress = 0x00;
        for (int i = 0; i < spriteMemory.length; i++)
            spriteMemory[i] = 0x00;
        // Clear Pattern Table Addresses
        bgPatternTableAddress = 0x0000;
        spPatternTableAddress = 0x0000;
        // Reset the VRAM Address Registers
        currentScanline = 0;
        reset2005Toggle();
        loopyV = 0x00;
        loopyT = 0x00;
        loopyX = 0x00;
        // Reset the PPU Latch
        ppuReadLatch2007 = 0x00;
        ppuLatch = 0x00;
        ppuAddressIncrement = 1;
        // Reset Palette
        nes.palette.calcPalette(gui.getTint(), gui.getHue(), gui.getBW(), 0);
        vram_write_protect = (vrom.length != 0);
        if (vram_write_protect) {
            System.out.println("PPU: Set VRAM Write Protect");
        }
    }

    /**
     * Read from PPU Memory through 0x2007.
     * 
     * @return The value at the currently indexed cell of PPU Memory.
     */
    public final int ppuRead() {
        // Determine the Address to Read from
        int addr = loopyV;
        // Increment and Wrap the PPU Address Register
        loopyV += ppuAddressIncrement;
        addr &= 0x3FFF;
        // Call the Read Function
        return ppuRead(addr);
    }

    /**
     * Read from PPU Memory at a specified address.
     * 
     * @return The value at the specified cell of PPU Memory.
     */
    public final int ppuRead(int addr) {
        if (addr < 0x2000)
            return readPatternTable(addr);
        if (addr >= 0x3000) {
            if (addr >= 0x3F00) {
                // Palette Read
                return paletteMemory[addr & 0x1F];
            }
            addr &= 0xEFFF;
        }
        return ppuVRAM(addr);
    }

    /**
     * Read value from Pattern Table.
     * 
     * @param address Address to read from in Pattern Table.
     * @return The value at the specifed address.
     */
    private final int readPatternTable(int addr) {
        // Ensure Range of Address
        addr &= 0x1FFF;
        // Check for VROM Banks
        if (ppuVROM.length != 0) {
            return ppuVROM[ppuBank[addr >> 10] + (addr & 0x3FF)];
        }
        return ppuMemory[ppuBank[addr >> 10] + (addr & 0x3FF)];
    }

    /**
     * Resets PPU Register 0x2005.
     */
    public final void reset2005Toggle() {
        ppuAddressMode = false;
    }

    /**
     * Sets VRAM Address Register 1.
     * 
     * @param value The byte written to 0x2005.
     */
    public final void set2005(int value) {
        // The next use of this function will determine the other byte
        ppuAddressMode = !ppuAddressMode;
        // Set the Corresponding Byte of the Address
        if (ppuAddressMode) {
            // First Write : Horizontal Scroll
            loopyT &= 0xFFE0;
            loopyT |= (value & 0xF8) >> 3;
            loopyX = value & 0x07;
        } else {
            // Second Write : Vertical Scroll
            loopyT &= 0xFC1F;
            loopyT |= (value & 0xF8) << 2;
            loopyT &= 0x8FFF;
            loopyT |= (value & 0x07) << 12;
        }
    }

    /**
     * Sets VRAM Address Register 2. Setting this address requires two writes to
     * this function, the first write will set the most significant byte of the
     * address and the second write will set the least significant byte of the
     * address.
     * 
     * @param value The byte written to 0x2006.
     */
    public final void set2006(int value) {
        // The next use of this function will determine the other byte
        ppuAddressMode = !ppuAddressMode;
        // Set the corresponding Byte of the address
        if (ppuAddressMode) {
            // First Write
            loopyT &= 0x00FF;
            loopyT |= (value & 0x3F) << 8;
        } else {
            // Second Write
            loopyT &= 0xFF00;
            loopyT |= value;
            loopyV = loopyT;
        }
    }

    /**
     * Sets the Mirroring Mode for PPU Memory.
     */
    private final void setMirror() {
        // Set Four Screen Mirroring
        if (mirrorFourScreen)
            setMirroring(0, 1, 2, 3);
        // Set Horizontal Mirroring
        else if (mirrorHorizontal)
            setMirroring(0, 0, 1, 1);
        // Set Vertical Mirroring
        else if (mirrorVertical)
            setMirroring(0, 1, 0, 1);
        // If no Mirroring is Known then Select Four Screen
        else
            setMirroring(0, 1, 2, 3);
    }

    /**
     * Sets the Name Table bank numbers for use in Mirroring. Each Nametable
     * bank must be within the range of 0-3
     */
    public final void setMirroring(int nt0, int nt1, int nt2, int nt3) {
        // Ensure within Correct Range
        nt0 &= 0x3;
        nt1 &= 0x3;
        nt2 &= 0x3;
        nt3 &= 0x3;
        // Set the Bank Offsets
        ppuBank[0x8] = 0x2000 + (nt0 << 10);
        ppuBank[0x9] = 0x2000 + (nt1 << 10);
        ppuBank[0xA] = 0x2000 + (nt2 << 10);
        ppuBank[0xB] = 0x2000 + (nt3 << 10);
    }

    /**
     * Write value to currently indexed cell of PPU Memory.
     * 
     * @param value The value to be written.
     */
    public final void ppuWrite(int value) {
        // Determine the PPU Address
        int addr = loopyV;
        // Increment the PPU Address
        loopyV += ppuAddressIncrement;
        addr &= 0x3FFF;
        // Determine the Address Range
        if (addr >= 0x3000) {
            // Deal with Palette Writes
            if (addr >= 0x3F00) {
                // Palette Entry
                if ((addr & 0x000F) == 0x0000) {
                    paletteMemory[0x00] = (value & 0x3F);
                    paletteMemory[0x10] = (value & 0x3F);
                } else {
                    paletteMemory[addr & 0x001F] = (value & 0x3F);
                }
                return;
            }
            // Mirror 0x3000 to 0x2000
            addr &= 0xEFFF;
        }
        // Write to VRAM
        if (!(vram_write_protect && addr < 0x2000)) {
            ppuMemory[ppuBank[addr >> 10] + (addr & 0x3FF)] = value;
        } else {
            System.out.println("PPU: VROM write protected - Write to " + addr + " Failed");
        }
    }

    /**
     * Returns a value from VRAM.
     */
    private final int ppuVRAM(int addr) {
        return ppuMemory[ppuBank[addr >> 10] + (addr & 0x3FF)];
    }

    /**
     * Sets the offset in VROM of a PPU Memory Bank.
     * 
     * @param bankNum The bank number to configure (0-7).
     * @param offsetInCHRROM The offset in VROM that the 1K bank starts at.
     */
    public final void setPPUBankStartAddress(int bankNum, int offsetInVROM) {
        // Validate
        int num1Kvrombanks = ppuVROM.length >> 10;
        if (bankNum >= num1Kvrombanks)
            return;
        if (offsetInVROM > ppuVROM.length)
            return;
        // Set the Bank Start address
        ppuBank[bankNum] = offsetInVROM;
    }

    /**
     * Set VRAM Bank
     */
    public final void setVRAMBank(int bank, int banknum) {
        if (bank < 8) {
            // Map to Start of Pattern Tables
            ppuBank[bank] = 0x0000 + ((banknum & 0x0F) << 10);
        } else if (bank < 12) {
            // Map to Start of Name Tables
            ppuBank[bank] = 0x2000 + ((banknum & 0x03) << 10);
        }
    }
}
