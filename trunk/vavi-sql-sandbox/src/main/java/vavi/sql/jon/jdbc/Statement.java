/*
 * Copyright (c) 2004 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sql.jon.jdbc;

import java.sql.SQLException;
import java.sql.SQLWarning;


/**
 * Statement.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040620 nsano initial version <br>
 */
public class Statement
    implements java.sql.Statement {

    /** */
    private java.sql.Connection connection = null;

    /** */
    private String sql = null;

    java.sql.ResultSet currentResultSet = null;

    /** */
    public void setConnection(java.sql.Connection connection) {
        this.connection = connection;
    }

    /** */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /** */
    public Statement(java.sql.Connection connection) {
        this.connection = connection;
    }

    /** */
    protected void finalize() {
        try {
            close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    /** */
    public java.sql.ResultSet executeQuery(String sql) throws SQLException {
        if (execute(sql) == false) {
            return null;
        }

        currentResultSet = new ResultSet(this);

        return currentResultSet;
    }

    /** */
    public int executeUpdate(String sql) throws SQLException {
        int count = -1;

        if (execute(sql) == true) {
            count = getUpdateCount();
        }

        return count;
    }

    /** TODO */
    public int executeUpdate(String sql, String[] columnNmaes)
        throws SQLException {

        int count = -1;

        if (execute(sql) == true) {
            count = getUpdateCount();
        }

        return count;
    }

    /** */
    public void close() throws SQLException {
        if (currentResultSet != null) {
            currentResultSet.close();
            currentResultSet = null;
        }
    }

    /** */
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    /** */
    public void setMaxFieldSize(int max) throws SQLException {
        if (max != 0) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    /** */
    public int getMaxRows() throws SQLException {
        return 0;
    }

    /** */
    public void setMaxRows(int max) throws SQLException {
        if (max != 0) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    /** */
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    /** */
    public void setQueryTimeout(int seconds) throws SQLException {
        if (seconds != 0) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    /** */
    public void cancel() throws SQLException {
    }

    /** */
    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void setCursorName(String name) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public boolean execute(String sql) throws SQLException {
        return false;
    }

    /** */
    public boolean execute(String sql, String[] x) throws SQLException {
        return false;
    }

    /** */
    public boolean execute(String sql, int[] x) throws SQLException {
        return false;
    }

    /** */
    public java.sql.ResultSet getResultSet() throws SQLException {
        return currentResultSet;
    }

    /** TODO */
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    /** */
    public boolean getMoreResults() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getResultSetConcurrency() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getResultSetType() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public java.sql.Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public java.sql.ResultSet getGeneratedKeys() {
        return null;
    }

    /**
     * @see java.sql.Statement#getResultSetHoldability()
     */
    public int getResultSetHoldability() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.sql.Statement#getMoreResults(int)
     */
    public boolean getMoreResults(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.sql.Statement#executeUpdate(java.lang.String, int)
     */
    public int executeUpdate(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.sql.Statement#execute(java.lang.String, int)
     */
    public boolean execute(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
     */
    public int executeUpdate(String arg0, int[] arg1) throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* @see java.sql.Statement#isClosed() */
    public boolean isClosed() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /* @see java.sql.Statement#isPoolable() */
    public boolean isPoolable() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /* @see java.sql.Statement#setPoolable(boolean) */
    public void setPoolable(boolean poolable) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    /* @see java.sql.Wrapper#isWrapperFor(java.lang.Class) */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /* @see java.sql.Wrapper#unwrap(java.lang.Class) */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}

/* */
