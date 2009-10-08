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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ch.laoe.audio.Audio;
import ch.laoe.audio.AudioException;
import ch.laoe.audio.AudioListener;
import ch.laoe.clip.AChannelPlotter;
import ch.laoe.clip.AChannelSelection;
import ch.laoe.clip.AClip;
import ch.laoe.ui.Debug;
import ch.laoe.ui.GClipPanel;
import ch.laoe.ui.GLanguage;
import ch.oli4.ui.UiCartesianLayout;
import ch.oli4.ui.UiControlEvent;
import ch.oli4.ui.UiControlListener;
import ch.oli4.ui.UiControlText;


/**
 * plugin to play/loop/record the clip.
 * 
 * @autor: olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 31.08.00 erster Entwurf oli4 <br>
 *          28.03.01 limit loop-pointers on reload oli4 <br>
 *          01.06.01 loop-pointer settable oli4 <br>
 *          01.06.01 add button-signaling oli4 <br>
 *          26.12.01 play/loop pointers always painted oli4 <br>
 *          20.03.02 loop checkbox introduced oli4 <br>
 */
public class GPPlayLoopRec extends GPluginFrame {
    public GPPlayLoopRec(GPluginHandler ph) {
        super(ph);
        initCursors();
        initGui();
    }

    protected String getName() {
        return "playLoopRec";
    }

    public JMenuItem createMenuItem() {
        return super.createMenuItem(KeyEvent.VK_P);
    }

    public void reload() {
        try {
            playBlockSize.setData(Audio.getPlayBlockSize());
            captureBlockSize.setData(Audio.getCaptureBlockSize());

            Audio a = getFocussedClip().getAudio();
            loop.setSelected(a.isLooping());
            sampleRate.setData(getFocussedClip().getSampleRate());
            getFocussedClip().getAudio().limitLoopPointers();
            getFocussedClip().getAudio().setAudioListener(eventDispatcher);
            updateButtons();
        } catch (NullPointerException e) {
        }
    }

    public void start() {
        super.start();
        pluginHandler.setFocussedPlugin(this);
    }

    // audio pointers

    private int selectedPointer;

    private static final int NO_POINTER = 0;

    private static final int LOOP_START_POINTER = 1;

    private static final int LOOP_END_POINTER = 2;

    // cursors
    Cursor defaultCursor;

    Cursor placeCursor;

    private void initCursors() {
        defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        placeCursor = new Cursor(Cursor.HAND_CURSOR);
    }

    public void mouseMoved(MouseEvent e) {
        AClip c = ((GClipPanel) e.getSource()).getClip();
        int x = e.getPoint().x;
        final int D_MAX = 5;

        // measure distance to the loop pointers...
        int dStart = Math.abs(c.getAudio().getPlotter().getXLoopStartPointer() - x);
        int dEnd = Math.abs(c.getAudio().getPlotter().getXLoopEndPointer() - x);

        // select a pointer...
        selectedPointer = NO_POINTER;
        // close to the loop end pointer ?
        if (dEnd < D_MAX) {
            selectedPointer = LOOP_END_POINTER;
            ((Component) e.getSource()).setCursor(placeCursor);
        }
        // close to the loop start pointer ?
        else if (dStart < D_MAX) {
            selectedPointer = LOOP_START_POINTER;
            ((Component) e.getSource()).setCursor(placeCursor);
        } else {
            ((Component) e.getSource()).setCursor(defaultCursor);
        }

        // System.out.println("selected pointer = "+selectedPointer);
        // System.out.println("x = "+x);
    }

    public void mouseDragged(MouseEvent e) {
        // is a pointer selected ?
        if (selectedPointer != NO_POINTER) {
            // mouse x in sample-domain...
            AClip c = ((GClipPanel) e.getSource()).getClip();
            int x = (int) c.getSelectedLayer().getChannel(0).getChannelPlotter().graphToSampleX(e.getPoint().x);

            switch (selectedPointer) {
            case LOOP_START_POINTER:
                c.getAudio().setLoopStartPointer(x);
                break;

            case LOOP_END_POINTER:
                c.getAudio().setLoopEndPointer(x);
                break;
            }
            // System.out.println("moved pointer = "+selectedPointer);
            // System.out.println("x = "+x);
            // draw
            repaintFocussedClipEditor();
        }
    }

    // GUI

    private JButton stopButton;

    private JButton pauseButton;

    private JButton rewButton;

    private JButton playButton;

    private JButton forwButton;

    private JButton recButton;

    private JCheckBox loop;

    private ImageIcon pausePassive, pauseActive;

    private ImageIcon playPassive, playActive;

    private ImageIcon recPassive, recActive;

    private UiControlText sampleRate, playBlockSize, captureBlockSize;

    private JComboBox loopPointerSettings;

    private EventDispatcher eventDispatcher;

    private class EventDispatcher implements ActionListener, UiControlListener, AudioListener {
        // action events
        public void actionPerformed(ActionEvent e) {
            try {
                if (e.getSource() == stopButton) {
                    Debug.println(1, "plugin " + getName() + " [stop] clicked");
                    getFocussedClip().getAudio().stop();
                } else if (e.getSource() == pauseButton) {
                    Debug.println(1, "plugin " + getName() + " [pause] clicked");
                    getFocussedClip().getAudio().pause();
                } else if (e.getSource() == rewButton) {
                    Debug.println(1, "plugin " + getName() + " [rew] clicked");
                    getFocussedClip().getAudio().rewind();
                } else if (e.getSource() == playButton) {
                    try {
                        Debug.println(1, "plugin " + getName() + " [play] clicked");
                        getFocussedClip().getAudio().play();
                    } catch (AudioException ae) {
                        showErrorDialog("audioError", ae.getMessage());
                    }
                } else if (e.getSource() == forwButton) {
                    Debug.println(1, "plugin " + getName() + " [forwind] clicked");
                    getFocussedClip().getAudio().forwind();
                } else if (e.getSource() == recButton) {
                    try {
                        Debug.println(1, "plugin " + getName() + " [rec] clicked");
                        getFocussedClip().getAudio().rec();
                    } catch (AudioException ae) {
                        showErrorDialog("audioError", ae.getMessage());
                    }
                } else if (e.getSource() == loop) {
                    Debug.println(1, "plugin " + getName() + " [loop] clicked");
                    getFocussedClip().getAudio().setLooping(loop.isSelected());
                } else if (e.getSource() == loopPointerSettings) {
                    Debug.println(1, "plugin " + getName() + " [loop pointer settings] clicked");
                    AClip c = getFocussedClip();
                    Audio a = c.getAudio();
                    AChannelPlotter p = c.getSelectedLayer().getChannel(0).getChannelPlotter();
                    AChannelSelection s = c.getSelectedLayer().getChannel(0).getChannelSelection();

                    switch (loopPointerSettings.getSelectedIndex()) {
                    case 0: // whole clip
                        a.setLoopStartPointer(0);
                        a.setLoopEndPointer(c.getMaxSampleLength());
                        break;

                    case 1: // zoomed range
                        a.setLoopStartPointer((int) p.getXOffset());
                        a.setLoopEndPointer((int) (p.getXOffset() + p.getXLength()));
                        break;

                    case 2: // selection
                        a.setLoopStartPointer(s.getOffset());
                        a.setLoopEndPointer((s.getOffset() + s.getLength()));
                        break;

                    case 3: // measure
                        a.setLoopStartPointer((int) GPMeasure.getLowerCursor());
                        a.setLoopEndPointer((int) GPMeasure.getHigherCursor());
                        break;
                    }
                    repaintFocussedClipEditor();
                }
            } catch (NullPointerException npe) {
            }
            updateButtons();
        }

        public void onDataChanging(UiControlEvent e) {
        }

        public void onDataChanged(UiControlEvent e) {
            if (e.getSource() == sampleRate) {
                Debug.println(1, "plugin " + getName() + " [samplerate] changed");
                getFocussedClip().setSampleRate((float) sampleRate.getData());
            } else if (e.getSource() == playBlockSize) {
                Audio.setPlayBlockSize((int) playBlockSize.getData());
            } else if (e.getSource() == captureBlockSize) {
                Audio.setCaptureBlockSize((int) captureBlockSize.getData());
            }
        }

        public void onValidate(UiControlEvent e) {
        }

        private boolean recording = false;

        public void onStateChange(int state) {
            updateButtons();
            switch (state) {
            case Audio.PLAY:
                recording = false;
                break;

            case Audio.PAUSE:
                if (recording) {
                    reloadFocussedClipEditor();
                    updateHistory(GLanguage.translate(getName()));
                }
                recording = false;
                break;

            case Audio.REC:
                recording = true;
                break;

            default:
                if (recording) {
                    reloadFocussedClipEditor();
                    updateHistory(GLanguage.translate(getName()));
                }
                recording = false;
                break;
            }
        }
    }

    private void updateButtons() {
        switch (getFocussedClip().getAudio().getState()) {
        case Audio.PLAY:
            pauseButton.setIcon(pausePassive);
            playButton.setIcon(playActive);
            recButton.setIcon(recPassive);
            break;

        case Audio.PAUSE:
            pauseButton.setIcon(pauseActive);
            playButton.setIcon(playPassive);
            recButton.setIcon(recPassive);
            break;

        case Audio.REC:
            pauseButton.setIcon(pausePassive);
            playButton.setIcon(playPassive);
            recButton.setIcon(recActive);
            break;

        default:
            pauseButton.setIcon(pausePassive);
            playButton.setIcon(playPassive);
            recButton.setIcon(recPassive);
            break;
        }
    }

    private void initGui() {
        JTabbedPane tab = new JTabbedPane();

        // control-tab
        // icons
        pauseActive = loadIcon("resources/pauseActive.gif");
        pausePassive = loadIcon("resources/pausePassive.gif");
        playActive = loadIcon("resources/playActive.gif");
        playPassive = loadIcon("resources/playPassive.gif");
        recActive = loadIcon("resources/recActive.gif");
        recPassive = loadIcon("resources/recPassive.gif");

        // buttons
        stopButton = new JButton(loadIcon("resources/stop.gif"));
        stopButton.setToolTipText(GLanguage.translate("stop"));
        stopButton.setPreferredSize(new Dimension(26, 26));
        pauseButton = new JButton(pausePassive);
        pauseButton.setToolTipText(GLanguage.translate("pause"));
        pauseButton.setPreferredSize(new Dimension(26, 26));
        rewButton = new JButton(loadIcon("resources/rewind.gif"));
        rewButton.setToolTipText(GLanguage.translate("rewind"));
        rewButton.setPreferredSize(new Dimension(26, 26));
        playButton = new JButton(playPassive);
        playButton.setToolTipText(GLanguage.translate("play"));
        playButton.setPreferredSize(new Dimension(26, 26));
        forwButton = new JButton(loadIcon("resources/forwind.gif"));
        forwButton.setToolTipText(GLanguage.translate("forwind"));
        forwButton.setPreferredSize(new Dimension(26, 26));
        recButton = new JButton(recPassive);
        recButton.setToolTipText(GLanguage.translate("record"));
        recButton.setPreferredSize(new Dimension(26, 26));
        loop = new JCheckBox(GLanguage.translate("loop"));

        JPanel pControl = new JPanel();
        UiCartesianLayout lControl = new UiCartesianLayout(pControl, 10, 3);
        lControl.setPreferredCellSize(new Dimension(35, 35));
        pControl.setLayout(lControl);
        pControl.add(stopButton, new Rectangle(0, 0, 1, 1));
        pControl.add(pauseButton, new Rectangle(1, 0, 1, 1));
        pControl.add(rewButton, new Rectangle(3, 0, 1, 1));
        pControl.add(playButton, new Rectangle(4, 0, 1, 1));
        pControl.add(forwButton, new Rectangle(5, 0, 1, 1));
        pControl.add(recButton, new Rectangle(7, 0, 1, 1));
        pControl.add(loop, new Rectangle(8, 0, 2, 1));

        pControl.add(new JLabel(GLanguage.translate("sampleRate")), new Rectangle(0, 1, 5, 1));
        sampleRate = new UiControlText(15, true, false);
        sampleRate.setDataRange(100, 48000);
        pControl.add(sampleRate, new Rectangle(5, 1, 5, 1));

        pControl.add(new JLabel(GLanguage.translate("loopPoints")), new Rectangle(0, 2, 5, 1));
        String loopPointerItems[] = {
            GLanguage.translate("wholeClip"), GLanguage.translate("zoomedRange"), GLanguage.translate("selection"), GLanguage.translate("measurePoints")
        };
        loopPointerSettings = new JComboBox(loopPointerItems);
        pControl.add(loopPointerSettings, new Rectangle(5, 2, 5, 1));

        tab.add(GLanguage.translate("control"), pControl);

        // configure tab
        JPanel pConfig = new JPanel();
        UiCartesianLayout lConfig = new UiCartesianLayout(pConfig, 10, 3);
        lConfig.setPreferredCellSize(new Dimension(35, 35));
        pConfig.setLayout(lConfig);

        pConfig.add(new JLabel(GLanguage.translate("playBlockSize")), new Rectangle(0, 0, 5, 1));
        playBlockSize = new UiControlText(15, true, false);
        playBlockSize.setDataRange(100, 10000);
        pConfig.add(playBlockSize, new Rectangle(5, 0, 5, 1));

        pConfig.add(new JLabel(GLanguage.translate("captureBlockSize")), new Rectangle(0, 1, 5, 1));
        captureBlockSize = new UiControlText(15, true, false);
        captureBlockSize.setDataRange(100, 10000);
        pConfig.add(captureBlockSize, new Rectangle(5, 1, 5, 1));

        tab.add(GLanguage.translate("configure"), pConfig);

        frame.getContentPane().add(tab);
        pack();

        eventDispatcher = new EventDispatcher();
        stopButton.addActionListener(eventDispatcher);
        pauseButton.addActionListener(eventDispatcher);
        rewButton.addActionListener(eventDispatcher);
        playButton.addActionListener(eventDispatcher);
        forwButton.addActionListener(eventDispatcher);
        recButton.addActionListener(eventDispatcher);
        loop.addActionListener(eventDispatcher);
        sampleRate.addControlListener(eventDispatcher);
        playBlockSize.addControlListener(eventDispatcher);
        captureBlockSize.addControlListener(eventDispatcher);
        loopPointerSettings.addActionListener(eventDispatcher);
    }
}
