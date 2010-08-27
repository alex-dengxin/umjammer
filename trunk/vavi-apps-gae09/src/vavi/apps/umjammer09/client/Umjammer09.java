
package vavi.apps.umjammer09.client;

import gwt.g2d.client.graphics.KnownColor;
import gwt.g2d.client.graphics.Surface;
import gwt.g2d.client.graphics.TextBaseline;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Umjammer09 implements EntryPoint {

    private Surface surface;
//private Surface surface2;
    
    private TextArea textArea;
    private Button previewButton;
    private Button toowitterButton;
//    private Button twitpicAsQrcodeButton;
    private Anchor signinAnchor;
    private Anchor viewAnchor;

    private ToowitterServiceAsync service = GWT.create(ToowitterService.class);

    private AsyncCallback<String> uploadCallback = new AsyncCallback<String>() {
        public void onFailure(Throwable caught) {
            Window.alert("ERROR\n" + caught.toString());                
        }
        public void onSuccess(String url) {
            viewAnchor.setHref(url);
            Window.alert("投稿成功しました。");                
        }
    };

    private AsyncCallback<Boolean> isSignedCallback = new AsyncCallback<Boolean>() {
        public void onFailure(Throwable caught) {
            Window.alert("ERROR\n" + caught.toString());                
        }
        public void onSuccess(Boolean isSignedin) {
            if (isSignedin) {
                signinAnchor.setEnabled(false);
                toowitterButton.setEnabled(true);
            } else {
                signinAnchor.setEnabled(true);
                toowitterButton.setEnabled(false);
            }
        }
    };

    static final String welcome =
        "ようこそ！「Too言ったー」へ！\n" +
        "\n" +
        "Too言ったーはtwi●erの140文字というセ●い限界を超えて\n" +
        "あなたの満足いくつぶやきを実現します！\n" +
        "\n" +
        "秘密はtw●pic(以下ry\n" +
//        "デジタル情報としてあなたのつぶやきを残すためにQRコードとしても\n" +
//        "保存可能ですよ！\n" +
        "\n" +
        "改行とか考慮していないのでテキストエリアの行末では\n" +
        "自分で改行してください。\n" +
        "\n";

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        surface = new Surface(400, 400);
//surface2 = new Surface(400, 400);

        textArea = new TextArea();
        textArea.setSize("400px", "400px");
        previewButton = new Button("Preview");
        toowitterButton = new Button("Too言っとく");
//        twitpicAsQrcodeButton = new Button("QR Code");

        previewButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                String text = textArea.getText();
                createImage(text);
            }
        });
        
        toowitterButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                String text = textArea.getText();
                createImage(text);

                String image = getImage();
                service.upload(image, uploadCallback);
            }
        });

//        twitpicAsQrcodeButton.addClickHandler(new ClickHandler() {
//            public void onClick(ClickEvent e) {
//                String text = textArea.getText();
//                createImage(text);
//
//                String image = getImage();
//                service.uploadAsQrcode(image, callback);
//            }
//        });

        Image image = new Image("toowitter.png");
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(previewButton);
        buttonPanel.add(toowitterButton);
        SimplePanel spacer = new SimplePanel();
        spacer.setSize("40px", "16px");
        buttonPanel.add(spacer);
        viewAnchor = new Anchor("View Your Post", "http://twitpic.com/");
        buttonPanel.add(viewAnchor);
//        buttonPanel.add(twitpicAsQrcodeButton);
        VerticalPanel panel = new VerticalPanel();
        HorizontalPanel bannerPanel = new HorizontalPanel();
        bannerPanel.add(image);
        spacer = new SimplePanel();
        spacer.setSize("140px", "16px");
        bannerPanel.add(spacer);
        signinAnchor = new Anchor("signin", "signin");
        bannerPanel.add(signinAnchor);
        panel.add(bannerPanel);
        panel.add(surface);
        spacer = new SimplePanel();
        spacer.setSize("40px", "16px");
        panel.add(spacer);
        panel.add(buttonPanel);
        panel.add(textArea);
//panel.add(surface2);

        createImage(welcome);
        //+ (isEnabledToDataURL() ? "" : "あなたのブラウザは残念ながら利用できません。"));

        service.isSigned(isSignedCallback);

        RootPanel.get().add(panel);
    }
    
    void createImage(String text) {
        surface.clear();

        surface.setFillStyle(KnownColor.BLACK);
        surface.setTextBaseline(TextBaseline.TOP);
        
        String[] lines = text.split("\n");
        int y = 0;
        for (String line : lines) {
            surface.fillText(line, 5, y * 16 + 5, 400);
            y++;
        }
    }

    protected native String getImage()
    /*-{
        var canvas = $doc.getElementsByTagName('canvas');
        var scheme = canvas[0].toDataURL("image/png");
        return scheme.replace('data:image/png;base64,', '');
!!$('check-canvas').toDataURL().match('image/png');
    }-*/;

    protected native boolean isEnabledToDataURL()
    /*-{
        var canvas = $doc.getElementsByTagName('canvas');
        return canvas[0].toDataURL("image/png").match('image/png');
    }-*/;
}
