/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */ 

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスはガロア体 GF(2) 上の単項式を表現する。
 * 単項式と単項式の加算、乗算をサポートし、ソートの為の比較演算子を
 * サポートする。
 * 単項式<code>a ^ A * x ^ X</code> を表現する {@link GaloisMonomia} lは、
 * {@link #integer}<code> = a ^ A</code>の整数表示
 * {@link #power}<code> = a ^ A</code>(べき乗表示)
 * {@link #valPower}<code> = X</code>(xの次数)
 * をデータとして持つ。
 *
 * @version	新規作成 2002/11/14(Thu) 石戸谷　顕太朗
 */
class GaloisMonomial implements Comparable<GaloisMonomial> {
    
    /**
     * デフォルトコンストラクタ。
     * private 属性なので、引数つきのコンストラクタでなければクラス外からはインスタ
     * ンスを作成できない。
     */
    private GaloisMonomial() {
        integer = 0;
        power = 0;
        valPower = 0;
    }
    
    /** 多項式クラス内からは、呼び出しを許可する。 */
    public GaloisMonomial(final GaloisMonomial source) {
        integer = source.getInteger();
        power = source.getPower();
        valPower = source.getValPower();
    }

    /** すべてのデータを引数でとって初期化するコンストラクタ。 */
    public GaloisMonomial(int integer, int power, int valPower){
        if (integer > 255) {
            throw new IllegalArgumentException("Integer > 255");
        }
        this.integer = integer;
        this.power = power % 255;
        this.valPower = valPower % 255;
    }
    
    /** 代入演算子 */
    public final GaloisMonomial operatorLet(final GaloisMonomial right){
        integer = right.getInteger();
        power = right.getPower();
        valPower = right.getValPower();
        return this;
    }

    /** 項の次数 {@link #valPower} で比較する */
    public boolean operatorLessThan(final GaloisMonomial right){
        if (valPower < right.getValPower()) {
            return true;
        }
        return false;
    }
    
    /** 項の次数 {@link #valPower} で比較する */
    public boolean operatorGreaterThan(final GaloisMonomial right){
        if (valPower > right.getValPower()) {
            return true;
        }
        return false;
    }

    /** 項の次数 {@link #ValPower} で比較する */
    public boolean operatorEqual(final GaloisMonomial right){
        if (valPower == right.getValPower()) {
            return true;
        }
        return false;
    }
    
    /** GF(2) 上の加算をします。 */
    public GaloisMonomial plus(final GaloisMonomial right, final PowerTable table) {
        // どちらかの項が 0 である場合は、0 でない方の項を返す。
        if (this == null) {
            return right;
        }
        if (right == null) {
            return this;
        }
        
        if (valPower != right.getValPower()) {
            throw new IllegalArgumentException("Error : Cant plus diffarent valpower@CGaloisMonomial::Plus");
        }
        int rinteger = integer ^ right.getInteger();
        int rpower = table.convertIntToPower(rinteger);
        GaloisMonomial ret = new GaloisMonomial(rinteger, rpower, valPower);
        
        if (rinteger == 0) {
            ret = null;
        }
        return ret;
    }
    
    /** GF(2) 上の乗算をします。 */
    public GaloisMonomial multiply(final GaloisMonomial right, final PowerTable table) {
        // どちらかの項が 0 である場合は、0 の項を返す。
        if (this == null || right == null) {
            return new GaloisMonomial();
        }
        int resultPower = (power + right.getPower()) % 255;
        int resultInteger = table.convertPowerToInt(resultPower);
        int resultValPower = (valPower + right.getValPower()) % 255;
        return new GaloisMonomial(resultInteger, resultPower, resultValPower);
    }
    
    /** 係数の整数表示を返す */
    public int getInteger() {
        return integer;
    }

    /** 係数のべき乗表示を返す */
    public int getPower() {
        return power;
    }

    /** 項の次数を返す。 */
    public int getValPower() {
        return valPower;
    }

    /** 係数の整数表示 */
    private int integer;

    /** 係数のべき乗表示 */
    private int power;

    /** 項の次数 */
    private int valPower;

    /** */
    public int compareTo(GaloisMonomial target) {
        return valPower - target.getValPower(); // TODO check
    }
}

/* */
