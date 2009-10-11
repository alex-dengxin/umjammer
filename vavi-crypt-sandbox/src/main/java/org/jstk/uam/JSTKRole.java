/*
 * @(#) $Id: JSTKRole.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
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


public class JSTKRole implements java.io.Serializable {
    String roleName;

    String roleDesc;

    Set<String> users;

    public JSTKRole(String roleName, String roleDesc) {
        this.roleName = roleName;
        this.roleDesc = roleDesc;
        users = new HashSet<String>();
    }

    public void addUser(String loginName) {
        users.add(loginName);
    }

    public void remUser(String loginName) {
        users.remove(loginName);
    }

    public boolean hasUsers() {
        return !users.isEmpty();
    }

    public Iterator<String> users() {
        return users.iterator();
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public int hashCode() {
        return roleName.hashCode();
    }

    public String toString() {
        return "[" + this.getClass().getName() + "]" + "role: " + roleName + ", desc: " + roleDesc;
    }
}
