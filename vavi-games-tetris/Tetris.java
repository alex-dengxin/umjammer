/*
 * tetris
 */

import java.awt.Graphics;


/**
 *
 */
public class Tetris {

    int kf4;
    int kf6;
    int kf2;
    int kf8;
    int kfs;
    int lev;
    int levc;
    int tokuten;
    int[] nyn = { 1, 1, 1 };
    int[][] name = {
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
    int[] score = {
        10000, 9000, 8000, 7000, 6000, 5000, 4000, 3000, 2000, 1000
    };
    int[] lines = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
    int nextptn;
    int counter;
    int[] xa;
    int[] ya;
    int[] xb;
    int[] yb;
    int[] xx;
    int[] yy;
    int kx;
    int ky;
    int kaitenx;
    int kaiteny;
    int[] spd = {
        40, 30, 20, 10, 7, 5, 4, 3, 2, 1, 5, 4, 3, 2, 1, 1, 1, 1, 1, 1
    };
    int[] tktn = { 0, 50, 100, 300, 1000 };
    int[][] tetrismap;
    int xxa;
    int yya;
    int xxx;
    int yyy;
    int kaitensu;
    int ptn;
    int gover;
    int dflag;
    int[] kesu;
    int nyi;
    int nyx;
    int nyy;
    int rkg;
    boolean tetrisloop;
    boolean rkflag;
    int linecount;
    int tensu;
    int ptflag;
    int lsu;
    int kesuflag;

    public void init() {
        xb = new int[4];
        yb = new int[4];
        xx = new int[4];
        yy = new int[4];
        xa = new int[4];
        ya = new int[4];
        tetrismap = new int[20][10];
        kesu = new int[20];
    }

    public void keyRset() {
        kf8 = kf4 = kf6 = kf2 = kfs = 0;
    }

    public void loop() {
        Graphics g = getGraphics();
        Graphics f = mg.getGraphics();

        while (true) {
            keyRset();
            titpaint(f, 0);
            g.drawImage(mg, 0, 0, this);

            while (kfs == 0) {
                Thread.yield();
            }

            titpaint(f, 1);
            g.drawImage(mg, 0, 0, this);
            tetrisInit();
            keyRset();

            clip.loop();
            while (tetrisloop) {
                if (gover != 0) {
                    gameover(f);
                } else if (kesuflag != 0) {
                    kesuyo(f);
                } else {
                    if (nextptn == (-1)) {
                        tetrisnext(f);
                    }

                    if (ptn == (-1)) {
                        tetrisptn();
                    } else if (gover == 0) {
                        tetrisgame(f);
                    }
                }

                if (ptflag != 0) {
                    g.drawImage(mg, 0, 0, this);
                    ptflag = 0;
                }

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                }
            }

            keyRset();
            tetrisloop = true;
            ptflag = 0;
            nyi = 12;
            nyx = 0;
            nyy = 1;

            //tokuten += 10000;
            rkflag = false;

            if (score[9] <= tokuten) {
                rkflag = true;

                while (tetrisloop) {
                    nyuu(f);

                    if (ptflag != 0) {
                        g.drawImage(mg, 0, 0, this);
                        ptflag = 0;
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                    }
                }

                for (int i = nyx; i < 3; i++) {
                    nyn[i] = 1;
                }

                for (int i = 0; i < 3; i++) {
                    name[9][i] = nyn[i];
                }

                score[9] = tokuten;
                lines[9] = linecount;
                rkg = 9;

                for (int i = 9; i >= 1; i--) {
                    if (score[i] >= score[i - 1]) {
                        rkg--;
                        score[i] ^= score[i - 1];
                        score[i - 1] ^= score[i];
                        score[i] ^= score[i - 1];
                        lines[i] ^= lines[i - 1];
                        lines[i - 1] ^= lines[i];
                        lines[i] ^= lines[i - 1];

                        for (int j = 0; j < 3; j++) {
                            name[i][j] ^= name[i - 1][j];
                            name[i - 1][j] ^= name[i][j];
                            name[i][j] ^= name[i - 1][j];
                        }
                    }
                }
            }

            titpaint(f, 2);
            tokuhyou(f);
            g.drawImage(mg, 0, 0, this);

            while (kfs == 0) {
                Thread.yield();
            }
        }
    }

    private void tokuhyou(Graphics g) {
        Graphics f = bufer.getGraphics();
        int a;
        int b;
        int c;
        int d;

        for (int i = 0; i < 10; i++) {
            if ((i == rkg) && (rkflag == true)) {
                d = 112;
            } else {
                d = 0;
            }

            for (int j = 0; j < 3; j++) {
                c = name[i][j];
                b = (c / 20) << 4;
                a = (c % 20) << 4;

                if (name[i][j] > 1) {
                    b += d;
                }

                f.translate(-a, -b);
                f.drawImage(image, 0, 0, this);
                f.translate(a, b);
                g.drawImage(bufer, (j + 13) << 4, (5 + (i * 2)) << 4, this);
            }

            c = score[i];
            a = 25;

            while (c > 0) {
                b = (9 + (c % 10)) << 4;
                c /= 10;
                f.translate(-b, -((2 << 4) + d));
                f.drawImage(image, 0, 0, this);
                f.translate(b, (2 << 4) + d);
                g.drawImage(bufer, a << 4, (5 + (i * 2)) << 4, this);
                a--;
            }

            c = lines[i];
            a = 32;

            while (c > 0) {
                b = (9 + (c % 10)) << 4;
                c /= 10;
                f.translate(-b, -((2 << 4) + d));
                f.drawImage(image, 0, 0, this);
                f.translate(b, (2 << 4) + d);
                g.drawImage(bufer, a << 4, (5 + (i * 2)) << 4, this);
                a--;
            }
        }

        if (rkflag == true) {
            c = rkg + 1;
            a = 8;

            while (c > 0) {
                b = (9 + (c % 10)) << 4;
                c /= 10;
                f.translate(-b, -((2 << 4) + 112));
                f.drawImage(image, 0, 0, this);
                f.translate(b, (2 << 4) + 112);
                g.drawImage(bufer, a << 4, (5 + (rkg * 2)) << 4, this);
                a--;
            }
        }
    }

    private void nyuu(Graphics g) {
        int a;
        int b;
        int nad = 49;

        if (kfs > 0) {
            if (nyi == 38) {
                tetrisloop = false;
            } else {
                nyn[nyx] = nyi + nad;
                nyx++;
            }

            nyy = 1;
            kfs = 0;
        }

        if (kf6 > 0) {
            if (nyi == 38) {
                tetrisloop = false;
            } else {
                nyn[nyx] = nyi + nad;
                nyx++;
            }

            nyy = 1;
            kf6 = 0;
        }

        if (kf4 > 0) {
            kf4 = 0;

            if (nyx > 0) {
                nyx--;
                nyi = nyn[nyx] - nad;

                Graphics f = bufer.getGraphics();

                if (tetrismap[10][3 + nyx + 1] != 0) {
                    b = 15 << 4;
                } else {
                    b = 1 << 4;
                }

                f.translate(-b, 0);
                f.drawImage(image, 0, 0, this);
                f.translate(b, 0);
                g.drawImage(bufer, (18 + nyx + 1) << 4, 13 << 4, this);
                nyy = 1;
            }
        }

        if ((kf2 > 0) || (kf8 > 0) || (nyy != 0)) {
            if (nyy != 0) {
                nyy = 0;
            }

            if (kf2 > 0) {
                kf2--;
                nyi = (nyi + 1) % 39;
            }

            if (kf8 > 0) {
                kf8--;
                nyi = (nyi + 38) % 39;
            }

            if (nyx >= 3) {
                nyi = 38;
                nyx = 3;
            }

            Graphics f = bufer.getGraphics();
            b = ((nad + nyi) / 20) << 4;
            a = ((nad + nyi) % 20) << 4;
            f.translate(-a, -b);
            f.drawImage(image, 0, 0, this);
            f.translate(a, b);
            g.drawImage(bufer, (18 + nyx) << 4, 13 << 4, this);
            ptflag = 1;
        }
    }

    private void gameover(Graphics g) {
        clip.stop();
        int a;
        int b;
        int c;
        Graphics f;
        counter++;
        f = bufer.getGraphics();
        a = counter % 10;
        b = counter / 10;

        if (counter >= 65) {
            if (counter > 150) {
                tetrisloop = false;
            }

            a = 19 - (counter - 65);

            if (a >= 0) {
                for (int i = 0; i < 10; i++) {
                    if (tetrismap[a][i] != 0) {
                        b = 15 << 4;
                    } else {
                        b = 1 << 4;
                    }

                    f.translate(-b, 0);
                    f.drawImage(image, 0, 0, this);
                    f.translate(b, 0);
                    g.drawImage(bufer, (15 + i) << 4, (3 + a) << 4, this);
                    ptflag = 1;
                }
            }
        } else {
            if (a == 0) {
                for (int i = 0; i < 4; i++) {
                    c = (ptn + 2) << 4;

                    if ((b % 2) == 0) {
                        c = (tetrismap[yb[i] + yyy][xb[i] + xxx] + 1) << 4;
                    }

                    f.translate(-c, 0);
                    f.drawImage(image, 0, 0, this);
                    f.translate(c, 0);
                    g.drawImage(bufer, (15 + xb[i] + xxx) << 4,
                                (3 + yb[i] + yyy) << 4, this);
                }

                ptflag = 1;
            }
        }
    }

    private void kesuyo(Graphics g) {
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
            Graphics f = bufer.getGraphics();
            int i = 19;

            while (i > 0) {
                if (kesu[i] == 10) {
                    for (int j = i; j > 0; j--) {
                        kesu[j] = kesu[j - 1];

                        for (int k = 0; k < 10; k++) {
                            tetrismap[j][k] = tetrismap[j - 1][k];
                        }
                    }
                } else {
                    i--;
                }
            }

            for (i = 0; i < 20; i++) {
                for (int j = 0; j < 10; j++) {
                    f.translate(-(tetrismap[i][j] + 1) << 4, 0);
                    f.drawImage(image, 0, 0, this);
                    f.translate((tetrismap[i][j] + 1) << 4, 0);
                    g.drawImage(bufer, (15 + j) << 4, (3 + i) << 4, this);
                }
            }

            if (tokuten > 999999) {
                tokuten = 999999;
            }

            a = tokuten;
            b = 11;

            while (a > 0) {
                c = a % 10;
                a /= 10;
                f.translate(-((c + 9) << 4), -32);
                f.drawImage(image, 0, 0, this);
                f.translate((c + 9) << 4, 32);
                g.drawImage(bufer, b << 4, 3 << 4, this);
                b--;
            }

            a = linecount;
            b = 11;

            while (a > 0) {
                c = a % 10;
                a /= 10;
                f.translate(-((c + 9) << 4), -32);
                f.drawImage(image, 0, 0, this);
                f.translate((c + 9) << 4, 32);
                g.drawImage(bufer, b << 4, 6 << 4, this);
                b--;
            }

            a = lev;
            b = 11;

            while (a > 0) {
                c = a % 10;
                a /= 10;
                f.translate(-((c + 9) << 4), -32);
                f.drawImage(image, 0, 0, this);
                f.translate((c + 9) << 4, 32);
                g.drawImage(bufer, b << 4, 9 << 4, this);
                b--;
            }

            keyRset();
            kesuflag = 0;
        }

        if (a > 0) {
            Graphics f = bufer.getGraphics();
            f.translate(-(a << 4), 0);
            f.drawImage(image, 0, 0, this);
            f.translate(a << 4, 0);

            for (int i = 0; i < 20; i++) {
                if (kesu[i] == 10) {
                    b++;

                    for (int j = 0; j < 10; j++) {
                        g.drawImage(bufer, (15 + j) << 4, (3 + i) << 4, this);
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
                    f.translate(-((i + 9) << 4), -32);
                    f.drawImage(image, 0, 0, this);
                    f.translate((i + 9) << 4, 32);
                    g.drawImage(bufer, (15 + b) << 4, (3 + d) << 4, this);
                    b--;
                }
            }

            ptflag = 1;
        }
    }

    private void tetrisgame(Graphics g) {
        Graphics f = bufer.getGraphics();
        int chflag = 0;
        counter++;

        while (kfs > 0) {
            kfs--;

            int j = 0;
            int[] aa = new int[4];
            int[] bb = new int[4];

            if (((ptn == 1) || (ptn == 4) || (ptn == 5)) && (kaitensu == 1)) {
                for (int i = 0; i < 4; i++) {
                    aa[i] = (-yb[i]) + ky;
                    bb[i] = xb[i] - ky;

                    if (((xxx + aa[i]) < 0) || ((xxx + aa[i]) > 9) ||
                        ((yyy + bb[i]) < 0) || ((yyy + bb[i]) > 19)) {
                        j = 1;
                    } else if (tetrismap[yyy + bb[i]][xxx + aa[i]] != 0) {
                        j = 1;
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    aa[i] = yb[i] + ky;
                    bb[i] = -xb[i] + kx;

                    if (((xxx + aa[i]) < 0) || ((xxx + aa[i]) > 9) ||
                        ((yyy + bb[i]) < 0) || ((yyy + bb[i]) > 19)) {
                        j = 1;
                    } else if (tetrismap[yyy + bb[i]][xxx + aa[i]] != 0) {
                        j = 1;
                    }
                }
            }

            if ((j == 0) && (ptn > 0)) {
                kaitensu = (kaitensu + 1) % 2;

                for (int i = 0; i < 4; i++) {
                    xb[i] = aa[i];
                    yb[i] = bb[i];
                }

                chflag = 1;
            }
        }

        while (kf6 > 0) {
            kf6--;

            int j = 0;

            for (int i = 0; i < 4; i++) {
                if ((xb[i] + xxx) > 8) {
                    j = 1;
                } else if (tetrismap[yb[i] + yyy][xb[i] + xxx + 1] != 0) {
                    j = 1;
                }
            }

            if (j == 0) {
                xxx++;
                chflag = 1;
            }
        }

        while (kf4 > 0) {
            kf4--;

            int j = 0;

            for (int i = 0; i < 4; i++) {
                if ((xb[i] + xxx) < 1) {
                    j = 1;
                } else if (tetrismap[yb[i] + yyy][(xb[i] + xxx) - 1] != 0) {
                    j = 1;
                }
            }

            if (j == 0) {
                xxx--;
                chflag = 1;
            }
        }

        if (counter >= spd[levc]) {
            int j = 0;
            counter = 0;

            for (int i = 0; i < 4; i++) {
                if ((yb[i] + yyy) > 18) {
                    j = 1;
                } else if (tetrismap[yb[i] + yyy + 1][xb[i] + xxx] != 0) {
                    j = 1;
                }
            }

            if (j == 0) {
                yyy++;
                chflag = 1;
                dflag = 0;
            } else {
                dflag += spd[levc];
            }
        } else if (kf2 > 0) {
            tokuten++;
            kf2--;
            counter = 0;

            int j = 0;

            for (int i = 0; i < 4; i++) {
                if ((yb[i] + yyy) > 18) {
                    j = 1;
                } else if (tetrismap[yb[i] + yyy + 1][xb[i] + xxx] != 0) {
                    j = 1;
                }
            }

            if (j == 0) {
                yyy++;
                chflag = 1;
                dflag = 0;
            } else {
                dflag += spd[levc];
            }

            tensu++;
        }

        if (chflag != 0) {
            ptflag = 1;
            f.translate(-16, 0);
            f.drawImage(image, 0, 0, this);
            f.translate(16, 0);

            for (int i = 0; i < 4; i++) {
                g.drawImage(bufer, (15 + xxa + xa[i]) << 4,
                            (3 + yya + ya[i]) << 4, this);
            }

            f.translate(-(ptn + 2) << 4, 0);
            f.drawImage(image, 0, 0, this);
            f.translate((ptn + 2) << 4, 0);

            for (int i = 0; i < 4; i++) {
                g.drawImage(bufer, (15 + xxx + xb[i]) << 4,
                            (3 + yyy + yb[i]) << 4, this);
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
                tetrismap[yyy + yb[i]][xxx + xb[i]] = ptn + 1;
                kesu[yyy + yb[i]]++;
            }

            for (int i = 0; i < 20; i++) {
                if (kesu[i] == 10) {
                    kesuflag = 1;
                    counter = 0;
                    lsu++;
                }
            }

            ptn = nextptn;
            nextptn = (-1);
            linecount += lsu;
            keyRset();

            if (linecount > 999) {
                linecount = 999;
            }

            lev = linecount / 4;
            levc = lev;

            if (lev > 99) {
                lev = 99;
            }

            if (levc > 19) {
                levc = 19;
            }

            a = tokuten;
            b = 11;

            if (tokuten > 99999) {
                tokuten = 99999;
            }

            while (a > 0) {
                c = a % 10;
                a /= 10;
                f.translate(-((c + 9) << 4), -32);
                f.drawImage(image, 0, 0, this);
                f.translate((c + 9) << 4, 32);
                g.drawImage(bufer, b << 4, 3 << 4, this);
                b--;
            }

            tokuten += tktn[lsu];
        }
    }

    private void tetrisptn() {
        counter++;

        if (counter >= 30) {
            ptn = nextptn;
            nextptn = (-1);

            for (int i = 0; i < 20; i++) {
                kesu[i] = 0;
            }
        }
    }

    private void tetrisnext(Graphics g) {
        Graphics f = bufer.getGraphics();
        nextptn = (int) ((Math.random() - 0.00001) * 7.0);
        dflag = counter = 0;

        for (int i = 0; i < 4; i++) {
            xb[i] = xx[i];
            yb[i] = yy[i];
        }

        kx = kaitenx;
        ky = kaiteny;

        switch (nextptn) {
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

        if (ptn != (-1)) {
            for (int i = 0; i < 4; i++) {
                if (tetrismap[yyy + yb[i]][xxx + xb[i]] != 0) {
                    gover = 1;
                    counter = 0;
                } else {
                    xa[i] = xb[i];
                    ya[i] = yb[i];
                }

//		if (gover != 0)tetrismap[yyy+yb[i]][xxx+xb[i]] = ptn+1;
            }

            f.translate(-(ptn + 2) << 4, 0);
            f.drawImage(image, 0, 0, this);
            f.translate((ptn + 2) << 4, 0);

            for (int i = 0; i < 4; i++) {
                g.drawImage(bufer, (15 + xb[i] + xxx) << 4,
                            (3 + yb[i] + yyy) << 4, this);
                xxa = xxx;
                yya = yyy;
            }
        }

        f.translate(-16, 0);
        f.drawImage(image, 0, 0, this);
        f.translate(16, 0);

        for (int i = 0; i < 4; i++) {
            g.drawImage(bufer, (19 + xb[i]) << 4, yb[i] << 4, this);
        }

        f.translate(-(nextptn + 2) << 4, 0);
        f.drawImage(image, 0, 0, this);
        f.translate((nextptn + 2) << 4, 0);

        if (gover == 0) {
            for (int i = 0; i < 4; i++) {
                g.drawImage(bufer, (19 + xx[i]) << 4, yy[i] << 4, this);
            }
        }

        ptflag = 1;
    }

    private void tetrisInit() {
        ptn = nextptn = -1;
        tokuten =
            linecount =
                levc = kesuflag = ptflag = tensu = lev = gover = counter = 0;
        tetrisloop = true;

        for (int i = 0; i < 20; i++) {
            kesu[i] = 0;

            for (int j = 0; j < 10; j++) {
                tetrismap[i][j] = 0;
            }
        }

        for (int i = 0; i < 4; i++) {
            xb[i] = xx[i] = yb[i] = yy[i] = 0;
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void paint(Graphics g) {
        g.drawImage(mg, 0, 0, this);
    }

    public void titpaint(Graphics g, int mm) {
        Graphics f;
        int a;
        int b;
        int c;
        f = bufer.getGraphics();

        for (int j = 0; j < 25; j++) {
            for (int i = 0; i < 40; i++) {
                c = map[mm][(j * 40) + i];
                a = c % 20;
                b = c / 20;
                f.translate(-(a << 4), -(b << 4));
                f.drawImage(image, 0, 0, this);
                f.translate(a << 4, b << 4);
                g.drawImage(bufer, i << 4, j << 4, this);
            }
        }
    }

    int[][] map = {
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

/* */
