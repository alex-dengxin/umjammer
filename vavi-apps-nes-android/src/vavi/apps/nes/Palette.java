/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes;


/**
 * Class for the Palette used by the NESCafe NES Emulator.
 * 
 * I would like to credit Kevin Horton who published the mathematical
 * representation
 * 
 * of the NTSC Chromo Decoder Matrix on www.egroups.com for people involved with
 * NES
 * 
 * Hardware development (group NESDEV). His posting was number 893.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class Palette {
    /**
     * 64 Colour Entry Palette
     */
    public int[] palette = new int[64 + 1];

    /**
     * Whether Palette has Changed
     */
    public boolean changed = true;

    /**
     * Dynamically calculate Palette. Equations and Example Code by Kevin Horton
     * from Nes Development on eGroups.com Message 893
     */
    public final void calcPalette(float Tint, float Hue, boolean bw, int reg2001) {
        // Determine the Tint (The Amount of White in the Colour)
        float tint = Tint / 256.0f;
        // Determine the Hue (A range of +/- 15-20 degrees)
        float hue = 332.0f + ((Hue - 128.0f) * (20.0f / 256.0f));
        // The Colour Angles Data Table
        final int[] cols = {
            0, 240, 210, 180, 150, 120, 90, 60, 30, 0, 330, 300, 270, 0, 0, 0
        };
        // The Brightness Tables
        final float[] br1 = {
            0.50f, 0.75f, 1.00f, 1.00f
        };
        final float[] br2 = {
            0.29f, 0.45f, 0.73f, 0.90f
        };
        final float[] br3 = {
            0.00f, 0.24f, 0.47f, 0.77f
        };
        // X is the Brightness Bits
        for (int x = 0; x < 4; x++) {
            // Z is the NES Colour Entry
            for (int z = 0; z < 16; z++) {
                // Grab the Tint
                float s = tint;
                // Grab the Default Luminance
                float y = br2[x];
                // If Colour XDh Get Luminance
                if (z == 0) {
                    s = 0;
                    y = br1[x];
                    // If Colour X0h Get Luminance
                } else if (z == 13) {
                    s = 0;
                    y = br3[x];
                    // If Colour XEh or XFh then set to Black
                } else if ((z == 14) || (z == 15)) {
                    s = 0;
                    y = 0;
                }
                // Grab Colour Angle (add Hue), Divide by 180 and Multiply by PI
                float theta = (float) Math.PI * ((cols[z] + hue) / 180.0f);
                // The Mathematical representation of the NTSC Chromo Decoder
                // Matrix
                float r = y + (s * (float) Math.sin(theta));
                float g = y - ((27.0f / 53.0f) * s * (float) Math.sin(theta)) + ((10.0f / 53.0f) * s * (float) Math.cos(theta));
                float b = y - (s * (float) (Math.cos(theta)));
                // Normalise RGB Components
                r = r * 256.0f;
                g = g * 256.0f;
                b = b * 256.0f;
                // Keeps RGB Components in Range
                if (r > 255.0f)
                    r = 255.0f;
                if (g > 255.0f)
                    g = 255.0f;
                if (b > 255.0f)
                    b = 255.0f;
                if (r < 0.0f)
                    r = 0.0f;
                if (g < 0.0f)
                    g = 0.0f;
                if (b < 0.0f)
                    b = 0.0f;
                // Find Index into NES Palette
                int index = (x * 16) + z;
                // Store the Colour in the Palette
                palette[index] = 0xFF000000;
                palette[index] |= ((int) r << 0x10);
                palette[index] |= ((int) g << 0x08);
                palette[index] |= ((int) b << 0x00);
            }
        }
        // Check for Color Emphiase
        if ((reg2001 & 0xe0) != 0) {
            // Loop Over Each Colour in Palette
            for (int i = 0; i < 64; i++) {
                // Determine the RGB Components
                float r = (palette[i] & 0x00FF0000) >> 0x10;
                float g = (palette[i] & 0x0000FF00) >> 0x08;
                float b = (palette[i] & 0x000000FF) >> 0x00;
                // Emphiase Colour
                switch (reg2001 & 0xe0) {
                case 0x20:
                    r *= 1.00;
                    g *= 0.80;
                    b *= 0.73;
                    break;
                case 0x40:
                    r *= 0.73;
                    g *= 1.00;
                    b *= 0.70;
                    break;
                case 0x60:
                    r *= 0.76;
                    g *= 0.78;
                    b *= 0.58;
                    break;
                case 0x80:
                    r *= 0.86;
                    g *= 0.80;
                    b *= 1.00;
                    break;
                case 0xA0:
                    r *= 0.83;
                    g *= 0.68;
                    b *= 0.85;
                    break;
                case 0xC0:
                    r *= 0.67;
                    g *= 0.77;
                    b *= 0.83;
                    break;
                case 0xE0:
                    r *= 0.68;
                    g *= 0.68;
                    b *= 0.68;
                    break;
                }
                // Store Entry back into Palette
                palette[i] = 0xFF000000;
                palette[i] |= ((int) r << 0x10);
                palette[i] |= ((int) g << 0x08);
                palette[i] |= ((int) b << 0x00);
            }
        }
        // Convert to a Black and White Screen
        if (bw) {
            // Run through the Palette Entries
            for (int i = 0; i < 64; i++) {
                // Determine the RGB Components
                float r = (palette[i] & 0x00FF0000) >> 0x10;
                float g = (palette[i] & 0x0000FF00) >> 0x08;
                float b = (palette[i] & 0x000000FF) >> 0x00;
                // Normalise Colours
                r *= 0.299;
                g *= 0.587;
                b *= 0.114;
                // Generate Normalised Colour Intensity
                int y = (int) (r + g + b);
                // Store Entry back into Palette
                palette[i] = 0xFF000000;
                palette[i] |= (y << 0x10);
                palette[i] |= (y << 0x08);
                palette[i] |= (y << 0x00);
            }
        }
        // Ensure Palette has Extra Black Entry
        palette[64] = 0xFF000000;
        changed = true;
    }
}
