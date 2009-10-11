/*
 * @(#) $Id: JSTKUserPrincipal.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.uam;

import java.security.Principal;


public class JSTKUserPrincipal implements Principal {
    String loginName;

    String userName;

    public JSTKUserPrincipal(String loginName, String userName) {
        this.loginName = loginName;
        this.userName = userName;
    }

    public boolean equals(Object another) {
        if (another instanceof JSTKUserPrincipal) {
            return loginName.equals(((JSTKUserPrincipal) another).getName());
        }
        return false;
    }

    public String getName() {
        return loginName;
    }

    public int hashCode() {
        return loginName.hashCode();
    }

    public String toString() {
        return "[" + this.getClass().getName() + "]" + "login: " + loginName + ", user: " + userName;
    }
}
