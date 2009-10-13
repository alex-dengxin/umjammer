/*
 * jicyshout relased under terms of the lesser GNU public license 
 * http://www.gnu.org/licenses/licenses.html#TOCLGPL
 */

package vavi.apps.karaoke;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;


/**
 * BdjDataSource. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080905 nsano initial version <br>
 */
public class BdjDataSource extends PullDataSource {

    protected MediaLocator ml;

    protected URLConnection urlConnection;

    protected SeekableInputStream seekStream;

    protected PullSourceStream[] sourceStreams;

    protected Object[] EMPTY_CONTROL_ARRAY = {};

    class Decoder extends FilterInputStream {
        int numEntries;
        boolean[] channels;
        long[] sizes;

        int current;

        Decoder(InputStream in) throws IOException {
            super(in);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(in));
            // check sound.bdmv header for format
            checkFormat(dis);

            // Refer to section 5.6.3 sound.bdmv - Syntax
            // SoundData_start_address (4 bytes)
            long soundDataStartAddr = readUnsignedInt(dis);

            // skip ExtensionData_start_address - 4 bytes
            dis.skip(4);

            // skip reserved 24 bytes
            dis.skip(24);

            final int sizeTillSoundIndex = 
                4 + /* type indicator */
                4 + /* version */
                4 + /* SoundData_start_address */
                4 + /* ExtensionData_start_address */
                24;   /* reserved_for_future_use */

            // refer to section 5.6.4.1 SoundIndex() - Syntax
            long soundIndexLength = readUnsignedInt(dis);
            if (soundIndexLength != 0) {
                // skip 1 reserved byte
                dis.skip(1);

                this.numEntries = dis.readUnsignedByte();
System.err.println("numEntries: " + numEntries);
                this.channels = new boolean[numEntries];
                this.sizes = new long[numEntries];

                // for each entry read sound attributes
                for (int soundId = 0; soundId < numEntries; soundId++) {

                    // channel configuration (4 bits): 1=mono, 3=stereo
                    // sampling rate (4 bits): must be 1=48 kHz
                    int data = dis.read();
                    channels[soundId] = ((data & 0x0F0) >> 4) == 1;
System.err.println("[" + soundId + "] channels: " + channels[soundId]);

                    // skip bits-per-sample and assume the default
                    dis.skip(1);

                    // skip sound_data_start_address (4 bytes)
                    dis.skip(4);

                    // sound_data_length (4 bytes)
                    sizes[soundId] = readUnsignedInt(dis);
System.err.println("[" + soundId + "] sizes: " + sizes[soundId]);
                }

                final int perEntrySize =
                        1 + /* channel_configuration, sampling freq */
                        1 + /* bits per sample + align */
                        4 + /* sound_data_start_address */
                        4;  /* sound_data_length */

                final int sizeofSoundIndex = 
                        4 +  /* length */
                        1 +  /* reserved */
                        1 +  /* number of entries */
                        /* variable size based on number of entries */
                        (numEntries*perEntrySize); 

                /* 
                 * We have to skip N1 padding_words (16 bits each) here.
                 * We compute pad by subtracing size of data read so far 
                 * from the value of SoundDataStartAddr.
                 */
                long N1bytes = soundDataStartAddr - (sizeTillSoundIndex + sizeofSoundIndex);
                if (N1bytes != 0) {
                    dis.skip(N1bytes);
                }
            }
        }

        public int available() throws IOException {
            return (int) (sizes[0] - current);
        }

        public int read() throws IOException {
            if (current < sizes[0]) {
                return in.read();
            } else {
                return -1;
            }
        }

        // file magic for .bdmv files
        private final byte[] SOUND_BDMV_TYPE_INDICATOR = "BCLK".getBytes();
        // sound.bdmv version string     
        private final byte[] SOUND_BDMV_VERSION = "0200".getBytes();

        private long readUnsignedInt(DataInputStream dis) throws IOException {
            return 0x0FFFFFFFFL & dis.readInt();
        }

        private void checkFormat(DataInputStream dis) throws IOException {

            // Refer to section 5.6.3 sound.bdmv - Syntax table

            // check type_indicator
            for (int i = 0; i < SOUND_BDMV_TYPE_INDICATOR.length; i++) {
                if (dis.read() != SOUND_BDMV_TYPE_INDICATOR[i]) {
                    throw new RuntimeException("Type indicator 'BCLK' expected");
                }
            }

            // check version string
            for (int i = 0; i < SOUND_BDMV_VERSION.length; i++) {
                if (dis.read() != SOUND_BDMV_VERSION[i]) {
                    throw new RuntimeException("Wrong version of sound.bdmv - '0200' expected");
                }
            }
        }
    }

    /** */
    public BdjDataSource(MediaLocator ml) {
        this.ml = ml;
    }

    /* */
    public void connect() throws IOException {
        urlConnection = ml.getURL().openConnection();
        InputStream is = new Decoder(urlConnection.getInputStream());

        seekStream = new SeekableInputStream(is);
        sourceStreams = new PullSourceStream[1];
        sourceStreams[0] = seekStream;
    }

    /* */
    public void disconnect() {
        try {
            seekStream.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /* */
    public String getContentType() {
        return "file";
    }

    /* */
    public void start() {
    }

    /* */
    public void stop() {
    }

    /* */
    public PullSourceStream[] getStreams() {
        return sourceStreams;
    }

    /* */
    public Time getDuration() {
        return DataSource.DURATION_UNBOUNDED;
    }

    /* */
    public Object getControl(String controlName) {
        return null;
    }

    /* */
    public Object[] getControls() {
        return EMPTY_CONTROL_ARRAY;
    }
}

/* */
