/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.yamatonadeshiko.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import vavi.apps.yamatonadeshiko.MailDAO;
import vavi.apps.yamatonadeshiko.Shuffler.Member;
import vavi.apps.yamatonadeshiko.Shuffler.Type;


/**
 * MailDAO.
 * 
 * @author <a href="vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050830 nsano initial version <br>
 */
public class MySQLMailDAO implements MailDAO {

    /** */
    private static Log log = LogFactory.getLog(MySQLMailDAO.class);

    /** JDBC URL */
    private static String url;

    /** JDBC user */
    private static String user;

    /** JDBC password */
    private static String password;

    /* @see vavi.apps.yamatonadeshiko.MailDAO#load(java.lang.String) */
    @SuppressWarnings("unchecked")
    public List<Member>[] load(String unit) throws SQLException {
        List<Member> femaleManagers = new ArrayList<Member>();
        List<Member> females = new ArrayList<Member>();
        List<Member> maleManagers = new ArrayList<Member>();
        List<Member> males = new ArrayList<Member>();
        final String sql = "SELECT email, type, unit FROM Mail " +
                           "WHERE unit = ? AND TO_DAYS(NOW()) = TO_DAYS(updatetime)";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, unit);
            
            ResultSet rset = pstmt.executeQuery();
            while (rset.next()) {
               unit = rset.getString("unit");
               Member member = new Member();
               member.email = rset.getString("email");
               member.type = Type.values()[rset.getInt("type")];
               switch (member.type) {
               case FemaleManager:
                   femaleManagers.add(member);
                   break;
               case Female:
                   females.add(member);
                   break;
               case MaleManager:
                   maleManagers.add(member);
                   break;
               case Male:
                   males.add(member);
                   break;
               default:
                   throw new IllegalStateException("unknown type " + member.type);
               }
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

        return new List[] { femaleManagers, females, maleManagers, males };
    }

    /* @see vavi.apps.yamatonadeshiko.MailDAO#clear(java.lang.String) */
    public void clear(String unit) throws SQLException {
        final String sql = "DELETE FROM Mail " +
                           "WHERE unit = ? AND TO_DAYS(NOW()) = TO_DAYS(updatetime)";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, unit);
            pstmt.executeUpdate();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /** JDBC 設定が書かれたプロパティファイル */
    private static final ResourceBundle rb = ResourceBundle.getBundle("vavi.apps.yamatonadeshiko.yamatonadeshiko", Locale.getDefault());

    /** */
    static {
        try {
            url = rb.getString("jdbc.url");
            user = rb.getString("jdbc.user");
            password = rb.getString("jdbc.password");

            String driver = rb.getString("jdbc.driver");
            Class.forName(driver);
        } catch (Exception e) {
            log.error(e);
        }
    }
}

/* */
