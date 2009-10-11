/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import vavi.apps.jwindiff.Controller.Order;


/**
 * SwingController.
 * TODO 自動生成、でもできないとこもあるぞ？？？
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060508 nsano initial version <br>
 */
class SwingController {

    /** Swing が Controller を兼ねている為それをバインドする用 */
    private Controller controller;

    /** Swing で作られた View */
    private View view;

    /** */
    SwingController(Controller controller, View view) {
        this.controller = controller;
        this.view = view;
    }

    /** */
    void bind() {
        bindMain();
        bindEditorDialog();
        bindPatternDialog();
        bindPicturePanel();
        bindSaveListDialog();
        bindTargetsDialog();
    }

    /** */
    private void bindTargetsDialog() {
        view.okTargetsDialogButton.addActionListener(okTargetsDialogAction);
        view.cancelTargetsDialogButton.addActionListener(cancelTargetsDialogAction);
    }

    /** */
    private void bindSaveListDialog() {
        view.okSaveListDialogButton.addActionListener(okSaveListDialogAction);
        view.cancelSaveListDialogButton.addActionListener(cancelSaveListDialogAction);
    }

    /** */
    private void bindEditorDialog() {
        view.okEditorDialogButton.addActionListener(okEditorDialogAction);
        view.cancelEditorDialogButton.addActionListener(cancelEditorDialogAction);
    }

    /** */
    private void bindPatternDialog() {
        view.okPatternDialogButton.addActionListener(okPatternDialogAction);
        view.cancelPatternDialogButton.addActionListener(cancelPatternDialogAction);
    }

    /** */
    private void bindPicturePanel() {
        view.pictView.addMouseListener(pml);
        view.pictView.addMouseMotionListener(pml);
    }

    /** */
    private void bindMain() {
        view.mainView.addListSelectionListener(lsl);
        view.mainView.addMouseListener(lml);
        view.mainView.addMouseMotionListener(lml);

        view.compareTargetsMenuItem.addActionListener(compareTargetsAction);
        view.abortMenuItem.addActionListener(abortAction);
        view.saveFilelistMenuItem.addActionListener(saveFilelistAction);
        view.exitMenuItem.addActionListener(exitAction);
        view.editLeftMenuItem.addActionListener(editLeftAction);
        view.editRightMenuItem.addActionListener(editRightAction);
        view.editLeftMenuItem2.addActionListener(editLeftAction);
        view.editRightMenuItem2.addActionListener(editRightAction);
        view.editLeftMenuItem3.addActionListener(editLeftAction);
        view.editRightMenuItem3.addActionListener(editRightAction);
        view.setEditorMenuItem.addActionListener(setEditorAction);
        view.viewOutline.addActionListener(outlineAction);
        view.viewExpand.addActionListener(expandAction);
        view.prevMenuItem.addActionListener(prevAction);
        view.nextMenuItem.addActionListener(nextAction);
        view.rescanMenuItem.addActionListener(rescanAction);
        view.prevMenuItem2.addActionListener(prevAction);
        view.nextMenuItem2.addActionListener(nextAction);
        view.rescanMenuItem2.addActionListener(rescanAction);
        view.prevMenuItem3.addActionListener(prevAction);
        view.nextMenuItem3.addActionListener(nextAction);
        view.rescanMenuItem3.addActionListener(rescanAction);
        view.showLeftOnly.addActionListener(setShowExpandModeAction);
        view.showRightOnly.addActionListener(setShowExpandModeAction);
        view.showBoth.addActionListener(setShowExpandModeAction);
        view.showLeftNums.addActionListener(setShowNumModeAction);
        view.showRightNums.addActionListener(setShowNumModeAction);
        view.hideNums.addActionListener(setShowNumModeAction);
        view.ignoreBlanks.addActionListener(ignoreBlanksAction);
        view.showIdentical.addActionListener(showIdenticalAction);
        view.showLeft.addActionListener(showLeftAction);
        view.showRight.addActionListener(showRightAction);
        view.showDifferent.addActionListener(showDifferentAction);
        view.markFileMenuItem.addActionListener(markFileAction);
        view.markPatternMenuItem.addActionListener(markPatternAction);
        view.hideMarked.addActionListener(hideMarkedAction);
        view.toggleMarkedMenuItem.addActionListener(toggleMarkedAction);
        view.expandMenuItem.addActionListener(expandAction);
        view.outlineMenuItem.addActionListener(outlineAction);
        view.changeMode.addActionListener(changeModeAction);
        view.top.addWindowListener(windowListener);

        view.sp.getVerticalScrollBar().addAdjustmentListener(pal);
    }

    //------------------------------------------------------------------------

    /**
     * compare (file)
     */
    ActionListener compareTargetsAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageCompareTargetsDialog();
        }
    };

    /**
     * abort (file)
     */
    ActionListener abortAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            // TODO can't yet interrupt
        }
    };

    /**
     * saveFilelist (file)
     */
    ActionListener saveFilelistAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageSaveFile();
        }
    };

    /**
     * exit (file)
     */
    ActionListener exitAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageMain_close();
        }
    };

    /**
     * editLeft (edit)
     */
    ActionListener editLeftAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.editLeft();
        }
    };

    /**
     * editRight (edit)
     */
    ActionListener editRightAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.editRight();
        }
    };

    /**
     * setEditor (edit)
     */
    ActionListener setEditorAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageEditorChooser();
        }
    };

    /**
     * outline
     */
    ActionListener outlineAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.toOutline();
        }
    };

    /**
     * expand
     */
    ActionListener expandAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.toExpand();
        }
    };

    /**
     * prev
     */
    ActionListener prevAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.findDiff(Order.Descent);
        }
    };

    /**
     * next
     */
    ActionListener nextAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.findDiff(Order.Ascent);
        }
    };

    /**
     * rescan
     */
    ActionListener rescanAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            Object[] selection = view.mainView.getSelectedValues();
            if (selection.length == 0 || !(selection[0] instanceof Pair)) {
                return;
            }
            Pair[] pairs = new Pair[selection.length];
            System.arraycopy(selection, 0, pairs, 0, selection.length);

            controller.rescan(pairs);
        }
    };

    /**
     * show mode (expand)
     */
    ActionListener setShowExpandModeAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.setShowExpandMode(ev.getActionCommand());
        }
    };

    /**
     * num mode (expand)
     */
    ActionListener setShowNumModeAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.setShowNumMode(ev.getActionCommand());
        }
    };

    /**
     * ignoreBlanks (options)
     */
    ActionListener ignoreBlanksAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            // only redisplay in outline mode
            controller.updateOutline();
        }
    };

    /**
     * showIdentical (options)
     */
    ActionListener showIdenticalAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            boolean showIdentical = view.showIdentical.isSelected();

            controller.setShowIdentical(!showIdentical);
        }
    };

    /**
     * showLeft (options)
     */
    ActionListener showLeftAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            boolean showLeft = view.showLeft.isSelected();

            controller.setShowLeft(!showLeft);
        }
    };

    /**
     * showRight (options)
     */
    ActionListener showRightAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            boolean showRight = view.showRight.isSelected();

            controller.setShowRight(!showRight);
        }
    };

    /**
     * showDifferent (options)
     */
    ActionListener showDifferentAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            boolean showDifferent = view.showDifferent.isSelected();

            controller.setShowDifferent(!showDifferent);
        }
    };

    /**
     * markFile (mark)
     */
    ActionListener markFileAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            Object[] selection = view.mainView.getSelectedValues();
            if (selection.length == 0 || !(selection[0] instanceof Pair)) {
                return;
            }
            Pair[] pairs = new Pair[selection.length];
            System.arraycopy(selection, 0, pairs, 0, selection.length);

            controller.toggleSelectedMark(pairs);
        }
    };

    /**
     * markPattern (mark)
     */
    ActionListener markPatternAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.markPattern();
        }
    };

    /**
     * hideMarked (mark)
     */
    ActionListener hideMarkedAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            boolean hideMarked = view.hideMarked.isSelected();

            controller.setHideMarked(hideMarked);
        }
    };

    /**
     * toggleMarked (mark)
     */
    ActionListener toggleMarkedAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.toggleAllMarks();
        }
    };

    /**
     * changeMode button
     */
    ActionListener changeModeAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.changeMode();
        }
    };

    /**
     * list selections on main window
     */
    ListSelectionListener lsl = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent ev) {
            if (!ev.getValueIsAdjusting()) {
                if (!(view.mainView.getSelectedValue() instanceof Pair)) {
                    return;
                }
                Pair current = (Pair) view.mainView.getSelectedValue();

                controller.setCurrent(current);
            }
        }
    };

    /**
     * list selections on main window and popup menu
     */
    MouseInputListener lml = new MouseInputAdapter() {

        /** */
        public void mouseClicked(MouseEvent ev) {
            if (ev.getClickCount() == 2) {
                controller.toExpand();
            } else if (SwingUtilities.isRightMouseButton(ev)) {
                int x = ev.getX();
                int y = ev.getY();

                // TODO menu control, selection control
                controller.pagePopup(x, y);
            }
        }

        /** */
        public void mousePressed(MouseEvent ev) {
            if (SwingUtilities.isLeftMouseButton(ev)) {
                controller.startSelection(ev.getPoint());
            }
        }

        /** */
        public void mouseDragged(MouseEvent ev) {
            if (SwingUtilities.isLeftMouseButton(ev)) {
                controller.continueSelection(ev.getPoint());
            }
        }

        /** */
        public void mouseReleased(MouseEvent ev) {
            if (SwingUtilities.isLeftMouseButton(ev)) {
                controller.endSelection(ev.getPoint());
            }
        }
    };

    // -------------------------------------------------------------------------

    /**
     * editorDialog: ok
     */
    ActionListener okEditorDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            String editor = view.editorChooser.getSelectedFile().toString();

            if (new File(editor).exists()) {
                controller.setEditor(editor);
            }
            controller.pageEditorDialog_close();
        }
    };

    /**
     * editorDialog: cancel
     */
    ActionListener cancelEditorDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageEditorDialog_close();
        }
    };

    /**
     * patternDialog: ok
     */
    ActionListener okPatternDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            String regex = (String) view.patternField.getSelectedItem();

            controller.markRegex(regex);
            controller.pagePatternDialog_close();
        }
    };

    /**
     * patternDialog: cancel
     */
    ActionListener cancelPatternDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pagePatternDialog_close();
        }
    };

    /**
     * targetsDialog: ok
     */
    ActionListener okTargetsDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            File left = view.leftTargetChooser.getSelectedFile();
            File right = view.rightTargetChooser.getSelectedFile();

            if (left.exists() && right.exists()) {
                controller.updateTargets(left, right);
                controller.update();
            }
            controller.pageTargetsDialog_close();
        }
    };

    /**
     * targetsDialog: cancel
     */
    ActionListener cancelTargetsDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageTargetsDialog_close();
        }
    };

    /**
     * saveListDialog: ok
     */
    ActionListener okSaveListDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            File file = view.listFileChooser.getSelectedFile();
            boolean hasIdentical = view.hasIdentical.isSelected();
            boolean hasDifferent = view.hasDifferent.isSelected();
            boolean hasLeft = view.hasLeft.isSelected();
            boolean hasRight = view.hasRight.isSelected();
            boolean hasNotMarked = view.hasNotMarked.isSelected();

            controller.setShowIdentical(hasIdentical);
            controller.setShowDifferent(hasDifferent);
            controller.setShowLeft(hasLeft);
            controller.setShowRight(hasRight);
            controller.setHideMarked(hasNotMarked);

            controller.serializePairs(file);
            controller.pageSaveListDialog_close();
        }
    };

    /**
     * saveListDialog: cancel
     */
    ActionListener cancelSaveListDialogAction = new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
            controller.pageSaveListDialog_close();
        }
    };

    /** */
    MouseInputListener pml = new MouseInputAdapter() {
        public void mousePressed(MouseEvent ev) {
            int x = ev.getX();
            int y = ev.getY();

            controller.moveCursor(x, y);
        }
    };

    /** */
    AdjustmentListener pal = new AdjustmentListener() {
        public void adjustmentValueChanged(AdjustmentEvent ev) {
            controller.updateGraphics();
        }
    };

    /** */
    WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent ev) {
            controller.pageMain_close2();
        }
    };
}

/* */
