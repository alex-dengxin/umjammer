/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * HuffmanEncoder.
 * 
 * @author Tomonori Kusanagi
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.00 020330 T.K inital version <br>
 *          2.00 031001 nsano refine <br>
 */
public class HuffmanEncoder {
    /**
     * ハフマン符号化する
     */
    public byte[] encode(byte[] data) throws IOException {
        int[] freq = new int[256];

        // 頻度配列
        for (int i = 0; i < 256; i++) {
            freq[i] = 0;
        }
        for (int i = 0; i < data.length; i++) {
            freq[data[i] + 128]++;
        }

/*
// テスト出力
for (int i = 0; i < 256; i++) {
 System.out.print(freq[i] + " ");
 if (i % 16 == 15)
  System.out.println();
}
*/

        // ハフマン木を作成
        int[] parent = new int[512];
        int[] l_node = new int[512];
        int[] r_node = new int[512];
        buildTree(freq, parent, l_node, r_node);

/*
// テスト出力
for (int i = 0; i < 512 - 1; i++)
 System.out.println("[" + i + "] l:" + l_node[i] + " r:" + r_node[i] + " p:" + parent[i]);
*/

        // 符号を作成
        int n;
        byte[][] code = new byte[256][];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 256; i++) {
            if (freq[i] == 0) {
                continue;
            }

            // 初期化
            n = i;
            baos.reset();
            while (parent[n] != -1) {
                if (parent[n] < 0) {
                    baos.write(1);
                } else {
                    baos.write(0);
                }
                n = Math.abs(parent[n]);
            }

            byte[] rev_code = baos.toByteArray();
            code[i] = new byte[rev_code.length];

            // 反転
            for (int j = 0; j < rev_code.length; j++) {
                code[i][j] = rev_code[rev_code.length - j - 1];
            }
        }

/*
// テスト出力
for (int i = 0; i < 256; i++) {
 System.out.print("["+i+"]:"+freq[i]+" - ");
 if (freq[i] > 0) {
  for (int j = 0; j < code[i].length; j++) {
   System.out.print(code[i][j]);
  }
 }
 System.out.println();
}
*/
        // 符号を得る
        baos.reset();

        for (int i = 0; i < data.length; i++) {
            baos.write(code[data[i] + 128], 0, code[data[i] + 128].length);

/*
n = data[i] + 128;
for (int j = 0; j < code[n].length; j++)
 System.out.print(code[n][j]);
*/
        }

        byte[] tmp = baos.toByteArray();

        byte[] huff = new byte[(tmp.length / 8) + 1];
        for (int i = 0; i < huff.length; i++) {
            // 符号化バイト列作成
            huff[i] = 0;
            for (int j = 0; j < 8; j++) {
                huff[i] <<= 1;
                if (((i * 8) + j) < tmp.length) {
                    huff[i] += tmp[(i * 8) + j];
                }
            }
        }

        baos.reset();

        DataOutputStream dos = new DataOutputStream(baos);

        // 符号化情報
        // type 0：
        //	 全要素の出現頻度をint型でもつ
        //   4[byte] * 256 = 1024[byte]	
        // type 1：
        //	 出現頻度が0でない要素だけについて、
        //   要素番号と出現頻度（int型）をもつ
        //   1[byte] + (1+4)[byte] * 256
        // type 1の場合の符号化情報データ量
        n = 0;
        for (int i = 0; i < 256; i++) {
            if (freq[i] != 0) {
                n++;
            }
        }

        // データが少なくてすむ方を自動的に選ぶ
        int type;
        if ((1 + (n * 5)) > (256 * 4)) {
            type = 0;
        } else {
            type = 1;
        }

//System.out.println("type : " + type);
        if (type == 0) {
            dos.writeByte(0); // 符号情報タイプ
            for (int i = 0; i < 256; i++) {
                dos.writeInt(freq[i]);
            }
        } else if (type == 1) {
            dos.writeByte(1);
            dos.writeByte(n - 128);
            for (int i = 0; i < 256; i++) {
                if (freq[i] != 0) {
                    dos.writeByte(i - 128);
                    dos.writeInt(freq[i]);
                }
            }
        } else {
            throw new IllegalArgumentException("符号化情報番号: " + type);
        }

        // 文字数
        dos.writeInt(data.length);

        // 符号化データ
        dos.write(huff, 0, huff.length);

        dos.close();

        byte[] out = baos.toByteArray();

        // テスト出力
        int prep = data.length;
        int postp = out.length;
System.err.println(prep + " → " + postp + " : " + ((postp * 100) / prep) + "%");

        return out;
    }

    /**
     * Huffman木をつくる
     */
    private void buildTree(int[] freq, int[] parent, int[] l_node, int[] r_node) {
        int[] freq_node = new int[512];

        // 初期化
        for (int i = 0; i < 512; i++) {
            parent[i] = -1;
            l_node[i] = -1;
            r_node[i] = -1;
            if (i < 256) {
                freq_node[i] = freq[i];
            } else {
                freq_node[i] = 0;
            }
        }

        // Huffman木を作成
        int minId;
        for (int i = 256; i < (512 - 1); i++) {
            // 親のない要素で最小のものを探す→新しい左ノード
            minId = findSmallest(i, freq_node, parent);
            l_node[i] = minId;
            parent[minId] = -i;
            freq_node[i] = freq_node[minId];

            // 親のない要素で最小のものを探す→新しい右ノード
            minId = findSmallest(i, freq_node, parent);
            r_node[i] = minId;
            parent[minId] = i;
            freq_node[i] += freq_node[minId];
        }
    }

    /**
     * 配列の親を持たない要素の中で最小のものを探し、その番号を返す
     */
    private int findSmallest(int n, int[] freq_node, int[] parent) {
        int min = -1;
        int minId = -1;

        for (int i = 0; i < n; i++) {
            if (parent[i] != -1) {
                continue;
            }

            if ((minId == -1) || ((minId != -1) && (freq_node[i] < min))) {
                minId = i;
                min = freq_node[i];
            }
        }
        return minId;
    }

    //----

    /** */
    public static void main(String[] args) {
        String inFile = null;

//  	inFile = "projects/Huffman/figure16.bmp";
        if (args.length != 0) {
            inFile = args[0];
        } else {
            System.err.println("引数がありません");
            System.exit(1);
        }

        DataInputStream dis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            dis = new DataInputStream(new FileInputStream(inFile));

            int len = 0;
            byte[] buff = new byte[1024];

            while ((len = dis.read(buff, 0, 1024)) != -1) {
                baos.write(buff, 0, len);
            }
            dis.close();

            byte[] data = baos.toByteArray();

/*
// テスト出力
int min = data[0], max = data[0];
for (int i = 0; i < data.length; i++) {
 if (data[i] > max)
  max = data[i];
 if (data[i] < min)
  min = data[i];
 System.out.print(data[i] + " ");
 if (i % 16 == 0)
  System.out.println();
}
System.out.println("size: " + data.length);
System.out.println("max: " + max);
System.out.println("min: " + min);
*/
            HuffmanEncoder enc = new HuffmanEncoder();
            byte[] encoded = enc.encode(data);

            // 結果出力
            String outFile = inFile + ".hff";
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(encoded, 0, encoded.length);
            fos.close();
        } catch (FileNotFoundException e) {
            System.err.println("そんなファイルありません");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

/* */
