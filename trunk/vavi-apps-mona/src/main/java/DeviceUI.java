/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * DeviceUI.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040325 nsano initial version <br>
 */
public class DeviceUI {

    /** */
    protected Device device;

    /** */
    private long time = 0;

    /** */
    private void writeCommand(int command) {
        try {
            device.writeCommand(3, command);

            if (time != 0) {
                long diff = System.currentTimeMillis() - time;
System.out.println("    <command>");
System.out.println("      <name>sleep</name>");
System.out.println("      <value>" + diff + "</value>");
System.out.println("    </command>");
            } else {
System.out.println("<CommandList>");
System.out.println("  <commands>");
            }

            time = System.currentTimeMillis();

            String value = device.getKeyName(command);
System.out.println("    <command>");
System.out.println("      <name>key</name>");
System.out.println("      <value>" + value + "</value>");
System.out.println("    </command>");
        } catch (IOException e) {
Debug.printStackTrace(e);
        }
    }

    /** */
    public void finalize() throws Throwable {
        System.out.println("  </commands>");
        System.out.println("</commandList>");
    }

    /** */
    protected ActionListener tfActionListener =
        new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String value = ((JTextField) ev.getSource()).getText();
                int c = Integer.parseInt(value, 16);
Debug.println("command: " + StringUtil.toHex2(c));
                writeCommand(c);
            }
        };

    /** */
    protected Action[] actions = new Action[256];

    /** */
    private class KeyAction extends AbstractAction {
        /** */
        private int keyNumber;
        /** */
        public KeyAction(int keyNumber, String name) {
            super(name);
            this.keyNumber = keyNumber;
        }
        /** */
        public void actionPerformed(ActionEvent ae) {
            writeCommand(keyNumber);
        }
    }

    /** */
    private void initActions() {
        for (int i = 0; i < 255; i++) {
            String keyName = device.getKeyName(i);
            if (keyName != null) {
                String displayName = device.getDisplayName(keyName);
                Action action = new KeyAction(i, displayName);
                actions[i] = action;
            }
        }
    }

    /** */
    private static final String[] keyOrder = {
        "NW", "UP", "NE",
        "LEFT", "CENTER", "RIGHT",
        "SW", "DOWN", "SE",
        "ONHOOK", "CLEAR", "OFFHOOK",
        "NUMPAD1" , "NUMPAD2", "NUMPAD3",
        "NUMPAD4", "NUMPAD5", "NUMPAD6",
        "NUMPAD7", "NUMPAD8", "NUMPAD9",
        "ASTERISK", "NUMPAD0", "SHARP"
    };

    /** */
    public DeviceUI(Device device) throws IOException {

        this.device = device;

        JPanel base = new JPanel();
        base.setLayout(new BorderLayout());

        JTextField tf = new JTextField();
        tf.addActionListener(tfActionListener);
        base.add(tf, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(180, 320));
        p.setLayout(new GridLayout(8, 3));

        initActions();

        for (int i = 0; i < keyOrder.length; i++) {
            JButton b = new JButton();
            b.setAction(actions[device.getKeyCode(keyOrder[i])]);
            p.add(b);
        }

        base.add(p);

        JFrame f = new JFrame(device.getDeviceId());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(base);
        f.pack();
        f.setVisible(true);
    }
}

/* */
