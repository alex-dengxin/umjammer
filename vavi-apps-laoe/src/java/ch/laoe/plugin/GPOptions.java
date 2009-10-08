/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import ch.laoe.ui.Debug;
import ch.laoe.ui.GDialog;
import ch.laoe.ui.GLanguage;
import ch.laoe.ui.GPersistance;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlText;


/**
 * general LAoE options editor.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.06.01 first draft oli4
 */
public class GPOptions extends GPluginFrame {
    public GPOptions(GPluginHandler ph) {
        super(ph);
        initGui();
        restoreOptions();
    }

    protected String getName() {
        return "laoeOptions";
    }

    // GUI
    private JTree tree;

    private JPanel nodePanel;

    private JTextField language;

    private UiControlText debugTraceLevel;

    private JCheckBox autoCloseEnable;

    private JCheckBox undoHistoryEnable, undoHistoryCompression;

    private UiControlText clipWidth, clipHeight;

    // private UiControlText zeroCrossWidth;
    private JTextArea defaultComment;

    private UiControlText fontSize;

    private JComboBox font;

    private JButton apply;

    private EventDispatcher eventDispatcher;

    private void initGui() {
        // create tree model
        DefaultMutableTreeNode model = new DefaultMutableTreeNode(GLanguage.translate("laoe"));
        // general
        DefaultMutableTreeNode n1 = new DefaultMutableTreeNode(GLanguage.translate("general"));
        model.add(n1);
        // font
        OptionNode n14 = new OptionNode(GLanguage.translate("font"));
        n1.add(n14);
        n14.add(new JLabel(GLanguage.translate("font")), 0, 0, 3, 1);
        font = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        n14.add(font, 3, 0, 5, 1);
        n14.add(new JLabel(GLanguage.translate("size")), 0, 1, 3, 1);
        fontSize = new UiControlText(5, true, false);
        n14.add(fontSize, 3, 1, 4, 1);
        // language
        OptionNode n11 = new OptionNode(GLanguage.translate("language"));
        n1.add(n11);
        n11.add(new JLabel(GLanguage.translate("language")), 0, 0, 4, 1);
        language = new JTextField(7);
        n11.add(language, 4, 0, 4, 1);
        // debug
        OptionNode n12 = new OptionNode(GLanguage.translate("debug"));
        n1.add(n12);
        n12.add(new JLabel(GLanguage.translate("verboseLevel")), 0, 0, 4, 1);
        debugTraceLevel = new UiControlText(7, true, false);
        debugTraceLevel.setDataRange(0, 10);
        n12.add(debugTraceLevel, 4, 0, 4, 1);
        // history
        OptionNode n13 = new OptionNode(GLanguage.translate("undoStack"));
        n1.add(n13);
        undoHistoryEnable = new JCheckBox(GLanguage.translate("enable"));
        n13.add(undoHistoryEnable, 0, 0, 5, 1);
        undoHistoryCompression = new JCheckBox(GLanguage.translate("compression"));
        n13.add(undoHistoryCompression, 0, 1, 5, 1);
        // main frame
        DefaultMutableTreeNode n2 = new DefaultMutableTreeNode(GLanguage.translate("mainFrame"));
        model.add(n2);
        // buttons
        n2.add(new DefaultMutableTreeNode(GLanguage.translate("buttons")));
        // plugins
        DefaultMutableTreeNode n3 = new DefaultMutableTreeNode(GLanguage.translate("plugins"));
        model.add(n3);
        // auto-close
        OptionNode n31 = new OptionNode(GLanguage.translate("autoClose"));
        n3.add(n31);
        autoCloseEnable = new JCheckBox(GLanguage.translate("enable"));
        n31.add(autoCloseEnable, 0, 0, 5, 1);
        // clip
        DefaultMutableTreeNode n4 = new DefaultMutableTreeNode(GLanguage.translate("clip"));
        model.add(n4);
        // frame size
        OptionNode n41 = new OptionNode(GLanguage.translate("frameSize"));
        n4.add(n41);
        n41.add(new JLabel(GLanguage.translate("width")), 0, 0, 4, 1);
        clipWidth = new UiControlText(7, true, false);
        clipWidth.setDataRange(30, 1000);
        n41.add(clipWidth, 4, 0, 4, 1);
        n41.add(new JLabel(GLanguage.translate("height")), 0, 1, 4, 1);
        clipHeight = new UiControlText(7, true, false);
        clipHeight.setDataRange(30, 700);
        n41.add(clipHeight, 4, 1, 4, 1);
        /*
         * //zeroCross OptionNode n42 = new OptionNode(GLanguage.translate("zeroCross")); n4.add(n42); n42.add(new
         * JLabel(GLanguage.translate("zeroCrossWidth")), 0, 0, 4, 1); zeroCrossWidth = new UiControlText(7, true, false);
         * zeroCrossWidth.setDataRange(10, 100); n42.add(zeroCrossWidth, 4, 0, 4, 1);
         */
        // default comment
        OptionNode n43 = new OptionNode(GLanguage.translate("defaultComment"));
        n4.add(n43);
        defaultComment = new JTextArea(4, 19);
        n43.add(defaultComment, 0, 0, 8, 3);

        // GUI
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        // GCartesianLayout layout = new GCartesianLayout(p, 10, 8);
        // layout.setPreferredCellSize(new Dimension(25, 35));
        // p.setLayout(layout);

        // tree
        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setScrollsOnExpand(true);
        tree.setShowsRootHandles(true);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setOpenIcon(loadIcon("resources/open.gif"));
        renderer.setClosedIcon(loadIcon("resources/close.gif"));
        renderer.setLeafIcon(loadIcon("resources/laoe.gif"));
        tree.setCellRenderer(renderer);
        JScrollPane scrolledTree = new JScrollPane(tree);

        // node panel
        nodePanel = new JPanel();
        nodePanel.setLayout(new BorderLayout());
        JScrollPane scrolledNodePanel = new JScrollPane(nodePanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrolledTree, scrolledNodePanel);
        // layout.add(splitPane, 0, 0, 10, 7);
        p.add(splitPane, BorderLayout.CENTER);

        // apply
        apply = new JButton(GLanguage.translate("apply"));
        // layout.add(apply, 3, 7, 4, 1);
        JPanel southP = new JPanel();
        southP.add(apply);
        p.add(southP, BorderLayout.SOUTH);

        frame.getContentPane().add(p);
        pack();
        frame.setResizable(true);
        frame.setSize(300, 400);
        splitPane.setDividerLocation(.5);

        eventDispatcher = new EventDispatcher();
        apply.addActionListener(eventDispatcher);
        tree.addTreeSelectionListener(eventDispatcher);
    }

    /**
     * this class encapsulates node-name and node configuration panel
     */
    private class OptionNode extends DefaultMutableTreeNode {
        public OptionNode(String name) {
            this.name = name;
            panel = new JPanel();
            layout = new UiCartesianLayout(panel, 8, 4);
            layout.setPreferredCellSize(new Dimension(25, 35));
            panel.setLayout(layout);
        }

        private String name;

        public String toString() {
            return name;
        }

        private JPanel panel;

        private UiCartesianLayout layout;

        public void add(JComponent c, int x, int y, int w, int h) {
            layout.add(c, x, y, w, h);
        }

        public JPanel getPanel() {
            return panel;
        }
    }

    private class EventDispatcher implements ActionListener, TreeSelectionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == apply) {
                Debug.println(1, "plugin " + getName() + " [apply] clicked");
                onApply();
                autoCloseFrame();
            }
        }

        private JPanel actualNode;

        public void valueChanged(TreeSelectionEvent e) {
            Object c = ((DefaultMutableTreeNode) e.getPath().getLastPathComponent());
            // hide old panel...
            if (actualNode != null) {
                actualNode.setVisible(false);
            }

            // show selected panel...
            if (c instanceof OptionNode) {
                actualNode = ((OptionNode) c).getPanel();
                nodePanel.add(actualNode, BorderLayout.CENTER);
                actualNode.setVisible(true);
                nodePanel.validate();
            }
        }
    }

    private void restoreOptions() {
        GPersistance p = GPersistance.createPersistance();
        language.setText(p.getString("language"));
        debugTraceLevel.setData(p.getInt("debug.verboseLevel"));
        autoCloseEnable.setSelected(p.getBoolean("plugin.autoCloseFrame"));
        undoHistoryEnable.setSelected(p.getBoolean("history.enable"));
        undoHistoryCompression.setSelected(p.getBoolean("history.compression"));
        clipWidth.setData(p.getInt("clip.frameSize.width"));
        clipHeight.setData(p.getInt("clip.frameSize.height"));
        // zeroCrossWidth.setData(p.getInt("operation.toolkit.zeroCrossWidth"));
        defaultComment.setText(p.getString("clip.defaultComment"));
        font.setSelectedItem(p.getString("font.name"));
        fontSize.setData(p.getInt("font.size"));
    }

    private void onApply() {
        GPersistance p = GPersistance.createPersistance();
        p.setString("language", language.getText());
        p.setInt("debug.verboseLevel", (int) debugTraceLevel.getData());
        p.setBoolean("plugin.autoCloseFrame", autoCloseEnable.isSelected());
        p.setBoolean("history.enable", undoHistoryEnable.isSelected());
        p.setBoolean("history.compression", undoHistoryCompression.isSelected());
        p.setInt("clip.frameSize.width", (int) clipWidth.getData());
        p.setInt("clip.frameSize.height", (int) clipHeight.getData());
        // p.setInt("operation.toolkit.zeroCrossWidth", (int)zeroCrossWidth.getData());
        p.setString("clip.defaultComment", defaultComment.getText());
        p.setString("font.name", (String) font.getSelectedItem());
        p.setInt("font.size", (int) fontSize.getData());
        GDialog.showWarningDialog(frame, GLanguage.translate("information"), GLanguage.translate("changesWillBeDoneAfterNewStartOfLaoe"));
    }

}
