import java.io.FileInputStream;

import com.anotherbigidea.flash.writers.SWFSaxWriter;
import com.anotherbigidea.flash.writers.SWFWriter;
import com.anotherbigidea.flash.writers.TagWriter;
import com.anotherbigidea.util.xml.Xerces;


/**
 * Example of converting an XML file to a SWF file.
 * Key point is the use of the SWFSaxWriter class - this implements
 * the standard SAX2 ContentHandler interface (via its parents) and
 * can be driven directly from any compliant SAX2 XML parser.
 *
 * SWFSaxWriter drives an implementation of the SWFTagTypes interface.
 *
 * To convert SWF to XML use the main method of the
 * com.anotherbigidea.flash.readers.SWFSaxParser class.
 *
 * The DTD for the XML is given in javaswf-dtd.txt
 *
 * Uses the Apache Xerces XML parser - download from apache.org.
 *
 * Arg[0] = input xml filename
 * Arg[1] = output swf filename
 */
public class Xml2Swf {
    public static void main(String[] args) throws Exception {
        FileInputStream in = new FileInputStream(args[0]);

        SWFWriter swftags = new SWFWriter(args[1]);
        TagWriter tagwriter = new TagWriter(swftags);
        SWFSaxWriter saxout = new SWFSaxWriter(tagwriter);

        Xerces.parse(saxout, in);
    }
}
