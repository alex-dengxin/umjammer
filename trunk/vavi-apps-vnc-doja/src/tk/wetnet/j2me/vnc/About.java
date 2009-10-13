/*
 * This file is part of J2ME VNC.
 *
 * Copyright (c) 2003 Michael Lloyd Lee
 *
 * J2ME VNC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * J2ME VNC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2ME VNC; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package tk.wetnet.j2me.vnc;

import com.nttdocomo.ui.Button;
import com.nttdocomo.ui.Component;
import com.nttdocomo.ui.ComponentListener;
import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.Label;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;
import com.nttdocomo.ui.Panel;
import com.nttdocomo.ui.TextBox;

import vavi.microedition.rms.RecordEnumeration;
import vavi.microedition.rms.RecordStore;


/**
 * About. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040911	nsano	initial version <br>
 */
public class About extends Panel
    implements ComponentListener {

    Button exit = new Button("#exit#");
    Button delete = new Button("#reset_hosts#");
    Button gpl = new Button("#gpl#");
    Button mit = new Button("#mit#");
    Button tranlators = new Button("#translators#");
    Button buts = new Button("#buts#");
    Button editCmds = new Button("#edit_cmds#");
    Button back = new Button("#back#");
    Button update = new Button("#update#");
    Button add = new Button("#add#");
    VNCCanvas c = new VNCCanvas(); // why ohh why arn't they static?
    Panel about = new Panel();
    Image ab = null;
    Dialog gplAlert = null;
    Dialog mitAlert = null;
    Dialog tranAlert = null;
    Dialog butsAlert = null;
    Panel commandForm = null;
    RecordStore rs;
    TextBox name = new TextBox("", 255, 1, TextBox.DISPLAY_ANY);
    TextBox macro = new TextBox("", 255, 1, TextBox.DISPLAY_ANY);
    Button currentID = null;

    public About() {
        super();

        try {
            MediaImage mi = MediaManager.getImage("resource:///VNC.gif");
            mi.use();
            ab = mi.getImage();
//            about.add(ab);
        } catch (Throwable t) {
t.printStackTrace();
        }

        about.add(new Label("#aboutstr#\n#mark#\n#konrad#\n#me#"));

        gplAlert = new Dialog(Dialog.DIALOG_INFO, "#gpl#");
        gplAlert.setText("#gplstr#");
        butsAlert = new Dialog(Dialog.DIALOG_INFO, "#buts#");
        butsAlert.setText(
            "#up#" +
            c.getKeypadState(Display.KEY_UP) + "\n#down#" +
            c.getKeypadState(Display.KEY_DOWN) + "\n#left#" +
            c.getKeypadState(Display.KEY_LEFT) + "\n#right#" +
            c.getKeypadState(Display.KEY_RIGHT) + "\n#fire#" +
            c.getKeypadState(Display.KEY_SELECT) + "\n#game_a#" +
            c.getKeypadState(Display.KEY_SOFT1));
        mitAlert = new Dialog(Dialog.DIALOG_INFO, "#mit#");
        mitAlert.setText("#mitstr#");
        tranAlert = new Dialog(Dialog.DIALOG_INFO, "#translators#");
        tranAlert.setText("#thanks#\n" + "Steffen Menne\n" +
                          "for the German version\n" + "Angela Antoniou\n" +
                          "for the Greek version\n" + "Nythil\n" +
                          "for the Polish version\n" + "Emeric Laroche\n" +
                          "for the French version");

        commandForm = new Panel();
        commandForm.add(name);
        commandForm.add(macro);
        commandForm.setComponentListener(this);

        commandForm.add(back);
        commandForm.add(add);

        try {
            rs = RecordStore.openRecordStore("cmds", true);

            if (rs.getNumRecords() == 0) {
                byte[] s = "Ctrl Alt Del|<\\c<\\a!\\d>\\a>\\c".getBytes();
                rs.addRecord(s, 0, s.length);
            }

            RecordEnumeration re = rs.enumerateRecords(null, null, false);

            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                String current = new String(rs.getRecord(id));
                String title = current;

                if (current.indexOf("|") > 0) {
                    title = current.substring(0, current.indexOf("|"));
                }

                Button cm = new Button(title);
                c.midlet.hostCommands.put(cm, new Integer(id));
                commandForm.add(cm);
            }
        } catch (Exception t) {
System.err.println("About:<init>: " + t.toString());
t.printStackTrace();
        }

        /*
         * My bad haiku
         * Memory of Net Positive
         * it is in.
         */
        about.add(exit);
        about.add(gpl);
        about.add(buts);
        about.add(mit);
        about.add(editCmds);
        about.add(tranlators);
        about.add(delete);
        about.setComponentListener(this);
    }

    /** */
    public void componentAction(Component source, int type, int param) {

        try {
            if (source == back) {
                Display.setCurrent(about);
            } else if (source == add) {
                byte[] s = (name.getText().replace('|', 'p') + "|" +
                            macro.getText().replace('|', 'p')).getBytes();
                int id = rs.addRecord(s, 0, s.length);
                Button hostCommand = new Button(name.getText().replace('|', 'p'));
                c.midlet.hostCommands.put(hostCommand, new Integer(id));
                commandForm.add(hostCommand);
//                commandForm.remove(add);
                commandForm.add(update);
                currentID = hostCommand;
            } else if (source == update) {
                byte[] s =
                    (name.getText().replace('|', 'p') + "|" +
                     macro.getText().replace('|', 'p')).getBytes();
                int id = ((Integer) c.midlet.hostCommands.get(currentID)).intValue();
                rs.setRecord(id, s, 0, s.length);
//                commandForm.remove(currentID.b);
                currentID = new Button(name.getText().replace('|', 'p'));
                c.midlet.hostCommands.put(currentID, new Integer(id));
                commandForm.add(currentID);
            } else if (source instanceof Button /* HostCommand */) {
                int id = ((Integer) c.midlet.hostCommands.get(source)).intValue();
                String tmp = new String(rs.getRecord(id));
                name.setText(tmp.substring(0, tmp.indexOf("|")));
                macro.setText(tmp.substring(tmp.indexOf("|") + 1, tmp.length()));
//                commandForm.remove(add);
                commandForm.add(update);
                currentID = (Button) source;
            }
        } catch (Throwable e) {
e.printStackTrace();
            Dialog dialog = new Dialog(Dialog.DIALOG_ERROR, "Error");
            dialog.setText(e.toString());
            dialog.show();
        }

        if (source == exit) {
            destroy(true);
        } else if (source == gpl) {
            Display.setCurrent(gplAlert);
        } else if (source == mit) {
            Display.setCurrent(mitAlert);
        } else if (source == tranlators) {
            Display.setCurrent(tranAlert);
        } else if (source == editCmds) {
            Display.setCurrent(commandForm);
        } else if (source == tranlators) {
            Display.setCurrent(tranAlert);
        } else if (source == buts) {
            Display.setCurrent(butsAlert);
        } else if (source == delete) {
            try {
                RecordStore.deleteRecordStore("hosts");
            } catch (Throwable e) {
e.printStackTrace();
            }
        }
    }

    /** */
    public void destroy(boolean parm1) {
        try {
            rs.closeRecordStore();
        } catch (Throwable t) {
t.printStackTrace();
        }
    }

    /** */
    public void start() {
        Display.setCurrent(about);
    }
}

/* */
