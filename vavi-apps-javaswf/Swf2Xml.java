import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.xml.sax.InputSource;

import com.anotherbigidea.flash.readers.SWFSaxParser;
import com.anotherbigidea.util.xml.XMLWriter;


/**
 * Reads and parses a SWF file and write XML to System.out.
 */
public class Swf2Xml {
    /**
     * @param args 0: in swf, 1: out xml
     */
    public static void main(String[] args) throws Exception {
        InputStream in = new FileInputStream(args[0]);

        SWFSaxParser parser = new SWFSaxParser();
        XMLWriter writer = new XMLWriter(new FileOutputStream(args[1]));
        parser.setContentHandler(writer);
        parser.parse(new InputSource(in));

        in.close();
    }
}

/* */
