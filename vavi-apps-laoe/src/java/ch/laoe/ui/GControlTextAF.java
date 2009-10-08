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

import ch.laoe.operation.AOToolkit;
import ch.oli4.ui.UiControlText;


/*********************************************************************************************************************************
 * 
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with LAoE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Class: GControlTextAF @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * UiControlText with units adapted to relative factors in frequency domain.
 * 
 * @version 02.12.01 first draft oli4
 * 
 */
public class GControlTextAF extends UiControlText {
    public GControlTextAF(int digits, boolean incrementVisible, boolean unitVisible) {
        super(digits, incrementVisible, unitVisible);
        setUnit("%");
    }

    protected void fillUnits() {
        addUnit(" ");
        addUnit("%");
        addUnit("htone");
        addUnit("oct");
    }

    protected double dataToUnit(double d) {
        switch (unitIndex) {
        case 0:
            return d;

        case 1:
            return d * 100;

        case 2:
            return AOToolkit.toHalfTone((float) d);

        case 3:
            return AOToolkit.toOctave((float) d);
        }
        return d;
    }

    protected double unitToData(double u) {
        switch (unitIndex) {
        case 0:
            return u;

        case 1:
            return u / 100;

        case 2:
            return AOToolkit.fromHalfTone((float) u);

        case 3:
            return AOToolkit.fromOctave((float) u);
        }
        return u;
    }
}
