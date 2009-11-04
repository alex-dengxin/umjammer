/*
 * http://www.nurs.or.jp/~calcium/3gpp/sources/
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import vavi.util.Debug;
import vavi.util.StringUtil;
import vavi.util.win32.WindowsProperties;


/**
 * ATOMChanger. 
 *
 * @author mobilehackerz
 * @see "http://www.nurs.or.jp/~calcium/3gpp/sources/"
 */
class ATOMChanger {

    private final String VERSION = "0.21";

    private final String APPSTR = "3GP_Converter www.nurs.or.jp/~calcium";

    private class MoofPosition {
        long pos;
        int duration;
        int trafNum;
        int trunNum;
        int smplNum;
    }

    private class DriftItem {
        long pos;
        int pointer;
        long posDrift;
        long ptrDrift;
    }

    private class InsertItem {
        String insertPath;
        boolean last;
        byte[] item;
    }

    private List<String> deleteItems;
    private List<InsertItem> insertItems;
    private MoofPosition[] moof;
    private int nowDuration;
    private int durationCount;
    private int durationTmp;
    private List<String> containItem;
    private int videoTrackID;
    private int audioTrackID;
    private int muxab1, muxab2;
    private int muxvb1, muxvb2;
    private int audr;
    private int audch;
    private double vidr;
    private int vidw, vidh;
    private int nowTrackID;
    private int videoTimeScale;
    private int bdefSampleFlag;
    private int defSampleFlag;
    private int bdefDuration;
    private int defDuration;
    private int trafCount;
    private int trunCount;
    private List<DriftItem> driftItems;
    private String title;
    private boolean forceTitle;
    private int mfraMode;
    private boolean edtsMode;
    private boolean iTunesMode;
    private long dcmdPos;
    private int moovSize;
    private int mqv;
    private boolean a5504t;
    private int baseOffset;
    /** [msec] */
    private long creationTime;
    private int forceWidth;
    private int forceHeight;

    /**
     * 
     * @param out
     * @param size whole size of an atom
     * @param atom 4cc string
     * @param buf data buffer
     * @param length data buffer size
     */
    private void writeAtom(RandomAccessFile out, int size, String atom, byte[] buf, int length) throws IOException {
Debug.println("write atom: " + atom + ", " + size + ", " + length);
        byte[] sizeBytes = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(sizeBytes);
        bb.putInt(size);
        out.write(sizeBytes, 0, 4);
        if (atom != null) {
            out.write(atom.getBytes(), 0, 4);
        }
        if (buf != null) {
            out.write(buf, 0, length);
        }
    }

    /**
     * @return written size
     */
    private int writeAtomEx(RandomAccessFile out, byte[] data) throws IOException {
        int size = data.length + 4;
        writeAtom(out, size, null, data, data.length);

        return size;
    }

    /** */
    private int getWord(byte[] buf, int offset) {
        return ByteBuffer.wrap(buf, offset, 2).asShortBuffer().get();
    }

    /** */
    private int getDword(byte[] buf, int offset) {
        return ByteBuffer.wrap(buf, offset, 4).asIntBuffer().get();
    }

    /** */
    private void setWord(byte[] buf, int offset, short value) {
        ByteBuffer.wrap(buf, offset, 2).asShortBuffer().put(value);
    }

    /** */
    private void setDword(byte[] buf, int offset, int value) {
        ByteBuffer.wrap(buf, offset, 4).asIntBuffer().put(value);
    }

    /** */
    private void addDriftPos(byte[] buf, int pos, long ptr) {
        DriftItem driftItem = new DriftItem();
        driftItem.pos = ptr + pos;
        driftItem.posDrift = 0;
        driftItem.pointer = getDword(buf, pos);
        driftItem.ptrDrift = 0;
        driftItems.add(driftItem);
    }

    /** */
    private void driftPos(long start, int drift) {
        for (DriftItem driftItem : driftItems) {
            if (driftItem.pos >= start) {
                driftItem.posDrift = driftItem.posDrift + drift;
            }
            if (driftItem.pointer >= start) {
                driftItem.ptrDrift = driftItem.ptrDrift + drift;
            }
        }
    }

    /**
     * 絶対アドレス記述部分の精査, タイトル情報の吸い上げ, 各種パラメータの読み出し
     */
    public void scanFile(RandomAccessFile in, long max, String title, String path) throws IOException {
        
        long startPointer = in.getFilePointer();
        long pointer = 0;

        while (pointer < max) {
            int size = in.readInt();

            // size = 0 異常 ... atom 検索終了
            if (size < 8) {
                in.seek(startPointer + max);
                pointer = max;

            } else {
                byte[] bytes = new byte[4];
                in.readFully(bytes, 0, 4);
                String atom = new String(bytes, 0, 4);

                // Atom チェック
                boolean contain = containItem.contains(atom);

                // 実処理
                if (atom.equals("moov")) {
                    nowTrackID = 0;
                    moovSize = size;
                }
                if (contain && size >= 16) {
                    // 子 atom をチェックするべきか？ → 階層を降りる
                    scanFile(in, size - 8, title, path + atom + '\\');

                } else {
                    // atom を処理
                    boolean exec = false;
                    byte[] buf;

                    // \moov\\udta\#251nam → タイトル
                    if (atom.equals((char) 0x251 + "nam") || atom.equals((char) 0x169 + "nam")) {
                        if (path.equals("\\moov\\udta\\")) {
                            buf = new byte[size - 8];
                            in.readFully(buf, 0, size - 8);
                            exec = true;
                            int n = getWord(buf, 0); // 文字数
                            if (n != 0) {
//                              buf[4 + n] = 0x00;
                                if (!forceTitle) {
                                    title = new String(buf, 4, buf.length - 4);
                                }
                            }
                        }
                    }

                    // \moov\\udta\titl → MobileMP4 形式タイトル
                    if (atom.equals("titl")) {
                        if (path.equals("\\moov\\udta\\")) {
                            buf = new byte[size - 8];
                            in.readFully(buf, 0, size - 8);
                            exec = true;
//                          buf[size - 8] = 0x00;
                            if (!forceTitle) {
                                title = new String(buf, 6, buf.length - 6);
                            }
                        }
                    }

                    // uuidprop → EZ ムービー形式タイトル
                    if (atom.equals("uuid") && path.equals("\\")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        String uuid = new String(buf, 0, 4);
                        if (uuid.equals("prop")) {
                            int i = 20;
                            while (i < (size - 8)) {
                                int n = getDword(buf, i);
                                uuid = new String(buf, i + 4, 4);
                                if (uuid.equals("titl")) {
                                    if (!forceTitle) {
                                        title = new String(buf, i + 8, buf.length).substring(1, n - 8);
                                    }
                                }
                                i = i + n;
                            }
                        }
                    }

                    // mvhd → CreationTime チェック
                    if (atom.equals("mvhd")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        creationTime = getDword(buf, 0x4) * 1000;
                    }

                    // tkhd → TrackID チェック
                    if (atom.equals("tkhd")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        nowTrackID = getDword(buf, 12);
                        int w = getDword(buf, 0x4A);
                        int h = getDword(buf, 0x4A + 4);
                        int v = getWord(buf, 0x24);
                        if (w != 0 && h != 0) {
                            if (videoTrackID != 0) {
                                System.err.println("A>Warning: VideoTrackが複数あります");
                            }
                            videoTrackID = nowTrackID;
                            vidw = w;
                            vidh = h;
                        }
                        if (v != 0) {
                            if (audioTrackID != 0) {
                                System.err.println("A>Warning: AudioTrackが複数あります");
                            }
                            audioTrackID = nowTrackID;
                        }
                    }

                    // mdhd → Time scale チェック
                    if (atom.equals("mdhd")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        if (nowTrackID == 0) {
                            System.err.println("A>Warning>mdhd:TrackIDが異常です");
                        } else {
                            if (videoTrackID == nowTrackID) {
                                videoTimeScale = getDword(buf, 0x0C);
                            }
                        }
                    }

                    // stts → フレームレートチェック
                    if (atom.equals("stts")) {
                        if (nowTrackID == videoTrackID) {
                            buf = new byte[size - 8];
                            in.readFully(buf, 0, size - 8);
                            exec = true;
                            int n = getDword(buf, 0x04);
                            int n3 = 0;
                            int du = 0;
                            for (int i = 0; i < n; i++) {
                                int n1 = getDword(buf, 8 + i * 8);
                                int n2 = getDword(buf, 12 + i * 8);
                                du = du + n1 * n2; // duration
                                n3 = n3 + n1; // frame
                            }
                            du = du / n3; // duration per frame
                            vidr = videoTimeScale / du;
                        }
                    }

                    // stsd → esds / ビットレートチェック
                    if (atom.equals("stsd")) {
                        if (nowTrackID == videoTrackID || nowTrackID == audioTrackID) {
                            buf = new byte[size - 8];
                            in.readFully(buf, 0, size - 8);
                            exec = true;
                            int n = 0x5e;
                            if (nowTrackID == audioTrackID) {
                                audr = getWord(buf, 0x28); // audio sample rate
                                audch = getWord(buf, 0x20); // audio channels
                                n = 0x2C;
                            }
                            int n2 = 0;
                            for (int n1 = n; n1 <= (size - 12); n1++) {
                                if ((buf[n1] == 'e') && (buf[n1 + 1] == 's') && (buf[n1 + 2] == 'd') && (buf[n1 + 3] == 's')) {
                                    n2 = n1;
                                }
                            }
                            if (n2 != 0) {
                                // found esds
                                n2 = n2 + 8;
                                while (n2 < size - 8) {
                                    n = buf[n2];
                                    switch (n) {
                                    case 3: {
                                        n2 = n2 + 1;
                                        int n1 = buf[n2];
                                        if (n1 == 0x80)
                                            n2 = n2 + 3;
                                        n2 = n2 + 4;
                                    }
                                        break;
                                    case 4: {
                                        n2 = n2 + 1;
                                        int n1 = buf[n2];
                                        if (n1 == 0x80)
                                            n2 = n2 + 3;
                                        if (nowTrackID == videoTrackID) {
                                            muxvb1 = getDword(buf, n2 + 6);
                                            muxvb2 = getDword(buf, n2 + 10);
    
                                        } else {
                                            muxab1 = getDword(buf, n2 + 6);
                                            muxab2 = getDword(buf, n2 + 10);
                                        }
                                        n2 = size - 8;
                                    }
                                        break;
                                    default: {
                                        n2 = size - 8;
                                    }
                                    }
                                }
                            }
                        }
                    }

                    // tfhd → TrackID チェック, ファイル位置情報の書き換え
                    if (atom.equals("tfhd")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        int flag = getWord(buf, 2); // tf_flags
                        if ((flag & 0x01) != 0) {
                            if (!a5504t) {
                                // base-data-offset
                                int p = getDword(buf, 12);
                                if (p != 0) {
                                    addDriftPos(buf, 12, startPointer + pointer + 8);
                                }
                                baseOffset = 0;

                            } else {
                                baseOffset = getDword(buf, 12);
                            }
                        }
                    }

                    // trun → ファイル位置情報の書き換え
                    if (atom.equals("trun")) {
                        if (baseOffset != 0) {
                            buf = new byte[size - 8];
                            in.readFully(buf, 0, size - 8);
                            exec = true;
                            int flag = getWord(buf, 2); // tr_flags
                            if ((flag & 0x01) != 0) { // data-offset-present
                                // data-offset
                                int p = getDword(buf, 8) + baseOffset;
                                setDword(buf, 8, p);
                                addDriftPos(buf, 8, startPointer + pointer + 8);
                            }
                        }
                    }

                    // stco → ファイル位置情報の書き換え
                    if (atom.equals("stco")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        int n = getDword(buf, 4);
                        for (int i = 0; i < n; i++) {
                            addDriftPos(buf, 8 + i * 4, startPointer + pointer + 8);
                        }
                    }

                    // tfra → ファイル位置情報の書き換え
                    if (atom.equals("tfra")) {
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        // length
                        int n = getDword(buf, 8);
                        int n1 = ((n >> 4) & 3) + 1;
                        int n2 = ((n >> 2) & 3) + 1;
                        int n3 = ((n) & 3) + 1;
                        // number_of_entry
                        n = getDword(buf, 12);
                        for (int i = 0; i < n; i++) {
                            addDriftPos(buf, 20 + i * (8 + n1 + n2 + n3), startPointer + pointer + 8);
                        }
                    }

                    // それ以外 → スキップ
                    if (!exec) {
                        in.skipBytes(size - 8);
                    }
                }
                pointer = in.getFilePointer() - startPointer;
            }
        }
    }

    /**
     * atom の追加 / 削除 / チェック
     */
    public int execute(RandomAccessFile in, RandomAccessFile out, long max, String path) throws IOException {
        
        long startPointer = in.getFilePointer();
        long pointer = 0;
        int result = 0; // 返り値 = atom サイズ増減

        // InsertAtom チェック (First)
        for (InsertItem insertItem : insertItems) {
            if (insertItem.insertPath.equals(path) && !insertItem.last) {
                int drift = writeAtomEx(out, insertItem.item);
                result += drift;
                driftPos(startPointer, drift);
            }
        }

        while (pointer < max) {
            // atom サイズ, atom の読み出し
            long atomPointer = in.getFilePointer();
            byte[] bytes = new byte[4];
            in.readFully(bytes, 0, 4);
            int size = ByteBuffer.wrap(bytes).asIntBuffer().get();
            int dur = 0;

            // size = 0 異常 ... atom 検索終了
            if (size < 8) {
                in.seek(startPointer + max);
                pointer = max;

            } else {
                in.readFully(bytes, 0, 4);
                String atom = new String(bytes, 0, 4);

                // Atom チェック
                boolean deleting = deleteItems.contains(atom);
                boolean contain = containItem.contains(atom);

                // 実処理
                if (deleting) {
                    // 削除
                    in.skipBytes(size - 8);
                    result -= size;
                    driftPos(atomPointer, -size);

                } else {
                    // mfra 構築用データチェック
                    if (atom.equals("moof")) {
                        // moof Position 記録
                        int i = moof.length;
                        moof = new MoofPosition[i + 1];
                        nowDuration = nowDuration + durationCount;
                        moof[i].pos = out.getFilePointer();
                        moof[i].duration = nowDuration;
                        moof[i].trafNum = 0;
                        moof[i].trunNum = 0;
                        moof[i].smplNum = 0;
                        trafCount = 0;
                        trunCount = 0;
                        durationTmp = 0;
                    }

                    if (atom.equals("traf")) {
                        trafCount = trafCount + 1;
                    }

                    // 着モーション用書き換え場所チェック
                    if (atom.equals("dcmd")) {
                        if (dcmdPos != 0) {
                            System.err.println("A>Warning: dcmd atomが複数あります");

                        } else {
                            dcmdPos = out.getFilePointer();
                        }
                    }

                    byte[] buf;

                    if (contain && size >= 16) {
                        // 子 atom をチェックするべきか？ → 階層を降りる
                        long temp_ptr = out.getFilePointer();
                        writeAtom(out, size, atom, null, 0);
                        int drift = execute(in, out, size - 8, path + atom + "\\");
                        // サイズが増減していたら size を書き換える
                        if (drift != 0) {
                            long temp_ptr2 = out.getFilePointer();
                            out.seek(temp_ptr);
                            buf = new byte[4];
                            out.read(buf, 0, 4);
                            setDword(buf, 0, (int) ((long) getDword(buf, 0) + drift));
                            out.skipBytes(-4);
                            out.write(buf, 0, 4);
                            out.seek(temp_ptr2);
                        }
                        return result + drift;

                    } else {
                        // atom を処理
                        buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);

                        // チェック
                        if (atom.equals("moov")) {
                            nowTrackID = 0;
                        }

                        // tkhd → Duration チェック
                        if (atom.equals("tkhd")) {
                            int w = getDword(buf, 0x4A);
                            int h = getDword(buf, 0x4A + 4);
                            int v = getWord(buf, 0x24);
                            nowTrackID = getDword(buf, 12);
                            if (w != 0 || h != 0 || v != 0) {
                                dur = getDword(buf, 0x14);
                            }
                        }

                        // trex → default_sample_flags チェック
                        if (atom.equals("trex")) {
                            if (videoTrackID == getDword(buf, 4)) {
                                bdefSampleFlag = getDword(buf, 20);
                                defSampleFlag = bdefSampleFlag;
                                bdefDuration = getDword(buf, 12);
                                defDuration = bdefDuration;
                            }
                        }

                        // mvhd → Duration チェック
                        if (atom.equals("mvhd"))
                            durationCount = getDword(buf, 16);

                        // tfhd → TrackID, default_sample_flags チェック
                        if (atom.equals("tfhd")) {
                            // A5504Tモードはbase_data_offsetを0に
                            int flag = getWord(buf, 2);
                            if (a5504t && (flag & 0x001) != 0) {
                                setDword(buf, 12, 0);
                            }

                            int n = getDword(buf, 4); // TrackID
                            if (n == videoTrackID) {
                                int i = moof.length - 1;
                                if (i >= 0) {
                                    moof[i].trafNum = trafCount;
                                }

                                // default_sample_flags チェック
                                defSampleFlag = bdefSampleFlag;
                                defDuration = bdefDuration;
                                flag = getWord(buf, 2);
                                int n1 = 8;
                                int n2 = -1;
                                int n3 = -1;
                                if ((flag & 0x001) != 0) {
                                    n1 = n1 + 8; // base_data_offset
                                }
                                if ((flag & 0x002) != 0) {
                                    n1 = n1 + 4; // sample_description_index
                                }
                                if ((flag & 0x008) != 0) {
                                    n2 = n1;
                                    n1 = n1 + 4;
                                } // default_sample_duration
                                if ((flag & 0x010) != 0) {
                                    n1 = n1 + 4; // default_sample_size
                                }
                                if ((flag & 0x020) != 0) {
                                    n3 = n1;
                                }
                                if (n2 >= 0) {
                                    defDuration = getDword(buf, n2);
                                }
                                if (n3 >= 0) {
                                    defSampleFlag = getDword(buf, n3);
                                }
                            }
                        }

                        // trun → Durationチェック, sample_flags チェック
                        if (atom.equals("trun")) {
                            trunCount = trunCount + 1;
                            int flag = getWord(buf, 2); // tr_flags
                            int n1 = 8;
                            int n2 = 0;
                            int n3 = -1;
                            int n = getDword(buf, 4); // sample-count
                            if ((flag & 0x001) != 0) {
                                n1 = n1 + 4; // data-offset-present
                            }
                            if ((flag & 0x004) != 0) {
                                n1 = n1 + 4; // first-sample-flags
                            }
                            if ((flag & 0x100) != 0) {
                                n2 = n2 + 4; // sample-duration
                            }
                            if ((flag & 0x200) != 0) {
                                n2 = n2 + 4; // sample-size
                            }
                            if ((flag & 0x400) != 0) {
                                n3 = n2;
                                n2 = n2 + 4;
                            } // sample-flags
                            if ((flag & 0x800) != 0) {
                                n2 = n2 + 4; // sample-composition-time-offsets
                            }
                            // sample_flags チェック
                            int w = moof.length - 1;
                            int h = 0;
                            if (w >= 0) {
                                if (moof[w].smplNum == 0) {
                                    if ((flag & 0x400) != 0) {
                                        for (int i = 0; i < n; i++) {
                                            int dflg = getDword(buf, n1 + i * n2 + n3);
                                            if ((dflg & 0x10000) == 0) { // when 1 signals a non-key or non-sync sample
                                                h = i + 1;
                                                break;
                                            }
                                        }
                                    } else {
                                        if ((defSampleFlag & 0x10000) == 0)
                                            h = 1;
                                    }
                                    if (h != 0) {
                                        moof[w].trunNum = trunCount;
                                        moof[w].smplNum = h;
                                        moof[w].duration = moof[w].duration + durationTmp;
                                        durationTmp = 0;
                                    }
                                }
                            }

                            // Duration チェック
                            for (int i = 0; i < n; i++) {
                                if ((flag & 0x100) != 0) {
                                    if (h > (i + 1)) {
                                        moof[w].duration = moof[w].duration + getDword(buf, n1 + i * n2);

                                    } else {
                                        durationTmp = durationTmp + getDword(buf, n1 + i * n2);
                                    }

                                } else {
//                                      if (mfraMode != 2) {
//                                        if (h > (i + 1)) {
//                                            Moof[w].Duration = Moof[w].Duration + defDuration;
//                                        } else {
//                                            DurationTmp = DurationTmp + defDuration;
//                                        }
//                                    }
                                }
                            }
//                            if (mfraMode != 2 || DurationTmp != 0) {
//                                DurationCnt = DurationTmp;
//                            }
                        }

                        writeAtom(out, size, atom, buf, size - 8);
                    }
                }
                pointer = in.getFilePointer() - startPointer;
            }

            // edts Insert (tkhd の後)
            if (edtsMode && dur != 0) {
                atomPointer = in.getFilePointer(); // atom サイズ位置
                ByteBuffer bb = ByteBuffer.allocate(256);
                bb.put("edts".getBytes());
                bb.putInt(0x1c);
                bb.put("elst".getBytes());
                bb.putInt(0); // Version,Flags
                bb.putInt(1); // Num
                bb.putInt(dur);
                bb.putInt(0);
                bb.putInt(0x100); // Duration
                bb.flip();
                int drift = writeAtomEx(out, bb.array());
                result += drift;
                driftPos(atomPointer, drift);
            }
        }

        // InsertAtom チェック (Last)
        long atom_ptr = in.getFilePointer(); // atom サイズ位置
        for (InsertItem insertItem : insertItems) {
            if (insertItem.insertPath.equals(path) && insertItem.last) {
                int drift = writeAtomEx(out, insertItem.item);
                result += drift;
                driftPos(atom_ptr, drift);
            }
        }
        return result;
    }

    /**
     * ずれたファイル位置情報の修正
     */
    public void drift(RandomAccessFile out) throws IOException {
        long startPos = out.getFilePointer();
        byte[] buf = new byte[4];
        for (DriftItem driftItem : driftItems) {
            out.seek(driftItem.pos + driftItem.posDrift);
            setDword(buf, 0, (int) (driftItem.pointer + driftItem.ptrDrift));
            out.write(buf, 0, 4);
        }
        out.seek(startPos);
    }

    /**
     * mfra の新規構築
     */
    private void buildMfra(RandomAccessFile in, int mode) throws IOException {
        int n = moof.length;
        if (n > 0 && videoTrackID != 0) {
            int size1 = 24 + n * 11; // tfra size
            int size2 = size1 + 16 + 8; // mfra size
            // mfra
            writeAtom(in, size2, "mfra", null, 0);

            // tfra
            byte[] buf = new byte[size1 - 8];
            setDword(buf, 0, 0x00000000);
            setDword(buf, 4, videoTrackID);
            setDword(buf, 8, 0x00000000);
            // 数
            setDword(buf, 12, n);

            // Moof
            for (int i = 0; i < n; i++) {
                setDword(buf, 16 + i * 11, moof[i].duration);
                setDword(buf, 20 + i * 11, (int) moof[i].pos);
                if (((moof[i].trafNum == 0) || (moof[i].trunNum == 0 && moof[i].smplNum == 0)) || (mode == 2)) {
                    buf[24 + i * 11] = 0x01;
                    buf[25 + i * 11] = 0x01;
                    buf[26 + i * 11] = 0x01;

                } else {
                    buf[24 + i * 11] = (byte) (moof[i].trafNum);
                    buf[25 + i * 11] = (byte) (moof[i].trunNum);
                    buf[26 + i * 11] = (byte) (moof[i].smplNum);
                }
            }
            writeAtom(in, size1, "tfra", buf, size1 - 8);

            // mfro
            buf = new byte[8];
            setDword(buf, 0, 0);
            setDword(buf, 4, size2);
            writeAtom(in, 16, "mfro", buf, 8);
        }
    }

    /**
     * URL デコード
     */
    private String decodeURL(String string) {

        StringBuilder result = new StringBuilder();

        while (!string.equals("")) {
            String s1 = string.substring(0, 1);
            string = string.substring(1, string.length());
            if (s1.equals("%")) {
                s1 = string.substring(0, 2);
                string = string.substring(2, string.length());
                result.append(Integer.parseInt("0x" + s1));

            } else {
                result.append(s1);
            }
        }

        return result.toString();
    }

    /** */
    private String decodeURL(String string, int length) {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            String s1 = string.substring(0, 1);
            string = string.substring(1, string.length());
            if (s1.equals("%")) {
                s1 = string.substring(0, 2);
                string = string.substring(2, string.length());
                result.append(Integer.parseInt("0x" + s1));

            } else {
                result.append(s1);
            }
        }

        return result.toString();
    }

    /**
     * メインルーチン
     */
    public static void main(String[] args) throws Exception {
        new ATOMChanger(args);
    }

    /** */
    ATOMChanger(String[] args) throws FileNotFoundException, IOException {

        // コマンドライン処理
        String exeName = "";
        if (args.length >= 0) {
            exeName = this.getClass().getName();
        }
        String appName = exeName.substring(this.getClass().getName().lastIndexOf('.') + 1);

        // Opening
        System.err.println(appName + " for 3GP_Converter Version " + VERSION);
        System.err.println("http://www.nurs.or.jp/~calcium/");

        // 初期値
        boolean optUsage = false;
        String inFilename = null;
        String outFilename = null;
        String iniFilename = null;
        String daystr = "1970/01/01 00:00:00";
        nowDuration = 0;
        durationCount = 0;
        durationTmp = 0;
        videoTrackID = 0;
        audioTrackID = 0;
        nowTrackID = 0;
        videoTimeScale = 90000;
        vidw = 160;
        vidh = 120;
        vidr = 1.0;
        muxab1 = 1;
        muxab2 = 1;
        muxvb1 = 1;
        muxvb2 = 1;
        audr = 16000;
        audch = 1;
        trafCount = 0;
        trunCount = 0;
        defSampleFlag = 0;
        bdefSampleFlag = 0;
        defDuration = 0;
        bdefDuration = 0;
        dcmdPos = 0;
        moovSize = 0;
        baseOffset = 0;
        creationTime = 0;
        forceTitle = false;
        moof = null;
        driftItems = new ArrayList<DriftItem>();
        insertItems = new ArrayList<InsertItem>();

        // コンテナとなるAtom
        containItem = new ArrayList<String>();
        containItem.add("moov");
        containItem.add("trak");
        containItem.add("edts");
        containItem.add("mdia");
        containItem.add("minf");
        containItem.add("dinf");
        containItem.add("stbl");
        containItem.add("mvex");
        containItem.add("moof");
        containItem.add("traf");
        containItem.add("mfra");
        containItem.add("skip");
        containItem.add("udta");
        containItem.add("drm ");

        // 引数処理
        
        for (int i = 0; i < args.length; i++) {
            String s = args[i];

            if (s.charAt(0) == '-') {
                // Error
                optUsage = true;

            } else {
                // fileName
                if (inFilename == null) {
                    inFilename = s;

                } else if (outFilename == null) {
                    outFilename = s;
                } else if (iniFilename == null) {
                    iniFilename = s;

                } else if (!forceTitle) {
                    forceTitle = true;
                    title = s;

                } else {
                    // Error
                    optUsage = true;
                }
            }
        }
        if (inFilename == null || outFilename == null) {
            optUsage = true;
        }

        // inputFile Test & default Title
        if (!forceTitle) {
            title = inFilename.substring(inFilename.lastIndexOf('\\') + 1).split("\\.")[0];
        }
        if (!new File(inFilename).exists()) {
            optUsage = true;
        }

        // 処理
        if (optUsage) {
            printUsage(appName);
        }

        // iniファイル名生成
        if (iniFilename == null) {
            iniFilename = appName + ".ini";

        } else {
            if (iniFilename.charAt(1) != ':' && iniFilename.charAt(0) != '\\') {
                iniFilename = new File(iniFilename).getCanonicalPath();
            }
        }

        // iniFile (1: Settings)
        Properties props = new WindowsProperties();
        props.load(new FileInputStream(iniFilename));
        String uuid = props.getProperty("Config.UUID", "");
        mfraMode = Integer.parseInt(props.getProperty("Config.Build_mfra", "0"));
        edtsMode = Boolean.parseBoolean(props.getProperty("Config.Build_edts", "false"));
        iTunesMode = Boolean.parseBoolean(props.getProperty("Config.Build_iTunes", "false"));
        mqv = Integer.parseInt(props.getProperty("Config.MQV", "0"));
        String desc = props.getProperty("Config.Title", "");
        boolean iMotion = Boolean.parseBoolean(props.getProperty("Config.iMotion", "false"));
        a5504t = Boolean.parseBoolean(props.getProperty("Config.A5504T", "false"));
        forceWidth = Integer.parseInt(props.getProperty("Config.ForceWidth", "0"));
        forceHeight = Integer.parseInt(props.getProperty("Config.ForceHeight", "0"));

        // 処理開始(処理内容表示)
        System.err.println(desc);

        // ファイル内容精査
        long max;
        RandomAccessFile in = new RandomAccessFile(inFilename, "r");
        if (in != null) {
            max = in.length();
            scanFile(in, max, title, "\\");
            in.close();
        }
        // TimeStamp
        Date fdt = new Date(new File(inFilename).lastModified());
        if (creationTime == 0) {
            // File Timestamp
            creationTime = fdt.getTime() + 0x7c25b080 * 1000;
        }
        String ts_s = String.format("%%%02X%%%02X%%%02X%%%02X",
                                    (creationTime / 1000 / 0x1000000) & 0xff,
                                    (creationTime / 1000 / 0x10000) & 0xff,
                                    (creationTime / 1000 / 0x100) & 0xff,
                                    (creationTime / 1000) & 0xff);
Debug.println("ts_s: " + ts_s);

        daystr = new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(fdt);
Debug.println("daystr: " + daystr);

        // IniFile (2: Atom Editings)
        initDeleteItems(props);

        initInsertItems(props, uuid, ts_s);

        // 実処理開始

        int result = 0;
        in = new RandomAccessFile(inFilename, "r");
        if (in != null) {
            // 解析結果表示
            if (videoTrackID != 0) {
                System.err.println("VideoTrack : " + videoTrackID + " " + vidw + " x " + vidh + " " + vidr/* ffFixed,5,2 */+ "fps " + muxvb2 + "bps");
            }
            if (audioTrackID != 0) {
                System.err.println("AudioTrack : " + audioTrackID + " " + audr + "Hz " + audch + "ch " + muxab2 + "bps");
            }

            // MQV向け処理ならMQVヘッダをInsertする
            // iTunes向け処理ならiTunesヘッダをInsertする
            if (mqv == 1 || iTunesMode) {

                insertItems.add(getMoov(daystr));
            }

            // PSP向け処理ならPSP向けヘッダをInsertする
            if (mqv == 2) {

                // moov\\uuid
                insertItems.add(getMoovUuid(daystr));

                try {
                    // \\uuid
                    insertItems.add(getUuid(uuid));
                } catch (IllegalStateException e) {
                    System.err.println(e.getMessage());
                    result = -2;
                }

                // moov\\trak\\uuid
                insertItems.add(getMoovTrakUuid(uuid));
            }

            // Pass 2: ファイルのコピーと atom 編集
            RandomAccessFile out = new RandomAccessFile(outFilename, "rw");
            if (out != null) {
                max = in.length();
                in.seek(0);
                execute(in, out, max, "\\");
                if (mfraMode != 0 && videoTrackID != 0) {
                    buildMfra(out, mfraMode);
                }
                drift(out);
                new File(outFilename).setLastModified(new File(inFilename).lastModified());

            } else {
                System.err.println("A>" + outFilename + "を開けませんでした。");
                result = -1;
            }

            // 着モーション向け設定なら dcmd データを書き換える
            if (iMotion && out != null) {
                if (dcmdPos == 0) {
                    System.err.println("A>Warning: dcmd atomが見つかりません");
                } else {
                    max = out.length();
                    int i = 0x0909;
                    if ((max % 2) != 0) {
                        i = i | 0x04;
                    }
                    if ((moovSize % 2) != 0) {
                        i = i | 0x02;
                    }
                    out.seek(dcmdPos + 8);
                    byte[] buf = new byte[2];
                    ByteBuffer.wrap(buf).asShortBuffer().put((short) i);
                    out.write(buf, 0, 2);
                }
            }

            if (in != null)
                in.close();
            if (out != null)
                out.close();

        } else {
            System.err.println("A>" + inFilename + "を開けませんでした。");
            result = -1;
        }

        deleteItems.clear();
Debug.println("result: " + result);
        System.exit(result);
    }

    /** Sony uuid */
    private static final byte[] sonyUuid = new byte[] { 0x21, (byte) 0xd2, (byte) 0x4f, (byte) 0xce, (byte) 0xbb, (byte) 0x88, (byte) 0x69, (byte) 0x5c, (byte) 0xfa, (byte) 0xc9, (byte) 0xc7, (byte) 0x40 };

    /** */
    private InsertItem getMoovUuid(String daystr) {
        ByteBuffer bb = ByteBuffer.allocate(256);
        ByteBuffer bb1 = ByteBuffer.allocate(256);
        ByteBuffer bb2 = ByteBuffer.allocate(256);

        bb2.put("MTDT".getBytes());
        bb2.putShort((short) 4);

        // und
        bb1.clear();
        bb1.putInt(0x0b);
        bb1.putShort((short) 0x55c4); // 'und'
        bb1.putShort((short) 0);
        bb1.putShort((short) 0x021c);
        bb1.flip();

        bb2.putShort((short) (bb1.limit() + 2));
        bb2.put(bb1);

        // converter
        String ws = APPSTR;
        bb1.clear();
        bb1.putInt(0x04);
        bb1.putShort((short) 0x15c7); // 'eng'
        bb1.putShort((short) 1);
        for (int n = 0; n < ws.length(); n++) {
            bb1.put((byte) ((ws.charAt(n) >> 8) & 0xff)); // TODO check
            bb1.put((byte) (ws.charAt(n) & 0xff));
        }
        bb1.put((byte) 0);
        bb1.put((byte) 0);
        bb1.flip();

        bb2.putShort((short) (bb1.limit() + 2));
        bb2.put(bb1);

        // Title
        ws = title;
        bb1.clear();
        bb1.putInt(0x01);
        bb1.putShort((short) 0x2a0e); // 'jpn'
        bb1.putShort((short) 1);
        for (int n = 0; n < ws.length(); n++) {
            bb1.put((byte) ((ws.charAt(n) >> 8) & 0xff)); // TODO check
            bb1.put((byte) (ws.charAt(n) & 0xff));
        }
        bb1.put((byte) 0);
        bb1.put((byte) 0);
        bb1.flip();

        bb2.putShort((short) (bb1.limit() + 2));
        bb2.put(bb1);

        // Day
        ws = daystr;
        bb1.clear();
        bb1.putInt(0x03);
        bb1.putShort((short) 0x55c4); // 'und'
        bb1.putShort((short) 1);
        for (int n = 0; n <= ws.length(); n++) {
            bb1.put((byte) ((ws.charAt(n) >> 8) & 0xff));
            bb1.put((byte) (ws.charAt(n) & 0xff));
        }
        bb1.put((byte) 0);
        bb1.put((byte) 0);
        bb1.flip();

        bb2.putShort((short) (bb1.limit() + 2));
        bb2.put(bb1);
        bb2.flip();

        //
        bb.clear();

        bb.put("uuid".getBytes());
        bb.put("USMT".getBytes());
        bb.put(sonyUuid);
        bb.putInt(bb2.limit() + 4);
        bb.put(bb2);
        bb.flip();

        InsertItem insertItem = new InsertItem();
        insertItem.insertPath = "\\moov\\";
        insertItem.last = true;
        insertItem.item = new byte[bb.limit()];

        bb.get(insertItem.item);

Debug.println("inserting: " + insertItem.insertPath + "\n" + StringUtil.getDump(insertItem.item));
        return insertItem;
    }

    /** */
    private InsertItem getUuid(String uuid) {
        ByteBuffer bb = ByteBuffer.allocate(256);
        ByteBuffer bb1 = ByteBuffer.allocate(256);

        int n = 1;
        if (audioTrackID != 0){
            n = n + 1;
        }
        if (videoTrackID != 0) {
            n = n + 1;
        }

        bb.put("uuid".getBytes());
        bb.put("PROF".getBytes());
        bb.put(uuid.getBytes());
        bb.putInt(0);
        bb.putInt(n);

        bb1.clear();
        bb1.put("FPRF".getBytes());
        bb1.putInt(0);
        bb1.putInt(0);
        bb1.putInt(0);
        bb1.flip();

        bb.putInt(bb1.limit() + 4);
        bb.put(bb1);

        if (audioTrackID != 0) {
            bb1.clear();
            bb1.put("APRF".getBytes());
            bb1.putInt(0);
            bb1.putInt(audioTrackID);
            bb1.put("mp4a".getBytes());
            bb1.putInt(0x020f); // FIXME
            bb1.putInt(0);
            bb1.putInt(muxab1 / 1000);
            bb1.putInt(muxab2 / 1000);
            bb1.putInt(audr);
            bb1.putInt(audch);
            bb1.flip();

            bb.putInt(bb1.limit() + 4);
            bb.put(bb1);

        } else {
            throw new IllegalStateException("A>変換中のファイルにオーディオトラックが見つかりません。正常に再生できない可能性があります。");
        }

        if (videoTrackID != 0) {
            bb1.clear();
            bb1.put("VPRF".getBytes());
            bb1.putInt(0);
            bb1.putInt(videoTrackID);
            bb1.put("avc1".getBytes());
            bb1.putInt(0x01f3); // FIXME
            bb1.putInt(0);
            bb1.putInt(muxvb1 / 1000);
            bb1.putInt(muxvb2 / 1000);
            bb1.putInt((int) Math.round(vidr * 65536));
            bb1.putInt((int) Math.round(vidr * 65536));
            if (forceWidth == 0) {
                bb1.putShort((short) vidw);
            } else {
                bb1.putShort((short) forceWidth);
            }
            if (forceHeight == 0) {
                bb1.putShort((short) vidh);
            } else {
                bb1.putShort((short) forceHeight);
            }
            bb1.putShort((short) 1);
            bb1.putShort((short) 1);
            bb1.flip();

            bb.putInt(bb1.limit() + 4);
            bb.put(bb1);

        } else {
            throw new IllegalStateException("A>変換中のファイルにビデオトラックが見つかりません。正常に再生できない可能性があります。");
        }

        bb.flip();

        InsertItem insertItem = new InsertItem();
        insertItem.insertPath = "\\";
        insertItem.last = false;
        insertItem.item = new byte[bb.limit()];

        bb.get(insertItem.item);

Debug.println("inserting: " + insertItem.insertPath + "\n" + StringUtil.getDump(insertItem.item));
        return insertItem;
    }

    /** */
    private InsertItem getMoovTrakUuid(String uuid) {
        ByteBuffer bb = ByteBuffer.allocate(256);
        ByteBuffer bb1 = ByteBuffer.allocate(256);

        bb1.put("MTDT".getBytes());
        bb1.putShort((short) 0x01);
        bb1.putShort((short) 0x12);
        bb1.putInt(0x0a);
        bb1.putShort((short) 0x55c4); // und
        bb1.putShort((short) 0);
        bb1.putShort((short) 0);
        bb1.putShort((short) 1);
        bb1.putShort((short) 0);
        bb1.putShort((short) 0);
        bb1.flip();

        bb.put("uuid".getBytes());
        bb.put("USMT".getBytes());
        bb.put(uuid.getBytes());

        bb.putInt(bb1.limit() + 4);
        bb.put(bb1);
        bb.flip();

        InsertItem insertItem = new InsertItem();
        insertItem.insertPath = "\\moov\\trak\\";
        insertItem.last = true;
        insertItem.item = new byte[bb.limit()];

        bb.get(insertItem.item);

Debug.println("inserting: " + insertItem.insertPath + "\n" + StringUtil.getDump(insertItem.item));
        return insertItem;
    }

    /**
     * @return
     */
    private InsertItem getMoov(String daystr) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(256);
        ByteBuffer bb1 = ByteBuffer.allocate(256);
        ByteBuffer bb2 = ByteBuffer.allocate(256);
        ByteBuffer bb3 = ByteBuffer.allocate(256);

        bb.put("udta".getBytes());

        if (iTunesMode) {
            // ilst
            bb1.put("ilst".getBytes());

            // ilst-name
            bb3.clear();
            bb3.put("data".getBytes());
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 1);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put(title.getBytes("UTF-8"));
            bb3.flip();

            bb2.clear();

            bb2.putInt(bb3.limit() + 4);
            bb2.put(bb3);
            bb2.flip();

            bb3.clear();

            bb3.put(((char) 0xa9 + "nam").getBytes());
            bb3.put(bb2);
            bb3.flip();

            bb1.putInt(bb3.limit() + 4);
            bb1.put(bb3);

            // ilst-tool
            bb3.clear();
            bb3.put("data".getBytes());
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 1);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put((byte) 0);
            bb3.put(APPSTR.getBytes("UTF-8"));
            bb3.flip();

            bb2.clear();

            bb2.putInt(bb3.limit() + 4);
            bb2.put(bb3);
            bb2.flip();

            bb3.clear();

            bb3.put(((char) 0xa9 + "too").getBytes());
            bb3.put(bb2);
            bb3.flip();

            bb1.putInt(bb3.limit() + 4);
            bb1.put(bb3);
            bb1.flip();

            // meta
            bb2.clear();
            bb2.put("meta".getBytes());
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            // meta-hdlr
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0x22);
            bb2.put("hdlr".getBytes());
            bb2.put((byte) 0); //
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0); //
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put("mdir".getBytes());
            bb2.put("appl".getBytes());
            bb2.put((byte) 0); //
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0); //
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0); //
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0);
            bb2.put((byte) 0); //
            bb2.put((byte) 0);
            // meta-ilst
            bb2.putInt(bb1.limit() + 4);
            bb2.put(bb1);
            bb2.flip();
            //
            bb.putInt(bb2.limit() + 4);
            bb.put(bb2);
            bb.flip();

        } else {

            bb.put("udta".getBytes());

            bb1.clear();
            bb1.put(((char) 0xa9 + "nam").getBytes());
            bb1.putShort((short) title.length());
            bb1.put((byte) 0);
            bb1.put((byte) 0);
            bb1.put(title.getBytes());
            bb1.flip();

            bb.putInt(bb1.limit() + 4);
            bb.put(bb1);

            bb1.clear();
            bb1.put(((char) 0xa9 + "day").getBytes());
            bb1.putShort((short) daystr.length());
            bb1.put((byte) 0);
            bb1.put((byte) 0);
            bb1.put(daystr.getBytes());
            bb1.flip();

            bb.putInt(bb1.limit() + 4);
            bb.put(bb1);

            bb1.clear();
            bb1.put(((char) 0xa9 + "swr").getBytes());
            bb1.putShort((short) APPSTR.length());
            bb1.put((byte) 0);
            bb1.put((byte) 0);
            bb1.put(APPSTR.getBytes());
            bb1.flip();

            bb.putInt(bb1.limit() + 4);
            bb.put(bb1);

            bb.put((byte) 0);
            bb.put((byte) 0);
            bb.put((byte) 0);
            bb.put((byte) 0x08);

            bb.put("mqds".getBytes());
            bb.flip();
        }

        InsertItem insertItem = new InsertItem();
        insertItem.insertPath = "\\moov\\";
        insertItem.last = true;
        insertItem.item = new byte[bb.limit()];

        bb.get(insertItem.item);

Debug.println("inserting: " + insertItem.insertPath + "\n" + StringUtil.getDump(insertItem.item));
        return insertItem;
    }

    /** */
    private void initInsertItems(Properties props, String uuid, String ts_s) {
        insertItems.clear();
        int i = 0;
        String s = "-";
        while (!s.equals("")) {
            s = props.getProperty("InsertAtom.Item" + i, "");
            if (!s.equals("")) {
                InsertItem insertItem = new InsertItem();
                s = s.replace("<%UUID%>", uuid);
                s = s.replace("<%TIMESTAMP%>", ts_s);
                insertItem.last = s.charAt(0) == '/'; // /で始まる場合Last
                insertItem.insertPath = "\\";
                // ポジション指定
                while (s.charAt(0) == '\\' || s.charAt(0) == '/') {
                    s = s.substring(1, s.length());
                    String s1 = s;
                    String s2 = decodeURL(s1, 4);
                    if (s1.charAt(0) == '\\' || s1.charAt(0) == '/') {
                        insertItem.insertPath = insertItem.insertPath + s2 + '\\';
                        s = s1;
                    }
                }
                insertItem.item = decodeURL(s).getBytes();
                insertItems.add(insertItem);
Debug.println("inserting: " + insertItem.insertPath + "\n" + StringUtil.getDump(insertItem.item));
            }
            i++;
        }
    }

    /** */
    private void initDeleteItems(Properties props) {
        deleteItems = new ArrayList<String>();
        deleteItems.clear();
        int i = 0;
        String s = "-";
        while (!s.equals("")) {
            s = props.getProperty("DeleteAtom.Item" + i, "");
            if (!s.equals("")) {
                deleteItems.add(decodeURL(s));
Debug.println("deleting: " + decodeURL(s));
            }
            i++;
        }
        if (mfraMode != 0) {
            deleteItems.add("mfra");
Debug.println("deleting: " + "mfra");
        }
        if (edtsMode) {
            deleteItems.add("edts");
Debug.println("deleting: " + "edts");
        }
    }

    /** */
    private void printUsage(String appName) {
        System.err.println("");
        System.err.println("Usage:");
        System.err.println(appName + " [inFile] [outFile] ([iniFile] ([Title]))");
        System.err.println("  inFileを読み込み、atomを書き換えoutFileに保存します。");
        System.exit(1);
    }
}

/* */
