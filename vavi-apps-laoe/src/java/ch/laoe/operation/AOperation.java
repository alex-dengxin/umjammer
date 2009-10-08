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
import ch.laoe.clip.AObject;


/**
 * parent class of all operations.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 26.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          01.01.01 stream-based oli4 <br>
 *          24.01.01 array-based again... oli4 <br>
 *          16.05.01 add start/endOperation oli4 <br>
 */
public abstract class AOperation extends AObject {
    /**
     * default constructor
     */
    public AOperation() {
        super();
        setDefaultName();
    }

    public void setDefaultName() {
        name = this.getClass().getName();
    }

    /**
     * performs the operation. implemented by children.
     */
    public void startOperation() {}

    public void endOperation() {}

    public void operate(AChannelSelection ch1) {}

    public void operate(AChannelSelection ch1, AChannelSelection ch2) {}

    public void operate(AChannelSelection ch1, AChannelSelection ch2, AChannelSelection ch3) {}

    // -------------------------------------------------------------------------

    /** */
    protected ProgressSupport progressSupport = new ProgressSupport();

    /** */
    public void addEditorListener(ProgressListener l) {
        progressSupport.addEditorListener(l);
    }

    /** */
    public void removeEditorListener(ProgressListener l) {
        progressSupport.removeEditorListener(l);
    }
}
