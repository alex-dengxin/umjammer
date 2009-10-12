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
 * Block.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031216 vavi initial version <br>
 */
public class Block {

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
    private long currentOffset;

    /** */
    public void setCurrentOffset(long currentOffset) {
        this.currentOffset = currentOffset;
    }

    /** */
    public long getCurrentOffset() {
        return currentOffset;
    }

    /** */
    private List<ReaderType> readerTypes = new ArrayList<ReaderType>();

    /** */
    public void addReaderType(ReaderType readerType) {
        this.readerTypes.add(readerType);
    }

    /** */
    public Collection<ReaderType> getReaderTypes() {
        return readerTypes;
    }
}

/* */
