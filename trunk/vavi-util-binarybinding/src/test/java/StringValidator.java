/*
 * Copyright (c) 2003 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	031019	vavi	initial version <br>
 */
public class StringValidator extends Validator {

    /** */
    private Pattern pattern;

    /**
     * @param regex マッチングさせる正規表現
     */
    public void setPattern(String regex) throws PatternSyntaxException {
        this.pattern = Pattern.compile(regex);
    }

    /** */
    public String getPattern() {
        return pattern.pattern();
    }
    
    /** */
    public boolean validate(String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}

/* */
