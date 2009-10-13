/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスはCFileReaderクラスから受け取ったデータをクライアント
 * から渡された、VersionとエラーレベルによってCSymbolを初期化して
 * 返す。
 *
 * @version	新規作成 2002/12/05(Thu) 石戸谷　顕太朗
 *          追加変更 2002/12/11(Wed) 石戸谷　顕太朗
 */
class VersionTable extends Table {

    /** */
    enum ErrorCollectionLevel {
        L,
        M,
        Q,
        H
    }

    /** */
    public VersionTable() {
        file = null;
    }
    
    /** */
    public boolean initialize(final String filename, FileReader fileReader) {
        try {
            if (fileReader == null) {
                return false;
            }
            file = fileReader;
            file.loadFromFile(filename);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /** シンボルを初期化する */
    public final Symbol getPartialSymbol(int version) {
        if (version == 0 && version > 40) {
            throw new IllegalArgumentException("Invalid Version or ECL");
        }
        Symbol symbol = new Symbol();
        symbol.setId((version - 1) * 4);
        symbol.setVersion(version);

        //各情報をファイルから読み込み。
        symbol.setModulesPerSide(file.getData(symbol.getId(), Type.ROW_MODULESPERSIDE.ordinal()));
        symbol.setFunctionModules(file.getData(symbol.getId(), Type.ROW_FUNCTIONMODULES.ordinal()));
        symbol.setVersionModules(file.getData(symbol.getId(), Type.ROW_VERSIONMODULES.ordinal()));
        symbol.setOtherModules(file.getData(symbol.getId(), Type.ROW_OTHERMODULES.ordinal()));
        symbol.setWholeCodeWords(file.getData(symbol.getId(), Type.ROW_WHOLECODEWORDS.ordinal()));
        symbol.setRemainderBits(file.getData(symbol.getId(), Type.ROW_REMAINDERBITS.ordinal()));
        symbol.setAlignmentPatterns(file.getData(symbol.getId(), Type.ROW_ALIGNMENTPATTERNS.ordinal()));
        symbol.setApPos1(file.getData(symbol.getId(), Type.ROW_APPOS1.ordinal()));
        symbol.setApPos2(file.getData(symbol.getId(), Type.ROW_APPOS2.ordinal()));
        symbol.setApPos3(file.getData(symbol.getId(), Type.ROW_APPOS3.ordinal()));
        symbol.setApPos4(file.getData(symbol.getId(), Type.ROW_APPOS4.ordinal()));
        symbol.setApPos5(file.getData(symbol.getId(), Type.ROW_APPOS5.ordinal()));
        symbol.setApPos6(file.getData(symbol.getId(), Type.ROW_APPOS6.ordinal()));
        symbol.setApPos7(file.getData(symbol.getId(), Type.ROW_APPOS7.ordinal()));

        symbol.CalcAPPositions();
        symbol.setPartial(true);
        return symbol;
    }
    
    /** シンボルを初期化する */
    public final Symbol getSymbol(int version, VersionTable.ErrorCollectionLevel ecl) {
        if (version == 0 && version > 40) {
            throw new IllegalArgumentException("Invalid Version or ECL");
        }
        Symbol symble = new Symbol();
        symble.setId(((version - 1) * 4) + ecl.ordinal());
        symble.setVersion(version);
        symble.setErrorCollectionLevel(ecl);

        // 各情報をファイルから読み込み。
        symble.setModulesPerSide(file.getData(symble.getId(), Type.ROW_MODULESPERSIDE.ordinal()));
        symble.setFunctionModules(file.getData(symble.getId(), Type.ROW_FUNCTIONMODULES.ordinal()));
        symble.setVersionModules(file.getData(symble.getId(), Type.ROW_VERSIONMODULES.ordinal()));
        symble.setOtherModules(file.getData(symble.getId(), Type.ROW_OTHERMODULES.ordinal()));
        symble.setWholeCodeWords(file.getData(symble.getId(), Type.ROW_WHOLECODEWORDS.ordinal()));
        symble.setRemainderBits(file.getData(symble.getId(), Type.ROW_REMAINDERBITS.ordinal()));
        symble.setDataCodeWords(file.getData(symble.getId(), Type.ROW_DATACODEWORDS.ordinal()));
        symble.setDataBits(file.getData(symble.getId(), Type.ROW_DATABITS.ordinal()));
        symble.setNumeric(file.getData(symble.getId(), Type.ROW_NUMERIC.ordinal()));
        symble.setAlphabet(file.getData(symble.getId(), Type.ROW_ALPHABET.ordinal()));
        symble.setKanji(file.getData(symble.getId(), Type.ROW_KANJI.ordinal()));
        symble.setBytes(file.getData(symble.getId(), Type.ROW_BYTE.ordinal()));
        symble.setEcCodeWords(file.getData(symble.getId(), Type.ROW_ECCODEWORDS.ordinal()));
        symble.setBlocks(file.getData(symble.getId(), Type.ROW_BLOCKS.ordinal()));
        symble.setRsBlock1EC(file.getData(symble.getId(), Type.ROW_RSBLOCK1.ordinal()));
        symble.setRsBlock1WholeCodes(file.getData(symble.getId(), Type.ROW_RSBLOCK1_WHOLECODES.ordinal()));
        symble.setRsBlock1DataCodeWords(file.getData(symble.getId(), Type.ROW_RSBLOCK1_DATACODEWORDS.ordinal()));
        symble.setRsBlock1EC(file.getData(symble.getId(), Type.ROW_RSBLOCK1_EC.ordinal()));
        symble.setRsBlock2(file.getData(symble.getId(), Type.ROW_RSBLOCK2.ordinal()));
        symble.setRsBlock2WholeCodes(file.getData(symble.getId(), Type.ROW_RSBLOCK2_WHOLECODES.ordinal()));
        symble.setRsBlock2DataCodeWords(file.getData(symble.getId(), Type.ROW_RSBLOCK2_DATACODEWORDS.ordinal()));
        symble.setRsBlock2EC(file.getData(symble.getId(), Type.ROW_RSBLOCK2_EC.ordinal()));
        symble.setAlignmentPatterns(file.getData(symble.getId(), Type.ROW_ALIGNMENTPATTERNS.ordinal()));
        symble.setApPos1(file.getData(symble.getId(), Type.ROW_APPOS1.ordinal()));
        symble.setApPos2(file.getData(symble.getId(), Type.ROW_APPOS2.ordinal()));
        symble.setApPos3(file.getData(symble.getId(), Type.ROW_APPOS3.ordinal()));
        symble.setApPos4(file.getData(symble.getId(), Type.ROW_APPOS4.ordinal()));
        symble.setApPos5(file.getData(symble.getId(), Type.ROW_APPOS5.ordinal()));
        symble.setApPos6(file.getData(symble.getId(), Type.ROW_APPOS6.ordinal()));
        symble.setApPos7(file.getData(symble.getId(), Type.ROW_APPOS7.ordinal()));
        symble.setCharCountNumeric(file.getData(symble.getId(), Type.ROW_CC_NUMERIC.ordinal()));
        symble.setCharCountAlpha(file.getData(symble.getId(), Type.ROW_CC_ALPHA.ordinal()));
        symble.setCharCountAscii(file.getData(symble.getId(), Type.ROW_CC_ASCII.ordinal()));
        symble.setCharCountKanji(file.getData(symble.getId(), Type.ROW_CC_KANJI.ordinal()));


        symble.CalcAPPositions();
        symble.setPartial(false); // @@@
        return symble;
    }        

    /** 位置合わせパターンの位置を初期化をする関数 */
    public void calcAlignmentPattern() {
        // @@@
    }

    /** version.csvからデータを取得する為の列情報 */
    enum Type {
        ROW_ID,
        ROW_MODULESPERSIDE,
        ROW_FUNCTIONMODULES,
        ROW_VERSIONMODULES,
        ROW_OTHERMODULES,
        ROW_WHOLECODEWORDS,
        ROW_REMAINDERBITS,
        ROW_DATACODEWORDS,
        ROW_DATABITS,
        ROW_NUMERIC,
        ROW_ALPHABET,
        ROW_BYTE,
        ROW_KANJI,
        ROW_ECCODEWORDS,
        ROW_BLOCKS,
        ROW_RSBLOCK1,
        ROW_RSBLOCK1_WHOLECODES,
        ROW_RSBLOCK1_DATACODEWORDS,
        ROW_RSBLOCK1_EC,
        ROW_RSBLOCK2,
        ROW_RSBLOCK2_WHOLECODES,
        ROW_RSBLOCK2_DATACODEWORDS,
        ROW_RSBLOCK2_EC,
        ROW_ALIGNMENTPATTERNS,
        ROW_APPOS1,
        ROW_APPOS2,
        ROW_APPOS3,
        ROW_APPOS4,
        ROW_APPOS5,
        ROW_APPOS6,
        ROW_APPOS7,
        ROW_CC_NUMERIC,
        ROW_CC_ALPHA,
        ROW_CC_ASCII,
        ROW_CC_KANJI
    }
}

/* */
