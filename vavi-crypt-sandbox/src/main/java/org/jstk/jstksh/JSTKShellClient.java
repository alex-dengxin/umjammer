/*
 * @(#) $Id: JSTKShellClient.java,v 1.1.1.1 2003/10/05 18:39:19 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.jstksh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;

import org.jstk.JSTKOptions;
import org.jstk.JSTKQuitException;


public class JSTKShellClient {
    public static class JSTKShellCallbackHandler implements CallbackHandler {
        public void handle(Callback[] cb) {
            try {
                for (int i = 0; i < cb.length; i++) {
                    if (cb[i] instanceof NameCallback) {
                        NameCallback nc = (NameCallback) cb[i];
                        System.out.print(nc.getPrompt() + " ");
                        System.out.flush();
                        String name = new BufferedReader(new InputStreamReader(System.in)).readLine();
                        nc.setName(name);
                    } else if (cb[i] instanceof PasswordCallback) {
                        PasswordCallback pc = (PasswordCallback) cb[i];
                        System.out.print(pc.getPrompt() + " ");
                        System.out.flush();
                        String pw = new BufferedReader(new InputStreamReader(System.in)).readLine();
                        pc.setPassword(pw.toCharArray());
                        pw = null;
                    }
                }
            } catch (IOException ioe) {
                System.out.println("ioe = " + ioe);
            }
            // throw new IllegalArgumentException("Not completely implemented yet");
        }
    }

    public static class JSTKShellClientAction implements PrivilegedAction<Object> {
        private String prompt = "jstksh> ";

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public Object run() {
            try {
                JSTKShell shell = new JSTKShellServer();
                runLoop(prompt, shell);
                return null;
            } catch (Exception e) {
                return e;
            }
        }
    }

    private static void runLoop(String prompt, JSTKShell shell) throws Exception {
        String sessId = shell.createSession();
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            String cmdline = new BufferedReader(new InputStreamReader(System.in)).readLine();
            // Embed the session Id in the request. Somewhat inefficeint but will do.
            cmdline = cmdline + " -sessionid " + sessId;
            String[] cmdargs = cmdline.split("\\s");
            try {
                String result = shell.execCommand(cmdargs);
                System.out.println(result);
            } catch (JSTKQuitException qe) {
                shell.destroySession(sessId);
                return; // Break out of the loop.
            } catch (ServerException se) {
                if ((se.getCause() instanceof RemoteException) && (((RemoteException) se.getCause()).getCause() instanceof JSTKQuitException)) {
                    shell.destroySession(sessId);
                    return;
                }
                throw se;
            }
        }
    }

    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("proto", "local");
    }

    public String briefDescription() {
        String briefDesc = "JSTKShell client";
        return briefDesc;
    }

    public String optionsDescription() {
        String optionsDesc = "  -proto <proto> : Protocol to interact with server(local|rmi).[" + defaults.get("proto") + "]\n" + "  -login           : Login to server.\n";
        return optionsDesc;
    }

    public String[] useForms() {
        String[] useForms = {
            "[-proto <proto>] [-login [-user <username>] [-pass <password>]]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] sampleUses = {
            "", "-proto rmi", "-login"
        };
        return sampleUses;
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        opts.setDefaults(defaults);
        String proto = opts.get("proto");
        boolean login = Boolean.valueOf(opts.get("login")).booleanValue();

        if (proto.equalsIgnoreCase("local")) { // JSTKShell object within the same JVM.
            if (login) {
                LoginContext lc = new LoginContext("JSTKShell", new JSTKShellCallbackHandler());
                lc.login();
                JSTKShellClientAction action = new JSTKShellClientAction();
                action.setPrompt("jstk-local-login>");
                Subject.doAs(lc.getSubject(), action);
            } else {
                JSTKShell shell = new JSTKShellServer();
                runLoop("jstksh-local>", shell);
            }
        } else if (proto.equalsIgnoreCase("rmi")) {
            if (login) {
                LoginContext lc = new LoginContext("JSTKShell", new JSTKShellCallbackHandler());
                lc.login();
                JSTKShell shell = (JSTKShell) Naming.lookup("//localhost/JSTKShellAuthRMIServer");
                shell.setSubject(lc.getSubject());
                runLoop("jstksh-rmi-login>", shell);
                /*
                 * JSTKShellClientAction action = new JSTKShellClientAction(); action.setPrompt("jstk-local-login>");
                 * Subject.doAs(lc.getSubject(), action);
                 */
            } else {
                JSTKShell shell = (JSTKShell) Naming.lookup("//localhost/JSTKShellRMIServer");
                runLoop("jstksh-rmi>", shell);
            }
        } else {
            System.err.println("Unknown protocol: " + proto);
        }
    }
}
