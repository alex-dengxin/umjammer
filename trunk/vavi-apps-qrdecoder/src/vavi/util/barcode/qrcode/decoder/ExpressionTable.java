/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.ArrayList;
import java.util.List;


/**
 * このクラスはCFileReaderクラスから受け取ったデータをクライアント
 * から渡された、VersionとエラーレベルによってCGaloisPlynomialとして
 * 返す。
 *
 * @version	新規作成 2002/12/14(Sat) 石戸谷　顕太朗
 */
class ExpressionTable extends Table {
    /** */
    public ExpressionTable() {
        file = null;
    }
    
    /** 初期化関数 */
    public boolean initialize(final String FileName, FileReader temp) {
        try {
            if (temp == null) {
                return false;
            }
            file = temp;
            file.loadFromFile(FileName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** データからECCodeWordに対する生成多項式の項の係数の次数を配列にして返す関数。 */
    List<Integer> getGenerateExp(final int ECCodeWords) {
        int index = 0;
        boolean isFound = false;
        for (int i = 0; i < file.getMaxRow(); i++) {
            if (file.getData(i, 0) == ECCodeWords) {
                index = i;
                isFound = true;
                break;
            }
        }

        if (!isFound) {
            throw new IllegalArgumentException("ECCount is not Exist");
        }

        List<Integer> gp = new ArrayList<Integer>();
//	List<Integer> gx;
        for (int i = 1; i <= ECCodeWords + 1; i++) {
            gp.add(new Integer(file.getData(index, i)));
        }
        return gp;
    }
}

/* */
