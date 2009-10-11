/*
 * @(#) $Id: JSTKShellActions.java,v 1.1.1.1 2003/10/05 18:39:19 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.jstksh;

import java.security.PrivilegedExceptionAction;


public class JSTKShellActions {
    public static class ExecCommandAction implements PrivilegedExceptionAction<Object> {
        private JSTKShell shell;

        private String[] cmdargs;

        public ExecCommandAction(JSTKShell shell, String[] cmdargs) {
            this.shell = shell;
            this.cmdargs = cmdargs;
        }

        public Object run() throws Exception {
            return shell.execCommand(cmdargs);
        }
    }

    public static class CreateSessionAction implements PrivilegedExceptionAction<Object> {
        private JSTKShell shell;

        public CreateSessionAction(JSTKShell shell) {
            this.shell = shell;
        }

        public Object run() throws Exception {
            return shell.createSession();
        }
    }

    public static class DestroySessionAction implements PrivilegedExceptionAction<Object> {
        private JSTKShell shell;

        private String sessId;

        public DestroySessionAction(JSTKShell shell, String sessId) {
            this.shell = shell;
            this.sessId = sessId;
        }

        public Object run() throws Exception {
            shell.destroySession(sessId);
            return null;
        }
    }
}
