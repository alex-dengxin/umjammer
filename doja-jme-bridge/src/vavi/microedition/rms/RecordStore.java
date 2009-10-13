/*
 * @(#)RecordStoreFile.java	1.40 02/10/03 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package vavi.microedition.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;

import com.nttdocomo.io.ConnectionException;
import com.nttdocomo.lang.UnsupportedOperationException;


/**
 * ｉモードでデータの保存用に使うスクラッチパッドの領域を動的に使用するためのクラス。
 * MIDPのRecordStoreのソースを改変して作成したため、多くの類似点があります。しかし容量削減のために幾つかの違いがあります。
 * 
 * スクラッチパッドを複数のScratchpadクラスで分割して扱うことはできません。
 * レコードとして保存するときには全てsetRecordメソッドを使います。RecordStoreのaddRecordメソッドのようなものはありません。
 * RecordStoreのようにdeleteRecordをしてもその番号は永久欠番にはなりません。setRecordメソッドを使えば再び使えるようになります。
 * RecordStoreのようにListenerをセットすることはありません。 更新回数や更新日時といった情報は保持されません。
 * 
 * 
 * Scratchpadクラスでフォーマットされた領域は先頭の32byteに制御情報を示すデータが格納され、その構造は次のようになっています。
 * 
 * Bytes - Usage 
 * 00-07 - シグネチャ="docomosp"
 * 08-11 - スクラッチパッドの総容量 
 * 12-15 - レコードの総数
 * 16-19 - 最初のレコードの場所
 * 20-23 - 最初の空白箇所の場所
 * 24-27 - データの開始位置
 * 28-31 - データの終了位置
 * 
 * また各レコードの先頭にも制御情報を示すデータが16byte格納されます。
 * 
 * これらの制御情報を用いることで鎖のようにレコード同士を繋ぐことで動的な領域確保を可能にしています。
 * 制御は自動で行われるため通常はこれらを意識する必要はありませんが、実際のデータよりヘッダ情報分だけ余分に消費することは覚えておいて下さい。
 * そのままでも使えますが以下の定数を変更することで最適化することができるのでソースを直接改変して使って下さい。
 * 
 * SP_CHACHE_SIZE 
 * SP_BLOCK_SIZE 
 * SP_COMPACTBUFFER_SIZE
 * 
 * このクラスは6.5k(!)程度の容量を必要とします。どうしても動的な確保が必要でない場合は使わないほうが良いでしょう:-)
 */
public class RecordStore {
    /**
     * スクラッチパッドのヘッダをキャッシュできる数です。
     * キャッシュがメモリに存在する場合はレコードを探す操作が無くなるため読み出しが高速化されます。
     * キャッシュは配列であり、各レコードはID % SP_CHACHE_SIZEの位置に格納されます。
     * よって例えばこの値が10ならばレコード0とレコード10は排他的にしかキャッシュできません。
     * この値を大きくすればそれだけメモリを消費しますので用途に応じて最適な値を設定してください。
     */
    private static int SP_CACHE_SIZE = 10;

    /**
     * 領域を確保する時の単位byte数です
     * ブロック境界をまたぐ変化が起きたときには内部でやや複雑な処理が発生します。
     * この値を余裕をもって大きく取ればこの変化は起こりにくくなりますが、容量の無駄は多くなります。
     * 例えばこの値が16で、書き込むデータが20byteだったとき、16*2=32バイトの領域が確保されます。
     * 後にデータの大きさが変わり、16byte以下か32byte以上になった時に通常より大きな処理が発生します。
     */
    private static int SP_BLOCK_SIZE = 16;

    /**
     * 空きブロックを詰める際にデータの移動が行われますが、それに使われるバッファのサイズ(byte)です
     * 大きく取れば一度に多くのデータを移動させることができるため高速になるでしょう。
     * ただしあまり大きく取りすぎるとメモリ容量オーバーになる可能性があります。
     * よっぽど大きなデータを頻繁に書き込んだり消したりしない限り特に変更する必要はないと思います。
     */
    private static int SP_COMPACTBUFFER_SIZE = 128;

    // ------------以下のフィールドはおそらく変更しない方が良いでしょう-------------------
    private static RecordStore spref = null;

    private static RecordHeaderCache recHeadCache = null;

    // 排他アクセス制御用のオブジェクト 各メソッドはこのオブジェクトによってロックをかけられる
    Object rsLock;

    // Databaseのヘッダフィールド 
    // スクラッチパッドの総容量 
    private static int spTotalSize;

    // 有効なレコード数 
    private static int spNumLiveRecords;

    // 最初のレコードへのオフセット 
    private static int spFirstRecordOffset;

    // 最初の空きブロックへのオフセット
    private static int spFirstFreeBlockOffset;

    // データの開始位置 通常は32になっている
    private static int spDataStart;

    // データの終了位置
    private static int spDataEnd;

    // 直接アクセス用のオフセット（SYSTEM_INFO + SP_...）のアドレスでアクセス
    private final static int SP_HEADER_LENGTH = 32;

    private final static int SP_SIGNATURE = 0;

    private final static int SP_TOTAL_SIZE = 8;

    private final static int SP_NUM_LIVE = 12;

    private final static int SP_REC_START = 16;

    private final static int SP_FREE_START = 20;

    private final static int SP_DATA_START = 24;

    private final static int SP_DATA_END = 28;

    private String accessPath(int pos) {
        return "scratchpad:///0;pos=" + pos;
    }

    /**
     * Scratchpadクラスを使ってフォーマットされた証に、スクラッチパッドの先頭に8バイトのシグネチャが保存されます。
     * asciiコードでdocomospを意味しています。
     */
    private static final byte[] FMT_TAG = new byte[] {
        (byte) 'd', (byte) 'o', (byte) 'c', (byte) 'o', (byte) 'm', (byte) 'o', (byte) 's', (byte) 'p'
    };

    /**
     * フォーマットの開始位置を示しています。
     * これ以降の全ての領域はScratchpadクラスでフォーマットされ、その先頭の位置にシグネチャとシステム情報が保存されます。
     */
    private static final int SYSTEM_INFO = 0; // システム情報が格納されている場所

    // Scratchpad : スクラッチパッドのインスタンスを作り、フォーマット情報を読み込む
    /**
     * Bytes - Usage
     * 00-07 - シグネチャ="docomosp"
     * 08-11 - スクラッチパッドの総容量
     * 12-15 - レコードの総数
     * 16-19 - 最初のレコードの場所
     * 20-23 - 最初の空白箇所の場所
     * 24-27 - データの開始位置
     * 28-31 - データの終了位置
     */
    private RecordStore() {
        spref = this; // 自身への参照を保存
        recHeadCache = new RecordHeaderCache(SP_CACHE_SIZE); // ヘッダのキャッシュを作る
        rsLock = new Object(); // 排他アクセス制御用オブジェクトを作る
        recordListener = new Vector(3);
        try {
            synchronized (rsLock) {

                DataInputStream in = Connector.openDataInputStream(accessPath(SYSTEM_INFO));
                for (int i = 0; i < FMT_TAG.length; i++) {
                    if (FMT_TAG[i] != in.readByte()) {
                        in.close();
                        throw new RecordStoreException();
                        // タグが無い場合は初期化
                    }
                    // 正常ならば順次フォーマット情報を読む
                    spTotalSize = in.readInt();
                    spNumLiveRecords = in.readInt();
                    spFirstRecordOffset = in.readInt();
                    spFirstFreeBlockOffset = in.readInt();
                    spDataStart = in.readInt();
                    spDataEnd = in.readInt();
                    in.close();
                }
            }
        } catch (Exception e) {
            deleteRecordStore(null);
        }
    }

    /**
     * このクラスの唯一のインスタンスを得ます。
     * @return このクラスのインスタンスを返します。
     */
    public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) {
        return spref == null ? new RecordStore() : spref;
    }

    /** */
    public static void deleteRecordStore(String recordStoreName) {
        spref.clear();
    }

    /**
     * スクラッチパッドの内容を初期化します。
     * このメソッドはインスタンスが作られたときにフォーマットが異常だった場合は自動的に呼ばれます。
     * 明示的に呼ぶこともできます。スクラッチパッドに記録された全てのデータは消去されます。
     */
    private void clear() {
        synchronized (rsLock) {
            try {
                DataOutputStream out = Connector.openDataOutputStream(accessPath(SYSTEM_INFO));
                int _spTotalSize = 0;
                try {
                    while (true) {
                        out.writeByte(0);
                        _spTotalSize++;
                    } // 容量を数えつつ全ての値を0で上書きする
                } catch (ConnectionException e) { // 書き込み値オーバー（数え終わり）
                    if (e.getStatus() == ConnectionException.SCRATCHPAD_OVERSIZE) {
                        spTotalSize = _spTotalSize;
                    }
                    out.close();
                }
                spNumLiveRecords = 0;
                spFirstRecordOffset = 0;
                spFirstFreeBlockOffset = 0;
                spDataStart = SP_HEADER_LENGTH;
                spDataEnd = SP_HEADER_LENGTH;
                write(FMT_TAG, SYSTEM_INFO);
                storeSPState();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    /**
     * スクラッチパッドへのアクセスを閉じます。
     * 再びopenScratchpadメソッドを呼ぶまではアクセスができません。
     * @throws RecordStoreException 終了時のデータ操作に異常があった場合に投げられます。
     */
    public void closeRecordStore()
        throws RecordStoreNotOpenException, RecordStoreException { 
        synchronized (rsLock) {
            if (spFirstFreeBlockOffset != 0) {
                compactRecords(); // 終了する前に整頓する
            }
            spref = null; // 自身への参照を廃棄
            recHeadCache = null; // レコードヘッダのキャッシュをクリア
        }
    }

    /**
     * 対象のレコードを削除します。
     * @param recordId 対象のレコード番号。
     * @throws InvalidRecordIDException 対象の番号が存在しなかったときに投げられます。
     */
    public void deleteRecord(int recordId) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
        synchronized (rsLock) {
            RecordHeader rh = null;
            try {
                rh = findRecord(recordId, false);
                freeRecord(rh);
                recHeadCache.invalidate(rh.id);
            } catch (IOException ioe) {
                throw new RecordStoreException("error updating file after" + " record deletion");
            }       
            // システム情報を更新
            spNumLiveRecords--;
            storeSPState();
        }
    }

    /**
     * 存在するレコードの総数を返します。
     * @return 存在するレコードの総数。
     */
    public int getNumRecords() throws RecordStoreNotOpenException {
        return spNumLiveRecords;
    }

    /**
     * 残りの空き容量（byte数）を返します。
     * @return 空き容量。
     */
    public int getSizeAvailable() throws RecordStoreNotOpenException {
        synchronized (rsLock) {
            try {
                int rest = spTotalSize - spDataEnd;
                int cur_offset = spFirstFreeBlockOffset;
                if (cur_offset == 0) {
                    return rest;
                }
                RecordHeader rh = new RecordHeader(cur_offset);
                while (cur_offset != 0) {
                    rh.load(cur_offset);
                    rest += rh.blockSize;
                    cur_offset = rh.dataLenOrNextFree;
                }
                return rest;
            } catch (IOException e) {
                return 0;
            }
        }
    }

    /**
     * Returns the data stored in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     * @param buffer the byte array in which to copy the data
     * @param offset the index into the buffer in which to start copying
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @exception ArrayIndexOutOfBoundsException if the record is
     *          larger than the buffer supplied
     *
     * @return the number of bytes copied into the buffer, starting at
     *          index <code>offset</code>
     * @see #setRecord
     */
    public int getRecord(int recordId, byte[] buffer, int offset) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
        synchronized (rsLock) {
            RecordHeader rh;
            try {
                // throws InvalidRecordIDException
                rh = findRecord(recordId, true);
                rh.read(buffer, offset);
            } catch (IOException ioe) {
                throw new RecordStoreException("error reading record data");
            }
            return rh.dataLenOrNextFree;
        }
    }

    /**
     * 対象のレコードに格納されているデータをバイト列で取得します。
     * @param recordId 対象のレコード番号。
     * @return 対象のレコードに格納されているデータのバイト列
     * @throws InvalidRecordIDException 対象が存在しないか、データの大きさが0の場合に投げられる
     * または読み込み時に何らかのエラーがあったときに投げられる（status=UNDEFINED）
     */
    public byte[] getRecord(int recordId) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
//      int size = 0;
        byte[] data = null;
        try {
            // throws InvalidRecordIDException
            RecordHeader rh = findRecord(recordId, true);
            if (rh.dataLenOrNextFree == 0) {
                return null;
            }
            data = new byte[rh.dataLenOrNextFree];
            rh.read(data, 0);
        } catch (IOException ioe) {
            throw new RecordStoreException("error reading record data");
        }
        return data;
    }

    /**
     * 対象のレコードに格納されているデータの大きさを得る。
     * @param recordId 対象のレコード番号。
     * @return 対象のレコードのデータの大きさ。
     * @throws InvalidRecordIDException 対象が存在しないときに投げられる。
     */
    public int getRecordSize(int recordId) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException {
        synchronized (rsLock) {
            try {
                RecordHeader rh = findRecord(recordId, true);
                return rh.dataLenOrNextFree;
            } catch (java.io.IOException ioe) {
                throw new RecordStoreException("error reading record data");
            }
        }
    }

    /**
     * スクラッチパッドの総容量を得る。
     * note:clear()メソッドの時点で総容量は決められる。
     * 
     * @return スクラッチパッドの総容量。
     */
    public int getSize() throws RecordStoreNotOpenException {
        return spTotalSize;
    }

    /**
     * 対象のレコードにデータを書き込む。 既にレコードが存在する場合には上書きされます。
     * newDataの中からoffsetからnumBytes分だけ書き込まれます。
     * 例えばnewDataを全てを書き込む場合は(recordId, newData, 0,
     * newData.length)のように引数を取ってください。
     * 
     * @param recordId 対象のレコード番号。
     * @param newData 書き込むデータ。
     * @param offset newDataをこの分だけ進んだ位置から書き込む
     * @param numBytes offsetで指定した位置からこの分だけ書き込む
     * @throws RecordStoreFullException サイズ不足で書き込めなかった場合(status=SIZE_FULL)
     *             書き込み時に不明なエラーが出た場合(status=UNDEFINED)
     */
    public void setRecord(int recordId, byte[] newData, int offset, int numBytes) throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, RecordStoreFullException {
        synchronized (rsLock) {

            if ((newData == null) && (numBytes > 0)) {
                throw new NullPointerException();
            }

            RecordHeader rh = null;
            RecordHeader newrh = null;

            try {
                rh = findRecord(recordId, false); // 対象のレコードを探す
            } catch (IOException ioe) {
                throw new RecordStoreException("error finding record data");
            }
            // 既に存在する場合は新しいレコードが古いレコードより大きいか小さいかで動作が違う
            if (numBytes <= rh.blockSize - SP_REC_HEADER_LENGTH) {
                // 新しいデータが置き換え先のデータより小さい
                int allocSize = getAllocSize(numBytes);
                if (rh.blockSize - allocSize >= SP_BLOCK_SIZE + SP_REC_HEADER_LENGTH) {
                    // 分割出来る余裕があるなら分割
                    splitRecord(rh, allocSize);
                }
                rh.dataLenOrNextFree = numBytes;
                try {
                    rh.store(); // 新しいレコードヘッダを書き込む
                    recHeadCache.insert(rh); // キャッシュに登録
                    if (newData != null) {
                        rh.write(newData, offset); // データの書き込み
                    }
                } catch (java.io.IOException ioe) {
                    throw new RecordStoreException("error writing record" + " data");
                }
            } else {

                freeRecord(rh); // 古いデータを開放

                newrh = allocateNewRecordStorage(recordId, numBytes); // 何とかして容量を確保する

                try {
                    if (newData != null) {
                        newrh.write(newData, offset);
                        recHeadCache.insert(newrh);
                    }
                } catch (IOException ioe) {
                    throw new RecordStoreException("error moving record " + "data");
                }
            }
            storeSPState();
        }
    }

    // ------------ 以下 private method --------------
    private byte[] read(int _offset, int _numBytes) throws IOException {
        byte[] bytes = new byte[_numBytes];
        DataInputStream in = Connector.openDataInputStream(accessPath(_offset));
        in.read(bytes, 0, _numBytes);
        in.close();
        return bytes;
    }

    private void write(byte[] _data, int _offset) throws IOException {
        DataOutputStream out = Connector.openDataOutputStream(accessPath(_offset));
        out.write(_data);
        out.close();
    }

    // allocateNewRecordStorage(id, dataSize) : dataSizeの大きさの容量を確保する
    private RecordHeader allocateNewRecordStorage(int _id, int _dataSize) throws RecordStoreException, RecordStoreFullException {
        int allocSize = getAllocSize(_dataSize); // ブロックに適した容量を確保する
        boolean foundBlock = false; // ブロックが見つかったらフラグを立てる
        RecordHeader block = new RecordHeader();
        try {
            int offset = spFirstFreeBlockOffset; // 空きブロックから探す
            while (offset != 0) {
                block.load(offset);
                if (block.blockSize >= allocSize) { // 十分な大きさの空きブロックがあったら
                    foundBlock = true;
                    break; // この空きブロックを使う
                }
                offset = block.dataLenOrNextFree;
            }
        } catch (IOException ioe) {
            throw new RecordStoreException("error finding first fit block");
        }

        // 十分な大きさの空きブロックが無かった場合
        if (foundBlock == false) {
            if (getSizeAvailable() < allocSize) { // 元々空き容量が足りない場合は例外を投げる
                throw new RecordStoreFullException();
            }
            // レコードの末尾に新しいレコードを確保
            block = new RecordHeader(spDataEnd, _id, spFirstRecordOffset, allocSize, _dataSize);
            try {
                block.store();
            } catch (IOException ioe) {
                throw new RecordStoreException("error writing " + "new record data"); 
            }
            spFirstRecordOffset = spDataEnd;
            spDataEnd += allocSize;
        } else {
            // block is where the new record should be stored
            if (block.id != -1) {
                throw new RecordStoreException("ALLOC ERR " + block.id + " is not a free block!");
            }

            removeFreeBlock(block); // remove from free block list

            block.id = _id;
            if (block.blockSize - allocSize >= SP_BLOCK_SIZE + SP_REC_HEADER_LENGTH) {
                splitRecord(block, allocSize); // sets block.blockSize
            }
            block.dataLenOrNextFree = _dataSize;
            try {
                block.store(); 
            } catch (java.io.IOException ioe) {
                throw new RecordStoreException("error writing free block " + "after alloc"); 
            }
        }
        // add new record to cache
        recHeadCache.insert(block);
        return block;
    }

    /**
     * ヘッダを見つける
     * @throws InvalidRecordIDException なければ投げる
     */
    private RecordHeader findRecord(int _recordId, boolean _addToCache) throws InvalidRecordIDException, IOException {
        RecordHeader rh;
        int cur_offset = spFirstRecordOffset;
        if (cur_offset == 0) { // レコードが１つも存在しない場合
            throw new InvalidRecordIDException();
        }
        rh = recHeadCache.get(_recordId); // キャッシュにある場合はそれを返す

        if (rh != null) {
            return rh;
        } // 逐次探す

        rh = new RecordHeader();
        while (cur_offset != 0) {
            rh.load(cur_offset);
            if (rh.id == _recordId) {
                break;
            } else {
                cur_offset = rh.nextOffset;
            }
        }
        if (cur_offset == 0) { // 探したが見つからなかった場合
            throw new InvalidRecordIDException();
        }
        if (_addToCache) {
            recHeadCache.insert(rh);
        }
        return rh;
    }

    /**
     * レコードを開放する
     */
    private void freeRecord(RecordHeader _rh) throws RecordStoreException {
        if (_rh.offset == spFirstRecordOffset) { // １番先頭のレコードを開放する場合
            spFirstRecordOffset = _rh.nextOffset; // 次のレコードを先頭に
            spDataEnd = _rh.offset; // このレコードがあった位置を終端にする
        } else {
            _rh.id = FREE_BLOCK; // idを空きブロックに設定
            _rh.dataLenOrNextFree = spFirstFreeBlockOffset;
            spFirstFreeBlockOffset = _rh.offset; // このレコードを空きブロックの先頭にする
            try {
                _rh.store();
            } catch (IOException ioe) {
                throw new RecordStoreException("free record failed");
            }
        }
    }

    /** 確保する容量を返す */
    private int getAllocSize(int _numBytes) {
        int rv;
        int pad;
        rv = SP_REC_HEADER_LENGTH + _numBytes;
        pad = SP_BLOCK_SIZE - (rv % SP_BLOCK_SIZE);
        if (pad != SP_BLOCK_SIZE) {
            rv += pad;
        }
        return rv;
    }

    /** 空きブロックのヘッダ情報を削除する */
    private void removeFreeBlock(RecordHeader _blockToFree) throws RecordStoreException {
        RecordHeader block = new RecordHeader();
        RecordHeader prev = new RecordHeader();
        RecordHeader tmp = null;
        try {
        int offset = spFirstFreeBlockOffset;
            while (offset != 0) {
                block.load(offset);
                if (block.offset == _blockToFree.offset) {
                    if (block.id != -1) {
                        throw new IOException();
                    }
                    if (prev.offset == 0) {
                        spFirstFreeBlockOffset = block.dataLenOrNextFree;
                    } else {
                        prev.nextOffset = block.dataLenOrNextFree;
                        prev.store();
                    }
                }
                offset = block.dataLenOrNextFree;
                tmp = prev;
                prev = block;
                block = tmp;
            }
        } catch (IOException ioe) {
            throw new RecordStoreException("removeFreeBlock block not found");
        }   
    }

    /**
     * 空きブロックの先頭allocSize分をレコードに、残りを空きブロックにする
     */
    private void splitRecord(RecordHeader _recHead, int _allocSize) throws RecordStoreException {
        RecordHeader newfb;
        int extraSpace = _recHead.blockSize - _allocSize;
        int oldBlockSize = _recHead.blockSize;
        _recHead.blockSize = _allocSize;
        if (_recHead.offset != spFirstRecordOffset) {
            int fboffset = _recHead.offset + _allocSize;
            newfb = new RecordHeader(fboffset, -1, _recHead.offset, extraSpace, 0);
            try {
                freeRecord(newfb);
                RecordHeader prh = new RecordHeader(_recHead.offset + oldBlockSize);
                prh.nextOffset = fboffset;
                prh.store();
                recHeadCache.invalidate(prh.id);
                storeSPState();
            } catch (IOException ioe) {
                throw new RecordStoreException("splitRecord error");
            }
        } else {
            spDataEnd = _recHead.offset + _recHead.blockSize;
        }
    }

    /** */
    private void storeSPState() throws RecordStoreException {
        try {
            // set modification time
            dbLastModified = System.currentTimeMillis();
            DataOutputStream out = Connector.openDataOutputStream(accessPath(SYSTEM_INFO + FMT_TAG.length));
            out.writeInt(spTotalSize);
            out.writeInt(spNumLiveRecords);
            out.writeInt(spFirstRecordOffset);
            out.writeInt(spFirstFreeBlockOffset);
            out.writeInt(spDataStart);
            out.writeInt(spDataEnd);
            out.close();
        } catch (IOException ioe) {
            throw new RecordStoreException("error writing record store " + "attributes");
        }
    }

    /**
     * 全ての空きレコードを詰めて整頓する
     */
    private void compactRecords() throws RecordStoreNotOpenException, RecordStoreException {
        int offset = spDataStart; // 通常のレコードヘッダ探査とは違い、レコードの先頭から探査する
        int target = 0;
        int bytesLeft;
        int numToMove;
        byte[] chunkBuffer = new byte[SP_COMPACTBUFFER_SIZE]; // バッファ
        RecordHeader rh = new RecordHeader();
        int prevRec = 0;
        while (offset < spDataEnd) { // 探査が終端まで達していないなら
            try {
                rh.load(offset); // ヘッダをロードする
            } catch (IOException ioe) {
                // NOTE - should throw some exception here
                System.out.println("Unexpected IOException in CompactRS!");
            }
            if (rh.id == FREE_BLOCK) { // ロードしたヘッダが空きブロックの場合
                if (target == 0) {
                    target = offset; // このヘッダの位置をtargetにする
                }
                offset += rh.blockSize; // 次のブロックに進む
            } else { // データブロックの場合
                if (target == 0) { // これまでの探査で空きブロックが無い場合は動かす必要がない
                    prevRec = offset;
                    offset += rh.blockSize;
                } else {
                    int old_offset = target; // targetの位置をold_offsetに保存
                    // Move a record back in the file
                    rh.offset = target; // このレコードヘッダの位置情報を移動先の空きブロックの位置に変更
                    rh.nextOffset = prevRec;
                    try {
                        rh.store(); // まずヘッダ情報を書き込む
                        offset += SP_REC_HEADER_LENGTH;
                        target += SP_REC_HEADER_LENGTH;
                        bytesLeft = (rh.blockSize - SP_REC_HEADER_LENGTH);
                        while (bytesLeft > 0) {
                            if (bytesLeft < SP_COMPACTBUFFER_SIZE) {
                                numToMove = bytesLeft;
                            } else {
                                numToMove = SP_COMPACTBUFFER_SIZE;
                            }
                            chunkBuffer = read(offset, numToMove);
                            write(chunkBuffer, target);
                            offset += numToMove;
                            target += numToMove;
                            bytesLeft -= numToMove;
                        }
                    } catch (IOException ioe) {
                        System.out.println("Unexpected IOException " + "in CompactRS!");
                    }
                    prevRec = old_offset;
                }
            }
        }
        if (rh.offset != 0) {
            spDataEnd = rh.offset + rh.blockSize; // データ終端の位置を修正
        }
        spFirstRecordOffset = rh.offset; // レコード開始位置を修正
        spFirstFreeBlockOffset = 0; // 全ての空きブロックは無くなった
        storeSPState();
    }

    /****************************************************************************
     * RecordHeader : 各レコードのヘッダ
     ***************************************************************************/

    /*
     * Bytes - Usage
     * 00-03 - レコードID（空きブロックでは-1）
     * 04-07 - 次のレコードまでのオフセット
     * 08-11 - レコードの容量
     * 12-15 - データのサイズ（空きブロックでは次の空きブロックまでのオフセット）
     * 16-xx - データ
     */
    private final static int SP_REC_HEADER_LENGTH = 16;
    private final static int REC_ID = 0;
    private final static int REC_NEXT_OFFSET = 4;
    private final static int REC_BLOCK_SIZE = 8;
    private final static int REC_DATA_LEN = 12;
    private final static int REC_DATA_OFFSET = 16;

    // データの始まり
    private final static int FINAL_BLOCK = 0;
    private final static int FREE_BLOCK = -1;

    /**
     * 各レコードのヘッダの制御情報を扱うクラスです。
     * これはスクラッチパッド内の位置、次のレコードへのオフセット、格納されているデータの長さ、このレコードのサイズの
     * 情報を持ち、Scratchpadはこの情報を用いてレコードを鎖のように繋いでいます。
     */
    private class RecordHeader {
        int offset;
        int id;
        int nextOffset;
        int blockSize;
        int dataLenOrNextFree;

        RecordHeader() {
        }

        RecordHeader(int _offset) throws IOException {
            load(_offset);
        }

        RecordHeader(int _offset, int _id, int _nextOffset, int _blockSize, int _dataLen) {
            offset = _offset;
            id = _id;
            nextOffset = _nextOffset;
            blockSize = _blockSize;
            dataLenOrNextFree = _dataLen;
        }

        // load(_offset) : _offsetの位置にあるレコードヘッダを読み込む
        void load(int _offset) throws IOException {
            DataInputStream in = Connector.openDataInputStream(accessPath(_offset));
            offset = _offset;
            id = in.readInt();
            nextOffset = in.readInt();
            blockSize = in.readInt();
            dataLenOrNextFree = in.readInt();
            in.close();
        }

        // store : レコードヘッダをScratchpadに書き込む。
        void store() throws IOException {
            DataOutputStream out = Connector.openDataOutputStream(accessPath(offset));
            out.writeInt(id);
            out.writeInt(nextOffset);
            out.writeInt(blockSize);
            out.writeInt(dataLenOrNextFree);
            out.close();
        }

        // read(buf, _offset) : 用意したバッファbufに_offsetで指定した位置以降を読み込む
        int read(byte[] buf, int _offset) throws IOException {
            InputStream in = Connector.openInputStream(accessPath(offset + REC_DATA_OFFSET));
            int len = in.read(buf, _offset, dataLenOrNextFree);
            in.close();
            return len;
        }

        // write(data, _offset) : _offsetの位置からデータdataを書き込む
        void write(byte[] data, int _offset) throws IOException {
            OutputStream out = Connector.openOutputStream(accessPath(offset + REC_DATA_OFFSET));
            out.write(data, _offset, dataLenOrNextFree);
            out.close();
        }
    }

    /**
     * このクラスは内部にRecordHeaderの配列を保持します。
     * Scratchpadクラスはまずこのクラスにアクセスし、対象の
     * レコードがキャッシュされているかどうかをチェックし、無かったらスクラッチパッド内から探します。
     */
    private class RecordHeaderCache {
        private RecordHeader[] cache;

        public RecordHeaderCache(int _size) {
            cache = new RecordHeader[_size];
        }

        public RecordHeader get(int _recordId) {
            for (int i = 0; i < cache.length; i++) {
                if (cache[i] != null && cache[i].id == _recordId) {
                    return cache[i];
                }
            }
            return null;
        }

        // insert(rh) : キャッシュに加える
        public void insert(RecordHeader _rh) {
            int idx = _rh.id % cache.length;
            cache[idx] = _rh;
        }

        // invalidate(rec_id) : キャッシュされていたら消す
        void invalidate(int _rec_id) {
            if (_rec_id > 0) {
                int idx = _rec_id % cache.length;
                if ((cache[idx] != null) && (cache[idx].id == _rec_id)) {
                    cache[idx] = null;
                }
            }
        }
    }

    /**
     * Returns an array of the names of record stores owned by the
     * MIDlet suite. Note that if the MIDlet suite does not
     * have any record stores, this function will return null.
     *
     * The order of RecordStore names returned is implementation
     * dependent.
     *
     * @return array of the names of record stores owned by the
     * MIDlet suite. Note that if the MIDlet suite does not
     * have any record stores, this function will return null.
     */
    public static String[] listRecordStores() {
        return new String[] { "ScratchPad" };
    }

    /**
     * Returns the name of this RecordStore.
     *
     * @return the name of this RecordStore
     *
     * @exception RecordStoreNotOpenException if the record store is not open
     */
    public String getName() throws RecordStoreNotOpenException {
        return "ScratchPad";
    }

    /**
     * Authorization to allow access only to the current MIDlet
     * suite. AUTHMODE_PRIVATE has a value of 0.
     */
    public final static int AUTHMODE_PRIVATE = 0;

    /**
     * Authorization to allow access to any MIDlet
     * suites. AUTHMODE_ANY has a value of 1.
     */
    public final static int AUTHMODE_ANY = 1;

    /**
     * Changes the access mode for this RecordStore. The authorization
     * mode choices are:
     *
     * <ul>
     * <li><code>AUTHMODE_PRIVATE</code> - Only allows the MIDlet
     *          suite that created the RecordStore to access it. This
     *          case behaves identically to
     *          <code>openRecordStore(recordStoreName,
     *          createIfNecessary)</code>.</li>
     * <li><code>AUTHMODE_ANY</code> - Allows any MIDlet to access the
     *          RecordStore. Note that this makes your recordStore
     *          accessible by any other MIDlet on the device. This
     *          could have privacy and security issues depending on
     *          the data being shared. Please use carefully.</li>
     * </ul>
     *
     * <p>The owning MIDlet suite may always access the RecordStore and
     * always has access to write and update the store. Only the
     * owning MIDlet suite can change the mode of a RecordStore.</p>
     *
     * @param authmode the mode under which to check or create access.
     *      Must be one of AUTHMODE_PRIVATE or AUTHMODE_ANY.
     * @param writable true if the RecordStore is to be writable by
     *      other MIDlet suites that are granted access
     *
     * @exception RecordStoreException if a record store-related
     *      exception occurred
     * @exception SecurityException if this MIDlet Suite is not
     *      allowed to change the mode of the RecordStore
     * @exception IllegalArgumentException if authmode is invalid
     * @since MIDP 2.0
     */
    public void setMode(int authmode, boolean writable) throws RecordStoreException {
        throw new UnsupportedOperationException();
    }


    /**
     * Each time a record store is modified (by
     * <code>addRecord</code>, <code>setRecord</code>, or
     * <code>deleteRecord</code> methods) its <em>version</em> is
     * incremented. This can be used by MIDlets to quickly tell if
     * anything has been modified.
     *
     * The initial version number is implementation dependent.
     * The increment is a positive integer greater than 0.
     * The version number increases only when the RecordStore is updated.
     *
     * The increment value need not be constant and may vary with each
     * update.
     *
     * @return the current record store version
     *
     * @exception RecordStoreNotOpenException if the record store is
     *            not open
     */
    public int getVersion() throws RecordStoreNotOpenException {
        return dbVersion;
    }

    /** time record store was last modified (in milliseconds */
    private long dbLastModified;

    /**
     * Returns the last time the record store was modified, in the
     * format used by System.currentTimeMillis().
     *
     * @return the last time the record store was modified, in the
     *      format used by System.currentTimeMillis()
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     */
    public long getLastModified() throws RecordStoreNotOpenException {
        return dbLastModified;
    }

    /** recordListeners of this record store */
    private Vector recordListener;

    /**
     * Adds the specified RecordListener. If the specified listener
     * is already registered, it will not be added a second time.
     * When a record store is closed, all listeners are removed.
     *
     * @param listener the RecordChangedListener
     * @see #removeRecordListener
     */
    public void addRecordListener(RecordListener listener) {
        synchronized (rsLock) {
            if (!recordListener.contains(listener)) {
                recordListener.addElement(listener);
            }
        }
    }

    /**
     * Removes the specified RecordListener. If the specified listener
     * is not registered, this method does nothing.
     *
     * @param listener the RecordChangedListener
     * @see #addRecordListener
     */
    public void removeRecordListener(RecordListener listener) {
        synchronized (rsLock) {
            recordListener.removeElement(listener);
        }
    }

    /** next record's id */
    private int dbNextRecordID = 1;
    
    /** record store version */
    private int dbVersion;  

    /**
     * Returns the recordId of the next record to be added to the
     * record store. This can be useful for setting up pseudo-relational
     * relationships. That is, if you have two or more
     * record stores whose records need to refer to one another, you can
     * predetermine the recordIds of the records that will be created
     * in one record store, before populating the fields and allocating
     * the record in another record store. Note that the recordId returned
     * is only valid while the record store remains open and until a call
     * to <code>addRecord()</code>.
     *
     * @return the recordId of the next record to be added to the
     *          record store
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a different record
     *      store-related exception occurred
     */
    public int getNextRecordID() throws RecordStoreNotOpenException, RecordStoreException {
        return dbNextRecordID;
    }

    /**
     * Adds a new record to the record store. The recordId for this
     * new record is returned. This is a blocking atomic operation.
     * The record is written to persistent storage before the
     * method returns.
     *
     * @param data the data to be stored in this record. If the record
     *      is to have zero-length data (no data), this parameter may be
     *      null.
     * @param offset the index into the data buffer of the first
     *      relevant byte for this record
     * @param numBytes the number of bytes of the data buffer to use
     *      for this record (may be zero)
     *
     * @return the recordId for the new record
     *
     * @exception RecordStoreNotOpenException if the record store is
     *      not open
     * @exception RecordStoreException if a different record
     *      store-related exception occurred
     * @exception RecordStoreFullException if the operation cannot be
     *      completed because the record store has no more room
     * @exception SecurityException if the MIDlet has read-only access
     *      to the RecordStore
     */
    public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreNotOpenException, RecordStoreException, RecordStoreFullException {
        synchronized (rsLock) {
            if ((data == null) && (numBytes > 0)) {
                throw new NullPointerException("illegal arguments: null " + "data,  numBytes > 0");
            }
            // get recordId for new record, update db's dbNextRecordID
            int id = dbNextRecordID++;

            /*
             * Find the offset where this record should be stored and seek to
             * that location in the file. allocateNewRecordStorage() allocates
             * the space for this record.
             */     
            RecordHeader rh = allocateNewRecordStorage(id, numBytes);
            try {
                if (data != null) {
                    rh.write(data, offset);
                }
            } catch (IOException ioe) {
                throw new RecordStoreException("error writing new record " + "data");
            }
            
            // Update the state changes to the db file.
            spNumLiveRecords++;
            dbVersion++;
            storeSPState();
            
            // Return the new record id
            return id;
        }
    }

    /**
     * Returns an enumeration for traversing a set of records in the
     * record store in an optionally specified order.<p>
     *
     * The filter, if non-null, will be used to determine what
     * subset of the record store records will be used.<p>
     *
     * The comparator, if non-null, will be used to determine the
     * order in which the records are returned.<p>
     *
     * If both the filter and comparator is null, the enumeration
     * will traverse all records in the record store in an undefined
     * order. This is the most efficient way to traverse all of the
     * records in a record store.  If a filter is used with a null
     * comparator, the enumeration will traverse the filtered records
     * in an undefined order.
     *
     * The first call to <code>RecordEnumeration.nextRecord()</code>
     * returns the record data from the first record in the sequence.
     * Subsequent calls to <code>RecordEnumeration.nextRecord()</code>
     * return the next consecutive record's data. To return the record
     * data from the previous consecutive from any
     * given point in the enumeration, call <code>previousRecord()</code>.
     * On the other hand, if after creation the first call is to
     * <code>previousRecord()</code>, the record data of the last element
     * of the enumeration will be returned. Each subsequent call to
     * <code>previousRecord()</code> will step backwards through the
     * sequence.
     *
     * @param filter if non-null, will be used to determine what
     *          subset of the record store records will be used
     * @param comparator if non-null, will be used to determine the
     *          order in which the records are returned
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *          current with any changes in the records of the record
     *          store. Use with caution as there are possible
     *          performance consequences. If false the enumeration
     *          will not be kept current and may return recordIds for
     *          records that have been deleted or miss records that
     *          are added later. It may also return records out of
     *          order that have been modified after the enumeration
     *          was built. Note that any changes to records in the
     *          record store are accurately reflected when the record
     *          is later retrieved, either directly or through the
     *          enumeration. The thing that is risked by setting this
     *          parameter false is the filtering and sorting order of
     *          the enumeration when records are modified, added, or
     *          deleted.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     *
     * @see RecordEnumeration#rebuild
     *
     * @return an enumeration for traversing a set of records in the
     *          record store in an optionally specified order
     */
    public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) throws RecordStoreNotOpenException {
        return new RecordEnumerationImpl(this, filter, comparator, keepUpdated);
    }

    /*
     * Package Private Methods
     */

    /**
     * Get the open status of this record store.  (Package accessable
     * for use by record enumeration objects.)
     *
     * @return true if record store is open, false otherwise. 
     */
    boolean isOpen() {
        if (spref == null) {
            return false;
        }
        return true;
    }

    /**
     * Returns all of the recordId's currently in the record store.
     *
     * MUST be called after obtaining rsLock, e.g in a 
     * <code>synchronized (rsLock) {</code> block.
     *
     * @return an array of the recordId's currently in the record store
     *         or null if the record store is closed.
     */
    int[] getRecordIDs() {
        if (spref == null) { // lower overhead than checkOpen()
            return null;
        }

        int index = 0;
        int[] tmp = new int[spNumLiveRecords];
        int offset = spFirstRecordOffset; // start at beginning of file
        RecordHeader rh = new RecordHeader();

        try {
            while (offset != 0) {
                rh.load(offset);
                if (rh.id > 0) {
                    tmp[index++] = rh.id;
                }
                offset = rh.nextOffset;
            }
        } catch (IOException ioe) {
            return null;
        }
        return tmp;
    }
}

/* */
