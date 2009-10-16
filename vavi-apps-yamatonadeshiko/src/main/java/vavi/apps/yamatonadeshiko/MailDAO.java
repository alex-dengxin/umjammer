/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko;

import java.sql.SQLException;
import java.util.List;

import vavi.apps.yamatonadeshiko.Shuffler.Member;


/**
 * MailDAO. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 090419 nsano initial version <br>
 */
public interface MailDAO {

    /**
     * ユニット名と本日分で絞り込んだメール送信者を取得します。
     * @return 0: female manager, 1: female, 2: male manager, 3:male 
     */
    List<Member>[] load(String unit) throws SQLException;

    /** ユニット名と本日分で絞り込んだメール送信者を削除します。 */
    void clear(String unit) throws SQLException;
}

/* */
