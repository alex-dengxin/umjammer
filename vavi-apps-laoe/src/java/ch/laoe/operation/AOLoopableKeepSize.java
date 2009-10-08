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


/**
 * makes a selection loopable, keeping the original size
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 01.02.02 first draft oli4
 */
public class AOLoopableKeepSize extends AOperation {
    public AOLoopableKeepSize(int borderWidth) {
        super();
        this.borderWidth = borderWidth;
    }

    private int order = AOFade.LINEAR;

    private int borderWidth;

    public void operate(AChannelSelection ch1) {
//      float s1[] = ch1.getChannel().sample;
        int o1 = ch1.getOffset();
        int l1 = ch1.getLength();

        // mark changed channels...
        ch1.getChannel().changeId();

        // fade in left part
        AChannelSelection ch2 = new AChannelSelection(ch1.getChannel(), o1, borderWidth);
        ch2.operateChannel(new AOFade(AOFade.IN, order, 0, false));

        // fade out right part
        AChannelSelection ch3 = new AChannelSelection(ch1.getChannel(), o1 + l1 - borderWidth, borderWidth);
        ch3.operateChannel(new AOFade(AOFade.OUT, order, 0, false));
    }

}
