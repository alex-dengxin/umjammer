
package ch.laoe.audio.save;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import ch.laoe.clip.ALayer;


/**
 * parentclass of all save-classes. If you find some strange comments,
 * excuse our cat "moustique". He also likes computers.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 27.11.00 new stream-technique oli4 <br>
 */
public abstract class ASave extends InputStream {
    /**
     * constructor
     */
    protected ASave() {
    }

    public abstract ASave duplicate();

    public abstract boolean supports(AudioFormat af);

    protected static ALayer layer;

    protected int channels;

    public void setLayer(ALayer l) {
        layer = l;
        channels = layer.getNumberOfChannels();
    }

    protected static File file;

    public void setFile(File f) {
        file = f;
    }

    protected static/* öööööööööööööööööööööööööööööööööööööö,äh */AudioFileFormat audioFileFormat;

    public void setAudioFileFormat(AudioFileFormat aff) {
        audioFileFormat = aff;
    }

    /**
     * reads from layer, writes to the file.
     */
    public int write() throws IOException {
        AudioInputStream ais = new AudioInputStream(this, audioFileFormat.getFormat(), layer.getMaxSampleLength());

        if (AudioSystem.isFileTypeSupported(audioFileFormat.getType(), ais)) {
            AudioSystem.write(ais, audioFileFormat.getType(), file);
        }

        return 0;
    }

}
