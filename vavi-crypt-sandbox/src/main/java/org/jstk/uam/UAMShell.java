/*
 * @(#) $Id: UAMShell.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.uam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.Iterator;

import org.jstk.JSTKAbstractTool;
import org.jstk.JSTKArgs;
import org.jstk.JSTKCommand;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class UAMShell extends JSTKAbstractTool {
    private static UserAccountManager uam;

    static class AddUserCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Login Name. AddUser failed.");
            String loginName = args.get(0);
            String userName = "";
            String passWord = "default";
            if (args.getNum() > 1)
                userName = args.get(1);
            if (args.getNum() > 2)
                passWord = args.get(2);
            uam.addUser(loginName, userName, passWord);
            return new JSTKResult(null, true, "User Added: " + loginName);
        }
    }

    static class RemUserCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Login Name. RemUser failed.");
            String loginName = args.get(0);
            if (uam.getUser(loginName) == null) {
                return new JSTKResult(null, true, "No such user: " + loginName);
            } else {
                uam.remUser(loginName);
                return new JSTKResult(null, true, "User removed: " + loginName);
            }
        }
    }

    static class AddRoleCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Role Name. AddRole failed.");
            String roleName = args.get(0);
            String roleDesc = "";
            if (args.getNum() > 1)
                roleDesc = args.get(1);
            uam.addRole(roleName, roleDesc);
            return new JSTKResult(null, true, "Role Added: " + roleName);
        }
    }

    static class RemRoleCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Login Name. RemRole failed.");
            String roleName = args.get(0);
            if (uam.getRole(roleName) == null) {
                return new JSTKResult(null, true, "No such role: " + roleName);
            } else {
                try {
                    uam.remRole(roleName);
                } catch (UserAccountManager.RoleNotFreeException e) {
                    return new JSTKResult(null, true, "Role has users: " + roleName);
                }
                return new JSTKResult(null, true, "Role removed: " + roleName);
            }
        }
    }

    static class AssignRoleCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Role Name. assignrole failed.");
            if (args.getNum() < 2)
                return new JSTKResult(null, false, "No User Name. assignrole failed.");
            String roleName = args.get(0);
            String loginName = args.get(1);

            try {
                uam.addRoleToUser(roleName, loginName);
            } catch (UserAccountManager.NoSuchRoleException e) {
                return new JSTKResult(null, false, "Non-existent Role: " + roleName);
            } catch (UserAccountManager.NoSuchUserException e) {
                return new JSTKResult(null, false, "Non-existent User: " + loginName);
            }
            return new JSTKResult(null, true, "Role " + roleName + " added to User " + loginName);
        }
    }

    static class UnassignRoleCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Role Name. unassignrole failed.");
            if (args.getNum() < 2)
                return new JSTKResult(null, false, "No User Name. unassignrole failed.");
            String roleName = args.get(0);
            String loginName = args.get(1);

            try {
                uam.remRoleFromUser(roleName, loginName);
            } catch (UserAccountManager.NoSuchRoleException e) {
                return new JSTKResult(null, false, "Non-existent Role: " + roleName);
            } catch (UserAccountManager.NoSuchUserException e) {
                return new JSTKResult(null, false, "Non-existent User: " + loginName);
            }
            return new JSTKResult(null, true, "Role " + roleName + " removed from User " + loginName);
        }
    }

    static class RolesCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            try {
                StringBuffer sb = new StringBuffer();
                Iterator<Principal> itr = uam.roles();
                while (itr.hasNext()) {
                    Principal roleP = itr.next();
                    sb.append(roleP.getName() + ":");
                    Iterator<Principal> itr1 = uam.roleUsers(roleP.getName());
                    while (itr1.hasNext()) {
                        Principal userP = itr1.next();
                        sb.append(" " + userP.getName());
                    }
                    sb.append("\n");
                }
                return new JSTKResult(null, true, "----- All Roles -----\n" + sb.toString());
            } catch (UserAccountManager.NoSuchRoleException e) {
                return new JSTKResult(null, false, "internal inconsistency");
            }
        }
    }

    static class UsersCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            try {
                StringBuffer sb = new StringBuffer();
                Iterator<Principal> itr = uam.users();
                while (itr.hasNext()) {
                    Principal userP = itr.next();
                    sb.append(userP.getName() + ":");
                    Iterator<Principal> itr1 = uam.userRoles(userP.getName());
                    while (itr1.hasNext()) {
                        Principal roleP = itr1.next();
                        sb.append(" " + roleP.getName());
                    }
                    sb.append("\n");
                }
                return new JSTKResult(null, true, "----- All Users -----\n" + sb.toString());
            } catch (UserAccountManager.NoSuchUserException e) {
                return new JSTKResult(null, false, "internal inconsistency");
            }
        }
    }

    static class UserRolesCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Login Name. userroles failed.");
            String loginName = args.get(0);
            StringBuffer sb = new StringBuffer();
            Iterator<Principal> itr = null;
            try {
                itr = uam.userRoles(loginName);
            } catch (UserAccountManager.NoSuchUserException e) {
                return new JSTKResult(null, false, "No Such User: " + loginName);
            }
            while (itr.hasNext()) {
                Principal role = itr.next();
                sb.append(role.getName() + "\n");
            }
            return new JSTKResult(null, true, "All Roles:\n" + sb.toString());
        }
    }

    static class ValidateCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            if (args.getNum() < 1)
                return new JSTKResult(null, false, "No Login Name. validate failed.");
            String loginName = args.get(0);
            if (args.getNum() < 2)
                return new JSTKResult(null, false, "No Password. validate failed.");
            String password = args.get(1);
            try {
                uam.validate(loginName, password);
            } catch (UserAccountManager.NoSuchUserException e) {
                return new JSTKResult(null, false, "No Such User: " + loginName);
            } catch (UserAccountManager.InvalidPasswordException e) {
                return new JSTKResult(null, false, "Invalid Password: " + password);
            }
            return new JSTKResult(null, true, "Validation SUCCESSFUL");
        }
    }

    static class QuitCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            System.exit(0);
            return null;
        }
    }

    static {
        cmds.put("adduser", new AddUserCommand());
        cmds.put("addrole", new AddRoleCommand());
        cmds.put("remuser", new RemUserCommand());
        cmds.put("remrole", new RemRoleCommand());
        cmds.put("assignrole", new AssignRoleCommand());
        cmds.put("unassignrole", new UnassignRoleCommand());
        cmds.put("users", new UsersCommand());
        cmds.put("roles", new RolesCommand());
        cmds.put("userroles", new UserRolesCommand());
        cmds.put("validate", new ValidateCommand());
        cmds.put("quit", new QuitCommand());
        cmds.put("exit", new QuitCommand());
    }

    public String progName() {
        String progName = "java org.jstk.example.bank.BankClient";
        return progName;
    }

    public String briefDescription() {
        return "Client program for bank example";
    }

    public void init(UserAccountManager uam) {
        UAMShell.uam = uam;
    }

    public String execCommand(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        if (args.length < 1) { // No argument. Print help message.
            return usageString();
        }
        String cmdString = args[0];
        if (cmdString.equals("-h") || cmdString.equals("help") || cmdString.equals("-?")) {
            return usageString();
        }

        JSTKCommand cmd = cmds.get(cmdString);
        if (cmd == null) { // Unknown command.
            System.out.println("Unknown Command: " + cmdString);
            return usageString();
        }

        if (args.length > 1 && (args[1].equals("-h") || args[1].equals("help") || args[1].equals("-?"))) {
            return cmdUsageString(cmd, cmdString);
        }

        opts.parse(args, 1);

        JSTKResult result = (JSTKResult) cmd.execute(opts);
        return result.getText();
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        String uamfile = opts.get("uamfile");
        if (uamfile == null)
            uamfile = "config/uamdb.ser";
        UAMShell shell = new UAMShell();
        DefaultUAMPersistenceManager pm = new DefaultUAMPersistenceManager(uamfile);
        shell.init(UserAccountManager.getInstance(pm));
        while (true) {
            System.out.print("uam>");
            System.out.flush();
            String cmdline = new BufferedReader(new InputStreamReader(System.in)).readLine();
            String[] cmdargs = cmdline.split("\\s");

            String result = shell.execCommand(cmdargs);
            System.out.println(result);
        }
    }
}
