/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.puyopuyo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;


/**
 * PuyoPuyoApp
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 090106 nsano initial version <br>
 */
public class PuyoPuyoApp implements Xlet {

    /** */
    private HScene scene;
    /** */
    private MyView gui;
    /** */
    private XletContext context;

    /* */
    public void initXlet(XletContext context) {
        this.context = context;

        this.scene = HSceneFactory.getInstance().getDefaultHScene();

        try {
            this.gui = new MyView();

            gui.setSize(640, 400);
            scene.add(gui, BorderLayout.CENTER);
        } catch (Exception e) {
e.printStackTrace(System.err);
        }

        scene.validate();
    }

    private Thread thread;

    /* */
    public void startXlet() {
        gui.setVisible(true);
        scene.setVisible(true);
        gui.requestFocus();

        thread = new Thread(gui);
        thread.start();
    }

    /* */
    public void pauseXlet() {
        gui.setVisible(false);
    }

    /* */
    public void destroyXlet(boolean unconditional) {
        thread.interrupt();
        gui.stop();

        scene.remove(gui);
        scene = null;
    }

    /** */
    private class MyView extends Container implements PuyoPuyo.View, Runnable {

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
//        private MediaPlayer[] clips;
    
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
//            for (int i = 0; i < clips.length; i++) {
//                clips[i].stop();
//            }
        }
    
        /** */
        MyView() {

            addKeyListener(keyListener);

            // パラメータを取得
            String[] args = (String[]) context.getXletProperty(XletContext.ARGS);
            args = new String[] { "2", "456", "272", "0", "1", "2" };

            int playersCount = Integer.parseInt(args[0]); // n
            stage = new PuyoPuyo.Stage(playersCount);
            offScreenWidth = Integer.parseInt(args[1]); // w
            offScreenHeight = Integer.parseInt(args[2]); // h
            stage.set = Integer.parseInt(args[3]); // s
            stage.soundFlag = Integer.parseInt(args[4]); // v
            stage.puyoFlag = Integer.parseInt(args[5]); // p

            // 音楽ファイル読み込み
//          clips = new MediaPlayer[6];
//          clips[0] = MediaPlayer.create(context, R.raw.puyo_08); // BGM
//          clips[1] = MediaPlayer.create(context, R.raw.a728); // 終了
//          clips[2] = MediaPlayer.create(context, R.raw.pyoro22); // 移動
//          clips[3] = MediaPlayer.create(context, R.raw.puu58); // 回転
//          clips[4] = MediaPlayer.create(context, R.raw.puu47); // 積み上げ
//          clips[5] = MediaPlayer.create(context, R.raw.open23); // おじゃま

            // 画像読み込み
            MediaTracker mt = new MediaTracker(this);
            wallImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/wall.gif"));
            mt.addImage(wallImage, 0);
            fieldImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/dt13.gif"));
            mt.addImage(fieldImage, 1);
            nextImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/next.gif"));
            mt.addImage(nextImage, 2);
            images = new Image[12];
            images[0] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/back.gif"));
            mt.addImage(images[0], 3);
            images[1] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gray.gif"));
            mt.addImage(images[1], 4);
            images[2] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/red.gif"));
            mt.addImage(images[2], 5);
            images[3] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/yellow.gif"));
            mt.addImage(images[3], 6);
            images[4] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/blue.gif"));
            mt.addImage(images[4], 7);
            images[5] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/green.gif"));
            mt.addImage(images[5], 8);
            images[6] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/purple.gif"));
            mt.addImage(images[6], 9);
            try { mt.waitForAll(); } catch (InterruptedException e) {}

            // 初期化
            stage.init();
            puyoSize = 16;
            startFlag = 0;
            stopFlag = 0;
        }
    
        /** */
        private KeyListener keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
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
//                        clips[0].setLooping(true);
//                        clips[0].start();
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
    
        /** */
        private boolean notified;

        /* */
        public void addNotify() {
            super.addNotify();
            notified = true;
        }

        /* */
        public void run() {
            while (!notified) {
                Thread.yield();
            }

            // 仮想画面を定義
            offscreenImage = createImage(offScreenWidth, offScreenHeight);
            ofscreenGraphics = offscreenImage.getGraphics();

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
        void stop() {
            for (int i = 0; i < stage.playersCount; i++) {
                stage.gameFlags[i] = 0;
            }
            stopClips();
        }
    
        /* */
        public void paint(Graphics g) {

            // フィールドを表示
            for (int n = 0; n < stage.playersCount; n++) {
                // 背景画像
                ofscreenGraphics.drawImage(fieldImage, fieldLefts[n], fieldTops[n], null);
                // 壁
                for (int i = 2; i < stage.lows; i++) {
                    ofscreenGraphics.drawImage(wallImage, fieldLefts[n], i * puyoSize + fieldTops[n], null);
                    ofscreenGraphics.drawImage(wallImage, fieldLefts[n] + (stage.columns + 1) * puyoSize, i * puyoSize + fieldTops[n], null);
                }
                for (int j = 0; j < stage.columns + 2; j++) {
                    ofscreenGraphics.drawImage(wallImage, fieldLefts[n] + j * puyoSize, stage.lows * puyoSize + fieldTops[n], null);
                }
                // ぷよ
                for (int i = 2; i < stage.lows; i++) {
                    for (int j = 0; j < stage.columns; j++) {
                        ofscreenGraphics.drawImage(images[0], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], null);
                        ofscreenGraphics.drawImage(images[stage.games[n].grid[i][j]], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], null);
                    }
                }
                // 予告おじゃま
                ofscreenGraphics.drawImage(images[1], puyoSize + fieldLefts[n], puyoSize + fieldTops[n], null);
                // 文字
                g.setColor(Color.black);
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
                        ofscreenGraphics.drawImage(images[stage.games[n].puyo1], (stage.games[n].pos[0][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[0][0]) * puyoSize + fieldTops[n], null);
                    }
                    if (stage.games[n].pos[1][0] > 1) {
                        ofscreenGraphics.drawImage(images[stage.games[n].puyo2], (stage.games[n].pos[1][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[1][0]) * puyoSize + fieldTops[n], null);
                    }
                }
                // NEXTぷよ
                ofscreenGraphics.drawImage(nextImage, 138 + fieldLefts[n], 47 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].npuyo1], 143 + fieldLefts[n], 51 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].npuyo2], 143 + fieldLefts[n], 67 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].nnpuyo1], 159 + fieldLefts[n], 59 + fieldTops[n], null);
                ofscreenGraphics.drawImage(images[stage.games[n].nnpuyo2], 159 + fieldLefts[n], 75 + fieldTops[n], null);
                // テスト用
                if (state.equals("test")) {
                    g.setColor(Color.black);
                    // 文字
                    ofscreenGraphics.drawString(stage.games[n].x, 10 + fieldLefts[n], 260 + fieldTops[n]); // "Chain=" + G[n].max_chain_num + ", " +
                    // 配列
                    for (int i = 0; i < stage.lows; i++) {
                        for (int j = 0; j < stage.columns; j++) {
                            if (stage.games[n].lastIgnitionLabel2[i][j] > 1) {
                                g.setColor(Color.red);
                            } else if (stage.games[n].lastIgnitionLabel2[i][j] == 1) {
                                g.setColor(Color.blue);
                            } else {
                                g.setColor(Color.black);
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
        public void play(int folder, int cn) {
//            play(getDocumentBase(), "sound/chain/" + folder + "/" + cn + ".au");
        }
    
        /* */
        public void playClip(int i) {
//            clips[i].start();
        }
    }
}

/* */