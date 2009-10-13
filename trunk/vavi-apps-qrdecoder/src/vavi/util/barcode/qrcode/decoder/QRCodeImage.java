/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.List;


/**
 * このクラスは、
 * Message → BinaryImage, BinaryImage -> Message の相互変換を行なう
 * クラスです。
 * Message -> BinaryImage 変換は以下のように行ないます。
 * MessageをBinaryImage に配置→マスク→出力
 * BinaryImage->Message 変換は以下のように行ないます。
 * BinaryImage を読み込み->マスク解除→出力
 * Mode の違う関数を呼び出すと例外を送出します。
 *
 * @version 新規作成 2002/12/16(Mon) 石戸谷顕太朗
 */
class QRCodeImage {
    /** */
    enum Mode {
        ENCODER,
        DECODER
    }

    /** */
    enum FormInfo {
        FORMINFO1,
        FORMINFO2
    }

    /** */
    public QRCodeImage() {
    }
    
    // Encoderで使う関数群 (Encoderモードでしか使えない)

    /** メッセージのセット (Encoder) */
    public void setMessage(final Message msg, final Symbol sym) {
        if (mode != Mode.ENCODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        if (msg == null) {
            throw new IllegalArgumentException("Message is not initialized");
        }
        if (sym == null) {
            throw new IllegalArgumentException("Symbol is not initialized");
        }
        modulesPerSide = sym.getModulesPerSide();
        image.initialize(modulesPerSide, modulesPerSide, false);
        data.initialize(modulesPerSide, modulesPerSide, false);
        function.initialize(modulesPerSide, modulesPerSide, false);
        system.initialize(modulesPerSide, modulesPerSide, false);
        positioning.initialize(modulesPerSide, modulesPerSide, false);
        mask.Initialize(data);
        masked = false;

        formatFunctionLayer(sym);
        Arrange(msg.getData(), sym);
    }
    
    /** 型番情報の設定 (Encoder) */
    public void setVersionInfo(final BinaryString info) {
        if (mode != Mode.ENCODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        if (info == null) {
            throw new IllegalArgumentException("Info is empty");
        }
        if (info.GetLength() != 18) {
            throw new IllegalArgumentException("invalid info length");
        }
        if (modulesPerSide <= 41) {
            throw new IllegalArgumentException("This version does not need VerInfo");
        }
        // 左下の型番情報の設定
        final int sx = modulesPerSide - 11;
        for (int x = sx; x < sx + 3; x++) {
            for (int y = 0; y < 6; y++) {
                system.putPixel(x, y, info.at(y * 3 + x - sx));
            }
        }
        // 右上の型番情報の設定
        final int sy = modulesPerSide - 11;
        for (int x = 0; x < 6; x++) {
            for (int y = sy; y < sy + 3; y++) {
                system.putPixel(x, y, info.at(x * 3 + y - sy));
            }
        }
    }

    /** 形式情報の設定 (Encoder) */
    public void setFormInfo(final BinaryString info) {
        if (mode != Mode.ENCODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        if (info == null) {
            throw new IllegalArgumentException("Info is empty");
        }
        if (info.GetLength() != 15) {
            throw new IllegalArgumentException("invalid info length");
        }

        // 左上の形式情報の設定
        system.putPixel(8, 0, info.at(14));
        system.putPixel(8, 1, info.at(13));
        system.putPixel(8, 2, info.at(12));
        system.putPixel(8, 3, info.at(11));
        system.putPixel(8, 4, info.at(10));
        system.putPixel(8, 5, info.at(9));
        system.putPixel(8, 7, info.at(8));
        system.putPixel(8, 8, info.at(7));
        system.putPixel(7, 8, info.at(6));
        system.putPixel(5, 8, info.at(5));
        system.putPixel(4, 8, info.at(4));
        system.putPixel(3, 8, info.at(3));
        system.putPixel(2, 8, info.at(2));
        system.putPixel(1, 8, info.at(1));
        system.putPixel(0, 8, info.at(0));

        int sx = modulesPerSide - 1;
        int sy = modulesPerSide - 1 - 6;
        // 右上から左下にかけての形式情報の設定
        system.putPixel(sx , 8, info.at(14));
        system.putPixel(sx - 1, 8, info.at(13));
        system.putPixel(sx - 2, 8, info.at(12));
        system.putPixel(sx - 3, 8, info.at(11));
        system.putPixel(sx - 4, 8, info.at(10));
        system.putPixel(sx - 5, 8, info.at(9));
        system.putPixel(sx - 6, 8, info.at(8));
        system.putPixel(sx - 7, 8, info.at(7));
        system.putPixel(8, sy , info.at(6));
        system.putPixel(8, sy + 1, info.at(5));
        system.putPixel(8, sy + 2, info.at(4));
        system.putPixel(8, sy + 3, info.at(3));
        system.putPixel(8, sy + 4, info.at(2));
        system.putPixel(8, sy + 5, info.at(1));
        system.putPixel(8, sy + 6, info.at(0));
    }
    
    /** マスクコードの設定 (Encoder) */
    public void setMaskCode(final MaskDecorator.Mask mc) {
        if (mode != Mode.ENCODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        maskCode = mc;
    }
    
    /** 画像の取得 (Encoder) */
    public BinaryImage getImage(MaskDecorator.Mask mc) {
        if (mode != Mode.ENCODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        BinaryImage temp = null; 
        temp.initialize(modulesPerSide, modulesPerSide, false);
        for (int x = 0; x < modulesPerSide; x++) {
            for (int y = 0; y < modulesPerSide; y++) {
                if (positioning.getPixel(x, y)) {
                    temp.putPixel(x, y, mask.GetMaskedPixel(x, y, mc));
                }
            }
        }
        return temp;
    }
    
    /**
     * Decoderで使う関数群 (Decoderモードでしか使えない)
     * 画像の設定 (Decoder)
     * @param img
     * @param sym
     */
    public void setImage(final BinaryImage img, final Symbol sym) {
        if (mode != Mode.DECODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        if (img == null) {
            throw new IllegalArgumentException("Image is empty");
        }
        if (sym == null) {
            throw new IllegalArgumentException("Symbol is empty");
        }
        modulesPerSide = sym.getModulesPerSide();
        image = img;
        formatFunctionLayer(sym);

        masked = true;
        
    }

    /** 形式情報の取得 (Decoder) */
    public BinaryString getFormInfo(final FormInfo which) {
        if (mode != Mode.DECODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }

        BinaryString info = null;
        if (which == FormInfo.FORMINFO1) {
            //左上の形式情報の取得
            info.add(image.getPixel(8, 0));
            info.add(image.getPixel(8, 1));
            info.add(image.getPixel(8, 2));
            info.add(image.getPixel(8, 3));
            info.add(image.getPixel(8, 4));
            info.add(image.getPixel(8, 5));
            info.add(image.getPixel(8, 7));
            info.add(image.getPixel(8, 8));
            info.add(image.getPixel(7, 8));
            info.add(image.getPixel(5, 8));
            info.add(image.getPixel(4, 8));
            info.add(image.getPixel(3, 8));
            info.add(image.getPixel(2, 8));
            info.add(image.getPixel(1, 8));
            info.add(image.getPixel(0, 8));

        } else if (which == FormInfo.FORMINFO2) {
            //右上から左下にかけての形式情報の取得
             int sx = modulesPerSide ;
             int sy = modulesPerSide - 7;
            info.add(image.getPixel(sx , 8));
            info.add(image.getPixel(sx - 1, 8));
            info.add(image.getPixel(sx - 2, 8));
            info.add(image.getPixel(sx - 3, 8));
            info.add(image.getPixel(sx - 4, 8));
            info.add(image.getPixel(sx - 5, 8));
            info.add(image.getPixel(sx - 6, 8));
            info.add(image.getPixel(sx - 7, 8));
            info.add(image.getPixel(8, sy));
            info.add(image.getPixel(8, sy + 1));
            info.add(image.getPixel(8, sy + 2));
            info.add(image.getPixel(8, sy + 3));
            info.add(image.getPixel(8, sy + 4));
            info.add(image.getPixel(8, sy + 5));
            info.add(image.getPixel(8, sy + 6));
        }
        return info;
        
    }

    /** マスク解除用のコード設定 (Decorder) */
    public void setUnMaskCode(final MaskDecorator.Mask mc) {
        if (mode != Mode.DECODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        maskCode = mc;
        unMask(mc);
        
    }
    
    /** */
    public BinaryString getMessage(final Symbol sym) {
        
        if (mode != Mode.DECODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        if (!masked) {
            throw new IllegalArgumentException("This Image was not UnMasked");
        }
        if (sym.isPartial()) {
            throw new IllegalArgumentException("This Method needs compleate symbol");
        }
        boolean isup = true;	// 上下フラグ
        boolean isleft = true;	// 右左フラグ
        int mleft = 0;		// 左連続移動
        int x = modulesPerSide - 1;
        int y = modulesPerSide - 1;
        int tx;
        int ty;
        BinaryString str = new BinaryString();
        for ( int i = 0; i < str.GetLength(); i++) {
            str.add(image.getPixel(x, y) ? new BinaryString("1") : new BinaryString("0"));
            tx = x;
            ty = y;
            while (true) {
                //左移動
                if (isleft) {
                    if (tx > 0) {
                        tx -= 1;
                    }
                    //一番上または下まで行って左に移動する時。
                    if (mleft > 0 && mleft < 4) {
                        mleft++;
                        isleft = true;
                    } else {
                        mleft = 0;
                        isleft = false;
                    }
                    //右上または右下移動
                } else {
                    if (isup) {
                        if (ty > 0) {
                            ty -= 1;
                            tx += 1;
                        } else {
                            isup = false;
                            mleft = 1;
                        }
                    } else {
                        if (ty < modulesPerSide - 1) {
                            ty += 1;
                            tx += 1;
                        } else {
                            isup = true;
                            mleft = 1;
                        }
                    }
                    isleft = true;
                }
                //移動した先にデータが取得できるか調べる
                if (positioning.getPixel(tx, ty)) {
                    x = tx;
                    y = ty;
                    break;
                }
            }
        }
        return str;        
    }
    
    // ユーティリティ

    /** モードの設定 */
    public void setMode(final Mode m) {
        mode = m;
    }

    /** QRコードイメージの取得 */
    public BinaryImage getQRCodeImage() {
        BinaryImage temp;
        if (mode == Mode.ENCODER) {
            temp = getImage(maskCode);
            temp = temp.or(function);
            temp = temp.or(system);
        } else {
            temp = image;
        }
        return temp;
    }

    /** モードの設定 */
    public final MaskDecorator.Mask getMaskCode() {
        return maskCode;
    }
    
    /** メッセージの配置 (Encoder) */
    private void Arrange(final BinaryString str, final Symbol sym) {
        if (mode != Mode.ENCODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
       if (str.GetLength() != sym.getWholeCodeWords() * 8) {
            throw new IllegalArgumentException("Invalid Data");
        }
        boolean isup = true;	// 上下フラグ
        boolean isleft = true;	// 右左フラグ
        int x = modulesPerSide - 1;
        int y = modulesPerSide - 1;
        int tx;
        int ty;
        int m = 0;
        while (m < str.GetLength()) {
            data.putPixel(x, y, str.at(m));
            tx = x;
            ty = y;
            while (true) {
                // タイミングパターンの列は飛ばす。
                if (tx == 6) {
                    tx--;
                }
                // 左移動
                if (isleft) {
                    if (tx > 0) {
                        tx -= 1;
                    }
                    isleft = false;
                    // 右上または右下移動
                } else {
                    if (isup) {
                        if (ty > 0) {
                            ty -= 1;
                            tx += 1;
                        } else {
                            isup = false;
                            if (tx > 0) {
                                tx -= 1;
                            }
                        }
                    } else {
                        if (ty < modulesPerSide - 1) {
                            ty += 1;
                            tx += 1;
                        } else {
                            isup = true;
                            if (tx > 0) {
                                tx -= 1;
                            }
                        }
                    }
                    isleft = true;
                }
                // 移動した先にデータを配置できるか調べる
                if (positioning.getPixel(tx, ty)) {
                    x = tx;
                    y = ty;
                    m++;
                    break;
                }
            }
        }
    }

    /** マスク解除 (Decoder) */
    private void unMask(final MaskDecorator.Mask mc) {
        if (mode != Mode.DECODER) {
            throw new IllegalArgumentException("Wrong Mode");
        }
        for (int x = 0; x < modulesPerSide; x++) {
            for (int y = 0; y < modulesPerSide; y++) {
                image.putPixel(x, y, mask.GetUnMaskedPixel(x, y, mc));
            }
        }
        
    }

    /**
     * メッセージの取得（Decoder）
     * 位置合わせ、位置検出、タイミングパターンの設定及び、ポジショニングレイヤーの
     * 設定
     */
    private void formatFunctionLayer(final Symbol sym) {
        // 位置検出パターンの作成
        BinaryImage f = new BinaryImage();
        f.initialize(7, 7, true);
        f.fill(1, 1, 6, 6, false);
        f.fill(2, 2, 5, 5, true);
        // 位置合わせパターンの作成
        BinaryImage p = new BinaryImage();
        p.initialize(5, 5, true);
        p.fill(1, 1, 4, 4, false);
        p.putPixel(2, 2, true);

        // 位置検出パターンの設定
        function = function.or(0, 0, f);
        function = function.or(modulesPerSide - 7, 0, f);
        function = function.or(0, modulesPerSide - 7, f);

        // 位置合わせパターンの設定
        List<Point> vp = sym.getApPositions();
        for (Point vpi : vp) {
            function = function.or((vpi).x - 2, (vpi).y - 2, p);
            positioning.fill((vpi).x - 2, (vpi).y - 2, (vpi).x + 3, (vpi).y + 3, true);
        }

        // タイミングパターンの設定
        // 縦のタイミングパターン
        boolean t = true;
        for (int y = 8; y < modulesPerSide - 8; y++) {
            function.putPixel(6, y, t);
            positioning.putPixel(6, y, true);
            t = !t;
        }
        // 横のタイミングパターン
        t = true;
        for (int x = 8; x < modulesPerSide - 8; x++) {
            function.putPixel(x, 6, t);
            positioning.putPixel(x, 6, true);
            t = !t;
        }

        // Positioningの位置検出パターン、分離パターン及び形式情報の設定
        // まず左上の位置検出パターンの周り
        positioning.fill(0, 0, 9, 9, true);
        positioning.fill(modulesPerSide - 8, 0, modulesPerSide, 9, true);
        positioning.fill(0, modulesPerSide - 8, 9, modulesPerSide, true);

        // Positioningの型番情報の設定。Version7以上
        if (sym.getVersion() > 6) {
            positioning.fill(modulesPerSide - 10, 0, modulesPerSide - 8, 2, true);
            positioning.fill(0, modulesPerSide - 10, 2, modulesPerSide - 8, true);
        }

        // 4V+9, 8の位置にある常に暗のモジュール。
        positioning.putPixel(8, sym.getVersion() * 4 + 9, true);
        function.putPixel(8, sym.getVersion() * 4 + 9, true);

        // 最後にpositioningを反転
        positioning = positioning.not();        
    }
    
    /** 全てのレイヤーを結合して持つレイヤー */
    private BinaryImage image;
    /** データ部分のみを持つレイヤー */
    private BinaryImage data;
    /** 機能モジュールのみを持つレイヤー */
    private BinaryImage function;
    /** 形式情報、型番情報をもつレイヤー */
    private BinaryImage system;
    /** 配置の為だけに使用されるレイヤー */
    private BinaryImage positioning;
    /** フィルタリングあるいはアンチフィルタリングする。 */
    private MaskDecorator mask;
    
    /** マスク情報 */
    private MaskDecorator.Mask maskCode;
    /** 一辺のモジュール数 */
    private int modulesPerSide;
    /** 機能の切り替え */
    private Mode mode;
    /** マスクされているか。 */
    private boolean masked;
}

/* */
