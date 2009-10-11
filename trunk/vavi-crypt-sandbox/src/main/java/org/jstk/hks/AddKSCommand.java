/*
 * @(#) $Id: AddKSCommand.java,v 1.1.1.1 2003/10/05 18:39:19 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.hks;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class AddKSCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("dbprops", "lib/db.properties");
        defaults.put("type", "KS");
    }

    public String briefDescription() {
        String briefDesc = "initializes database for Hosted Key Stores";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -dbprops <file> : Property file to read database parameters.[" + defaults.get("dbprops") + "]\n" + "  -username <user>: User name.\n" + "  -password <pass>: Password to authenticate the user.\n" + "  -type <type>    : (KS|TS).[" + defaults.get("type") + "]\n" + "  -file <filename>: KeyStore or TrustStore file.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-dbprops <file>] -username <user> -password <pass> -file <filename> [-type <type>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "-username u1 -password p1 -file test.ks", "-username u1 -password p1 -file test.ks -dbprops test.props -type TS"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String dbpropFile = args.get("dbprops");
            String type = args.get("type");
            String username = args.get("username");
//          String password = args.get("password");
            String ksFile = args.get("file");

            if (username == null) {
                return new JSTKResult(null, true, "Username not set. User -username option.");
            }

            if (!type.equals("KS") && !type.equals("TS")) {
                return new JSTKResult(null, true, "Invalid type: " + type);
            }

            FileInputStream fis = new FileInputStream(dbpropFile);
            Properties dbprops = new Properties();
            dbprops.load(fis);

            File f = new File(ksFile);
            int nbytes = (int) f.length();
            FileInputStream ksfis = new FileInputStream(ksFile);

            String jdbcDriver = dbprops.getProperty("jdbcdriver");
            String dburl = dbprops.getProperty("dburl");
            String dbuser = dbprops.getProperty("dbuser");
            String dbpass = dbprops.getProperty("dbpass");
            if (dbpass == null)
                dbpass = "";

            Class.forName(jdbcDriver);
            Connection con = DriverManager.getConnection(dburl, dbuser, dbpass);

            Statement stmt = con.createStatement();
            String inscmd = "INSERT INTO HKSTABLE(USERID) VALUES('" + username + "')";
            try {
                stmt.executeUpdate(inscmd);
            } catch (SQLException sqle) {
                // May be the entry for useris exists. Ignore this error.
                // System.out.println("SQLException: " + sqle);
            }

            String updcmd = "UPDATE HKSTABLE SET " + type + " = ? WHERE USERID LIKE '" + username + "'";
            PreparedStatement stmt1 = con.prepareStatement(updcmd);
            stmt1.setBinaryStream(1, ksfis, nbytes);
            stmt1.executeUpdate();
            con.close();

            return new JSTKResult(null, true, "Updated Entry: " + username + ", " + ksFile);
        } catch (Exception exc) {
            throw new JSTKException("AddKSCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        AddKSCommand addCmd = new AddKSCommand();
        JSTKResult result = (JSTKResult) addCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
