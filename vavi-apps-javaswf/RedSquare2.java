import java.io.IOException;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFShape;
import com.anotherbigidea.flash.interfaces.SWFTagTypes;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.writers.SWFWriter;
import com.anotherbigidea.flash.writers.TagWriter;


/**
 * An example of writing directly to the SWF file format interfaces.
 * Create a movie consisting of 4 instances of a Movie Clip containing
 * a rotating red square.
 *
 * Compare this to RedSquare.java
 */
public class RedSquare2 {
    /**
     * First arg is output filename
     */
    public static void main(String[] args) throws IOException {
        SWFWriter writer = new SWFWriter(args[0]);
        SWFTagTypes swf = new TagWriter(writer);

        swf.header(5, //Flash version
                   -1, //unknown length
                   550 * SWFConstants.TWIPS, //width in twips
                   400 * SWFConstants.TWIPS, //height in twips
                   12, //frames per sec
                   -1); //unknown frame count

        swf.tagSetBackgroundColor(new Color(255, 255, 255));

        // define a shape
        Rect outline = new Rect(-1020, -1020, 1020, 1020);
        SWFShape shape = swf.tagDefineShape(1, outline); //id = 1

        // define the shape geometry - coords are in twips (= 1/20 pixel)
        shape.defineFillStyle(new Color(255, 0, 0));
        shape.defineLineStyle(40, new Color(0, 0, 0));
        shape.setFillStyle1(1);
        shape.setLineStyle(1);
        shape.move(-1000, -1000); //move coords are absolute
        shape.line(2000, 0); //line coords are deltas
        shape.line(0, 2000);
        shape.line(-2000, 0);
        shape.line(0, -2000);
        shape.done(); //don't forget this

        // define a sprite (movie clip)
        SWFTagTypes sprite = swf.tagDefineSprite(2); //id =2

        // place the shape in the first frame of the sprite
        sprite.tagPlaceObject2(false, -1, 1, 1, new Matrix(), null, -1, null, 0);

        sprite.tagShowFrame(); //end the first frame

        // Rotate the square (using degrees for clarity)
        for (int angle = 10; angle < 90; angle += 10) {
            // Convert degrees to radians
            double radians = (angle * Math.PI) / 180.0;

            // Create a rotation matrix
            double sin = Math.sin(radians);
            double cos = Math.cos(radians);
            Matrix matrix = new Matrix(cos, cos, sin, -sin, 0, 0);

            // Alter the square using the transformation matrix
            sprite.tagPlaceObject2(true, -1, 1, -1, matrix, null, -1, null, 0);
            sprite.tagShowFrame();
        }

        // End the sprite timeline
        sprite.tagEnd();

        // Place 4 instances of teh sprite (at depths 1,2,3,4)
        swf.tagPlaceObject2(false, -1, 1, 2, new Matrix(2000, 2000), null, -1, null, 0);
        swf.tagPlaceObject2(false, -1, 2, 2, new Matrix(6000, 2000), null, -1, null, 0);
        swf.tagPlaceObject2(false, -1, 3, 2, new Matrix(2000, 6000), null, -1, null, 0);
        swf.tagPlaceObject2(false, -1, 4, 2, new Matrix(6000, 6000), null, -1, null, 0);

        // End the first frame of the main timeline
        swf.tagShowFrame();

        // End the main timeline
        swf.tagEnd();
    }
}
