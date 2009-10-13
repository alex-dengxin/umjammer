import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;

import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Symbol;
import com.anotherbigidea.flash.readers.MovieBuilder;
import com.anotherbigidea.flash.readers.SWFReader;
import com.anotherbigidea.flash.readers.TagParser;


/**
 * Simple example of using MovieBuilder to create a Movie object.
 * Also shows that MovieBuilder creates a map of the defined symbols.
 */
public class ReadMovie {
    public static void main(String[] args) throws Exception {
        FileInputStream in = new FileInputStream(args[0]);

        MovieBuilder builder = new MovieBuilder();
        TagParser parser = new TagParser(builder);
        SWFReader reader = new SWFReader(parser, in);
        reader.readFile();
        in.close();

        Movie movie = builder.getMovie();
        Map<Integer, Symbol> symbols = builder.getDefinedSymbols();

        for (Iterator<Integer> it = symbols.keySet().iterator(); it.hasNext();) {
            Integer id = it.next();
            Symbol symbol = symbols.get(id);

            System.err.println("Movie " + movie + ": Symbol " + id + ": class=" + symbol.getClass().getName());
        }
    }
}
