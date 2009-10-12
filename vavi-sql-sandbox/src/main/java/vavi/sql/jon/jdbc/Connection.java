/*
 * Copyright (c) 2004 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sql.jon.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import vavi.util.Debug;


/**
 * Connection.
 * <li>TODO FooConnection って特化すべき
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040620 nsano initial version <br>
 */
public class Connection
    implements java.sql.Connection {

    /** */
    private boolean connected = false;
    /** */
    private boolean connectionClosed = true;

    /** */
    private java.sql.Connection connection;

    /**
     * 自動コミットを行うかどうか。デフォルトは ON
     */
    private boolean autoCommit = true;

    /** */
    public java.sql.Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    /** */
    public java.sql.PreparedStatement prepareStatement(String sql)
        throws SQLException {

        return connection.prepareStatement(sql);
    }

    /** TODO */
    public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames)
        throws SQLException {

        return connection.prepareStatement(sql);
    }

    /** */
    public java.sql.CallableStatement prepareCall(String sql)
        throws SQLException {

        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public String nativeSQL(String sql) throws SQLException {
        return sql;
    }

    /** */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
    }

    /** */
    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    /** TODO */
    public void commit() throws SQLException {
    }

    /** TODO */
    public void rollback() throws SQLException {
    }

    /**
     * 
     */
    public void connect(String url, Properties props)
        throws SQLException {

        // 念のため2度目の入りを阻止します。
        if (connected) {
            return;
        }

        this.connected = true;
        this.connectionClosed = false;

        String user = props.getProperty("user", "未定義").trim();
        String password = props.getProperty("password", "未定義").trim();

Debug.println("DB 接続: " + url + ": " + user + ": " + password);
        //TODO url からプラグイン選択？ちがうぞ
        //this.connection = 
    }

    /** */
    public void close() throws SQLException {
        this.connectionClosed = true;
    }

    /** */
    public boolean isClosed() throws SQLException {
        return connectionClosed;
    }

    /** */
    public java.sql.DatabaseMetaData getMetaData() throws SQLException {
        return new DatabaseMetaData();
    }

    /** */
    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    /** */
    public void setCatalog(String catalog) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public String getCatalog() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void setTransactionIsolation(int level) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public int getTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    /** */
    public void clearWarnings() throws SQLException {
    }

    /** */
    public java.sql.Statement createStatement(int resultSetType,
                                              int resultSetConcurrency)
        throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public java.sql.PreparedStatement prepareStatement(String sql,
                                                       int resultSetType,
                                                       int resultSetConcurrency)
        throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public java.sql.CallableStatement prepareCall(String sql,
                                                  int resultSetType,
                                                  int resultSetConcurrency)
        throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public Map<String,Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /** */
    public Savepoint setSavepoint(String name) {
        return null;
    }

    /**
     * @see java.sql.Connection#getHoldability()
     */
    public int getHoldability() throws SQLException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see java.sql.Connection#setHoldability(int)
     */
    public void setHoldability(int arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.sql.Connection#setSavepoint()
     */
    public Savepoint setSavepoint() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
     */
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.sql.Connection#rollback(java.sql.Savepoint)
     */
    public void rollback(Savepoint arg0) throws SQLException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.sql.Connection#createStatement(int, int, int)
     */
    public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
     */
    public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int)
     */
    public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
     */
    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
     */
    public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[]) */
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#createBlob() */
    public Blob createBlob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#createClob() */
    public Clob createClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#createNClob() */
    public NClob createNClob() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#createSQLXML() */
    public SQLXML createSQLXML() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[]) */
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#getClientInfo() */
    public Properties getClientInfo() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#getClientInfo(java.lang.String) */
    public String getClientInfo(String name) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    /* @see java.sql.Connection#isValid(int) */
    public boolean isValid(int timeout) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    /* @see java.sql.Connection#setClientInfo(java.util.Properties) */
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        // TODO Auto-generated method stub
        
    }

    /* @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String) */
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
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
