/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.karaoke;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * OGG decoder using JOrbis
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 081014 nsano initial version <br>
 * @see "http://www.jcraft.com/jorbis/"
 */
class OggInputStream extends DecodingInputStream {
    /** */
    private SyncState oy;
    /** */
    private StreamState os;
    /** */
    private Page og;
    /** */
    private Packet op;

    /** */
    private Info vi;
    /** */
    private Comment vc;
    /** */
    private DspState vd;
    /** */
    private Block vb;

    /** */
    private byte[] buffer = null;
    /** */
    private int bytes = 0;

    /** */
    private final int BUFSIZE = 4096 * 2;
    /** */
    private int convsize = BUFSIZE * 2;
    /** */
    private byte[] convbuffer = new byte[convsize];

    /** */
    OggInputStream(InputStream in) throws IOException {
        super(in);
        oy = new SyncState();
        os = new StreamState();
        og = new Page();
        op = new Packet();

        vi = new Info();
        vc = new Comment();
        vd = new DspState();
        vb = new Block(vd);

        buffer = null;
        bytes = 0;

        oy.init();
    }

    /* */
    public int decode(byte[] b) throws IOException {
        boolean chained = false;

loop:   while (true) {
            int eos = 0;

            int index = oy.buffer(BUFSIZE);
            buffer = oy.data;
            bytes = in.read(buffer, index, BUFSIZE);
            oy.wrote(bytes);

            if (chained) {
                chained = false;   
            } else {
                if (oy.pageout(og) != 1) {
                    if (bytes < BUFSIZE) {
                        break;
                    }
                    throw new IllegalStateException("Input does not appear to be an Ogg bitstream.");
                }
            }
            os.init(og.serialno());
            os.reset();

            vi.init();
            vc.init();

            if (os.pagein(og) < 0) {
                // error; stream version mismatch perhaps
                throw new IllegalStateException("Error reading first page of Ogg bitstream data.");
            }

            if (os.packetout(op) != 1) {
                // no page? must not be vorbis
System.err.println("Error reading initial header packet.");
                break;
            }

            if (vi.synthesis_headerin(vc, op) < 0) {
                // error case; not a vorbis header
                throw new IllegalStateException("This Ogg bitstream does not contain Vorbis audio data.");
            }

            int i = 0;

            while (i < 2) {
                while (i < 2) {
                    int result = oy.pageout(og);
                    if (result == 0)
                        break; // Need more data
                            if (result == 1) {
                                os.pagein(og);
                                while (i < 2) {
                                    result = os.packetout(op);
                                    if (result == 0) {
                                        break;
                                    }
                                    if (result == -1) {
System.err.println("Corrupt secondary header.  Exiting.");
                                break loop;
                            }
                            vi.synthesis_headerin(vc, op);
                            i++;
                        }
                    }
                }

                index = oy.buffer(BUFSIZE);
                buffer = oy.data;
                bytes = in.read(buffer, index, BUFSIZE);
                if (bytes == 0 && i < 2) {
                    throw new IllegalStateException("End of file before finding all Vorbis headers!");
                }
                oy.wrote(bytes);
            }

            convsize = BUFSIZE / vi.channels;

            vd.synthesis_init(vi);
            vb.init(vd);

            float[][][] _pcmf = new float[1][][];
            int[] _index = new int[vi.channels];

            while (eos == 0) {
                while (eos == 0) {

                    int result = oy.pageout(og);
                    if (result == 0) {
                        break; // need more data
                    }
                    if (result == -1) { // missing or corrupt data at this page position
//System.err.println("Corrupt or missing data in bitstream; continuing...");
                    } else {
                        os.pagein(og);

                        if (og.granulepos() == 0) {
                            chained = true;
                            eos = 1;
                            break;
                        }

                        while (true) {
                            result = os.packetout(op);
                            if (result == 0) {
                                break; // need more data
                            }
                            if (result == -1) { // missing or corrupt data at this page position
                                // no reason to complain; already complained above
// System.err.println("no reason to complain; already complained above");
                            } else {
                                // we have a packet. Decode it
                                int samples;
                                if (vb.synthesis(op) == 0) { // test for success!
                                    vd.synthesis_blockin(vb);
                                }
                                while ((samples = vd.synthesis_pcmout(_pcmf, _index)) > 0) {
                                    float[][] pcmf = _pcmf[0];
                                    int bout = (samples < convsize ? samples : convsize);

                                    // convert doubles to 16 bit signed ints
                                    // (host order) and interleave
                                    for (i = 0; i < vi.channels; i++) {
                                        int ptr = i * 2;
                                        int mono = _index[i];
                                        for (int j = 0; j < bout; j++) {
                                            int val = (int) (pcmf[i][mono + j] * 32767.);
                                            if (val > 32767) {
                                                val = 32767;
                                            }
                                            if (val < -32768) {
                                                val = -32768;
                                            }
                                            if (val < 0) {
                                                val = val | 0x8000;
                                            }
                                            convbuffer[ptr] = (byte) (val);
                                            convbuffer[ptr + 1] = (byte) (val >>> 8);
                                            ptr += 2 * (vi.channels);
                                        }
                                    }
//                                            outputLine.write(convbuffer, 0, 2 * vi.channels * bout);
                                    vd.synthesis_read(bout);
                                }
                            }
                        }
                        if (og.eos() != 0)
                            eos = 1;
                    }
                }

                if (eos == 0) {
                    index = oy.buffer(BUFSIZE);
                    buffer = oy.data;
                    bytes = in.read(buffer, index, BUFSIZE);
                    if (bytes == -1) {
                        break;
                    }
                    oy.wrote(bytes);
                    if (bytes == 0) {
                        eos = 1;
                    }
                }
            }

            os.clear();
            vb.clear();
            vd.clear();
            vi.clear();
        }

        return 0;
    }

    /* */
    public void close() throws IOException {
        oy.clear();
        super.close();
    }
}

/* */
