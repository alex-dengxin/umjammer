/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps.vendor.sony;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import vavi.gps.Channel;
import vavi.gps.GpsData;
import vavi.gps.GpsFormat;
import vavi.gps.MapVector;
import vavi.gps.PointMap3D;
import vavi.gps.PointSurface;


/**
 * SONY IPS 系 GPS データフォーマットを表すクラスです。
 *
 * <code><pre>
 * 1. 生データ
 *
 * SM00200312163182241n3753525E14006172+02530001809812163181058Q3BREFABXGdABSCKABPCGACDHIABJEiABGCrBGMGrCEdDCEO
 * 
 * 2. データ文字列の意味
 *
 * ofs len      data            description
 * ---+---+--------------------+-----------------------------------------------
 *   0   6	SM0020		ベンダーバージョン
 *   6  13	0312163182241	日付と時刻
 *				03 (年)
 *				12 (月)
 *				16 (日)
 *				 3 (曜日: 0(Sunday) - 6(Saturday))
 *				18 (時)
 *				22 (分)
 *				41 (秒)
 *  19   1	N		北緯 (N)あるいは南緯(S) n,s は非測位中
 *  20   2	37		緯度 37 (度)
 *  22   2	53		緯度 53 (分)
 *  24   3	525		緯度 52.5 (秒)
 *  27   1	E		東経 (E)あるいは西経(W)
 *  28   3	140		経度 140 (度)
 *  31   2	06		経度 06 (分)
 *  33   3	172		経度 17.2 (秒)
 *  36   5	+0253		高度 (海抜？) m
 *  41   3	000		速度 km/h
 *  44   3	180		方角 (北から時計回り) degrees (True North)
 *  47  13	9812163181058	日付と時刻 (計測時)
 *		
 *  60   1	Q		dilution of precision (DOP)
 *	 			データ	値	データ	値
 *	 			A	1	J	10
 *	 			B	2	K	11 - 12
 *	 			C	3	L	13 - 15
 *	 			D	4	M	16 - 20
 *	 			E	5	N	21 - 30
 *	 			F	6	O	31 - 50
 *	 			G	7	P	51 - 99
 *	 			H	8	Q	100+
 *	 			I	9
 *		 
 *  61   1	3		測定モード
 *		       	データ	モード
 *		       	3	衛星3つ使用の2次元モード
 *		       	4	衛星4つ使用の3次元モード
 *		
 *  62   1	B		高度補正データ
 *		
 *  63   5	REFAB		チャネル[1]
 *  68   5	XGdAB		チャネル[2]
 *  73   5	SCKAB		チャネル[3]
 *  78   5	PCGAC		チャネル[4]
 *  83   5	DHIAB		チャネル[5]
 *  88   5	JEiAB		チャネル[6]
 *  93   5	GCrBG		チャネル[7]
 *  98   5	MGrCE		チャネル[8]
 * 103   5	REFAB		チャネル[9]
 * 108   5	XGdAB		チャネル[10]
 * 113   5	SCKAB		チャネル[11]
 * 118   5	PCGAC		チャネル[12]
 * 123   5	DHIAB		チャネル[13]
 * 128   5	JEiAB		チャネル[14]
 * 133   5	GCrBG		チャネル[15]
 * 138   5	MGrCE		チャネル[16]
 *		 
 *		 		1 番目の文字	衛星の PRN 番号
 *		 		2 番目の文字	衛星の高度
 *		 		3 番目の文字	衛星の方位
 *		 		4 番目の文字	受信機と衛星の情報
 *		 		5 番目の文字	衛星からの信号の強さ
 *		 
 *		 		1. 衛星の PRN (Pseudo-Range Navigation) 番号
 *		 		データ	PRN#	データ	PRN#	データ PRN#
 *		 		A	1	L	12	W	23
 *		 		B	2	M	13	X	24
 *		 		C	3	N	14	a	25
 *		 		D	4	O	15	b	26
 *		 		E	5	P	16	c	27
 *		 		F	6	Q	17	d	28
 *		 		G	7	R	18	e	29
 *		 		H	8	S	19	f	30
 *		 		I	9	T	20	g	31
 *		 		J	10	U	21	h	32
 *		 		K	11	V	22
 *		 
 *		 		2. 衛星の高度
 *		 		データ	  高度		データ	  高度
 *		 		A	  0 >  +5	a	  0 >  -5
 *		 		B	 +6 > +15	b	 -6 > -15
 *		 		C	+16 > +25	c	-16 > -25
 *		 		D	+26 > +35	d	-26 > -35
 *		 		E	+36 > +45	e	-36 > -45
 *		 		F	+46 > +55	f	-46 > -55
 *		 		G	+56 > +65	g	-56 > -65
 *		 		H	+66 > +75	h	-66 > -75
 *		 		I	+76 > +85	i	-76 > -85
 *		 		J	+86 > +90	j	-86 > -90
 *		 
 *		 		3. 衛星の方位
 *		 		データ	   方位		データ	   方位
 *		 		A	   0 >   +5	a	   0 >   -5
 *		 		B	  +6 >  +15	b	  -6 >  -15
 *		 		C	 +16 >  +25	c	 -16 >  -25
 *		 		D	 +26 >  +35	d	 -26 >  -35
 *		 		E	 +36 >  +45	e	 -36 >  -45
 *		 		F	 +46 >  +55	f	 -46 >  -55
 *		 		G	 +56 >  +65	g	 -56 >  -65
 *		 		H	 +66 >  +75	h	 -66 >  -75
 *		 		I	 +76 >  +85	i	 -76 >  -85
 *		 		J	 +86 >  +95	j	 -86 >  -95
 *		 		K	 +96 > +105	k	 -96 > -105
 *		 		L	+106 > +115	l	-106 > -115
 *		 		M	+116 > +125	m	-116 > -125
 *		 		N	+126 > +135	n	-126 > -135
 *		 		O	+136 > +145	o	-136 > -145
 *		 		P	+146 > +155	p	-146 > -155
 *		 		Q	+156 > +165	q	-156 > -165
 *		 		R	+166 > +175	r	-166 > -175
 *		 		S	+176 > +180	s	-176 > -180
 *		 
 *		 		4. 受信機と衛星の情報
 *		 		データ	状態
 *		 		A 衛星を探している (SCAN)
 *		 		B 受信機は衛星の信号に同調中 (LOCK)
 *		 		C 位置情報の計算準備完了 (READY)
 *		 		D 衛星からの信号に割り込みがかかった (HOLD)
 *		 		E 衛星が不調かもしくは計算不能 (ILL)
 *		 		F 衛星は位置情報計算可能 (OK)
 *		 
 *		 		5. 衛星からの信号の強さ
 *		 		[A-Z] のデータ
 *		 		A:	低レベル	Z: 高レベル
 *		 
 * 143   1	d		内部基準発信器の状態 (Hz)
 *				TCXO のずれ (1.57542Ghz 換算)
 * 144   2	DC		不明
 * 146   1	E		Lat and Lon are shown as DMS if in Alphabet
 *				Lat and Lon are shown as DMD if in Numeric
 *
 * 147   1	O		パリティ(either E or O)
 * </pre></code>
 *		
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	030315	nsano	initial version <br>
 *		0.01	030318	nsano	IPS compatible <br>
 *		0.02	030319	nsano	fix seconds <br>
 */
public class IpsGpsFormat implements GpsFormat {

    private static final String HEADER_UNKNOWN1 = "SONY73";
    private static final String HEADER_UNKNOWN2 = "SONY80";
    private static final String HEADER_IPS5000 = "SONY81";
    private static final String HEADER_IPS5200 = "SONY82";
    private static final String HEADER_IPS8000 = "SONY99";
    private static final String HEADER_HGR1 = "SM01??";
    private static final String HEADER_HGR3 = "SM0020";

    // 衛星の PRN 番号
    static final String prnData = "ABCDEFGHIJKLMNOPQRSTUVWXabcdefgh";

    // dilution of precision (DOP)
    static final String dopData = "ABCDEFGHIJKLMNOPQ_";

    static final int[] dopValues = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        12, 14, 18, 25, 40, 75, 100, GpsData.INVALID_DOP
    };

    // 衛星の高度
    static final String elevationData = "ABCDEFGHIJabcdefghij_";

    static final int[] elevationValues = {
        0, 10, 20, 30, 40, 50, 60, 70, 80, 90,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        Channel.INVALID_ELEVTION
    };

    // 衛星の方位
    static final String azimuthData =
        "ABCDEFGHIJKLMNOPQRSabcdefghijklmnopqrs_";

    static final int[] azimuthValues = {
        0, 10, 20, 30, 40, 50, 60, 70, 80, 90,
        100, 110, 120, 130, 140, 150, 160, 170, 180,
        359, 350, 340, 330, 320, 310, 300, 290, 280,
        270, 260, 250, 240, 230, 220, 210, 200, 190, 180,
        Channel.INVALID_AZIMUTH
    };

    // 内部基準発信器の状態
    static final String differenceOfClockData =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-";

    static final int[] differenceOfClockValues = {
        100, 300, 500, 700, 900, 1100, 1300,
        1500, 1700, 1900, 2100, 2300, 2500, 2700,
        2900, 3100, 3300, 3500, 3700, 3900, 4100,
        4300, 4500, 4700, 4900, 5100,
        -100, -300, -500, -700, -900, -1100, -1300,
        1500, -1700, -1900, -2100, -2300, -2500, -2700,
        2900, -3100, -3300, -3500, -3700, -3900, -4100,
        4300, -4500, -4700, -4900, -5100,
        IpsGpsData.TCXO_UNLOCKED
    };

    //-------------------------------------------------------------------------

    /** */
    private static Date toDate(String time) {
        final DateFormat sdf = new SimpleDateFormat("yyMMdd HHmmss z");
        try {
            int wday = Integer.parseInt(time.substring(6, 7));
            time = time.substring(0, 6) + " " + time.substring(7) + " GMT";
            return sdf.parse(time);
        }
        catch (Exception e) {
//Debug.println(e);
            return null;
        }
    }

    /**
     * @throws IllegalArgumentException
     */
    public GpsData parse(byte[] line) {

        if (! new String(line).startsWith("SM0020")) { // TODO only HGR3S
            throw new IllegalArgumentException("bad data, drop this line");
        }

        IpsGpsData data = new IpsGpsData();

        data.setRawData(line);	// TODO

        data.setVersionString(new String(line, 0, 6));

        String dateTime = new String(line, 6, 13);
        data.setDateTime(toDate(dateTime));
//Debug.println("dateTime: " + dateTime + ": " + data.getDateTime());

        PointMap3D map = new PointMap3D();

        PointSurface point = new PointSurface();
        String type = new String(line, 19, 1);
//Debug.println("n/s: " + type);
        point.setType("N".equalsIgnoreCase(type) ?
                      PointSurface.NORTH_LATITUDE :
                      PointSurface.SOUTH_LATITUDE);

        if ("n".equals(type) || "s".equals(type)) {
            data.setReady(false);
        }
        else {
            data.setReady(true);
        }

        // TODO dmd reader
        point.setDegrees(Integer.parseInt(new String(line, 20, 2)));
        point.setMinutes(Integer.parseInt(new String(line, 22, 2)));
        String seconds =
            new String(line, 24, 2) + "." + new String(line, 26, 1);
        point.setSeconds(Float.parseFloat(seconds));
//Debug.println("point: " + new String(line, 19, 8) + ": " + point);

        map.setLatitude(point);

        point = new PointSurface();
        type = new String(line, 27, 1);
//Debug.println("E/W: " + type);
        point.setType("E".equals(type) ? PointSurface.EAST_LONGITUDE :
                                         PointSurface.WEST_LONGITUDE);
        point.setDegrees(Integer.parseInt(new String(line, 28, 3)));
        point.setMinutes(Integer.parseInt(new String(line, 31, 2)));
        seconds = new String(line, 33, 2) + "." + new String(line, 35, 1);
        point.setSeconds(Float.parseFloat(seconds));
//Debug.println("point: " + new String(line, 27, 9) + ": " + point);

        map.setLongitude(point);

        String sign = new String(line, 36, 1);
        map.setAltitude(Integer.parseInt(new String(line, 37, 4)));
        if ("-".equals(sign)) {
            map.setAltitude(map.getAltitude() * -1);
        }
//Debug.println(map);

        data.setPoint(map);

        MapVector vector = new MapVector();
        vector.setVelocity(Integer.parseInt(new String(line, 41, 3)));
        vector.setBearingDirection(Integer.parseInt(new String(line, 44, 3)));
//Debug.println(vector);

        data.setVector(vector);

        dateTime = new String(line, 47, 13);
        Date timeOfFix = toDate(dateTime);
        if (timeOfFix == null) {
            data.setReady(false);
        }
        else {
            data.setReady(true);
        }
        data.setTimeOfFix(timeOfFix);
//Debug.println("timeOfFix: " + data.getTimeOfFix());

        String mode = new String(line, 61, 1);
        data.setMeasurementMode("3".equals(mode) ? GpsData.MODE_2D :
                                                   GpsData.MODE_3D);
//Debug.println("3/4: " + mode + ": " + data.getMeasurementMode() + "D");

        String dop = new String(line, 60, 1);
        int p = dopData.indexOf(dop);
        if (data.getMeasurementMode() == GpsData.MODE_3D) {
            data.setPDop(dopValues[p]);
            data.setHDop(GpsData.INVALID_DOP);
//Debug.println("pdop: " + dop + ": " + data.getPDop());
        }
        else {
            data.setPDop(GpsData.INVALID_DOP);
            data.setHDop(dopValues[p]);
//Debug.println("hdop: " + dop + ": " + data.getHDop());
        }
        data.setVDop(GpsData.INVALID_DOP);

        String mapDatum = new String(line, 62, 1);
        data.setMapDatum(mapDatum.charAt(0) - 'A');
//Debug.println("mapDatum: " + data.getMapDatumString());

        for (int i = 0; i < data.getMaxChannels(); i++) {
            Channel channel = new Channel();

//System.err.println(new String(line, 63 + i * 5, 5));
            String prn = new String(line, 63 + i * 5, 1);
            p = prnData.indexOf(prn);
            channel.setPrn(p + 1);

            String elevation = new String(line, 64 + i * 5, 1);
            p = elevationData.indexOf(elevation);
            channel.setElevation(elevationValues[p]);

            String azimuth = new String(line, 65 + i * 5, 1);
            p = azimuthData.indexOf(azimuth);
            channel.setAzimuth(azimuthValues[p]);

            String info = new String(line, 66 + i * 5, 1);
            channel.setInfo(info.charAt(0) - 'A');

            String signalStrength = new String(line, 67 + i * 5, 1);
            channel.setSignalStrength((signalStrength.charAt(0) - 'A') * 3);

            data.addChannel(channel);
//Debug.println(channel);
        }

        String differenceOfClock = new String(line, 143, 1);
        p = differenceOfClockData.indexOf(differenceOfClock);
        data.setDifferenceOfClock(differenceOfClockValues[p]);
//Debug.println("diffrenceOfClock: " + differenceOfClock + ": " + differenceOfClockValues[p]);

        System.arraycopy(line, 144, data.unknown, 0, 2);
//Debug.println("unknown: "+(char) data.unknown[0]+", "+(char) data.unknown[1]);

        String unitType = new String(line, 146, 1);
        char c = unitType.charAt(0);
        if ('0' >= c && '9' <= c) {
            data.setUnitType(IpsGpsData.UNIT_DMD);
        }
        else if (('A' >= c && 'Z' <= c) || ('a' >= c && 'z' <= c)) {
            data.setUnitType(IpsGpsData.UNIT_DMS);
        }
//Debug.println("unitType: " + unitType + ": " + (data.getUnitType() == IpsGpsData.UNIT_DMD ? "DMD" : "DMS"));

        String parity = new String(line, 147, 1);
//Debug.println("parity: " + parity + ": " + (data.getParity(line) == IpsGpsData.PARITY_EVEN ? "E" : "O"));

        return data;
    }

    /** */
    public byte[] format(GpsData data) {

        byte[] line = new byte[148];

        //
        System.arraycopy("SM0020".getBytes(), 0, line, 0, 6);

        //
        Date dateTime = data.getDateTime();

        DateFormat sdf = new SimpleDateFormat("yyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String value = sdf.format(dateTime);
        System.arraycopy(value.getBytes(), 0, line, 6, 6);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        line[12] = (byte) ('0' + dayOfWeek);

        sdf = new SimpleDateFormat("HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        value = sdf.format(dateTime);
        System.arraycopy(value.getBytes(), 0, line, 13, 6);

        //
        PointSurface point = data.getPoint().getLatitude();
        int type = point.getType();
        if (data.ready()) {
            line[19] = (byte) (type == PointSurface.NORTH_LATITUDE ? 'N' : 'S');
        }
        else {
            line[19] = (byte) (type == PointSurface.NORTH_LATITUDE ? 'n' : 's');
        }

        DecimalFormat df3 = new DecimalFormat("000");
        DecimalFormat df2 = new DecimalFormat("00");

        value = df2.format(point.getDegrees());
        System.arraycopy(value.getBytes(), 0, line, 20, 2);
        value = df2.format(point.getMinutes());
        System.arraycopy(value.getBytes(), 0, line, 22, 2);
        value = df3.format(Math.round(point.getSeconds()));
        System.arraycopy(value.getBytes(), 0, line, 24, 3);

        //
        point = data.getPoint().getLongitude();
        type = point.getType();
        line[27] = (byte) (type == PointSurface.EAST_LONGITUDE ? 'E' : 'W');

        value = df3.format(point.getDegrees());
        System.arraycopy(value.getBytes(), 0, line, 28, 3);
        value = df2.format(point.getMinutes());
        System.arraycopy(value.getBytes(), 0, line, 31, 2);
        value = df3.format(Math.round(point.getSeconds()));
        System.arraycopy(value.getBytes(), 0, line, 33, 3);

        //
        int altitude = data.getPoint().getAltitude();
        line[36] = (byte) (altitude >= 0 ? '+' : '-');

        DecimalFormat df4 = new DecimalFormat("0000");

        value = df4.format(altitude);
        System.arraycopy(value.getBytes(), 0, line, 37, 4);

        //
        int velocity = data.getVector().getVelocity();

        value = df3.format(velocity);
        System.arraycopy(value.getBytes(), 0, line, 41, 3);

        //
        int bearingDirection = data.getVector().getBearingDirection();

        value = df3.format(bearingDirection);
        System.arraycopy(value.getBytes(), 0, line, 44, 3);

        //
        dateTime = data.getTimeOfFix();

        sdf = new SimpleDateFormat("yyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        value = sdf.format(dateTime);
        System.arraycopy(value.getBytes(), 0, line, 47, 6);

        calendar.setTime(dateTime);
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        line[53] = (byte) ('0' + dayOfWeek);

        sdf = new SimpleDateFormat("HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        value = sdf.format(dateTime);
        System.arraycopy(value.getBytes(), 0, line, 54, 6);

        //
        int mode = data.getMeasurementMode();
        line[61] = (byte) ((mode == GpsData.MODE_2D) ? '3' : '4');

        //
        int dop;
        if (mode == GpsData.MODE_2D) {
            dop = data.getHDop();
        }
        else {
            dop = data.getPDop();
        }
        if (dop > 0 && dop <= 10) {
            line[60] = (byte) ('A' + (dop - 1));
        }
        else if (dop >= 11 && dop < 12) {
            line[60] = 'K';
        }
        else if (dop >= 13 && dop < 15) {
            line[60] = 'L';
        }
        else if (dop >= 16 && dop < 20) {
            line[60] = 'M';
        }
        else if (dop >= 21 && dop < 30) {
            line[60] = 'N';
        }
        else if (dop >= 31 && dop < 50) {
            line[60] = 'O';
        }
        else if (dop >= 51 && dop < 99) {
            line[60] = 'P';
        }
        else {
            line[60] = 'Q';
        }

        //
        int mapDatum = data.getMapDatum();
        line[62] = (byte) ('A' + mapDatum);

        //
        for (int i = 0; i < 16; i++) {
            value = (char) ('A' + i) + "__AA";
            System.arraycopy(value.getBytes(), 0, line, 63 + i * 5, 5);
        }

        //
        line[143] = 'd';

        // unknown
        line[144] = 'D';
        line[145] = '3';

        // DMS/DMD
        line[146] = 'A';

        //
        int parity = ((IpsGpsData) data).getParity(line);
        line[147] = (byte) ((parity == IpsGpsData.PARITY_ODD) ? 'O' : 'E');

        return line;
    }
}

/* */
