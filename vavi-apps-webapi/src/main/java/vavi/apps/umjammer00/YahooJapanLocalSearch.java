/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import vavi.net.rest.EnumType;
import vavi.net.rest.Enumerated;
import vavi.net.rest.Ignored;
import vavi.net.rest.Parameter;
import vavi.net.rest.Rest;


/**
 * Yahoo Japan WebServices LocalSearch.
 * 
 * <li> リクエストパラメータで、p(検索キーワード)か、lat(緯度)・lon(経度)・dist(距離)は、どちらか一方が必須となります。
 * <li> 緯度経度で使用できる書式は以下のとおりです。
 * <ul>
 * <li> 度.分.秒形式(35.39.26.180,139.43.56.868)
 * <li> 度/分/秒形式(35/39/26.180,139/43/56.868)
 * <li> 度形式(35.657272,139.732463)
 * </ul>
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 * @see "http://developer.yahoo.co.jp/map/localsearch/V1/localsearch.html"
 */
@Rest(protocol = "HTTP",
      method="GET",
      url="http://api.map.yahoo.co.jp/LocalSearchService/V1/LocalSearch")
public class YahooJapanLocalSearch {

    /**
     * アプリケーションID。
     */
    @Parameter(name = "appid", required = true)
    private String token;

    /**
     * (UTF-8エンコードされた)検索キーワード
     */
    @Parameter(name = "p", required = true)
    @Ignored(when = "lat != -1 && lon != -1 && dist != -1")
    private String keyword;

    /**
     * 緯度
     * TODO ホントは required = true
     */
    @Parameter(required = false)
    @Ignored(when = "lat == -1")
    private int lat = -1;

    /**
     * 経度
     * TODO ホントは required = true
     */
    @Parameter(required = false)
    @Ignored(when = "lon == -1")
    private int lon = -1;

    /**
     * 距離(単位km)
     * TODO ホントは required = true
     */
    @Parameter(required = false)
    @Ignored(when = "dist == -1")
    private float dist = -1;

    /** 検索対象カテゴリ */
    enum Category {
        /** 住所を対象に検索 */
        address,
        /** 郵便番号を対象に検索 */
        zipcode,
        /** 施設(役所、学校、病院、郵便局など)を対象に検索 */
        landmark,
        /** 駅を対象に検索 */
        station,
        /** Yahoo!クーポンを対象に検索 */
        courpon
    }

    /**
     * 検索対象カテゴリ
     * デフォルトは、address、zipcode、landmark、stationを対象に検索します。
     *
     * Yahoo!クーポンは周辺検索でのみ検索対象となります。
     */
    @Parameter
    @Enumerated(value = EnumType.STRING)
    private Category category;

    /**
     * 表示開始位置。
     * 1(デフォルト) 
     * 最終位置(b + n - 1)は、100を超えられません。
     */
    @Parameter(name = "b")
    @Ignored(when = "begin == 1")
    private int begin = 1;

    /**
     * 表示件数
     * 10(デフォルト), 100(最大)
     */
    @Parameter(name = "n")
    @Ignored(when = "number == 10")
    private int number = 10;

    /** 出力タイプ */
    enum OutputType {
        /** (デフォルト) */
        xml,
        json
    }

    /**
     * 出力タイプ
     */
    @Parameter
    @Enumerated(value = EnumType.STRING)
    private OutputType outputType;

    /** 測地系 */
    enum GeographicCoordinateSystem {
        /** 日本測地系 (デフォルト) */
        tky,
        /** 世界測地系 */
        wgs
    }

    /**
     * 指定した緯度経度の測地系
     */
    @Parameter
    @Enumerated(value = EnumType.STRING)
    private GeographicCoordinateSystem datum;

    /**
     * 1, 2, 3 住所レベルの絞り込み
     * 1 - 市区町村レベル
     * 2 - 町、大字レベル
     * 3 - 丁目、字レベル
     * 住所を対象に検索した場合のみ有効です。
     */
    @Parameter(name = "al")
    @Ignored(when = "addressLevel == -1")
    private int addressLevel = -1;

    /** */
    public String getToken() {
        return token;
    }

    /** */
    public void setToken(String token) {
        this.token = token;
    }

    /** */
    public String getKeyword() {
        return keyword;
    }

    /** */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /** */
    public int getLat() {
        return lat;
    }

    /** */
    public void setLat(int lat) {
        this.lat = lat;
    }

    /** */
    public int getLon() {
        return lon;
    }

    /** */
    public void setLon(int lon) {
        this.lon = lon;
    }

    /** */
    public float getDist() {
        return dist;
    }

    /** */
    public void setDist(float dist) {
        this.dist = dist;
    }

    /** */
    public Category getCategory() {
        return category;
    }

    /** */
    public void setCategory(Category category) {
        this.category = category;
    }

    /** */
    public int getBegin() {
        return begin;
    }

    /** */
    public void setBegin(int begin) {
        this.begin = begin;
    }

    /** */
    public int getNumber() {
        return number;
    }

    /** */
    public void setNumber(int number) {
        this.number = number;
    }

    /** */
    public OutputType getOutputType() {
        return outputType;
    }

    /** */
    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }

    /** */
    public GeographicCoordinateSystem getDatum() {
        return datum;
    }

    /** */
    public void setDatum(GeographicCoordinateSystem datum) {
        this.datum = datum;
    }

    /** */
    public int getAddressLevel() {
        return addressLevel;
    }

    /** */
    public void setAddressLevel(int addressLevel) {
        this.addressLevel = addressLevel;
    }
}

/* */
