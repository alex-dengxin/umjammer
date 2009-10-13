/*
 * jicyshout relased under terms of the lesser GNU public license 
 * http://www.gnu.org/licenses/licenses.html#TOCLGPL
 */

package vavi.apps.karaoke;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceStream;


/**
 * Uses a BufferedInputStream to implement a Seekable.
 * <p>
 * The reason for this class is that the JMF mp3 parser calls seek() a bunch of
 * times when the DataSource is connected. Without a proper implementation, the
 * parser is unable to find the mpeg frame-synchs, typically blowing up while
 * trying to figure out the bitrate, sampling frequency, etc. of the stream.
 * With a plain-jane BufferedInputStream and a fake implementation of Seekable,
 * I got a lot of this:
 * 
 * <pre>
 *  java.lang.ArrayIndexOutOfBoundsException
 *  at com.ibm.media.parser.video.MpegParser.detectStreamType(MpegParser.java:580)
 *  at com.ibm.media.parser.video.MpegParser.getTracks(MpegParser.java:341)
 *  at com.sun.media.BasicSourceModule.doRealize(BasicSourceModule.java:204)
 *  at com.sun.media.PlaybackEngine.doConfigure1(PlaybackEngine.java:253)
 *  at com.sun.media.PlaybackEngine.doConfigure(PlaybackEngine.java:217)
 *  at com.sun.media.ConfigureWorkThread.process(BasicController.java:1447)
 *  at com.sun.media.StateTransitionWorkThread.run(BasicController.java:1416)
 * </pre>
 * 
 * <p>
 * Note however, that the Seekable implementation is only good for the first
 * buffer-ful and is only meant to get the mp3 decoder happy with his initial
 * parse of the stream. Though there's no attempt to force it programmatically,
 * it is not going to work to stream for 20 minutes and then try to do
 * <code>seek(0)</code>. Fortunately, once it is playing, the decoder does
 * not attempt to do this.
 * 
 * @author Chris Adamson, invalidname@mac.com
 */
public class SeekableInputStream extends BufferedInputStream implements PullSourceStream, Seekable {

    protected int tellPoint;

    /**
     * The mark we attempt to maintain at start-time, also the minimum size of
     * the buffer (128KB).
     * <p>
     * 128k is huge, but the mp3 parser seems to read 100000 bytes and then
     * reset to 0 just after we realize(), so we must be able to roll back that
     * far so as to not have an invalid mark
     */
    public final static int MAX_MARK = 131072; // 128k

    protected ContentDescriptor unknownCD;

    protected Object[] EMPTY_CONTROL_ARRAY = {};

    /**
     * Creates a SeekableInputStream size==MAX_MARK.
     */
    public SeekableInputStream(InputStream in) {
        super(in, MAX_MARK);
        tellPoint = 0;
        mark(MAX_MARK);
        unknownCD = new ContentDescriptor("unknown");
    }

    /**
     * Creates a SeekableInputStream with the given buffer size, or MAX_MARK,
     * whichever is greater.
     */
    public SeekableInputStream(InputStream in, int size) {
        super(in, Math.max(size, MAX_MARK));
        tellPoint = 0;
        mark(Math.max(size, MAX_MARK));
        unknownCD = new ContentDescriptor("unknown");
    }

    /**
     * Trivial - reads a byte and increments tellPoint.
     */
    public int read() throws IOException {
        int readByte = super.read();
        tellPoint++;
        return readByte;
    }

    /**
     * Trivial - reads bytes and increments tellPoint. Specified by
     * PullSourceStream.
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        int bytesRead = super.read(buf, off, len);
        tellPoint += bytesRead;
        return bytesRead;
    }

    /**
     * Trivial - reads bytes and increments tellPoint.
     */
    public int read(byte[] buf) throws IOException {
        int bytesRead = super.read(buf);
        tellPoint += bytesRead;
        return bytesRead;
    }

    /**
     * Returns true if in.available() <= 0 (ie, no bytes to read without
     * blocking or end-of-stream). Specified by PullSourceStream
     */
    public boolean willReadBlock() {
        try {
System.err.println("Will block?  available() == " + in.available());
            return (in.available() <= 0);
        } catch (IOException e) {
            // let's assume this indicates the stream is hosed
            return true;
        }
    }

    /**
     * resets and sets the tellPoint to 0 (bogus once you've read more than a
     * buffer-ful)
     */
    public void reset() throws IOException {
System.err.println("reset(), tellPoint is " + tell());
        super.reset();
        tellPoint = 0; // kind of bogus
    }

    /**
     * Trivial - skips bytes as usual and updates the tell point.
     */
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        tellPoint += skipped;
        return skipped;
    }

    /**
     * trivial
     */
    public void mark(int readLimit) {
//System.err.println("SeekableInputStream.mark(" + readLimit + "), pos = " + pos + ", old markPos = " + markpos);
        super.mark(readLimit);
    }

    /**
     * Returns the "unknown" ContentDecriptor. Specified by SourceStream.
     */
    public ContentDescriptor getContentDescriptor() {
        return unknownCD;
    }

    /**
     * Returns SourceStream.LENGTH_UNKNOWN, because we don't know when or if the
     * stream will ever end. Specified by SourceStream.
     */
    public long getContentLength() {
        return SourceStream.LENGTH_UNKNOWN;
    }

    /**
     * Returns false because streams never end (at least not until the lawyers
     * get involved). Specified by SourceStream.
     */
    public boolean endOfStream() {
        return false;
    }

    /**
     * Returns null -- no Controls for the stream are available. Specified by
     * Controls interface.
     */
    public Object getControl(String controlName) {
        return null;
    }

    /**
     * Returns an empty array -- no Controls for the stream are available.
     * Specified by Controls interface.
     */
    public Object[] getControls() {
        return EMPTY_CONTROL_ARRAY;
    }

    // Seekable stuff -- com.sun.media.content.unknown.Handler
    // requires us to be Seekable, though this is pretty dodgy

    /**
     * Returns false, in a vain attempt to get the IBM parser to realize this is
     * not a random-access file.
     */
    public boolean isRandomAccess() {
System.err.println("Called Seekable.isRandomAccess()");
        return false;
    }

    /**
     * Resets back to the mark (presumably the beginning of the stream) and then
     * skip()s ahead the specified number of bytes. This only works so long as
     * the mark on the beginning of the stream is valid -- in practice, the JMF
     * mp3 parser seeks around the beginning of the stream about a half-dozen
     * times while it's getting set up.
     * <p>
     * This is only meant to work during that initial startup period (within the
     * first MAX_MARK bytes). If you stream for 20 minutes and then try to
     * seek(0), you're going to get an invalid-mark exception.
     */
    public long seek(long where) {
System.err.println("Called Seekable.seek(" + where + ")");
        try {
            reset();
            mark(MAX_MARK);
            skip(where);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return tell();
    }

    /**
     * Tells how many bytes into the stream we are, adjusts for all seeks,
     * resets, skips, etc.
     */
    public long tell() {
//System.err.println("Called Seekable.tell()");
        return tellPoint;
    }
}

/* */
