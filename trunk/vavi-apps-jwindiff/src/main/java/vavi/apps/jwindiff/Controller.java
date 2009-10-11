/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.DefaultListModel;

import vavi.apps.jwindiff.Model.DisplayMode;
import vavi.apps.jwindiff.Model.ShowExpandMode;
import vavi.apps.jwindiff.Model.ShowNumMode;
import vavi.util.Debug;
import vavi.util.gnu.DiffUtil;


/**
 * Controller. 
 * アプリケーションそのもの。View を除いたもの。
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060726 nsano initial version <br>
 */
class Controller {

    /** */
    private static final ResourceBundle rb = ResourceBundle.getBundle("JWinDiffResources", Locale.getDefault());

    enum Order {
        Ascent,
        Descent
    }

    //----

    /** モデル */
    Model model;

    /** 遷移モデル */
    Form form;

    //----

    /** */
    Controller(Model model, Form form) {
        this.model = model;
        this.form = form;
    }

    //----

    /**
     * Rescans the currently selected file.
     * @controller
     */
    void rescan(Pair[] pairs) {
        form.setTitle(rb.getString("frame.title.scanning"));
        if (model.displayMode != Model.DisplayMode.OUTLINE_MODE) {
            model.current.rescan();
        } else {
            model.rescan(pairs);
        }
        finishWork();
        update();
    }

    /**
     * Display outline.
     * @controller
     */
    void updateOutline() {

// Debug.println(displayMode);
        if (model.displayMode != Model.DisplayMode.OUTLINE_MODE) {
            return;
        }

        form.redisplayOutlineBefore(model.getLeftFilePath(), model.getRightFilePath());

        DefaultListModel listModel = new DefaultListModel();

        // set the current visibility of each pair
        for (int i = 0; i < model.pairs.size(); i++) {
            Pair pair = model.pairs.get(i);
            if (pair.isVisible(model.isShowIdentical(), model.isShowLeft(), model.isShowRight(), model.isShowDifferent(), model.isHideMarked())) {
                if (pair.getDiff() == Pair.Type.NOTYETDIFFED) {
                    pair.quickDiff();
                }
// pair.debug();
                listModel.addElement(pair);
            }
        }

        form.redisplayOutlineAfter(listModel, model.current);
    }

    /**
     * Display expanded.
     * @controller
     */
    void updateExpanded() {

        if (model.displayMode != Model.DisplayMode.EXPANDED_MODE) {
            return;
        }

        form.redisplayExpandedBefore();

        try {
            if (model.current.getRight() == null) {
Debug.println("here1");
                form.setNames(model.current.getCommonName());
                displaySingleFile(model.current.getLeft());
                form.setNames(model.current.getCommonName());
            } else if (model.current.getLeft() == null) {
Debug.println("here2");
                form.setNames(model.current.getCommonName());
                displaySingleFile(model.current.getRight());
                form.setNames(model.current.getCommonName());
            } else if ((model.current.getDiff() == Pair.Type.NOTYETDIFFED) || (model.current.getDiff() == Pair.Type.DIFFERENT_NOTSURE)) {
Debug.println("here3");
                // diff both (regular) files,
                // and show the newly obtained results
                if (model.isMultiMode()) {
                    form.setNames(model.current.getCommonName());
                    form.setPaths(model.getLeftFilePath() + " : " + model.getRightFilePath());
                } else {
                    form.setNames(model.current.getLeft().getName() + " : " + model.current.getRight().getName());
                    form.setPaths(model.getLeftFilePath() + " : " + model.getRightFilePath());
                }
                model.current.slowDiff(model.isIgnoreBlanks());
model.current.debug();
                Printer printer = new Printer(model);
                printer.print(model.current.script);
                model.viewUpdated(this, "updateMain", printer.getResult());
            } else if (model.current.getDiff() == Pair.Type.IDENTICAL) {
Debug.println("here4");
                displaySingleFile(model.current.getLeft());
                form.setNames(model.current.getCommonName());
            } else {
Debug.println("here5");
                // redisplay known diff results.
                Printer printer = new Printer(model);
                printer.print(model.current.script);
                model.viewUpdated(this, "updateMain", printer.getResult());
            }
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
            form.displayException(e);
        }

        form.updateGraphics();
    }

    /**
     * Display as selected mode.
     * @controller
     */
    void update() {
// Debug.println(displayMode);
        if (model.displayMode == Model.DisplayMode.OUTLINE_MODE) {
            updateOutline();
        } else {
            updateExpanded();
        }
    }

    /**
     * form
     */
    void displaySingleFile(File file) throws IOException {
// Debug.println("here");
        String lines[] = DiffUtil.readLines(file);
        form.displaySingleFile(lines);
    }

    /**
     * form
     */
    void toExpand() {
        if (model.displayMode == Model.DisplayMode.EXPANDED_MODE) {
            form.toExpand();
            return;
        }
        if (model.current == null) {
            form.toOutline();
            return;
        }
        if (!model.current.isVisible(model.isShowIdentical(), model.isShowLeft(), model.isShowRight(), model.isShowDifferent(), model.isHideMarked())) {
Debug.println("here");
model.current.debug();
            form.toOutline();
            return;
        }
        model.displayMode = Model.DisplayMode.EXPANDED_MODE;
        updateExpanded();
        form.toSelection();
    }

    /**
     * @controller
     */
    void toOutline() {
        if (model.displayMode == Model.DisplayMode.OUTLINE_MODE) {
            form.toOutline();
            return;
        }
        model.displayMode = Model.DisplayMode.OUTLINE_MODE;
        updateOutline();
    }

    /** The configuration file TODO model??? */
    private static File configFile = new File(System.getProperty("user.home") + File.separator + ".jwindiff");

    /** Loads properties */
    private void loadOptions() {

        // DI properties -> model
        try {
            model.deserialize(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
System.err.println(e);
            saveOptions();
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
        }
    }

    /**
     * Writes out all of the configuration options.
     */
    private void saveOptions() {
        try {
            model.serialize(new FileOutputStream(configFile));
        } catch (IOException e) {
Debug.println(Level.SEVERE, e);
        }
    }

    /**
     * @controller unused
     */
    void clearPatternHistory() {
        model.patterns.clear();
    }

    /**
     * @controller
     */
    void addPatternHistory(String value) {
        value = value == null ? "" : String.valueOf(value);
        model.patterns.add(value);
    }

    /**
     * @controller
     */
    void serializePairs(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            model.serializePairs(os);
            os.close();
        } catch (IOException e) {
Debug.printStackTrace(e);
        }
    }

    /**
     * @controller
     */
    void updateTargets(File left, File right) {
        form.setTitle(rb.getString("frame.title.scanning"));

        model.getLeftFiles().clear();
        model.getRightFiles().clear();

        try {
            model.getLeftFiles().add(left.getCanonicalFile());
            model.getRightFiles().add(right.getCanonicalFile());
        } catch (IOException e) {
Debug.println(e);
        }

        model.updateTargets();

        finishWork();
    }

    /**
     * @controller
     */
    void setEditor(String editor) {
        model.editor = editor;
    }

    /**
     * expand
     * @controller
     */
    void setShowExpandMode(String name) {
        model.setShowExpandMode(ShowExpandMode.valueOf(name));
        updateExpanded();
    }

    /**
     * expand
     * @controller
     */
    void setShowNumMode(String name) {
        model.setShowNumMode(ShowNumMode.valueOf(name));
        updateExpanded();
    }

    /**
     * option
     * @controller
     */
    void setIgnoreBlanks(boolean ignoreBlanks) {
        model.ignoreBlanks = ignoreBlanks;
    }

    /**
     * option
     * @controller
     */
    void setShowIdentical(boolean showIdentical) {
        model.setShowIdentical(showIdentical);
        updateOutline();
    }

    /**
     * option
     * @controller
     */
    void setShowDifferent(boolean showDifferent) {
        model.setShowDifferent(showDifferent);
        updateOutline();
    }

    /** option */
    void setShowLeft(boolean showLeft) {
        model.setShowLeft(showLeft);
        updateOutline();
    }

    /** option */
    void setShowRight(boolean showRight) {
        model.setShowRight(showRight);
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void setHideMarked(boolean hideMarked) {
        model.setHideMarked(hideMarked);
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void toggleAllMarks() {
        model.toggleAllMarks();
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void toggleSelectedMark(Pair[] selected) {
        model.toggleSelectedMark(selected);
        updateOutline();
    }

    /**
     * mark
     * @controller
     */
    void markRegex(String regex) {
        model.markRegex(regex);
        updateOutline();
    }

    //----

    /** form */
    void setCurrent(Pair pair) {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            model.current = pair;
        }
    }

    /** form */
    void initMain(String[] args) throws IOException {
        // Get the targets from the command line arguments now that Xt has
        // removed its options
        if (args.length == 1) {
            throw new IllegalArgumentException(rb.getString("message.usage"));
        }

        for (int i = 0; i < args.length; i++) {
            if ("-1".equals(args[i])) {
                i++;
                while (i < args.length) {
                    File file = new File(args[i]);
                    if (file.exists()) {
                        file = new File(file.getCanonicalPath());
                        model.getLeftFiles().add(file);
Debug.println(i + ": add left: " + file);
                    } else {
                        continue;
                    }
                    i++;
                }
            } else if ("-2".equals(args[i])) {
                i++;
                while (i < args.length) {
                    File file = new File(args[i]);
                    if (file.exists()) {
                        file = new File(file.getCanonicalPath());
                        model.getRightFiles().add(file);
Debug.println(i + ": add right: " + file);
                    } else {
                        continue;
                    }
                    i++;
                }
            } else {
                File file = new File(args[i]);
Debug.println("file: " + file);
                if (file.exists()) {
                    if (i == 0) {
                        file = new File(file.getCanonicalPath());
                        model.getLeftFiles().add(file);
Debug.println(i + ": add left: " + file);
                    } else if (i == 1) {
                        File left = model.getLeftFiles().get(0);
                        if (left.isDirectory() && file.isDirectory()) {
                            file = new File(file.getCanonicalPath());
                            model.getRightFiles().add(file);
Debug.println(i + ": add right as directory: " + file);
                        } else if (!left.isDirectory() && !file.isDirectory()) {
                            file = new File(file.getCanonicalPath());
                            model.getRightFiles().add(file);
Debug.println(i + ": add right as file: " + file);
                        } else {
                            String s = MessageFormat.format(rb.getString("message.error"), left, file);
                            throw new IllegalArgumentException(s);
                        }
                    } else {
Debug.println(i + ":???: " + file);
                    }
                } else {
Debug.println(i + ":ignore: " + file);
                }
            }
        }

        if (args.length != 0 && (model.getLeftFiles().size() == 0 || model.getRightFiles().size() == 0)) {
            throw new IllegalArgumentException(rb.getString("message.usage"));
        }
    }

    /** form */
    void pageMain() {
        loadOptions();
        form.initMain(model);

        form.pageMain();

        form.setTitle(rb.getString("frame.title.scanning"));
        model.updateTargets();
        finishWork();

        // redisplayOutline();
        update();
    }

    /** form */
    void pageMain_close() {
        form.pageMain_close();
        pageMain_close2();
    }

    /** form */
    void pageMain_close2() {
        saveOptions();
        System.exit(0);
    }

    /** form */
    void pagePopup(int x, int y) {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            form.pagePopupOutline(x, y);
        } else {
            form.pagePopupExpanded(x, y);
        }
    }

    /** form */
    void pageSaveFile() {
        form.pageSaveFile();
    }

    /** form */
    void pageEditorChooser() {
        if (model.editor != null && model.editor.length() != 0) {
            File file = new File(model.editor);
            form.initEditorDialog(file);
        }
        form.pageEditorChooser();
    }

    /** form */
    void pageCompareTargetsDialog() {
        if (model.displayMode == DisplayMode.NONE_MODE) {
            File left = model.getLeftFiles().get(0);
            File right = model.getRightFiles().get(0);
            form.initCompareTargetsDialog(left, right);
        }
        form.pageCompareTargetsDialog();
    }

    /** form */
    void pageTargetsDialog_close() {
        form.pageTargetsDialog_close();
    }

    /** form */
    void pageSaveListDialog_close() {
        form.pageSaveListDialog_close();
    }

    /** form */
    void pageEditorDialog_close() {
        form.pageEditorDialog_close();
    }

    /** form */
    void pagePatternDialog_close() {
        form.pagePatternDialog_close();
    }

    /** form */
    void editLeft() {
        if (model.current != null) {
            editFile(model.current.getLeft());
        }
    }

    /** form */
    void editRight() {
        if (model.current != null) {
            editFile(model.current.getRight());
        }
    }

    /**
     * Invokes a editor.
     */
    private void editFile(File file) {
        try {
            if (file != null && file.exists() && model.editor != null) {
                Runtime.getRuntime().exec(model.editor + " \"" + file.getAbsolutePath() + "\"");
            }
        } catch (Exception e) {
Debug.printStackTrace(e);
        }
    }

    /** */
    void findDiff(Order order) {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            form.findOutline(order);
        } else {
            form.findExpand(order);
        }
    }

    /** form */
    void markPattern() {
        form.markPattern();
    }

    /** form */
    void changeMode() {
        if (model.displayMode == DisplayMode.OUTLINE_MODE) {
            toExpand();
        } else {
            toOutline();
        }
    }

    /** form */
    void startSelection(Point point) {
        form.startSelection(point);
    }

    /** form */
    void continueSelection(Point point) {
        form.continueSelection(point);
    }

    /** form */
    void endSelection(Point point) {
        form.endSelection(point);
    }

    /** form */
    void moveCursor(int x, int y) {
        if (x > 50 && x < 80) {
            form.moveCursor(x, y);
        }
    }

    /** form */
    void updateGraphics() {
        if (model.displayMode == DisplayMode.EXPANDED_MODE) {
            form.updateGraphics();
        }
    }

    /** form sub */
    private void finishWork() {
        if (model.isMultiMode()) {
            String title = MessageFormat.format(rb.getString("frame.title.compare"), model.getLeftFilePath(), model.getRightFilePath());
            form.setTitle(title);
        } else {
            form.setTitle(rb.getString("frame.title.default"));
        }
    }
}

/* */
