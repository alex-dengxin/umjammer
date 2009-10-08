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

import ch.laoe.ui.GCookiesListener;
import ch.laoe.ui.GCookiesPool;


/**
 * parent class of all models.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 24.01.01 first draft oli4
 */
public abstract class AModel extends AObject {
    /**
     * constructor
     */
    public AModel() {
        super();

        // cookies
        cookies = new GCookiesPool();
    }

    // parent
    protected AModel parent;

    /**
     * set parent
     */
    protected void setParent(AModel m) {
        parent = m;
    }

    /**
     * get parent
     */
    protected AModel getParent() {
        return parent;
    }

    // ********************* cookies *************************

    private GCookiesPool cookies;

    /**
     * returns the cookies-pool of this model (clip/layer/channel...)
     */
    public GCookiesListener getCookies() {
        return cookies;
    }

    // selection

    protected ASelection selection;

    /**
     * returns the selection
     */
    public ASelection getSelection() {
        return selection;
    }

    // graphic view

    protected APlotter plotter;

    /**
     * returns the view
     */
    public APlotter getPlotter() {
        return plotter;
    }

}
