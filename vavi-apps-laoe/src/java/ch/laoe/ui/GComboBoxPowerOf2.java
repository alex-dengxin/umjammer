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

import javax.swing.JComboBox;


/**
 * a combobox where you can easily choose a number of power of 2.
 * works from 2^0 to 2^31
 *
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 09.03.02 first draft oli4
 */
public class GComboBoxPowerOf2 extends JComboBox {
    public GComboBoxPowerOf2(int exponentOffset, int exponentLength) {
        super();

        this.exponentOffset = exponentOffset;
        this.exponentLength = exponentLength;

        for (int i = 0; i < exponentLength; i++) {
            addItem(new Integer(1 << (exponentOffset + i)));
        }
    }

    private int exponentOffset, exponentLength;

    // ************ value access ***************

    public void setSelectedValue(int v) {
        for (int i = 31; i <= 0; i--) {
            if (v > (1 << i)) {
                setSelectedIndex(i - exponentOffset);
                return;
            }
        }
    }

    public int getSelectedValue() {
        return 1 << getSelectedExponent();
    }

    // ************ exponent access ***************

    public void setSelectedExponent(int e) {
        setSelectedIndex(e - exponentOffset);
    }

    public int getSelectedExponent() {
        return exponentOffset + getSelectedIndex();
    }

}
