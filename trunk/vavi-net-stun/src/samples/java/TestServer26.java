
import java.io.File;
import java.io.IOException;

import javax.media.DataSink;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.Processor;
import javax.media.ProcessorModel;
import javax.media.format.AudioFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;


/** JMF controler */
/**
 * ストリーミング送信クラス
 * --------------How to use: -------------------------------------
 * java TestServer26 クライアントのＩＰアドレス 音楽ファイルのパス(mp3は無理。wav推奨)
 *
 *by takuro
 */
public class TestServer26 {

    private static String url = "";

    private static Player player = null;

    private static DataSink dataSink = null;

    private static MediaLocator mediaLocator = null;

    private static ContentDescriptor descriptor = new ContentDescriptor(ContentDescriptor.RAW_RTP);

    private static Format[] formats;

    private static Processor mediaProcessor = null;

    public static void main(String argv[]) {

        System.out.println(argv[1]);

        String _url3 = "rtp://" + argv[0] + ":44444/audio";

        formats = new Format[] {
            new AudioFormat(AudioFormat.MPEG_RTP)
        };
        url = argv[1];
        setLocator(new File(url), _url3);
        // stopPlayer();
        System.out.println("gogo");

    }

    public static void setLocator(File file, String url3) {
        System.out.println(">>> startPlayer()");
        try {

            MediaLocator locator = new MediaLocator(file.toURI().toURL());
            DataSource source = Manager.createDataSource(locator);
            String type = source.getContentType();
            System.out.println(type);
            mediaProcessor = Manager.createRealizedProcessor(new ProcessorModel(source, formats, descriptor));

            mediaLocator = new MediaLocator(url3);

            // dataSink = Manager.createDataSink(source,mediaLocator);

            dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), mediaLocator);

            /** for test * */
            player = Manager.createRealizedPlayer(locator);

            System.out.println("hoge");
            startPlayer();

            /*********************************************************************************************************************
             * player = Manager.createRealizedPlayer(locator);
             * 
             * player.start();
             ********************************************************************************************************************/

        } catch (Exception e) {
            System.err.println(e.toString());
        }
        System.out.println("<<< startPlayer()");
    }

    public static void startPlayer() {
        try {
            mediaProcessor.start();
            System.out.println("before");
            dataSink.open();

            System.out.println("midium");
            dataSink.start();

            // player.start(); ローカルで鳴らす場合

            System.out.println("after");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** Stop transmitting the media. */
    public static void stopPlayer() {

        try {

            dataSink.close();
            dataSink.stop();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
