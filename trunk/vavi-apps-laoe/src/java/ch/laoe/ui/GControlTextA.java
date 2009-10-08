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

import ch.oli4.ui.UiControlText;


/**
 * UiControlText with units adapted to amplitude or attenuation.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 10.04.01 first draft oli4
 */
public class GControlTextA extends UiControlText {
    public GControlTextA(int digits, boolean incrementVisible, boolean unitVisible) {
        super(digits, incrementVisible, unitVisible);
        setUnit("%");
    }

    protected void fillUnits() {
        addUnit(" ");
        addUnit("%");
        addUnit("dB");
    }

    protected double dataToUnit(double d) {
        switch (unitIndex) {
        case 0:
            return d;

        case 1:
            return d * 100;

        case 2:
            return 8.685890 * Math.log(Math.abs(d));
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
            return Math.exp(u / 8.685890);
        }
        return u;
    }
}
