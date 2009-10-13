/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;

import java.io.IOException;
import java.util.List;


/**
 * このクラスは行、列で構成されたファイルを読み込んで、データの取得
 * サポートするためのものです。
 * 主にテーブルクラスへデータアクセスを提供するインターフェイスとな
 * る、抽象基底クラスです。
 *
 * @version	新規作成 2002/11/11 石戸谷顕太朗
 */
interface FileReader {
    /** 読み込みインターフェイス 
     * @throws IOException*/
    void loadFromFile(String fileName) throws IOException;
    /** データ取得インターフェイス */
    List<Integer> getData(int row);
    /** データ取得インターフェイス */
    int getData(int row, int col);
    /** 最大行の取得 */
    int getMaxRow();
    /** 最大列の取得 */
    int getMaxCol();
}

/* */
