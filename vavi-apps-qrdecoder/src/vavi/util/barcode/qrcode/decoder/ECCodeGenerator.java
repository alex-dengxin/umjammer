/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.ArrayList;
import java.util.List;


/**
 * このクラスは ExpressionTable から生成多項式を取得し、Execute された
 * されたときに入力される RS ブロックからエラー訂正語を作成して返す。
 * ECCodeGenerator == ErrorCollectionCodeGenerator
 *
 * @version	新規作成 2002/11/15(Sun) 石戸谷　顕太朗
 */
class ECCodeGenerator {
    /** */
    public ECCodeGenerator() {
    }

    /** エラーコードを生成する関数 */
    public BinaryString execute(final int ec, final int wc, BinaryString block, final PowerTable power, final ExpressionTable exp) {
        if (power == null || exp == null) {
            throw new IllegalArgumentException("Table is NULL");
        }
        if (block == null) {
            throw new IllegalArgumentException("RSBlock is NULL");
        }
        List<Integer> gp = exp.getGenerateExp(ec);
        List<Integer> gx = new ArrayList<Integer>();
        for (int i = ec; i >= 0; i--) {
            gx.add(new Integer(i));
        }
        GaloisPolynomial g = new GaloisPolynomial(gp, gx, power, GaloisPolynomial.POW_INDICATION);

        List<Integer> fi = block.GetDataByUINTArray();
        List<Integer> fx = new ArrayList<Integer>();
        for (int i = wc - 1; i >= ec; i--) {
            fx.add(new Integer(i));
        }
        GaloisPolynomial f = new GaloisPolynomial(fi, fx, power, GaloisPolynomial.INT_INDICATION);
        return f.operatorModulo(g).getDataByBinaryString();
    }
}

/* */
