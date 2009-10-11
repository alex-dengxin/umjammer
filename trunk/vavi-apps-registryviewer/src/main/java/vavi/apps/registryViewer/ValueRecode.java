/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.registryViewer;

import java.io.IOException;
import java.io.InputStream;

import vavi.util.Debug;
import vavi.util.win32.registry.Registry;


/**
 * ツリーノードのユーザオブジェクトです． {@link vavi.util.win32.registry.Registry} の実装のサンプルになっています．
 * <ul>
 * <li>コンストラクタをオーバライドする
 * <li>getRoot メソッドを追加する (super.getRoot を使用)
 * <li>TreeRecodeImpl を継承したクラスを作成する
 * <ul>
 * <li>コンストラクタをオーバライドする
 * <li>ユーザが使用するデータを返すメソッドを追加する (getKeySize, getValueName, getValueDataXXX を使用)
 * </ul>
 * </ul>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990630 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 *          1.01 020430 nsano change <init> arg <br>
 */
public class ValueRecode extends Registry {

    /** レジストリの実装を構築します． */
    public ValueRecode(InputStream is) throws IOException {
        super(is);
    }

    /** レジストリのルートを取得します． */
    public TreeRecode getRoot() {
        return (TreeRecode) super.getRoot(TreeRecode.class);
    }

    /** レジストリツリーの１レコードです． */
    public class TreeRecode extends TreeRecodeImpl {

        /** Value to display. */
        private ValueRecodeTableModel value;

        /** TreeRecode を構築します． */
        public TreeRecode(int offset) {
            super(offset);
        }

        /** テーブル用のデータを取得します． */
        public ValueRecodeTableModel getValue() {

            if (value == null) {
                value = new ValueRecodeTableModel();

                for (int i = 0; i < getKeySize(); i++) {
                    String name = getValueName(i);
                    switch (getValueType(i)) {
                    case RegSZ:
                        value.addValue(name, getValueDataAsString(i));
                        break;
                    case RegBin:
                        value.addValue(name, getValueData(i));
                        break;
                    case RegDWord:
                        value.addValue(name, getValueDataAsDWord(i));
                        break;
                    default:
                        Debug.println("type: Unknown: " + getValueType(i));
                        value.addValue(name, getValueData(i), getValueType(i));
                        break;
                    }
                }
            }

            return value;
        }

        /** */
        public String toString() {
            return getKeyName();
        }

        /** */
        public boolean contains(String str) {
            if (value == null) {
                return false;
            }

            for (int i = 0; i < value.getRowCount(); i++) {
                // Debug.println(value.getValueAt(i, 1).getClass().getName());
                if (((String) value.getValueAt(i, 1)).toLowerCase().indexOf(str.toLowerCase()) != -1) {
                    return true;
                }
            }

            return false;
        }
    }
}

/* */
