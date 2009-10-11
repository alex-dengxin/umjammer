/*
 * @(#) $Id: RepTool.java,v 1.1.1.1 2003/10/05 18:39:16 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.cert.rep;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.jstk.JSTKCommand;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class RepTool {
    public static final Logger logger = Logger.getLogger("org.jstk.cert.rep");

    static Map<String, JSTKCommand> cmds = new HashMap<String, JSTKCommand>(); // Keep it accessible by BenchCommand.
    static {
        cmds.put("add", new AddCommand());
        cmds.put("list", new ListCommand());
    }

    public String progName() {
        String progName = System.getProperty("org.jstk.cert.rep.progname");
        if (progName == null)
            progName = "java org.jstk.cert.rep.RepTool";

        return progName;
    }

    public String briefDescription() {
        return "Repository tool";
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
        System.out.println("Description:: \n  " + briefDescription());
        System.out.println("Usage:: \n  " + progName() + " <cmd> [<options>]");
        System.out.println("Commands:: ");
        System.out.println(extendedUsageString());
        System.out.println("Notes:: ");
        System.out.println("  -- Type \"" + progName() + " <cmd> help\" to get command specific help.");
        System.out.println("  -- Specify option \"-showtime\" to get command execution time. Example:");
        System.out.println("       " + progName() + " genk -showtime");
    }

    public void printCmdUsage(JSTKCommand cmd, String cmdString) {
        System.out.println("Description:: \n  " + cmd.briefDescription());
        System.out.println("\nUsage:: \n  " + progName() + " " + cmdString + " [<options>]\n");

        String[] forms = cmd.useForms();
        if (forms != null) {
            for (int i = 0; i < forms.length; i++) {
                System.out.println("  " + progName() + " " + cmdString + " " + forms[i]);
            }
        }

        System.out.println("\nOptions:: ");
        System.out.println(cmd.optionsDescription());
        String[] uses = cmd.sampleUses();
        if (uses != null) {
            System.out.println("Sample Uses:: ");
            for (int i = 0; i < uses.length; i++) {
                System.out.println("  " + progName() + " " + cmdString + " " + uses[i]);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RepTool rept = new RepTool();
        JSTKOptions opts = new JSTKOptions();
        if (args.length < 1) { // No argument. Print help message.
            rept.printUsage();
            return;
        }
        String cmdString = args[0];
        if (cmdString.equals("-h") || cmdString.equals("help") || cmdString.equals("-?")) {
            rept.printUsage();
            return;
        }

        JSTKCommand cmd = cmds.get(cmdString);
        if (cmd == null) { // Unknown command.
            System.out.println("Unknown Command: " + cmdString);
            rept.printUsage();
            return;
        }

        if (args.length > 1 && (args[1].equals("-h") || args[1].equals("help") || args[1].equals("-?"))) {
            rept.printCmdUsage(cmd, cmdString);
            return;
        }

        opts.parse(args, 1);
        boolean showtime = Boolean.valueOf(opts.get("showtime")).booleanValue();
        long ts = 0, tt = 0;
        if (showtime)
            ts = System.currentTimeMillis();

        JSTKResult result = (JSTKResult) cmd.execute(opts);

        if (showtime)
            tt = System.currentTimeMillis() - ts;
        System.out.println(result.getText());
        if (showtime)
            System.out.println("Execution Time: " + tt / 1000.0 + " secs.");
        System.exit(result.isSuccess() ? 0 : 1);
    }
}
