/*
 * Copyright (c) 2000-2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスはX、Yの二次元の座標を格納するクラスです。
 * X, Y は公開されていますが、変更しないで下さい･･･。
 *
 * @version	新規作成 2000/06/26 石戸谷顕太朗
 *          追加変更 2002/12/08 石戸谷顕太朗
 *			 CObjectからの継承を取った。
 *			 WindowsのRECT構造体との互換性の排除
 */
class Point {
    /** */
    public Point(final int tempX /* = 0 */, final int tempY /* = 0 */) {
        setPoint(tempX, tempY);
    }

    /** */
    public Point(final Point temp) {
        setPoint(temp);
    }
    
    /** + 演算子の多重定義 */
    public Point operatorPlus(final Point right) {
        return new Point(x + right.x, y + right.y);
    }

    /** -演算子の多重定義 */
    public Point operatorMinus(final Point right) {
        return new Point(x - right.x, y - right.y);
    }

    /** ＝ 演算子の多重定義 */
    public final Point operatorLet(final Point right) {
        setPoint(right.x, right.y);
        return this;
    }

    /** += 演算子の多重定義 */
    public final Point operatorPlusLet(final Point right) {
        addPoint(right);
        return this;
    }

    /** -= 演算子の多重定義 */
    public final Point operatorMinusLet(final Point right) {
        subPoint(right);
        return this;
    }

    /** () 演算子の多重定義 */
    public final Point operatorFunction(final int TempX, final int TempY) {
        setPoint(TempX, TempY);
        return this;
    }

    /** < 演算子の多重定義 */
    public boolean operatorLessThan(final Point right) {
        if (x < right.x && y < right.y) {
            return true;
        }
        return false;
    }

    /** > 演算子の多重定義 */
    public boolean operatorGreaterThan(final Point right) {
        if (x > right.x && y > right.y) {
            return true;
        }
        return false;
    }

    /** ==演算子の多重定義 */
    public boolean operatorEqual(final Point right) {
        if (x == right.x && y == right.y) {
            return true;
        }
        return false;
    }

    /** != 演算子の多重定義 */
    public boolean operatorNotEqual(final Point right) {
        if (x != right.x || y != right.y) {
            return true;
        }
        return false;
    }

    /** <= 演算子の多重定義 */
    public boolean operatorLessEqual(final Point right) {
        if (x <= right.x && y <= right.y) {
            return true;
        }
        return false;
    }

    /** >= 演算子の多重定義 */
    public boolean operatorGreaterEqual(final Point right) {
        if (x >= right.x && y >= right.y) {
            return true;
        }
        return false;
    }
    
    /** */
    public int x;
    /** */
    public int y;

    /** ポイントのセット関数 */
    private void setPoint(final Point temp) {
        x = temp.x;
        y = temp.y;
    }

    /** ポイントのセット関数 */
    private void setPoint(final int tempX, final int tempY) {
        x = tempX;
        y = tempY;
    }

    /** ポイントをたす関数 */
    private void addPoint(final Point temp) {
        x += temp.x;
        y += temp.y;
    }

    /** ポイントを引く関数 */
    private void subPoint(final Point temp) {
        x -= temp.x;
        y -= temp.y;
    }
}
