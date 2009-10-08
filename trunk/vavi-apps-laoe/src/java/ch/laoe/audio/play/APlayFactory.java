
package ch.laoe.audio.play;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import ch.laoe.audio.AudioException;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;


/**
 * Class: APlayFactory @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * factory to build players.
 * 
 * @version 05.07.01 first draft oli4
 */
public class APlayFactory {
    private static List<APlay> classList;

    static {
        preloadClasses();
    }

    private static void preloadClasses() {
        class ClassFileFilter implements FileFilter {
            public boolean accept(File file) {
                return file.getName().endsWith(".class");
            }
        }

        classList = new ArrayList<APlay>();

        // load all players...
        classList.add(new APlayPcmUnsigned8Bit());
        classList.add(new APlayPcmSigned8Bit());
        classList.add(new APlayPcmUnsigned16BitLittleEndian());
        classList.add(new APlayPcmUnsigned16BitBigEndian());
        classList.add(new APlayPcmSigned16BitLittleEndian());
        classList.add(new APlayPcmSigned16BitBigEndian());
    }

    public static final APlay create(AClip c, ALayer l) throws AudioException {
        AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, c.getSampleRate(), c.getSampleWidth(), l.getNumberOfChannels(), c.getSampleWidth() / 8 * l.getNumberOfChannels(), c.getSampleRate(), c.isBigEndian());
        APlay p = create(af);
        if (p == null) {
            throw new AudioException("unsupportedAudioFormat");
        }
        p.setAudioFormat(af);
        p.setLayer(l);
        return p;
    }

    private static final APlay create(AudioFormat af) {
        // search the correct player...
        for (int i = 0; i < classList.size(); i++) {
            APlay l = classList.get(i);
            if (l.supports(af)) {
                return l.duplicate();
            }
        }
        return null;
    }

}
