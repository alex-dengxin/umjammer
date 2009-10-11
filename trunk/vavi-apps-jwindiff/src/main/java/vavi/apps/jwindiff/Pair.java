/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import vavi.util.Debug;
import vavi.util.gnu.Diff;
import vavi.util.gnu.DiffUtil;


/**
 * Pair.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 040612 vavi refactoring <br>
 */
class Pair {

    /** TODO init */
    String rightFilePath;

    /** TODO init */
    String leftFilePath;

    /**
     * 
     */
    enum Type {
        /**  0 ..... */
        NOTYETDIFFED(false),
        /**  1 ....+ */
        DIFFERENT_NOTSURE(true),
        /**  3 ...++ */
        DIFFERENT_BLANKS(true),
        /**  5 ..+.+ */
        DIFFERENT(true),
        /**  8 .+... */
        IDENTICAL(false),
        /** 16 +.... */
        INCOMPARABLE(false);
        /* */
        boolean different;
        /** */
        Type(boolean different) {
            this.different = different;
        }
        /** */
        boolean isDifferent() {
            return different;
        }
    }

    /** */
    File left;

    /** */
    public void setLeft(File left) {
        this.left = left;
    }

    /** */
    public File getLeft() {
        return left;
    }

    /** */
    File right;

    /** */
    public void setRight(File right) {
        this.right = right;
    }

    /** */
    public File getRight() {
        return right;
    }

    /** saved diff results for this pair of files */
    Diff.Change script;

    /** this file has been marked */
    boolean marked;

    /** */
    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    /** */
    public boolean getMarked() {
        return marked;
    }

    /** results of diffing the two files */
    Type diff = Type.NOTYETDIFFED;

    /** */
    public void setDiff(Type diff) {
        this.diff = diff;
    }

    /** */
    public Type getDiff() {
        return diff;
    }

    /** */
    public Pair(String lbase, File left, String rbase, File right) {
        this.leftFilePath = lbase;
        this.left  = left;
        this.rightFilePath = rbase;
        this.right = right;
    }

    /**
     * Rescans the currently selected file.
     */
    public void rescan() {
        rescanInternal();
        quickDiff();
    }

    /** */
    private void rescanInternal() {
        if (left == null) {
            String rf = right.getPath();
            String rt = rightFilePath;
            String lf = rf.substring(rt.length(), rf.length());
            String lt = leftFilePath;
            File file = new File(lt, lf);
            if (file.exists()) {
                left = file;
Debug.println("new left: " + left);
            }
        } else if (right == null) {
            String lt = leftFilePath;
            String lf = left.getPath();
            String rt = rightFilePath;
            String rf = lf.substring(lt.length(), lf.length());
            File file = new File(rt, rf);
            if (file.exists()) {
                right = file;
Debug.println("new right: " + right);
            }
        } else {
            if (!left.exists()) {
                left = null;
Debug.println("disappear left");
            } if (!right.exists()) {
                right = null;
Debug.println("disappear right");
            }
        }
    }

    /** */
    public String getCommonName() {
        try {
            if (right == null) {
                String lt = leftFilePath;
                String lf = left.getPath();
                if (lf.startsWith(lt)) {
                    lf = lf.substring(lt.length());
                    return "." + lf;
                } else {
                    return left.toString();
                }
            } else /* if (left == null) */ {
                String rt = rightFilePath;
                String rf = right.getPath();
                if (rf.startsWith(rt)) {
                    rf = rf.substring(rt.length());
                    return "." + rf;
                } else {
                    return right.toString();
                }
            }
        } catch (NullPointerException e) {
Debug.printStackTrace(e);
            return null;
        }
    }

    /**
     * Compare two files with specified names
     * This is self-contained; it opens the files and closes them.
     * Value is 0 if files are the same, 1 if different,
     * 2 if there is a problem opening them.
     */
    private void compare(boolean ignoreWhiteSpace) throws IOException {

        String[] a;
        String[] b;

        if (ignoreWhiteSpace) {
            a = DiffUtil.readLinesIgnoreWhiteSpace(left );
            b = DiffUtil.readLinesIgnoreWhiteSpace(right);
        }
        else {
            a = DiffUtil.readLines(left );
            b = DiffUtil.readLines(right);
        }

        Diff d = new Diff(a, b);
        script = d.getChange(false);
    }

    /**
     * 
     */
    public void quickDiff() {

        if (left == null || right == null) {
            return;
        }

        // to begin with, allow blank-only diffs to show up
        // regardless of the current mode (for speed)
        try {
            compare(false);
            diff = script == null ? Type.IDENTICAL : Type.DIFFERENT_NOTSURE;
        } catch (IOException e) {
Debug.println(Level.SEVERE, left + " : " + right);
Debug.println(Level.SEVERE, e);
            // TODO binary diff
            if (left.length() == right.length()) {
                diff = Type.IDENTICAL;
            } else {
                diff = Type.DIFFERENT_NOTSURE;
            }
        }
    }

    /**
     * 
     */
    public void slowDiff(boolean isIgnoreBlanks) {
        try {
            compare(isIgnoreBlanks);
            if (script == null) {
                quickDiff();
                if (diff == Type.IDENTICAL) {
                    diff = Type.IDENTICAL;
                } else {
                    diff = Type.DIFFERENT_BLANKS;
                }
            } else {
                diff = Type.DIFFERENT;
            }
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
            diff = Type.INCOMPARABLE;
        }
    }

    /**
     * Determines whether the given pair should currently be visible in outline
     * mode.
     */
    public boolean isVisible(boolean identical, boolean showLeftOnly, boolean showRightOnly, boolean showDifferent, boolean hideMarked) {
        if (diff.equals(Type.IDENTICAL) && !identical) {
            return false;
        }
        if (right == null && !showLeftOnly) {
            return false;
        }
        if (left == null && !showRightOnly) {
            return false;
        }
        if (diff.isDifferent() && !showDifferent) {
            return false;
        }
        if (marked && hideMarked) {
            return false;
        }
        return true;
    }

    /** for debug */
    void debug() {
        System.err.println("---- " + this + " ----");
        System.err.println("left:\t" + left);
        System.err.println("right:\t" + right);
        System.err.println("diff:\t" + diff);
        System.err.println("marked:\t" + marked);
        System.err.println("script:\t" + script);
    }
}

/* */
