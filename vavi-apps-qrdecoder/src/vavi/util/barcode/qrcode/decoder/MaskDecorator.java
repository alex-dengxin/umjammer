/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスは、BinaryImageのデータの上にかぶさるフィルタとしての
 * 機能を持ちます。選択されたマスクコードによって、BinaryImage上の
 * データをマスクオペレーションによって変化させます。
 *
 * @version	新規作成 2002/12/16(Mon) 石戸谷 顕太朗
 */
class MaskDecorator {
    /**
     * マスクパターンの列挙 (NOTMASKED 以外は使う機会ないけれども)
     */
    enum Mask {
        PATTERN0,
        PATTERN1,
        PATTERN2,
        PATTERN3,
        PATTERN4,
        PATTERN5,
        PATTERN6,
        PATTERN7,
        NOTMASKED
    }

    /** */
    public MaskDecorator() {
        image = null;
    }

    /**
     * イニシャライザ、イメージへの参照をセットする。
     */
    void Initialize(BinaryImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image does not initialized");
        }
        this.image = image;
    }

    /**
     * x, y で表される位置のピクセルを {@link Mask} で表されるパターンでフィルタリングして
     * 返す関数。{@link Mask#NOTMASKED} が選択されていると生データを取得できる。
     */
    boolean GetMaskedPixel(final int x, final int y, final Mask mc /* = NOTMASKED */) {
        if (x > image.getMaxCol() || y > image.getMaxRow()) {
            throw new IllegalArgumentException("Index over flow");
        }
        boolean pixel = image.getPixel(x, y);
        // 取得したモジュールにマスク処理をして返す。
        switch (mc) {
            // 仕様では (x + y) % 2 = 0 が真になるとき反転とあるが、! 演算子を使いたくないので
            // 条件式が真のとき pixel をそのまま返し、偽の時 pixel を反転させて返す。
        case PATTERN0:
            return ((x + y) % 2) != 0 ? pixel : !pixel;
        case PATTERN1:
            return (y % 2) != 0 ? pixel : !pixel;
        case PATTERN2:
            return (x % 3) != 0 ? pixel : !pixel;
        case PATTERN3:
            return ((x + y) % 3) != 0 ? pixel : !pixel;
        case PATTERN4:
            return (((x / 3) + (y / 2)) % 2) != 0 ? pixel : !pixel;
        case PATTERN5:
            return ((x * y) % 2 + (x * y) % 3) != 0 ? pixel : !pixel;
        case PATTERN6:
            return (((x * y) % 2 + (x * y) % 3) % 2) != 0 ? pixel : !pixel;
        case PATTERN7:
            return (((x + y) % 3 + (x + y) % 2) % 2) != 0 ? pixel : !pixel;
        default	:
            return pixel;
        }
    }

    /**
     * x, y で表される位置のピクセルを {@link Mask} で表されるパターンでマスク解除して
     * 返す関数。{@link Mask#NOTMASKED} が選択されていると生データを取得できる。
     */
    boolean GetUnMaskedPixel(final int x, final int y, final Mask mc /* = NOTMASKED */) {
        if (x > image.getMaxCol() || y > image.getMaxRow()) {
            throw new IllegalArgumentException("Index over flow");
        }
        boolean pixel = image.getPixel(x, y);
        // マスク解除する為に符号を反転させておく。
        pixel = !pixel;
        // 取得したモジュールにマスク処理をして返す。
        switch (mc) {
            // 仕様では(x + y) % 2 = 0が真になるとき反転とあるが、!演算子を使いたくないので
            // 条件式が真のときpixelをそのまま返し、偽の時pixelを反転させて返す。
        case PATTERN0:
            return ((x + y) % 2) != 0 ? pixel : pixel;
        case PATTERN1:
            return (y % 2) != 0 ? pixel : !pixel;
        case PATTERN2:
            return (x % 3) != 0 ? pixel : !pixel;
        case PATTERN3:
            return ((x + y) % 3) != 0 ? pixel : !pixel;
        case PATTERN4:
            return (((x / 3) + (y / 2)) % 2) != 0 ? pixel : !pixel;
        case PATTERN5:
            return ((x * y) % 2 + (x * y) % 3) != 0 ? pixel : !pixel;
        case PATTERN6:
            return (((x * y) % 2 + (x * y) % 3) % 2) != 0 ? pixel : !pixel;
        case PATTERN7:
            return (((x + y) % 3 + (x + y) % 2) % 2) != 0 ? pixel : !pixel;
        default	:
            return pixel;
        }
    }
    
    /** 関連付けされた画像 */
    private BinaryImage image;
}

/* */
