

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import vavi.net.www.protocol.p2p.P2PConnection;
import vavi.net.www.protocol.p2p.P2PConnectionEvent;
import vavi.net.www.protocol.p2p.P2PConnectionListener;
import vavi.net.www.protocol.p2p.Peer;
import vavi.net.www.protocol.stun.StunURLConnection;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * @author suno
 * Created on 2003/07/08
 */
public class SampleApplication {

    /** */
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OptionBuilder.withDescription("use CUI")
                          .create("c"));

        CommandLineParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);

        try {
            new SampleApplication(cl.getArgs()[0], cl.hasOption("c"));
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /** */
    private Presentation presentation;

    /** */
    private StunURLConnection connection;

    /** */
    private OutputStream bloadcastOutputStream;

    /** */
    SampleApplication(String url, boolean isCui) throws IOException {
Debug.println("reading default node:" + url);

        if (isCui) {
            this.presentation = new CharacterPresentation();
        } else {
            this.presentation = new SwingPresentation();
        }

        connection = (StunURLConnection) new URL(url).openConnection(); 
        connection.addP2PConnectionListener(new P2PConnectionListener() {
            public void dataArrived(P2PConnectionEvent event) {
                byte[] data = (byte[]) event.getData();
                presentation.addNewMessage(new String(data));
            }
            public void newNode(P2PConnectionEvent event) {
                Peer peer = event.getPeer();
                presentation.addNewUserID(peer.getName());
            }
            public void connected(P2PConnectionEvent event) {
                P2PConnection connection = event.getConnection();
                presentation.addNewMessage("connection established with:" + connection);
                presentation.showBoard(connection);
            }
        });
        bloadcastOutputStream = connection.getOutputStream();
    }

    /** */
    interface Presentation {
        void addNewMessage(String message);
        void addNewUserID(String id);
        void showBoard(P2PConnection connection);
    }

    /**
     */
    class CharacterPresentation implements Presentation {
        /** */
        ExecutorService service;
        /** */
        CharacterPresentation() {
            service = Executors.newSingleThreadExecutor();
            service.execute(handler);
        }
        
        Runnable handler = new Runnable() {
            public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    try {
                        System.out.print(">");
                        System.out.flush();
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.length() == 0) {
                            continue;
                        }
    
                        String[] args = StringUtil.splitCommandLine(line);
    
                        switch (args[0].charAt(0)) {
                        case 'b':
                            addNewMessage("sending a Broadcast Message");
                            bloadcastOutputStream.write(args[1].getBytes());
                            break;
                        case 'u':
                            addNewMessage("sending a unicast Message to " + args[1]);
                            unicastOutputStream.write(args[1].getBytes());
                            break;
                        case 'd':
                            addNewMessage("requesting a direct connection to " + args[1]);
                            connection.connect(args[1]);
                            break;
                        case 'q':
                            return;
                        default:
                            System.out.println("[b|u|d] args");
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
        };

        /** */
        public void addNewMessage(String message) {
            System.out.println(message);
        }

        /** */
        public void addNewUserID(String id) {
        }

        /** current peer */
        private P2PConnection p2pConnection;

        /** current peer */
        private OutputStream unicastOutputStream;

        /** */
        public void showBoard(P2PConnection connection) {
            try {
                this.p2pConnection = connection;
                this.unicastOutputStream = p2pConnection.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     */
    class SwingPresentation extends JFrame implements Presentation {
        /** */
        public static final String CONTENTS = "audio";

        /** */
        private JTextField messageText = new JTextField(10);

        /** */
        private JButton broadcastButton = new JButton("broadcast");
        /** */
        private JButton unicastButton = new JButton("unicast");
        /** */
        private JButton directConnectionButton = new JButton("Direct Connection");

        /** */
        private JList userList = new JList();

        /** */
        private JTextArea messageHistory = new JTextArea(5, 100);

        /** */
        public SwingPresentation() {
            this.setBounds(300, 300, 300, 300);
            this.getContentPane().setLayout(new BorderLayout());
            this.setTitle("Control Panel");

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            this.getContentPane().add(panel, BorderLayout.NORTH);
            panel.add(messageText, BorderLayout.WEST);
            panel.add(broadcastButton, BorderLayout.CENTER);
            panel.add(unicastButton, BorderLayout.EAST);

            JPanel panel2 = new JPanel();
            panel2.add(userList, BorderLayout.CENTER);
            panel2.add(directConnectionButton, BorderLayout.EAST);

            this.getContentPane().add(panel2, BorderLayout.CENTER);

            JScrollPane someScrollPane = new JScrollPane(messageHistory);
            someScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.getContentPane().add(someScrollPane, BorderLayout.SOUTH);

            this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    System.exit(0);
                }
            });

            broadcastButton.addActionListener(broadcastAction);
            unicastButton.addActionListener(unicastAction);
            directConnectionButton.addActionListener(directConnectionAction);

            setVisible(true);
        }

        /** */
        ActionListener broadcastAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    addNewMessage("sending a Broadcast Message");
                    bloadcastOutputStream.write(messageText.getText().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        /** */
        ActionListener unicastAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if (userList.getSelectedValue() != null) {
                        addNewMessage("sending a unicast Message to " + ((String) userList.getSelectedValue()).toString());
                        unicastOutputStream.write(messageText.getText().getBytes());
                    } else {
                        addNewMessage("select a target");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        /** */
        ActionListener directConnectionAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if (userList.getSelectedValue() != null) {
                        addNewMessage("requesting a direct connection to " + (String) userList.getSelectedValue());
Debug.println("id: " + userList.getSelectedValue() + ", connection: " + connection.hashCode());
                        connection.connect((String) userList.getSelectedValue());
                    } else {
                        addNewMessage("select a target");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        /** */
        public void addNewMessage(String message) {
            messageHistory.setText(message + "\r\n" + messageHistory.getText());
        }

        /** */
        public void addNewUserID(String id) {
            ListModel someModel = userList.getModel();
            Vector<Object> vector = new Vector<Object>();
            for (int i = 0; i < someModel.getSize(); i++) {
                if (someModel.getElementAt(i).equals(id)) {
                    return;
                }
                vector.add(someModel.getElementAt(i));
            }
            vector.add(id);
            userList.setListData(vector);
        }

        /** current peer */
        private P2PConnection p2pConnection;

        /** current peer */
        private OutputStream unicastOutputStream;

        /** */
        public void showBoard(P2PConnection connection) {
            try {
                addNewMessage("sending a Broadcast Message");
                bloadcastOutputStream.write(messageText.getText().getBytes());
                new NetworkWhiteBoard(connection).setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/* */
