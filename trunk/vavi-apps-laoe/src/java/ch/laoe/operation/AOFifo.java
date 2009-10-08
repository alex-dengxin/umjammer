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


/**
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * FIFO for audio track data, works also as circular buffer.
 * the pick-methods works with float index and 1st-order interpolation.
 * 
 * @version 29.07.00 erster Entwurf oli4 <br>
 *          19.12.00 float audio samples oli4 <br>
 *          21.04.01 useage of toolkit oli4
 */
public class AOFifo {
    /**
     * constructor, give the maximum FIFO-size
     */
    public AOFifo(int maxSize) {
        this.maxSize = maxSize + 1;
        body = new float[this.maxSize];
        head = 0;
        tail = 0;
    }

    // FIFO
    private float body[];

    private int head;

    private int tail;

    private int maxSize; // total memory size

    /**
     * clears the FIFO
     */
    public void clear() {
        head = 0;
        tail = 0;
    }

    /**
     * put an element to the head of the FIFO. if FIFO is full, the oldest element is erased.
     */
    public void put(float o) {
        // already full ?
        if (!isNotFull()) {
            tail = (tail + 1) % maxSize; // erase oldest element
        }
        body[head] = o;
        head = (head + 1) % maxSize;
    }

    /**
     * get an element from the tail of the FIFO
     */
    public float get() {
        float o = body[tail];
        tail = (tail + 1) % maxSize;
        return o;
    }

    /**
     * replace an element inside the FIFO
     */
    public void replace(float o, int index) {
        body[(tail + index) % maxSize] = o;
    }

    /**
     * pick an element of inside the FIFO. index is tail-aligned, float and returns interpolation.
     */
    public float pick(float index) {
        float i = tail + index;

        if (i < getActualSize()) {
            return AOToolkit.interpolate2(body, i);
        } else {
            return AOToolkit.interpolate2(body, i - maxSize);
        }
    }

    /**
     * pick an element of inside the FIFO index is head-aligned, float and returns interpolation.
     */
    public float pickFromHead(float index) {
        float i = head - index;

        if (i < 0) {
            return AOToolkit.interpolate2(body, i + maxSize);
        } else {
            return AOToolkit.interpolate2(body, i);
        }
    }

    /**
     * returns true if the FIFO is not empty
     */
    public boolean isNotEmpty() {
        return head != tail;
    }

    /**
     * returns true if the FIFO is not full
     */
    public boolean isNotFull() {
        return ((head + 1) % maxSize) != tail;
    }

    /**
     * get the actual FIFO-size
     */
    public int getActualSize() {
        return (head + maxSize - tail) % maxSize;
    }

    public static void main(String[] arg) {
        AOFifo f = new AOFifo(10);

        for (int i = 0; i < 15; i++) {
            f.put(i);
        }

        for (int i = 0; i < 20; i++) {
            float o = f.pickFromHead(i / 3.f);
            // float o = f.get();
            System.out.println(o);
        }
    }

}
