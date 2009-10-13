import java.io.FileInputStream;
import java.io.IOException;

import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;
import com.anotherbigidea.flash.writers.SWFTagDumper;


/**
 * Reads and parses a SWF file and dumps a textual representation
 * to System.err.
 *
 * SWFTagDumper is a class that implements the SWFTagTypes interface
 * so it can be driven by the TagParser class.
 */
public class DumpSWF {
    /**
     * First arg is the name of the input SWF file
     * If second arg exists then actions are decompiled - the arg is ignored
     */
    public static void main(String[] args) throws IOException {
        FileInputStream in = new FileInputStream(args[0]);

        SWFTagDumper dumper = new SWFTagDumper(false, args.length > 1);
        TagParser parser = new TagParser(dumper);
        SWFReader reader = new SWFReader(parser, in);
        reader.readFile();
        in.close();

        //must flush - or the output will be lost when the process ends
        dumper.flush();
    }
}
