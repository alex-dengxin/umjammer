/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.ArrayList;
import java.util.List;


/**
 * このクラスは、'0'と'1'で二値化された画像データを扱う為のクラスです。
 *
 * @version	新規作成 2002/12/16(Mon) 石戸谷顕太朗
 */
class BinaryImage {

    /** コンストラクタ */
    public BinaryImage() {
        data.clear();
    }

    /** コピーコンストラクタ */
    public BinaryImage(final BinaryImage temp) {
        data.clear();
        List<List<Boolean>> v = temp.data;
        data.addAll(v);
        row = temp.row;
        col = temp.col;
    }
    
    /** 代入演算子 */
    public final BinaryImage operatorLet(final BinaryImage right) {
        data.clear();
        List<List<Boolean>> v = right.data;
        data.addAll(v);
        row = right.row;
        col = right.col;
        return this;
    }

    /** 初期化関数、row * col の大きさの領域を確保し、Valで初期化する。 */
    public void initialize(final int x, final int y, boolean val /* = false */) {
        col = x;
        row = y;
        List<List<Boolean>> Data = new ArrayList<List<Boolean>>(row);
        for (int r = 0; r < row; r++) {
            List<Boolean> cs = new ArrayList<Boolean>(col);
            Data.add(cs);
        }
    }

    /** 初期化関数、渡された二次元配列で初期化する関数。 */
    public void initialize(final List<List<Boolean>> v) {
        col = v.get(0).size();
        row = v.size();
        data = v;
    }

    /** 画像中の 1 ピクセルを Value の値で書き換える。 */
    public void putPixel(final int x, final int y, boolean val) {
        data.get(y).add(x, new Boolean(val));
    }

    /** 画像中の X, Y の位置にあるピクセルの値を取得。 */
    public boolean getPixel(final int x, final int y) {
        return data.get(y).get(x).booleanValue();
    }

    /** 格納されているイメージの最大行を取得する関数。 */
    public int getMaxRow() {
        return row;
    }

    /** 格納されているイメージの最大行を取得する関数。 */
    public int getMaxCol() {
        return col;
    }

    /** 画像中の X, Y の位置にあるピクセルの値を反転。 */
    public void flip(final int x, final int y) {
        data.get(y).set(x, new Boolean(!data.get(y).get(x).booleanValue()));
    }

    /** 与えられた矩形をvで塗りつぶす */
    public void fill(final int l, final int t, final int r, final int b, boolean v) {
        if (row < t || row < b || col < t || col < b) {
            throw new IllegalArgumentException("overflow");
        }
        for (int y = t; y < b; y++) {
            for (int x = l; x < r; x++) {
                putPixel(x, y, v);
            }
        }
    }

    /** データを破棄しクリアする関数 */
    public void clear() {
        data.clear();
        row = 0;
        col = 0;
    }

    /** 入力されたイメージと格納されているイメージの Or をとり返す。 */
    public BinaryImage or(BinaryImage temp) {
        if (row != temp.row || col != temp.col) {
            throw new IllegalArgumentException("Size does not matched");
        }

        BinaryImage ret = new BinaryImage();
        ret.initialize(col, row, false);
        for (int y = 0; y < row; y++) {
            for (int x = 0; x < col; x++) {
                ret.putPixel(x, y, getPixel(x, y) | temp.getPixel(x, y));
            }
        }
        return ret;
    }

    /**
     * 入力された部分イメージと格納されているイメージの X, Y 位置の同じ大きさの部分と
     * の Or をとり返す。
     */
    public BinaryImage or(final int sx, final int sy, BinaryImage temp) {
        if (row < sy + temp.row || col < sx + temp.col) {
            throw new IllegalArgumentException("Error : Overflow @ CBinaryImage::or x,y");
        }

        BinaryImage ret = this;
        for (int y = sy; y < sy + temp.row; y++) {
            for (int x = sx; x < sx + temp.col; x++) {
                ret.putPixel(x, y, getPixel(x, y) | temp.getPixel(x - sx, y - sy));
            }
        }
        return ret;
    }

    /** Dataを全て反転して返す。 */
    public BinaryImage not() {
        BinaryImage ret = new BinaryImage();
        ret.initialize(col, row, false);
        for (int y = 0; y < row; y++) {
            for (int x = 0; x < col; x++) {
                ret.putPixel(x, y, !getPixel(x, y));
            }
        }
        return ret;
    }

    /** */
    private List<List<Boolean>> data;

    /** */
    private int row;

    /** */
    private int col;
}

/* */
