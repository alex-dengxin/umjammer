/*
 * @(#) $Id: JSTKUser.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.uam;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class JSTKUser implements java.io.Serializable {
    private String loginName;

    private String userName;

    private String passWord;

    private Set<String> roles;

    public JSTKUser(String loginName, String userName, String passWord) {
        this.loginName = loginName;
        this.userName = userName;
        this.passWord = passWord;
        roles = new HashSet<String>();
    }

    public void addRole(String roleName) {
        roles.add(roleName);
    }

    public void remRole(String roleName) {
        roles.remove(roleName);
    }

    public Iterator<String> roles() {
        return roles.iterator();
    }

    public boolean isUserInRole(String roleName) {
        return roles.contains(roleName);
    }

    public String getLoginName() {
        return loginName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public int hashCode() {
        return loginName.hashCode();
    }

    public String toString() {
        return "[" + this.getClass().getName() + "]" + "login: " + loginName + ", user: " + userName;
    }
}
