/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031019 vavi initial version <br>
 */
public class DateValidator extends Validator {

    /** {@link DateFormat} で使用されるフォーマットの文字列のコレクション。 */
    private Set<String> formats = new HashSet<String>();

    /**
     * @param format {@link DateFormat} で使用されるフォーマットの文字列
     */
    public void addFormat(String format) {
        this.formats.add(format);
    }

    /** */
    public Collection<String> getFormats() {
        return formats;
    }

    /** */
    public boolean validate(String value) {

        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);

        try {
            for (String format : formats) {
                DateFormat sdf = new SimpleDateFormat(format);
                sdf.format(calendar.getTime());
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

/* */
