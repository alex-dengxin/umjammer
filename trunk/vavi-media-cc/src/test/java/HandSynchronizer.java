/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
//import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import vavi.media.ui.cc.Scheduler;
import vavi.media.ui.cc.Synchronizer;


/**
 * HandSynchronizer.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030305 nsano initial version <br>
 */
public class HandSynchronizer extends JFrame implements Synchronizer {

    private JTextField timeField;
    private JButton syncButton;

    private Scheduler scheduler;

    public HandSynchronizer(Scheduler scheduler) {

        this.scheduler = scheduler;

//      timeField = new JTextField(7);
        syncButton = new JButton(syncAction);
        setTitle("Synchronizer");
        setSize(120, 64);
        getContentPane().setLayout(new FlowLayout());
//      getContentPane().add(timeField);
        getContentPane().add(syncButton);

        setVisible(true);
    }

    private long time = 0;

    private Action syncAction = new AbstractAction("Synchronize") {
        public void actionPerformed(ActionEvent ev) {
//          SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss z");
//          try {
//              time = sdf.parse(timeField.getText() + " GMT").getTime();
                synchronize();
//          } catch (ParseException e) {
//Debug.println(e);
//          }
            setVisible(false);
        }
    };

    /** */
    public void synchronize() {
        scheduler.start();
    }
}

/* */
