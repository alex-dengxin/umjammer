/*
 * Copyright (c) 2005 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sql.jon.server.ws;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;


/**
 * JDBC over Network Driver.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00 050523 nsano initial version <br>
 */
public class Driver implements java.sql.Driver {

    /** */
    private java.sql.Driver driver;

    /** */
    public java.sql.Connection connect(String urlString, Properties props)
        throws SQLException {

        String realDriverClassName = props.getProperty("vavi.sql.jon.driverClass");
        String realDriverURL = props.getProperty("vavi.sql.jon.driverURL");

        try {
            @SuppressWarnings("unchecked")
            Class<java.sql.Driver> realDriverClass = (Class<java.sql.Driver>) Class.forName(realDriverClassName);
            driver = realDriverClass.newInstance();
        } catch (Exception e) {
            throw (SQLException) new SQLException("real driver class not found").initCause(e);
        }

        return driver.connect(realDriverURL, props);
    }

    /** */
    public boolean acceptsURL(String url) throws SQLException {
        return driver.acceptsURL(url);
    }

    /** */
    public DriverPropertyInfo[] getPropertyInfo(String urlString, Properties props) throws SQLException {
        return driver.getPropertyInfo(urlString, props);
    }

    /** */
    public int getMajorVersion() {
        return driver.getMajorVersion();
    }

    /** */
    public int getMinorVersion() {
        return driver.getMinorVersion();
    }

    /** */
    public boolean jdbcCompliant() {
        return driver.jdbcCompliant();
    }
}

/* */
