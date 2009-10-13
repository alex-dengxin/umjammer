using System;
using System.Threading;
using System.Drawing;
using System.Drawing.Imaging;
using System.Windows.Forms;
using System.IO;


namespace Tetris {

    /// <summary>
    /// Tetris .Net
    /// </summary>
    partial class Form1 : Tetris.View {
        /// <summary>
        /// 必要なデザイナ変数です。
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// 使用中のリソースをすべてクリーンアップします。
        /// </summary>
        /// <param name="disposing">マネージ リソースが破棄される場合 true、破棄されない場合は false です。</param>
        protected override void Dispose(bool disposing) {
            if (disposing && (components != null)) {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows フォーム デザイナで生成されたコード

        /// <summary>
        /// デザイナ サポートに必要なメソッドです。このメソッドの内容を
        /// コード エディタで変更しないでください。
        /// </summary>
        private void InitializeComponent() {
            this.SuspendLayout();
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(640, 400);
            this.Name = "Form1";
            this.Text = "Tetris#";
            this.ResumeLayout(false);

        }

        #endregion

        private Thread thread;

        private Tetris game;

        /** */
        private void keyPressed(object sender, KeyEventArgs e) {
            Keys keyCode = e.KeyCode;
//Console.Out.WriteLine("k: " + ((int) keyCode));
            switch (keyCode) {
            case Keys.Up:
//Console.Out.WriteLine("up: " + ((int) Keys.Up));
                game.keyUp();
                break;
            case Keys.Left:
                game.keyLeft();
                break;
            case Keys.Right:
                game.keyRight();
                break;
            case Keys.Down:
                game.keyDown();
                break;
            case Keys.Enter:
                game.keyDown(); // TODO ぎりぎり落ち
                game.keyDown();
                game.keyDown();
                game.keyDown();
                game.keyDown();
                game.keyDown();
                game.keyDown();
                break;
            case Keys.Space:
                game.keyRotate();
                break;
            }    
        }

        private Bitmap image;
        
        private Graphics graphics;

        private Image patterns;

        /** */
        protected void init() {
            this.game = new Tetris();
            game.setView(this);

            this.KeyDown += new KeyEventHandler(keyPressed);

            this.Size = new System.Drawing.Size(640, 422);

            //
            image = new Bitmap(640, 400);
            graphics = Graphics.FromImage(image);

            patterns = new Bitmap("tetris.png");

            thread = new Thread(run);
            thread.Start();
        }

        /** */
        private void run() {
Thread.Sleep(1000);            
            game.loop();
        }

        /* */
        public void drawImage(int c, int l, int x, int y) {
            x <<= 4;
            y <<= 4;
            c <<= 4;
            l <<= 4;
            BeginInvoke(new drawImageDelagate(drawImageReal), c, l, x, y);
        }

        private delegate void drawImageDelagate(int c, int l, int x, int y);

        /** */
        private void drawImageReal(int c, int l, int x, int y) {
            graphics.DrawImage(patterns, new Rectangle(x, y, 16, 16), new Rectangle(c, l, 16, 16), GraphicsUnit.Pixel);
            Invalidate(new Rectangle(x, y, 16, 16));
        }

        public void repaint() {
        }

        protected override void OnPaint(PaintEventArgs args) {
            Graphics g = args.Graphics;
            Rectangle r = args.ClipRectangle;
            g.DrawImage(image, r, r, GraphicsUnit.Pixel);
        }
    }
}

