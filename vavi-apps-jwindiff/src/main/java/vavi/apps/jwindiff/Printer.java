/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vavi.apps.jwindiff.Model.ShowExpandMode;
import vavi.apps.jwindiff.Model.ShowNumMode;
import vavi.util.gnu.Diff;
import vavi.util.gnu.DiffUtil;


/**
 * Printer.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040612 vavi refactoring <br>
 */
class Printer extends DiffUtil.BasicPrinter {

    /** */
    private Model model;

    /** */
    public Printer(Model model) throws IOException {
        super(DiffUtil.readLines(model.current.left), DiffUtil.readLines(model.current.right));
        this.model = model;
    }

    /**
     * Print a hunk of a normal diff.
     * This is a contiguous portion of a complete edit script,
     * describing changes in consecutive lines.
     */
    protected void printHunk(Diff.Change hunk) {

        // Determine range of line numbers involved in each file.
        analyzeHunk(hunk);
        if (!hasDiff()) {
            return;
        }

        if (model.getShowExpandMode().equals(ShowExpandMode.left) || model.getShowNumMode().equals(ShowNumMode.left)) {
            insertRight();
        } else {
            insertLeft();
        }
    }

    /** */
    private void insertLeft() {
        // Print the lines that the second file has.
        if (hasInserts() && !model.getShowExpandMode().equals(ShowExpandMode.left)) {
            for (int i = getStartInserts(); i <= getLastInserts(); i++) {
                listModel.get(i + inserted).setFlag(Line.Type.INSERTED);
            }
        }

        // Print the lines that the first file has.
        if (hasDeletes() && !model.getShowExpandMode().equals(ShowExpandMode.right)) {
            int c = 0;
            for (int i = getStartDeletes(); i <= getLastDeletes(); i++) {
                Line line = new Line(i, (String) file0[i], Line.Type.DELETED);
                listModel.add(getStartInserts() + c + inserted, line);
                c++;
            }
            inserted += c;
        }
    }

    /** */
    private void insertRight() {
        // Print the lines that the first file has.
        if (hasDeletes() && !model.getShowExpandMode().equals(ShowExpandMode.right)) {
            for (int i = getStartDeletes(); i <= getLastDeletes(); i++) {
                listModel.get(i + inserted).setFlag(Line.Type.DELETED);
            }
        }

        // Print the lines that the second file has.
        if (hasInserts() && !model.getShowExpandMode().equals(ShowExpandMode.left)) {
            int c = 0;
            for (int i = getStartInserts(); i <= getLastInserts(); i++) {
                Line line = new Line(i, (String) file1[i], Line.Type.INSERTED);
                listModel.add(getStartDeletes() + 1 + c + inserted, line);
                c++;
            }
            inserted += c;
        }
    }

    /** */
    private int inserted;

    /** */
    private List<Line> listModel;

    /** */
    private void fillRight() {
        for (int i = 0; i < file1.length; i++) {
            listModel.add(new Line(i, (String) file1[i], Line.Type.PLAIN));
        }
    }

    /** */
    private void fillLeft() {
        for (int i = 0; i < file0.length; i++) {
            listModel.add(new Line(i, (String) file0[i], Line.Type.PLAIN));
        }
    }

    /**
     * Divide SCRIPT into pieces by calling HUNKFUN and
     * print each piece with PRINTFUN.
     * Both functions take one arg, an edit script.
     *
     * PRINTFUN takes a subscript which belongs together (with a null
     * link at the end) and prints it.
     */
    public void print(Diff.Change script) {

        // Target is mainView, not #out
        listModel = new ArrayList<Line>();
        inserted = 0;

        if (model.getShowExpandMode().equals(ShowExpandMode.left) || model.getShowNumMode().equals(ShowNumMode.left)) {
            fillLeft();
        } else {
            fillRight();
        }

        //
        Diff.Change next = script;

        while (next != null) {
            Diff.Change t, end;

            // Find a set of changes that belong together.
            t = next;
            end = hunkfun(next);

            // Disconnect them from the rest of the changes,
            // making them a hunk, and remember the rest
            // for next iteration.
            next = end.link;
            end.link = null;

            // Print this hunk.
            printHunk(t);

            // Reconnect the script so it will all be freed properly.
            end.link = next;
        }
    }

    /** */
    public List<Line> getResult() {
        return listModel;
    }
}

/* */
