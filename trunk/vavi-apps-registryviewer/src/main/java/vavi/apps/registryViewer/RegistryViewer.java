/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import vavi.util.Debug;
import vavi.util.RegexFileFilter;


/**
 * レジストリビューアです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 *          1.01 020430 nsano change VlueRecode::<init> arg <br>
 *          1.10 020430 nsano refine, no search result bug fix <br>
 *          1.11 020503 nsano refine <br>
 */
public class RegistryViewer {

    /** Window for showing Tree. */
    private JFrame frame;

    /** Tree used for the example. */
    private JTree tree;

    /** Tree model. */
    private DefaultTreeModel treeModel;

    private JTable table;

    private ValueRecode valueRecode;

    /**
     * Constructs a new instance of RegistryViewer.
     */
    public RegistryViewer(File file) throws IOException {

        JMenuBar menuBar = constructMenuBar();

        frame = new JFrame("RegistryViewer");
        frame.setJMenuBar(menuBar);

        TableColumnModel tcm = new DefaultTableColumnModel();
        TableColumn tc;
        tc = new TableColumn(0, 150);
        tc.setHeaderValue("Name");
        tcm.addColumn(tc);
        tc = new TableColumn(1, 300);
        tc.setHeaderValue("Data");
        tcm.addColumn(tc);

        table = new JTable(new ValueRecodeTableModel(), tcm);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.getColumn("Name").setCellRenderer(new ValueRecodeTableCellRenderer());

//      ToolTipManager.sharedInstance().registerComponent(table);

        /* Create the tree. */
        tree = new JTree();

        open(file);

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // fill right table
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    fillTable((ValueRecodeTreeNode) path.getLastPathComponent());
                }
            }
        });

        // Enable tool tips for the tree, without this tool tips
        // will not be picked up.
//      ToolTipManager.sharedInstance().registerComponent(tree);

        tree.setCellRenderer(new RegistryViewerTreeCellRenderer());

        /* Make tree ask for the height of each row. */
        tree.setRowHeight(-1);

        /* Put the Tree in a scroller. */
        JScrollPane left = new JScrollPane(tree);
        left.setPreferredSize(new Dimension(300, 500));

        JScrollPane right = new JScrollPane();
        right.setPreferredSize(new Dimension(450, 500));
        right.getViewport().setLayout(new BorderLayout());
        right.getViewport().add(BorderLayout.CENTER, table);

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        sp.setBackground(Color.white);

        JToolBar toolBar = constructToolBar();

        /* And show it. */

        frame.getContentPane().add(BorderLayout.CENTER, sp);
        frame.getContentPane().add(BorderLayout.NORTH, toolBar);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
    }

    /** */
    private void open(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        valueRecode = new ValueRecode(is);

        searchResults.removeAllElements();

        /* Create the JTreeModel. */
        TreeNode root = new ValueRecodeTreeNode(valueRecode.getRoot());
        treeModel = new DefaultTreeModel(root);

        tree.setModel(treeModel);

        fillTable((ValueRecodeTreeNode) root);
    }

    /** */
    private void fillTable(ValueRecodeTreeNode node) {
        table.setModel(((ValueRecode.TreeRecode) node.getUserObject()).getValue());
        table.repaint();
    }

    /** The search box on the tool bar */
    private JComboBox searchTexts;

    /** Creates the tool bar */
    private JToolBar constructToolBar() {
        JToolBar toolBar = new JToolBar();
        JButton button;
        // ImageIcon icon;
        final Insets insets0 = new Insets(0, 0, 0, 0);

//      toolBar.setMargin(insets0);
        toolBar.setFloatable(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));

        button = (JButton) toolBar.add(new JButton(openAction));
        button.setMargin(insets0);
        button.setToolTipText(button.getText());
        button.setText("");

        searchTexts = (JComboBox) toolBar.add(new JComboBox());
        searchTexts.setEditable(true);
        searchTexts.setSize(40, 16);
        searchTexts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                searchAction.actionPerformed(ev);
            }
        });

        button = (JButton) toolBar.add(new JButton(searchAction));
        button.setMargin(insets0);
        button.setToolTipText(button.getText());
        button.setText("");
//      button.setDefaultCapable(true);

        return toolBar;
    }

    /** The open action */
    private Action openAction = new AbstractAction("Open", UIManager.getIcon("registryViewer.openIcon")) {

        private String lastPath = System.getProperty("user.dir");

        private JFileChooser fc = new JFileChooser();

        private RegexFileFilter filter = new RegexFileFilter("^.+\\.[dD][aA][tT]$", "Windows Registry");

        {
            fc.setFileFilter(filter);
        }

        public void actionPerformed(ActionEvent ev) {
            fc.setCurrentDirectory(new File(lastPath));
            int returnVal = fc.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    open(fc.getSelectedFile());
                    lastPath = fc.getSelectedFile().getPath();
                } catch (IOException e) {
                    Debug.printStackTrace(e);
                }
            }
        }
    };

    /** Search results of TreePath objects */
    private Vector<TreePath> searchResults = new Vector<TreePath>();

    /** The search action */
    private Action searchAction = new AbstractAction("Search", UIManager.getIcon("registryViewer.searchIcon")) {

        /** The previous searched text */
        private String text;

        /** The current index in search result */
        private int index = 0;

        /** */
        public void actionPerformed(ActionEvent ev) {
            addItemToSearchTexts();

            // clear if search text changed

            if (!((String) searchTexts.getSelectedItem()).equals(text)) {
                searchResults.removeAllElements();
                text = (String) searchTexts.getSelectedItem();
            }

            if (searchResults.size() == 0) {

                // new search

                index = 0;

                ValueRecodeTreeNode root = (ValueRecodeTreeNode) treeModel.getRoot();

                searchAll(root, text);
            } else {
                // next point

                if (index + 1 == searchResults.size()) {
                    index = 0;
                } else {
                    index++;
                }
            }

            if (searchResults.size() > 0) {
                TreePath path = searchResults.elementAt(index);
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
//              tree.repaint();
                fillTable((ValueRecodeTreeNode) path.getLastPathComponent());
// Debug.println(": done");
            }
// ValueRecodeTreeNode node = search(root, text); if (node != null) { Debug.println(node.getAbsoluteName()); } else { Debug.println("not found"); }
        }

        /** */
        @SuppressWarnings("unused")
        private ValueRecodeTreeNode search(ValueRecodeTreeNode parent, String string) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                ValueRecodeTreeNode child = (ValueRecodeTreeNode) parent.getChildAt(i);
                if (child.getChildCount() > 0) {
                    ValueRecodeTreeNode node = search(child, string);
                    if (node != null) {
                        return node;
                    }
                }
                if (child.contains(string)) {
                    return child;
                }
            }

            return null;
        }

        /** */
        private void searchAll(ValueRecodeTreeNode parent, String string) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                ValueRecodeTreeNode child = (ValueRecodeTreeNode) parent.getChildAt(i);
                if (child.contains(string)) {
// Debug.println(child.getAbsoluteName());
                    searchResults.addElement(new TreePath(child.getPath()));
                }
                if (child.getChildCount() > 0) {
                    searchAll(child, string);
                }
            }
        }

        /** */
        private void addItemToSearchTexts() {
            String c = (String) searchTexts.getSelectedItem();

            for (int i = 0; i < searchTexts.getItemCount(); i++) {
                if (searchTexts.getItemAt(i).equals(c)) {
                    return;
                }
            }

            searchTexts.addItem(c);
        }
    };

    /** Construct a menu. */
    private JMenuBar constructMenuBar() {
        JMenu menu;
        JMenuBar menuBar = new JMenuBar();
        JMenuItem menuItem;

        /* Good ol exit. */
        menu = new JMenu("File");
        menu.setMnemonic('F');
        menuBar.add(menu);

        menuItem = menu.add(openAction);
        menuItem.setMnemonic('O');

        menu.addSeparator();

        menuItem = menu.add(new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menuItem.setMnemonic('x');

        return menuBar;
    }

    // -------------------------------------------------------------------------

    /** */
    static {
        Toolkit t = Toolkit.getDefaultToolkit();
        Class<?> clazz = RegistryViewer.class;
        UIDefaults table = UIManager.getDefaults();
        table.put("registryViewer.openIcon", new ImageIcon(t.getImage(clazz.getResource("resources/open.gif"))));
        table.put("registryViewer.searchIcon", new ImageIcon(t.getImage(clazz.getResource("resources/search.gif"))));
    }

    /** */
    public static void main(String[] args) throws Exception {
//      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new RegistryViewer(new File(args[0]));
    }
}

/* */
