/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.games.puyopuyo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;


/**
 * PuyoPuyoApp
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 090106 nsano initial version <br>
 */
public class PuyoPuyoApp extends Activity {

    /* */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        MyView view = new MyView(this);
        setContentView(view);
        view.start();
    }

    /** */
    private class MyView extends View implements PuyoPuyo.View {
        /** */
        private PuyoPuyo.Stage stage;
    
        /** */
        private Bitmap offscreenImage;
        /** */
        private Canvas ofscreenGraphics;
        /** */
        private Paint paint = new Paint();
        /** */
        private Bitmap wallImage;
        /** */
        private Bitmap fieldImage;
        /** */
        private Bitmap nextImage;
        /** */
        private Bitmap[] images;
    
        /** */
        private int offScreenWidth;
        /** */
        private int offScreenHeight;
        /** */
        private MediaPlayer[] clips;
    
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
    
        /** */
        MyView(Context context) {
            super(context);

            setFocusable(true);

            Resources r = getContext().getResources();

            // パラメータを取得
            int playersCount = Integer.parseInt(r.getString(R.string.main_activity_n));
            stage = new PuyoPuyo.Stage(playersCount);
            offScreenWidth = Integer.parseInt(r.getString(R.string.main_activity_w));
            offScreenHeight = Integer.parseInt(r.getString(R.string.main_activity_h));
            stage.set = Integer.parseInt(r.getString(R.string.main_activity_s));
            stage.soundFlag = Integer.parseInt(r.getString(R.string.main_activity_v));
            stage.puyoFlag = Integer.parseInt(r.getString(R.string.main_activity_p));
            // 仮想画面を定義
            offscreenImage = Bitmap.createBitmap(offScreenWidth, offScreenHeight, Bitmap.Config.ARGB_8888);
            ofscreenGraphics = new Canvas(offscreenImage);
            // 音楽ファイル読み込み
            clips = new MediaPlayer[6];
            clips[0] = MediaPlayer.create(context, R.raw.puyo_08); // BGM
            clips[1] = MediaPlayer.create(context, R.raw.a728); // 終了
            clips[2] = MediaPlayer.create(context, R.raw.pyoro22); // 移動
            clips[3] = MediaPlayer.create(context, R.raw.puu58); // 回転
            clips[4] = MediaPlayer.create(context, R.raw.puu47); // 積み上げ
            clips[5] = MediaPlayer.create(context, R.raw.open23); // おじゃま
            // 画像読み込み
            wallImage = BitmapFactory.decodeResource(r, R.drawable.wall);
            fieldImage = BitmapFactory.decodeResource(r, R.drawable.dt13);
            nextImage = BitmapFactory.decodeResource(r, R.drawable.next);
            images = new Bitmap[12];
            images[0] = BitmapFactory.decodeResource(r, R.drawable.back);
            images[1] = BitmapFactory.decodeResource(r, R.drawable.gray);
            images[2] = BitmapFactory.decodeResource(r, R.drawable.red);
            images[3] = BitmapFactory.decodeResource(r, R.drawable.yellow);
            images[4] = BitmapFactory.decodeResource(r, R.drawable.blue);
            images[5] = BitmapFactory.decodeResource(r, R.drawable.green);
            images[6] = BitmapFactory.decodeResource(r, R.drawable.purple);
            // 初期化
            stage.init();
            puyoSize = 16;
            startFlag = 0;
            stopFlag = 0;
        }
    
        /** */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_R) {
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
            } else if (startFlag == 0 && keyCode == KeyEvent.KEYCODE_S) {
                // スタート
                startFlag = 1;
                if (stage.soundFlag == 1) {
                    clips[0].setLooping(true);
                    clips[0].start();
                }
                for (int i = 0; i < stage.playersCount; i++) {
                    stage.games[i].start();
                }
            } else if (startFlag == 1 && stage.games[0].waitFlag == 0) {
                // ゲームがスタートしていたら

                // ストップ
                if (keyCode == KeyEvent.KEYCODE_S && stage.playersCount == 1) {
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
                    if (keyCode == KeyEvent.KEYCODE_X) { // 回転
                        stage.games[0].rotate(1);
                    } else if (keyCode == KeyEvent.KEYCODE_Z) { // 回転
                        stage.games[0].rotate(2);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { // 左移動
                        stage.games[0].left();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { // 右移動
                        stage.games[0].right();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { // 下移動
                        stage.games[0].down();
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { // 上移動
                        stage.games[0].up();
                    } else if (keyCode == KeyEvent.KEYCODE_SPACE) { // 一気に下移動
                        stage.games[0].bottom();
                    } else if (keyCode == KeyEvent.KEYCODE_A) { // オートモード切り替え
                        if (stage.games[0].autoFlag == 0) {
                            stage.games[0].autoFlag = 1;
                            stage.games[0].autoMove();
                        } else {
                            stage.games[0].autoFlag = 0;
                        }
                        repaint();
                    } else if (keyCode == KeyEvent.KEYCODE_R) { // repaint
                        repaint();
                    }
                }
            }
            return super.onKeyDown(keyCode, event);
        }
    
        /* */
        void start() {
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
    
        /** */
        public void repaint() {
            postInvalidate();
        }

        /* */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // フィールドを表示
            for (int n = 0; n < stage.playersCount; n++) {
                // 背景画像
                ofscreenGraphics.drawBitmap(fieldImage, fieldLefts[n], fieldTops[n], null);
                // 壁
                for (int i = 2; i < stage.lows; i++) {
                    ofscreenGraphics.drawBitmap(wallImage, fieldLefts[n], i * puyoSize + fieldTops[n], null);
                    ofscreenGraphics.drawBitmap(wallImage, fieldLefts[n] + (stage.columns + 1) * puyoSize, i * puyoSize + fieldTops[n], null);
                }
                for (int j = 0; j < stage.columns + 2; j++) {
                    ofscreenGraphics.drawBitmap(wallImage, fieldLefts[n] + j * puyoSize, stage.lows * puyoSize + fieldTops[n], null);
                }
                // ぷよ
                for (int i = 2; i < stage.lows; i++) {
                    for (int j = 0; j < stage.columns; j++) {
                        ofscreenGraphics.drawBitmap(images[0], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], null);
                        ofscreenGraphics.drawBitmap(images[stage.games[n].grid[i][j]], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], null);
                    }
                }
                // 予告おじゃま
                ofscreenGraphics.drawBitmap(images[1], puyoSize + fieldLefts[n], puyoSize + fieldTops[n], null);
                // 文字
                paint.setARGB(0xff, 0, 0, 0);
                ofscreenGraphics.drawText("Next", 137 + fieldLefts[n], 41 + fieldTops[n], paint); // NEXT
                ofscreenGraphics.drawText("" + stage.disturbCounts[n], 38 + fieldLefts[n], 30 + fieldTops[n], paint); // おじゃま
                ofscreenGraphics.drawText(stage.games[n].overMessage, 135 + fieldLefts[n], 130 + fieldTops[n], paint); // ゲームオーバー
                if (stopFlag == 1) { // Stop
                    ofscreenGraphics.drawText("STOP", 135 + fieldLefts[n], 190 + fieldTops[n], paint);
                }
                if (state != "test") {
                    ofscreenGraphics.drawText("Score: " + stage.games[n].score, 135 + fieldLefts[n], 170 + fieldTops[n], paint); // Score
                    if (stage.games[n].autoFlag == 1) { // Auto
                        ofscreenGraphics.drawText("AUTO", 135 + fieldLefts[n], 210 + fieldTops[n], paint);
                    }
                }
                // 連鎖
                if (stage.games[n].chainCount >= 1) {
                    ofscreenGraphics.drawText(stage.games[n].message + "！", 135 + fieldLefts[n], 130 + fieldTops[n], paint);
                    ofscreenGraphics.drawText("(" + stage.games[n].chainCount + "連鎖)", 135 + fieldLefts[n], 145 + fieldTops[n], paint);
                }
                // 今ぷよ
                if ((stage.games[n].waitFlag == 0 || stopFlag == 1) && stage.gameFlags[n] == 1) {
                    if (stage.games[n].pos[0][0] > 1) {
                        ofscreenGraphics.drawBitmap(images[stage.games[n].puyo1], (stage.games[n].pos[0][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[0][0]) * puyoSize + fieldTops[n], null);
                    }
                    if (stage.games[n].pos[1][0] > 1) {
                        ofscreenGraphics.drawBitmap(images[stage.games[n].puyo2], (stage.games[n].pos[1][1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[1][0]) * puyoSize + fieldTops[n], null);
                    }
                }
                // NEXTぷよ
                ofscreenGraphics.drawBitmap(nextImage, 138 + fieldLefts[n], 47 + fieldTops[n], null);
                ofscreenGraphics.drawBitmap(images[stage.games[n].npuyo1], 143 + fieldLefts[n], 51 + fieldTops[n], null);
                ofscreenGraphics.drawBitmap(images[stage.games[n].npuyo2], 143 + fieldLefts[n], 67 + fieldTops[n], null);
                ofscreenGraphics.drawBitmap(images[stage.games[n].nnpuyo1], 159 + fieldLefts[n], 59 + fieldTops[n], null);
                ofscreenGraphics.drawBitmap(images[stage.games[n].nnpuyo2], 159 + fieldLefts[n], 75 + fieldTops[n], null);
                // テスト用
                if (state.equals("test")) {
                    paint.setARGB(0xff, 0, 0, 0);
                    // 文字
                    ofscreenGraphics.drawText(stage.games[n].x, 10 + fieldLefts[n], 260 + fieldTops[n], paint); // "Chain=" + G[n].max_chain_num + ", " +
                    // 配列
                    for (int i = 0; i < stage.lows; i++) {
                        for (int j = 0; j < stage.columns; j++) {
                            if (stage.games[n].lastIgnitionLabel2[i][j] > 1) {
                                paint.setARGB(0xff, 255, 0, 0);
                            } else if (stage.games[n].lastIgnitionLabel2[i][j] == 1) {
                                paint.setARGB(0xff, 0, 0, 255);
                            } else {
                                paint.setARGB(0xff, 0, 0, 0);
                            }
                            ofscreenGraphics.drawText("" + stage.games[n].lastChainLabel[i][j], 11 * j + 150 + fieldLefts[n], 11 * i + 100 + fieldTops[n], paint);
                        }
                    }
                }
            }
            // 一括表示
            canvas.drawBitmap(offscreenImage, 0, 0, null);
        }
    
        /* */
//        @Override
        public void play(int folder, int cn) {
//            play(getDocumentBase(), "sound/chain/" + folder + "/" + cn + ".au");
        }
    
        /* */
//        @Override
        public void playClip(int i) {
            clips[i].start();
        }
    }
}

/* */