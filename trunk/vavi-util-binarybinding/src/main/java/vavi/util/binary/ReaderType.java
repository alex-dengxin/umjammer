/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.binary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * ReaderType.
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	031216	vavi	initial version <br>
 */
public class ReaderType {

    /** */
    private String type;

    /** */
    public void setType(String type) {
        this.type = type;
    }

    /** */
    public String getType() {
        return type;
    }

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
    private int length;

    /** */
    public void setLength(int length) {
        this.length = length;
    }

    /** */
    public int getLength() {
        return length;
    }

    /** */
    private List<Property> props = new ArrayList<Property>();

    /** */
    public void addProperty(Property property) {
        this.props.add(property);
    }

    /** */
    public Collection<Property> getProperties() {
        return props;
    }
}

/* */
