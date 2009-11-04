/*
 * http://www.nurs.or.jp/~calcium/3gpp/sources/
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.io.IOConstants;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.comp.Component;
import quicktime.std.comp.ComponentDescription;
import quicktime.std.comp.ComponentIdentifier;
import quicktime.std.movies.Atom;
import quicktime.std.movies.AtomContainer;
import quicktime.std.movies.AtomData;
import quicktime.std.movies.Movie;
import quicktime.std.movies.Track;
import quicktime.std.qtcomponents.MovieExporter;

import vavi.util.Debug;
import vavi.util.win32.WindowsProperties;


/**
 * QT3GPPFlatten.
 * <p>
 * CAUTION! this program run on 32bit only 
 *
 * @author mobilehackerz
 * @see "http://www.nurs.or.jp/~calcium/3gpp/sources/"
 */
class QT3GPPFlatten {

    /** */
    private static final String VERSION = "0.08";
    /** spit */
    private static final int MovieExportType = toType("spit");
    /** TVOD */
    private static final int FCC_TVOD = toType("TVOD");
    /** 3gpp */
    private static final int _3GPPFileType = toType("3gpp");

    /** */
    private int dialogType;

    /** */
    private List<String> containItem;

    /**
     * atom のファイルへの書き込み
     */
    private void writeAllAtom(Properties props, AtomContainer container, Atom parent, String dirPos) {
Debug.println("parent: " + parent + ", dirPos: " + dirPos);

        Atom currentAtom = null;
        boolean flag = true;
        int i = 0;
        while (flag) {
            try {
                Atom atom = container.nextChildAnyType(parent, currentAtom);
                if (atom == null) {
                    flag = false;
                } else {
                    // atmの情報を取得
                    int /*QTAtomType*/ atomType = container.getAtomType(atom);
                    int /* QTAtomID */ id = container.getAtomID(atom);
                    AtomData atomData = container.getAtomData(atom);
                    long size = container.getAtomDataSize(atom);
                    String s0 = dirPos + ':' + i;
                    String s1 = toFourCC(atomType);
                    StringBuilder s2 = new StringBuilder();
                    byte[] p = atomData.getBytes();
                    for (int j = 0; j < size; j++) {
                        s2.append(String.format("%02X", p[j]));
                    }

                    // 親の情報を更新
                    props.setProperty(dirPos + ".childs", String.valueOf(i + 1));
                    // 子の情報を書き込み
                    props.setProperty(s0 + ".atom", s1);
                    props.setProperty(s0 + ".id", String.valueOf(id));
                    props.setProperty(s0 + ".size", String.valueOf(size));
                    props.setProperty(s0 + ".data", s2.toString());

                    // 子を検索
                    writeAllAtom(props, container, atom, s0);
                    // 次へ
                    currentAtom = atom;
                }
            } catch (StdQTException e) {
                flag = false;
            }
            i++;
        }
    }

    /**
     * atom のファイルからの読み込み
     */
    private void readAllAtom(Properties props, AtomContainer container, Atom parent, String dirPos) throws QTException {
Debug.println("parent: " + parent + ", dirPos: " + dirPos);
        String atom = props.getProperty(dirPos + ".atom");
        if (atom != null) {
            // atomを追加
            int  id = Integer.parseInt(props.getProperty(dirPos + ".id", "1"));
            int  size = Integer.parseInt(props.getProperty(dirPos + ".size", "0"));
            String data = props.getProperty(dirPos + ".data", "");
Debug.println("id: " + id + ", size: " + size + ", data: " + data);
            byte[] p = null;
//            if (size != 0) {
                p = new byte[size];
//            }
            for (int j = 0; j < size; j++) {
                p[j] = (byte) Integer.parseInt(data.substring(j * 2, j * 2 + 1), 16);
            }
            int /* QTAtomType */ atomType = toType(atom.substring(0, 4));
            Atom newAtom = container.insertChild(parent, atomType, id, 0, p);
            parent = newAtom;
        }

        // 子を検索
        int n = Integer.parseInt(props.getProperty(dirPos + ".childs", "0"));
        for (int i = 0; i < n; i++) {
            readAllAtom(props, container, parent, dirPos + ':' + i);
        }
    }

    /** */
    private static int toType(String atom) {
        try {
Debug.println("atom: [" + atom + "], " + toFourCC(ByteBuffer.wrap(atom.getBytes("ISO-8859-1")).asIntBuffer().get()));
            return ByteBuffer.wrap(atom.getBytes("ISO-8859-1")).asIntBuffer().get();
        } catch (UnsupportedEncodingException e) {
            assert false : e.getMessage();
            return 0;
        }
    }

    /**
     * 使える Export Type をリストアップする
     */
    private static String toFourCC(int data) {
        try {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(data);
            return new String(bb.array(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            assert false : e.getMessage();
            return null;
        }
    }

    /** */
    public void listType() throws QTException {

        QTSession.initialize(0); // Initialize QTML
        QTSession.enterMovies(); // Initialize QuickTime
        try {
            ComponentDescription looking = new ComponentDescription();
            looking.setType(MovieExportType);
            looking.setSubType(0);
            looking.setManufacturer(0);
            looking.setFlags(0);
            looking.setMask(0);
            ComponentIdentifier foundComponent = null;
            foundComponent = ComponentIdentifier.find(foundComponent, looking);
            while (foundComponent != null) {
                ComponentDescription found = foundComponent.getInfo();
                System.out.println(toFourCC(found.getSubType()) + ' ');
                foundComponent = ComponentIdentifier.find(foundComponent, looking);
            }
        } finally {
            QTSession.exitMovies(); // Terminate QuickTime
            QTSession.terminate(); // Terminate QTML
        }
    }

    /**
     * 一番シンプルな ConvertMovieToFile
     */
    public int convertMovieToFileSimple(String inFile) throws QTException {
        
        OpenMovieFile omf;
//        short mRes;
//        short ResID;
//        String MName;
//        boolean chg;
        int result = 0;
        QTSession.initialize(0); // Initialize QTML
        QTSession.enterMovies(); // Initialize QuickTime
        try {
            // Open Movie
            Movie mov = null;
            QTFile fs = new QTFile(inFile);
            QTFile outfs = new QTFile(inFile);
            try {
                omf = OpenMovieFile.asRead(fs);
            } catch (QTException e) {
                System.err.println("A>ConvertMovieToFile:OpenMovieFile Error");
                result = e.errorCode();
                return result;
            }
//            ResID = 0;
            try {
                mov = Movie.fromFile(omf); //, mRes, ResID, MName, StdQTConstants.newMovieActive, chg);
                omf.close();
            } catch (QTException e) {
e.printStackTrace();
                System.err.println("A>ConvertMovieToFile:NewMovieFromFile Error");
                result = e.errorCode();
                return result;
            }

            // ConvertMovie
            if (mov != null) {
//                mRes = 0;
                try {
                    mov.convertToFile((Track) null, outfs, 0, FCC_TVOD, IOConstants.smSystemScript, StdQTConstants.createMovieFileDeleteCurFile | StdQTConstants.showUserSettingsDialog | StdQTConstants.movieFileSpecValid | StdQTConstants.movieToFileOnlyExport, null);
                } catch (QTException e) {
                    System.err.println("A>ConvertMovieToFile:ConvertMovieToFile Error");
                    result = e.errorCode();
                    return result;
                }
            }

            return 0;
        } finally {
            QTSession.exitMovies(); // Terminate QuickTime
            QTSession.terminate(); // Terminate QTML
        }
    }

    /**
     * 設定つき
     */
    public int convertMovieToFile(String inFilename, String outFilename, Properties props) throws IOException, QTException {
        
        OpenMovieFile omf;
//        short mRes;
//        short ResID;
//        String MName;
        AtomContainer acon;
        int result = 0;
        QTSession.initialize(0); // Initialize QTML
        QTSession.enterMovies(); // Initialize QuickTime
        ComponentIdentifier ci = null;
        try {
            // Open Movie
            Movie mov = null;
            QTFile fs = new QTFile(inFilename);
Debug.println("inFilename: " + inFilename);
            try {
                omf = OpenMovieFile.asRead(fs);
            } catch (QTException e) {
e.printStackTrace();
                System.err.println("A>ConvertMovieToFile:OpenMovieFile Error");
                result = e.errorCode();
                return result;
            }
//            ResID = 0;
            try {
                mov = Movie.fromFile(omf); // , mRes, ResID, MName, StdQTConstants.newMovieActive, flg);
                omf.close();
            } catch (QTException e) {
e.printStackTrace();
                System.err.println("A>ConvertMovieToFile:NewMovieFromFile Error");
                result = e.errorCode();
                return result;
            }

            // ConvertMovie
            if (mov != null) {
                // Check INi File
                boolean dialogFlag = true;
                if (props != null) {
                    if (Integer.parseInt(props.getProperty("0.childs", "-1")) < 0) {
                        dialogFlag = true;
                    } else {
                        dialogFlag = false;
                    }
                }

                // Configurate Exporter
                ci = new Component(MovieExportType, dialogType);
                if (ci == null) {
                    System.err.println("A>ConvertMovieToFile:OpenDefaultComponent Error");
                    result = -1;
                    return result;
                }
                MovieExporter exp = new MovieExporter(ci);
                boolean saveFlag = false;
                if (!dialogFlag) {
                    // ダイアログの表示
                    boolean flag = exp.doUserDialog(mov, null, 0, 0);
                    if (flag) {
                        System.err.println("A>ConvertMovieToFile:User cancelled");
                        result = -2;
                        return result;
                    }
                    saveFlag = true;
                } else {
                    // INIファイルの読み込み
                    acon = new AtomContainer();
                    readAllAtom(props, acon, Atom.kParentIsContainer, "0");
                    exp.setExportSettingsFromAtomContainer(acon);
                    acon.disposeQTObject();
                }

                // Save INI File
                if (saveFlag && props != null) {
                    acon = exp.getExportSettingsFromAtomContainer();
                    writeAllAtom(props, acon, Atom.kParentIsContainer, "0");
                    acon.disposeQTObject();
                }

                // Do Conversion
//                mRes = 0;
                mov.setActive(true);
Debug.println("outFilename: " + outFilename);
                QTFile outfs = new QTFile(outFilename);
                try {
                    mov.convertToFile(null, outfs, 0, FCC_TVOD, IOConstants.smSystemScript, StdQTConstants.createMovieFileDeleteCurFile, exp);
                } catch (QTException e) {
e.printStackTrace(System.err);
                    System.err.println("A>ConvertMovieToFile:ConvertMovieToFile Error");
                    result = e.errorCode();
                    return result;
                }
            }

            return 0;
        } finally {
            if (ci != null) {
                ci.disposeQTObject();
            }
            QTSession.exitMovies(); // Terminate QuickTime
            QTSession.terminate(); // Terminate QTML
        }
    }

    /**
     * Flatten
     */
    public int flatten(String inFile, String outFile) throws QTException {
        
        OpenMovieFile omf;
//        short mRes;
//        short ResID;
//        String MName;
//        boolean chg;
        int result = 0;
        QTSession.initialize(0); // Initialize QTML
        QTSession.enterMovies(); // Initialize QuickTime
        try {
            // Open Movie
            Movie mov = null;
            QTFile fs = new QTFile(inFile);
            QTFile outfs = new QTFile(outFile);
            try {
                omf = OpenMovieFile.asRead(fs);
            } catch (QTException e) {
                System.err.println("A>QT3GPP_Flatten:OpenMovieFile Error");
                result = e.errorCode();
                return result;
            }
//            ResID = 0;
            try {
                mov = Movie.fromFile(omf); // , mRes, ResID, MName, StdQTConstants.newMovieActive, chg);
                omf.close();
            } catch (QTException e) {
                System.err.println("A>QT3GPP_Flatten:NewMovieFromFile Error");
                result = e.errorCode();
                return result;
            }

            // FlattenMovie
            if (mov != null) {
//                mRes = 0;
                try {
                    mov.flattenData(StdQTConstants.flattenAddMovieToDataFork | StdQTConstants.flattenForceMovieResourceBeforeMovieData, outfs, FCC_TVOD, IOConstants.smSystemScript, StdQTConstants.createMovieFileDeleteCurFile);
                } catch (QTException e) {
                    System.err.println("A>QT3GPP_Flatten:FlattenMovie Error");
                    result = e.errorCode();
                    return result;
                }
            }

            return 0;
        } finally {
            QTSession.exitMovies(); // Terminate QuickTime
            QTSession.terminate(); // Terminate QTML
        }
    }

    /**
     * テンポラリファイル名の生成
     */
    private String getTempName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%08X", random.nextInt()));
            if (i != 3) {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    /**
     * CreationTimeの 読み出し
     */
    private long scanCreationTime(RandomAccessFile in, long max, long creationTime) throws IOException {
        
        long startPointer = in.getFilePointer();
        long pointer = 0;

        while (pointer < max) {
            int size = in.readInt();

            // size = 0 異常 ... atom 検索終了
            if (size < 8) {
                in.seek(startPointer + max);
                pointer = max;
            } else {
                byte[] ch = new byte[4];
                in.readFully(ch, 0, 4);
                String atom = new String(ch, 0, 4);

                // Atom チェック
                boolean contain = containItem.contains(atom);

                // 実処理
                if (contain && size >= 16) {
                    creationTime = scanCreationTime(in, size - 8, creationTime);
                } else {
                    boolean exec = false;

                    if (atom.equals("mvhd")) {
                        byte[] buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;
                        creationTime = ByteBuffer.wrap(buf, 4, 4).getInt() * 1000;
                    }

                    // それ以外 → スキップ
                    if (!exec) {
                        in.skipBytes(size - 8);
                    }
                }

                pointer = in.getFilePointer() - startPointer;
            }
        }

        return creationTime;
    }

    /**
     * CreationTimeの書き込み
     */
    private void writeCreationTime(RandomAccessFile in, long max, long creationTime) throws IOException {

        long startPointer = in.getFilePointer();
        long pointer = 0;

        while (pointer < max) {
            int size = in.readInt();

            // size = 0 異常 ... atom 検索終了
            if (size < 8) {
                in.seek(startPointer + max);
                pointer = max;
            } else {
                byte[] ch = new byte[4];
                in.readFully(ch, 0, 4);
                String atom = new String(ch, 0, 4);

                // Atom チェック
                boolean contain = containItem.contains(atom);

                // 実処理
                if (contain && size >= 16) {
                    writeCreationTime(in, size - 8, creationTime);
                } else {
                    boolean exec = false;

                    if (atom.equals("mvhd") || atom.equals("tkhd") || atom.equals("mdhd")) {
                        byte[] buf = new byte[size - 8];
                        in.readFully(buf, 0, size - 8);
                        exec = true;

                        ByteBuffer bb = ByteBuffer.wrap(buf, 4, 8);
                        // creationTime
                        bb.putInt((int) (creationTime / 1000));
                        // ModificationTime
                        bb.putInt((int) (creationTime / 1000));

                        in.seek(in.getFilePointer() - (size - 8));
                        in.write(buf, 0, size - 8);
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
     * メインルーチン
     */
    public static void main(String[] args) throws Exception {
        new QT3GPPFlatten(args);
    }

    /** */
    private Random random = new Random(System.currentTimeMillis());

    /** */
    QT3GPPFlatten(String[] args) throws IOException, QTException {
        
        // コマンドライン処理
        String exeName = "";
        if (args.length >= 0) {
            exeName = this.getClass().getName();
        }
        String appName = exeName.substring(exeName.lastIndexOf('.') + 1);

        // Opening
        System.err.println(appName + " for 3GP_Converter Version " + VERSION);
        System.err.println("http://www.nurs.or.jp/~calcium/");

        // 初期値
        boolean optUsage = false;
        boolean optIniFile = false;
        boolean needRename = false;
        boolean onlyFlatten = false;
        String inputFile = null;
        String outputFile = null;
        String trueOutput = null;
        String iniFile = null;
        dialogType = _3GPPFileType;
        long creationTime = 0;

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
        
        for (int i = 0; i < args.length; ) {
            String s = args[i];
            i++;
            if (s.charAt(0) == '-') {
                // Option
                s = s.substring(1).toUpperCase();
                if (s.equals("C")) {
                    // IniFile
                    if (i < args.length) {
                        iniFile = args[i];
                        i++;
                        optIniFile = true;
                    } else {
                        optUsage = true;
                    }
                } else {
                    if (s.equals("T")) {
                        if (i < args.length) {
                            s = args[i];
                            i++;
                            if (s.length() > 4) {
                                optUsage = true;
                            } else {
                                s = (s + "    ").substring(0, 4);
                                dialogType = toType(s);
                            }
                        }
                    } else {
                        if (s.equals("F")) {
                            onlyFlatten = true;
                        } else {
                            // Error
                            optUsage = true;
                        }
                    }
                }
            } else {
                // fileName
                if (inputFile == null) {
                    inputFile = s;
                } else {
                    if (outputFile == null) {
                        outputFile = s;
                    } else {
                        // Error
                        optUsage = true;
                    }
                }
            }
        }
        if (inputFile == null) {
            optUsage = true;
        }

        // 処理
        if (optUsage) {
            System.err.println("");
            System.err.println("Usage:");
            System.err.println(appName + " [inFile]");
            System.err.println("  inFileを読み込み、保存先と処理内容をダイアログで指定し保存します");
            System.err.println("");
            System.err.println(appName + " [inFile] [outFile]");
            System.err.println("  inFileを読み込み、処理内容をダイアログで指定しoutFileに保存します");
            System.err.println("");
            System.err.println(appName + " [inFile] [outFile] -f");
            System.err.println("  inFileを読み込み、outFileに保存します");
            System.err.println("");
            System.err.println(appName + " [inFile] [outFile] -c [iniFile] -t [type]");
            System.err.println("  inFileを読み込み、iniFileに従った処理をしoutFileに保存します");
            System.err.println("  iniFileが見つからなかった場合は処理内容を指定するダイアログを表示し、");
            System.err.println("  設定内容をiniFileに保存します。");
            System.err.println("  typeを指定した場合は処理内容ダイアログの種別を変更できます。");
            System.err.println("");
            System.err.println("[type]の一覧：");
            listType();
            System.err.println("");
            System.exit(1);
        }

        int r;
        if (outputFile == null) {
            r = convertMovieToFileSimple(inputFile);
        } else {
            // outputFileの加工
            // テンポラリなファイル名にする
            String s1 = new File(outputFile).getParent();
            String s2 = getTempName();
            String s3 = outputFile.substring(outputFile.lastIndexOf('.') + 1);
            trueOutput = outputFile;
            outputFile = s1 + File.separator + s2 + "." + s3;
            needRename = true;

            if (onlyFlatten) {
                r = flatten(inputFile, outputFile);
            } else {
                if (!optIniFile) {
                    r = convertMovieToFile(inputFile, outputFile, null);
                } else {
                    if (iniFile.charAt(1) != ':' && iniFile.charAt(0) != '\\') {
                        iniFile = exeName.substring(0, exeName.lastIndexOf(File.separator)) + iniFile;
                    }
                    Properties props = null;
                    if (iniFile != "") {
                        props = new WindowsProperties();
                        props.load(new FileInputStream(iniFile));
                    }
                    r = convertMovieToFile(inputFile, outputFile, props);
                }
            }

            // inFileからoutFileにCreationTimeをコピー
            RandomAccessFile file = new RandomAccessFile(inputFile, "r");
            if (file != null) {
                long max = file.length();
                file.seek(0);
                creationTime = scanCreationTime(file, max, 0);
                file.close();
            }
            if (creationTime != 0) {
                file = new RandomAccessFile(outputFile, "rw");
                if (file != null) {
                    long max = file.length();
                    file.seek(0);
                    writeCreationTime(file, max, creationTime);
                    file.close();
                }
            }
        }

        if (needRename) {
            if (new File(trueOutput).exists()) {
                new File(trueOutput).delete();
            }
Debug.println("trueOutput: " + trueOutput);
            new File(outputFile).renameTo(new File(trueOutput));
        }
Debug.println("DONE: " + r);
        System.exit(r);
    }
}

/* */

