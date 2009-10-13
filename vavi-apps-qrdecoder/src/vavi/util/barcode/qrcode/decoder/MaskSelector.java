/*
 * Copyright (c) 2002 Kentaro Ishitoya & Manabu Shibata. All rights reserved.
 */

package vavi.util.barcode.qrcode.decoder;


/**
 * このクラスはCQRCodeImageにかけられたマスクについて失点を計算し、
 * 一番失点の少ないマスク指定子をunsigned intで返す。
 *
 * @version 2002/02/14(Fri)	石戸谷　顕太朗
 */
class MaskSelector {
    final static int WEIGHT_Adjacent = 3;
    final static int WEIGHT_Block = 3;
    final static int WEIGHT_Pattern = 40;
    final static int WEIGHT_Dark = 10;

    /** コンストラクタ */
    public MaskSelector() {}

    /** 渡された QRCodeImage に一番適切なマスク指定子を返す */
    public MaskDecorator.Mask rateMask(QRCodeImage target) {
        int rate;
        int least = 0;
        MaskDecorator.Mask index = null;
        BinaryImage temp;

        // それぞれのパターンについて採点を行う。
        for (MaskDecorator.Mask i : MaskDecorator.Mask.values()) {
            rate = 0;
            temp = target.getImage(i);
            rate += rateMaskAdjacent(temp);
            rate += rateMaskBlock(temp);
            rate += rateMaskPattern(temp);
            rate += rateMaskDark(temp);
            if (rate < least || i == MaskDecorator.Mask.PATTERN0) {
                least = rate;
                index = i;
            }
            System.out.println(i +  "        :");
            System.out.println("Adjacent : " + rateMaskAdjacent(temp));
            System.out.println("Block    : " + rateMaskBlock(temp));
            System.out.println("Pattern  : " + rateMaskPattern(temp));
            System.out.println("Dark     : " + rateMaskDark(temp));
            System.out.println("Rate     : " + rate);
            System.out.println();
        }
        return index;
    }

    /** 同色の隣接するモジュールについて失点を計算する関数 */
    private int rateMaskAdjacent(BinaryImage image) {
        final int row = image.getMaxRow();
        final int col = image.getMaxCol();
        boolean module;
        int adjacent;
        int rate = 0;
        for (int y = 0; y < row; y++) {
            adjacent = 0;
            for (int x = 0; x < row; x++) {
                module = image.getPixel(x, y);

                if (x + 1 >= row) {
                    continue;
                }

                if (image.getPixel(x + 1, y) != module) {
                    if (adjacent > 5) {
                        rate += adjacent - 5;
                    }
                    adjacent = 0;
                }
                adjacent++;
            }
        }
        for (int x = 0; x < row; x++) {
            adjacent = 0;
            for (int y = 0; y < col; y++) {
                module = image.getPixel(x, y);

                if (y + 1 >= col) {
                    continue;
                }

                if (image.getPixel(x, y + 1) != module) {
                    if (adjacent > 5) {
                        rate += adjacent - 5;
                    }
                    adjacent = 0;
                }
                adjacent++;
            }
        }
        return rate;
    }

    /** 同色のモジュールブロックについて失点を計算する。 */
    private int rateMaskBlock(BinaryImage image) {
        final int row = image.getMaxRow();
        final int col = image.getMaxCol();
        BinaryImage temp = new BinaryImage();
        temp.initialize(row, col, false);
        BinaryImage bimg = new BinaryImage();
        bimg.initialize(2, 2, true);
        boolean module;
        int block = 0;
        for (int x = 0; x < row; x++) {
            for (int y = 0; y < col; y++) {
                if (temp.getPixel(x, y)) {
                    continue;
                } //もうすでに評価されていたら。

                module = image.getPixel(x, y);
                if (!module) {
                    continue;
                }

                if (x + 1 >= col || y + 1 >= row) {
                    continue;
                }		// 一番右下のピクセルか。
                if (image.getPixel(x + 1, y) != module) {
                    continue;
                }		// 右隣
                if (image.getPixel(x, y + 1) != module) {
                    continue;
                }		// 下
                if (image.getPixel(x + 1, y + 1) != module) {
                    continue;
                }		// 右下
                // 全部同色なら失点を加算。
                block++;
                temp.or(x, y, bimg);
            }
        }
        return WEIGHT_Block * block;
    }

    /** 1：1：3：1：1比率のモジュールパターンについて失点を計算する。 */
    private int rateMaskPattern(BinaryImage image) {
        final int row = image.getMaxRow();
        final int col = image.getMaxCol();

//      boolean module;
        int rate = 0;
        // 列方向への走査
        for (int y = 0; y < col; y++) {
            for (int x = 0; x < row; x++) {
                if (image.getPixel(x, y) && (x + 6) < row) {
                    // 1:1:3:1:1（暗:明:暗:明:暗）のピクセル列を探す
                    // image.GetPixel() == true が暗 ==false が明

                    if (x != 0) {
                        if (image.getPixel(x - 1, y)) {
                            continue;
                        } // 一つ前が暗モジュールか
                    }
                    if ( image.getPixel(x + 1, y)) {
                        continue;
                    } // 明モジュールか
                    if (!image.getPixel(x + 2, y)) {
                        continue;
                    } // 暗モジュールか
                    if (!image.getPixel(x + 3, y)) {
                        continue;
                    } // 暗モジュールか
                    if (!image.getPixel(x + 4, y)) {
                        continue;
                    } // 暗モジュールか
                    if ( image.getPixel(x + 5, y)) {
                        continue;
                    } // 明モジュールか
                    if (!image.getPixel(x + 6, y)) {
                        continue;
                    } // 暗モジュールか
                    if (x + 7 != row) {
                        if (image.getPixel(x + 7, y)) {
                            continue;
                        } // 一つ後が暗モジュールか
                    }
                    rate += WEIGHT_Pattern;
                }
            }
        }
        // 列方向への走査
        for (int x = 0; x < row; x++) {
            for (int y = 0; y < col; y++) {
                if (image.getPixel(x, y) && (y + 6) < col) {
                    // 1:1:3:1:1（暗:明:暗:明:暗）のピクセル列を探す
                    // image.getPixel() == true が暗 ==false が明

                    if (y != 0) {
                        if (image.getPixel(x, y - 1)) {
                            continue;
                        }	// 一つ前が明モジュール
                    }
                    if ( image.getPixel(x, y + 1)) {
                        continue;
                    }	// 明モジュールか
                    if (!image.getPixel(x, y + 2)) {
                        continue;
                    }	// 暗モジュールか
                    if (!image.getPixel(x, y + 3)) {
                        continue;
                    }	// 暗モジュールか
                    if (!image.getPixel(x, y + 4)) {
                        continue;
                    }	// 暗モジュールか
                    if ( image.getPixel(x, y + 5)) {
                        continue;
                    }	// 明モジュールか
                    if (!image.getPixel(x, y + 6)) {
                        continue;
                    }	// 暗モジュールか
                    if (y + 7 != col) {
                        if (image.getPixel(x, y + 7)) {
                            continue;
                        }	// 一つ後が暗モジュール
                    }
                    rate += WEIGHT_Pattern;
                }
            }
        }
        return rate;
    }

    /** 全体に対する暗モジュールの比率について失点を計算する。 */
    private int rateMaskDark(BinaryImage image) {
        final int row = image.getMaxRow();
        final int col = image.getMaxCol();

        int modules = 0;
        // 列方向への走査
        for (int y = 0; y < col; y++) {
            for (int x = 0; x < row; x++) {
                if (!image.getPixel(x, y)) {
                    modules++;
                }
            }
        }
        return (int) (Math.abs(((double) modules / (double) (row * col) * 100.0 - 50.0) / 5.0) * WEIGHT_Dark);
    }
}

/* */
