/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

/**
 * CsvFormat.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031019 vavi initial version <br>
 */
public class CsvFormat {

    /** */
    private int count;

    /** */
    private List<Validator> validators = new ArrayList<Validator>();

    /** */
    public void addValidator(Validator validator) {
        this.validators.add(validator);
    }

    /** */
    public Collection<Validator> getValidators() {
        return validators;
    }

    /** */
    public void setCount(int count) {
        this.count = count;
    }

    /** */
    public int getCount() {
        return count;
    }
}

/* */
