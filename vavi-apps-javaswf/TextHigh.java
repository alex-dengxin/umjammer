import com.anotherbigidea.flash.movie.Font;
import com.anotherbigidea.flash.movie.FontDefinition;
import com.anotherbigidea.flash.movie.FontLoader;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Text;
import com.anotherbigidea.flash.structs.Color;


/**
 * Example of creating text using the Movie package.
 * Copies the font glyph definitions from an existing SWF.
 */
public class TextHigh {
    /**
     * arg[0] is the name of the SWF to be output.
     * Requires VerdanaFont.swf to be in the current directory
     */
    public static void main(String[] args) throws Exception {
        // Load a font from another movie.  That movie should contain only an
        // edit field that is specified to include all the glyphs in the
        // appropriate font.
        // Font definitions can be referenced by multiple fonts (for example if
        // there is a font used for a text block and another font used with an
        // edit field which restricts the allowable characters).
        FontDefinition fontdef = FontLoader.loadFont("VerdanaFont.swf");

        Movie movie = new Movie();
        Frame frame = movie.appendFrame();

        // Create a text object with a default transform
        Text text = new Text(null);

        // The font references the Font Definition and pulls over only the 
        // glyph definitions that are required for any text referencing the font
        Font font = new Font(fontdef);

        // Add a row of characters - specify the starting (x,y) within the text symbol
        text.row(font.chars("Hello", 25), new Color(0, 0, 255), 0, 0, true, true);

        // Add another row - different color, no (x,y) specified so the chars will
        // flow immediately after the preceding chars.
        text.row(font.chars(" World !!", 25), new Color(255, 0, 255), 0, 0, false, false);

        // instantiate the text
        frame.placeSymbol(text, 200, 200);

        // save the movie
        movie.write(args[0]);
    }
}
