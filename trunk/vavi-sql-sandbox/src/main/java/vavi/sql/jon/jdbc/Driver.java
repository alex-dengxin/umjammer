/*
 * Copyright (c) 2004 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sql.jon.jdbc;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;


/**
 * JDBC Front End Driver.
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040620	nsano	initial version <br>
 */
public class Driver implements java.sql.Driver {

    /** */
    private URL url = null;

    /** */
    public java.sql.Connection connect(String urlString, Properties props)
        throws SQLException {

        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw (SQLException) new SQLException().initCause(e);
        }

        java.sql.Connection connection = new Connection();
        ((Connection) connection).connect(url.toString(), props);

        return connection;
    }

    /** */
    public boolean acceptsURL(String url) {
        return false;
    }

    /** */
    public DriverPropertyInfo[] getPropertyInfo(String urlString, Properties props) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getMajorVersion() {
        return 0;
    }

    /** */
    public int getMinorVersion() {
        return 0;
    }

    /** */
    public boolean jdbcCompliant() {
        return false;
    }
}

/* */
