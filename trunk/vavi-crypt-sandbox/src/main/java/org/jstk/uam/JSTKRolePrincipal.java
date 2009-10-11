/*
 * @(#) $Id: JSTKRolePrincipal.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.uam;

import java.security.Principal;


public class JSTKRolePrincipal implements Principal {
    String roleName;

    String roleDesc;

    public JSTKRolePrincipal(String roleName, String roleDesc) {
        this.roleName = roleName;
        this.roleDesc = roleDesc;
    }

    public boolean equals(Object another) {
        if (another instanceof JSTKRolePrincipal) {
            return roleName.equals(((JSTKRolePrincipal) another).getName());
        }
        return false;
    }

    public String getName() {
        return roleName;
    }

    public int hashCode() {
        return roleName.hashCode();
    }

    public String toString() {
        return "[" + this.getClass().getName() + "]" + "role: " + roleName + ", desc: " + roleDesc;
    }
}
