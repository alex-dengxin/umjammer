/*
 * @(#) $Id: UserAccountManager.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.uam;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


public class UserAccountManager implements java.io.Serializable {
    public static class NoSuchUserException extends Exception {
    }

    public static class NoSuchRoleException extends Exception {
    }

    public static class RoleNotFreeException extends Exception {
    }

    public static class InvalidPasswordException extends Exception {
    }

    private Map<String, JSTKUser> users = null;

    private Map<String, JSTKRole> roles = null;

    private transient UAMPersistenceManagerIntf pmi;

    private static UserAccountManager instance = null;

    public UserAccountManager() {
        users = new HashMap<String, JSTKUser>();
        roles = new HashMap<String, JSTKRole>();
    }

    public static UserAccountManager getInstance(UAMPersistenceManagerIntf pmi) throws Exception {
        if (instance == null)
            instance = pmi.load();
        instance.pmi = pmi;
        return instance;
    }

    public void addUser(String loginName, String userName, String passWord) {
        JSTKUser user = users.get(loginName);
        if (user != null) {
            user.setUserName(userName);
            user.setPassWord(passWord);
        } else {
            user = new JSTKUser(loginName, userName, passWord);
            users.put(loginName, user);
        }
        pmi.addUser(loginName, userName, passWord);
    }

    public void changePassWord(String loginName, String passWord) throws NoSuchUserException {
        JSTKUser user = users.get(loginName);
        if (user == null)
            throw new NoSuchUserException();
        user.setPassWord(passWord);
        pmi.changePassWord(loginName, passWord);
    }

    // Returns a copy and not a reference.
    public Principal getUser(String loginName) {
        JSTKUser user = users.get(loginName);
        if (user == null)
            return null;
        else
            return new JSTKUserPrincipal(user.getLoginName(), user.getUserName());
    }

    public void remUser(String loginName) {
        JSTKUser user = users.get(loginName);
        if (user == null) // Nothing to remove.
            return;

        // Remove from all roles
        Iterator<String> itr = user.roles();
        while (itr.hasNext()) {
            String roleName = itr.next();
            JSTKRole role = roles.get(roleName);
            role.remUser(loginName);
        }

        // Remove the user.
        users.remove(loginName);
        pmi.remUser(loginName);
    }

    // Iterator of user Principals
    public Iterator<Principal> users() {
        Iterator<JSTKUser> itr = users.values().iterator();
        Vector<Principal> v = new Vector<Principal>();
        while (itr.hasNext()) {
            JSTKUser user = itr.next();
            v.add(new JSTKUserPrincipal(user.getLoginName(), user.getUserName()));
        }
        return v.iterator();
    }

    public void addRole(String roleName, String desc) {
        JSTKRole role = roles.get(roleName);
        if (role != null) {
            role.setRoleDesc(desc);
        } else {
            role = new JSTKRole(roleName, desc);
            roles.put(roleName, role);
        }
        pmi.addRole(roleName, desc);
    }

    // Returns a copy and not a reference.
    public Principal getRole(String roleName) {
        JSTKRole role = roles.get(roleName);
        if (role == null)
            return null;
        else
            return new JSTKRolePrincipal(role.getRoleName(), role.getRoleDesc());
    }

    public void remRole(String roleName) throws RoleNotFreeException {
        JSTKRole role = roles.get(roleName);
        if (role == null)
            return;
        if (role.hasUsers())
            throw new RoleNotFreeException();
        roles.remove(roleName);
        pmi.remRole(roleName);
    }

    // Iterator over role Principals
    public Iterator<Principal> roles() {
        Iterator<JSTKRole> itr = roles.values().iterator();
        Vector<Principal> v = new Vector<Principal>();
        while (itr.hasNext()) {
            JSTKRole role = itr.next();
            v.add(new JSTKRolePrincipal(role.getRoleName(), role.getRoleDesc()));
        }
        return v.iterator();
    }

    // Iterator over user Principals of a given role
    public Iterator<Principal> roleUsers(String roleName) throws NoSuchRoleException {
        JSTKRole role = roles.get(roleName);
        if (role == null) // Nothing to remove.
            throw new NoSuchRoleException();

        Iterator<String> itr = role.users();
        Vector<Principal> v = new Vector<Principal>();
        while (itr.hasNext()) {
            String loginName = itr.next();
            JSTKUser user = users.get(loginName);
            v.add(new JSTKUserPrincipal(user.getLoginName(), user.getUserName()));
        }
        return v.iterator();
    }

    // Iterator over role Principals of a given user
    public Iterator<Principal> userRoles(String loginName) throws NoSuchUserException {
        JSTKUser user = users.get(loginName);
        if (user == null) // Nothing to remove.
            throw new NoSuchUserException();

        Iterator<String> itr = user.roles();
        Vector<Principal> v = new Vector<Principal>();
        while (itr.hasNext()) {
            String roleName = itr.next();
            JSTKRole role = roles.get(roleName);
            v.add(new JSTKRolePrincipal(role.getRoleName(), role.getRoleDesc()));
        }
        return v.iterator();
    }

    public void addRoleToUser(String roleName, String loginName) throws NoSuchUserException, NoSuchRoleException {
        JSTKRole role = roles.get(roleName);
        if (role == null)
            throw new NoSuchRoleException();

        JSTKUser user = users.get(loginName);
        if (user == null)
            throw new NoSuchUserException();

        if (user.isUserInRole(roleName)) // Nothing to do.
            return;

        role.addUser(loginName);
        user.addRole(roleName);
        pmi.addRoleToUser(roleName, loginName);
    }

    public void remRoleFromUser(String roleName, String loginName) throws NoSuchUserException, NoSuchRoleException {
        JSTKRole role = roles.get(roleName);
        if (role == null)
            throw new NoSuchRoleException();

        JSTKUser user = users.get(loginName);
        if (user == null)
            throw new NoSuchUserException();

        if (!user.isUserInRole(roleName)) // Nothing to do.
            return;

        role.remUser(loginName);
        user.remRole(roleName);
        pmi.remRoleFromUser(roleName, loginName);
    }

    public void validate(String loginName, String passWord) throws NoSuchUserException, InvalidPasswordException {
        JSTKUser user = users.get(loginName);
        if (user == null)
            throw new NoSuchUserException();
        if (!passWord.equals(user.getPassWord()))
            throw new InvalidPasswordException();
        return;
    }
}
