import java.io.FileInputStream;
import java.io.IOException;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFTags;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.writers.SWFWriter;


/**
 * Shows how to insert a number of frames at the end of a movie.
 *
 * A "pipeline" is set up in the main method:
 *
 *    SWFReader-->AddFrames-->SWFWriter
 *
 * SWFReader reads the input SWF file and separates out the header
 * and the tags.  The separated contents are passed to AddFrames which
 * passes them on to SWFWriter, which writes to the output file.
 *
 * AddFrames intercepts the End tag (which is the last tag in a SWF
 * file) and sends a number of ShowFrame tags before the End tag.
 */
public class AddFrames implements SWFTags {
    protected SWFTags _swftags;
    protected int _count;

    public AddFrames(SWFTags swftags, int count) {
        _swftags = swftags;
        _count = count;
    }

    /**
     * SWFTags interface
     */
    public void header(int version, long length, int twipsWidth, int twipsHeight, int frameRate, int frameCount) throws IOException {
        // Pass the header through
        _swftags.header(version, -1, //length: -1 to force a recalculation
                        twipsWidth, twipsHeight, frameRate, -1); //frame count: -1 to force a recalculation
    }

    /**
     * SWFTags interface
     *
     * Intercept the end tag and insert extra frames
     */
    public void tag(int tagType, boolean longTag, byte[] contents) throws IOException {
        if (tagType == SWFConstants.TAG_END) {
            while (_count > 0) {
                _swftags.tag(SWFConstants.TAG_SHOWFRAME, false, null);
                _count--;
            }
        }

        // Pass the tag through
        _swftags.tag(tagType, longTag, contents);
    }

    /**
     * Arguments are:
     * 0. Name of input SWF
     * 1. Name of output SWF
     * 2. Number of frames to append
     *
     * e.g. java AddFrames movie1.swf out.swf 25
     */
    public static void main(String[] args) throws Exception {
        FileInputStream in = new FileInputStream(args[0]);

        int count = Integer.parseInt(args[2]);

        // SWFWriter implements SWFTags and writes to an output file
        SWFWriter swfwriter = new SWFWriter(args[1]);

        // AddFrames implements the SWFTags interface
        AddFrames adder = new AddFrames(swfwriter, count);

        //  SWFReader reads an input file and drives a SWFTags interface
        SWFReader reader = new SWFReader(adder, in);

        // read the input SWF file and pass it through the interface pipeline
        reader.readFile();
        in.close();
    }
}
