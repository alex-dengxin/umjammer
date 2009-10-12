/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.util.Iterator;

import org.apache.commons.betwixt.io.BeanReader;


/**
 * Binary Binding test.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031215 vavi initial version <br>
 */
public class t110_1 {

    /**
     *
     */
    public static void main(String[] args) throws Exception {

        BeanReader reader = new BeanReader();
        reader.registerBeanClass(CsvFormat.class);
        CsvFormat bean = (CsvFormat) reader.parse(new File(args[0]));

        System.err.println("bean: " + bean);
        System.err.println("count: " + bean.getCount());
        System.err.println("validators: " + bean.getValidators().size());
        Iterator<Validator> i = bean.getValidators().iterator();
        while (i.hasNext()) {
            System.err.println("validator: " + i.next());
        }
    }
}

/* */
