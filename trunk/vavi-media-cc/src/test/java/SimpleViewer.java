/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import vavi.media.ui.cc.ClosedCaption;
import vavi.media.ui.cc.Viewer;
import vavi.util.Debug;


/**
 * サブタイトルビューアのサンプルです。
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030218 nsano initial version <br>
 *          0.01 030305 nsano be simple <br>
 */
public class SimpleViewer extends Window implements Viewer {

    /** */
    private JLabel label;

    /** */
    public SimpleViewer() {
        super(new Frame());

        setLayout(new BorderLayout());
        JLayeredPane layer = new JLayeredPane();
        layer.setPreferredSize(new Dimension(640, 100));
        add(layer, BorderLayout.CENTER);

        label = new JLabel();
        label.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 40));
        label.setForeground(Color.blue);
        label.setSize(new Dimension(640, 100));
        layer.add(label, JLayeredPane.PALETTE_LAYER);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        pack();

        setLocation((screen.width - getWidth()) / 2,
                    (screen.height / 4 - getHeight()) / 2 +
                     screen.height / 4 * 3);

        setVisible(true);
    }

    /** */
    public void showClosedCaption(ClosedCaption cc) {
Debug.println(cc.getText());
        label.setText(cc.getText());
    }
}

/* */
