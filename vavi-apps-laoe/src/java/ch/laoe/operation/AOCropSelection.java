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

package ch.laoe.operation;

import ch.laoe.clip.AChannelSelection;


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
 * Class: AOCropSelection @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * cut the non-selected parts.
 * 
 * @version 30.10.00 erster Entwurf oli4 19.12.00 float audio samples oli4 26.01.01 array-based again...
 * oli4
 * 
 */
public class AOCropSelection extends AOperation {
    public AOCropSelection() {
        super();
    }

    public void operate(AChannelSelection ch1) {
        float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        float tmp[] = new float[Math.max(2, l1)];

        // mark changed channels...
        ch1.getChannel().changeId();

        try {
            // copy
            for (int i = 0; i < l1; i++)
                tmp[i] = s1[i + o1];
        } catch (ArrayIndexOutOfBoundsException oob) {
        }

        // replace original
        ch1.getChannel().sample = tmp;

    }
}
