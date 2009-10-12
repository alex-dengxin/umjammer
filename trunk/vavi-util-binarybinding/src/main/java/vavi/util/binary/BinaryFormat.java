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
 * BinaryFormat.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031216 vavi initial version <br>
 */
public class BinaryFormat {

    /** */
    private List<Block> blocks = new ArrayList<Block>();

    /** */
    public void addBlock(Block block) {
        this.blocks.add(block);
    }

    /** */
    public Collection<Block> getBlocks() {
        return blocks;
    }
}

/* */
