/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.puyopuyo;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


/**
 * PuyoPuyoApp
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 090105 nsano initial version <br>
 */
public class PuyoPuyoApp extends Applet implements PuyoPuyo.View {

    /** */
    private PuyoPuyo.Stage stage;

    /** */
    private Image offscreenImage;
    /** */
    private Graphics ofscreenGraphics;
    /** */
    private Image wallImage;
    /** */
    private Image fieldImage;
    /** */
    private Image nextImage;
    /** */
    private Image[] images;

    /** */
    private int offScreenWidth;
    /** */
    private int offScreenHeight;
    /** */
    private AudioClip[] clips;

    /** */
    private int[] fieldLefts, fieldTops;
    /** ぷよの幅 */
    private int puyoSize;

    /** ゲームがスタートしたか判定 */
    private int startFlag;
    /** 一時停止・スタート判定 */
    private int stopFlag;

    private String state = ""; // "test";

    /** 音楽の停止 */
    public void stopClips() {
        for (int i = 0; i < clips.length; i++) {
            clips[i].stop();
        }
    }

    /* */
    public void init() {
        // パラメータを取得
        int playersCount = Integer.parseInt(getParameter("n"));
        stage = new PuyoPuyo.Stage(playersCount);
        offScreenWidth = Integer.parseInt(getParameter("w"));
        offScreenHeight = Integer.parseInt(getParameter("h"));
        stage.set = Integer.parseInt(getParameter("s"));
        stage.soundFlag = Integer.parseInt(getParameter("v"));
        stage.puyoFlag = Integer.parseInt(getParameter("p"));
        // 仮想画面を定義
        offscreenImage = createImage(offScreenWidth, offScreenHeight);
        ofscreenGraphics = offscreenImage.getGraphics();
        // 音楽ファイル読み込み
        clips = new AudioClip[6];
//        clips[0] = getAudioClip(getDocumentBase(), "sound/ca_at.au"); // BGM
        clips[0] = getAudioClip(getDocumentBase(), "sound/puyo_08.mid"); // BGM
        clips[1] = getAudioClip(getDocumentBase(), "sound/728.au"); // 終了
        clips[2] = getAudioClip(getDocumentBase(), "sound/pyoro22.au"); // 移動
        clips[3] = getAudioClip(getDocumentBase(), "sound/puu58.au"); // 回転
        clips[4] = getAudioClip(getDocumentBase(), "sound/puu47.au"); // 積み上げ
        clips[5] = getAudioClip(getDocumentBase(), "sound/open23.au"); // おじゃま
        // 画像読み込み
        wallImage = getImage(getDocumentBase(), "img/wall.gif");
        fieldImage = getImage(getDocumentBase(), "img/dt13.gif");
        nextImage = getImage(getDocumentBase(), "img/next.gif");
        images = new Image[12];
        images[0] = getImage(getDocumentBase(), "img/back.gif");
        images[1] = getImage(getDocumentBase(), "img/puyo/gray.gif");
        images[2] = getImage(getDocumentBase(), "img/puyo/red.gif");
        images[3] = getImage(getDocumentBase(), "img/puyo/yellow.gif");
        images[4] = getImage(getDocumentBase(), "img/puyo/blue.gif");
        images[5] = getImage(getDocumentBase(), "img/puyo/green.gif");
        images[6] = getImage(getDocumentBase(), "img/puyo/purple.gif");
        // キーコード取得
        addKeyListener(controller);
        // 初期化
        stage.init();
        puyoSize = 16;
        startFlag = 0;
        stopFlag = 0;
    }

    /** */
    private KeyListener controller = new KeyAdapter() {
        /* */
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_R) {
                // リセット
                stopClips();
                stage.init();
                puyoSize = 16;
                startFlag = 0;
                stopFlag = 0;
                for (int i = 0; i < stage.playersCount; i++) {
                    stage.games[i].init();
                }
                repaint();
            } else if (startFlag == 0 && keyCode == KeyEvent.VK_S) {
                // スタート
                startFlag = 1;
                if (stage.soundFlag == 1) {
                    clips[0].loop();
                }
                for (int i = 0; i < stage.playersCount; i++) {
                    stage.games[i].start();
                }
            } else if (startFlag == 1 && stage.games[0].waitFlag == 0) {
                // ゲームがスタートしていたら

                // ストップ
                if (keyCode == KeyEvent.VK_S && stage.playersCount == 1) {
                    if (stopFlag == 0) {
                        stage.games[0].waitFlag = 1;
                        stage.games[0].sleep(PuyoPuyo.FallSpeed, "Stop");
                    } else {
                        stage.games[0].autoFall();
                    }
                    // ゲーム再開＆一時停止
                    if (stopFlag == 1) {
                        stopFlag = 0;
                    } else {
                        stopFlag = 1;
                    }
                    // 描画
                    repaint();
                }
                // 移動・回転・オートモード
                if (stopFlag == 0 && stage.games[0].waitFlag == 0) {
                    if (keyCode == KeyEvent.VK_X) { // 回転
                        stage.games[0].rotate(1);
                    } else if (keyCode == KeyEvent.VK_Z) { // 回転
                        stage.games[0].rotate(2);
                    } else if (keyCode == KeyEvent.VK_LEFT) { // 左移動
                        stage.games[0].left();
                    } else if (keyCode == KeyEvent.VK_RIGHT) { // 右移動
                        stage.games[0].right();
                    } else if (keyCode == KeyEvent.VK_DOWN) { // 下移動
                        stage.games[0].down();
                    } else if (keyCode == KeyEvent.VK_UP) { // 上移動
                        stage.games[0].up();
                    } else if (keyCode == KeyEvent.VK_SPACE) { // 一気に下移動
                        stage.games[0].bottom();
                    } else if (keyCode == KeyEvent.VK_A) { // オートモード切り替え
                        if (stage.games[0].autoFlag == 0) {
                            stage.games[0].autoFlag = 1;
                            stage.games[0].autoMove();
                        } else {
                            stage.games[0].autoFlag = 0;
                        }
                        repaint();
                    } else if (keyCode == KeyEvent.VK_R) { // repaint
                        repaint();
                    }
                }
            }
        }
    };

    /* */
    public void start() {
        // フォーカスを合わせる
        this.requestFocus();
        // オブジェクトを定義
        fieldLefts = new int[stage.playersCount];
        fieldTops = new int[stage.playersCount];
        for (int i = 0; i < stage.playersCount; i++) {
            stage.games[i] = new PuyoPuyo(stage, i);
            stage.games[i].setView(this);
            // フィールド開始位置
            fieldLefts[i] = (i % 4) * ((stage.columns + 2) * puyoSize + 100);
            fieldTops[i] = (i - i % 4) / 4 * (stage.lows * puyoSize + 44);
        }
    }

    /* */
    public void stop() {
        for (int i = 0; i < stage.playersCount; i++) {
            stage.gameFlags[i] = 0;
        }
        stopClips();
    }

    /* */
    public void update(Graphics g) {
        paint(g);
    }

    /* */
    synchronized public void paint(Graphics g) {
        // フィールドを表示
        for (int n = 0; n < stage.playersCount; n++) {
            // 背景画像
            ofscreenGraphics.drawImage(fieldImage, fieldLefts[n], fieldTops[n], this);
            // 壁
            for (int i = 2; i < stage.lows; i++) {
                ofscreenGraphics.drawImage(wallImage, fieldLefts[n], i * puyoSize + fieldTops[n], this);
                ofscreenGraphics.drawImage(wallImage, fieldLefts[n] + (stage.columns + 1) * puyoSize, i * puyoSize + fieldTops[n], this);
            }
            for (int j = 0; j < stage.columns + 2; j++) {
                ofscreenGraphics.drawImage(wallImage, fieldLefts[n] + j * puyoSize, stage.lows * puyoSize + fieldTops[n], this);
            }
            // ぷよ
            for (int i = 2; i < stage.lows; i++) {
                for (int j = 0; j < stage.columns; j++) {
                    ofscreenGraphics.drawImage(images[0], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], this);
                    ofscreenGraphics.drawImage(images[stage.games[n].grid[i][j]], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], this);
                }
            }
            // 予告おじゃま
            ofscreenGraphics.drawImage(images[1], puyoSize + fieldLefts[n], puyoSize + fieldTops[n], this);
            // 文字
            ofscreenGraphics.setColor(new Color(0, 0, 0));
            ofscreenGraphics.setFont(new Font("ＭＳ Ｐゴシック", Font.PLAIN, 12));
            ofscreenGraphics.drawString("Next", 137 + fieldLefts[n], 41 + fieldTops[n]); // NEXT
            ofscreenGraphics.drawString("" + stage.disturbCounts[n], 38 + fieldLefts[n], 30 + fieldTops[n]); // おじゃま
            ofscreenGraphics.drawString(stage.games[n].overMessage, 135 + fieldLefts[n], 130 + fieldTops[n]); // ゲームオーバー
            if (stopFlag == 1) { // Stop
                ofscreenGraphics.drawString("STOP", 135 + fieldLefts[n], 190 + fieldTops[n]);
            }
            if (state != "test") {
                ofscreenGraphics.drawString("Score: " + stage.games[n].score, 135 + fieldLefts[n], 170 + fieldTops[n]); // Score
                if (stage.games[n].autoFlag == 1) { // Auto
                    ofscreenGraphics.drawString("AUTO", 135 + fieldLefts[n], 210 + fieldTops[n]);
                }
            }
            // 連鎖
            if (stage.games[n].chainCount >= 1) {
                ofscreenGraphics.drawString(stage.games[n].message + "！", 135 + fieldLefts[n], 130 + fieldTops[n]);
                ofscreenGraphics.drawString("(" + stage.games[n].chainCount + "連鎖)", 135 + fieldLefts[n], 145 + fieldTops[n]);
            }
            // 今ぷよ
            if ((stage.games[n].waitFlag == 0 || stopFlag == 1) && stage.gameFlags[n] == 1) {
                if (stage.games[n].pos[0][0] > 1) {
                    ofscreenGraphics.drawImage(images[stage.games[n].puyo1], (stage.games[n].pos[0][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[0][0]) * puyoSize + fieldTops[n], this);
                }
                if (stage.games[n].pos[1][0] > 1) {
                    ofscreenGraphics.drawImage(images[stage.games[n].puyo2], (stage.games[n].pos[1][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[1][0]) * puyoSize + fieldTops[n], this);
                }
            }
            // NEXTぷよ
            ofscreenGraphics.drawImage(nextImage, 138 + fieldLefts[n], 47 + fieldTops[n], this);
            ofscreenGraphics.drawImage(images[stage.games[n].npuyo1], 143 + fieldLefts[n], 51 + fieldTops[n], this);
            ofscreenGraphics.drawImage(images[stage.games[n].npuyo2], 143 + fieldLefts[n], 67 + fieldTops[n], this);
            ofscreenGraphics.drawImage(images[stage.games[n].nnpuyo1], 159 + fieldLefts[n], 59 + fieldTops[n], this);
            ofscreenGraphics.drawImage(images[stage.games[n].nnpuyo2], 159 + fieldLefts[n], 75 + fieldTops[n], this);
            // テスト用
            if (state.equals("test")) {
                ofscreenGraphics.setColor(new Color(0, 0, 0));
                // 文字
                ofscreenGraphics.drawString(stage.games[n].x, 10 + fieldLefts[n], 260 + fieldTops[n]); // "Chain=" + G[n].max_chain_num + ", " +
                // 配列
                for (int i = 0; i < stage.lows; i++) {
                    for (int j = 0; j < stage.columns; j++) {
                        if (stage.games[n].lastIgnitionLabel2[i][j] > 1) {
                            ofscreenGraphics.setColor(new Color(255, 0, 0));
                        } else if (stage.games[n].lastIgnitionLabel2[i][j] == 1) {
                            ofscreenGraphics.setColor(new Color(0, 0, 255));
                        } else {
                            ofscreenGraphics.setColor(new Color(0, 0, 0));
                        }
                        ofscreenGraphics.drawString("" + stage.games[n].lastChainLabel[i][j], 11 * j + 150 + fieldLefts[n], 11 * i + 100 + fieldTops[n]);
                    }
                }
            }
        }
        // 一括表示
        g.drawImage(offscreenImage, 0, 0, null);
    }

    /* */
    @Override
    public void play(int folder, int cn) {
        play(getDocumentBase(), "sound/chain/" + folder + "/" + cn + ".wav");
    }

    /* */
    @Override
    public void playClip(int i) {
        clips[i].play();
    }
}

/* */