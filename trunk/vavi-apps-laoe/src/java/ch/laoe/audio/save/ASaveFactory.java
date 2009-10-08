
package ch.laoe.audio.save;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import ch.laoe.audio.AudioException;
import ch.laoe.clip.AClip;
import ch.laoe.clip.ALayer;


/**
 * Class: ASaveFactory @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * factory to build savers.
 * 
 * @version 05.07.01 first draft oli4
 */
public class ASaveFactory {
    private static List<ASave> classList;

    static {
        preloadClasses();
    }

    private static void preloadClasses() {
        class ClassFileFilter implements FileFilter {
            public boolean accept(File file) {
                return file.getName().endsWith(".class");
            }
        }

        classList = new ArrayList<ASave>();

        classList.add(new ASavePcmUnsigned8Bit());
        classList.add(new ASavePcmSigned8Bit());
        classList.add(new ASavePcmUnsigned16BitLittleEndian());
        classList.add(new ASavePcmUnsigned16BitBigEndian());
        classList.add(new ASavePcmSigned16BitLittleEndian());
        classList.add(new ASavePcmSigned16BitBigEndian());
        classList.add(new ASaveUlaw8Bit());
    }

    public static final ASave create(AClip clip, ALayer l, File f) throws AudioException {
        AudioFormat af = new AudioFormat(clip.getAudio().getEncoding(), clip.getSampleRate(), clip.getSampleWidth(), l.getNumberOfChannels(), clip.getSampleWidth() / 8 * l.getNumberOfChannels(), clip.getSampleRate(), clip.isBigEndian());

        AudioFileFormat aff = new AudioFileFormat(clip.getAudio().getFileType(), af, l.getMaxSampleLength());

        ASave s = create(af);
        if (s == null) {
            throw new AudioException("unsupportedAudioFormat");
        }
        s.setAudioFileFormat(aff);
        s.setFile(f);
        s.setLayer(l);
        return s;
    }

    private static final ASave create(AudioFormat af) {
        // search the correct loader...
        for (int i = 0; i < classList.size(); i++) {
            ASave l = classList.get(i);
            if (l.supports(af)) {
                return l.duplicate();
            }
        }
        return null;
    }

}
