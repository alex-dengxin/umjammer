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

import java.util.Vector;


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * parent class of all models that contains sub-elements (like clip contains layers).
 * 
 * @version 28.12.00 first draft oli4 <br>
 *          24.01.01 array-based oli4 <br>
 */
public abstract class AContainerModel extends AModel {
    /**
     * constructor
     */
    public AContainerModel() {
        super();
        elements = new Vector<AModel>();
        selectedIndex = 0;
    }

    // subelements
    private Vector<AModel> elements;

    private void cleanUp() {
        System.gc();
    }

    public void add(AModel e) {
        e.setParent(this);
        elements.add(e);
    }

    public void link(AModel e) {
        elements.add(e);
    }

    public void insert(AModel e, int index) {
        e.setParent(this);
        elements.insertElementAt(e, index);
    }

    public void replace(AModel e, int index) {
        e.setParent(this);
        elements.set(index, e);
        cleanUp();
    }

    public AModel get(int index) {
        return elements.get(index);
    }

    public boolean contains(AModel m) {
        for (int i = 0; i < getNumberOfElements(); i++) {
            if (get(i) == m) {
                return true;
            }
        }
        return false;
    }

    public void remove(int index) {
        if (elements.size() > 1) {
            elements.remove(index);
            cleanUp();
        }
    }

    public void removeAll() {
        elements.removeAllElements();
        cleanUp();
    }

    public int getNumberOfElements() {
        return elements.size();
    }

    public void moveUp(int index) {
        // range ok ?
        if ((index < getNumberOfElements() - 1) && (index >= 0)) {
            AModel e = elements.get(index + 1);
            elements.set(index + 1, elements.get(index));
            elements.set(index, e);
        }
    }

    public void moveDown(int index) {
        moveUp(index - 1);
    }

    public AModel getTop() {
        return elements.get(getNumberOfElements() - 1);
    }

    public AModel getBottom() {
        return elements.get(0);
    }

    // selected element
    protected int selectedIndex;

    public AModel getSelected() {
        return get(getSelectedIndex());
    }

    public int getSelectedIndex() {
        // range limiter
        if (selectedIndex >= elements.size())
            selectedIndex = elements.size() - 1;

        if (selectedIndex < 0)
            selectedIndex = 0;

        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    /**
     * returns the biggest number of subelements per element
     */
    public int getMaxNumberOfSubElements() {
        int m = 0;
        for (int i = 0; i < elements.size(); i++) {
            int s = ((AContainerModel) elements.get(i)).getNumberOfElements();
            if (s > m)
                m = s;
        }
        return m;
    }

}
