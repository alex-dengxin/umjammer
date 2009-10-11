/*
 * @(#) $Id: BenchCommand.java,v 1.1.1.1 2003/10/05 18:39:16 pankaj_kumar Exp $
 *
 * Copyright (c) 2002-03 by Pankaj Kumar (http://www.pankaj-k.net). 
 * All rights reserved.
 *
 * The license governing the use of this file can be found in the 
 * root directory of the containing software.
 */

package org.jstk.crypt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.jstk.JSTKArgs;
import org.jstk.JSTKCommand;
import org.jstk.JSTKCommandAdapter;
import org.jstk.JSTKException;
import org.jstk.JSTKOptions;
import org.jstk.JSTKResult;


public class BenchCommand extends JSTKCommandAdapter {
    private static Map<String, String> defaults = new HashMap<String, String>();
    static {
        defaults.put("cmdfile", "bin/ctbench.cmds");
        defaults.put("runcount", "1");
        defaults.put("warmuptime", "60");
        defaults.put("loopcount", "0");
        defaults.put("minimize_et", "true");
    }

    public String briefDescription() {
        return "benchmarks crypttool commands";
    }

    public String optionsDescription() {
        return "  -cmdfile <file>     : read commands from this file.\n" + "  -warmuptime <wt>    : minimum JVM warmup time in seconds.\n" + "  -runcount <rc>      : how many runs?.\n" + "  -minmize_et         : compute loopcount to minimize execution time.\n" + "  -loopcount <lc>     : how many loops for each command within a run.\n";
    }

    public String[] useForms() {
        String[] useForms = {
            "[-provider <provider>] [-info] [-props] [-csinfo]"
        };
        return useForms;
    }

    public String[] sampleUses() {
        String[] uses = {
            "", "-cmdfile test.cmds", "-warmuptime 900 -minimize_et", "-runcount 5 -loopcount 5"
        };
        return uses;
    }

    private String[][] parseCmdFile(String cmdfile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cmdfile)));

        // Create and initialize the tokenizer so that it can read the command script file.
        StreamTokenizer st = new StreamTokenizer(br);
        st.resetSyntax();
        st.whitespaceChars('\u0000', '\u0020');
        st.wordChars('!', '~');
        st.commentChar('#');
        st.quoteChar('"');
        st.slashSlashComments(true);
        st.slashStarComments(true);
        st.eolIsSignificant(true);

        Vector<String[]> cmdLines = new Vector<String[]>();
        Vector<String> cmdWords = null;
        int tok;
        do {
            tok = st.nextToken();
            if (tok == StreamTokenizer.TT_WORD) {
                if (cmdWords == null)
                    cmdWords = new Vector<String>();
                cmdWords.add(st.sval);
            } else if ((tok == StreamTokenizer.TT_EOL) || (tok == StreamTokenizer.TT_EOF)) {
                if (cmdWords == null)
                    continue;
                String[] cmdArgs = new String[cmdWords.size()];
                for (int i = 0; i < cmdWords.size(); i++)
                    cmdArgs[i] = cmdWords.elementAt(i);
                cmdWords = null;
                cmdLines.add(cmdArgs);
            }
        } while (tok != StreamTokenizer.TT_EOF);
        String[][] cmds = new String[cmdLines.size()][];
        for (int i = 0; i < cmdLines.size(); i++)
            cmds[i] = cmdLines.elementAt(i);
        return cmds;
    }

    public Object execute(JSTKArgs args) throws JSTKException {
        try {
            args.setDefaults(defaults);
            String cmdfile = args.get("cmdfile");
//          boolean minimize_et = Boolean.valueOf(args.get("minimize_et")).booleanValue();
            int warmuptime = Integer.parseInt(args.get("warmuptime"));
            int runcount = Integer.parseInt(args.get("runcount"));
            int loopcount = Integer.parseInt(args.get("loopcount"));

            // Parse the commands script file.
            System.out.print("Parsing the command file \"" + cmdfile + "\" ... ");
            String[][] cmdLines = parseCmdFile(cmdfile);
            System.out.println(" ... done.");

            JSTKCommand[] cmds = new JSTKCommand[cmdLines.length];
            JSTKOptions[] optsArray = new JSTKOptions[cmdLines.length];
            long[] cmdExecTimeArray = new long[cmdLines.length];
            int[] loopcountArray = new int[cmdLines.length];

            // Intialize JSTKCommand and JSTKOption objects.
            System.out.print("Intializing ... ");
            for (int i = 0; i < cmds.length; i++) {
                String cmdString = cmdLines[i][0];
                cmds[i] = CryptTool.cmds.get(cmdString);
                if (cmds[i] == null) { // Unknown command.
                    return new JSTKResult(null, false, "Unknown Command: " + cmdString + ". Aborting ...");
                }
                optsArray[i] = new JSTKOptions();
                optsArray[i].parse(cmdLines[i], 1);
            }
            System.out.println(" ... done.");

            // Validation round.
            System.out.println("Validating arguments ... ");
            for (int i = 0; i < cmds.length; i++) {
                System.out.print("Running command[" + i + "]:");
                for (int j = 0; j < cmdLines[i].length; j++)
                    System.out.print(" " + cmdLines[i][j]);
                System.out.print(" ... ");

                JSTKResult res = (JSTKResult) cmds[i].execute(optsArray[i]);

                if (!res.isSuccess()) {
                    return new JSTKResult(null, false, "Command execution failed: " + cmds[i] + ". reason: " + res.getText() + ". Aborting ...");
                } else {
                    System.out.println(" ... succeeded.");
                    System.out.println("Result: " + res.getText());
                }
            }
            System.out.println(" ... done.");

            // Caliberation round.
            System.out.println("Caliberating ... ");
            for (int i = 0; i < cmds.length; i++) {
                long st = System.currentTimeMillis();
                /*JSTKResult res = (JSTKResult)*/ cmds[i].execute(optsArray[i]);
                cmdExecTimeArray[i] = System.currentTimeMillis() - st;
                System.out.println("Execution Time[" + i + "]: " + cmdExecTimeArray[i] + " milli secs.");
            }
            System.out.println(" ... done.");

            // Warmup round.
            System.out.println("Estimated warmup Time: " + warmuptime + " secs.");
            System.out.println("Warming up ... ");
            long st = System.currentTimeMillis();
            for (int i = 0; i < cmds.length; i++) {
                long timeForCmd = (long) (warmuptime * 1000.0) / cmds.length;
                if (cmdExecTimeArray[i] == 0) {
                    loopcountArray[i] = 100;
                } else {
                    loopcountArray[i] = (int) (timeForCmd / cmdExecTimeArray[i]);
                }

                for (int l = 0; l < loopcountArray[i]; l++) {
                    JSTKResult res = (JSTKResult) cmds[i].execute(optsArray[i]);
                    if (!res.isSuccess()) {
                        return new JSTKResult(null, false, "Command execution failed: " + cmds[i] + ". reason: " + res.getText() + ". Aborting ...");
                    }
                }
            }
            long et = System.currentTimeMillis() - st;
            System.out.println(" ... done.");
            System.out.println("Actual warmup Time: " + (et / 1000.0) + " secs.");

            // Measurement Round
            System.out.println("Measuring ... ");
            for (int r = 0; r < runcount; r++) {
                System.out.println("Round# ::" + r);
                for (int i = 0; i < cmds.length; i++) {
                    long timeForCmd = 6000; // Each command should run for at least 6 secs.
                    if (loopcount != 0) {
                        loopcountArray[i] = loopcount;
                    } else if (cmdExecTimeArray[i] == 0) {
                        loopcountArray[i] = 1000; // Fixed loopcount
                    } else {
                        loopcountArray[i] = (int) (timeForCmd / cmdExecTimeArray[i]) + 1;
                    }
                    System.out.println("loopcountArray[" + i + "] = " + loopcountArray[i]);
                    cmds[i].getPerfData().reset();
                    long st0 = System.currentTimeMillis();
                    for (int l = 0; l < loopcountArray[i]; l++) {
                        JSTKResult res = (JSTKResult) cmds[i].execute(optsArray[i]);
                        if (!res.isSuccess()) {
                            return new JSTKResult(null, false, "Command execution failed: " + cmds[i] + ". reason: " + res.getText() + ". Aborting ...");
                        }
                    }
                    cmdExecTimeArray[i] = System.currentTimeMillis() - st0;

                    System.out.println("Cmd# ::" + i + ", Loops: " + loopcountArray[i] + ", Tot. Time: " + cmdExecTimeArray[i] + " ms." + ", Avg. Time: " + ((double) cmdExecTimeArray[i] / loopcountArray[i]) + " ms.");
                    cmds[i].getPerfData().store(System.out);
                }
            }

            return new JSTKResult(null, true, "done");
        } catch (Exception exc) {
            throw new JSTKException("BenchCommand execution failed", exc);
        }
    }

    public static void main(String[] args) throws Exception {
        JSTKOptions opts = new JSTKOptions();
        opts.parse(args, 0);
        BenchCommand benchCmd = new BenchCommand();
        JSTKResult result = (JSTKResult) benchCmd.execute(opts);
        System.out.println(result.getText());
    }
}
