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

package ch.laoe.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/*********************************************************************************************************************************
 * 
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with LAoE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * Class: GDialog @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * generates some common used standard dialogs.
 * 
 * @version 09.07.01 first draft oli4
 * 
 */
public class GDialog {
    /**
     * shows a modal ERROR dialog
     */
    public static void showErrorDialog(Component comp, String title, String details) {
        JOptionPane.showMessageDialog(comp, GLanguage.translate(details), GLanguage.translate(title), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * shows a modal WARNING dialog
     */
    public static void showWarningDialog(Component comp, String title, String details) {
        JOptionPane.showMessageDialog(comp, GLanguage.translate(details), GLanguage.translate(title), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * shows a modal QUESTION dialog, with yes/no options
     * 
     * @return return true if yes was choosen
     */
    public static boolean showYesNoQuestionDialog(Component comp, String title, Object details) {
        return (JOptionPane.showConfirmDialog(comp, details, GLanguage.translate(title), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION);
    }

    public static boolean showCustomOkCancelDialog(JFrame owner, Component custom, String title) {
        // general
        final JDialog dialog = new JDialog(owner, true);
        dialog.setTitle(title);
        Container container = dialog.getContentPane();
        container.setLayout(new BorderLayout());
        JPanel p = new JPanel();
        container.add(p, BorderLayout.SOUTH);

        // ok / cancel
        final JButton ok = new JButton(GLanguage.translate("ok"));
        p.add(ok);

        final JButton cancel = new JButton(GLanguage.translate("cancel"));
        p.add(cancel);

        container.add(custom, BorderLayout.CENTER);
        dialog.pack();

        // event
        class EventDispatcher implements ActionListener {
            private boolean isOk = false;

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == ok) {
                    isOk = true;
                } else if (e.getSource() == cancel) {
                    isOk = false;
                }
                dialog.setVisible(false);
            }

            public boolean isOk() {
                return isOk;
            }
        }

        EventDispatcher ed = new EventDispatcher();
        ok.addActionListener(ed);
        cancel.addActionListener(ed);

        // location
        if (owner != null) {
            int x = owner.getLocation().x + (owner.getWidth() - dialog.getWidth()) / 2;
            int y = owner.getLocation().y + (owner.getHeight() - dialog.getHeight()) / 2;
            dialog.setLocation(new Point(x, y));
        } else {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (d.width - dialog.getWidth()) / 2;
            int y = (d.height - dialog.getHeight()) / 2;
            dialog.setLocation(new Point(x, y));
        }

        dialog.setVisible(true);
        return ed.isOk();
    }

}
