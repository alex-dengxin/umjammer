/*
 * Copyright (c) 1999 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.win32.registry;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * Windows のレジストリ情報を表すクラスです．
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 990629 nsano initial version <br>
 *          1.00 010908 nsano refine <br>
 *          1.01 020430 nsano change <init> arg <br>
 *          1.02 030606 nsano chnage error trap <br>
 */
public class Registry {

    /** 文字列のデータ型 */
    public static final int RegSZ = 0x00000001;
    /** バイナリのデータ型 */
    public static final int RegBin = 0x00000003;
    /** 数値のデータ型 */
    public static final int RegDWord = 0x00000004;

    /** レジストリファイルのバッファ */
    private byte[] memory;

    /** CREG */
    private CREG creg;
    /** RGKN */
    private RGKN rgkn;
    /** RGDB のベクタ */
    private RGDB[] rgdbs;

    /** The encoding */
    private static final String encoding = "JISAutoDetect";

    /**
     * レジストリをストリームから構築します．
     *
     * <pre>
     * CREG
     * RGKN
     *  TreeRecode[0]	???
     *  TreeRecode[1]
     *  TreeRecode[2]
     *  :
     * RGDB[0]
     *  RGDBRecode[0]
     *   ValueRecode[0]
     *   ValueRecode[1]
     *   ValueRecode[2]
     *   :
     *  RGDBRecode[1]
     *  RGDBRecode[2]
     *  :
     * RGDB[1]
     * RGDB[2]
     * :
     * </pre>
     *
     * @param is a registry file stream
     * @throws IOException if an error occurs
     */
    public Registry(InputStream is) throws IOException {

        memory = new byte[is.available()];

        int l = 0;
Debug.println("stream length: " + is.available());
	    while (l < is.available()) {
            l += is.read(memory, l, is.available() - l);
        }

        creg = new CREG();
        rgkn = new RGKN();

        rgdbs = new RGDB[creg.numberOfRGDB];
        int o = creg.offsetOf1stRGDB;
        for (int i = 0; i < creg.numberOfRGDB; i++) {
//Debug.println("RGDB: " + i);
//Debug.println("offset: " + o);
//if (i == 79) f = true;
            rgdbs[i] = new RGDB(o);
            o += rgdbs[i].size;
        }

//	listTreeRecode(new TreeRecode(0x20 + rgkn.offsetOfRootRecode));
    }

//private boolean f = false;

    //-------------------------------------------------------------------------

    /** Test */
    private void listTreeRecode(TreeRecode tr) {
        while (true) {
            if (tr.offsetOf1stSubkey != -1) {
                listTreeRecode(new TreeRecode(0x20 + tr.offsetOf1stSubkey));
            }
            if (tr.offsetOfNext != -1) {
                tr = new TreeRecode(0x20 + tr.offsetOfNext);
            } else {
                break;
            }
        }
    }

    //-------------------------------------------------------------------------

    /** レジストリのルートを取得します． */
    protected TreeRecode getRoot(Class<?> inner) {
        return newInstance(inner, 0x20 + rgkn.offsetOfRootRecode);
    }

    /** 新しい TreeRecode のインスタンスを返します． */
    private TreeRecode newInstance(Class<?> inner, int offset) {
        try {
/*
Debug.println(inner.getName());
Constructor[] cs = inner.getConstructors();
//Debug.println(cs.length);
for(int i = 0; i < cs.length; i++) {
Debug.println("-- " + i + " --");
Class[] ps = cs[i].getParameterTypes();
for(int j = 0; j < ps.length; j++) {
Debug.println(ps[j].getName());
}}*/
            Constructor<?> c = inner.getConstructor(this.getClass(), Integer.TYPE);
            Object treeRecode = c.newInstance(this, new Integer(offset));

            return (TreeRecode) treeRecode;
        } catch (Exception e) {
Debug.printStackTrace(e);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * TreeRecode のユーザインターフェースです．
     */
    protected class TreeRecodeImpl extends TreeRecode {
        
        /** TreeRecode の ValueRecode がある RGDBRecode */
        private RGDBRecode rgdbRecode;
        
        /**  TreeRecode の ValueRecode */
        private ValueRecode[] valueRecodes;
        
        /** TreeRecodeImpl を構築します． */
        public TreeRecodeImpl(int offset) {
            super(offset);

//Debug.println("rgdbs: " + rgdbs.length + ", n: " + numberOfRGDB);
            if (numberOfRGDB == -1) {
Debug.println("no rgdb data, maybe root");
                return;
            }
            
            RGDB rgdb = rgdbs[numberOfRGDB];
            
            int index = getIdNumber();
//Debug.println("index: " + "0x" + StringUtil.toHex8(index));

            Iterator<RGDBRecode> e = rgdb.rrs.iterator();
            while (e.hasNext()) {
                RGDBRecode rr = e.next();
                if (rr.idNumber == index) {
//Debug.println("id: " + numberOfRGDB + "-" + numberInRGDB);
                    rgdbRecode = rr;
                }
            }
            
            if (rgdbRecode == null) {
Debug.println("rgdb recode not found: " + StringUtil.toHex8(index));
return;
            }
            valueRecodes = rgdbRecode.vrs;
        }

        /** 子供の TreeRecode があるかどうかを返します． */
        public boolean hasChildTreeRecodes() {
//Debug.println(offsetOf1stSubkey != -1);
            return offsetOf1stSubkey != -1;
        }
        
        /** 最初の子供の TreeRecode を取得します． */
        public TreeRecode get1stChildTreeRecode() {
            return newInstance(getClass(), 0x20 + offsetOf1stSubkey);
        }
        
        /** 次の TreeRecode があるかどうかを返します． */
        public boolean hasNextTreeRecode() {
//Debug.println(offsetOfNext != -1);
            return offsetOfNext != -1;
        }
        
        /** 次の TreeRecode を取得します． */
        public TreeRecode getNextTreeRecode() {
            return newInstance(getClass(), 0x20 + offsetOfNext);
        }
        
        /** TreeRecode のキー名を取得します． */
        public String getKeyName() {
            if (rgdbRecode != null) {
                return rgdbRecode.keyName;
            } else if (numberOfRGDB == -1) {
                return "HKEY_root";
            } else {
                return "???";	// たぶんシンボリックリンクじゃないの？
            }
        }
        
        /** TreeRecode が持つキーの数を返します． */
        protected int getKeySize() {
            if (valueRecodes != null) {
                return valueRecodes.length;
            } else {
                return 0;
            }
        }
        
        /** インデックスで指定したデータの名前を取得します． */
        protected String getValueName(int index) {
            return valueRecodes[index].valueName;
        }
        
        /** インデックスで指定したデータの型を取得します． */
        protected int getValueType(int index) {
            return valueRecodes[index].type;
        }
        
        /** インデックスで指定したデータの値を取得します． */
        protected byte[] getValueData(int index) {
            return valueRecodes[index].valueData;
        }
        
        /** インデックスで指定したデータの値を String として取得します．*/
        protected String getValueDataAsString(int index) {
            try {
                return new String(getValueData(index), encoding);
            } catch (UnsupportedEncodingException e) {
Debug.printStackTrace(e);
                return new String(getValueData(index));
            }
        }
        
        /** インデックスで指定したデータの値を int として取得します． */
        protected int getValueDataAsDWord(int index) {
            byte[] b = getValueData(index);
            return getDWord(b[0], b[1], b[2], b[3]);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * レジストリのヘッダ情報を表します．
     */
    private final class CREG {

        /** 最初の RGDB へのオフセット */
        private int offsetOf1stRGDB;
        /** RGDB の数 */
        private int numberOfRGDB;
        
        /** CREG を構築します． */
        public CREG() throws IOException {
            
            if (!checkHeader("CREG",
                             memory[0x00],
                             memory[0x01],
                             memory[0x02],
                             memory[0x03])) {
                throw new IllegalArgumentException("CREG");
            }
            
            offsetOf1stRGDB = getDWord(memory[0x08],
                                       memory[0x09],
                                       memory[0x0a],
                                       memory[0x0b]);
//Debug.println("offsetOf1stRGDB: " + "0x" + StringUtil.toHex8(offsetOf1stRGDB));
            
            numberOfRGDB = getWord(memory[0x10],
                                   memory[0x11]);
            Debug.println("numberOfRGDB: " + numberOfRGDB);
        }
    }

    /**
     * レジストリのツリー情報のヘッダを表します．
     */
    private final class RGKN {

        /** RGKN のサイズ？ */
        int size;
        /** ルートの TreeRecode へのオフセット */
        int offsetOfRootRecode;
        
        /** CREG のサイズ？ */
        static final int offset = 0x20;
        
        /** RGKN を構築します． */
        RGKN() {
            
            if (!checkHeader("RGKN",
                             memory[offset+0x00],
                             memory[offset+0x01],
                             memory[offset+0x02],
                             memory[offset+0x03])) {
                throw new IllegalArgumentException("RGKN");
            }
            
            size = getDWord(memory[offset+0x04],
                            memory[offset+0x05],
                            memory[offset+0x06],
                            memory[offset+0x07]);
//Debug.println("size: " + "0x" + StringUtil.toHex8(size));

            offsetOfRootRecode = getDWord(memory[offset+0x08],
                                          memory[offset+0x09],
                                          memory[offset+0x0a],
                                          memory[offset+0x0b]);
//Debug.println("offsetOfRootRecode: "+"0x"+StringUtil.toHex8(offsetOfRootRecode));
        }
    }

    /**
     * レジストリのツリー構造を表すクラスです．
     */
    private class TreeRecode {

        /** RGDBRecode のハッシュ値？ */
        int hash;
        /** 親の TreeRecode へのオフセット */
        int offsetOfParent;
        /** 最初の子供の TreeRecode へのオフセット */
        int offsetOf1stSubkey;
        /** 次の TreeRecode へのオフセット */
        int offsetOfNext;
        /** RGDBRecode がある RGDB のインデックス */
        int numberOfRGDB;
        /** RGDB 内での RGDBRecode のインデックス */
        int numberInRGDB;
        
        /** TreeRecode を構築します． */
        TreeRecode(int offset) {
            
            int dummy;
            
            dummy = getDWord(memory[offset+0x00],
                             memory[offset+0x01],
                             memory[offset+0x02],
                             memory[offset+0x03]);
//Debug.println("always 0: " + dummy);

            hash = getDWord(memory[offset+0x04],
                            memory[offset+0x05],
                            memory[offset+0x06],
                            memory[offset+0x07]);
//Debug.println("hash: " + "0x" + StringUtil.toHex8(hash));

            dummy = getDWord(memory[offset+0x08],
                             memory[offset+0x09],
                             memory[offset+0x0a],
                             memory[offset+0x0b]);
//Debug.println("always -1: " + dummy);

            offsetOfParent = getDWord(memory[offset+0x0c],
                                      memory[offset+0x0d],
                                      memory[offset+0x0e],
                                      memory[offset+0x0f]);
//Debug.println("offsetOfParent: " + "0x" + StringUtil.toHex8(offsetOfParent));

            offsetOf1stSubkey = getDWord(memory[offset+0x10],
                                         memory[offset+0x11],
                                         memory[offset+0x12],
                                         memory[offset+0x13]);
//Debug.println("offsetOf1stSubkey: "+"0x"+StringUtil.toHex8(offsetOf1stSubkey));

            offsetOfNext = getDWord(memory[offset+0x14],
                                    memory[offset+0x15],
                                    memory[offset+0x16],
                                    memory[offset+0x17]);
//Debug.println("offsetOfNext: " + "0x" + StringUtil.toHex8(offsetOfNext));

/*
	        int idNumber = getDWord(memory[offset+0x18],
                        	        memory[offset+0x19],
                        	        memory[offset+0x1a],
                        	        memory[offset+0x1b]);
Debug.println("idNumber: " + "0x" + StringUtil.toHex8(idNumber));
    	    numberOfRGDB = (idNumber >> 16) & 0xffff;
    	    numberInRGDB = idNumber & 0x0000ffff;
//Debug.println("numberOfRGDB: " + numberOfRGDB);
//Debug.println("numberInRGDB: " + "0x" + StringUtil.toHex8(numberInRGDB));
*/
            numberInRGDB = getWord(memory[offset+0x18],
                                   memory[offset+0x19]);
//Debug.println("numberInRGDB: " + numberInRGDB);
            numberOfRGDB = getWord(memory[offset+0x1a],
                                   memory[offset+0x1b]);
//Debug.println("numberOfRGDB: " + numberOfRGDB);
        }
        
        /** ID を返します． */
        int getIdNumber() { return (numberOfRGDB << 16) | numberInRGDB; }
    }

    /**
     * レジストリのブロックです．複数の RGDBRecode を内包します．
     */
    private final class RGDB {

        /** RGDB のサイズ */
        int size;
        /** RGDBRecode のベクタ */
        List<RGDBRecode> rrs = new ArrayList<RGDBRecode>();
        
        /** RGDB を構築します． */
        RGDB(int offset)	{
            
            if (!checkHeader("RGDB",
                             memory[offset+0x00],
                             memory[offset+0x01],
                             memory[offset+0x02],
                             memory[offset+0x03])) {
                throw new IllegalArgumentException("RGDB");
            }
            
            size = getDWord(memory[offset+0x04],
                            memory[offset+0x05],
                            memory[offset+0x06],
                            memory[offset+0x07]);
//Debug.println("size: " + "0x" + StringUtil.toHex8(size));
            
            int o = 0;
            // 0x20 sizeof CREG ??? */
            while (offset + 0x20 + o < memory.length) {
//if(f)Debug.println("offset: " + (offset + 0x20 + o));
                RGDBRecode rr = new RGDBRecode(offset + 0x20 + o);
                rrs.add(rr);
                if (rr.idNumber == -1) {
                    break;
                }
                o += rr.length;
//try{while(System.in.available()>0)System.in.read();System.in.read();}
//catch(IOException e){}
            }
        }
    }

    /**
     * キーを表します．複数の ValueRecode を内包します．
     */
    private final class RGDBRecode {
        
        /** RGDBRecode のサイズ */
        int length;
        /** TreeRecode で参照される ID */
        int idNumber;
        /** RGDBRecode の？ */
        int size;
        /** キーの名前の長さ */
        int textLength;
        /** レジストリの値の数 */
        int numberOfValues;
        /** キーの名前 */
        String keyName;
        /** レジストリの値 */
        ValueRecode[] vrs;
        
        /** RGDBRecode を構築します． */
        RGDBRecode(int offset) {
            
            int dummy;
            
            length = getDWord(memory[offset+0x00],
                              memory[offset+0x01],
                              memory[offset+0x02],
                              memory[offset+0x03]);
//Debug.println("length: " + "0x" + StringUtil.toHex8(length));
            
            idNumber = getDWord(memory[offset+0x04],
                                memory[offset+0x05],
                                memory[offset+0x06],
                                memory[offset+0x07]);
//Debug.println("idNumber: " + "0x" + StringUtil.toHex8(idNumber));

            size = getDWord(memory[offset+0x08],
                            memory[offset+0x09],
                            memory[offset+0x0a],
                            memory[offset+0x0b]);
//Debug.println("size: " + "0x" + StringUtil.toHex8(size));

/*if (idNumber == -1 && size == -1) {
Debug.println("maybe end: length: " + length);
} else */if (idNumber == -1 && size != -1) {
Debug.println("maybe end: size: "+StringUtil.toHex8(size)+": length: "+length);
}

            textLength = getWord(memory[offset+0x0c],
                                 memory[offset+0x0d]);
//Debug.println("textLength: " + textLength);

            numberOfValues = getWord(memory[offset+0x0e],
                                     memory[offset+0x0f]);
//Debug.println("numberOfValues: " + "0x" + StringUtil.toHex4(numberOfValues));

            dummy = getDWord(memory[offset+0x10],
                             memory[offset+0x11],
                             memory[offset+0x12],
                             memory[offset+0x13]);
//Debug.println("always 0: " + dummy);
if(dummy != 0) {
Debug.println("may be end: dummy: " + StringUtil.toHex8(dummy) + ": length: " + StringUtil.toHex8(length));
Debug.println("may be end: index: " + StringUtil.toHex8(idNumber));
idNumber = -1;
return;}

            try {
                keyName = new String(memory, offset + 0x14, textLength, encoding);
            } catch (UnsupportedEncodingException e) {
Debug.println(e);
                idNumber = -1;
                return;
            }
//if(idNumber == -1 && size != -1)Debug.println("keyName: " + keyName);

            vrs = new ValueRecode[numberOfValues];
            // 0x14 sizeof RGDBRecode (base)
            int o = offset + 0x14 + textLength;
            for (int i = 0; i < numberOfValues; i++) {
                vrs[i] = new ValueRecode(o);
//if(idNumber == -1 && size != -1)Debug.println("vr: " + i);
		// 0x0c sizeof VlueRecode (base)
                o += 0x0c + vrs[i].lengthOfValueName + vrs[i].lengthOfValueData;
            }
        }

        /** TreeRecode で参照されるハッシュ値を取得します． */
        int getHash() {
            
            int hash = 0;
            byte[] name = null;
            
            try {
                name = keyName.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
Debug.println(e);
            }
            
            for (int i = 0; i < textLength; i++) {
                if (name[i] < 0x80) {
                    hash += name[i];
                }
            }
            
            return hash;
        }
    }

    /**
     * レジストリの１つのデータを表すクラスです．
     */
    private final class ValueRecode {

        /** データの型 */
        int type;
        /** データの名前の長さ */
        int lengthOfValueName;
        /** データの長さ */
        int lengthOfValueData;
        /** データの名前 */
        String valueName;
        /** データ */
        byte[] valueData;
        
        /** ValueRecode を構築します． */
        ValueRecode(int offset) {
            int dummy;

            type = getDWord(memory[offset+0x00],
                            memory[offset+0x01],
                            memory[offset+0x02],
                            memory[offset+0x03]);
//Debug.println("type: " + getTypeName(type) + "(" + StringUtil.toHex8(type) + ")");

            dummy = getDWord(memory[offset+0x04],
                             memory[offset+0x05],
                             memory[offset+0x06],
                             memory[offset+0x07]);
//Debug.println("always 0: " + dummy);

            lengthOfValueName = getWord(memory[offset+0x08],
                                        memory[offset+0x09]);
//Debug.println("lengthOfValueName: " + lengthOfValueName);

            lengthOfValueData = getWord(memory[offset+0x0a],
                                        memory[offset+0x0b]);
//Debug.println("lengthOfValueData: " + lengthOfValueData);

            try {
                valueName = new String(memory, offset + 0x0c, lengthOfValueName, encoding);
            } catch (UnsupportedEncodingException e) {
Debug.println(e);
                return;
            }
//Debug.println("valueName: " + valueName);

            switch (type) {
            case RegSZ: // 0x00000001
                valueData = new byte[lengthOfValueData];
                for (int j = 0; j < lengthOfValueData; j++) {
                    valueData[j] = memory[offset + 0x0c + lengthOfValueName + j];
                }
//try {
//Debug.println("valueData: " + new String(valueData, encoding));
//} catch(Throwable e) {e.printStackTrace();}
                break;
            case RegBin: // 0x00000003
                valueData = new byte[lengthOfValueData];
                for (int j = 0; j < lengthOfValueData; j++)
                    valueData[j] = memory[offset + 0x0c + lengthOfValueName + j];
//Debug.print("valueData:");
//for(int i = 0; i < lengthOfValueData; i++) {
//System.err.print(" " + StringUtil.toHex2(valueData[i]));
//}Debug.out.println();
                break;
            case RegDWord:		// 0x00000004
                valueData = new byte[4];
                for (int j = 0; j < 4; j++)
                    valueData[j] = memory[offset+0x0c+lengthOfValueName+j];
                break;
            case 0x00000000:
            case 0x00000002:
            case 0x00000007:
            default:
Debug.println("data: unknown(" + type + ")");
            break;
            }
        }

        /** データの方を文字列として返します． */
        String getTypeName(int type) {
            
            switch (type) {
            case RegSZ:			// 0x00000001
                return "RegSZ";
            case RegBin:		// 0x00000003
                return "RegBin";
            case RegDWord:		// 0x00000004
                return "RegDWord";
            case 0x00000000:
            case 0x00000007:
            default:
                return "Unknown";
            }
        }
    }

    //-------------------------------------------------------------------------

    /** リトルエンディアンで 4 Byte 長の int としてデータを読みます． */
    private static final int getDWord(byte ll, byte lh, byte hl, byte hh) {
        return (getWord(hl, hh) & 0xffff) << 16 |
               (getWord(ll, lh) & 0xffff);
    }

    /** リトルエンディアンで 2 Byte 長の short としてデータを読みます． */
    private static final short getWord(byte l, byte h) {
            return (short) ((h & 0xff) << 8 | (l & 0xff));
    }

    /**
     * ヘッダ文字を確認します．
     */
    private static final boolean checkHeader(String header,
				       byte b1, byte b2, byte b3, byte b4) {

        StringBuilder sb = new StringBuilder();
        
        sb.append((char) b1);
        sb.append((char) b2);
        sb.append((char) b3);
        sb.append((char) b4);
        
//Debug.println(sb.toString());
        return sb.toString().equals(header);
    }

    //-------------------------------------------------------------------------

    /**
     * Tests this clas.
     * @param args registry file
     */
    public static void main(String[] args) throws Exception {
        InputStream is = new BufferedInputStream(new FileInputStream(args[0]));
        Registry reg = new Registry(is);
    }
}

/* */
