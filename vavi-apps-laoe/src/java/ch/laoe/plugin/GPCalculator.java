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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.laoe.operation.AOToolkit;
import ch.laoe.ui.Debug;
import ch.oli4.ui.UiCartesianLayout;


/**
 * plugin to calculate sound-based stuff...
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 31.08.00 erster Entwurf oli4 <br>
 *          17.05.01 new layout, smaller characters oli4
 */
public class GPCalculator extends GPluginFrame {
    public GPCalculator(GPluginHandler ph) {
        super(ph);
        initGui();
    }

    protected String getName() {
        return "calculator";
    }

    // GUI

    private JTextField display;

    private JButton keyboard[][];

    private static final int KEY_COL = 5;

    private static final int KEY_ROW = 6;

    private static final String[][] keyName = {
        {
            "+oct", "+htone", "->s", "->dB", "CE"
        }, {
            "-oct", "-htone", "s->", "dB->", "/"
        }, {
            "sqrt", "7", "8", "9", "*"
        }, {
            "x^2", "4", "5", "6", "-"
        }, {
            "1/x", "1", "2", "3", "+"
        }, {
            "", "0", ".", "+/-", "="
        },
    };

    private static final int[][] keyColor = {
        {
            0, 0, 0, 0, 2
        }, {
            0, 0, 0, 0, 0
        }, {
            0, 1, 1, 1, 0
        }, {
            0, 1, 1, 1, 0
        }, {
            0, 1, 1, 1, 0
        }, {
            0, 1, 0, 0, 0
        }
    };

    private EventDispatcher eventDispatcher;

    private class EventDispatcher implements ActionListener {
        // action events
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < KEY_ROW; i++) {
                for (int j = 0; j < KEY_COL; j++) {
                    if (e.getSource() == keyboard[i][j]) {
                        Debug.println(1, "plugin " + getName() + " [" + e.getActionCommand() + "] clicked");
                        calculate(e.getActionCommand());
                        return;
                    }
                }
            }

            if (e.getSource() == display) {
                Debug.println(1, "plugin " + getName() + " display [enter] clicked");
                calculate("enter");
            }
        }
    }

    private void initGui() {
        JPanel p = new JPanel();
        UiCartesianLayout cl = new UiCartesianLayout(p, 5, 7);
        cl.setPreferredCellSize(new Dimension(60, 25));
        cl.setCellGap(.03f);
        p.setLayout(cl);

        Color color1 = new Color(0xA0A060);
        Color color2 = new Color(0xA06060);

        display = new JTextField(30);
        display.addActionListener(eventDispatcher);
        cl.add(display, 0, 0, 5, 1);

        eventDispatcher = new EventDispatcher();

        keyboard = new JButton[KEY_ROW][KEY_COL];
        for (int i = 0; i < KEY_ROW; i++) {
            keyboard[i] = new JButton[KEY_COL];
            for (int j = 0; j < KEY_COL; j++) {
                keyboard[i][j] = new JButton(keyName[i][j]);
                cl.add(keyboard[i][j], j, i + 1, 1, 1);
                keyboard[i][j].addActionListener(eventDispatcher);
                keyboard[i][j].setMargin(new Insets(1, 1, 1, 1));
                // keyboard[i][j].setFont(new Font("", Font.TRUETYPE_FONT, 12));
                switch (keyColor[i][j]) {
                case 1:
                    keyboard[i][j].setBackground(color1);
                    break;

                case 2:
                    keyboard[i][j].setBackground(color2);
                    break;

                }
            }
        }

        frame.getContentPane().add(p);
        pack();
    }

    private double input1;

    private double input2;

    private String binaryOperation;

    private void calculate(String s) {
        try {
            if (s.compareTo("0") == 0) {
                display.setText(new String(display.getText() + "0"));
            } else if (s.compareTo("1") == 0) {
                display.setText(new String(display.getText() + "1"));
            } else if (s.compareTo("2") == 0) {
                display.setText(new String(display.getText() + "2"));
            } else if (s.compareTo("3") == 0) {
                display.setText(new String(display.getText() + "3"));
            } else if (s.compareTo("4") == 0) {
                display.setText(new String(display.getText() + "4"));
            } else if (s.compareTo("5") == 0) {
                display.setText(new String(display.getText() + "5"));
            } else if (s.compareTo("6") == 0) {
                display.setText(new String(display.getText() + "6"));
            } else if (s.compareTo("7") == 0) {
                display.setText(new String(display.getText() + "7"));
            } else if (s.compareTo("8") == 0) {
                display.setText(new String(display.getText() + "8"));
            } else if (s.compareTo("9") == 0) {
                display.setText(new String(display.getText() + "9"));
            } else if (s.compareTo(".") == 0) {
                display.setText(new String(display.getText() + "."));
            } else if (s.compareTo("CE") == 0) {
                display.setText("");
            }

            // unary operation
            else if (s.compareTo("enter") == 0) {
                input1 = Double.parseDouble(display.getText());
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("1/x") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = 1 / input1;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("+/-") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = -input1;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("+oct") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 *= 2;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("-oct") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 *= .5;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("+htone") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 *= 1.0594630943592952645618252949463;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("-htone") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 /= 1.0594630943592952645618252949463;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("->s") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = input1 / pluginHandler.getFocussedClip().getSampleRate();
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("s->") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = input1 * pluginHandler.getFocussedClip().getSampleRate();
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("sqrt") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = Math.sqrt(input1);
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("x^2") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = input1 * input1;
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("->dB") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = AOToolkit.todB((float) input1);
                display.setText(String.valueOf(input1));
            } else if (s.compareTo("dB->") == 0) {
                input1 = Double.parseDouble(display.getText());
                input1 = AOToolkit.fromdB((float) input1);
                display.setText(String.valueOf(input1));
            }

            // binary operation
            else if (s.compareTo("*") == 0) {
                input1 = Double.parseDouble(display.getText());
                binaryOperation = "*";
                display.setText("");
            } else if (s.compareTo("/") == 0) {
                input1 = Double.parseDouble(display.getText());
                binaryOperation = "/";
                display.setText("");
            } else if (s.compareTo("+") == 0) {
                input1 = Double.parseDouble(display.getText());
                binaryOperation = "+";
                display.setText("");
            } else if (s.compareTo("-") == 0) {
                input1 = Double.parseDouble(display.getText());
                binaryOperation = "-";
                display.setText("");
            } else if (s.compareTo("=") == 0) {
                input2 = Double.parseDouble(display.getText());
                if (binaryOperation.compareTo("*") == 0) {
                    display.setText(String.valueOf(input1 * input2));
                } else if (binaryOperation.compareTo("/") == 0) {
                    display.setText(String.valueOf(input1 / input2));
                } else if (binaryOperation.compareTo("+") == 0) {
                    display.setText(String.valueOf(input1 + input2));
                } else if (binaryOperation.compareTo("-") == 0) {
                    display.setText(String.valueOf(input1 - input2));
                }
            }

        } catch (NumberFormatException nfe) {
            display.setText("ERROR");
        }
    }

}
