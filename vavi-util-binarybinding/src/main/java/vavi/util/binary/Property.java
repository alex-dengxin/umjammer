/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.binary;


/**
 * 名前と値のペアを表すクラスです。
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	031216	vavi	initial version <br>
 */
public class Property {

    /** */
    private String name;

    /** */
    public void setName(String name) {
        this.name = name;
    }

    /** */
    public String getName() {
        return name;
    }

    /** */
    private String value;

    /** */
    public void setValue(String value) {
        this.value = value;
    }

    /** */
    public String getValue() {
        return value;
    }
}

/* */
