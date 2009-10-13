/*
 * (c) イガラシヒロアキ
 */

using System;
using System.Threading;

namespace PuyoPuyo {

	/// <summery>
	/// PuyoPuyo
	/// <p>
	/// コンピュータは基本的に3連鎖以上しかしないようにしてあります。
	/// ただし、フィールド上のぷよの数が一定数を超えるとぷよを次々に消していくモードに切り替わり、
	/// ぷよの数が一定数を下回ると通常のモードに戻ります。
	/// </p><p>
	/// コンピュータは計画的にぷよを積んでいるというわけではなく、行き当たりばったりです。
	/// Nextぷよを参照した後、各々の落とし方についてパラメータを計算し、パラメータの優先順位から落とし方を決定します。
	/// パラメータは全部で15種類あり、それぞれの落とし方に対してパラメータを優先順位の高い順に比較していき、
	/// 最初に、あるパラメータが一方よりも大きくなった方を採用するようにしています。
	/// </p><p>
	/// コンピュータはぷよの積み方に計画性がないため、序盤は圧倒的に人間が有利になります。
	/// 逆に、ぷよがフィールド上に溜まってくると、計算に強いコンピュータが有利になります。
	/// したがって、コンピュータに勝つためには、序盤にぷよを計画的に積み、コンピュータよりも早く大きな連鎖を完成させることです。
	/// 4連鎖を仕掛けた後で2連鎖でチマチマ攻撃していけば勝てます。多分。
	/// </p>
	///
	/// @author イガラシヒロアキ
	/// @see "http://www7.plala.or.jp/isaragi/Lisu-ca/java/puyo/"
	/// </summery>
	public class PuyoPuyo {
	
	    /** */
	    public interface View {
	        /** */
	        void repaint();
	        /** */
	        void playClip(int i);
	        /** */
	        void play(int folder, int cn);
	        /** */
	        void stopClips();
	    }
	
	    /** */
	    private View view;
	
	    /** */
	    public void setView(View view) {
	        this.view = view;
	    }
	
	    /** */
	    public class Stage {
	        public Stage(int playersCount) {
	            this.playersCount = playersCount;
	            games = new PuyoPuyo[playersCount];
	            disturbCounts = new int[playersCount];
	            rankings = new int[playersCount];
	            gameFlags = new int[playersCount];
	        }
	
	        /** */
	        public void init() {
	            leftPos = 2;
	            lows = 14;
	            columns = 6;
	            ranking = playersCount;
	            for (int i = 0; i < playersCount; i++) {
	                disturbCounts[i] = 0;
	                rankings[i] = 0;
	                gameFlags[i] = 0;
	            }
	            // Nextぷよ
	            if (puyoFlag == 1) {
	                int puyolist_count = (PuyoPuyo.colors.Length - 1) * 50;
	                puyoList = new int[puyolist_count];
	                for (int i = 0; i < puyolist_count; i++) {
	                    puyoList[i] = (i % (PuyoPuyo.colors.Length - 1)) + 2;
	                }
	                for (int i = 0; i < puyolist_count; i++) {
	                    int a = (int) random.Next(puyolist_count);
	                    int b = (int) random.Next(puyolist_count);
	                    int c = puyoList[a];
	                    puyoList[a] = puyoList[b];
	                    puyoList[b] = c;
	                }
	            }
	        }
	
	        /** プレイ人数 */
	        public int playersCount;
	
	        public PuyoPuyo[] games;
	
	        /** 1: 初期ぷよ有り */
	        public int setFlag;
	        /** */
	        public int soundFlag;
	        /** 1: ぷよ譜, else: ランダム */
	        public int puyoFlag;
	        /** */
	        public int skipFlag = 0;
	        /** 1: 底まで落とす */
	        public int sss = 1;
	        /** */
	        public int ef1 = 0;
	        /** */
	        public int ef2 = 0;
	        /** */
	        public int many = 0;
	
	        /** ぷよの落ちてくる位置 (0 ~ 5) ※ 0 or 5 だと自動モードに不具合発生 */
	        public int leftPos;
	        /** フィールドの縦幅 */
	        public int lows;
	        /** フィールドの横幅 */
	        public int columns;
	        /** 順位 */
	        public int ranking;
	
	        /** おじゃま数カウント */
	        public int[] disturbCounts;
	        /** ゲーム中か判定する配列 */
	        public int[] gameFlags;
	        /** 順位用配列 */
	        public int[] rankings;
	        /** */
	        public int[] puyoList;
	    }
	
	    /** 色 */
	    private static string[] colors = {
	        "gray", "red", "yellow", "blue", "green", "purple"
	    };
	
	    /** */
	    private Stage stage;
	
	    /** */
	    private Thread autoFallTask;
	    /** */
	    private Thread otherTask;
	
	    /** */
	    private int id;
	    /** */
	    private Grid gridObject;
	    /** */
	    private int puyoListSize;
	
// 動作テスト用
public string x;
//int[] cc = new int[5];
//int cc_sum;
	
	    /** オートモード関連 */
	    private int mtime;
	    /** オートモードでの回転方向 */
	    private int rotateDirection;
	    /** オートモードでの回転の仕方 */
	    private int autoRotate;
	    /** オートモードでの列番号 */
	    private int autoLow;
	
	    /** ぷよナンバー */
	    public int puyo1;
	    /** */
	    public int puyo2;
	    /** */
	    public int npuyo1;
	    /** */
	    public int npuyo2;
	    /** */
	    public int nnpuyo1;
	    /** */
	    public int nnpuyo2;
	    /** 他 */
	    private int randomDisturbPos;
	    /** スレッドカウント */
	    private int fallThreadCount;
	    /** */
	    private int eraseNumber;
	
	    /** 連結数 */
	    private int connectCount;
	    /** 回転ラベル */
	    private int rotationPosition;
	    /** */
	    public string overMessage;
	    /** スリープ後実行処理 */
	    private String sleepMode;
	    /** スリープ用時間 */
	    private int sleepTime;
	
	    // 連鎖関連
	
	    /** 連鎖数 */
	    public int chainCount;
	    /** 発火ぷよ発火による連鎖数 */
	    private int ignitionChainCount;
	    /** それまでの最大連鎖数を保持 */
	    private int maxChainCount;
	    /** 連鎖メッセージ */
	    public string message;
	    /** 連鎖の声のフォルダ */
	    private int folder;
	
	    // ぷよ関連のスピード
	
	    /** 落下 */
	    public static int FallSpeed = 1500;
	    /** 左右移動 */
	    private static int MoveSpeed = 50;
	    /** 回転 */
	    private static int RotaSpeed = 100;
	
	    /** 判定するための待ってるか判定 */
	    public int waitFlag;
	    /** 連鎖する可能性があるか判定 */
	    private int chainFlag;
	    /** 埋まるかどうか判定 */
	    private int notBuryFlag;
	    /** 連結数が3以上の箇所が見つかったか判定 */
	//    private int connect3Flag;
	    /** ちぎったか判定 */
	//    private int tearFlag;
	    /** オートモードか判定 */
	    public int autoFlag;
	    /** オートモードで判定中か判定 */
	    private int judgeFlag;
	    /** オートモードで発火ぷよ判定中か判定 */
	    private int ignitionJudgeFlag;
	
	    /** おじゃま */
	    private int disturbCount;
	    /** */
	    private int tempDisturbCount;
	    /** */
	    private int damDisturbCount;
	    /** */
	    private int disturbRest;
	    /** */
	    private int disturbRate;
	    /** */
	    private int disturbFlag;
	    /** */
	    private int disturbHeight;
	    /** */
	    private int damDisturbHeight;
	    /** */
	    private int disturbHeightRest;
	
	    // 得点用A-B-C-D
	
	    /** @public */
	    public int score;
	    /** */
	    private int dScore;
	    /** */
	    private int A;
	    /** */
	    private static int[] B = {
	        0, 4, 20, 24, 32,
	        48, 96, 160, 240,
	        320, 480, 600, 700,
	        800, 900, 999
	    };
	    /** */
	    private static int[] C = {
	        0, 2, 3, 4,
	        5, 6, 7, 10
	    };
	    /** */
	    private int cMax;
	    /** */
	    private static int[] D = {
	        0, 3, 6, 12, 24
	    };
	    /** */
	    private int dSum;
	    /** score */
	    private int bcd;
	
	    // 各種配列
	
	    /** @public マス目 */
	    public int[,] grid;
	    /** ダミーのマス目積んだ直後 */
	    private int[,] dummyGrid;
	    /** 連鎖終了後 */
	    private int[,] preDummyGrid;
	    /** 連鎖時ラベル用 */
	    private int[,] connectionLabel;
	    /** どれくらい落ちたのか */
	    private int[,] fallLabel;
	    /** 連鎖ラベル */
	    private int[,] chainLabel;
	    /** @public */
	    public int[,] lastChainLabel;
	    /** */
	    private int[,] preChainLabel;
	    /** 発火ぷよラベル */
	    private int[,] ignitionLabel2;
	    /** @public */
	    public int[,] lastIgnitionLabel2;
	    /** 列に色ぷよがあるか */
	    private int[,] colorList;
	    /** 連結数保存用 */
	    private int[,] connectionNumber;
	    /** 落ちてきたぷよの座標 */
	    public int[,] pos = new int[2, 2];
	    /** それぞれの色ぷよの消した個数 */
	    private int[] erasedNumber = new int[colors.Length - 1];
	    /** 連鎖回数保存用 */
	    private int[] chainNumber = new int[5];
	    /** 色ごとに消しぷよ数を保存する用 (一時的に) */
	    private int[] erasedPuyoList = new int[colors.Length - 1];
	    /** 落下パターン優先順位 */
	    private int[] params_ = new int[20];
	    private int[] tempParams;
	    private int[] paramLabels;
	    private string[] paramList;
	
	    // 連鎖メッセージ
	    private static string[] voice1 = {
	        "いてっ", "やったなー", "げげげっ", "大打撃っ",
	        "ふにゃぁ〜", "いててててて", "うわぁぁぁ〜"
	    };
	
	    private static string[] voice2 = {
	        "えいっ", "ファイヤー", "アイスストーム", "ダイアキュート",
	        "ブレインダムド", "じゅげむ", "ばよえ〜ん"
	    };
	
	    private string[] voice;

        private static Random random = new Random(Environment.TickCount);

	    /** コンストラクタ */
	    public PuyoPuyo(Stage stage, int id) {
	
	        this.stage = stage;
	
	        // スレッド
	        autoFallTask = new Thread(doAutoFall);
	        otherTask = new Thread(doOther);
	
	        grid = new int[stage.lows, stage.columns];
	        dummyGrid = new int[stage.lows, stage.columns];
	        preDummyGrid = new int[stage.lows, stage.columns];
	        connectionLabel = new int[stage.lows, stage.columns];
	        fallLabel = new int[stage.lows, stage.columns];
	        chainLabel = new int[stage.lows, stage.columns];
	        lastChainLabel = new int[stage.lows, stage.columns];
	        preChainLabel = new int[stage.lows, stage.columns];
	        ignitionLabel2 = new int[stage.lows, stage.columns];
	        lastIgnitionLabel2 = new int[stage.lows, stage.columns];
	        colorList = new int[stage.columns, colors.Length - 1];
	        connectionNumber = new int[stage.lows, stage.columns];
	
		    tempParams = new int[params_.Length];
		    paramLabels = new int[params_.Length];
		    paramList = new string[params_.Length];

	        //
	        this.id = id;
	        if (id == 0) {
	            // プレイヤー
	            voice = voice2;
	            folder = 1;
	        } else {
	            // コンピューター
	            voice = voice1;
	            folder = 2;
	        }
	        // Gridオブジェクト生成
	        gridObject = new Grid(this);
	        // 初期化
	        init();
	    }
	
	    /** 初期化 */
	    public void init() {
	
	        // 変数
	        if (id == 0) {
	            autoFlag = 0;
	        } else {
	            autoFlag = 1;
	        }
// テスト用
x = "";
//for (int i = 0; i < cc.length; i++) {
//    cc[i] = 0;
//}
//cc_sum = 0;
	        // オートモード
	        rotateDirection = 1; // オートモードでの回転方向
	        autoRotate = 0; // オートモードでの回転の仕方
	        autoLow = 0; // オートモードでの列番号
	        // 色々
	        fallThreadCount = 0; // スレッドカウント
	        connectCount = 0; // 連結数
	        rotationPosition = 0; // 回転ラベル
	        overMessage = "";
	        chainCount = 0; // 連鎖数
	        ignitionChainCount = 0; // 発火ぷよ発火による連鎖数
	        maxChainCount = 0; // それまでの最大連鎖数を保持
	//        message = ""; // 連鎖メッセージ
	        // Flag
	        waitFlag = 0; // 待ってるか判定
	        chainFlag = 0; // 連鎖する可能性があるか判定
	        notBuryFlag = 0; // 埋まるかどうか判定
	//        connect3Flag = 0; // 連結数が3以上の箇所が見つかったか判定
	//        tearFlag = 0; // ちぎったか判定
	        judgeFlag = 0; // オートモードで判定中か判定
	        ignitionJudgeFlag = 0; // オートモードで発火ぷよ判定中か判定
	        // おじゃま
	        disturbCount = 0;
	        tempDisturbCount = 0;
	        damDisturbCount = 0;
	        disturbRest = 0;
	        disturbRate = 120;
	        disturbFlag = 0;
	        // 得点
	        score = 0;
	        dScore = 0;
	        A = 0;
	        cMax = 0;
	        dSum = 0;
	        // 配列
	        for (int i = 0; i < stage.lows; i++) {
	            for (int j = 0; j < stage.columns; j++) {
	                grid[i, j] = 0; // マス目
	                dummyGrid[i, j] = 0; // ダミーのマス目
	                preDummyGrid[i, j] = 0; // ダミーのマス目2
	                fallLabel[i, j] = 0; // どれくらい落ちたのか
	                chainLabel[i, j] = 0; // 連鎖ラベル
	                lastChainLabel[i, j] = 0; // 連鎖ラベル
	                preChainLabel[i, j] = 0;
	                connectionLabel[i, j] = 0; // 連鎖時ラベル用
	                connectionNumber[i, j] = 0; // 連結数保存用
	                ignitionLabel2[i, j] = 0;
	                lastIgnitionLabel2[i, j] = 0;
	            }
	        }
	        for (int i = 0; i < colors.Length - 1; i++) {
	            erasedPuyoList[i] = 0; // 色ごとに消しぷよ数を保存する用（一時的に）
	            erasedNumber[i] = 0; // それぞれの色ぷよの消した個数
	        }
	        // 連鎖回数保存用
	        for (int i = 0; i < 5; i++) {
	            chainNumber[i] = 0;
	        }
	        // 順位
	        for (int i = 0; i < stage.playersCount; i++) {
	            stage.rankings[i] = 0;
	        }
	        for (int i = 0; i < params_.Length; i++) {
	            params_[i] = 0;
	            tempParams[i] = 0;
	            paramLabels[i] = 0;
	        }
	        for (int i = 0; i < colors.Length - 1; i++) {
	            for (int j = 0; j < stage.columns; j++) {
	                colorList[j, i] = 0;
	            }
	        }
	
	        // ぷよ生成
	        puyoListSize = -1;
	        makePuyo1();
	        // Gridクラス初期化
	        gridObject.init();
	    }
	
	    // ゲーム本体
	
	    /** setTimeout()に似たようなもの */
	    public void sleep(int time, string mode) {
	        sleepTime = time;
	        sleepMode = mode;
	        otherTask = null;
	        otherTask = new Thread(doOther);
	        otherTask.Start();
	    }
	
	    /** ゲームスタート */
	    public void start() {
	        stage.gameFlags[id] = 1;
	        // ぷよの座標をリセット
	        pos[0, 0] = 0;
	        pos[0, 1] = stage.leftPos;
	        pos[1, 0] = 1;
	        pos[1, 1] = stage.leftPos;
	        // NEXT2ぷよ生成＆表示
	        makePuyo2();
	        view.repaint();
	        // 動かす
	        autoFall();
	        if (autoFlag == 1) {
	            sleep(300, "AutoMove");
	        }
	    }
	
	    /** ゲームオーバー */
	    void gameOver() {
	        // フラグをオフに
	        stage.gameFlags[id] = 0;
	        autoFlag = 0;
	        // 画像を消す
	        for (int i = 0; i < stage.lows; i++) {
	            for (int j = 0; j < stage.columns; j++) {
	                grid[i, j] = 0;
	            }
	        }
	        // 順位付け
	        stage.rankings[id] = stage.ranking;
	        if (stage.playersCount == 1) {
	            if (stage.soundFlag == 1) {
	                view.stopClips();
	                view.playClip(1);
	            }
	            overMessage = "おしまい";
	        } else if (stage.playersCount == 2) {
	            if (stage.ranking == 1) {
	                overMessage = "勝ち";
	            }
	            if (stage.ranking == 2) {
	                overMessage = "負け";
	            }
	        } else {
	            overMessage = stage.rankings[id] + "位";
	        }
	        stage.ranking--;
	        // 2位が決定したら
	        if (stage.ranking == 1) {
	            for (int i = 0; i < stage.playersCount; i++) {
	                if (stage.rankings[i] == 0) {
	                    stage.games[i].gameOver();
	                }
	            }
	            if (stage.soundFlag == 1) {
	                view.stopClips();
	                view.playClip(1);
	            }
	        }
	        // 表示
	        view.repaint();
	    }
	
	    /** ぷよ積み上げ */
	    void stack() {
	        // ちぎる（ぷよが横の場合）
	        if (pos[0, 1] != pos[1, 1]) {
	//            tearFlag = 1;
	            while (pos[0, 0] + 1 < stage.lows && grid[pos[0, 0] + 1, pos[0, 1]] == 0) {
	                pos[0, 0]++;
	            }
	            while (pos[1, 0] + 1 < stage.lows && grid[pos[1, 0] + 1, pos[1, 1]] == 0) {
	                pos[1, 0]++;
	            }
	        }
	        // 今ぷよ積み上げ
	        grid[pos[0, 0], pos[0, 1]] = puyo1;
	        grid[pos[1, 0], pos[1, 1]] = puyo2;
	        // gridの値をコピー
	        gridObject.copy1();
	        // 表示
	        if (stage.soundFlag == 1) {
	            view.playClip(4);
	        }
	        view.repaint();
	        chainCount = 0;
	        if (stage.skipFlag == 1) {
	            sleep(0, "Chain");
	        } else {
	            sleep(300, "Chain");
	        }
	    }
	
	    /** 連鎖系処理 */
	    void chain() {
	        gridObject.erase();
	        // dummyGridの値をコピー
	        gridObject.copy2();
	        if (chainFlag == 0) {
	            // 消えてない
	            if (chainCount >= 1) {
	                chainNumber[Math.Min(chainCount, 5) - 1]++;
	            }
	            next();
	        } else {
	            // 消えた
	
	            // 連鎖
	            chainCount++;
	            int cn = Math.Min(chainCount, voice.Length);
	            message = voice[cn - 1];
	            if (stage.soundFlag == 1) {
	                view.play(folder, cn);
	            }
	            if (maxChainCount < chainCount) {
	                maxChainCount = chainCount;
	            }
	            // 得点計算
	            calcScore();
	            // 落とす
	            if (stage.skipFlag == 1) {
	                sleep(0, "Fall");
	            } else {
	                if (stage.soundFlag == 1) {
	                    sleep(500, "Fall");
	                } else {
	                    sleep(300, "Fall");
	                }
	            }
	            view.repaint();
	        }
	    }
	
//int maxdef = 0;
	
	    /// 連鎖終了後処理
	    void next() {
// テスト表示
//if (chainCount >= 1) {
//    cc[Math.min(chainCount, 5) - 1]++;
//    cc_sum++;
//}
//maxdef = Math.max(maxdef, cc[2] - cc[0]);
//x = "mny=" + many + ",cc=" + cc[0] + "," + cc[1] + "," + cc[2] + "," + cc[3] + "," + cc[4] + ",md=" + maxdef;
//view.repaint();
	        // リセット
	        pos[0, 0] = 0;
	        pos[0, 1] = stage.leftPos;
	        pos[1, 0] = 1;
	        pos[1, 1] = stage.leftPos;
	        rotationPosition = 0;
	        chainCount = 0;
	        message = "";
	        // ゲームオーバー
	        if (grid[2, 2] != 0 && stage.gameFlags[id] == 1) {
	            if (disturbFlag == 1) {
	                sleep(1500, "Over");
	            } else {
	                sleep(500, "Over");
	            }
	        } else {
	            // おじゃま
	            disturb();
	            if (grid[2, 2] != 0 && stage.gameFlags[id] == 1) { // ゲームオーバー
	                if (disturbFlag == 1) {
	                    sleep(1500, "Over");
	                } else {
	                    sleep(500, "Over");
	                }
	            } else { // 落とす
	                if (disturbFlag == 1) {
	                    sleep(1000, "AutoFall");
	                } else {
	                    sleep(300, "AutoFall");
	                }
	            }
	        }
	    }
	
	    /** connectionLabel をリセット */
	    void resetConnectionLabel() {
	        for (int i = 0; i < stage.lows; i++) {
	            for (int j = 0; j < stage.columns; j++) {
	                connectionLabel[i, j] = 0;
	            }
	        }
	    }
	
	    /** おじゃま */
	    void disturb() {
	        // 発生させたおじゃまの数
	        disturbCount += tempDisturbCount;
	        tempDisturbCount = 0;
	        if (disturbCount > stage.disturbCounts[id]) {
	            // 自分の発生させたおじゃま数が、ストックのおじゃま数より多い
	            disturbCount -= stage.disturbCounts[id];
	            for (int i = 0; i < stage.playersCount; i++) {
	                if (i != id && stage.gameFlags[i] == 1) {
	                    stage.disturbCounts[i] += disturbCount;
	                }
	            }
	            stage.disturbCounts[id] = 0;
	        } else {
	            // 自分の発生させたおじゃま数が、ストックのおじゃま数以下
	            stage.disturbCounts[id] -= disturbCount;
	        }
	        // リセット
	        disturbCount = 0;
	        // おじゃまを降らせる
	        disturbFlag = 0;
	        if (stage.disturbCounts[id] > 0) {
	            disturbFlag = 1;
	            if (stage.disturbCounts[id] >= 6) {
	                // ストックのおじゃまが6個以上
	                while (stage.disturbCounts[id] > 30) {
	                    damDisturbCount++;
	                    stage.disturbCounts[id]--;
	                }
	                disturbHeight = (stage.disturbCounts[id] - stage.disturbCounts[id] % stage.columns) / stage.columns;
	                disturbHeightRest = stage.disturbCounts[id] % stage.columns;
	                for (int j = 0; j < stage.columns; j++) {
	                    damDisturbHeight = disturbHeight;
	                    for (int i = stage.lows - 1; i >= 0; i--) {
	                        if (grid[i, j] == 0 && damDisturbHeight > 0) {
	                            stage.disturbCounts[id]--;
	                            damDisturbHeight--;
	                            grid[i, j] = 1;
	                            if (damDisturbHeight == 0) {
	                                break;
	                            }
	                        }
	                    }
	                    if (damDisturbHeight > 0) {
	                        stage.disturbCounts[id] -= damDisturbHeight;
	                    }
	                }
	            } else {
	                // ストックのおじゃまが6個未満
	                disturbHeightRest = stage.disturbCounts[id];
	            }
	            if (disturbHeightRest > 0) {
	                int[] disturbList = new int[stage.columns];
	                for (int a = 0; a < stage.columns; a++) {
	                    disturbList[a] = 0;
	                }
	                while (stage.disturbCounts[id] > 0) {
	                    randomDisturbPos = (int) (random.Next(stage.columns));
	                    if (disturbList[randomDisturbPos] == 1) {
	                        continue;
	                    } else {
	                        disturbList[randomDisturbPos] = 1;
	                        for (int i = stage.lows - 1; i >= 0; i--) {
	                            if (grid[i, randomDisturbPos] == 0) {
	                                stage.disturbCounts[id]--;
	                                grid[i, randomDisturbPos] = 1;
	                                break;
	                            }
	                        }
	                    }
	                }
	            }
	            if (damDisturbCount > 0) {
	                stage.disturbCounts[id] = damDisturbCount;
	                damDisturbCount = 0;
	            }
	            view.repaint();
	            if (stage.soundFlag == 1) {
	                view.playClip(5);
	            }
	        }
	    }
	
	    /** 得点計算 */
	    void calcScore() {
	        // 連鎖数補正（得点計算のため）
	        if (chainCount > B.Length - 1) {
	            chainCount = B.Length - 1;
	        }
	        // AとCとD
	        for (int i = 0; i < erasedPuyoList.Length; i++) {
	            // 消しぷよ総数
	            A += erasedPuyoList[i];
	            // 同時消しぷよ
	            if (cMax < erasedPuyoList[i] - 4) {
	                cMax = erasedPuyoList[i] - 4;
	            }
	            if (cMax > C.Length - 1) {
	                cMax = C.Length - 1;
	            }
	            // 同時消し色
	            if (erasedPuyoList[i] != 0) {
	                dSum++;
	            }
	        }
	        // 得点計算（A*10*(B+C+D) = 消しぷよ数*10*(連鎖+同時ぷよ+同時色)）
	        if (B[chainCount] + C[cMax] + D[dSum - 1] == 0) {
	            bcd = 1;
	        } else if (B[chainCount] + C[cMax] + D[dSum - 1] > 999) {
	            bcd = 999;
	        } else {
	            bcd = B[chainCount] + C[cMax] + D[dSum - 1];
	        }
	        dScore = A * 10 * bcd;
	        score += dScore;
	        // おじゃま
	        tempDisturbCount += ((dScore + disturbRest) - (dScore + disturbRest) % disturbRate) / disturbRate;
	        disturbRest = (disturbRest + dScore) % disturbRate;
	        // リセット
	        A = 0;
	        cMax = 0;
	        dSum = 0;
	        for (int i = 0; i < erasedPuyoList.Length; i++) {
	            erasedPuyoList[i] = 0;
	        }
	    }
	
	    /** ぷよ回転処理 */
	    public void rotate(int code) {
	        if (code == 1) {
	            // 時計回り
	            if (rotationPosition == 0 && pos[0, 1] + 1 < stage.columns) {
	                ajustRotation(1, 1, 1);
	            } else if (rotationPosition == 1 && pos[0, 0] + 1 < stage.lows) {
	                ajustRotation(1, -1, 1);
	            } else if (rotationPosition == 2 && pos[0, 1] > 0) {
	                ajustRotation(-1, -1, 1);
	            } else if (rotationPosition == 3 && pos[0, 0] > 0) {
	                ajustRotation(-1, 1, 1);
	            }
	        } else if (code == 2) {
	            // 半時計回り
	            if (rotationPosition == 0 && pos[0, 1] > 0) {
	                ajustRotation(1, -1, -1);
	            } else if (rotationPosition == 3 && pos[0, 0] + 1 < stage.lows) {
	                ajustRotation(1, 1, -1);
	            } else if (rotationPosition == 2 && pos[0, 1] + 1 < stage.columns) {
	                ajustRotation(-1, 1, -1);
	            } else if (rotationPosition == 1 && pos[0, 0] > 0) {
	                ajustRotation(-1, -1, -1);
	            }
	        }
	        view.repaint();
	    }
	
	    /** ぷよ回転の際の座標の推移 */
	    void ajustRotation(int i, int j, int k) {
	        if (grid[pos[0, 0] + i, pos[0, 1] + j] == 0) {
	            if (stage.soundFlag == 1) {
	                view.playClip(3);
	            }
	            pos[0, 0] += i;
	            pos[0, 1] += j;
	            rotationPosition += k;
	            if (rotationPosition < 0) {
	                rotationPosition += 4;
	            }
	            rotationPosition %= 4;
	        }
	    }
	
	    /** ぷよ上移動 */
	    public void up() {
	        if (pos[0, 0] > 1 && pos[1, 0] > 1) {
	            pos[0, 0]--;
	            pos[1, 0]--;
	            if (stage.soundFlag == 1) {
	                view.playClip(2);
	            }
	        }
	        view.repaint();
	    }
	
	    /** ぷよ右移動 */
	    public void right() {
	        if (!(((pos[0, 1] == stage.columns - 1) || (pos[0, 1] < stage.columns - 1 && grid[pos[0, 0], pos[0, 1] + 1] != 0)) ||
	              ((pos[1, 1] == stage.columns - 1) || (pos[1, 1] < stage.columns - 1 && grid[pos[1, 0], pos[1, 1] + 1] != 0)))) {
	            pos[0, 1]++;
	            pos[1, 1]++;
	            if (stage.soundFlag == 1) {
	                view.playClip(2);
	            }
	        }
	        view.repaint();
	    }
	
	    /** ぷよ左移動 */
	    public void left() {
	        if (!(((pos[0, 1] == 0) || (pos[0, 1] - 1 >= 0 && grid[pos[0, 0], pos[0, 1] - 1] != 0)) ||
	              ((pos[1, 1] == 0) || (pos[1, 1] - 1 >= 0 && grid[pos[1, 0], pos[1, 1] - 1] != 0)))) {
	            pos[0, 1]--;
	            pos[1, 1]--;
	            if (stage.soundFlag == 1) {
	                view.playClip(2);
	            }
	        }
	        view.repaint();
	    }
	
	    /** ぷよ下移動 */
	    public void down() {
	        if (!(((pos[0, 0] + 1 == stage.lows) || (pos[0, 0] + 1 < stage.lows && grid[pos[0, 0] + 1, pos[0, 1]] != 0)) ||
	              ((pos[1, 0] + 1 == stage.lows) || (pos[1, 0] + 1 < stage.lows && grid[pos[1, 0] + 1, pos[1, 1]] != 0)))) {
	            // 下に移動する
	            pos[0, 0]++;
	            pos[1, 0]++;
	            if (stage.soundFlag == 1) {
	                view.playClip(2);
	            }
	            view.repaint();
	        } else {
	            // 下に移動できないので、積み上げる
	            waitFlag = 1;
	            stack();
	        }
	    }
	
	    /** ぷよ一気に下移動 */
	    public void bottom() {
	        if (pos[0, 1] != pos[1, 1]) {
	            // ぷよが横のとき
	            while (pos[0, 0] + 1 < stage.lows && grid[pos[0, 0] + 1, pos[0, 1]] == 0) {
	                pos[0, 0]++;
	            }
	            while (pos[1, 0] + 1 < stage.lows && grid[pos[1, 0] + 1, pos[1, 1]] == 0) {
	                pos[1, 0]++;
	            }
	        } else {
	            // ぷよが縦のとき
	            while (pos[0, 0] + 1 < stage.lows &&
	                   pos[1, 0] + 1 < stage.lows &&
	                   grid[pos[0, 0] + 1, pos[0, 1]] == 0 &&
	                   grid[pos[1, 0] + 1, pos[1, 1]] == 0) {
	                for (int i = 0; i < 2; i++) {
	                    pos[i, 0]++;
	                }
	            }
	        }
	        grid[pos[0, 0], pos[0, 1]] = puyo1;
	        grid[pos[1, 0], pos[1, 1]] = puyo2;
	        view.repaint();
	        waitFlag = 1;
	        stack();
	    }
	
	    /** ぷよ自動落下 */
	    public void autoFall() {
	        if (!(((pos[0, 0] + 1 == stage.lows) ||
	               (pos[0, 0] + 1 < stage.lows && grid[pos[0, 0] + 1, pos[0, 1]] != 0)) ||
	              ((pos[1, 0] + 1 == stage.lows) ||
	               (pos[1, 0] + 1 < stage.lows && grid[pos[1, 0] + 1, pos[1, 1]] != 0)))) {
	            // 下に移動できる
	            pos[0, 0]++;
	            pos[1, 0]++;
	            view.repaint();
	            // スレッド
	            if (stage.gameFlags[id] == 1) {
	                autoFallTask = null;
	                autoFallTask = new Thread(doAutoFall);
	                autoFallTask.Start();
	            }
	        } else if (waitFlag == 0) {
	            // 下に移動できないので、積み上げる
	            waitFlag = 1;
	            stack();
	        }
	    }
	
	    /** ぷよ生成1  (ロード直後のみ) */
	    void makePuyo1() {
	        // NEXTおよびNEXT2ぷよを生成
	        if (stage.puyoFlag == 1) {
	            npuyo1 = getCopiedyPuyo();
	            npuyo2 = getCopiedyPuyo();
	            nnpuyo1 = getCopiedyPuyo();
	            nnpuyo2 = getCopiedyPuyo();
	        } else {
	            npuyo1 = getRandomPuyo();
	            npuyo2 = getRandomPuyo();
	            nnpuyo1 = getRandomPuyo();
	            nnpuyo2 = getRandomPuyo();
	        }
	        // 初期化
	        if (stage.setFlag == 1) {
	            // 7連鎖
	            npuyo1 = 5;
	            int[,] initialGrid = {
	                { 0, 0, 0, 0, 0, 0 },
	                { 0, 0, 0, 0, 0, 0 },
	                { 0, 0, 0, 0, 0, 0 },
	                { 0, 0, 0, 0, 0, 0 },
	                { 0, 0, 0, 0, 0, 0 },
	                { 0, 0, 0, 0, 0, 0 },
	                { 3, 0, 0, 0, 0, 0 },
	                { 2, 0, 0, 0, 0, 0 },
	                { 5, 0, 0, 0, 0, 0 },
	                { 5, 0, 0, 0, 0, 0 },
	                { 5, 4, 5, 6, 2, 0 },
	                { 2, 3, 4, 5, 6, 2 },
	                { 2, 3, 4, 5, 6, 2 },
	                { 2, 3, 4, 5, 6, 2 }
	            };
	            // 初期化
	            for (int i = 0; i < stage.lows; i++) {
	                for (int j = 0; j < stage.columns; j++) {
	                    grid[i, j] = initialGrid[i, j];
	                }
	            }
	        }
	    }
	
	    /** ぷよ生成2 (予告ぷよを推移させたり) */
	    void makePuyo2() {
	        // ぷよ推移
	        puyo1 = npuyo1;
	        puyo2 = npuyo2;
	        npuyo1 = nnpuyo1;
	        npuyo2 = nnpuyo2;
	        // NEXT2ぷよを生成
	        if (stage.puyoFlag == 1) {
	            nnpuyo1 = getCopiedyPuyo();
	            nnpuyo2 = getCopiedyPuyo();
	        } else {
	            nnpuyo1 = getRandomPuyo();
	            nnpuyo2 = getRandomPuyo();
	        }
	    }
	
	    /** 用意しておいたぷよを返す */
	    int getCopiedyPuyo() {
	        puyoListSize++;
	        if (puyoListSize == stage.puyoList.Length) {
	            puyoListSize = 0;
	        }
	        return stage.puyoList[puyoListSize];
	    }
	
	    /** ぷよ生成用の乱数を返す */
	    int getRandomPuyo() {
	        return (int) random.Next(colors.Length - 1) + 2;
	    }
	
	    // オートモード関連
	
	    /** メイン */
	    public void autoMove() {
	        // リセット1
	        judgeFlag = 1;
	        mtime = 0;
	        autoRotate = 0;
	        autoLow = 0;
	        rotateDirection = 1;
	        for (int k = 0; k < params_.Length; k++) {
	            params_[k] = 0;
	        }
	        // gridの値をコピー
	        gridObject.copy1();
	        // アルゴリズムを決定
	        int puyon = gridObject.getPuyoNum();
	        if (puyon > 36) { // (int) (width * height / 3)
	            stage.ef1 = 1;
	            stage.ef2 = 1;
	            stage.many = 1;
	        } else if (stage.many == 0 || (stage.many == 1 && puyon < 24)) {
	//            stage.ef1 = (int) (Math.random() * 4);
	//            stage.ef2 = 1;
	            stage.ef1 = 0;
	            stage.ef2 = 0;
	            stage.many = 0;
	        }
	        // パラメータ取得の順番を決定
	        if (stage.many == 0) {
	            paramList[2] = "ignition_sum"; // 発火ぷよラベルの合計
	            paramList[3] = "ignition_count1"; // 発火ぷよ（連結数が2以上）の個数
	            paramList[4] = "chain_sum"; // 連鎖ラベルの合計
	            paramList[5] = "ignition_connect_sum"; // 発火ぷよ（連結数が2以上）の連結数の合計
	            paramList[6] = "connect_increment1"; // 発火後の連結数の増分の合計1
	            paramList[7] = "wall_count"; // 壁の個数
	            paramList[8] = "connect_sum"; // 最終状態の連結数
	            paramList[9] = "connect_increment2"; // 発火後の連結数の増分の合計2
	            paramList[10] = "ignition_count2"; // 発火ぷよ（連結数が1）の個数（連結数の合計）
	            paramList[11] = "connect_bury_count"; // どれだけの連結数を大きくできそうな箇所が埋まっているか
	            paramList[12] = "bury_count"; // どれだけのぷよが埋まっているか
	            paramList[13] = "dist"; // 同色ぷよまでの距離
	            paramList[14] = "h_point"; // 低さ
	        } else {
	            paramList[2] = "wall_count"; // 壁の個数
	            paramList[3] = "ignition_sum"; // 発火ぷよラベルの合計
	            paramList[4] = "chain_sum"; // 連鎖ラベルの合計
	            paramList[5] = "connect_sum"; // 最終状態の連結数
	            paramList[6] = "ignition_count1"; // 発火ぷよ（連結数が2以上）の個数
	            paramList[7] = "ignition_connect_sum"; // 発火ぷよ（連結数が2以上）の連結数の合計
	            paramList[8] = "ignition_count2"; // 発火ぷよ（連結数が1）の個数（連結数の合計）
	            paramList[9] = "connect_increment1"; // 発火後の連結数の増分の合計1
	            paramList[10] = "connect_increment2"; // 発火後の連結数の増分の合計2
	            paramList[11] = "connect_bury_count"; // どれだけの連結数を大きくできそうな箇所が埋まっているか
	            paramList[12] = "bury_count"; // どれだけのぷよが埋まっているか
	            paramList[13] = "dist"; // 同色ぷよまでの距離
	            paramList[14] = "h_point"; // 低さ
	        }
	        // 各々の列の高さを取得
	        int[] heightlist = gridObject.getHeight();
	        // 壁を取得
	        int leftWall = -1;
	        int rightWall = stage.columns;
	
	//        for (int j = stage.leftPos - 1; j >= 0; j--) {
	//            if (dummyGrid[2, j] != 0) {
	//                left_wall = j;
	//                break;
	//            }
	//        }
	//        for (int j = stage.leftPos + 1; j < width; j++) {
	//            if (dummyGrid[2, j] != 0) {
	//                right_wall = j;
	//                break;
	//            }
	//        }
	
	        // 回転させたりして判定
	        for (int j = leftWall + 1; j < rightWall; j++) {
	            if (heightlist[j] >= 3) {
	                // そのまま
	                judgePart(heightlist[j] - 1, j, puyo2, heightlist[j] - 2, j, puyo1, 0);
	                if (j + 1 < rightWall && heightlist[j + 1] >= 3) {
	                    // 右回転
	                    judgePart(heightlist[j] - 1, j, puyo2, heightlist[j + 1] - 1, j + 1, puyo1, 1);
	                }
	                if (j - 1 >= leftWall + 1 && heightlist[j - 1] >= 3) {
	                    // 左回転
	                    judgePart(heightlist[j] - 1, j, puyo2, heightlist[j - 1] - 1, j - 1, puyo1, 2);
	                }
	                if (heightlist[j] >= 4) {
	                    // 2回転
	                    judgePart(heightlist[j] - 1, j, puyo1, heightlist[j] - 2, j, puyo2, 3);
	                }
	            }
	        }
	        // 一番のポイントに落とす
	        mtime = autoLow - stage.leftPos;
	        if (autoRotate == 2 || (autoRotate == 3 && mtime >= 0)) {
	            rotateDirection = 2;
	        }
	        autoMove2();
	        // リセット
	        judgeFlag = 0;
// テスト表示
//x = "";
//for (int i = 0; i < 15; i++) {
//    x += params_[i] + ",";
//}
//view.repaint();
	    }
	
	    /** 移動判定 */
	    void autoMove2() {
	        if (stage.skipFlag == 1) {
	            if (mtime > 0) { // 右
	                sleep(0, "Right");
	            } else if (mtime < 0) { // 左
	                sleep(0, "Left");
	            } else { // そのまま
	                sleep(0, "Rotate");
	            }
	        } else {
	            if (mtime > 0) { // 右
	                sleep(MoveSpeed, "Right");
	            } else if (mtime < 0) { // 左
	                sleep(MoveSpeed, "Left");
	            } else { // そのまま
	                sleep(RotaSpeed + MoveSpeed, "Rotate");
	            }
	        }
	    }
	
	    /** 右移動 */
	    void autoMoveRight() {
	        right();
	        mtime--;
	        if (stage.skipFlag == 1) {
	            if (mtime > 0) {
	                sleep(0, "Right");
	            } else {
	                sleep(0, "Rotate");
	            }
	        } else {
	            if (mtime > 0) {
	                sleep(MoveSpeed, "Right");
	            } else {
	                sleep(RotaSpeed + MoveSpeed, "Rotate");
	            }
	        }
	    }
	
	    /** 左移動 */
	    void autoMoveLeft() {
	        left();
	        mtime++;
	        if (stage.skipFlag == 1) {
	            if (mtime < 0) {
	                sleep(0, "Left");
	            } else {
	                sleep(0, "Rotate");
	            }
	        } else {
	            if (mtime < 0) {
	                sleep(MoveSpeed, "Left");
	            } else {
	                sleep(RotaSpeed + MoveSpeed, "Rotate");
	            }
	        }
	    }
	
	    /** 落下パターン判定 */
	    void judgePart(int i, int j, int g, int i2, int j2, int g2, int r) {
	
	        // フィールドにあるぷよと落とした組ぷよのコピーを残しておく＆ラベルをリセット
	        for (int k = 0; k < stage.lows; k++) {
	            for (int l = 0; l < stage.columns; l++) {
	                dummyGrid[k, l] = grid[k, l];
	                chainLabel[k, l] = 0;
	                preChainLabel[k, l] = 0;
	                ignitionLabel2[k, l] = 0;
	            }
	        }
	        // 現ぷよを積む
	        dummyGrid[i, j] = g;
	        dummyGrid[i2, j2] = g2;
	        gridObject.copy1(dummyGrid);
	        // 変数初期化
	        eraseNumber = 0;
	        chainFlag = 0;
	        int nonStackFlag = 0;
	        // param関連配列をリセット
	        for (int k = 0; k < params_.Length; k++) {
	            paramLabels[k] = 0;
	        }
	        // 連鎖数と消しぷよ総数
	        tempParams[0] = getChainCount();
	        tempParams[1] = eraseNumber;
	        // 連鎖後のフィールドにあるぷよのコピーを残しておく
	        for (int k = 0; k < stage.lows; k++) {
	            for (int l = 0; l < stage.columns; l++) {
	                preDummyGrid[k, l] = dummyGrid[k, l];
	            }
	        }
	        // 消えないなら、高く積んでないか判定
	        if (tempParams[0] == 0) {
	            int nh = (stage.lows / 3) + 2;
	            if (gridObject.getPuyoNum() < 36 && (i < nh || i2 < nh)) {
	                nonStackFlag = 1;
	            }
	        }
	        // 更新する必要があるか判定
	        if (nonStackFlag == 0 &&
	            dummyGrid[2, 2] == 0 &&
	            ((tempParams[0] == 1 && stage.ef1 == 1) ||
	             (tempParams[0] == 2 && stage.ef2 == 1) ||
	             (tempParams[0] != 1 && tempParams[0] != 2))) {
	
	            // パラメータを取得していきながら比較
	            for (int p = 0; p < params_.Length; p++) {
	                if (paramLabels[p] == 0) {
	                    getParam(p);
	                }
	                if (tempParams[p] < params_[p]) {
	                    // 値が小さいならダメぽ
	                    break;
	                } else if (tempParams[p] > params_[p]) {
	                    // 値が大きいなら更新
	
	                    // 残りのパラメータを求める
	                    for (int k = p + 1; k < params_.Length; k++) {
	                        if (paramLabels[k] == 0) {
	                            getParam(k);
	                        }
	                    }
	                    // 値を代入
	                    for (int k = 0; k < params_.Length; k++) {
	                        params_[k] = tempParams[k];
	                    }
	                    // 連鎖ラベル
	                    for (int k = 0; k < stage.lows; k++) {
	                        for (int l = 0; l < stage.columns; l++) {
	                            lastChainLabel[k, l] = chainLabel[k, l];
	                            lastIgnitionLabel2[k, l] = ignitionLabel2[k, l];
	                        }
	                    }
	                    // パターンを更新
	                    autoRotate = r;
	                    autoLow = j;
	                    // 抜ける
	                    break;
	                }
	            }
	        }
	    }
	
	    /** パラメータ取得 */
	    void getParam(int p) {
	
	        if (paramList[p] == "ignition_connect_sum" ||
	            paramList[p] == "ignition_count1" ||
	            paramList[p] == "ignition_count2" ||
	            paramList[p] == "connect_increment1" ||
	            paramList[p] == "connect_increment2" ||
	            paramList[p] == "ignition_sum" ||
	            paramList[p] == "chain_sum") {
	            // 発火ぷよ関連
	
	            // 配列に値を代入
	            int[] array = gridObject.judgeIgnition();
	            int ignition_connect_sum = array[0];
	            int ignition_count1 = array[1];
	            int ignition_count2 = array[2];
	            int connect_increment1 = array[3];
	            int connect_increment2 = array[4];
	            int ignition_sum = array[5];
	            // 連鎖ラベルの合計を算出
	            int chain_sum = 0;
	            for (int i = 0; i < stage.lows; i++) {
	                for (int j = 0; j < stage.columns; j++) {
	                    chain_sum += chainLabel[i, j];
	                    preChainLabel[i, j] = 0; // リセット
	                }
	            }
	            // temp_paramを更新
	            for (int i = 0; i < params_.Length; i++) {
	                int flag = 0;
	                if (paramList[i] == "ignition_connect_sum") {
	                    tempParams[i] = ignition_connect_sum;
	                } else if (paramList[i] == "ignition_count1") {
	                    tempParams[i] = ignition_count1;
	                } else if (paramList[i] == "ignition_count2") {
	                    tempParams[i] = ignition_count2;
	                } else if (paramList[i] == "connect_increment1") {
	                    tempParams[i] = connect_increment1;
	                } else if (paramList[i] == "connect_increment2") {
	                    tempParams[i] = connect_increment2;
	                } else if (paramList[i] == "ignition_sum") {
	                    tempParams[i] = ignition_sum;
	                } else if (paramList[i] == "chain_sum") {
	                    tempParams[i] = chain_sum;
	                } else {
	                    flag = 1;
	                }
	                if (flag == 0) {
	                    paramLabels[i] = 1;
	                }
	            }
	        } else if (paramList[p] == "wall_count") {
	            // 壁になっている列の個数 (端を除く) を計算
	            tempParams[p] = 0;
	            for (int j = 1; j < stage.columns - 1; j++) {
	                if (dummyGrid[2, j] != 0) {
	                    tempParams[p]++;
	                }
	            }
	            tempParams[p] *= -1;
	        } else if (paramList[p] == "connect_sum") {
	            // 連結数の合計を計算
	            tempParams[p] = gridObject.getConnectionSum();
	        } else if (paramList[p] == "connect_bury_count") {
	            // 連結数を大きくできそうな箇所がいくつ埋まっているか
	            tempParams[p] = 0;
	            for (int i = 0; i < stage.lows; i++) {
	                for (int j = 0; j < stage.columns; j++) {
	                    tempParams[p] += gridObject.judgeEraseStack(i, j, dummyGrid[i, j]);
	                }
	            }
	            tempParams[p] *= -1;
	        } else if (paramList[p] == "bury_count") {
	            // 全てのぷよについて埋まっているかどうか判定
	            tempParams[p] = 0;
	            for (int i = 0; i < stage.lows; i++) {
	                for (int j = 0; j < stage.columns; j++) {
	                    if (gridObject.judgeStack(i, j) == 0) {
	                        tempParams[p]++;
	                    }
	                }
	            }
	            tempParams[p] *= -1;
	        } else if (paramList[p] == "dist") {
	            // 他の列にある同色ぷよまでの距離の最大値を算出
	            tempParams[p] = gridObject.getDistance();
	            tempParams[p] *= -1;
	        } else if (paramList[p] == "h_point") {
	            // 各々の列の高さを取得＆それぞれのぷよの高さの合計を算出
	            tempParams[p] = 0;
	            int[] heightlist = gridObject.getHeight();
	            for (int i = 0; i < stage.columns; i++) {
	                for (int j = heightlist[i]; j < stage.lows; j++) {
	                    tempParams[p] += j;
	                }
	            }
	        }
	    }
	
	    // 連鎖系処理2
	    int getChainCount() {
	        int count = 0;
	        gridObject.erase();
	        while (chainFlag == 1) {
	            count++;
	            gridObject.fall();
	            chainFlag = 0;
	            gridObject.erase();
	        }
	        return count;
	    }
	
	    /**
	     * Gridクラス
	     */
	    class Grid {

            /** TODO ありえん */
        	private PuyoPuyo instance;

	        public Grid(PuyoPuyo instance) {
	            this.instance = instance;
	        }

	        /** 初期化 */
	        public void init() {
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    instance.dummyGrid[i, j] = 0;
	                }
	            }
	        }
	
	        /** gridの値をコピー (引数を取らない) */
	        public void copy1() {
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    instance.dummyGrid[i, j] = instance.grid[i, j];
	                }
	            }
	        }
	
	        /** gridの値をコピー (引数を取る) */
	        public void copy1(int[,] g) {
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    instance.dummyGrid[i, j] = g[i, j];
	                }
	            }
	        }
	
	        /** dummy_gridの値をコピー */
	        public void copy2() {
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    instance.grid[i, j] = instance.dummyGrid[i, j];
	                }
	            }
	        }
	
	        /** ぷよ1つの連結数を取得 (メイン) */
	        int getConnection(int i2, int j2) {
	            int i = i2;
	            int j = j2;
	            instance.connectCount = 0;
	            getConnection2(i, j);
	            instance.resetConnectionLabel();
	            return instance.connectCount;
	        }
	
	        /** ぷよ1つの連結数を取得 (再起呼び出し部) */
	        private void getConnection2(int i2, int j2) {
	            int i = i2;
	            int j = j2;
	            if (instance.dummyGrid[i, j] > 1 && instance.connectionLabel[i, j] != 1) {
	                instance.connectionLabel[i, j] = 1;
	                instance.connectCount++;
	                if (i - 1 >= 0 && instance.dummyGrid[i, j] == instance.dummyGrid[i - 1, j]) {
	                    getConnection2(i - 1, j);
	                }
	                if (i + 1 <= instance.stage.lows - 1 && instance.dummyGrid[i, j] == instance.dummyGrid[i + 1, j]) {
	                    getConnection2(i + 1, j);
	                }
	                if (j - 1 >= 0 && instance.dummyGrid[i, j] == instance.dummyGrid[i, j - 1]) {
	                    getConnection2(i, j - 1);
	                }
	                if (j + 1 <= instance.stage.columns - 1 && instance.dummyGrid[i, j] == instance.dummyGrid[i, j + 1]) {
	                    getConnection2(i, j + 1);
	                }
	            }
	        }
	
	        /** 積み上がっていたのを落とす */
	        public void fall() {
	            int di;
	            for (int i = instance.stage.lows - 2; i >= 0; i--) {
	                for (int j = instance.stage.columns - 1; j >= 0; j--) {
	                    if (instance.dummyGrid[i, j] != 0) {
	                        di = i;
	                        while (di + 1 < instance.stage.lows && instance.dummyGrid[di + 1, j] == 0) {
	                            instance.dummyGrid[di + 1, j] = instance.dummyGrid[di, j];
	                            instance.dummyGrid[di, j] = 0;
	                            di++;
	                            // オートモード判定中
	                            if (instance.ignitionJudgeFlag == 1) {
	                                instance.fallLabel[di, j] = instance.fallLabel[di - 1, j] + 1;
	                                instance.fallLabel[di - 1, j] = 0;
	                            }
	                        }
	                    }
	                }
	            }
	            // 実際の場合
	            if (instance.judgeFlag == 0) {
	                copy2();
	                instance.view.repaint();
	                if (instance.stage.skipFlag == 1) {
	                    instance.sleep(0, "Chain");
	                } else {
	                    if (instance.stage.soundFlag == 1) {
	                        instance.sleep(500, "Chain");
	                    } else {
	                        instance.sleep(300, "Chain");
	                    }
	                }
	            }
	        }
	
	        /** 連結数が4以上のぷよを消す */
	        public void erase() {
	            instance.chainFlag = 0;
	            // 全てのぷよの連結数を計算
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    instance.connectionNumber[i, j] = getConnection(i, j);
	                }
	            }
	            // 全てのぷよについて判定
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    // 連結数が4以上なら
	                    if (instance.connectionNumber[i, j] >= 4) {
	                        instance.chainFlag = 1;
	                        if (instance.judgeFlag == 0) {
	                            // 実際に消す場合
	                            instance.erasedPuyoList[instance.dummyGrid[i, j] - 2]++; // 色ごとに消しぷよ数を保存する用（一時的に）
	                            instance.erasedNumber[instance.dummyGrid[i, j] - 2]++; // それぞれの色ぷよの消した個数をプラス
	                        } else if (instance.ignitionJudgeFlag == 0) {
	                            // オートモードで、発火ぷよ判定をしていない場合
	                            instance.eraseNumber++;
	                        }
	                        // 消す
	                        instance.dummyGrid[i, j] = 0;
	                        instance.fallLabel[i, j] = 0;
	                        if (i > 0 && instance.dummyGrid[i - 1, j] == 1) {
	                            instance.dummyGrid[i - 1, j] = 0;
	                            instance.fallLabel[i - 1, j] = 0;
	                        }
	                        if (j + 1 < instance.stage.columns && instance.dummyGrid[i, j + 1] == 1) {
	                            instance.dummyGrid[i, j + 1] = 0;
	                            instance.fallLabel[i, j + 1] = 0;
	                        }
	                        if (i + 1 < instance.stage.lows && instance.dummyGrid[i + 1, j] == 1) {
	                            instance.dummyGrid[i + 1, j] = 0;
	                            instance.fallLabel[i + 1, j] = 0;
	                        }
	                        if (j > 0 && instance.dummyGrid[i, j - 1] == 1) {
	                            instance.dummyGrid[i, j - 1] = 0;
	                            instance.fallLabel[i, j - 1] = 0;
	                        }
	                    }
	                }
	            }
	        }
	
	        /** 発火ぷよ判定 */
	        public int[] judgeIgnition() {
	            // 変数初期化
	            instance.ignitionJudgeFlag = 1;
	            // 変数定義
	            int[] array = {
	                0, 0, 0, 0, 0, 0
	            };
	            // 全てのぷよについて判定
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    // 変数初期化
	                    instance.ignitionChainCount = 1;
	                    int connectSum1 = 0;
	                    int connectSum2 = 0;
	                    int connect = getConnection(i, j);
	                    // 色ぷよについてのみ判定（発火点にぷよを置ける場合）
	                    if (instance.dummyGrid[i, j] > 1 && judgeIgnitionPoint(i, j) == 1) {
	                        // 連鎖判定まだのぷよ
	                        if (instance.preChainLabel[i, j] == 0) {
	                            // 該当ぷよ、及びそれと連結したぷよを消して連鎖発動
	                            erase2(i, j, instance.dummyGrid[i, j]);
	                            fall();
	                            instance.ignitionChainCount = instance.getChainCount() + 1;
	                            // 連鎖終了後の連結数の合計を算出
	                            connectSum2 = getConnectionSum();
	                            // 連鎖に使ったぷよにラベルを付けて元の位置に戻す
	                            myRet();
	                            // 連鎖に使わなかったぷよの連鎖開始前の連結数の合計を算出
	                            connectSum1 = getConnectionSum();
	                        }
	                        // ぷよとfallLabelを戻す
	                        for (int k = 0; k < instance.stage.lows; k++) {
	                            for (int l = 0; l < instance.stage.columns; l++) {
	                                instance.dummyGrid[k, l] = instance.preDummyGrid[k, l];
	                                instance.fallLabel[k, l] = 0;
	                            }
	                        }
	                        if (instance.ignitionChainCount >= 2) { // 連鎖したなら発火ぷよ
	                            // 連鎖ラベルを付ける
	                            attacheLabel(i, j, instance.ignitionChainCount - 1, instance.chainLabel[i, j]);
	                            instance.resetConnectionLabel();
	                            // 連結数が2以上の発火ぷよ
	                            if (connect > 1) {
	                                array[0] += connect * connect; // 連結数の合計
	                                array[1] += connect; // 個数
	                            }
	                            // 連結数が1の発火ぷよ
	                            else {
	//                                array[0]++; // 連結数の合計
	                                array[2]++; // 個数
	                            }
	                            // 連結数が増えていたなら、変数に値を代入
	                            if (connectSum2 > connectSum1) {
	                                array[3] += (connectSum2 - connectSum1) * connect;
	                            }
	                        } else { // 連鎖してないぞ
	                            // 連結数が増えていたなら、変数に値を代入
	                            if (connectSum2 > connectSum1) {
	                                array[4] += connectSum2 - connectSum1;
	                            }
	                        }
	                    }
	                }
	            }
	            // 発火ぷよラベルの合計を算出
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    if (instance.ignitionLabel2[i, j] > 1) {
	                        array[5] += instance.ignitionLabel2[i, j];
	                    }
	                }
	            }
	            // ここまで
	            instance.ignitionJudgeFlag = 0;
	            return array;
	        }
	
	        /** ぷよに連鎖ラベルを付ける */
	        void attacheLabel(int i2, int j2, int l, int c) {
	            int i = i2;
	            int j = j2;
	            if (instance.dummyGrid[i, j] > 1 && instance.connectionLabel[i, j] != 1) {
	                instance.connectionLabel[i, j] = 1;
	                instance.ignitionLabel2[i, j] = l;
	                instance.chainLabel[i, j] = c;
	                if (i - 1 >= 0 && instance.dummyGrid[i, j] == instance.dummyGrid[i - 1, j]) {
	                    attacheLabel(i - 1, j, l, c);
	                }
	                if (i + 1 <= instance.stage.lows - 1 && instance.dummyGrid[i, j] == instance.dummyGrid[i + 1, j]) {
	                    attacheLabel(i + 1, j, l, c);
	                }
	                if (j - 1 >= 0 && instance.dummyGrid[i, j] == instance.dummyGrid[i, j - 1]) {
	                    attacheLabel(i, j - 1, l, c);
	                }
	                if (j + 1 <= instance.stage.columns - 1 && instance.dummyGrid[i, j] == instance.dummyGrid[i, j + 1]) {
	                    attacheLabel(i, j + 1, l, c);
	                }
	            }
	        }
	
	        /** 連鎖に使うぷよにラベルを付ける */
	        void myRet() {
	            // 落ちたぷよを元の高さに戻す
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    if (instance.fallLabel[i, j] > 0) {
	                        instance.dummyGrid[i - instance.fallLabel[i, j], j] = instance.dummyGrid[i, j];
	                        instance.dummyGrid[i, j] = 0;
	                        instance.fallLabel[i, j] = 0;
	                    }
	                }
	            }
	            // ラベル付け
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    if (instance.preDummyGrid[i, j] != instance.dummyGrid[i, j] && instance.ignitionChainCount >= 2) {
	                        instance.chainLabel[i, j] += instance.ignitionChainCount;
	                    }
	                }
	            }
	        }
	
	        int rarara = 0;
	
	        /** 発火点にぷよを置けるか判定 (メイン) */
	        int judgeIgnitionPoint(int i2, int j2) {
	            int i = i2;
	            int j = j2;
	            rarara = 0;
	//            if (i2 + 1 < height && instance.dummyGrid[i2 + 1, j2] == 0) {
	//                rarara = 0;
	//            }
	            myJudgeIgnitionPlace2(i, j);
	            instance.resetConnectionLabel();
	            return rarara;
	        }
	
	        /** 発火点にぷよを置けるか判定 (再起呼び出し部) */
	        private void myJudgeIgnitionPlace2(int i, int j) {
	            if (instance.connectionLabel[i, j] != 1) {
	                instance.connectionLabel[i, j] = 1;
	                rarara = getIgnitionPointLabel(i, j);
	                if (rarara == 0) {
	                    if (i - 1 >= 0 && instance.dummyGrid[i, j] == instance.dummyGrid[i - 1, j]) {
	                        // 上
	                        myJudgeIgnitionPlace2(i - 1, j);
	                    }
	                    if (i + 1 <= instance.stage.lows - 1 && instance.dummyGrid[i, j] == instance.dummyGrid[i + 1, j]) {
	                        // 下
	                        myJudgeIgnitionPlace2(i + 1, j);
	                    }
	                    if (j - 1 >= 0 && instance.dummyGrid[i, j] == instance.dummyGrid[i, j - 1]) {
	                        // 左
	                        myJudgeIgnitionPlace2(i, j - 1);
	                    }
	                    if (j + 1 <= instance.stage.columns - 1 && instance.dummyGrid[i, j] == instance.dummyGrid[i, j + 1]) {
	                        // 右
	                        myJudgeIgnitionPlace2(i, j + 1);
	                    }
	                }
	            }
	        }
	
	        /** 発火点にぷよを置けるか判定 (本体) */
	        int getIgnitionPointLabel(int i3, int j3) {
	            int flag = 0;
	            int i = i3;
	            int j = j3;
	            if (i - 1 >= 0 && instance.dummyGrid[i - 1, j] == 0) {
	                // 上にぷよがない
	                flag = 1;
	            } else if ((j - 1 >= 0 && instance.dummyGrid[i, j - 1] == 0) &&
	                      !(i + 2 < instance.stage.lows && instance.dummyGrid[i + 2, j - 1] == 0)) {
	                // 左にぷよがない
	                flag = 1;
	            } else if ((j + 1 < instance.stage.columns && instance.dummyGrid[i, j + 1] == 0) &&
	                      !(i + 2 < instance.stage.lows && instance.dummyGrid[i + 2, j + 1] == 0)) {
	                // 右にぷよがない
	                flag = 1;
	            }
	            // 返す
	            return flag;
	        }
	
	        /** 連結したぷよ群が埋まるっているか判定 (メイン) */
	        public int judgeStack(int i2, int j2) {
	            int i = i2;
	            int j = j2;
	            instance.notBuryFlag = 0;
	            judgeStack2(i, j);
	            instance.resetConnectionLabel();
	            return instance.notBuryFlag;
	        }
	
	        /** 連結したぷよ群が埋まっているか判定 (再起呼び出し部) */
	        private void judgeStack2(int i, int j) {
	            if (instance.connectionLabel[i, j] != 1) {
	                instance.connectionLabel[i, j] = 1;
	                if (i - 1 >= 0) { // 上
	                    if (instance.dummyGrid[i, j] == instance.dummyGrid[i - 1, j]) {
	                        judgeStack2(i - 1, j);
	                    } else if (instance.dummyGrid[i - 1, j] == 0) {
	                        instance.notBuryFlag = 1;
	                    }
	                }
	                if (i + 1 <= instance.stage.lows - 1) { // 下
	                    if (instance.dummyGrid[i, j] == instance.dummyGrid[i + 1, j]) {
	                        judgeStack2(i + 1, j);
	                    } else if (instance.dummyGrid[i + 1, j] == 0) {
	                        instance.notBuryFlag = 1;
	                    }
	                }
	                if (j - 1 >= 0) { // 左
	                    if (instance.dummyGrid[i, j] == instance.dummyGrid[i, j - 1]) {
	                        judgeStack2(i, j - 1);
	                    } else if (instance.dummyGrid[i, j - 1] == 0) {
	                        instance.notBuryFlag = 1;
	                    }
	                }
	                if (j + 1 <= instance.stage.columns - 1) { // 右
	                    if (instance.dummyGrid[i, j] == instance.dummyGrid[i, j + 1]) {
	                        judgeStack2(i, j + 1);
	                    } else if (instance.dummyGrid[i, j + 1] == 0) {
	                        instance.notBuryFlag = 1;
	                    }
	                }
	            }
	        }
	
	        /** 連結数を大きくできそうな箇所が他のぷよで埋まっているか判定 (メイン) */
	        public int judgeEraseStack(int i, int j, int g) {
	            int count = 0;
	            int[] array = new int[colors.Length];
	            for (int k = 0; k < colors.Length; k++) {
	                array[k] = 0;
	            }
	            // 色ぷよのみ
	            if (g > 1) {
	                if (i + 1 <= instance.stage.lows - 1 && instance.dummyGrid[i + 1, j] > 1) {
	                    // 右
	                    array[instance.dummyGrid[i + 1, j] - 2] += judgeEraseStack2(i + 1, j, g);
	                }
	                if (j - 1 >= 0 && instance.dummyGrid[i, j - 1] > 1) {
	                    // 左
	                    array[instance.dummyGrid[i, j - 1] - 2] += judgeEraseStack2(i, j - 1, g);
	                }
	                if (j + 1 <= instance.stage.columns - 1 && instance.dummyGrid[i, j + 1] > 1) {
	                    // 下
	                    array[instance.dummyGrid[i, j + 1] - 2] += judgeEraseStack2(i, j + 1, g);
	                }
	            }
	            // 数える
	            for (int k = 0; k < colors.Length; k++) {
	                if (array[k] >= 2) {
	                    count++;
	                }
	            }
	            // 戻す
	            return count;
	        }
	
	        /** 連結数を大きくできそうな箇所が他のぷよで埋まっているか判定 (再起呼び出し部) */
	        private int judgeEraseStack2(int i2, int j2, int g2) {
	            int i = i2;
	            int j = j2;
	            int g = g2;
	            int count = 0;
	            if (instance.dummyGrid[i, j] != 1 && instance.dummyGrid[i, j] != g) {
	                count++;
	            }
	            return count;
	        }
	
	        /** 他の列にある同色ぷよまでの横の距離の最大値を算出 */
	        public int getDistance() {
	            int dist = 0;
	            int dist1 = 0;
	            int dist2 = 0;
	            // 代入
	            for (int j = 0; j < instance.stage.columns; j++) {
	                for (int i = 0; i < instance.stage.lows; i++) {
	                    if (instance.dummyGrid[i, j] > 1) {
	                        instance.colorList[j, instance.dummyGrid[i, j] - 2] = 1;
	                    }
	                }
	            }
	            // やるぞ
	            for (int i = 0; i < colors.Length - 1; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    if (instance.colorList[j, i] == 1) {
	                        dist1 = 0;
	                        dist2 = 0;
	                        // 左側
	                        for (int l = j - 1; l >= 0; l--) {
	                            if (instance.colorList[l, i] == 1) {
	                                dist1 = j - l;
	                            }
	                        }
	                        // 右側
	                        for (int l = j + 1; l < instance.stage.columns; l++) {
	                            if (instance.colorList[l, i] == 1) {
	                                dist2 = l - j;
	                            }
	                        }
	                        dist += Math.Max(dist1, dist2);
	                    }
	                }
	            }
	            // リセット
	            for (int i = 0; i < colors.Length - 1; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    instance.colorList[j, i] = 0;
	                }
	            }
	            // 返す
	            return dist;
	        }
	
	        /** 各々の列の高さを取得 */
	        public int[] getHeight() {
	            int[] heightList = new int[instance.stage.columns];
	            for (int j = 0; j < instance.stage.columns; j++) {
	                heightList[j] = instance.stage.lows;
	                for (int i = 0; i < instance.stage.lows; i++) {
	                    if (instance.dummyGrid[i, j] > 0) {
	                        heightList[j] = i;
	                        break;
	                    }
	                }
	            }
	            return heightList;
	        }
	
	        /** フィールドにあるぷよの個数を数える */
	        public int getPuyoNum() {
	            int count = 0;
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    if (instance.dummyGrid[i, j] > 0) {
	                        count++;
	                    }
	                }
	            }
	            return count;
	        }
	
	        /** 連結数の合計を算出 */
	        public int getConnectionSum() {
	            int connectionSum = 0;
	            for (int i = 0; i < instance.stage.lows; i++) {
	                for (int j = 0; j < instance.stage.columns; j++) {
	                    connectionSum += getConnection(i, j);
	                }
	            }
	            return connectionSum;
	        }
	
	        /**
	         * 該当ぷよ及びそれと連結したぷよを消す
	         * おじゃまぷよについては後回し
	         */
	        void erase2(int i2, int j2, int g2) {
	            int i = i2;
	            int j = j2;
	            int g = g2;
	            instance.dummyGrid[i, j] = 0;
	            instance.preChainLabel[i, j] = 1;
	            if (i - 1 >= 0 && instance.dummyGrid[i - 1, j] == g) { // 上
	                erase2(i - 1, j, g);
	            }
	            if (i + 1 <= instance.stage.lows - 1 && instance.dummyGrid[i + 1, j] == g) { // 下
	                erase2(i + 1, j, g);
	            }
	            if (j - 1 >= 0 && instance.dummyGrid[i, j - 1] == g) { // 左
	                erase2(i, j - 1, g);
	            }
	            if (j + 1 <= instance.stage.columns - 1 && instance.dummyGrid[i, j + 1] == g) { // 右
	                erase2(i, j + 1, g);
	            }
	        }
	    }
	
	    /**
	     * myAutoFallを他のスレッドとは独立に呼び出すためのスレッド
	     */
	    void doAutoFall() {
            fallThreadCount++;
            if (fallThreadCount == 1) {
                Thread.Sleep(FallSpeed);
                fallThreadCount--;
                if (waitFlag == 0) {
                    autoFall();
                }
            } else {
                fallThreadCount--;
            }
        }
	
	    /**
	     * autoFall以外を呼び出すためのスレッド
	     */
	    void doOther() {
            // 止める
            Thread.Sleep(sleepTime);
            if (stage.gameFlags[id] == 1) {
                // 連鎖終了後処理
                if (sleepMode.Equals("AutoFall")) {
                    makePuyo2(); // ぷよ生成
                    waitFlag = 0; // ボタン入力受付開始
                    if (fallThreadCount == 0) {
                        // 落とす
                        autoFall();
                    }
                    Thread.Sleep(300);
                    if (stage.gameFlags[id] == 1 && autoFlag == 1) {
                        // オートモード起動
                        autoMove();
                    }
                // 他
                } else if (sleepMode.Equals("AutoMove")) { // オートモード(ゲームスタート時のみ)
                    autoMove();
                } else if (sleepMode.Equals("Stack")) { // ぷよ積み上げ
                    stack();
                } else if (sleepMode.Equals("Chain")) { // 連鎖判定
                    chain();
                } else if (sleepMode.Equals("Fall")) { // 連鎖時落下
                    gridObject.fall();
                } else if (sleepMode.Equals("Right")) { // 右自動移動
                    autoMoveRight();
                } else if (sleepMode.Equals("Left")) { // 左自動移動
                    autoMoveLeft();
                } else if (sleepMode.Equals("Stop")) { // ストップ
                    waitFlag = 0;
                } else if (sleepMode.Equals("Over")) { // ゲームオーバー
                    gameOver();
                } else if (sleepMode.Equals("Rotate")) { // オートモード回転
                    // 止める
                    Thread.Sleep(sleepTime);
                    if (stage.gameFlags[id] == 1) {
                        if (autoRotate == 0) { // そのまま
                            if (stage.sss == 1) {
                                bottom();
                            }
                        } else { // 他
                            // とりあえず回転
                            rotate(rotateDirection);
                            view.repaint();
                            // 2回転の場合
                            if (autoRotate == 3) {
                                Thread.Sleep(sleepTime);
                                if (stage.gameFlags[id] == 1) {
                                    rotate(rotateDirection);
                                    view.repaint();
                                }
                            }
                            // 止める
                            Thread.Sleep(sleepTime);
                            // 落とす
                            if (stage.gameFlags[id] == 1 && stage.sss == 1) {
                                bottom();
                            }
                        }
                    }
                }
            }
        }
    }
}

/* */