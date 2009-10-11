/*
 * Copyright (c) 2002 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import vavi.util.Debug;


/**
 * Java version of windiff.
 * <p>
 * TODO JList ‚ð JTable ‚É
 * </p>
 * @author David Drysdale
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 020425 vavi port from <a href="http://www.lurklurk.org/xwindiff.html">xwindiff</a> <br>
 *          0.10 020428 vavi rewrite almost <br>
 *          0.11 021117 vavi performance up <br>
 *          0.12 021222 vavi rescan multiple when outline mode <br>
 *          0.13 021222 vavi set default button <br>
 *          0.14 030128 vavi not use getCanonicalPath <br>
 *          0.15 030129 vavi modify file listing <br>
 *          0.16 030210 vavi add Pair#rescan <br>
 *          0.17 030214 vavi use comparator <br>
 */
public class JWinDiff {

    /** */
    View view;

    /** */
    Model model;

    /** */
    Form form;

    /** */
    Controller controller;

    /**
     * usage: JWinDiff [ldir rdir | lfile rfile | -1 lfile1 [lfile2 ...] -2 rfile1 [rfile2 ...]]
     */
    private JWinDiff(String[] args) throws Exception {

        model = new Model();
        form = new Form();
        view = new View();
        controller = new Controller(model, form);

        // TODO wanna be hide
        SwingController sc = new SwingController(controller, view);
        sc.bind();

        model.addViewListener(view.mainViewListener);
        form.addViewListener(view.mainViewListener);

        // Create the main window and all of the dialog widgets
        controller.initMain(args);
        controller.pageMain();
    }

    /**
     * The program entry point.
     */
    public static void main(String[] args) {
        try {
            new JWinDiff(args);
        } catch (Exception e) {
Debug.printStackTrace(e);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}

/* */
