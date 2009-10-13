/*
 * Tetris
 */

using System;
using System.Threading;


namespace Tetris {

    /// <summary>
    /// Tetris.
    ///
    /// @author ‘å’J —z–¾
    /// </summary>
    public class Tetris {

        public interface View {
            void repaint();
            void drawImage(int c, int l, int x, int y);
        }

        private View view;

        public void setView(View view) {
            this.view = view;
        }

        public void keyUp() {
            upKey++;
        }

        public void keyDown() {
            downKey++;
        }

        public void keyLeft() {
            leftKey++;
        }

        public void keyRight() {
            rightKey++;
        }

        public void keyRotate() {
            rotationKey++;
        }

        private int leftKey;
        private int rightKey;
        private int downKey;
        private int upKey;
        private int rotationKey;
        private int level;
        private int levelCount;
        private int score;
        private int[] nyn = { 1, 1, 1 };
        private int[,] name = {
            { 70, 76, 74 },
            { 81, 79, 61 },
            { 65, 74, 67 },
            { 70, 61, 72 },
            { 61, 74, 61 },
            { 71, 72, 73 },
            { 76, 63, 73 },
            { 61, 80, 73 },
            { 64, 82, 64 },
            { 84, 85, 86 }
        };
        private int[] scores = {
            10000, 9000, 8000, 7000, 6000, 5000, 4000, 3000, 2000, 1000
        };
        private int[] lines = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        private int nextPattern;
        private int counter;
        private int[] xa = new int[4];
        private int[] ya = new int[4];
        private int[] xb = new int[4];
        private int[] yb = new int[4];
        private int[] xx = new int[4];
        private int[] yy = new int[4];
        private int kx;
        private int ky;
        private int kaitenx;
        private int kaiteny;
        private int[] speed = {
            40, 30, 20, 10, 7, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 1, 1, 1, 1, 1
        };
        private int[] tktn = { 0, 50, 100, 300, 1000 };
        private int[,] map = new int[20, 10];
        private int xxa;
        private int yya;
        private int xxx;
        private int yyy;
        private int kaitensu;
        private int pattern;
        private int gover;
        private int dflag;
        private int[] kesu = new int[20];
        private int nyi;
        private int nyx;
        private int nyy;
        private int ranking;
        private bool loopFlag;
        private bool rankFlag;
        private int lineCount;
        private int tensu;
        private int patternFlag;
        private int lsu;
        private int clearFlag;

        /** */
        private void keyRset() {
            upKey = leftKey = rightKey = downKey = rotationKey = 0;
        }

        /** */
        public void loop() {
            while (true) {
                keyRset();
                showTitle(0);
                view.repaint();
        
    //System.err.println("kfs: " + kfs);
                while (rotationKey == 0) {
                    Thread.Sleep(0);
                }
        
                showTitle(1);
                view.repaint();
                init();
                keyRset();
        
                while (loopFlag) {
                    if (gover != 0) {
                        doGameOver();
                    } else if (clearFlag != 0) {
                        kesuyo();
                    } else {
                        if (nextPattern == -1) {
                            setNext();
                        }
        
                        if (pattern == -1) {
                            setPattern();
                        } else if (gover == 0) {
                            doGame();
                        }
                    }
        
                    if (patternFlag != 0) {
                        view.repaint();
                        patternFlag = 0;
                    }

                    Thread.Sleep(33);
                }

                keyRset();
                loopFlag = true;
                patternFlag = 0;
                nyi = 12;
                nyx = 0;
                nyy = 1;

    //          score += 10000;
                rankFlag = false;

                if (scores[9] <= score) {
                    rankFlag = true;

                    while (loopFlag) {
                        nyuu();

                        if (patternFlag != 0) {
                            view.repaint();
                            patternFlag = 0;
                        }

                        Thread.Sleep(33);
                    }

                    for (int i = nyx; i < 3; i++) {
                        nyn[i] = 1;
                    }

                    for (int i = 0; i < 3; i++) {
                        name[9, i] = nyn[i];
                    }

                    scores[9] = score;
                    lines[9] = lineCount;
                    ranking = 9;

                    for (int i = 9; i >= 1; i--) {
                        if (scores[i] >= scores[i - 1]) {
                            ranking--;
                            scores[i] ^= scores[i - 1];
                            scores[i - 1] ^= scores[i];
                            scores[i] ^= scores[i - 1];
                            lines[i] ^= lines[i - 1];
                            lines[i - 1] ^= lines[i];
                            lines[i] ^= lines[i - 1];

                            for (int j = 0; j < 3; j++) {
                                name[i, j] ^= name[i - 1, j];
                                name[i - 1, j] ^= name[i, j];
                                name[i, j] ^= name[i - 1, j];
                            }
                        }
                    }
                }

                showTitle(2);
                tokuhyou();
                view.repaint();

                while (rotationKey == 0) {
                    Thread.Sleep(0);
                }
            }
        }

        /** */
        private void tokuhyou() {
            int a;
            int b;
            int c;
            int d;

            for (int i = 0; i < 10; i++) {
                if (i == ranking && rankFlag == true) {
                    d = 7;
                } else {
                    d = 0;
                }

                for (int j = 0; j < 3; j++) {
                    c = name[i, j];
                    b = c / 20;
                    a = c % 20;

                    if (name[i, j] > 1) {
                        b += d;
                    }

                    view.drawImage(a, b, j + 13, 5 + (i * 2));
                }

                c = scores[i];
                a = 25;

                while (c > 0) {
                    b = (9 + (c % 10));
                    c /= 10;
                    view.drawImage(b, 2 + d, a, 5 + (i * 2));
                    a--;
                }

                c = lines[i];
                a = 32;

                while (c > 0) {
                    b = (9 + (c % 10));
                    c /= 10;
                    view.drawImage(b, 2 + d, a, 5 + (i * 2));
                    a--;
                }
            }

            if (rankFlag == true) {
                c = ranking + 1;
                a = 8;

                while (c > 0) {
                    b = (9 + (c % 10));
                    c /= 10;
                    view.drawImage(b, 2 + 7, a, 5 + (ranking * 2));
                    a--;
                }
            }
        }

        /** */
        private void nyuu() {
            int a;
            int b;
            int nad = 49;

            if (rotationKey > 0) {
                if (nyi == 38) {
                    loopFlag = false;
                } else {
                    nyn[nyx] = nyi + nad;
                    nyx++;
                }

                nyy = 1;
                rotationKey = 0;
            }

            if (rightKey > 0) {
                if (nyi == 38) {
                    loopFlag = false;
                } else {
                    nyn[nyx] = nyi + nad;
                    nyx++;
                }

                nyy = 1;
                rightKey = 0;
            }

            if (leftKey > 0) {
                leftKey = 0;

                if (nyx > 0) {
                    nyx--;
                    nyi = nyn[nyx] - nad;

                    if (map[10, 3 + nyx + 1] != 0) {
                        b = 15;
                    } else {
                        b = 1;
                    }

                    view.drawImage(b, 0, 18 + nyx + 1, 13);
                    nyy = 1;
                }
            }

            if (downKey > 0 || upKey > 0 || nyy != 0) {
                if (nyy != 0) {
                    nyy = 0;
                }

                if (downKey > 0) {
                    downKey--;
                    nyi = (nyi + 1) % 39;
                }

                if (upKey > 0) {
                    upKey--;
                    nyi = (nyi + 38) % 39;
                }

                if (nyx >= 3) {
                    nyi = 38;
                    nyx = 3;
                }

                b = (nad + nyi) / 20;
                a = (nad + nyi) % 20;
                view.drawImage(a, b, 18 + nyx, 13);
                patternFlag = 1;
            }
        }

        /** */
        private void doGameOver() {
            int a;
            int b;
            int c;
            counter++;
            a = counter % 10;
            b = counter / 10;

            if (counter >= 65) {
                if (counter > 150) {
                    loopFlag = false;
                }

                a = 19 - (counter - 65);

                if (a >= 0) {
                    for (int i = 0; i < 10; i++) {
                        if (map[a, i] != 0) {
                            b = 15;
                        } else {
                            b = 1;
                        }

                        view.drawImage(b, 0, 15 + i, 3 + a);
                        patternFlag = 1;
                    }
                }
            } else {
                if (a == 0) {
                    for (int i = 0; i < 4; i++) {
                        c = pattern + 2;

                        if ((b % 2) == 0) {
                            c = map[yb[i] + yyy, xb[i] + xxx] + 1;
                        }

                        view.drawImage(c, 0, 15 + xb[i] + xxx, 3 + yb[i] + yyy);
                    }

                    patternFlag = 1;
                }
            }
        }

        /** */
        private void kesuyo() {
            int a = 0;
            int b = 0;
            int c = 0;
            int d = 0;
            counter++;

            switch (counter) {
            case 3:
                a = 9;
                break;
            case 6:
                a = 10;
                break;
            case 9:
                a = 11;
                break;
            case 12:
                a = 12;
                break;
            case 15:
                a = 13;
                break;
            case 18:
                a = 14;
                break;
            }

            if (counter >= 30) {
                int i = 19;

                while (i > 0) {
                    if (kesu[i] == 10) {
                        for (int j = i; j > 0; j--) {
                            kesu[j] = kesu[j - 1];

                            for (int k = 0; k < 10; k++) {
                                map[j, k] = map[j - 1, k];
                            }
                        }
                    } else {
                        i--;
                    }
                }

                for (i = 0; i < 20; i++) {
                    for (int j = 0; j < 10; j++) {
                        view.drawImage(map[i, j] + 1, 0, 15 + j, 3 + i);
                    }
                }

                if (score > 999999) {
                    score = 999999;
                }

                a = score;
                b = 11;

                while (a > 0) {
                    c = a % 10;
                    a /= 10;
                    view.drawImage(c + 9, 2, b, 3);
                    b--;
                }

                a = lineCount;
                b = 11;

                while (a > 0) {
                    c = a % 10;
                    a /= 10;
                    view.drawImage(c + 9, 2, b, 6);
                    b--;
                }

                a = level;
                b = 11;

                while (a > 0) {
                    c = a % 10;
                    a /= 10;
                    view.drawImage(c + 9, 2, b, 9);
                    b--;
                }

                keyRset();
                clearFlag = 0;
            }

            if (a > 0) {
                for (int i = 0; i < 20; i++) {
                    if (kesu[i] == 10) {
                        b++;

                        for (int j = 0; j < 10; j++) {
                            view.drawImage(a, 0, 15 + j, 3 + i);
                        }

                        d = i;
                    }
                }

                if (a == 14) {
                    int i;
                    c = tktn[b];

                    if (c >= 100) {
                        b = 6;
                    } else {
                        b = 5;
                    }

                    while (c > 0) {
                        i = c % 10;
                        c /= 10;
                        view.drawImage(i + 9, 2, 15 + b, 3 + d);
                        b--;
                    }
                }

                patternFlag = 1;
            }
        }

        /** */
        private void doGame() {
            int chflag = 0;
            counter++;

            while (rotationKey > 0) {
                rotationKey--;

                int j = 0;
                int[] aa = new int[4];
                int[] bb = new int[4];

                if ((pattern == 1 || pattern == 4 || pattern == 5) && kaitensu == 1) {
                    for (int i = 0; i < 4; i++) {
                        aa[i] = -yb[i] + ky;
                        bb[i] = xb[i] - ky;

                        if ((xxx + aa[i]) < 0 || (xxx + aa[i]) > 9 ||
                            (yyy + bb[i]) < 0 || (yyy + bb[i]) > 19) {
                            j = 1;
                        } else if (map[yyy + bb[i], xxx + aa[i]] != 0) {
                            j = 1;
                        }
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        aa[i] = yb[i] + ky;
                        bb[i] = -xb[i] + kx;

                        if ((xxx + aa[i]) < 0 || (xxx + aa[i]) > 9 ||
                            (yyy + bb[i]) < 0 || (yyy + bb[i]) > 19) {
                            j = 1;
                        } else if (map[yyy + bb[i], xxx + aa[i]] != 0) {
                            j = 1;
                        }
                    }
                }

                if ((j == 0) && (pattern > 0)) {
                    kaitensu = (kaitensu + 1) % 2;

                    for (int i = 0; i < 4; i++) {
                        xb[i] = aa[i];
                        yb[i] = bb[i];
                    }

                    chflag = 1;
                }
            }

            while (rightKey > 0) {
                rightKey--;

                int j = 0;

                for (int i = 0; i < 4; i++) {
                    if (xb[i] + xxx > 8) {
                        j = 1;
                    } else if (map[yb[i] + yyy, xb[i] + xxx + 1] != 0) {
                        j = 1;
                    }
                }

                if (j == 0) {
                    xxx++;
                    chflag = 1;
                }
            }

            while (leftKey > 0) {
                leftKey--;

                int j = 0;

                for (int i = 0; i < 4; i++) {
                    if (xb[i] + xxx < 1) {
                        j = 1;
                    } else if (map[yb[i] + yyy, (xb[i] + xxx) - 1] != 0) {
                        j = 1;
                    }
                }

                if (j == 0) {
                    xxx--;
                    chflag = 1;
                }
            }

            if (counter >= speed[levelCount]) {
                int j = 0;
                counter = 0;

                for (int i = 0; i < 4; i++) {
                    if ((yb[i] + yyy) > 18) {
                        j = 1;
                    } else if (map[yb[i] + yyy + 1, xb[i] + xxx] != 0) {
                        j = 1;
                    }
                }

                if (j == 0) {
                    yyy++;
                    chflag = 1;
                    dflag = 0;
                } else {
                    dflag += speed[levelCount];
                }
            } else if (downKey > 0) {
                score++;
                downKey--;
                counter = 0;

                int j = 0;

                for (int i = 0; i < 4; i++) {
                    if ((yb[i] + yyy) > 18) {
                        j = 1;
                    } else if (map[yb[i] + yyy + 1, xb[i] + xxx] != 0) {
                        j = 1;
                    }
                }

                if (j == 0) {
                    yyy++;
                    chflag = 1;
                    dflag = 0;
                } else {
                    dflag += speed[levelCount];
                }

                tensu++;
            }

            if (chflag != 0) {
                patternFlag = 1;

                for (int i = 0; i < 4; i++) {
                    view.drawImage(1, 0, 15 + xxa + xa[i], 3 + yya + ya[i]);
                }

                for (int i = 0; i < 4; i++) {
                    view.drawImage(pattern + 2, 0, 15 + xxx + xb[i], 3 + yyy + yb[i]);
                    xa[i] = xb[i];
                    ya[i] = yb[i];
                }

                xxa = xxx;
                yya = yyy;
            }

            if (dflag > 6) {
                int a;
                int b;
                int c;
                lsu = 0;

                for (int i = 0; i < 4; i++) {
                    map[yyy + yb[i], xxx + xb[i]] = pattern + 1;
                    kesu[yyy + yb[i]]++;
                }

                for (int i = 0; i < 20; i++) {
                    if (kesu[i] == 10) {
                        clearFlag = 1;
                        counter = 0;
                        lsu++;
                    }
                }

                pattern = nextPattern;
                nextPattern = -1;
                lineCount += lsu;
                keyRset();

                if (lineCount > 999) {
                    lineCount = 999;
                }

                level = lineCount / 4;
                levelCount = level;

                if (level > 99) {
                    level = 99;
                }

                if (levelCount > 19) {
                    levelCount = 19;
                }

                a = score;
                b = 11;

                if (score > 99999) {
                    score = 99999;
                }

                while (a > 0) {
                    c = a % 10;
                    a /= 10;
                    view.drawImage(c + 9, 2, b, 3);
                    b--;
                }

                score += tktn[lsu];
            }
        }

        /** */
        private void setPattern() {
            counter++;

            if (counter >= 30) {
                pattern = nextPattern;
                nextPattern = -1;

                for (int i = 0; i < 20; i++) {
                    kesu[i] = 0;
                }
            }
        }

        private Random random = new Random(Environment.TickCount);

        /** */
        private void setNext() {
            nextPattern = (int) ((random.NextDouble() - 0.00001) * 7.0);
            dflag = counter = 0;

            for (int i = 0; i < 4; i++) {
                xb[i] = xx[i];
                yb[i] = yy[i];
            }

            kx = kaitenx;
            ky = kaiteny;

            switch (nextPattern) {
            case 0:
                xx[0] = 0;
                yy[0] = 0;
                xx[1] = 1;
                yy[1] = 0;
                xx[2] = 0;
                yy[2] = 1;
                xx[3] = 1;
                yy[3] = 1;
                kaitenx = 0;
                kaiteny = 1;
                break;
            case 1:
                xx[0] = -1;
                yy[0] = 0;
                xx[1] = 0;
                yy[1] = 0;
                xx[2] = 1;
                yy[2] = 0;
                xx[3] = 2;
                yy[3] = 0;
                kaitenx = 1;
                kaiteny = 1;
                break;
            case 2:
                xx[0] = -1;
                yy[0] = 0;
                xx[1] = 0;
                yy[1] = 0;
                xx[2] = 1;
                yy[2] = 0;
                xx[3] = -1;
                yy[3] = 1;
                kaitenx = 0;
                kaiteny = 0;
                break;
            case 3:
                xx[0] = -1;
                yy[0] = 0;
                xx[1] = 0;
                yy[1] = 0;
                xx[2] = 1;
                yy[2] = 0;
                xx[3] = 1;
                yy[3] = 1;
                kaitenx = 0;
                kaiteny = 0;
                break;
            case 4:
                xx[0] = 0;
                yy[0] = 0;
                xx[1] = 1;
                yy[1] = 0;
                xx[2] = -1;
                yy[2] = 1;
                xx[3] = 0;
                yy[3] = 1;
                kaitenx = 0;
                kaiteny = 0;
                break;
            case 5:
                xx[0] = -1;
                yy[0] = 0;
                xx[1] = 0;
                yy[1] = 0;
                xx[2] = 0;
                yy[2] = 1;
                xx[3] = 1;
                yy[3] = 1;
                kaitenx = 0;
                kaiteny = 0;
                break;
            default:
                xx[0] = -1;
                yy[0] = 0;
                xx[1] = 0;
                yy[1] = 0;
                xx[2] = 1;
                yy[2] = 0;
                xx[3] = 0;
                yy[3] = 1;
                kaitenx = 0;
                kaiteny = 0;
                break;
            }

            xxx = 4;
            yyy = 0;
            kaitensu = 0;

            if (pattern != -1) {
                for (int i = 0; i < 4; i++) {
                    if (map[yyy + yb[i], xxx + xb[i]] != 0) {
                        gover = 1;
                        counter = 0;
                    } else {
                        xa[i] = xb[i];
                        ya[i] = yb[i];
                    }

    //    		    if (gover != 0) {
    //                  tetrismap[yyy + yb[i]][xxx + xb[i]] = pattern + 1;
    //              }
                }

                for (int i = 0; i < 4; i++) {
                    view.drawImage(pattern + 2, 0, 15 + xb[i] + xxx, 3 + yb[i] + yyy);
                    xxa = xxx;
                    yya = yyy;
                }
            }

            for (int i = 0; i < 4; i++) {
                view.drawImage(1, 0, 19 + xb[i], yb[i]);
            }

            if (gover == 0) {
                for (int i = 0; i < 4; i++) {
                    view.drawImage(nextPattern + 2, 0, 19 + xx[i], yy[i]);
                }
            }

            patternFlag = 1;
        }

        /** */
        private void init() {
            pattern = nextPattern = -1;
            score =
                lineCount =
                    levelCount = clearFlag = patternFlag = tensu = level = gover = counter = 0;
            loopFlag = true;

            for (int i = 0; i < 20; i++) {
                kesu[i] = 0;

                for (int j = 0; j < 10; j++) {
                    map[i, j] = 0;
                }
            }

            for (int i = 0; i < 4; i++) {
                xb[i] = xx[i] = yb[i] = yy[i] = 0;
            }
        }

        /** */
        private void showTitle(int mm) {

            for (int j = 0; j < 25; j++) {
                for (int i = 0; i < 40; i++) {
                    int c = maps[mm, (j * 40) + i];
                    int a = c % 20;
                    int b = c / 20;
                    view.drawImage(a, b, i, j);
                }
            }
        }

        /** */
        private static int[,] maps = {
            {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                151, 1, 1, 1, 151, 151, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 151, 1, 151, 151, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 151,
                151, 1, 1, 151, 151, 1, 1, 1, 1, 1, 151, 1, 1, 1, 151, 1, 1, 151,
                151, 1, 1, 1, 1, 151, 151, 151, 151, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 151, 151, 1, 1, 151, 151, 151, 151, 1, 151, 151, 151, 151, 1, 151,
                151, 151, 151, 151, 151, 1, 151, 151, 1, 151, 151, 151, 151, 1, 151,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 151, 151, 17, 151, 151, 151, 151, 151,
                1, 151, 151, 33, 151, 151, 46, 47, 151, 151, 151, 151, 151, 151, 151,
                121, 151, 151, 151, 125, 126, 151, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 151,
                18, 26, 20, 21, 22, 23, 29, 30, 31, 32, 34, 48, 88, 89, 90, 91, 97,
                98, 99, 100, 101, 120, 122, 151, 127, 128, 129, 130, 151, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 151, 151, 19, 151, 25, 151, 24, 151, 26, 151, 151,
                35, 92, 93, 25, 151, 94, 102, 103, 151, 104, 105, 123, 151, 131, 132,
                151, 151, 151, 151, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 151, 151, 151, 151,
                25, 151, 151, 151, 26, 37, 151, 151, 151, 151, 25, 151, 151, 106,
                103, 107, 108, 151, 123, 151, 151, 133, 134, 135, 151, 151, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 151, 151, 151, 151, 25, 151, 151, 36, 26, 38,
                39, 151, 151, 151, 25, 151, 151, 109, 110, 111, 112, 113, 123, 151,
                151, 151, 151, 136, 137, 151, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 151, 151,
                151, 151, 25, 151, 151, 151, 26, 151, 151, 43, 151, 151, 25, 151,
                151, 106, 103, 151, 114, 115, 123, 151, 138, 139, 151, 140, 141, 151,
                1, 1, 1, 1, 1, 152, 152, 152, 152, 152, 151, 151, 151, 27, 26, 28,
                151, 40, 26, 41, 42, 44, 151, 27, 95, 96, 151, 116, 117, 151, 118,
                119, 124, 96, 142, 143, 144, 145, 146, 151, 152, 152, 152, 152, 152,
                153, 153, 153, 153, 153, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 45, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 153, 153, 153, 153, 153, 153, 153,
                153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153,
                153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 153,
                153, 153, 153, 153, 153, 153, 153, 153, 153, 153, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154, 154,
                154, 154, 154, 154, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 155, 155, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 151, 80, 65, 80, 78, 69, 79, 1, 1, 151, 155, 155, 155,
                155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 151,
                1, 79, 81, 76, 65, 78, 1, 1, 151, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 155, 155, 155, 155, 155, 155, 151, 1, 82, 65, 78, 79,
                69, 75, 74, 151, 155, 155, 155, 155, 155, 155, 155, 155, 155, 155,
                155, 155, 155, 155, 155, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151, 151,
                151, 151, 151, 151, 151, 151, 151, 151, 151
            }, {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 74, 65, 84, 80, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 79, 63, 75,
                78, 65, 1, 1, 156, 157, 157, 158, 1, 1, 1, 1, 159, 157, 157, 160, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 49, 1, 1, 161, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 161, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                161, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 161, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 72, 69, 74, 65, 79, 1, 1, 161, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 161, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 49, 1, 1, 162, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 168, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 162, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                168, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                72, 65, 82, 65, 72, 1, 1, 162, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 168, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 49, 1, 1, 162, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 168, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                162, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 168, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 162, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 168, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 163, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 163, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 164, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 164,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 164, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 164, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                165, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 165, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 166, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 170, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 167, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 171, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 167, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 171,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 167, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 171, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                167, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 171, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 165, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 165, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 165, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 165, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 172, 173, 174, 175, 175, 175, 175, 175,
                175, 176, 177, 178, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 179, 180, 181, 182, 182, 182,
                182, 182, 182, 183, 184, 185, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1
            }, {
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 78, 61, 74, 71, 69, 74, 67, 1, 1, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 78, 61, 74, 71, 5, 5, 5, 74,
                61, 73, 65, 5, 5, 5, 79, 63, 75, 78, 65, 5, 5, 5, 5, 72, 69, 74, 65,
                79, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 50, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1,
                1, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 51, 5, 5,
                5, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 52, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1,
                1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 53, 5, 5, 5,
                5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 54, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 1,
                1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 55, 5, 5, 5, 5,
                1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 56, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1,
                1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 57, 5, 5, 5, 5, 1,
                1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 58, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1,
                5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 50, 49, 5, 5, 5, 5, 1,
                1, 1, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5, 1, 1, 1, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            }
        };
    }
}

/* */
