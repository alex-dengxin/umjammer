/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * このクラスは CSV のデータを int 形で取得する手段を提供します。
 *
 * @version	新規作成 2002/11/11(Mon) 石戸谷　顕太朗
 *          実装完了 2002/11/12(Tue) 石戸谷　顕太朗
 */
class CSVFileReader implements FileReader {
    /** コンストラクタ */
    public CSVFileReader() {
        maxRow = 0;
        maxCol = 0;
    }

    /** ファイルを {@link FileReader#stringList} に読み込んだ上で、FormatData を呼び出す関数 
     * @throws IOException*/
    public void loadFromFile(String fn) throws IOException {
        try {
            if (data != null) {
                data.clear();
                maxRow = 0;
                maxCol = 0;
            }
            BufferedReader ifs = new BufferedReader(new java.io.FileReader(fn));
            while (ifs.ready()) {
                String str = ifs.readLine(); 
                data.add(convertString(str));
            }
            maxRow = data.size();
            maxCol = data.get(0).size();
            ifs.close();
        } finally {
            release();
        }
    }

    /** データを行単位の配列で返す関数。 */
    public List<Integer> getData(int row) {
        try {
            if (data.isEmpty()) {
                throw new IllegalArgumentException("Data is NULL");
            }
            if (row > maxRow) {
                throw new IllegalArgumentException("index over flow");
            }
            return data.get(row);
        } finally {
            release();
        }
    }
    
    /** データを行、列から特定して int で返す関数 */
    public int getData(int row, int col) {
        try {
            if (data.isEmpty()) {
                throw new IllegalArgumentException("Data is NULL");
            }
            if (row > maxRow || col > data.get(row).size()) {
                throw new IllegalArgumentException("index over flow");
            }
            return data.get(row).get(col).intValue();
        } finally {
            release();
        }
    }

    /** 行の最大数を返す関数 */
    public int getMaxRow() {
        return maxRow;
    }

    /** 列の最大数を返す関数 */
    public int getMaxCol() {
        return maxCol;
    }

    /** 解放を行なう関数 */
    private void release() {
        if (data != null) {
            data.clear();
        }
        maxRow = 0;
        maxCol = 0;
    }

    //  送られてきたカンマ区切りの文字列を、intの配列にして返す関数。
    private List<Integer> convertString(String str) {
        try {
            if (str == null) {
                throw new IllegalArgumentException("str is NULL");
            }
            List<Integer> v = new ArrayList<Integer>();;

            StringTokenizer iss = new StringTokenizer(str, ", \t");
            while (iss.hasMoreTokens()) {
                v.add(new Integer(iss.nextToken()));
            }
            return v;
        } finally {
            release();
        }
    }

    /** データを格納する。 */
    private List<List<Integer>> data;

    /** 最大行数 */
    private int maxRow;

    /** 最大列数 */
    private int maxCol;
}