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

package ch.laoe.audio.capture;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import ch.laoe.audio.AudioException;
import ch.laoe.clip.AClip;
import ch.laoe.ui.Debug;


/**
 * factory to build capturers.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 05.07.01 first draft oli4
 */
public class ACaptureFactory {
    private static List<ACapture> classList;

    static {
        preloadClasses();
    }

    private static void preloadClasses() {
        class ClassFileFilter implements FileFilter {
            public boolean accept(File file) {
                return file.getName().endsWith(".class");
            }
        }

        classList = new ArrayList<ACapture>();

        classList.add(new ACapturePcmUnsigned8Bit());
        classList.add(new ACapturePcmSigned8Bit());
        classList.add(new ACapturePcmUnsigned16BitLittleEndian());
        classList.add(new ACapturePcmUnsigned16BitBigEndian());
        classList.add(new ACapturePcmSigned16BitLittleEndian());
        classList.add(new ACapturePcmSigned16BitBigEndian());
    }

    public static final ACapture create(AClip c) throws AudioException {
        // try to find out audio file format
        AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, c.getSampleRate(), c.getSampleWidth(), c.getLayer(0).getNumberOfChannels(), c.getSampleWidth() / 8 * c.getLayer(0).getNumberOfChannels(), c.getSampleRate() / (c.getSampleWidth() / 8 * c.getLayer(0).getNumberOfChannels()), false);
        ACapture capture = create(af);
        if (capture == null) {
            throw new AudioException("unsupportedAudioFormat");
        }

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, af);
        // error
        if (!AudioSystem.isLineSupported(info)) {
            throw new AudioException("missingAudioResource");
        }

        TargetDataLine line = null;
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open();
            line.start();
            capture.setLine(line);
        } catch (LineUnavailableException lue) {
            line.stop();
            line.close();
            Debug.printStackTrace(5, lue);
            throw new AudioException("missingAudioResource");
        }

        capture.setClip(c);
        return capture;
    }

    private static final ACapture create(AudioFormat af) {
        // search the correct player...
        for (int i = 0; i < classList.size(); i++) {
            ACapture l = classList.get(i);
            if (l.supports(af)) {
                return l.duplicate();
            }
        }
        return null;
    }

}
