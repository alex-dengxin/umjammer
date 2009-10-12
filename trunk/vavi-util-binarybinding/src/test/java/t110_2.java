/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.util.Iterator;

import org.apache.commons.betwixt.io.BeanReader;

import vavi.util.StringUtil;
import vavi.util.binary.BinaryFormat;
import vavi.util.binary.Block;


/**
 * BinaryFormat Test.
 * 
 * @author <a href=mailto:"vavivavi@yahoo.co.jp">Naohide Sano</a>(nsano)
 * @version 0.00 031216 vavi initial version <br>
 */
public class t110_2 {

    /**
     *
     */
    public static void main(String[] args) throws Exception {

        BeanReader reader = new BeanReader();
        reader.registerBeanClass(BinaryFormat.class);

        BinaryFormat bean = (BinaryFormat) reader.parse(new File(args[0]));

        System.err.println("bean: " + bean);
        System.err.println("blocks: " + bean.getBlocks().size());
        Iterator<Block> i = bean.getBlocks().iterator();
        while (i.hasNext()) {
            System.err.println("block: " + StringUtil.paramStringDeep(i.next()));
        }
    }
}

/* */
