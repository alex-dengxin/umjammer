/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * このクラスはガロア体 GF(2) 上の多項式を表現する。
 * 多項式と多項式の加算、乗算をサポートする。
 *
 * @version	新規作成 2002/11/14(Thu) 石戸谷　顕太朗
 *          追加変更 2002/12/01(Sun) 石戸谷　顕太朗
 *          追加変更 2002/12/15(Sun) 石戸谷　顕太朗
 *          追加変更 2002/02/22(Sun) 石戸谷　顕太朗
 */
class GaloisPolynomial {
    final static int POW_INDICATION = 0;
    final static int INT_INDICATION = 1;

    /** コンストラクタ。必ず初期化されなければならないので private 属性。 */
    private GaloisPolynomial() {
        table = null;
    }

    /** コンストラクタ */
    public GaloisPolynomial(final GaloisPolynomial source) {
        if (!polynomial.isEmpty()) {
            polynomial.clear();
        }
        List<GaloisMonomial> v;
        v = source.polynomial;
        polynomial.addAll(v);
        table = source.table;
    }

    /** 単項式を受け取って初期化する関数 */
    public GaloisPolynomial(final GaloisMonomial monomial, final PowerTable table) {
        if (monomial == null) {
            throw new IllegalArgumentException("m is null");
        }
        if (table == null) {
            throw new IllegalArgumentException("table is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        this.polynomial.add(new GaloisMonomial(monomial));
        this.table = table;
    }

    /** 単項式の配列を受け取って初期化する */
    public GaloisPolynomial(final List<GaloisMonomial> v, final PowerTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table is null");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        for (GaloisMonomial p : v) {
            if (p == null) {
                throw new IllegalArgumentException("This Monomiall is not Initialized");
            }
            this.polynomial.add(p);
        }
        this.table = table;
        Collections.sort(this.polynomial);
    }

    /** 係数の整数表示またはべき乗表示の配列と、項の次数を受け取って初期化するコンストラクタ。 */
    public GaloisPolynomial(final List<Integer> i, final List<Integer> v, final PowerTable temp, final int flag /* = POW_INDICATION */) {
        if (temp == null) {
            throw new IllegalArgumentException("table is null");
        }
        if (i.size() != v.size()) {
            throw new IllegalArgumentException("not match size");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        this.table = temp;
        Iterator<Integer> pv = v.iterator();

        if (flag == INT_INDICATION) {
            for (Integer pi : i) {
                this.polynomial.add(new GaloisMonomial(pi.intValue(), this.table.convertIntToPower(pi.intValue()), pv.next().intValue()));
            }
        } else if (flag == POW_INDICATION) {
            for (Integer pi : i) {
                this.polynomial.add(new GaloisMonomial(this.table.convertPowerToInt(pi.intValue()), pi.intValue(), pv.next().intValue()));
            }
        }
        Collections.sort(this.polynomial);
    }

    /**
     * {@link BinaryString} で与えられた code を多項式にして初期化するコンストラクタ。
     * @param code 整数表示であると仮定する
     */
    public GaloisPolynomial(final BinaryString code, final PowerTable table) {
        if (table == null) {
            throw new IllegalArgumentException("table is null");
        }
        if ((code.GetLength() % 8) != 0) {
            throw new IllegalArgumentException("Code is not 8bit Blocks");
        }
        if (!this.polynomial.isEmpty()) {
            this.polynomial.clear();
        }
        this.table = table;
        int max = code.GetLength() / 8;
        for (int i = 0; i < max; i++) {
            this.polynomial.add(new GaloisMonomial(code.getSubByte(i), this.table.convertIntToPower(code.getSubByte(i)), i));
        }
        Collections.sort(this.polynomial);
    }
    
    /** 代入演算子 */
    public final GaloisPolynomial operatorLet(final GaloisPolynomial right){
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }
        if (!polynomial.isEmpty()) {
            polynomial.clear();
        }
        List<GaloisMonomial> v;
        v = right.polynomial;
        polynomial.addAll(v);
        table = right.table;
        Collections.sort(polynomial);
        return this;
    }

    /** 加算代入演算子 */
    public final GaloisPolynomial operatorPlusLet(final GaloisPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }
        List<GaloisMonomial> v = plus(polynomial, right.polynomial);
        polynomial.clear();
        polynomial.addAll(v);
        return this;
    }

    /** 乗算代入演算子 */
    public final GaloisPolynomial operatorMultiplyLet(final GaloisPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }
        List<GaloisMonomial> v = multiply(polynomial, right.polynomial);
        polynomial.clear();
        polynomial.addAll(v);
        return this;
    }

    /** 剰余代入演算子 */
    public final GaloisPolynomial operatorModuloLet(final GaloisPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }
        GaloisPolynomial v = remainder(this, right);
        polynomial.clear();
        polynomial.addAll(v.polynomial);
        return this;
    }

    /** 加算演算子 */
    public GaloisPolynomial operatorPlus(final GaloisPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }

        return new GaloisPolynomial(plus(polynomial, right.polynomial), table);
    }

    /** 乗算演算子 */
    public GaloisPolynomial operatorMultiply(final GaloisPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }

        return new GaloisPolynomial(multiply(polynomial, right.polynomial), table);
    }

    /** 乗算演算子 */
    public GaloisPolynomial operatorModulo(final GaloisPolynomial right) {
        if (right == null) {
            throw new IllegalArgumentException("right is null");
        }

        return remainder(this, right);
    }
    
    /** index で表される位置の項を返す */
    public GaloisMonomial get(final int index) {
        if (index > polynomial.size()) {
            throw new IllegalArgumentException("invalid index");
        }
        return polynomial.get(index);
    }

    /** 格納されている多項式を整数表示の多項式の文字列にして返す */
    public String toStringIntIndication() {
        StringWriter buffer = new StringWriter();
        int count = 0;
        for (GaloisMonomial monomial : polynomial) {
            if (monomial.getInteger() != 1) {
                buffer.write(monomial.getInteger());
            } else if (monomial.getValPower() == 0) {
                buffer.write(monomial.getInteger());
            }

            if (monomial.getValPower() == 1) {
                buffer.write("X");
            } else if (monomial.getValPower() != 0) {
                buffer.write("X^");
                buffer.write(monomial.getValPower());
            }
            count++;
            if (count != polynomial.size() - 1) {
                buffer.write(" + ");
            }
        }
        return buffer.toString();
    }

    /** 格納されている多項式をべき表示の多項式の文字列にして返す */
    public String toStringPowIndication() {
        StringWriter buffer = new StringWriter();

        int count = 0;
        for (GaloisMonomial monomial : polynomial) {
            if (monomial.getPower() == 1) {
                buffer.write("A");
            } else if (monomial.getPower() != 0) {
                buffer.write("A^");
                buffer.write(monomial.getPower());
            }
            if (monomial.getValPower() == 1) {
                buffer.write("X");
            } else if (monomial.getValPower() != 0) {
                buffer.write("X^");
                buffer.write(monomial.getValPower());
            }
            count++;
            if (count < polynomial.size() - 1) {
                buffer.write(" + ");
            }
        }
        return buffer.toString();
    }

    /** 格納されている多項式を係数表示のunsigned intの配列にして返す。 */
    public List<Integer> getDataByIntArray() {
        List<Integer> v = new ArrayList<Integer>();
        for (GaloisMonomial monomial : polynomial) {
            v.add(new Integer(monomial.getInteger()));
        }
        return v;
    }

    /** 格納されている多項式を係数表示のBinaryStringにして返す。 */
    public BinaryString getDataByBinaryString() {
        BinaryString bs = new BinaryString();
        for (GaloisMonomial monomial : polynomial) {
            bs.operatorPlusLet(new BinaryString(monomial.getInteger()));
        }
        return bs;
    }

    /**
     * 内部で演算を行なうメソッド
     * 実際に加算を行なう関数
     */
    private List<GaloisMonomial> plus(final List<GaloisMonomial> monomialA, final List<GaloisMonomial> monomialB) {
        if (monomialA.isEmpty() || monomialB.isEmpty()) {
            throw new IllegalArgumentException("Error : Invalid Paramiter @ CGaloisPolynomial::Multi");
        }

        List<GaloisMonomial> result = new ArrayList<GaloisMonomial>();

        int max;
        // 二つの多項式中、xの最高次数を取得
        if (monomialA.get(0).getValPower() > monomialB.get(0).getValPower()) {
            max = monomialA.get(0).getValPower();
        } else {
            max = monomialB.get(0).getValPower();
        }

        // monomial1 の中に存在し monomial2 の中に存在しない項、その逆の場合答えに入れる。
        // 両方に存在する項の場合、足してから答えに入れる。
        for (int i = 0; i <= max; i++) {
            int n = max - i;
            GaloisMonomial monomialA2 = monomialA.get(new GaloisMonomial(1, 0, n).getInteger());
            GaloisMonomial monomialB2 = monomialB.get(new GaloisMonomial(1, 0, n).getInteger());

            if (monomialA2 != monomialA.get(monomialA.size() - 1) && monomialB2 == monomialB.get(monomialB.size() - 1)) {
                result.add(monomialA2);
            } else if (monomialB2 != monomialB.get(monomialB.size() - 1) && monomialA2 == monomialA.get(monomialA.size() - 1)) {
                result.add(monomialB2);
            } else if (monomialA2 != monomialA.get(monomialA.size() - 1) && monomialB2 != monomialB.get(monomialB.size() - 1)) {
                if (monomialA2.plus(monomialB2, table) != null) {
                    result.add((monomialA2).plus(monomialB2, table));
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    /** 実際に乗算を行なう関数 */
    private List<GaloisMonomial> multiply(final List<GaloisMonomial> a, final List<GaloisMonomial> b) {
        if (a.isEmpty() || b.isEmpty()) {
            throw new IllegalArgumentException("Invalid Paramiter");
        }
        List<GaloisMonomial> ret = new ArrayList<GaloisMonomial>();
        List<GaloisMonomial> temp = new ArrayList<GaloisMonomial>();
        GaloisMonomial m;
        GaloisMonomial t;

        // A のそれぞれの項にBの項を掛ける
        for (GaloisMonomial pb : b) {
            for (GaloisMonomial pa : a) {
                temp.add(pa.multiply(pb, table));
            }
        }
        Collections.sort(temp);
        Iterator<GaloisMonomial> ptemp = temp.iterator();

        // 同次数の項をリストアップし順に足して答えにする。
        while (ptemp.hasNext()) {
            int num = temp.indexOf(ptemp);
            if (num != 0) {
                m = ptemp.next();
                if (num == 1) {
                    ret.add(m);
                } else {
                    for (int i = 0; i < num - 1; i++) {
                        if ((t = m.plus(ptemp.next(), table)) != null) {
                            m = t;
                        }
                    }
                    ret.add(m);
                }
            }
        }
        Collections.sort(ret);
        return ret;
    }

    /**
     * 実際に剰余を求めます。
     * @param i 通報多項式
     * @param g が生成多項式 (注：逆にすると動きません)
     */
    private GaloisPolynomial remainder(final GaloisPolynomial i, final GaloisPolynomial g) {
        if (i == null || g == null) {
            throw new IllegalArgumentException("Invalid Paramiter");
        }

        GaloisMonomial a = i.get(0);
        int max = g.get(0).getValPower();
        int num = a.getValPower() - g.get(0).getValPower();
        GaloisPolynomial r = new GaloisPolynomial(new GaloisMonomial(a.getInteger(), a.getPower(), num), table);
        GaloisPolynomial f = g.operatorMultiply(r).operatorPlus(i);

        for (int n = num - 1; n >= 0; n--) {
            a = f.get(0);
            r = new GaloisPolynomial(new GaloisMonomial(a.getInteger(), a.getPower(), n), table);
            f = g.operatorMultiply(r).operatorPlus(f);

        }

        for (int ii = max - 1; ii >= 0; ii--) {
            int p = f.polynomial.indexOf(new GaloisMonomial(0, 0, ii));
            if (p != f.polynomial.size() - 1) {
                f.polynomial.add(new GaloisMonomial(0, 0, ii));
            }
        }
        return f;
    }

    /** データ */
    private List<GaloisMonomial> polynomial;

    /** */
    private PowerTable table;
}

/* */
