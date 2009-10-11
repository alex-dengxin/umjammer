/*
 * @(#) $Id: InitHKSCommand.java,v 1.1.1.1 2003/10/05 18:39:19 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.hks;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class InitHKSCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("dbprops", "config/db.properties");
        defaults.put("username", "sa");
        defaults.put("password", "sa");
    }

    public String briefDescription() {
        String briefDesc = "initializes database for Hosted Key Stores";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -dbprops <file> : Property file to read database parameters.[" + defaults.get("dbprops") + "]\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-dbprops <file>]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-dbprops test.props"
        };
        return sampleUses;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String dbpropFile = args.get("dbprops");
//            String username = args.get("username");
//            String password = args.get("password");

            FileInputStream fis = new FileInputStream(dbpropFile);
            Properties dbprops = new Properties();
            dbprops.load(fis);

            String jdbcDriver = dbprops.getProperty("jdbcdriver");
            String dburl = dbprops.getProperty("dburl");
            String dbuser = dbprops.getProperty("dbuser");
            String dbpass = dbprops.getProperty("dbpass");
            String crcmd = dbprops.getProperty("crcmd");

            Class.forName(jdbcDriver);
            if (dbpass == null)
                dbpass = "";
            Connection con = DriverManager.getConnection(dburl, dbuser, dbpass);
            Statement stmt = con.createStatement();

            stmt.executeUpdate(crcmd);
            con.close();

            return new JSTKResult(null, true, "HKS Database \"" + dburl + "\" initialized.");
        } catch (Exception exc) {
            throw new JSTKException("InitHKSCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        InitHKSCommand initCmd = new InitHKSCommand();
        JSTKResult result = (JSTKResult) initCmd.execute(opts);
        System.out.println(result.getText());
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
