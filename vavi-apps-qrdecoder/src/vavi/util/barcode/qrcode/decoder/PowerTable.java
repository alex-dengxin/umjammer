/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.io.IOException;


/**
 * このクラスは FileReader クラスから受け取ったデータをクライアント
 * から渡された、べき乗表示と整数表示の相互変換を提供するクラスです。
 *
 * @version	新規作成 2002/11/13(Wed) 石戸谷 顕太朗
 *          実装完了 2002/11/14(Thu) 石戸谷 顕太朗
 */
class PowerTable extends Table {

    /** コンストラクタ */
    public PowerTable() {
        file = null;
    }

    /**
     * Stringでファイル名を受け取るイニシャライザ。
     * FileReaderクラスを受け取ってFileを初期化し、ファイルを読み込む。
     * @throws IOException
     */
    public boolean initialize(final String fn, FileReader temp) throws IOException {
        if (temp == null) {
            return false;
        }
        file = temp;
        file.loadFromFile(fn);
        return true;
    }

    /** テーブルから次数をキーに整数を取り出す関数。 */
    public int convertPowerToInt(int p) {
        if (file == null) {
            throw new IllegalStateException("File is NULL");
        }
        p = p % 255;
        return file.getData(p, 0);
    }

    /** テーブルから整数をキーに次数を取り出す関数。 */
    public int convertIntToPower(int i) {
        if (file == null) {
            throw new IllegalStateException("File is NULL");
        }
        if (i > 255) {
            return 0;
        }
        return file.getData(i, 1);
    }
}

/* */
