/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.ui;

import java.awt.Font;

import javax.swing.plaf.FontUIResource;

import ch.oli4.ui.UiLFTheme;


/**
 * Class: GLaoeTheme @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * a special LAoe metal look-and-feel theme
 * 
 * @version 29.07.01 first draft oli4
 */
public class GLaoeTheme extends UiLFTheme {

    public String getName() {
        return "LAoE-theme";
    }

    // colors...

    /*
     * private static final ColorUIResource color1 = new ColorUIResource(0x22, 0x22, 0x77); private static final ColorUIResource
     * color2 = new ColorUIResource(0x44, 0x44, 0x88); private static final ColorUIResource color3 = new ColorUIResource(0x77,
     * 0x77, 0xAA); private static final ColorUIResource color4 = new ColorUIResource(0x88, 0x88, 0x99); private static final
     * ColorUIResource color5 = new ColorUIResource(0xAA, 0xAA, 0xBB); private static final ColorUIResource color6 = new
     * ColorUIResource(0xCC, 0xCC, 0xCC);
     * 
     * 
     * protected ColorUIResource getPrimary1 () { return color1; }
     * 
     * protected ColorUIResource getPrimary2 () { return color2; }
     * 
     * protected ColorUIResource getPrimary3 () { return color3; }
     * 
     * protected ColorUIResource getSecondary1 () { return color4; }
     * 
     * protected ColorUIResource getSecondary2 () { return color5; }
     * 
     * protected ColorUIResource getSecondary3 () { return color6; }
     */

    // fonts...

    private static final FontUIResource font1 = new FontUIResource(GPersistance.createPersistance().getString("font.name"), Font.PLAIN, GPersistance.createPersistance().getInt("font.size"));

    public FontUIResource getSubTextFont() {
        return font1;
    }

    public FontUIResource getSystemTextFont() {
        return font1;
    }

    public FontUIResource getUserTextFont() {
        return font1;
    }

    public FontUIResource getControlTextFont() {
        return font1;
    }

    public FontUIResource getMenuTextFont() {
        return font1;
    }

}
