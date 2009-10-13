/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.util.ArrayList;
import java.util.List;


/**
 * このクラスは、QR コードのシンボル情報を全て持つクラスです。
 * このクラスは {@link vavi.util.barcode.qrcode.decoder.VersionTable} によって初期化されます。
 * {@link vavi.util.barcode.qrcode.decoder.VersionTable} を介すこと以外で {@link Symbol}
 * クラスを使用可能な状態にすることはできません。
 * 必ず {@link vavi.util.barcode.qrcode.decoder.VersionTable#formatSymbol()}
 * を使って初期化してください。
 *
 * @version	新規作成 2002/12/08(Sun) 石戸谷　顕太朗
 *          追加変更 2002/12/11(Wed) 石戸谷　顕太朗
 */
class Symbol {
    /** */
    Symbol() {
        this.id = 0;
        this.version = 0;
        this.errorCollectionLevel = VersionTable.ErrorCollectionLevel.L;
        this.modulesPerSide = 0;
        this.functionModules	= 0;
        this.versionModules = 0;
        this.otherModules = 0;
        this.wholeCodeWords = 0;
        this.remainderBits = 0;
        this.dataCodeWords = 0;
        this.dataBits = 0;
        this.numeric = 0;
        this.alphabet = 0;
        this.bytes = 0;
        this.kanji = 0;
        this.ecCodeWords = 0;
        this.blocks = 0;
        this.rsBlock1 = 0;
        this.rsBlock1WholeCodes = 0;
        this.rsBlock1DataCodeWords = 0;
        this.rsBlock1EC = 0;
        this.rsBlock2 = 0;
        this.rsBlock2WholeCodes = 0;
        this.rsBlock2DataCodeWords = 0;
        this.rsBlock2EC = 0;
        this.alignmentPatterns = 0;
        this.apPos1 = 0;
        this.apPos2 = 0;
        this.apPos3 = 0;
        this.apPos4 = 0;
        this.apPos5 = 0;
        this.apPos6 = 0;
        this.apPos7 = 0;
        this.charCountNumeric = 0;
        this.charCountAlpha = 0;
        this.charCountAscii = 0;
        this.charCountKanji = 0;
        this.partial = false;
        apPositions.clear();
    }
    
    /** 代入演算子 */
    public final Symbol operatorLet(final Symbol right) {
        if (right == this) {
            return this;
        }
        if (right == null) {
            throw new IllegalArgumentException("Data is not Initialized");
        }

        id = right.id;
        version	= right.version;
        modulesPerSide = right.modulesPerSide;
        functionModules = right.functionModules;
        versionModules = right.versionModules;
        otherModules = right.otherModules;
        wholeCodeWords = right.wholeCodeWords;
        remainderBits = right.remainderBits;
        alignmentPatterns = right.alignmentPatterns;
        apPos1 = right.apPos1;
        apPos2 = right.apPos2;
        apPos3 = right.apPos3;
        apPos4 = right.apPos4;
        apPos5 = right.apPos5;
        apPos6 = right.apPos6;
        apPos7 = right.apPos7;
        charCountNumeric = right.charCountNumeric;
        charCountAlpha = right.charCountAlpha;
        charCountAscii = right.charCountAscii;
        charCountKanji = right.charCountKanji;
        apPositions.addAll(right.apPositions);
        if (!right.isPartial()) {
            errorCollectionLevel = right.errorCollectionLevel;
            dataCodeWords	= right.dataCodeWords;
            dataBits = right.dataBits;
            numeric = right.numeric;
            alphabet = right.alphabet;
            bytes = right.bytes;
            kanji = right.kanji;
            ecCodeWords	= right.ecCodeWords;
            blocks = right.blocks;
            rsBlock1 = right.rsBlock1;
            rsBlock1WholeCodes = right.rsBlock1WholeCodes;
            rsBlock1DataCodeWords = right.rsBlock1DataCodeWords;
            rsBlock1EC = right.rsBlock1EC;
            rsBlock2 = right.rsBlock2;
            rsBlock2WholeCodes	= right.rsBlock2WholeCodes;
            rsBlock2DataCodeWords = right.rsBlock2DataCodeWords;
            rsBlock2EC = right.rsBlock2EC;
        }
        partial = right.partial;
        return this;
    }

    /** id の取得メソッド */
    public final int getId() {
        return id;
    }

    /** versionの取得メソッド */
    public final int getVersion() {
        return version;
    }

    /** エラー訂正レベルの取得メソッド */
    public final VersionTable.ErrorCollectionLevel getErrorCollectionLevel() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return errorCollectionLevel;
    }

    /** 一辺のモジュール数の取得メソッド */
    public final int getModulesPerSide() {
        return modulesPerSide;
    }

    /** 機能モジュール数の取得メソッド */
    public final int getFunctionModules() {
        return functionModules;
    }

    /** 型番モジュール数の取得メソッド */
    public final int getVersionModules() {
        return versionModules;
    }

    /** その他のモジュール数の取得メソッド */
    public final int getOtherModules() {
        return otherModules;
    }

    /** 総コード語数を取得するメソッド */
    public final int getWholeCodeWords() {
        return wholeCodeWords;
    }

    /** 剰余コードビット数の取得メソッド */
    public final int getRemainderBits() {
        return remainderBits;
    }

    /** データコード語の取得メソッド */
    public final int getDataCodeWords() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return dataCodeWords;
    }

    /** データビット数の取得メソッド */
    public final int getDataBits() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return dataBits;
    }

    /** 数字の格納可能数の取得メソッド */
    public final int getNumeric() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return numeric;
    }

    /** 英語の格納可能数の取得メソッド */
    public final int getAlphabet() {
        return alphabet;
    }

    /** 8 ビットバイトの格納可能数種得メソッド */
    public final int getByte() {
        return bytes;
    }

    /** 漢字の格納可能数種得メソッド */
    public final int getKanji() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return kanji;
    }

    /** エラー訂正語の数を返す関数 */
    public final int getECCodeWords() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return ecCodeWords;
    }

    /** RSブロックの種類の数を返す関数 */
    public final int getBlocks() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return blocks;
    }

    /** RSブロック１の数を返す関数 */
    public final int getRsBlock1() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock1;
        
    }

    /** RSブロック１の総コード数を返す関数 */
    public final int getRSBlock1WholeCodes() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock1WholeCodes;
    }

    /** RSブロックのデータ語数を取得する関数 */
    public final int getRSBlock1DataCodeWords() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock1DataCodeWords;
    }

    /** RS1ブロックの誤り訂正数を取得する関数 */
    public final int getRsBlock1EC() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock1EC;
    }

    /** RSブロック2の数を返す関数 */
    public final int getRsBlock2() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock2;
    }

    /** RSブロック2の総コード数を返す関数 */
    public final int getRsBlock2WholeCodes() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock2WholeCodes;
    }

    /** RSブロック2のデータ語数を取得する関数 */
    public final int getRsBlock2DataCodeWords() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock2DataCodeWords;
    }

    /** RSブロック2の誤り訂正数を取得する関数 */
    public final int getRsBlock2EC() {
        if (partial) {
            throw new IllegalArgumentException("partial mode");
        }
        return rsBlock2EC;
    }

    /** 数字モードの文字数指示子のビット長を返す関数。 */
    public final int getCharCountNumeric() {
        return charCountNumeric;
    }

    /** 英数字モードの文字数指示子のビット長を返す関数。 */
    public final int getCharCountAlpha() {
        return charCountAlpha;
    }

    /** 8ビットバイトモードの文字数指示子のビット長を返す関数。 */
    public final int getCharCountAscii() {
        return charCountAscii;
    }

    /** 漢字モードの文字数指示子のビット長を返す関数 */
    public final int getCharCountKanji() {
        return charCountKanji;
    }

    /** 位置あわせパターンの二次元座標リストを返すメソッド */
    public final List<Point> getApPositions() {
        return apPositions;
    }

    /** シンボルが部分構成であるかどうかを返す */
    public final boolean isPartial() {
        return partial;
    }

    /**
     * @param partial The partial to set.
     */
    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    /** */
    private boolean partial;

    /** VersionTableからは直接メンバを触れる。 */
    private VersionTable versionTable;

    /** APPositionsを初期化する関数。 */
    void CalcAPPositions() {
        if (version == 0) {
            throw new IllegalArgumentException("Symbol wasnt initialized");
        }
        List<Integer> posx = new ArrayList<Integer>();
        List<Integer> posy = new ArrayList<Integer>();
        posx.add(new Integer(apPos1));
        posx.add(new Integer(apPos2));
        posx.add(new Integer(apPos3));
        posx.add(new Integer(apPos4));
        posx.add(new Integer(apPos5));
        posx.add(new Integer(apPos6));
        posx.add(new Integer(apPos7));
        posy.addAll(posx);
        for (Integer y : posy) {
            for (Integer x : posx) {
                // 右上、左上、右下の座標はスキップ。
                if ((x != posx.get(0) && y != posy.get(0)) &&
                        x != posx.get(posx.size() - 1) && y != posy.get(0) &&
                        x != posx.get(0) && y != posy.get(posy.size() - 1) && y.intValue() !=  0 && x.intValue() != 0) {
                    apPositions.add(new Point(x.intValue(), y.intValue()));
                }
            }
        }
    }

    /**
     * シンボルを決定するのに必要なデータ。
     * VersionTable からデータを引く為の一意な ID
     */
    private int id;

    /** シンボルの型番 */
    private int version;

    /** シンボルのエラー訂正レベル */
    private VersionTable.ErrorCollectionLevel errorCollectionLevel;
    
    /**
     * 型番にあったシンボル情報。
     * 一辺のモジュール数
     */
    private int modulesPerSide;
    /** 機能モジュール数 */
    private int functionModules;
    /** 型番モジュール数 */
    private int versionModules;
    /** その他のモジュール数 */
    private int otherModules;
    /** 総コード語数 */
    private int wholeCodeWords;
    /** 剰余ビット数 */
    private int remainderBits;
    /** データコード語の数 */
    private int dataCodeWords;
    /** データコード語のビット数 */
    private int dataBits;
    /** 数字の格納可能数 */
    private int numeric;
    /** アルファベットの格納可能数 */
    private int alphabet;
    /** 8 ビットバイトの格納可能数 */
    private int bytes;
    /** 漢字の格納可能数 */
    private int kanji;
    /** エラー訂正語の数 */
    private int ecCodeWords;
    /** RSブロックの種類の数 */
    private int blocks;
    /** RSブロック1型のブロックの数 */
    private int rsBlock1;
    /** RSブロック1型に格納される総コード語数 */
    private int rsBlock1WholeCodes;
    /** RSブロック1型に格納されるデータコード語数 */
    private int rsBlock1DataCodeWords;
    /** RSブロック1型に誤り訂正数 */
    private int rsBlock1EC;
    /** RSブロック2型のブロック数 */
    private int rsBlock2;
    /** RSブロック2型に格納される総コード語数 */
    private int rsBlock2WholeCodes;
    /** RSブロック2型に格納されるデータコード */
    private int rsBlock2DataCodeWords;
    /** RSブロック2型の誤り訂正数 */
    private int rsBlock2EC;
    /** 位置合わせパターンの数 */
    private int alignmentPatterns;
    /** 位置合わせパターンの行列座標1 */
    private int apPos1;
    /** 位置合わせパターンの行列座標2 */
    private int apPos2;
    /** 位置合わせパターンの行列座標3 */
    private int apPos3;
    /** 位置合わせパターンの行列座標4 */
    private int apPos4;
    /** 位置合わせパターンの行列座標5 */
    private int apPos5;
    /** 位置合わせパターンの行列座標6 */
    private int apPos6;
    /** 位置合わせパターンの行列座標7 */
    private int apPos7;
    /** 数字モードのときの文字数指定子のビット数 */
    private int charCountNumeric;
    /** 英数モードのときの文字数指定子のビット数 */
    private int charCountAlpha;
    /** バイトモードのときの文字数指定子のビット数 */
    private int charCountAscii;
    /** 漢字モードのときの文字数指定子のビット数 */
    private int charCountKanji;
    /** 位置合わせパターンの二次元座標 */
    private List<Point> apPositions;

    /**
     * @param alignmentPatterns The alignmentPatterns to set.
     */
    public void setAlignmentPatterns(int alignmentPatterns) {
        this.alignmentPatterns = alignmentPatterns;
    }

    /**
     * @param alphabet The alphabet to set.
     */
    public void setAlphabet(int alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * @param pos1 The aPPos1 to set.
     */
    public void setApPos1(int pos1) {
        this.apPos1 = pos1;
    }

    /**
     * @param pos2 The aPPos2 to set.
     */
    public void setApPos2(int pos2) {
        this.apPos2 = pos2;
    }

    /**
     * @param pos3 The aPPos3 to set.
     */
    public void setApPos3(int pos3) {
        this.apPos3 = pos3;
    }

    /**
     * @param pos4 The aPPos4 to set.
     */
    public void setApPos4(int pos4) {
        this.apPos4 = pos4;
    }

    /**
     * @param pos5 The aPPos5 to set.
     */
    public void setApPos5(int pos5) {
        this.apPos5 = pos5;
    }

    /**
     * @param pos6 The aPPos6 to set.
     */
    public void setApPos6(int pos6) {
        this.apPos6 = pos6;
    }

    /**
     * @param pos7 The aPPos7 to set.
     */
    public void setApPos7(int pos7) {
        this.apPos7 = pos7;
    }

    /**
     * @param apPositions The apPositions to set.
     */
    public void setApPositions(List<Point> apPositions) {
        this.apPositions = apPositions;
    }

    /**
     * @param bytes The bytes to set.
     */
    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    /**
     * @param alpha The cC_Alpha to set.
     */
    public void setCharCountAlpha(int alpha) {
        this.charCountAlpha = alpha;
    }

    /**
     * @param ascii The cC_Ascii to set.
     */
    public void setCharCountAscii(int ascii) {
        this.charCountAscii = ascii;
    }

    /**
     * @param numeric The cC_Numeric to set.
     */
    public void setCharCountNumeric(int numeric) {
        this.charCountNumeric = numeric;
    }

    /**
     * @param charCountKanji The charCountKanji to set.
     */
    public void setCharCountKanji(int charCountKanji) {
        this.charCountKanji = charCountKanji;
    }

    /**
     * @param dataBits The dataBits to set.
     */
    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    /**
     * @param dataCodeWords The dataCodeWords to set.
     */
    public void setDataCodeWords(int dataCodeWords) {
        this.dataCodeWords = dataCodeWords;
    }

    /**
     * @param ecCodeWords The ecCodeWords to set.
     */
    public void setEcCodeWords(int ecCodeWords) {
        this.ecCodeWords = ecCodeWords;
    }

    /**
     * @param errorCollectionLevel The errorCollectionLevel to set.
     */
    public void setErrorCollectionLevel(VersionTable.ErrorCollectionLevel errorCollectionLevel) {
        this.errorCollectionLevel = errorCollectionLevel;
    }

    /**
     * @param functionModules The functionModules to set.
     */
    public void setFunctionModules(int functionModules) {
        this.functionModules = functionModules;
    }

    /**
     * @param id The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param kanji The kanji to set.
     */
    public void setKanji(int kanji) {
        this.kanji = kanji;
    }

    /**
     * @param modulesPerSide The modulesPerSide to set.
     */
    public void setModulesPerSide(int modulesPerSide) {
        this.modulesPerSide = modulesPerSide;
    }

    /**
     * @param numeric The numeric to set.
     */
    public void setNumeric(int numeric) {
        this.numeric = numeric;
    }

    /**
     * @param otherModules The otherModules to set.
     */
    public void setOtherModules(int otherModules) {
        this.otherModules = otherModules;
    }

    /**
     * @param remainderBits The remainderBits to set.
     */
    public void setRemainderBits(int remainderBits) {
        this.remainderBits = remainderBits;
    }

    /**
     * @param blocks The blocks to set.
     */
    public void setBlocks(int blocks) {
        this.blocks = blocks;
    }

    /**
     * @param block1_EC The rSBlock1_EC to set.
     */
    public void setRsBlock1EC(int block1_EC) {
        this.rsBlock1EC = block1_EC;
    }

    /**
     * @param rsBlock1DataCodeWords The rsBlock1DataCodeWords to set.
     */
    public void setRsBlock1DataCodeWords(int rsBlock1DataCodeWords) {
        this.rsBlock1DataCodeWords = rsBlock1DataCodeWords;
    }

    /**
     * @param rsBlock1WholeCodes The rsBlock1WholeCodes to set.
     */
    public void setRsBlock1WholeCodes(int rsBlock1WholeCodes) {
        this.rsBlock1WholeCodes = rsBlock1WholeCodes;
    }

    /**
     * @param block2 The rSBlock2 to set.
     */
    public void setRsBlock2(int block2) {
        this.rsBlock2 = block2;
    }

    /**
     * @param block2DataCodeWords The rSBlock2_DataCodeWords to set.
     */
    public void setRsBlock2DataCodeWords(int block2DataCodeWords) {
        this.rsBlock2DataCodeWords = block2DataCodeWords;
    }

    /**
     * @param block2EC The rSBlock2_EC to set.
     */
    public void setRsBlock2EC(int block2EC) {
        this.rsBlock2EC = block2EC;
    }

    /**
     * @param block2WholeCodes The rSBlock2_WholeCodes to set.
     */
    public void setRsBlock2WholeCodes(int block2WholeCodes) {
        this.rsBlock2WholeCodes = block2WholeCodes;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @param versionModules The versionModules to set.
     */
    public void setVersionModules(int versionModules) {
        this.versionModules = versionModules;
    }

    /**
     * @param versionTable The versionTable to set.
     */
    public void setVersionTable(VersionTable versionTable) {
        this.versionTable = versionTable;
    }

    /**
     * @param wholeCodeWords The wholeCodeWords to set.
     */
    public void setWholeCodeWords(int wholeCodeWords) {
        this.wholeCodeWords = wholeCodeWords;
    }
}

/* */
