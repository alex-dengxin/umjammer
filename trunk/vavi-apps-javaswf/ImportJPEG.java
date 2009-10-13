import java.io.FileInputStream;

import com.anotherbigidea.flash.movie.ImageUtil;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Shape;


/**
 * Example of importing a JPEG image.
 */
public class ImportJPEG {
    /**
     * args[0] is filename of JPEG
     * args[1] is filename of output SWF
     */
    public static void main(String[] args) throws Exception {
        // open the JPEG
        FileInputStream jpegIn = new FileInputStream(args[0]);

        // create a shape that uses the image as a fill
        // (images cannot be placed directly - they can only be used as shape fills)
        int[] size = new int[2];
        Shape image = ImageUtil.shapeForImage(jpegIn, size);

        int width = size[0];
        int height = size[1];
        jpegIn.close();

        // Add a black border to the image shape (origin is in top left corner)
        image.defineLineStyle(1, null); //default color is black
        image.setLineStyle(1);
        image.line(width, 0);
        image.line(width, height);
        image.line(0, height);
        image.line(0, 0);

        Movie movie = new Movie(width + 10, height + 10, 12, 5, null);
        movie.appendFrame().placeSymbol(image, 5, 5);

        movie.write(args[1]);
    }
}
