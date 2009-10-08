
package ch.laoe.audio.load;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import ch.laoe.audio.AudioException;
import ch.laoe.ui.Debug;


/**
 * Class: ALoadFactory @author olivier gäumann, neuchâtel (switzerland) @target JDK 1.3
 * 
 * factory to build loaders.
 * 
 * @version 03.07.01 first draft oli4 18.07.02 encoded file support oli4
 */
public class ALoadFactory {
    private static List<ALoad> classList;

    static {
        preloadClasses();
    }

    private static void preloadClasses() {
        class ClassFileFilter implements FileFilter {
            public boolean accept(File file) {
                return file.getName().endsWith(".class");
            }
        }

        classList = new ArrayList<ALoad>();

        classList.add(new ALoadPcmUnsigned8Bit());
        classList.add(new ALoadPcmSigned8Bit());
        classList.add(new ALoadPcmUnsigned16BitLittleEndian());
        classList.add(new ALoadPcmUnsigned16BitBigEndian());
        classList.add(new ALoadPcmSigned16BitLittleEndian());
        classList.add(new ALoadPcmSigned16BitBigEndian());
        classList.add(new ALoadUlaw8Bit());
    }

    public static final ALoad create(File f) throws AudioException {
        try {
            // stream
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);

            // make difference between encoded and non-encoded streams, because mp3
            // doesn't give streamlength information :-(
            boolean isEncoded = !ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED) && !ais.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && !ais.getFormat().getEncoding().equals(AudioFormat.Encoding.ALAW) && !ais.getFormat().getEncoding().equals(AudioFormat.Encoding.ULAW);

            AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
            if (isEncoded) {
                ais = AudioSystem.getAudioInputStream(targetEncoding, ais); // this is required for mp3
            }
            AudioFormat af = ais.getFormat();

            // count size...
            int sl = 0;

            if (isEncoded)
            // if (false)
            {
                int st = 0;
                byte a[] = new byte[4096];

                try {
                    while ((st = ais.read(a, 0, a.length - 1)) >= 0) {
                        sl += st;
                    }
                    sl /= ais.getFormat().getChannels() * ais.getFormat().getSampleSizeInBits() / 8;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                sl = (int) (ais.getFrameLength() * af.getFrameSize() / af.getChannels() / (af.getSampleSizeInBits() >> 3));
            }

            // stream for data loading
            ais = AudioSystem.getAudioInputStream(f);
            targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
            if (isEncoded) {
                ais = AudioSystem.getAudioInputStream(targetEncoding, ais); // this is required for mp3
            }

            af = ais.getFormat();
            Debug.println(3, "audioformat = " + af.toString());

            // search the correct loader...
            for (int i = 0; i < classList.size(); i++) {
                ALoad l = classList.get(i);
                if (l.supports(af)) {
                    l = l.duplicate();
                    l.setAudioInputStream(ais, sl);
                    l.setFile(f);
                    return l;
                }
            }

            Debug.println(3, "unsupported audioformat = " + af.toString());
            throw new AudioException("unsupportedAudioFormat");
        } catch (UnsupportedAudioFileException uafe) {
            Debug.printStackTrace(5, uafe);
            throw new AudioException("unsupportedAudioFormat");
        } catch (IOException ioe) {
            Debug.printStackTrace(5, ioe);
            throw new AudioException("unsupportedAudioFormat");
        }
    }

}
