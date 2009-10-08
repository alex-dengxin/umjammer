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

package ch.laoe.clip;


/**
 * parent class of all selections.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.07.00 erster Entwurf oli4 <br>
 *          03.08.00 neuer Stil oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          28.12.00 stream-based oli4 <br>
 *          28.12.00 array-based again... oli4
 */
public abstract class ASelection extends AObject {
    /**
     * constructor
     */
    public ASelection(AModel m) {
        super();
        model = m;
    }

    // model
    protected AModel model;

    /**
     * get model
     */
    public AModel getModel() {
        return model;
    }

    public abstract boolean isSelected();
}
