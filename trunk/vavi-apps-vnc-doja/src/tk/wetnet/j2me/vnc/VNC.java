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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.StreamConnection;

import com.nttdocomo.io.HttpConnection;
import com.nttdocomo.ui.Button;
import com.nttdocomo.ui.Component;
import com.nttdocomo.ui.ComponentListener;
import com.nttdocomo.ui.Dialog;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.IApplication;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.KeyListener;
import com.nttdocomo.ui.Label;
import com.nttdocomo.ui.ListBox;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;
import com.nttdocomo.ui.Panel;
import com.nttdocomo.ui.TextBox;

import tk.wetnet.vnc.RFBProto;

import vavi.microedition.rms.RecordEnumeration;
import vavi.microedition.rms.RecordStore;


/**
 * VNC. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	040911	nsano	initial version <br>
 */
public class VNC extends IApplication
    implements ComponentListener, Runnable {

    /** */
    RecordStore rs = null;
    /** */
    RecordStore options = null;
    /** <Button, Integer> */
    Hashtable hostCommands = new Hashtable();
    /** */
    VNCCanvas canvas;
    /** */
    RFBProto rfb;
    /** */
    StreamConnection con;
    /** */
    Thread run;

    /* DEBUG CODE > */
    Button log = new Button("Log");
    private Button uploadLog = new Button("Upload");
    private Button backLog = new Button("Back");
    private Frame backDisplayable = null;
    /* < END DEBUG CODE */
    
    private Panel connectionForm = new Panel();
    private Panel connectingForm = new Panel();
    private TextBox url = new TextBox("", 25, 1, TextBox.DISPLAY_ANY);
    private TextBox password = new TextBox("", 14, 1, TextBox.DISPLAY_PASSWORD);
    private Button connect = new Button("#connect#");
    private Button add = new Button("#add#");
    private Button manage = new Button("#manage#");
    private Button delete = new Button("#delete#");
    private Button back = new Button("#back#");
    private Button setProxy = new Button("#setproxy#");
    private ListBox hosts = null;
    private String host = "";
    private int port = 5900;
    private Panel httpProxyForm = new Panel();
    private TextBox httpProxy = new TextBox("", 255, 1, TextBox.DISPLAY_ANY);
//    private Gauge connectionDisplay = new Gauge("#connecting#", false, 5, 0);
    private ListBox shared = new ListBox(ListBox.CHECK_BOX);
    int conD = 0;

    /** */
    protected void incConnectionStatus() {
        conD++;
//        connectionDisplay.setValue(conD);
    }

    /** */
    public void run() {
        if ((url.getText() == null) ||
            ((url.getText() != null) && (url.getText().length() < 1))) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#no_host#");
            a.setText("#host_box_blank#");
            a.show();

            return;
        }

        if ((password.getText() != null) &&
            (password.getText().length() < 6) &&
            (password.getText().length() != 0)) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#password_problem#");
            a.setText("#password_is_too_short#");
            a.show();

            return;
        }

        /**
         * TODO: Add an animated clock while it connects
         */
        Thread.yield();
        incConnectionStatus();
        host = url.getText();

        if (host.indexOf(":") >= 0) {
            try {
                port = Integer.parseInt(host.substring(host.indexOf(":") + 1, host.length()));
            } catch (NumberFormatException nfe) {
                Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
                a.setText("#format_is_wrong#" + host.substring(host.indexOf(":") + 1, host.length()) + "#is_not_a_number#");
                a.show();

                return;
            }

            host = host.substring(0, host.indexOf(":"));
        }

        if (port < 5900) {
            port += 5900;
        }

log("Connecting");

        try {
            if (shared.isIndexSelected(2)) {
log("Use HTTP Socket");
                con = (new tk.wetnet.j2me.httpsocket.HTTPSocket(host + ":" + port, httpProxy.getText()));

                ((tk.wetnet.j2me.httpsocket.HTTPSocket) con).ready();
            } else {
                con = (StreamConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE);
            }

            incConnectionStatus();
log("Connection Open");
        } catch (IllegalArgumentException e) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
            a.setText("#format_is_wrong" + "#please_double_check#" + "\n" + e.toString()); // todo debug var?
            a.show();

            return;
        } catch (ConnectionNotFoundException e) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
            a.setText("#unable_to_connect#" + ((e.getMessage() == null) ? "Is the host correct?" : e.getMessage()) + "?");
            a.show();

            return;
        } catch (IOException e) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
            a.setText("#unknown_problem#" + e.toString());
            a.show();

            return;
        } catch (Throwable t) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
            a.setText("#unknown_problem#" + t.toString());
            a.show();

            return;
        }

        byte[] tmp = password.getText().getBytes();
        byte[] b = new byte[(((tmp.length + 1) > 8) ? (tmp.length + 1) : 8)];
        System.arraycopy(tmp, 0, b, 0, tmp.length);
log("Creating VNC Canvas" + b.length + " " + tmp.length);
        canvas = new VNCCanvas(this);
        incConnectionStatus();
log("Canvas Created");

        try {
log("Creating RFBProto");
            rfb = new RFBProto(((InputConnection) con).openDataInputStream(),
                               con.openOutputStream(), b, canvas,
                               shared.isIndexSelected(0), shared.isIndexSelected(1));
log("RFBProto Created");
        } catch (IOException ioe) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
            a.setText("#unknown_problem#" + ioe.toString());
            a.show();

            return;
        } catch (Throwable t) {
            Dialog a = new Dialog(Dialog.DIALOG_ERROR, "#problem_connecting#");
            a.setText("#unknown_problem#" + t.toString());
            a.show();

            return;
        }

        run = new Thread(rfb);
        run.start();
        cleanUp();
        incConnectionStatus();

        try {
            rs.closeRecordStore();
        } catch (Throwable t) {
t.printStackTrace();
        }
    }

    /** */
    public void componentAction(Component source, int type, int param) {
        if (source == connect) {
            Display.setCurrent(connectingForm);
            new Thread(this).start();
        } else if (source == add) {
            try {
                String tmp = url.getText() + "|" + password.getText();
                Button cm = new Button(url.getText());
                int id = rs.addRecord(tmp.getBytes(), 0, tmp.length());
                hostCommands.put(cm, new Integer(id));
                connectionForm.add(cm);
            } catch (Throwable t) {
t.printStackTrace();
            }
        } else if (source == manage) {
            Panel manageForm = new Panel();
            manageForm.setTitle("#manage_hosts#");
            hosts = new ListBox(ListBox.RADIO_BUTTON);

            try {
                Enumeration e = hostCommands.elements();

//System.out.println(size);
                while (e.hasMoreElements()) {
                    Button current = (Button) e.nextElement();
                    int id = ((Integer) hostCommands.get(current)).intValue();
//System.out.println(i + " " + current.id + " " + current.getLabel());
                    hosts.append(id + " " + current);
                }
            } catch (Throwable t) {
t.printStackTrace();
            }

            manageForm.setComponentListener(this);
            manageForm.add(hosts);
            manageForm.add(delete);
            manageForm.add(back);
            Display.setCurrent(manageForm);
        } else if (source == delete) {
            String removes = hosts.getItem(hosts.getSelectedIndex());
            int remove =
                Integer.parseInt(removes.substring(0, removes.indexOf(" ")));

//System.out.println(remove);
//            hosts.delete(hosts.getSelectedIndex());

            try {
                rs.deleteRecord(remove);
            } catch (Throwable t) {
System.err.println("delete" + t);
            }

            Enumeration e = hostCommands.elements();
            while (e.hasMoreElements()) {
                Button hc = (Button) e.nextElement();
                int id = ((Integer) hostCommands.get(hc)).intValue(); 
                if (id == remove) {
//                    connectionForm.remove(((HostCommand) hostCmds.elementAt(i)).getButton());
                    hostCommands.remove(hc);
                }
            }
        } else if (source == setProxy) {
            if (source == httpProxy) {
                Display.setCurrent(connectionForm);
            } else {
                Panel l = new Panel();
                l.add(httpProxy);
                Display.setCurrent(l);
            }

            /*
             * DEBUG CODE >
             */
        } else if (source == log) {
            Panel l = new Panel();
            l.setTitle(last_line);
            l.add(new Label(complete_log));
            l.setComponentListener(this);
            l.add(backLog);
            l.add(uploadLog);
            backDisplayable = Display.getCurrent();
            Display.setCurrent(l);
        } else if (source == backLog) {
            Display.setCurrent(backDisplayable);
        } else if (source == uploadLog) {
            InputStream is = null;
            OutputStream os = null;
            HttpConnection c1 = null;

            try {
                con.close();

                c1 = (HttpConnection) Connector.open("http://j2mevnc.sourceforge.net/cgi-bin/upload.pl",
                                                    Connector.READ_WRITE, true);

                // Set the request method and headers
                c1.setRequestMethod(HttpConnection.POST);

                // Getting the output stream may flush the headers
                c1.setRequestProperty("If-Modified-Since",
                                      "29 Oct 1999 19:43:31 GMT");
                c1.setRequestProperty("User-Agent", "Upload");
                c1.setRequestProperty("Content-Language", "en-gb");

                os = c1.openOutputStream();
                os.write(complete_log.getBytes());
                os.close();

                // Opening the InputStream will open the connection
                // and read the HTTP headers. They are stored until
                // requested.
                is = c1.openInputStream();

                // Get the ContentType
                String t = c1.getType();

                String s;

                if (c1.getResponseCode() != HttpConnection.HTTP_OK) {
                    s = c1.getResponseCode() + " : " + c1.getResponseMessage();
                } else {
                    // Get the length and process the data
                    int len = (int) c1.getLength();

                    if (len > 0) {
                        byte[] data = new byte[len];
                        int actual = is.read(data);
                        s = new String(data);
                    } else {
                        int ch;
                        s = new String();

                        while ((ch = is.read()) != -1) {
                            s = s + (char) ch;
                        }
                    }
                }

                Dialog a = new Dialog(Dialog.DIALOG_INFO, "Uploaded Log");
                a.setText(s);
                a.show();
            } catch (IOException e) {
                Dialog a = new Dialog(Dialog.DIALOG_INFO, "Upload Failed");
                a.setText(e.toString());
                a.show();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
e.printStackTrace();
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
e.printStackTrace();
                    }
                }

                if (source != null) {
                    try {
                        c1.close();
                    } catch (Exception e) {
e.printStackTrace();
                    }
                }
            }

            /*
             * < END DEBUG CODE
             */
        } else if (source == back) {
            Display.setCurrent(connectionForm);
        } else if (source instanceof Button /* HostCommand */) {
            try {
                Display.setCurrent(connectingForm);

                Button cmd = (Button) source;
                int id = ((Integer) hostCommands.get(cmd)).intValue();
                String tmp = new String(rs.getRecord(id));
                url.setText(tmp.substring(0, tmp.indexOf("|")));
                password.setText(tmp.substring(tmp.indexOf("|") + 1,
                                                 tmp.length()));
                (new Thread(this)).start();
            } catch (Throwable t) {
t.printStackTrace();
            }
        }
    }

    /** */
    protected Image aboutImage = null;

    /** */
    public VNC() {
        connectionForm.add(url);
        connectionForm.add(password);
        connectionForm.add(shared);
        connectionForm.add(connect);
        connectionForm.add(add);
        connectionForm.add(manage);
        connectionForm.add(log);
//        connectionForm.add(setProxy);
        connectionForm.setComponentListener(this);

        httpProxyForm.add(httpProxy);
        httpProxyForm.add(setProxy);
        httpProxyForm.setComponentListener(this);

        shared.append("#shareddesktop#");
        shared.append("#ncm#");
        shared.append("#usehttp#");

        try {
            rs = RecordStore.openRecordStore("hosts", true);

            RecordEnumeration re = rs.enumerateRecords(null, null, false);

            int id = 0; // TODO
            while (re.hasNextElement()) {
                String current = new String(re.nextRecord());
                String title = current;

                if (current.indexOf("|") > 0) {
                    title = current.substring(0, current.indexOf("|"));
                }

                Button cm = new Button(title);
                hostCommands.put(cm, new Integer(id++));
                connectionForm.add(cm);
            }

            options = RecordStore.openRecordStore("options", true);
            re = options.enumerateRecords(null, null, false);

            if (re.hasNextElement()) {
                rid = re.nextRecordId();

                byte[] opts = options.getRecord(rid);

                if ((opts != null) && (opts.length > 0)) {
                    if ((opts[0] & 1) == 1) {
                        shared.select(0);
                    } else {
                    }
                    if ((opts[0] & 2) == 2) {
                        shared.select(1);
                    } else {
                    }

                    if (opts.length > 1) {
                        httpProxy.setText(new String(opts, 1, opts.length - 1));
                    }
                }
            } else {
                rid = -100;
            }

            MediaImage mi = MediaManager.getImage("resource:///VNC.gif");
            mi.use();
            aboutImage = mi.getImage();
//            connectingForm.add(log);
            connectingForm.setKeyListener((KeyListener) this);
//            connectingForm.add(aboutImage);
//            connectingForm.append(connectionDisplay);
        } catch (Throwable t) {
System.err.println("VNC::<init>: " + t.toString());
t.printStackTrace();
        }
    }

    /** */
    private int rid = 0;

    /** */
    protected void pauseApp() {
    }

    /** */
    protected void destroy(boolean parm1) {

        if (canvas != null) {
            canvas.close();
        }

        try {
            byte[] b1 = httpProxy.getText().getBytes();

            byte[] b = new byte[1 + b1.length];
            System.arraycopy(b1, 0, b, 1, b1.length);
            b[0] =
                (byte) ((shared.isIndexSelected(0) ? 1 : 0) +
                (shared.isIndexSelected(1) ? 2 : 0));
            System.out.println(b[0]);

            if (rid != -100) {
                options.setRecord(rid, b, 0, b.length);
                System.out.println("hi");
            } else {
                options.addRecord(b, 0, b.length);
            }

            rs.closeRecordStore();
            options.closeRecordStore();
//	    options.deleteRecordStore("options");
        } catch (Throwable t) {
            System.err.println(t.toString());
        }
    }

    /** */
    public void start() {
        if (canvas == null) {
            Display.setCurrent(connectionForm);
        } else {
            Display.setCurrent(canvas);
        }
    }

    /** */
    private void cleanUp() {
        connectionForm = null;
        connectingForm = null;

        url = null;
        password = null;

        connect = null;

        add = null;
        manage = null;

        delete = null;
        back = null;

        hosts = null;
        host = "";
        System.gc();
    }

    /*
     * DEBUG CODE >
     */
    static String complete_log = "";
    static String last_line = "NOT STARTED TO LOG!";
    
    public static void log(String log) {
        System.out.println(log + "\n");
        last_line = log;
        complete_log += (log + "\n");
    }
    /*
     * < END DEBUG CODE
     */
}

/* */
