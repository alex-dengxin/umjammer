import java.io.IOException;

import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.flash.interfaces.SWFTagTypes;
import com.anotherbigidea.flash.structs.AlphaColor;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.structs.Matrix;
import com.anotherbigidea.flash.structs.Rect;
import com.anotherbigidea.flash.writers.SWFWriter;
import com.anotherbigidea.flash.writers.TagWriter;


/**
 * Creates an Edit Field and writes the current date/time to it in a loop
 */
public class TimeField {
    /**
     * First arg is output filename
     */
    public static void main(String[] args) throws IOException {
        SWFWriter writer = new SWFWriter(args[0]);
        SWFTagTypes swf = new TagWriter(writer);

        swf.header(5, //Flash version
                   -1, //unknown length
                   300 * SWFConstants.TWIPS, //width in twips
                   50 * SWFConstants.TWIPS, //height in twips
                   2, //frames per sec
                   -1); //frame count to be determined

        swf.tagSetBackgroundColor(new Color(255, 255, 255));

        // Serif system font
        swf.tagDefineFont2(1, SWFConstants.FONT2_ANSI, "_serif", 0, 0, 0, 0, null, null, null, null, null, null);

        // Edit Field with variable "foo"
        swf.tagDefineTextField(2, "foo", null, new Rect(0, 0, 6000, 600), 0, new AlphaColor(0, 0, 255, 255), SWFConstants.TEXTFIELD_ALIGN_CENTER, 1, 280, 0, 0, 0, 0, 0);

        // Place the field
        swf.tagPlaceObject2(false, -1, 1, 2, new Matrix(200, 200), null, -1, null, 0);

        // End the first frame of the main timeline
        swf.tagShowFrame();

        // Actions..
        SWFActions acts = swf.tagDoAction();
        acts.start(0); //frame actions have no conditions
        acts.lookupTable(new String[] { "mydate", "Date", "foo", "toString" });
        acts.lookup(0); //push "mydate"
        acts.push(0); //number of args for new Date()
        acts.lookup(1); //push "Date"
        acts.newObject(); //new Date()
        acts.setVariable(); //mydate = new Date()

        acts.lookup(2); //push "foo"
        acts.push(0); //number of args for toString()
        acts.lookup(0); //push "mydate"
        acts.getVariable(); //push value of mydate
        acts.lookup(3); //push "toString"
        acts.callMethod(); //call mydate.toString()
        acts.setVariable(); //foo = mydate.toString()
        acts.gotoFrame(1); //actually frame 2 - frames indices start with zero
        acts.play(); //goto and play

        acts.end();
        acts.done();

        // End the second frame
        swf.tagShowFrame();

        // End the main timeline
        swf.tagEnd();
    }
}
