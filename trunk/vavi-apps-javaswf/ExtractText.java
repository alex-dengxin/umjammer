import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.anotherbigidea.flash.interfaces.SWFText;
import com.anotherbigidea.flash.interfaces.SWFVectors;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;
import com.anotherbigidea.flash.structs.AlphaColor;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.writers.SWFTagTypesImpl;


/**
 * Shows how to parse a Flash movie and extract all the text in Text symbols
 * and the initial text in Edit Fields.  Output is to System.out.
 *
 * A "pipeline" is set up in the main method:
 *
 * SWFReader-->TagParser-->ExtractText
 *
 * SWFReader reads the input SWF file and separates out the header
 * and the tags.  The separated contents are passed to TagParser which
 * parses out the individual tag types and passes them to ExtractText.
 *
 * ExtractText extends SWFTagTypesImpl and overrides some methods.
 */
public class ExtractText extends SWFTagTypesImpl {
    /**
     * Store font info keyed by the font symbol id
     * Each entry is an int[] of character codes for the correspnding font glyphs
     * (An empty array denotes a System Font)
     */
    protected Map<Integer, int[]> fontCodes = new HashMap<Integer, int[]>();

    public ExtractText() {
        super(null);
    }

    /**
     * SWFTagTypes interface
     * Save the Text Font character code info
     */
    public void tagDefineFontInfo(int fontId, String fontName, int flags, int[] codes) throws IOException {
        fontCodes.put(new Integer(fontId), codes);
    }

    /**
     * SWFTagTypes interface
     * Save the character code info
     */
    public SWFVectors tagDefineFont2(int id, int flags, String name, int numGlyphs, int ascent, int descent, int leading, int[] codes, int[] advances, Rect[] bounds, int[] kernCodes1, int[] kernCodes2, int[] kernAdjustments) throws IOException {
        fontCodes.put(new Integer(id), (codes != null) ? codes : new int[0]);

        return null;
    }

    /**
     * SWFTagTypes interface
     * Dump any initial text in the field
     */
    public void tagDefineTextField(int fieldId, String fieldName, String initialText, Rect boundary, int flags, AlphaColor textColor, int alignment, int fontId, int fontSize, int charLimit, int leftMargin, int rightMargin, int indentation, int lineSpacing) throws IOException {
        if (initialText != null) {
            System.out.println(initialText);
        }
    }

    /**
     * SWFTagTypes interface
     */
    public SWFText tagDefineText(int id, Rect bounds, Matrix matrix) throws IOException {
        return new TextDumper();
    }

    /**
     * SWFTagTypes interface
     */
    public SWFText tagDefineText2(int id, Rect bounds, Matrix matrix) throws IOException {
        return new TextDumper();
    }

    public class TextDumper implements SWFText {
        protected Integer fontId;
        protected boolean firstY = true;

        public void font(int fontId, int textHeight) {
            this.fontId = new Integer(fontId);
        }

        public void setY(int y) {
            if (firstY) {
                firstY = false;
            } else {
                System.out.println(); //Change in Y - dump a new line
            }
        }

        public void text(int[] glyphIndices, int[] glyphAdvances) {
            int[] codes = fontCodes.get(fontId);
            if (codes == null) {
                System.out.println("\n**** COULD NOT FIND FONT INFO FOR TEXT ****\n");
                return;
            }

            // Translate the glyph indices to character codes
            char[] chars = new char[glyphIndices.length];

            for (int i = 0; i < chars.length; i++) {
                int index = glyphIndices[i];

                if (index >= codes.length) //System Font ?
                 {
                    chars[i] = (char) index;
                } else {
                    chars[i] = (char) (codes[index]);
                }
            }

            System.out.print(chars);
        }

        public void color(Color color) {
        }

        public void setX(int x) {
        }

        public void done() {
            System.out.println();
        }
    }

    /**
     * Arguments are:
     * 0. Name of input SWF
     */
    public static void main(String[] args) throws IOException {
        InputStream in = new FileInputStream(args[0]);

        ExtractText extractor = new ExtractText();

        // TagParser implements SWFTags and drives a SWFTagTypes interface
        TagParser parser = new TagParser(extractor);

        //  SWFReader reads an input file and drives a SWFTags interface
        SWFReader reader = new SWFReader(parser, in);

        // read the input SWF file and pass it through the interface pipeline
        reader.readFile();
        in.close();
    }
}
