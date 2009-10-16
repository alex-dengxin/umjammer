/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import vavi.apps.yamatonadeshiko.UnitDAO;


/**
 * UnitDAO. 
 *
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050830 nsano initial version <br>
 */
public class LocalFileUnitDAO implements UnitDAO {
    /** */
    private static Log log = LogFactory.getLog(LocalFileUnitDAO.class);

    /** unit 名が書かれているファイル名 */
    private static String file;

    /* @see vavi.apps.yamatonadeshiko.UnitDAO#load() */
    public String load() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String unit = reader.readLine();
        reader.close();
//log.debug("unit: " + unit);
        return unit;
    }

    /* @see vavi.apps.yamatonadeshiko.UnitDAO#save(java.lang.String) */
    public void save(String unit) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(unit, 0, unit.length());
        writer.close();
    }

    /** ファイル名を指定するプロパティファイル */
    private static final ResourceBundle rb = ResourceBundle.getBundle("vavi.apps.yamatonadeshiko.yamatonadeshiko", Locale.getDefault());

    /** */
    static {
        try {
            file = rb.getString("unit.file");
        } catch (Exception e) {
            log.error(e);
        }
    }
}

/* */
