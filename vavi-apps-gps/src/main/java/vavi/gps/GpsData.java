/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.gps;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * GpsData.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 030318 nsano initial version <br>
 *          0.01 030325 nsano separate hdop, pdop, vdop <br>
 */
public abstract class GpsData {

    /** ベンダーバージョン */
    private String versionString;

    /** */
    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    /** */
    public String getVersionString() {
        return versionString;
    }

    /** 日付と時刻 */
    private Date dateTime;

    /** */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    /** */
    public Date getDateTime() {
        return dateTime;
    }

    /** 緯度, 経度 */
    private PointMap3D point;

    /** */
    public void setPoint(PointMap3D point) {
        this.point = point;
    }

    /** */
    public PointMap3D getPoint() {
        return point;
    }

    /** 速度, 方角 */
    private MapVector vector;

    /** */
    public void setVector(MapVector vector) {
        this.vector = vector;
    }

    /** */
    public MapVector getVector() {
        return vector;
    }

    /** 日付と時刻 (計測時) */
    private Date timeOfFix;

    /** */
    public void setTimeOfFix(Date timeOfFix) {
//Debug.println(timeOfFix);
        this.timeOfFix = timeOfFix;
    }

    /** */
    public Date getTimeOfFix() {
//Debug.println(timeOfFix);
        return timeOfFix;
    }

    /** */
    public static final int INVALID_DOP = -999;

    /** dilution of precision (PDOP) TODO */
    private int pdop;

    /** */
    public void setPDop(int pdop) {
        this.pdop = pdop;
    }

    /** */
    public int getPDop() {
        return pdop;
    }

    /** dilution of precision (HDOP) TODO */
    private int hdop;

    /** */
    public void setHDop(int hdop) {
        this.hdop = hdop;
    }

    /** */
    public int getHDop() {
        return hdop;
    }

    /** dilution of precision (VDOP) TODO */
    private int vdop;

    /** */
    public void setVDop(int vdop) {
        this.vdop = vdop;
    }

    /** */
    public int getVDop() {
        return vdop;
    }

    /** */
    public static final int MODE_2D = 2;
    /** */
    public static final int MODE_3D = 3;

    /** 測定モード */
    private int measurementMode;

    /** */
    public void setMeasurementMode(int measurementMode) {
        this.measurementMode = measurementMode;
    }

    /** */
    public int getMeasurementMode() {
        return measurementMode;
    }

    /** 高度補正データ 0 - 25 TODO depends IPS */
    private int mapDatum;

    /** */
    public void setMapDatum(int mapDatum) {
        if (mapDatum >= 0 && mapDatum < mapDatumStrings.length) {
            this.mapDatum = mapDatum;
        }
        else {
            throw new IllegalArgumentException(String.valueOf(mapDatum));
        }
    }

    /** */
    public void setMapDatum(String mapDatumString) {
        for (int i = 0; i < mapDatumStrings.length; i++) {
            if (mapDatumStrings[i].equals(mapDatumString)) {
                this.mapDatum = i;
            }
        }

        throw new IllegalArgumentException(mapDatumString);
    }

    /** */
    public int getMapDatum() {
        return mapDatum;
    }

    /** チャネル */
    protected Map<String,Channel> channels = new HashMap<String,Channel>();

    /** */
    public void addChannel(Channel channel) {
        int max = getMaxChannels();
        if (channels.size() == max) {
            throw new IllegalArgumentException("satellites over " + max);
        }

        channels.put(channel.getKeyString(), channel);
    }

    /** */
    public Collection<Channel> getChannels() {
        return channels.values();
    }

    //----

    /** */
    public abstract int getMaxChannels();

    /** */
    public abstract boolean ready();

    /** */
    public int getAvailableChannelsCount() {
        int count = 0;
        for (Channel channel : getChannels()) {
            if (channel.available()) {
                count++;
            }
        }
        return count;
    }

    /** */
    public String getMapDatumString() {
        return mapDatumStrings[mapDatum];
    }

    /** 高度補正データ */
    private static final String[] mapDatumStrings = {
        "WGS-84",		// 補正の必要無し
        "Tokyo",
        "ADINDAN",
        "ARC 1950",
        "MERCHICH",
        "HONG KONG 1963",
        "SOUTH ASIA",
        "LUZON",
        "INDIAN",
        "INDIAN",
        "FERTAU 1948",
        "NORTH AMERICAN 1927", // (use for Belize, Costa Rica, El Salvador, Guatemala, Honduras, Nicaragua)
        "EUROPEAN 1950, EUROPEAN 1979",
        "IRELAND 1965     ",
        "ORDNANCE SURVEY OF GREAT BRITAN 1936",
        "NAHRWAN",
        "NAHRWAN",
        "OLD EGYPTIAN",
        "NORTH AMERICAN 1927", // (use for Canada, Newfoundland Island)
        "NORTH AMERICAN 1983", // (Alaska, Canada, Mexico, Central America, USA)
        "AUSTRALIAN GEODETIC 1984",
        "GEODETIC DATUM 1949",
        "PROVISIONAL SOUTH AMERICAN 1956",
        "SOUTH AMERICAN 1969",
        "CAMPO INCHAUSPE",
        "CORREGO ALEGRE"
    };
}

/* */
