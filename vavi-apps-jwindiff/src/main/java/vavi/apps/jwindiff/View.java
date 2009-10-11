/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;

import vavi.apps.jwindiff.Controller.Order;
import vavi.apps.jwindiff.Model.DisplayMode;
import vavi.apps.jwindiff.Model.ShowExpandMode;
import vavi.apps.jwindiff.Model.ShowNumMode;
import vavi.swing.JFileChooserField;
import vavi.swing.JFileChooserHistoryComboBox;
import vavi.swing.JHistoryComboBox;
import vavi.util.Debug;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;


/**
 * View.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060504 nsano initial version <br>
 */
class View {

    /** */
    private static final ResourceBundle rb = ResourceBundle.getBundle("JWinDiffResources", Locale.getDefault());

    // CONSTANTS
    private static final int DEFAULT_WIDTH = 800;

    /** */
    View() {
        createFrame();

        createTargetsDialog();
        createSaveListDialog();
        createEditorDialog();
        createPatternDialog();
    }

    // main view ----

    /** */
    JList mainView;

    /** TODO é©ìÆê∂ê¨ */
    GenericListener mainViewListener = new GenericListener() {
        @SuppressWarnings("unchecked")
        public void eventHappened(GenericEvent ev) {
            String name = ev.getName();
            if ("pageMain_close".equals(name)) {
                pageMain_close();
            } else if ("pageSaveListDialog".equals(name)) {
                pageSaveListDialog();
            } else if ("pagePopupOutline".equals(name)) {
                pagePopupOutline((Integer) ev.getArguments()[0], (Integer) ev.getArguments()[1]);
            } else if ("pagePopupExpanded".equals(name)) {
                pagePopupExpanded((Integer) ev.getArguments()[0], (Integer) ev.getArguments()[1]);
            } else if ("initCompareTargetsDialog".equals(name)) {
                initCompareTargetsDialog((File) ev.getArguments()[0], (File) ev.getArguments()[1]);
            } else if ("pageCompareTargetsDialog".equals(name)) {
                pageCompareTargetsDialog();
            } else if ("initEditorDialog".equals(name)) {
                initEditorDialog((File) ev.getArguments()[0]);
            } else if ("pageEditorChooser".equals(name)) {
                pageEditorChooser();
            } else if ("pageEditorDialog_close".equals(name)) {
                pageEditorDialog_close();
            } else if ("pagePatternDialog_close".equals(name)) {
                pagePatternDialog_close();
            } else if ("pageCompareTargetsDialog_close".equals(name)) {
                pageCompareTargetsDialog_close();
            } else if ("pageSaveListDialog_close".equals(name)) {
                pageSaveListDialog_close();
            } else if ("startSelection".equals(name)) {
                startSelection((Point) ev.getArguments()[0]);
            } else if ("endSelection".equals(name)) {
                endSelection((Point) ev.getArguments()[0]);
            } else if ("continueSelection".equals(name)) {
                continueSelection((Point) ev.getArguments()[0]);
            } else if ("moveCursor".equals(name)) {
                moveCursor((Integer) ev.getArguments()[0], (Integer) ev.getArguments()[1]);
            } else if ("updateGraphics".equals(name)) {
                updateGraphics();
            } else if ("findOutline".equals(name)) {
                findOutline((Order) ev.getArguments()[0]);
            } else if ("findExpand".equals(name)) {
                findExpand((Order) ev.getArguments()[0]);
            } else if ("setTitle".equals(name)) {
                setTitle((String) ev.getArguments()[0]);
            } else if ("pagePatternDialog".equals(name)) {
                pagePatternDialog();
            } else if ("displaySingleFile".equals(name)) {
                displaySingleFile((String[]) ev.getArguments()[0]);
            } else if ("toExpand".equals(name)) {
                toExpand();
            } else if ("toOutline".equals(name)) {
                toOutline();
            } else if ("toSelection".equals(name)) {
                toSelection();
            } else if ("displayException".equals(name)) {
                displayException((Exception) ev.getArguments()[0]);
            } else if ("setNames".equals(name)) {
                setNames((String) ev.getArguments()[0]);
            } else if ("setPaths".equals(name)) {
                setPaths((String) ev.getArguments()[0]);
            } else if ("redisplayExpandedBefore".equals(name)) {
                redisplayExpandedBefore();
            } else if ("redisplayOutlineAfter".equals(name)) {
                redisplayOutlineAfter((DefaultListModel) ev.getArguments()[0], (Pair) ev.getArguments()[1]);
            } else if ("redisplayOutlineBefore".equals(name)) {
                redisplayOutlineBefore((String) ev.getArguments()[0], (String) ev.getArguments()[1]);
            } else if ("pageMain".equals(name)) {
                pageMain();
            } else if ("initMain".equals(name)) {
                initMain((Model) ev.getArguments()[0]);
            } else if ("updateMain".equals(name)) {
                updateMain((List<Line>) ev.getArguments()[0]);
            } else {
                assert false : name;
            }
        }
    };

    /** */
    void updateMain(List<Line> listModel) {
        DefaultListModel viewListModel = new DefaultListModel();
        for (Line line : listModel) {
            viewListModel.addElement(line);
        }
        mainView.setModel(viewListModel);
    }

    /** TODO */
    void redisplayOutlineBefore(String left, String right) {
        changeMode.setText(rb.getString("button.changeMode.text.expand"));
        viewOutline.setSelected(true);
        viewExpand.setSelected(false);

        names.setText("");
        paths.setText(left + " : " + right);
    }

    /**
     * TODO 
     * set the diff display widget into outline mode
     */
    void redisplayOutlineAfter(ListModel listModel, Pair current) {
        mainView.setModel(listModel);
        mainView.setSelectedValue(current, true);

        pictView.setVisible(false);
        pictView.setEnabled(false);
    }

    /** TODO */
    void redisplayExpandedBefore() {
        changeMode.setText(rb.getString("button.changeMode.text.outline"));
        viewOutline.setSelected(false);
        viewExpand.setSelected(true);

        pictView.setEnabled(true);
        pictView.setVisible(true);
    }

    /** */
    void setNames(String name) {
        names.setText(name + " ");
    }

    /** */
    void setPaths(String path) {
        paths.setText(" " + path);
    }

    /** */
    void displayException(Exception e) {
        DefaultListModel model = new DefaultListModel();
        model.addElement(new Line(0, e.toString(), Line.Type.PLAIN));
        mainView.setModel(model);
    }

    /** */
    void displaySingleFile(String[] lines) {
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < lines.length; i++) {
            model.addElement(new Line(i, lines[i], Line.Type.PLAIN));
        }
        mainView.setModel(model);
    }

    /** */
    void updateGraphics() {
        pictView.repaint();
    }

    /** */
    void toExpand() {
        viewExpand.setSelected(true);
    }

    /** */
    void toOutline() {
        viewOutline.setSelected(true);
    }

    /** */
    void toSelection() {
        mainView.ensureIndexIsVisible(0);
    }

    /** */
    void startSelection(Point point) {
        first = mainView.locationToIndex(point);
// Debug.println(first);
    }

    /** */
    void continueSelection(Point point) {
        last = mainView.locationToIndex(point);
        mainView.setSelectionInterval(first, last);
// Debug.println(first + ", " + last);
    }

    /** */
    void endSelection(Point point) {
        last = mainView.locationToIndex(point);
        if (first != last) {
            mainView.setSelectionInterval(first, last);
// Debug.println(first + ", " + last);
        }
    }

    /** */
    void setTitle(String title) {
        top.setTitle(title);
    }

    /** */
    void selectionChanged(String name) { // TODO unused???
        if (!mainView.isSelectionEmpty()) {
Debug.println(mainView.getSelectedIndex());
            names.setText(name);
//      } else {
//Debug.println("A: " + min + ", " + max);
//          mainView.setValueIsAdjusting(false);
        }
    }

    /** */
    void moveCursor(int x, int y) {
        int height = pictView.getSize().height;
        int size = mainView.getModel().getSize();
//      int first = mainView.getFirstVisibleIndex();
//      int last = mainView.getLastVisibleIndex();
//      int h = Math.abs((last - first) / 2);
//      int index = y * size / height - h;
        int index = y * size / height;
        mainView.ensureIndexIsVisible(index);
        pictView.repaint();
    }

    //------------------------------------------------------------------------

    /**
     * Variables for the various dialogs
     */
    private JDialog targetsDialog;
    /** */
    JFileChooserField leftTargetChooser;
    /** */
    JFileChooserField rightTargetChooser;
    /** */
    JButton okTargetsDialogButton;
    /** */
    JButton cancelTargetsDialogButton;

    /**
     * Code to create each of the dialogs
     */
    private void createTargetsDialog() {

        int w = top.getSize().width;
        // int h = top.getSize().height;

        targetsDialog = new JDialog(top, true);
        targetsDialog.setTitle(rb.getString("dialog.targets.title"));

        JPanel base = new JPanel(new GridLayout(4, 1));
        targetsDialog.getContentPane().add(base);

        JPanel p = new JPanel(new GridLayout(2, 1));
        JLabel l = new JLabel(rb.getString("dialog.targets.label.0.text"));
        l.setPreferredSize(new Dimension(w * 6 / 10, 0));
        p.add(l);

        l = new JLabel(System.getProperty("user.dir"));
        l.setOpaque(true);
        l.setBackground(Color.white);
        p.add(l);

        base.add(p);

        p = new JPanel(new GridLayout(2, 1));

        l = new JLabel(rb.getString("dialog.targets.label.1.text"));
        p.add(l);

        leftTargetChooser = new JFileChooserHistoryComboBox();
        p.add(leftTargetChooser);

        base.add(p);

        p = new JPanel(new GridLayout(2, 1));

        l = new JLabel(rb.getString("dialog.targets.label.2.text"));
        p.add(l);

        rightTargetChooser = new JFileChooserHistoryComboBox();
        p.add(rightTargetChooser);

        base.add(p);

        p = new JPanel();

        okTargetsDialogButton = new JButton();
        okTargetsDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        targetsDialog.getRootPane().setDefaultButton(okTargetsDialogButton);
        p.add(okTargetsDialogButton);

        cancelTargetsDialogButton = new JButton();
        cancelTargetsDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelTargetsDialogButton);

        base.add(p);

        targetsDialog.pack();
    }

    /** */
    void initCompareTargetsDialog(File left, File right) {
        leftTargetChooser.setCurrentDirectory(left);
        leftTargetChooser.setSelectedFile(left);
        rightTargetChooser.setCurrentDirectory(right);
        rightTargetChooser.setSelectedFile(right);
    }

    /** */
    void pageCompareTargetsDialog() {
        targetsDialog.setLocationRelativeTo(top);
        targetsDialog.setVisible(true);
    }

    /** */
    void pageCompareTargetsDialog_close() {
        targetsDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    private JDialog saveListDialog;

    /** */
    JFileChooserField listFileChooser;

    /** */
    JCheckBox hasIdentical;

    /** */
    JCheckBox hasDifferent;

    /** */
    JCheckBox hasLeft;

    /** */
    JCheckBox hasRight;

    /** */
    JCheckBox hasNotMarked;

    /** */
    JButton okSaveListDialogButton;

    /** */
    JButton cancelSaveListDialogButton;

    /** */
    private void createSaveListDialog() {

        int w = top.getSize().width;
        // int h = top.getSize().height;

        saveListDialog = new JDialog(top, true);
        saveListDialog.setTitle(rb.getString("dialog.seveList.title"));

        JPanel base = new JPanel(new BorderLayout());
        saveListDialog.getContentPane().add(base);

        JPanel p = new JPanel(new GridLayout(2, 1));
        JLabel l = new JLabel(rb.getString("dialog.seveList.label.0.text"));
        l.setPreferredSize(new Dimension(w * 6 / 10, 0));
        p.add(l);

        listFileChooser = new JFileChooserHistoryComboBox();
        listFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        p.add(listFileChooser);

        base.add(BorderLayout.NORTH, p);

        p = new JPanel(new GridLayout(6, 1));
        l = new JLabel(rb.getString("dialog.seveList.label.1.text"));
        p.add(l);

        hasIdentical = new JCheckBox();
        hasIdentical.setText(rb.getString("dialog.seveList.checkBox.0.text"));
        p.add(hasIdentical);
        hasDifferent = new JCheckBox(rb.getString("dialog.seveList.checkBox.1.text"));
        p.add(hasDifferent);
        hasLeft = new JCheckBox(rb.getString("dialog.seveList.checkBox.2.text"));
        p.add(hasLeft);
        hasRight = new JCheckBox(rb.getString("dialog.seveList.checkBox.3.text"));
        p.add(hasRight);
        hasNotMarked = new JCheckBox(rb.getString("dialog.seveList.checkBox.4.text"));
        p.add(hasNotMarked);

        base.add(BorderLayout.CENTER, p);

        p = new JPanel();
        okSaveListDialogButton = new JButton();
        okSaveListDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        saveListDialog.getRootPane().setDefaultButton(okSaveListDialogButton);
        p.add(okSaveListDialogButton);

        cancelSaveListDialogButton = new JButton();
        cancelSaveListDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelSaveListDialogButton);

        base.add(BorderLayout.SOUTH, p);

        saveListDialog.pack();
    }

    /**
     * initialize what to include in the list according to current prefs
     */
    void pageSaveListDialog() {
        // TODO model Ç©ÇÁéÊÇÈÇ◊Ç´Ç©ÅH
        hasIdentical.setSelected(showIdentical.isSelected());
        hasDifferent.setSelected(showDifferent.isSelected());
        hasLeft.setSelected(showLeft.isSelected());
        hasRight.setSelected(showRight.isSelected());
        hasNotMarked.setSelected(hideMarked.isSelected());
    
        saveListDialog.setLocationRelativeTo(top);
        saveListDialog.setVisible(true);
    }

    /** */
    void pageSaveListDialog_close() {
        saveListDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    private JDialog editorDialog;

    /** The editor path name */
    JFileChooserField editorChooser;

    /** */
    JButton okEditorDialogButton;

    /** */
    JButton cancelEditorDialogButton;

    /** */
    private void createEditorDialog() {

        int w = top.getSize().width;
//      int h = top.getSize().height;

        editorDialog = new JDialog(top, true);
        editorDialog.setTitle(rb.getString("dialog.editor.title"));

        JPanel base = new JPanel(new GridLayout(2, 1));
        editorDialog.getContentPane().add(base);

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(w * 6 / 10, 0));
        editorChooser = new JFileChooserHistoryComboBox();
        editorChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        p.add(BorderLayout.SOUTH, editorChooser);
        base.add(p);

        p = new JPanel();

        okEditorDialogButton = new JButton();
        okEditorDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        editorDialog.getRootPane().setDefaultButton(okEditorDialogButton);
        p.add(okEditorDialogButton);

        cancelEditorDialogButton = new JButton();
        cancelEditorDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelEditorDialogButton);
        base.add(p);

        editorDialog.pack();
    }

    /** */
    void initEditorDialog(File file) {
        editorChooser.setCurrentDirectory(file);
        editorChooser.setSelectedFile(file);
    }

    /** */
    void pageEditorChooser() {
        editorChooser.requestFocus();
        editorDialog.setLocationRelativeTo(top);
        editorDialog.setVisible(true);
    }

    void pageEditorDialog_close() {
        editorDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    private JDialog patternDialog;

    /** The text for matching */
    JHistoryComboBox patternField;

    /** */
    JButton okPatternDialogButton;

    /** */
    JButton cancelPatternDialogButton;

    /** */
    private void createPatternDialog() {

        int w = top.getSize().width;
        // int h = top.getSize().height;

        patternDialog = new JDialog(top, true);
        patternDialog.setTitle(rb.getString("dialog.pattern.title"));

        JPanel base = new JPanel(new GridLayout(2, 1));
        patternDialog.getContentPane().add(base);

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(w * 4 / 10, 0));
        patternField = new JHistoryComboBox();
        p.add(BorderLayout.SOUTH, patternField);
        base.add(p);

        p = new JPanel();

        okPatternDialogButton = new JButton();
        okPatternDialogButton.setText(rb.getString("dialog.common.button.ok.text"));
        patternDialog.getRootPane().setDefaultButton(okPatternDialogButton);
        p.add(okPatternDialogButton);

        cancelPatternDialogButton = new JButton();
        cancelPatternDialogButton.setText(rb.getString("dialog.common.button.cancel.text"));
        p.add(cancelPatternDialogButton);
        base.add(p);

        patternDialog.pack();
    }

    /** */
    void pagePatternDialog() {
        patternField.requestFocus();
        patternDialog.setLocationRelativeTo(top);
        patternDialog.setVisible(true);
    }

    /** */
    void pagePatternDialog_close() {
        patternDialog.setVisible(false);
    }

    // -------------------------------------------------------------------------

    /** */
    int first;

    /** */
    int last;

    /** */
    private class PicturePanel extends JPanel {
        public void paint(Graphics g) {
            super.paint(g);
            drawBar(g);
            drawCursor(g);
        }

        private static final int BR = 42;

        private static final int BW = 8;

        /** */
        private void drawBar(Graphics g) {
            Line.Type flag = null;
            int i = 0;
            int first = -1;
// Debug.println("----");
            while (i < mainView.getModel().getSize()) {
                // TODO Ç©Ç¡Ç±à´Ç¢
                if (!(mainView.getModel().getElementAt(0) instanceof Line)) {
                    return;
                }

                Line line = (Line) mainView.getModel().getElementAt(i);
                if (line.getFlag() != flag) {
                    if (first != -1) {
                        drawBarImpl(g, first, i, flag);
                    }
                    flag = line.getFlag();
                    first = i;
                }
                i++;
            }
            drawBarImpl(g, first, i, flag);
        }

        private void drawBarImpl(Graphics g, int first, int last, Line.Type flag) {
            int height = getSize().height;
            int size = mainView.getModel().getSize();

            int y = Math.round((float) first / size * height);
            int h = Math.round((float) (last - first) / size * height);
// Debug.println(first + ", " + last + ", " + flag);
            if (flag == Line.Type.PLAIN) {
                g.setColor(Color.black);
                g.drawRect(BR, y, BW, h);
            } else if (flag == Line.Type.DELETED) {
                g.setColor(Color.red);
                g.fillRect(BR, y, BW, h);
            } else if (flag == Line.Type.INSERTED) {
                g.setColor(Color.yellow);
                g.fillRect(BR, y, BW, h);
            }
        }

        private static final int CX = 60;

        private static final int CW = 8;

        /** */
        private void drawCursor(Graphics g) {
            int height = getSize().height;
            int size = mainView.getModel().getSize();
            int first = mainView.getFirstVisibleIndex();
            int last = mainView.getLastVisibleIndex();

            int y = Math.round((float) first / size * height);
            int h = Math.round((float) (last - first + 1) / size * height);

            g.setColor(Color.blue);
            g.fillRect(CX, y, CW, h);
        }

        public Dimension getPreferredSize() {
            int height = getSize().height;
            return new Dimension(80, height);
        }
    }

    // -------------------------------------------------------------------------

    /** */
    JCheckBoxMenuItem viewExpand;
    /** */
    JCheckBoxMenuItem viewOutline;
    /** In expanded mode, which files to show -- mutually exclusive */
    JCheckBoxMenuItem showLeftOnly;
    /** In expanded mode, which files to show -- mutually exclusive */
    JCheckBoxMenuItem showRightOnly;
    /** In expanded mode, which files to show -- mutually exclusive */
    JCheckBoxMenuItem showBoth;
    /** In expanded mode, which file numbering to show -- mutually exclusive */
    JCheckBoxMenuItem showLeftNums;
    /** In expanded mode, which file numbering to show -- mutually exclusive */
    JCheckBoxMenuItem showRightNums;
    /** In expanded mode, which file numbering to show -- mutually exclusive */
    JCheckBoxMenuItem hideNums;
    /** */
    JCheckBoxMenuItem ignoreBlanks;
    /** in outline mode, which files to list */
    JCheckBoxMenuItem showIdentical;
    /** */
    JCheckBoxMenuItem showDifferent;
    /** */
    JCheckBoxMenuItem showLeft;
    /** */
    JCheckBoxMenuItem showRight;
    /** */
    JCheckBoxMenuItem hideMarked;
    /** The popup menu */
    private JPopupMenu popupOutline;
    /** */
    private JPopupMenu popupExpanded;
    /** Widgets */
    JFrame top;
    /** */
    JScrollPane sp;
    /** */
    JPanel pictView;
    /** */
    private JLabel names;
    /** */
    private JLabel paths;
    /** */
    JButton changeMode;
    /** */
    JMenuItem compareTargetsMenuItem;
    /** */
    JMenuItem abortMenuItem;
    /** */
    JMenuItem saveFilelistMenuItem;
    /** */
    JMenuItem exitMenuItem;
    /** */
    JMenuItem editLeftMenuItem;
    /** */
    JMenuItem editRightMenuItem;
    /** */
    JMenuItem setEditorMenuItem;
    /** */
    JMenuItem prevMenuItem;
    /** */
    JMenuItem nextMenuItem;
    /** */
    JMenuItem rescanMenuItem;
    /** */
    JMenuItem markFileMenuItem;
    /** */
    JMenuItem markPatternMenuItem;
    /** */
    JMenuItem toggleMarkedMenuItem;
    /** */
    JButton modeButton;
    /** */
    JMenuItem expandMenuItem;
    /** */
    JMenuItem outlineMenuItem;

    /** */
    JMenuItem prevMenuItem2;
    /** */
    JMenuItem nextMenuItem2;
    /** */
    JMenuItem rescanMenuItem2;
    /** */
    JMenuItem editLeftMenuItem2;
    /** */
    JMenuItem editRightMenuItem2;

    /** */
    JMenuItem prevMenuItem3;
    /** */
    JMenuItem nextMenuItem3;
    /** */
    JMenuItem rescanMenuItem3;
    /** */
    JMenuItem editLeftMenuItem3;
    /** */
    JMenuItem editRightMenuItem3;
    
    /**
     * Build the widget structure of the main window
     */
    private void createFrame() {

        // Set up the actions table for the whole application
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        int width = DEFAULT_WIDTH;
        int height = d.height * 3 / 4;

        top = new JFrame();
        top.setIconImage(t.getImage(this.getClass().getResource(rb.getString("frame.jWinDiff.iconImage"))));
        top.setTitle(rb.getString("frame.title.scanning"));
        top.setSize(width, height);

        JPanel base = new JPanel();
        base.setLayout(new BorderLayout());
        top.getContentPane().add(base);

        pictView = new PicturePanel();
        pictView.setOpaque(true);
        pictView.setBackground(Color.white);
        base.add(pictView, BorderLayout.WEST);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setOpaque(true);
//      panel.setBackground(Color.cyan);
        base.add(panel, BorderLayout.NORTH);

        mainView = new JList();
        mainView.setFont(new Font(rb.getString("panel.jWinDiff.font.name"), Font.PLAIN, 12));

        sp = new JScrollPane();
        sp.setViewportView(mainView);
        base.add(sp);

        // Now create each of the menus

        JMenuBar menuBar = new JMenuBar();

        // file
        JMenu menu = new JMenu();
        menu.setText(rb.getString("menu.file.text"));
        menu.setMnemonic(KeyEvent.VK_F);

        compareTargetsMenuItem = new JMenuItem();
        compareTargetsMenuItem.setText(rb.getString("menuItem.compareTargets.text"));
        compareTargetsMenuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(compareTargetsMenuItem);

        menu.addSeparator();

        abortMenuItem = new JMenuItem();
        abortMenuItem.setText(rb.getString("menuItem.abort.text"));
        abortMenuItem.setMnemonic(KeyEvent.VK_A);
        abortMenuItem.setEnabled(false);
        menu.add(abortMenuItem);

        menu.addSeparator();

        saveFilelistMenuItem = new JMenuItem();
        saveFilelistMenuItem.setText(rb.getString("menuItem.saveFileList.text"));
        saveFilelistMenuItem.setMnemonic(KeyEvent.VK_S);
        menu.add(saveFilelistMenuItem);

        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.copyFiles.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.print.text"));
        menuItem.setMnemonic(KeyEvent.VK_P);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        exitMenuItem = new JMenuItem();
        exitMenuItem.setText(rb.getString("menuItem.exit.text"));
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        menu.add(exitMenuItem);

        menuBar.add(menu);

        // edit
        menu = new JMenu();
        menu.setText(rb.getString("menu.edit.text"));
        menu.setMnemonic(KeyEvent.VK_E);

        editLeftMenuItem = new JMenuItem();
        editLeftMenuItem.setText(rb.getString("menuItem.editLeftFile.text"));
        editLeftMenuItem.setMnemonic(KeyEvent.VK_L);
        menu.add(editLeftMenuItem);

        editRightMenuItem = new JMenuItem();
        editRightMenuItem.setText(rb.getString("menuItem.editRightFile.text"));
        editRightMenuItem.setMnemonic(KeyEvent.VK_R);
        menu.add(editRightMenuItem);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.editCompositeFile.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        setEditorMenuItem = new JMenuItem();
        setEditorMenuItem.setText(rb.getString("menuItem.setEditor.text"));
        setEditorMenuItem.setMnemonic(KeyEvent.VK_E);
        menu.add(setEditorMenuItem);

        menuBar.add(menu);

        // view
        ButtonGroup bg = new ButtonGroup();

        menu = new JMenu(rb.getString("menu.view.text"));
        menu.setMnemonic(KeyEvent.VK_V);

        viewOutline = new JCheckBoxMenuItem();
        viewOutline.setText(rb.getString("menuItem.outline.text"));
        viewOutline.setMnemonic(KeyEvent.VK_O);
        menu.add(viewOutline);
        bg.add(viewOutline);

        viewExpand = new JCheckBoxMenuItem();
        viewExpand.setText(rb.getString("menuItem.expand.text"));
        viewExpand.setMnemonic(KeyEvent.VK_E);
        menu.add(viewExpand);
        bg.add(viewExpand);

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem();
        menuItem.setText(rb.getString("menuItem.picture.text"));
//      menuItem.setMnemonic(KeyEvent.VK_P); // M+p
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        prevMenuItem = new JMenuItem();
        prevMenuItem.setText(rb.getString("menuItem.previousChange.text"));
        prevMenuItem.setMnemonic(KeyEvent.VK_P); // F7
        menu.add(prevMenuItem);

        nextMenuItem = new JMenuItem();
        nextMenuItem.setText(rb.getString("menuItem.nextChange.text"));
        nextMenuItem.setMnemonic(KeyEvent.VK_N); // F8
        menu.add(nextMenuItem);

        menu.addSeparator();

        rescanMenuItem = new JMenuItem();
        rescanMenuItem.setText(rb.getString("menuItem.rescanSelectedFile.text"));
        rescanMenuItem.setMnemonic(KeyEvent.VK_R);
        menu.add(rescanMenuItem);

        menuBar.add(menu);

        // expand
        menu = new JMenu(rb.getString("menu.expand.text"));
        menu.setMnemonic(KeyEvent.VK_X);

        bg = new ButtonGroup();

        showLeftOnly = new JCheckBoxMenuItem();
        showLeftOnly.setText(rb.getString("menuItem.leftFileOnly.text"));
        showLeftOnly.setMnemonic(KeyEvent.VK_F); // M+l
        showLeftOnly.setActionCommand(ShowExpandMode.left.name());
        menu.add(showLeftOnly);
        bg.add(showLeftOnly);

        showRightOnly = new JCheckBoxMenuItem();
        showRightOnly.setText(rb.getString("menuItem.rightFileOnly.text"));
        showRightOnly.setMnemonic(KeyEvent.VK_H); // M-r
        showRightOnly.setActionCommand(ShowExpandMode.right.name());
        menu.add(showRightOnly);
        bg.add(showRightOnly);

        showBoth = new JCheckBoxMenuItem();
        showBoth.setText(rb.getString("menuItem.bothFiles.text"));
        showBoth.setMnemonic(KeyEvent.VK_O); // M-b
        showBoth.setActionCommand(ShowExpandMode.both.name());
        showBoth.setSelected(true);
        menu.add(showBoth);
        bg.add(showBoth);

        menu.addSeparator();

        bg = new ButtonGroup();

        showLeftNums = new JCheckBoxMenuItem();
        showLeftNums.setText(rb.getString("menuItem.leftLineNumbers.text"));
        showLeftNums.setMnemonic(KeyEvent.VK_L);
        showLeftNums.setActionCommand(ShowNumMode.left.name());
        menu.add(showLeftNums);
        bg.add(showLeftNums);

        showRightNums = new JCheckBoxMenuItem();
        showRightNums.setText(rb.getString("menuItem.rightLineNumbers.text"));
        showRightNums.setMnemonic(KeyEvent.VK_R);
        showRightNums.setActionCommand(ShowNumMode.right.name());
        menu.add(showRightNums);
        bg.add(showRightNums);

        hideNums = new JCheckBoxMenuItem();
        hideNums.setText(rb.getString("menuItem.noLineNumbers.text"));
        hideNums.setMnemonic(KeyEvent.VK_N);
        hideNums.setActionCommand(ShowNumMode.none.name());
        hideNums.setSelected(true);
        menu.add(hideNums);
        bg.add(hideNums);

        menuBar.add(menu);

        // opts
        menu = new JMenu(rb.getString("menu.opts.text"));
        menu.setMnemonic(KeyEvent.VK_O);

        ignoreBlanks = new JCheckBoxMenuItem();
        ignoreBlanks.setText(rb.getString("menuItem.ignoreBlanks.text"));
        ignoreBlanks.setMnemonic(KeyEvent.VK_B);
        ignoreBlanks.setSelected(true);
        menu.add(ignoreBlanks);

        menu.addSeparator();

        menuItem = new JCheckBoxMenuItem(rb.getString("menuItem.monoColours.text"));
        menuItem.setMnemonic(KeyEvent.VK_M);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        showIdentical = new JCheckBoxMenuItem();
        showIdentical.setText(rb.getString("menuItem.showIdenticalFiles.text"));
        showIdentical.setMnemonic(KeyEvent.VK_I);
        showIdentical.setSelected(true);
        menu.add(showIdentical);

        showLeft = new JCheckBoxMenuItem();
        showLeft.setText(rb.getString("menuItem.showLeftOnlyFiles.text"));
        showLeft.setMnemonic(KeyEvent.VK_L);
        showLeft.setSelected(true);
        menu.add(showLeft);

        showRight = new JCheckBoxMenuItem();
        showRight.setText(rb.getString("menuItem.showRightOnlyFiles.text"));
        showRight.setMnemonic(KeyEvent.VK_R);
        showRight.setSelected(true);
        menu.add(showRight);

        showDifferent = new JCheckBoxMenuItem();
        showDifferent.setText(rb.getString("menuItem.showDifferentFiles.text"));
        showDifferent.setMnemonic(KeyEvent.VK_D);
        showDifferent.setSelected(true);
        menu.add(showDifferent);

        menuBar.add(menu);

        // mark
        menu = new JMenu();
        menu.setText(rb.getString("menu.mark.text"));
        menu.setMnemonic(KeyEvent.VK_K);

        markFileMenuItem = new JMenuItem();
        markFileMenuItem.setText(rb.getString("menuItem.markFile.text"));
        markFileMenuItem.setMnemonic(KeyEvent.VK_M); // M+m
        menu.add(markFileMenuItem);

        markPatternMenuItem = new JMenuItem();
        markPatternMenuItem.setText(rb.getString("menuItem.markPattern.text"));
        markPatternMenuItem.setMnemonic(KeyEvent.VK_P);
        menu.add(markPatternMenuItem);

        hideMarked = new JCheckBoxMenuItem();
        hideMarked.setText(rb.getString("menuItem.hideMarkedFiles.text"));
        hideMarked.setMnemonic(KeyEvent.VK_H);
        menu.add(hideMarked);

        toggleMarkedMenuItem = new JMenuItem();
        toggleMarkedMenuItem.setText(rb.getString("menuItem.toggleMarkedState.text"));
        toggleMarkedMenuItem.setMnemonic(KeyEvent.VK_T);
        menu.add(toggleMarkedMenuItem);

        menuBar.add(menu);

        // help
        menu = new JMenu();
        menu.setText(rb.getString("menu.help.text"));
        menu.setMnemonic(KeyEvent.VK_H);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.contents.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.about.text"));
        menuItem.setMnemonic(KeyEvent.VK_A);
        menuItem.setEnabled(false);
        menu.add(menuItem);

        menuBar.add(menu);

        top.setJMenuBar(menuBar);

        // popup outline menu
        popupOutline = new JPopupMenu();

        markFileMenuItem.setMnemonic(KeyEvent.VK_M);
        popupOutline.add(markFileMenuItem);

        popupOutline.addSeparator();

        nextMenuItem2 = new JMenuItem();
        nextMenuItem2.setText(rb.getString("menuItem.nextChange.text"));
        nextMenuItem2.setMnemonic(KeyEvent.VK_N); // F8
        popupOutline.add(nextMenuItem2);

        prevMenuItem2 = new JMenuItem();
        prevMenuItem2.setText(rb.getString("menuItem.previousChange.text"));
        prevMenuItem2.setMnemonic(KeyEvent.VK_P); // F7
        popupOutline.add(prevMenuItem2);

        expandMenuItem = new JMenuItem();
        expandMenuItem.setText(rb.getString("button.changeMode.text.expand"));
        expandMenuItem.setMnemonic(KeyEvent.VK_E);
        popupOutline.add(expandMenuItem);

        rescanMenuItem2 = new JMenuItem();
        rescanMenuItem2.setText(rb.getString("menuItem.rescanSelectedFile.text"));
        rescanMenuItem2.setMnemonic(KeyEvent.VK_R);
        popupOutline.add(rescanMenuItem2);

        popupOutline.addSeparator();

        editLeftMenuItem2 = new JMenuItem();
        editLeftMenuItem2.setText(rb.getString("menuItem.editLeftFile.text"));
        editLeftMenuItem2.setMnemonic(KeyEvent.VK_L);
        popupOutline.add(editLeftMenuItem2);

        editRightMenuItem2 = new JMenuItem();
        editRightMenuItem2.setText(rb.getString("menuItem.editRightFile.text"));
        editRightMenuItem2.setMnemonic(KeyEvent.VK_R);
        popupOutline.add(editRightMenuItem2);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.editCompositeFile.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        popupOutline.add(menuItem);

        // popup expanded menu
        popupExpanded = new JPopupMenu();

        menuItem = popupExpanded.add(rb.getString("menuItem.showMoveDest.text"));
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setEnabled(false);

        popupExpanded.addSeparator();

        nextMenuItem3 = new JMenuItem();
        nextMenuItem3.setText(rb.getString("menuItem.nextChange.text"));
        nextMenuItem3.setMnemonic(KeyEvent.VK_N); // F8
        popupExpanded.add(nextMenuItem3);

        prevMenuItem3 = new JMenuItem();
        prevMenuItem3.setText(rb.getString("menuItem.previousChange.text"));
        prevMenuItem3.setMnemonic(KeyEvent.VK_P); // F7
        popupExpanded.add(prevMenuItem3);

        outlineMenuItem = new JMenuItem();
        outlineMenuItem.setText(rb.getString("button.changeMode.text.outline"));
        outlineMenuItem.setMnemonic(KeyEvent.VK_O);
        popupExpanded.add(outlineMenuItem);

        rescanMenuItem3 = new JMenuItem();
        rescanMenuItem3.setText(rb.getString("menuItem.rescanSelectedFile.text"));
        rescanMenuItem3.setMnemonic(KeyEvent.VK_R);
        popupExpanded.add(rescanMenuItem3);

        popupExpanded.addSeparator();

        editLeftMenuItem3 = new JMenuItem();
        editLeftMenuItem3.setText(rb.getString("menuItem.editLeftFile.text"));
        editLeftMenuItem3.setMnemonic(KeyEvent.VK_L);
        popupExpanded.add(editLeftMenuItem3);

        editRightMenuItem3 = new JMenuItem();
        editRightMenuItem3.setText(rb.getString("menuItem.editRightFile.text"));
        editRightMenuItem3.setMnemonic(KeyEvent.VK_R);
        popupExpanded.add(editRightMenuItem3);

        menuItem = new JMenuItem();
        menuItem.setText(rb.getString("menuItem.editCompositeFile.text"));
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setEnabled(false);
        popupExpanded.add(menuItem);

        // labels
        names = new JLabel();
        names.setForeground(Color.blue);
        panel.add(BorderLayout.WEST, names);

        paths = new JLabel();
        paths.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panel.add(BorderLayout.CENTER, paths);

        // chageMode button
        changeMode = new JButton();
        changeMode.setMnemonic(KeyEvent.VK_D);
        panel.add(BorderLayout.EAST, changeMode);
    }

    /** */
    void initMain(Model model) {
        // 1.
        mainView.setCellRenderer(new SimpleListCellRenderer(model));
    
        // 2.
        viewOutline.setSelected(model.displayMode == DisplayMode.OUTLINE_MODE);
        viewExpand.setSelected(model.displayMode == DisplayMode.EXPANDED_MODE);
        changeMode.setText(model.displayMode == DisplayMode.OUTLINE_MODE ? rb.getString("button.changeMode.text.outline") : rb.getString("button.changeMode.text.expand"));

        // 3.1. DI model -> view
        switch (model.getShowExpandMode()) {
        case left:
            showLeftOnly.setSelected(true);
            break;
        case right:
            showRightOnly.setSelected(true);
            break;
        case both:
            showBoth.setSelected(true);
            break;
        }

        switch (model.getShowNumMode()) {
        case left:
            showLeftNums.setSelected(true);
            break;
        case right:
            showRightNums.setSelected(true);
            break;
        case none:
            hideNums.setSelected(true);
            break;
        }
    
        ignoreBlanks.setSelected(model.isIgnoreBlanks());
        showIdentical.setSelected(model.isShowIdentical());
        showLeft.setSelected(model.isShowLeft());
        showRight.setSelected(model.isShowRight());
        showDifferent.setSelected(model.isShowDifferent());
        hideMarked.setSelected(model.isHideMarked());
    
        // 3.2. history
        for (int i = 0; i < model.patterns.size(); i++) {
            patternField.addItem(model.patterns.get(i));
        }
    }

    /** */
    void pageMain() {
        top.setVisible(true);
    }

    /** */
    void pageMain_close() {
        top.dispose();
    }

    // ----

    /** */
    void pagePopupOutline(int x, int y) {
        popupOutline.show(mainView, x, y);
    }

    /** */
    void pagePopupExpanded(int x, int y) {
        popupExpanded.show(mainView, x, y);
    }

    // -------------------------------------------------------------------------

    /**
     * Find next or prev diffs.
     * TODO use model instead of model in view
     */
    void findOutline(Order param) {
        if (param == Order.Ascent) {
            int index;
            if (mainView.isSelectionEmpty()) {
                index = 0;
            } else {
                index = mainView.getSelectedIndex();
            }
            ListModel model = mainView.getModel();
            while (index++ < model.getSize() - 1) {
                Pair.Type diff = ((Pair) model.getElementAt(index)).getDiff();
                if (diff.isDifferent()) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
            }
        } else {
            ListModel model = mainView.getModel();
            int index;
            if (mainView.isSelectionEmpty()) {
                index = model.getSize();
            } else {
                index = mainView.getSelectedIndex();
            }
            while (index-- > 0) {
                Pair.Type diff = ((Pair) model.getElementAt(index)).getDiff();
                if (diff.isDifferent()) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
            }
        }
        mainView.repaint(); // TODO for popup menu garbage
    }

    /**
     * TODO use model instead of model in view
     */
    void findExpand(Order param) {
        if (param == Order.Ascent) {
            int index;
            if (mainView.isSelectionEmpty()) {
                index = 0;
            } else {
                index = mainView.getSelectedIndex();
            }
            ListModel model = mainView.getModel();
            while (index++ < model.getSize() - 1) {
                if (((Line) model.getElementAt(index)).getFlag() == Line.Type.PLAIN) {
                    break;
                }
            }
            while (index < model.getSize()) {
                if (((Line) model.getElementAt(index)).getFlag() != Line.Type.PLAIN) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
                index++;
            }
        } else {
            ListModel model = mainView.getModel();
            int index;
            if (mainView.isSelectionEmpty()) {
                index = model.getSize();
            } else {
                index = mainView.getSelectedIndex();
            }
            while (index-- > 0) {
                if (((Line) model.getElementAt(index)).getFlag() == Line.Type.PLAIN) {
                    break;
                }
            }
            while (index >= 0) {
                if (((Line) model.getElementAt(index)).getFlag() != Line.Type.PLAIN) {
                    mainView.setSelectedIndex(index);
                    mainView.ensureIndexIsVisible(index);
                    break;
                }
                index--;
            }
        }
        mainView.repaint(); // TODO for popup menu garbage
    }
}

/* */
