/*
 * @(#) $Id: JSTKLoginModule.java,v 1.1.1.1 2003/10/05 18:39:27 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.uam;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


public class JSTKLoginModule implements LoginModule {

    private UserAccountManager uam;

    private boolean initStatus;

    // initial state
    private Subject subject;

    private CallbackHandler callbackHandler;

//    private Map sharedState;

//    private Map options;

    // configurable option
    private boolean debug = false;

    // the authentication status
    private boolean succeeded = false;

    private boolean commitSucceeded = false;

    // username and password
    private String username;

    private char[] password;

    // testUser's SamplePrincipal
    private Principal userPrincipal;

    private Vector<Principal> rolePrincipals = null;

    /**
     * Initialize this <code>LoginModule</code>.
     * 
     * <p>
     * 
     * @param subject the <code>Subject</code> to be authenticated.
     *            <p>
     * 
     * @param callbackHandler a <code>CallbackHandler</code> for communicating with the end user (prompting for user names and
     *            passwords, for example).
     *            <p>
     * 
     * @param sharedState shared <code>LoginModule</code> state.
     *            <p>
     * 
     * @param options options specified in the login <code>Configuration</code> for this particular <code>LoginModule</code>.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {

        this.subject = subject;
        this.callbackHandler = callbackHandler;
//        this.sharedState = sharedState;
//        this.options = options;

        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        String uamfile = (String) options.get("uamfile");
        if (debug)
            System.out.println("\t\t[JSTKLoginModule] uamfile = " + uamfile);
        if (uamfile != null) {
            DefaultUAMPersistenceManager pm = new DefaultUAMPersistenceManager(uamfile);
            try {
                uam = UserAccountManager.getInstance(pm);
                initStatus = true;
            } catch (Exception e) {
                initStatus = false;
            }
        } else {
            initStatus = false;
        }
    }

    /**
     * Authenticate the user by prompting for a user name and password.
     * 
     * <p>
     * 
     * @return true in all cases since this <code>LoginModule</code> should not be ignored.
     * 
     * @exception FailedLoginException if the authentication fails.
     *                <p>
     * 
     * @exception LoginException if this <code>LoginModule</code> is unable to perform the authentication.
     */
    public boolean login() throws LoginException {

        if (!initStatus)
            throw new LoginException("Error: JSTKLoginModule initialization failed ");

        // prompt for a user name and password
        if (callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " + "to garner authentication information from the user");

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login: ");
        callbacks[1] = new PasswordCallback("password: ", false);

        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
            if (tmpPassword == null) {
                // treat a NULL password as an empty password
                tmpPassword = new char[0];
            }
            password = new char[tmpPassword.length];
            System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
            ((PasswordCallback) callbacks[1]).clearPassword();

        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() + " not available to garner authentication information " + "from the user");
        }

        // print debugging information
        if (debug) {
            System.out.println("\t\t[JSTKLoginModule] " + "user entered user name: " + username);
            System.out.print("\t\t[JSTKLoginModule] " + "user entered password: ");
            for (int i = 0; i < password.length; i++)
                System.out.print(password[i]);
            System.out.println();
        }

        // verify the username/password
        boolean usernameCorrect = true;
        boolean passwordCorrect = true;
        try {
            uam.validate(username, new String(password));
        } catch (UserAccountManager.NoSuchUserException e) {
            usernameCorrect = false;
        } catch (UserAccountManager.InvalidPasswordException e) {
            passwordCorrect = false;
        }
        if (!usernameCorrect || !passwordCorrect) {
            // authentication failed -- clean out state
            if (debug)
                System.out.println("\t\t[JSTKLoginModule] " + "authentication failed");
            succeeded = false;
            username = null;
            for (int i = 0; i < password.length; i++)
                password[i] = ' ';
            password = null;
            if (!usernameCorrect) {
                throw new FailedLoginException("User Name Incorrect");
            } else {
                throw new FailedLoginException("Password Incorrect");
            }
        }
        if (debug)
            System.out.println("\t\t[SampleLoginModule] " + "authentication succeeded");
        succeeded = true;
        return true;
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication succeeded (the relevant REQUIRED, REQUISITE, SUFFICIENT
     * and OPTIONAL LoginModules succeeded).
     * 
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates a <code>SamplePrincipal</code> with the <code>Subject</code>
     * located in the <code>LoginModule</code>. If this LoginModule's own authentication attempted failed, then this method
     * removes any state that was originally saved.
     * 
     * <p>
     * 
     * @exception LoginException if the commit fails.
     * 
     * @return true if this LoginModule's own login and commit attempts succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException {
        if (succeeded == false) {
            return false;
        } else {
            // add a Principal (authenticated identity)
            // to the Subject

            // assume the user we authenticated is the SamplePrincipal
            userPrincipal = uam.getUser(username);
            if (!subject.getPrincipals().contains(userPrincipal)) {
                subject.getPrincipals().add(userPrincipal);
                if (debug) {
                    System.out.println("\t\t[JSTKLoginModule] " + "added user Principal Subject: " + userPrincipal);
                }
            }
            try {
                Iterator<Principal> itr = uam.userRoles(username);
                rolePrincipals = new Vector<Principal>();
                while (itr.hasNext()) {
                    Principal rolePrincipal = itr.next();
                    if (!subject.getPrincipals().contains(rolePrincipal)) {
                        subject.getPrincipals().add(rolePrincipal);
                        rolePrincipals.add(rolePrincipal);
                        if (debug) {
                            System.out.println("\t\t[JSTKLoginModule] " + "added role Principal to Subject: " + rolePrincipal);
                        }
                    }
                }
            } catch (UserAccountManager.NoSuchUserException e) {
                // Just go on.
            }

            // in any case, clean out state
            username = null;
            for (int i = 0; i < password.length; i++)
                password[i] = ' ';
            password = null;

            commitSucceeded = true;
            return true;
        }
    }

    /**
     * <p>
     * This method is called if the LoginContext's overall authentication failed. (the relevant REQUIRED, REQUISITE, SUFFICIENT
     * and OPTIONAL LoginModules did not succeed).
     * 
     * <p>
     * If this LoginModule's own authentication attempt succeeded (checked by retrieving the private state saved by the
     * <code>login</code> and <code>commit</code> methods), then this method cleans up any state that was originally saved.
     * 
     * <p>
     * 
     * @exception LoginException if the abort fails.
     * 
     * @return false if this LoginModule's own login and/or commit attempts failed, and true otherwise.
     */
    public boolean abort() throws LoginException {
        if (succeeded == false) {
            return false;
        } else if (succeeded == true && commitSucceeded == false) {
            // login succeeded but overall authentication failed
            succeeded = false;
            username = null;
            if (password != null) {
                for (int i = 0; i < password.length; i++)
                    password[i] = ' ';
                password = null;
            }
            userPrincipal = null;
            rolePrincipals = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    /**
     * Logout the user.
     * 
     * <p>
     * This method removes the <code>SamplePrincipal</code> that was added by the <code>commit</code> method.
     * 
     * <p>
     * 
     * @exception LoginException if the logout fails.
     * 
     * @return true in all cases since this <code>LoginModule</code> should not be ignored.
     */
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(userPrincipal);
        Iterator<Principal> itr = rolePrincipals.iterator();
        while (itr.hasNext()) {
            Principal rolePrincipal = itr.next();
            subject.getPrincipals().remove(rolePrincipal);
        }

        succeeded = false;
        succeeded = commitSucceeded;
        username = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++)
                password[i] = ' ';
            password = null;
        }
        userPrincipal = null;
        rolePrincipals = null;
        return true;
    }

    private static void printPrincipals(Subject sub, String label) {
        System.out.println(label);
        if (sub == null)
            return;
        Iterator<Principal> itr = sub.getPrincipals().iterator();
        int index = 0;
        while (itr.hasNext()) {
            System.out.println("Principal# " + index++ + ": " + itr.next());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("------ JSTKLoginModule Test ------");

        LoginContext lc = new LoginContext("Test", new DefaultCallbackHandler());
        printPrincipals(lc.getSubject(), "Subject Principals before login:");
        lc.login();
        printPrincipals(lc.getSubject(), "Subject Principals after login:");
        lc.logout();
        printPrincipals(lc.getSubject(), "Subject Principals after logout:");
    }
}
