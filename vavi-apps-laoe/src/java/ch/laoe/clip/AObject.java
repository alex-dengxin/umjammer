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

import ch.laoe.ui.Debug;
import ch.laoe.ui.GLanguage;


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * parent class of all array-based objects.
 * 
 * @version 24.01.01 first draft oli4
 */
public abstract class AObject {
    /**
     * constructor
     */
    public AObject() {
        setDefaultName();
    }

    // name
    protected String name;

    /**
     * get the name of the model
     */
    public String getName() {
        return name;
    }

    /**
     * set the name of the model
     */
    public void setName(String n) {
        name = n;
    }

    public String toString() {
        return name;
    }

    /**
     * set the default name of the model
     */
    public abstract void setDefaultName();

    /**
     * set the default name, with the given text to use
     */
    protected void setDefaultName(String text, int number) {
        try {
            name = "<" + (GLanguage.translate(text)) + " " + number + ">";
        } catch (Exception e) {
            Debug.printStackTrace(5, e);
        }
    }
}
