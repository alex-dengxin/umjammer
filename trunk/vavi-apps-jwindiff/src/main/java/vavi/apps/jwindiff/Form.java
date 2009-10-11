/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.jwindiff;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;

import vavi.apps.jwindiff.Controller.Order;
import vavi.util.event.GenericEvent;
import vavi.util.event.GenericListener;
import vavi.util.event.GenericSupport;


/**
 * Form. (one of model???)
 * モデルに影響しないデータ。画面遷移等
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060726 nsano initial version <br>
 */
class Form {

    /** view listener */
    private GenericSupport gs = new GenericSupport();

    /** */
    public void addViewListener(GenericListener listener) {
        gs.addGenericListener(listener);
    }

    /** */
    public void viewUpdated(Object source, String name, Object ... arg) {
        gs.fireEventHappened(new GenericEvent(source, name, arg));
    }

    //----

    /** page */
    void pageMain_close() {
        viewUpdated(this, "pageMain_close");
    }

    /** page */
    void pageSaveFile() {
        viewUpdated(this, "pageSaveListDialog");
    }

    /** page */
    void pagePopupOutline(int x, int y) {
        viewUpdated(this, "pagePopupOutline", x, y);
    }

    /** page */
    void pagePopupExpanded(int x, int y) {
        viewUpdated(this, "pagePopupExpanded", x, y);
    }

    /** */
    void initCompareTargetsDialog(File left, File right) {
        viewUpdated(this, "initCompareTargetsDialog", left, right);
    }

    /** page */
    void pageCompareTargetsDialog() {
        viewUpdated(this, "pageCompareTargetsDialog");
    }

    /** */
    void initEditorDialog(File file) {
        viewUpdated(this, "initEditorDialog", file);
    }

    /** page */
    void pageEditorChooser() {
        viewUpdated(this, "pageEditorChooser");
    }

    /** page */
    void pageEditorDialog_close() {
        viewUpdated(this, "pageEditorDialog_close");
    }

    /** page */
    void pagePatternDialog_close() {
        viewUpdated(this, "pagePatternDialog_close");
    }

    /** page */
    void pageTargetsDialog_close() {
        viewUpdated(this, "pageCompareTargetsDialog_close");
    }

    /** page */
    void pageSaveListDialog_close() {
        viewUpdated(this, "pageSaveListDialog_close");
    }

    /** */
    void startSelection(Point point) {
        viewUpdated(this, "startSelection", point);
    }

    /** */
    void endSelection(Point point) {
        viewUpdated(this, "endSelection", point);
    }

    /** */
    void continueSelection(Point point) {
        viewUpdated(this, "continueSelection", point);
    }

    /** */
    void moveCursor(int x, int y) {
        viewUpdated(this, "moveCursor", x, y);
    }

    /** */
    void updateGraphics() {
        viewUpdated(this, "updateGraphics");
    }

    /** */
    void findOutline(Order order) {
        viewUpdated(this, "findOutline", order);
    }

    /** */
    void findExpand(Order order) {
        viewUpdated(this, "findExpand", order);
    }

    /** */
    void setTitle(String title) {
        viewUpdated(this, "setTitle", title);
    }

    /** */
    void markPattern() {
        viewUpdated(this, "pagePatternDialog");
    }

    /** */
    void displaySingleFile(String[] lines) {
        viewUpdated(this, "displaySingleFile", (Object) lines);
    }

    /** */
    void toExpand() {
        viewUpdated(this, "toExpand");
    }

    /** */
    void toOutline() {
        viewUpdated(this, "toOutline");
    }

    /** */
    void toSelection() {
        viewUpdated(this, "toSelection");
    }

    /** */
    void displayException(IOException e) {
        viewUpdated(this, "displayException", e);
    }

    /** */
    void setNames(String name) {
        viewUpdated(this, "setNames", name);
    }

    /** */
    void setPaths(String path) {
        viewUpdated(this, "setPaths", path);
    }

    /** */
    void redisplayExpandedBefore() {
        viewUpdated(this, "redisplayExpandedBefore");
    }

    /** */
    void redisplayOutlineAfter(DefaultListModel listModel, Pair current) {
        viewUpdated(this, "redisplayOutlineAfter", listModel, current);
    }

    /** */
    void redisplayOutlineBefore(String leftFilePath, String rightFilePath) {
        viewUpdated(this, "redisplayOutlineBefore", leftFilePath, rightFilePath);
    }

    /** */
    void pageMain() {
        viewUpdated(this, "pageMain");
    }

    /** */
    void initMain(Model model) {
        viewUpdated(this, "initMain", model);
    }
}

/* */
