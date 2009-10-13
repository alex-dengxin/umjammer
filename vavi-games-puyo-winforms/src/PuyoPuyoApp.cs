/*
 * Copyright (c) 2009 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

using System;
using System.Threading;
using System.Drawing;
using System.Drawing.Imaging;
using System.Windows.Forms;
using System.Media;


namespace PuyoPuyo {

	///
	/// PuyoPuyoApp
	///
	/// @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
	/// @version 0.00 090106 nsano initial version <br>
	///
	public class PuyoPuyoApp : Form, PuyoPuyo.View {
	
        /** */
        private PuyoPuyo.Stage stage;
    
        /** */
        private Bitmap offscreenImage;
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
        private SoundPlayer[] clips;
    
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
            for (int i = 0; i < clips.Length; i++) {
                clips[i].Stop();
            }
        }
    
        /** */
        PuyoPuyoApp() {
            this.SuspendLayout();

            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(456, 272);
            this.Text = "PuyoPuyo (WinForms)";
            this.AutoSize = true;

            this.ResumeLayout(false);

            this.KeyDown += new KeyEventHandler(keyPressed);

            // パラメータを取得
            int playersCount = 2;
            stage = new PuyoPuyo.Stage(playersCount);
            offScreenWidth = 456;
            offScreenHeight = 272;
            stage.setFlag = 0;
            stage.soundFlag = 1;
            stage.puyoFlag = 1;
            // 仮想画面を定義
            offscreenImage = new Bitmap(offScreenWidth, offScreenHeight);
            ofscreenGraphics = Graphics.FromImage(offscreenImage);
            // 音楽ファイル読み込み
            clips = new SoundPlayer[6];
            clips[0] = new SoundPlayer("sticker.wav"); // BGM
Console.WriteLine("BGM: " + clips[0]);
            clips[1] = new SoundPlayer("a728.wav"); // 終了
            clips[2] = new SoundPlayer("pyoro22.wav"); // 移動
            clips[3] = new SoundPlayer("puu58.wav"); // 回転
            clips[4] = new SoundPlayer("puu47.wav"); // 積み上げ
            clips[5] = new SoundPlayer("open23.wav"); // おじゃま
            // 画像読み込み
            wallImage = new Bitmap("wall.gif");
            fieldImage = new Bitmap("dt13.gif");
            nextImage = new Bitmap("next.gif");
            images = new Bitmap[12];
            images[0] = new Bitmap("back.gif");
            images[1] = new Bitmap("gray.gif");
            images[2] = new Bitmap("red.gif");
            images[3] = new Bitmap("yellow.gif");
            images[4] = new Bitmap("blue.gif");
            images[5] = new Bitmap("green.gif");
            images[6] = new Bitmap("purple.gif");
            // 初期化
            stage.init();
            puyoSize = 16;
            startFlag = 0;
            stopFlag = 0;

            new Thread(start).Start();
        }
    
        /** */
        private void keyPressed(object sender, KeyEventArgs e) {
            Keys keyCode = e.KeyCode;
            if (keyCode == Keys.R) {
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
            } else if (startFlag == 0 && keyCode == Keys.S) {
                // スタート
                startFlag = 1;
                if (stage.soundFlag == 1) {
                    clips[0].PlayLooping();
                }
                for (int i = 0; i < stage.playersCount; i++) {
                    stage.games[i].start();
                }
            } else if (startFlag == 1 && stage.games[0].waitFlag == 0) {
                // ゲームがスタートしていたら

                // ストップ
                if (keyCode == Keys.S && stage.playersCount == 1) {
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
                    if (keyCode == Keys.X) { // 回転
                        stage.games[0].rotate(1);
                    } else if (keyCode == Keys.Z) { // 回転
                        stage.games[0].rotate(2);
                    } else if (keyCode == Keys.Left) { // 左移動
                        stage.games[0].left();
                    } else if (keyCode == Keys.Right) { // 右移動
                        stage.games[0].right();
                    } else if (keyCode == Keys.Down) { // 下移動
                        stage.games[0].down();
                    } else if (keyCode == Keys.Up) { // 上移動
                        stage.games[0].up();
                    } else if (keyCode == Keys.Space) { // 一気に下移動
                        stage.games[0].bottom();
                    } else if (keyCode == Keys.A) { // オートモード切り替え
                        if (stage.games[0].autoFlag == 0) {
                            stage.games[0].autoFlag = 1;
                            stage.games[0].autoMove();
                        } else {
                            stage.games[0].autoFlag = 0;
                        }
                        repaint();
                    } else if (keyCode == Keys.R) { // repaint
                        repaint();
                    }
                }
            }
        }
    
        /* */
        void start() {
            // オブジェクトを定義
            fieldLefts = new int[stage.playersCount];
            fieldTops = new int[stage.playersCount];
            for (int i = 0; i < stage.playersCount; i++) {
                stage.games[i] = new PuyoPuyo(stage, i);
                stage.games[i].setView(this);
                // フィールド開始位置
                fieldLefts[i] = (i % 4) * ((stage.columns + 2) * puyoSize + 100);
                fieldTops[i] = (i - i % 4) / 4 * (stage.lows * puyoSize + 44);
Console.WriteLine("fieldTops[" + i + "]" + fieldTops[i]);
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
            Invoke(new repaintDelagate(repaintReal));
        }

        private delegate void repaintDelagate();

        /** */
        private void repaintReal() {
            Invalidate();
        }

        /* */
        protected override void OnPaint(PaintEventArgs args) {
            Image image;
            Rectangle r;

            // フィールドを表示
            for (int n = 0; n < stage.playersCount; n++) {
                // 背景画像
                r = new Rectangle(0, 0, fieldImage.Width, fieldImage.Height);
                ofscreenGraphics.DrawImage(fieldImage, fieldLefts[n], fieldTops[n], r, GraphicsUnit.Pixel);
                // 壁
                for (int i = 2; i < stage.lows; i++) {
                    r = new Rectangle(0, 0, wallImage.Width, wallImage.Height);
                    ofscreenGraphics.DrawImage(wallImage, fieldLefts[n], i * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                    r = new Rectangle(0, 0, wallImage.Width, wallImage.Height);
                    ofscreenGraphics.DrawImage(wallImage, fieldLefts[n] + (stage.columns + 1) * puyoSize, i * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                }
                for (int j = 0; j < stage.columns + 2; j++) {
                    r = new Rectangle(0, 0, wallImage.Width, wallImage.Height);
                    ofscreenGraphics.DrawImage(wallImage, fieldLefts[n] + j * puyoSize, stage.lows * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                }
                // ぷよ
                for (int i = 2; i < stage.lows; i++) {
                    for (int j = 0; j < stage.columns; j++) {
                        r = new Rectangle(0, 0, images[0].Width, images[0].Height);
                        ofscreenGraphics.DrawImage(images[0], puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                        image = images[stage.games[n].grid[i, j]];
                        r = new Rectangle(0, 0, image.Width, image.Height);
                        ofscreenGraphics.DrawImage(image, puyoSize + j * puyoSize + fieldLefts[n], i * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                    }
                }
                // 予告おじゃま
                r = new Rectangle(0, 0, images[1].Width, images[1].Height);
                ofscreenGraphics.DrawImage(images[1], puyoSize + fieldLefts[n], puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                // 文字
                Brush brush = new SolidBrush(Color.FromArgb(0, 0, 0));
                ofscreenGraphics.DrawString("Next", this.Font, brush, 137 + fieldLefts[n], 25 + fieldTops[n]); // NEXT
                ofscreenGraphics.DrawString("" + stage.disturbCounts[n], this.Font, brush, 38 + fieldLefts[n], 14 + fieldTops[n]); // おじゃま
                ofscreenGraphics.DrawString(stage.games[n].overMessage, this.Font, brush, 135 + fieldLefts[n], 98 + fieldTops[n]); // ゲームオーバー
                if (stopFlag == 1) { // Stop
                    ofscreenGraphics.DrawString("STOP", this.Font, brush, 135 + fieldLefts[n], 158 + fieldTops[n]);
                }
                if (state != "test") {
                    ofscreenGraphics.DrawString("Score: " + stage.games[n].score, this.Font, brush, 135 + fieldLefts[n], 138 + fieldTops[n]); // Score
                    if (stage.games[n].autoFlag == 1) { // Auto
                        ofscreenGraphics.DrawString("AUTO", this.Font, brush, 135 + fieldLefts[n], 178 + fieldTops[n]);
                    }
                }
                // 連鎖
                if (stage.games[n].chainCount >= 1) {
                    ofscreenGraphics.DrawString(stage.games[n].message + "！", this.Font, brush, 135 + fieldLefts[n], 98 + fieldTops[n]);
                    ofscreenGraphics.DrawString("(" + stage.games[n].chainCount + "連鎖)", this.Font, brush, 135 + fieldLefts[n], 113 + fieldTops[n]);
                }
                // 今ぷよ
                if ((stage.games[n].waitFlag == 0 || stopFlag == 1) && stage.gameFlags[n] == 1) {
                    if (stage.games[n].pos[0, 0] > 1) {
                        image = images[stage.games[n].puyo1];
                        r = new Rectangle(0, 0, image.Width, image.Height);
                        ofscreenGraphics.DrawImage(image, (stage.games[n].pos[0, 1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[0, 0]) * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                    }
                    if (stage.games[n].pos[1, 0] > 1) {
                        image = images[stage.games[n].puyo2];
                        r = new Rectangle(0, 0, image.Width, image.Height);
                        ofscreenGraphics.DrawImage(image, (stage.games[n].pos[1, 1] + 1) * puyoSize + fieldLefts[n], (stage.games[n].pos[1, 0]) * puyoSize + fieldTops[n], r, GraphicsUnit.Pixel);
                    }
                }
                // NEXTぷよ
                r = new Rectangle(0, 0, nextImage.Width, nextImage.Height);
                ofscreenGraphics.DrawImage(nextImage, 138 + fieldLefts[n], 47 + fieldTops[n], r, GraphicsUnit.Pixel);
                image = images[stage.games[n].npuyo1];
                r = new Rectangle(0, 0, image.Width, image.Height);
                ofscreenGraphics.DrawImage(image, 143 + fieldLefts[n], 51 + fieldTops[n], r, GraphicsUnit.Pixel);
                image = images[stage.games[n].npuyo2];
                r = new Rectangle(0, 0, image.Width, image.Height);
                ofscreenGraphics.DrawImage(image, 143 + fieldLefts[n], 67 + fieldTops[n], r, GraphicsUnit.Pixel);
                image = images[stage.games[n].nnpuyo1];
                r = new Rectangle(0, 0, image.Width, image.Height);
                ofscreenGraphics.DrawImage(image, 159 + fieldLefts[n], 59 + fieldTops[n], r, GraphicsUnit.Pixel);
                image = images[stage.games[n].nnpuyo2];
                r = new Rectangle(0, 0, image.Width, image.Height);
                ofscreenGraphics.DrawImage(image, 159 + fieldLefts[n], 75 + fieldTops[n], r, GraphicsUnit.Pixel);
                // テスト用
                if (state.Equals("test")) {
                    brush = new SolidBrush(Color.FromArgb(0, 0, 0));
                    // 文字
                    ofscreenGraphics.DrawString(stage.games[n].x, this.Font, brush, 10 + fieldLefts[n], 212 + fieldTops[n]); // "Chain=" + G[n].max_chain_num + ", " +
                    // 配列
                    for (int i = 0; i < stage.lows; i++) {
                        for (int j = 0; j < stage.columns; j++) {
                            if (stage.games[n].lastIgnitionLabel2[i, j] > 1) {
                                brush = new SolidBrush(Color.FromArgb(255, 0, 0));
                            } else if (stage.games[n].lastIgnitionLabel2[i, j] == 1) {
                                brush = new SolidBrush(Color.FromArgb(0, 0, 255));
                            } else {
                                brush = new SolidBrush(Color.FromArgb(0, 0, 0));
                            }
                            ofscreenGraphics.DrawString("" + stage.games[n].lastChainLabel[i, j], this.Font, brush, 11 * j + 150 + fieldLefts[n], 11 * i + 52 + fieldTops[n]);
                        }
                    }
                }
            }
            // 一括表示
            Graphics g = args.Graphics;
            r = args.ClipRectangle;
            g.DrawImage(offscreenImage, r, r, GraphicsUnit.Pixel);
        }
    
        /* */
        public void play(int folder, int cn) {
//            play(getDocumentBase(), "sound/chain/" + folder + "/" + cn + ".au");
        }
    
        /* */
        public void playClip(int i) {
            clips[i].Play();
        }

	    /// <summary>
	    /// アプリケーションのメイン エントリ ポイントです。
	    /// </summary>
	    [STAThread]
	    static void Main() {
	        Application.EnableVisualStyles();
	        Application.SetCompatibleTextRenderingDefault(false);
	        Application.Run(new PuyoPuyoApp());
	    }
    }
}

/* */