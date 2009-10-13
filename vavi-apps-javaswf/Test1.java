/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Instance;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.MovieClip;
import com.anotherbigidea.flash.movie.Shape;
import com.anotherbigidea.flash.movie.Transform;
import com.anotherbigidea.flash.structs.AlphaColor;
import com.anotherbigidea.flash.structs.Color;


/**
 * Test1.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070618 nsano initial version <br>
 */
public class Test1 {

    public static void main(String[] args) throws Exception {

        final int width = 400;
        final int height = 300;

        // 縁
        Shape border = new Shape();
        border.defineLineStyle(5, new Color(0, 255, 0));
        border.setLineStyle(1);
        border.move(0, 0);
        border.line(width, 0);
        border.line(width, height);
        border.line(0, height);

        // テキスト
        Shape text = new Shape();
        text.defineLineStyle(1, null);
        text.setLineStyle(1);
        java.awt.Font font = java.awt.Font.decode(null).deriveFont(30.0f);
        GryphOut.PathDrawer drawer = new GryphOut.SwfPathDrawer(text);
        GryphOut go = new GryphOut();
        go.setFont(font);
        go.draw("ｷﾀ━━━━(ﾟ∀ﾟ)━━━━!!", 0, 0, drawer);

        // テキストをアニメーションのオブジェクトへセット
        MovieClip clip = new MovieClip();
        Frame clipFrame = clip.appendFrame();
        Instance inst = clipFrame.placeSymbol(text, 0, 0);

        // テキストのアニメーション設定
        for (int i = 0; i < 360; i += 3) {
            // Transform matrix = new Transform(i * Math.PI / 180.0, 0.0, 0.0);
            Transform matrix = new Transform(i * Math.PI / 180.0, i / 50.0, i / 50.0, 0.0, 0.0);
            Frame f1 = clip.appendFrame();
            f1.alter(inst, matrix, null);
        }

        // メインswf環境設定
        Movie movie = new Movie();
        movie.setWidth(width);
        movie.setHeight(height);
        movie.setBackColor(new AlphaColor(255, 255, 255, 0));
        Frame movieFrame = movie.appendFrame();
        // 縁をセット
        movieFrame.placeSymbol(border, 0, 0);
        // アニメーションのオブジェクトをセット
        movieFrame.placeSymbol(clip, 100, 100);

        movie.write("sample3.swf");
    }
}

/* */
