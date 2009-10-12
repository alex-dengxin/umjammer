
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;


/** JMF controler */
/**
 * ストリーミング受信クラス
 * ----------------How to use:-------------------------
 * java TestClient サーバーがあがってるＩＰアドレス
 * by takuro
 */
public class TestClient {

    private static Player player = null;

    public static void main(String argv[]) {
        String _url = "rtp://" + argv[0] + ":44444/audio";
        startPlayer(_url);
    }

    public static void startPlayer(String url) {
        System.out.println(">>> startPlayer()");
        try {
            System.out.println(url);
            MediaLocator locator = new MediaLocator(url);
            System.out.println("hoge2");
            player = Manager.createRealizedPlayer(locator);
            System.out.println("go to play");
            player.start();

        } catch (Throwable t) {
            System.out.println("Cannot get audio stream from " + url);
        }
        System.out.println("<<< startPlayer()");
    }

    public static void stopPlayer() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

}
