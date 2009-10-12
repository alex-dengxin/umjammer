
package vavi.apps.docopin.jmf;

/**
 * ストリーミング受信クラス
 * 
 * $Id: ReceiveControl.java,v 1.4 2003/01/21 18:48:50 matsu Exp $
 */

import java.awt.Component;
import java.awt.Label;

import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;


/** JMF controler */
public class ReceiveControl implements MediaController {

    private String url = "";

    private Player player = null;

    private Component component_display = new Label("none");

    private Component component_controller = new Label("none");

    public ReceiveControl(String url) {
        System.out.println(url);
        this.url = url;
    }

    public void startPlayer() {
        System.out.println(">>> startPlayer()");
        try {
            MediaLocator locator = new MediaLocator(url);
            player = Manager.createRealizedPlayer(locator);

            component_display = player.getVisualComponent();
            component_controller = player.getControlPanelComponent();

            player.start();

        } catch (Throwable t) {
            // t.printStackTrace();
            System.err.println("Cannot get audio stream from " + url);
        }
        System.out.println("<<< startPlayer()");
    }

    public void stopPlayer() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

    public Component getDisplay() {
        System.out.println("### getDisplay()");
        return component_display;
    }

    public Component getController() {
        System.out.println("### getController()");
        return component_controller;
    }
}
