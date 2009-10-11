/*
 * @(#) $Id: JSTKShellServer.java,v 1.1.1.1 2003/10/05 18:39:20 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.jstksh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.jstk.JSTKAbstractTool;
import org.jstk.JSTKArgs;
import org.jstk.JSTKCommand;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKQuitException;
import org.jstk.JSTKResult;


public class JSTKShellServer extends JSTKAbstractTool implements JSTKShell {
//    private String homeDir = null;

//    private String curDir = null;

    private static int curId = -1;

    private static Map<String, SessionInfo> sessionTable = new HashMap<String, SessionInfo>();

    static class SessionInfo {
        String homeDir = null;

        String curDir = null;

        public SessionInfo() {
            homeDir = System.getProperty("user.dir");
            curDir = homeDir;
        }
    }

    static class EchoCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            StringBuffer msg = new StringBuffer();
            for (int i = 0; i < args.getNum(); i++) {
                msg.append(args.get(i) + " ");
            }
            return new JSTKResult(null, true, msg.toString());
        }
    }

    static class QuitCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            throw new JSTKQuitException();
        }
    }

    static class PWDCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            if (curDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");
            return new JSTKResult(null, true, curDir);
        }
    }

    static class LSCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            if (curDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");

            String targetDir = curDir;
            File file = new File(targetDir);
            if (!file.isDirectory())
                return new JSTKResult(null, false, "Not a directory: " + targetDir);
            String[] fnames = file.list();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < fnames.length; i++) {
                sb.append(fnames[i] + "\n");
            }
            return new JSTKResult(null, true, sb.toString());
        }
    }

    static class CDCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            String homeDir = getCurDir(args);
            if (curDir == null || homeDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");

            String targetDir = homeDir;
            if (args.getNum() > 1)
                return new JSTKResult(null, false, "Specify only one target directory.");
            else if (args.getNum() == 1)
                targetDir = args.get(0);

            try {
                File file = createFile(targetDir, curDir);
                if (!file.isDirectory())
                    return new JSTKResult(null, false, "Not a directory: " + targetDir);
                curDir = file.getCanonicalPath();
                setCurDir(args, curDir);
            } catch (IOException ioe) {
                throw new JSTKException("cd failed", ioe);
            }
            return new JSTKResult(null, true, "Changed Directory To: " + curDir);
        }
    }

    static class MkdirCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            if (curDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");

            String targetDir = null;
            if (args.getNum() > 1)
                return new JSTKResult(null, false, "Specify only one directory name.");
            else if (args.getNum() == 1)
                targetDir = args.get(0);
            else
                return new JSTKResult(null, false, "No directory name specified.");

            boolean result = false;
            try {
                File file = createFile(targetDir, curDir);
                if (file.exists())
                    return new JSTKResult(null, false, "File or directory exists: " + targetDir);

                result = file.mkdirs();
            } catch (IOException ioe) {
                throw new JSTKException("mkdir failed", ioe);
            }

            if (result)
                return new JSTKResult(null, true, "Directory Created: " + targetDir);
            else
                return new JSTKResult(null, false, "Cannot create directory: " + targetDir);
        }
    }

    static class RMCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            if (curDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");

            String targetFile = null;
            if (args.getNum() > 1)
                return new JSTKResult(null, false, "Specify only one target.");
            else if (args.getNum() == 1)
                targetFile = args.get(0);
            else
                return new JSTKResult(null, false, "No target specified.");

            boolean result = false;
            try {
                File file = createFile(targetFile, curDir);
                if (!file.exists())
                    return new JSTKResult(null, false, "Target doesn't exist: " + targetFile);

                result = file.delete();
            } catch (IOException ioe) {
                return new JSTKResult(null, false, "rm failed : " + targetFile + ", Exception: " + ioe);
            }

            if (result)
                return new JSTKResult(null, true, "Target removed: " + targetFile);
            else
                return new JSTKResult(null, false, "Target ccould not be removed: " + targetFile);
        }
    }

    static class CatCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            if (curDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");

            String targetFile = null;
            if (args.getNum() > 1)
                return new JSTKResult(null, false, "Specify only one target.");
            else if (args.getNum() == 1)
                targetFile = args.get(0);
            else
                return new JSTKResult(null, false, "No target specified.");

            try {
                File file = createFile(targetFile, curDir);
                if (!file.exists())
                    return new JSTKResult(null, false, "Target doesn't exist: " + targetFile);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                int ch;
                while ((ch = bis.read()) != -1)
                    baos.write(ch);

                return new JSTKResult(null, true, baos.toString());
            } catch (IOException ioe) {
                throw new JSTKException("cat failed", ioe);
            }
        }
    }

    static class CpCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String curDir = getCurDir(args);
            if (curDir == null)
                return new JSTKResult(null, true, "Session Info. missing or invalid.");

            String srcFile = null;
            String dstFile = null;
            if (args.getNum() > 2)
                return new JSTKResult(null, false, "Too many arguments.");
            else if (args.getNum() == 2) {
                srcFile = args.get(0);
                dstFile = args.get(1);
            } else
                return new JSTKResult(null, false, "Insufficient arguments.");

            try {
                File sFile = createFile(srcFile, curDir);
                if (!sFile.exists())
                    return new JSTKResult(null, false, "Source doesn't exist: " + srcFile);

                File dFile = createFile(dstFile, curDir);
                if (dFile.exists())
                    return new JSTKResult(null, false, "Destination exists: " + dstFile);

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dFile));
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sFile));
                int ch;
                while ((ch = bis.read()) != -1)
                    bos.write(ch);

                bis.close();
                bos.close();

                return new JSTKResult(null, true, "cp: " + srcFile + " --> " + dstFile);
            } catch (IOException ioe) {
                throw new JSTKException("cp failed", ioe);
            }
        }
    }

    static class ShowCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            String target = "pdomain";
            if (args.getNum() > 0)
                target = args.get(0);

            Class<?> cls = ShowCommand.class;
            ProtectionDomain pDomain = cls.getProtectionDomain();
            if (target.equalsIgnoreCase("pdom")) {
                StringBuffer sb = new StringBuffer();
                CodeSource cs = pDomain.getCodeSource();
                sb.append("CodeSource: " + cs.toString() + "\n");
                Principal[] principals = pDomain.getPrincipals();
                if (principals != null) {
                    for (int i = 0; i < principals.length; i++) {
                        sb.append("Principal[" + i + "]: " + principals[i] + "\n");
                    }
                }
                PermissionCollection permsColl = pDomain.getPermissions();
                sb.append("Permissions: " + permsColl.toString());
                return new JSTKResult(null, true, sb.toString());
            } else if (target.equalsIgnoreCase("perms")) {
                CodeSource cs = pDomain.getCodeSource();
                PermissionCollection permsColl = Policy.getPolicy().getPermissions(cs);
                return new JSTKResult(null, true, permsColl.toString());
            } else if (target.equalsIgnoreCase("cs")) {
                CodeSource cs = pDomain.getCodeSource();
                return new JSTKResult(null, true, cs.toString());
            } else if (target.equalsIgnoreCase("classloaders")) {
                StringBuffer sb = new StringBuffer();
                ClassLoader cl = cls.getClassLoader();
                int idx = 0;
                while (cl != null) {
                    sb.append("[" + idx + "]ClassLoader: " + cl + "\n");
                    cl = cl.getParent();
                    ++idx;
                }
                return new JSTKResult(null, true, sb.toString());
            }
            return new JSTKResult(null, false, "unknown target: " + target);
        }
    }

    static class WhoAmICommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            AccessControlContext acc = AccessController.getContext();
            Subject sub = Subject.getSubject(acc);
            Object[] principals = sub.getPrincipals().toArray();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < principals.length; i++)
                sb.append("[" + i + "]" + principals[i].toString());

            return new JSTKResult(null, true, sb.toString());
        }
    }

    static class TimeCommand extends JSTKCommandAdapter {
        public Object execute(JSTKArgs args) throws JSTKException {
            try {
                int loopcount = 1000;
                String loopcountS = args.get("loopcount");
                if (loopcountS != null)
                    loopcount = Integer.parseInt(loopcountS);
                if (args.getNum() < 1)
                    return new JSTKResult(null, false, "No command specified.");

                // Setup command for execution in loop.
                String cmdString = args.get(0);
                String[] cmdargs = new String[2 + args.getNum() - 1];
                cmdargs[0] = "-sessionid";
                cmdargs[1] = args.get("sessionid");
                for (int i = 1; i < args.getNum(); i++) {
                    cmdargs[i + 1] = args.get(i);
                }

                JSTKCommand cmd = cmds.get(cmdString);
                if (cmd == null) // Unknown command.
                    return new JSTKResult(null, false, "Unknown Command: " + cmdString);

                JSTKOptions opts = new JSTKOptions();
                opts.parse(cmdargs, 0);
                JSTKResult result = (JSTKResult) cmd.execute(opts);

                long ts = System.currentTimeMillis();
                for (int i = 0; i < loopcount; i++)
                    cmd.execute(opts);
                long te = System.currentTimeMillis();

                StringBuffer sb = new StringBuffer();
                sb.append(result.getText() + "\n");
                sb.append("Elapsed Time for " + loopcount + " invocations: " + (te - ts) + " millisecs.");
                return new JSTKResult(null, true, sb.toString());
            } catch (Exception e) {
                return new JSTKResult(null, false, "time failed. Exception: " + e);
            }
        }
    }

    public static File createFile(String name, String curDir) throws IOException {
        File f = new File(name);
        if (!name.equalsIgnoreCase(f.getCanonicalPath())) // name not absolute.
            f = new File(curDir, name);
        return f;
    }

    public static String getCurDir(JSTKArgs args) {
        String sessId = args.get("sessionid");
        if (sessId == null)
            return null;
        SessionInfo sessInfo = sessionTable.get(sessId);
        if (sessInfo == null)
            return null;
        return sessInfo.curDir;
    }

    public static void setCurDir(JSTKArgs args, String curDir) {
        String sessId = args.get("sessionid");
        if (sessId == null)
            return;
        SessionInfo sessInfo = sessionTable.get(sessId);
        if (sessInfo == null)
            return;
        sessInfo.curDir = curDir;
    }

    public static String getHomeDir(JSTKArgs args) {
        String sessId = args.get("sessionid");
        if (sessId == null)
            return null;
        SessionInfo sessInfo = sessionTable.get(sessId);
        if (sessInfo == null)
            return null;
        return sessInfo.homeDir;
    }

    public static final Logger logger = Logger.getLogger("org.jstk.access");
    static {
        cmds.put("echo", new EchoCommand());
        cmds.put("pwd", new PWDCommand());
        cmds.put("ls", new LSCommand());
        cmds.put("cd", new CDCommand());
        cmds.put("md", new MkdirCommand());
        cmds.put("rm", new RMCommand());
        cmds.put("cat", new CatCommand());
        cmds.put("cp", new CpCommand());
        cmds.put("show", new ShowCommand());
        cmds.put("whoami", new WhoAmICommand());
        cmds.put("time", new TimeCommand());
        cmds.put("quit", new QuitCommand());
        cmds.put("exit", new QuitCommand());
    }

    public JSTKShellServer() {
        super();

    }

    public String progName() {
        String progName = "java org.jstk.access.JSTKShell";
        return progName;
    }

    public String briefDescription() {
        return "a minimal shell to demostrate Java Access Control features";
    }

    public void setSubject(Subject sub) {
        // Do nothing.
    }

    public String createSession() throws Exception {
        String sessId = "sess_" + (++curId);
        SessionInfo sessInfo = new SessionInfo();
        sessionTable.put(sessId, sessInfo);
        return sessId;
    }

    public void destroySession(String sessId) throws Exception {
        sessionTable.remove(sessId);
    }

    public String execCommand(String[] args) throws Exception {
        try {
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
        } catch (SecurityException se) {
            return "Security Violation: " + se;
        }
    }
}
