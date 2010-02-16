/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.mona;

import org.junit.Test;


import static org.junit.Assert.*;


/**
 * UtilTest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080902 nsano initial version <br>
 */
public class HtmlUtilTest {

    @Test
    public void test01() throws Exception {
        String a = "xxxx<a href=\"../test/read.cgi/livecx/1220339513/204\" target=\"_blank\">&gt;&gt;204</a>yyyy";
        String b = HtmlUtil.toPlainText(a);
        assertEquals(b, "xxxx>>204yyyy");
    }

    @Test
    public void test02() throws Exception {
        String a = "186 ：∈(ﾟハﾟ)∋ ◆/CXNAMAz8. ：2008/09/02(火) 18:24:44.95 ID:VJLyi6dX <br> <a href=\"../test/read.cgi/livecx/1220346892/172\" target=\"_blank\">&gt;&gt;172</a>  <br> 石岡辺りか  <br>  <br>  <br> 187 ：名無しでいいとも！：2008/09/02(火) 18:24:49.04 ID:x+mcVm8S <br> <a href=\"../test/read.cgi/livecx/1220346892/172\" target=\"_blank\">&gt;&gt;172</a>  <br> おまいは石岡  <br>  ";
        String b = HtmlUtil.toPlainText(a);
        assertEquals(b, "186 ：∈(ﾟハﾟ)∋ ◆/CXNAMAz8. ：2008/09/02(火) 18:24:44.95 ID:VJLyi6dX \n >>172  \n 石岡辺りか  \n  \n  \n 187 ：名無しでいいとも！：2008/09/02(火) 18:24:49.04 ID:x+mcVm8S \n >>172  \n おまいは石岡  \n  ");
    }
}

/* */
