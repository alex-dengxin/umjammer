/*
 * @(#) $Id: JSTKAbstractTool.java,v 1.1.1.1 2003/10/05 18:39:10 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public abstract class JSTKAbstractTool {
    protected static Map<String, JSTKCommand> cmds = new HashMap<String, JSTKCommand>(); // Keep it accessible by BenchCommand.

    public String progName() {
        return "java org.jstk..JSTKAbstractTool";
    }

    public String briefDescription() {
        return "should be replaced by concrete tool description";
    }

    public String extendedUsageString() {
        StringBuffer sb = new StringBuffer();
        Iterator<Map.Entry<String, JSTKCommand>> itr = cmds.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, JSTKCommand> ent = itr.next();
            String key = ent.getKey();
            JSTKCommand cmd = ent.getValue();

            sb.append("  " + key);
            int blanksNeeded = 12 - key.length();
            for (int i = 0; i < blanksNeeded; i++)
                sb.append(" ");
            sb.append(cmd.briefDescription() + "\n");
        }
        return sb.toString();
    }

    public void printUsage() {
        System.out.print(usageString());
        System.out.flush();
    }

    public String usageString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Description:: \n  " + briefDescription() + "\n");
        sb.append("Usage:: \n  " + progName() + " <cmd> [<options>]\n");
        sb.append("Commands:: \n");
        sb.append(extendedUsageString() + "\n");
        sb.append("Notes:: \n");
        sb.append("  -- Type \"" + progName() + " <cmd> help\" to get command specific help.\n");
        sb.append("  -- Specify option \"-showtime\" to get command execution time. Example:\n");
        sb.append("       " + progName() + " <command> -showtime\n");
        return sb.toString();
    }

    public void printCmdUsage(JSTKCommand cmd, String cmdString) {
        System.out.print(cmdUsageString(cmd, cmdString));
        System.out.flush();
    }

    public String cmdUsageString(JSTKCommand cmd, String cmdString) {
        StringBuffer sb = new StringBuffer();
        sb.append("Description:: \n  " + cmd.briefDescription() + "\n");
        sb.append("\nUsage:: \n  " + progName() + " " + cmdString + " [<options>]\n\n");

        String[] forms = cmd.useForms();
        if (forms != null) {
            for (int i = 0; i < forms.length; i++) {
                sb.append("  " + progName() + " " + cmdString + " " + forms[i] + "\n");
            }
        }

        sb.append("\nOptions:: \n");
        sb.append(cmd.optionsDescription() + "\n");
        String[] uses = cmd.sampleUses();
        if (uses != null) {
            sb.append("Sample Uses:: \n");
            for (int i = 0; i < uses.length; i++) {
                sb.append("  " + progName() + " " + cmdString + " " + uses[i] + "\n");
            }
        }
        return sb.toString();
    }

    protected int execute(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        if (args.length < 1) { // No argument. Print help message.
            printUsage();
            return 1;
        }
        String cmdString = args[0];
        if (cmdString.equals("-h") || cmdString.equals("help") || cmdString.equals("-?")) {
            printUsage();
            return 1;
        }

        JSTKCommand cmd = cmds.get(cmdString);
        if (cmd == null) { // Unknown command.
            System.out.println("Unknown Command: " + cmdString);
            printUsage();
            return 1;
        }

        if (args.length > 1 && (args[1].equals("-h") || args[1].equals("help") || args[1].equals("-?"))) {
            printCmdUsage(cmd, cmdString);
            return 1;
        }

        opts.parse(args, 1);
        boolean showtime = Boolean.valueOf(opts.get("showtime")).booleanValue();
        long ts = 0, tt = 0;
        if (showtime) {
            ts = System.currentTimeMillis();
        }

        JSTKResult result = (JSTKResult) cmd.execute(opts);

        if (showtime) {
            tt = System.currentTimeMillis() - ts;
        }
        System.out.println(result.getText());
        if (showtime) {
            System.out.println("Execution Time: " + tt / 1000.0 + " secs.");
        }
        return (result.isSuccess() ? 0 : 1);
    }
}
