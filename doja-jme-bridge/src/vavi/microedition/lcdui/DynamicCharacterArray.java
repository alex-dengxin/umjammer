/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.microedition.lcdui;

import java.util.Vector;


/**
 * DynamicCharacterArray. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040920	nsano	initial version <br>
 */
class DynamicCharacterArray {

    /** */
    private Vector chars;
    
    /**
     * @param maxSize
     */
    public DynamicCharacterArray(int maxSize) {
        chars = new Vector(maxSize);
    }

    /**
     * @return
     */
    public int capacity() {
        return chars.capacity();
    }

    /**
     * @param data
     * @param offset
     * @param length
     */
    public void set(char[] data, int offset, int length) {
        for (int i = 0; i < data.length; i++) {
            chars.addElement(new Character(data[i]));
        }
    }

    /**
     * @return
     */
    public int length() {
        return chars.size();
    }

    /**
     * @param i
     * @return
     */
    public char charAt(int i) {
        return ((Character) chars.elementAt(i)).charValue();
    }

    /**
     * @param data
     * @param offset
     * @param length
     * @param position
     * @return
     */
    public int insert(char[] data, int offset, int length, int position) {
        int i = 0;
        for (; i < data.length; i++) {
            chars.insertElementAt(new Character(data[i]), position + i);
        }
        return position + i;
    }

    /**
     * @param pos
     * @param length
     */
    public void delete(int pos, int length) {
        for (int i = 0; i < length; i++) {
            chars.removeElementAt(pos);
        }
    }

    /**
     * @param i
     * @param j
     * @param data
     * @param k
     */
    public void getChars(int i, int j, char[] data, int k) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param i
     * @param str
     */
    public void insert(int i, String str) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param oldCapacity
     */
    public void setCapacity(int capacity) {
        chars.setSize(capacity);
    }
}

/* */
