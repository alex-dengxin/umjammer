/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.xml.util;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * JTreePrinter.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 060925 nsano initial version <br>
 */
public class JTreePrinter {

    private boolean showWS;

    /** */
    public JTreePrinter() {
        this(false);
    }

    /**
     * @param showWS show whote space 
     */
    public JTreePrinter(boolean showWS) {
        this.showWS = showWS;
    }

    /**
     * TODO tree renderer
     * TODO scroll pane
     * TODO xpath search, create
     */
    public void print(Node node) throws IOException {

        TreeNode treeNode = new TreeNode(node);
        if (node.hasChildNodes()) {
            addChildNodes(treeNode, node);
        }

        TreeModel treeModel = new DefaultTreeModel(treeNode);        

        JTree tree = new JTree();
        tree.setPreferredSize(new Dimension(640, 480));
        tree.setModel(treeModel);

        JScrollPane sp = new JScrollPane();
        sp.setViewportView(tree);
        sp.setAutoscrolls(true);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(node.getNodeName());
        frame.setContentPane(sp);
        frame.pack();
        frame.setVisible(true);
    }

    private void addChildNodes(TreeNode treeNode, Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (!showWS) {
//System.err.println(childNode.getNodeType() + ", " + Node.TEXT_NODE + ", " + (childNode.getNodeValue() != null ? StringUtil.getDump(childNode.getNodeValue()) : "null"));
                if (childNode.getNodeType() == Node.TEXT_NODE &&
                    (childNode.getNodeValue() == null ||
                     childNode.getNodeValue().equals("") ||
                     childNode.getNodeValue().matches("\\s*"))) {
                    continue;
                }
            }

            TreeNode childTreeNode = new TreeNode(childNode);
            treeNode.add(childTreeNode);        

            if (childNode.hasChildNodes()) {
                addChildNodes(childTreeNode, childNode);
            }
        }
    }

    /** */
    class TreeNode extends DefaultMutableTreeNode {
        /** */
        TreeNode(Node userObject) {
            super(userObject);
        }
        /** */
        public String toString() {
            Node node = Node.class.cast(userObject);
            StringBuffer sb = new StringBuffer();
            sb.append("<");
            sb.append(node.getNodeName());
            sb.append("/>  ");
            NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                sb.append(attrs.item(i).getNodeName());
                sb.append('=');
                sb.append(attrs.item(i).getNodeValue());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            return sb.toString();
        }
    }
}

/* */
