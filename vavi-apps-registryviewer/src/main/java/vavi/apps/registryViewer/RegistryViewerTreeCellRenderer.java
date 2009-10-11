/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * ツリーノードのセルレンダラです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 *          1.01 020522 nsano refine <br>
 */
public class RegistryViewerTreeCellRenderer extends DefaultTreeCellRenderer {

    /** */
    static {
        Class<?> clazz = RegistryViewerTreeCellRenderer.class;
        UIDefaults table = UIManager.getDefaults();
        table.put("registryViewer.rootIcon", LookAndFeel.makeIcon(clazz, "resources/root.gif"));
        table.put("registryViewer.collapsedIcon", LookAndFeel.makeIcon(clazz, "resources/collapsed.gif"));
        table.put("registryViewer.expandedIcon", LookAndFeel.makeIcon(clazz, "resources/expanded.gif"));
    }

    /**
     * This is messaged from JTree whenever it needs to get the size of the component or it wants to draw it. This attempts to set
     * the font based on value, which will be a TreeNode.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);

        this.hasFocus = hasFocus;

        setText(stringValue);
        /* Tooltips used by the tree. */
        setToolTipText(stringValue);

        if (selected)
            setForeground(getTextSelectionColor());
        else
            setForeground(getTextNonSelectionColor());

        String name = ((ValueRecode.TreeRecode) ((ValueRecodeTreeNode) value).getUserObject()).getKeyName();

        /* Set the image. */
        if ("HKEY_root".equals(name))
            setIcon(UIManager.getIcon("registryViewer.rootIcon"));
        else if (expanded)
            setIcon(UIManager.getIcon("registryViewer.expandedIcon"));
        else if (!leaf)
            setIcon(UIManager.getIcon("registryViewer.collapsedIcon"));
        else
            setIcon(UIManager.getIcon("registryViewer.collapsedIcon"));

        setComponentOrientation(tree.getComponentOrientation());

        this.selected = selected;

        return this;
    }
}

/* */
