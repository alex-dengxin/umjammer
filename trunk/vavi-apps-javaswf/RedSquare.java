import java.io.IOException;

import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.MovieClip;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.movie.Transform;
import com.anotherbigidea.flash.structs.Color;


/**
 * An example of using the Movie package to create a simple Flash movie
 * consisting of 4 instances of a Movie Clip containing a rotating red
 * square.
 */
public class RedSquare {
    /**
     * First arg is output filename
     */
    public static void main(String[] args) throws IOException {
        Movie movie = new Movie();

        // Create a red square with a 2 pixel black outline
        // centered on (0,0)
        Shape shape = new Shape();
        shape.defineFillStyle(new Color(255, 0, 0));
        shape.defineLineStyle(2.0, new Color(0, 0, 0));
        shape.setRightFillStyle(1);
        shape.setLineStyle(1);
        shape.move(-50, -50);
        shape.line(50, -50);
        shape.line(50, 50);
        shape.line(-50, 50);
        shape.line(-50, -50);

        // Create a Movie Clip (Sprite)
        MovieClip clip = new MovieClip();
        Frame f1 = clip.appendFrame();

        // Place the red square in the center of the movie clip
        Instance inst = f1.placeSymbol(shape, 0, 0);

        // Rotate the square (using degrees for clarity)
        for (int angle = 10; angle < 90; angle += 10) {
            // Convert degrees to radians
            double radians = (angle * Math.PI) / 180.0;

            Frame f = clip.appendFrame();

            // Create a rotation matrix
            Transform matrix = new Transform(radians, 0.0, 0.0);

            // Alter the square using the transformation matrix
            f.alter(inst, matrix, null);
        }

        // Add a single frame to the movie and give it the stop action to
        //  prevent it from looping (the Movie Clip loops independently)
        Frame frame = movie.appendFrame();
        frame.stop();

        // Place 4 instances of the Movie Clip
        frame.placeSymbol(clip, 100, 100);
        frame.placeSymbol(clip, 300, 100);
        frame.placeSymbol(clip, 100, 300);
        frame.placeSymbol(clip, 300, 300);

        // Save the movie to the output file
        movie.write(args[0]);
    }
}
