/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import java.util.List;

import vavi.net.rest.EnumType;
import vavi.net.rest.Enumerated;
import vavi.net.rest.Parameter;
import vavi.net.rest.Rest;


/**
 * YahooJapanImageSearch. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080319 nsano initial version <br>
 * @see "http://developer.yahoo.co.jp/search/web/V1/webSearch.html"
 */
@Rest(protocol = "HTTP",
      method="GET",
      url="http://api.search.yahoo.co.jp/ImageSearchService/V1/imageSearch")
public class YahooJapanImageSearch {

    /** アプリケーションID。 */ 
    @Parameter(required = true)
    String appid;

    /**
     * (UTF-8エンコードされた) 検索クエリーです。
     * 文字を含む場合は '+'、含まない場合は '-' を指定します。
     * フレーズの場合は "検索 クエリー" のように " でくくります。
     */ 
    @Parameter(required = true)
    String query;

    enum Type {
        /** (デフォルト) 全クエリー文字を含む検索結果を返します。 */ 
        all,
        /** クエリー文字のうちいずれかを含む検索結果を返します。 */ 
        any,
        /** クエリー文字を文章として含む検索結果を返します。 */ 
        phrase
    }

    @Parameter
    @Enumerated(value = EnumType.STRING)
    Type type;

    /** 10 (デフォルト), 50 (最大) 返却結果の数です。 */ 
    @Parameter
    Integer results;

    /**
     * 1 (デフォルト)
     * 返却結果の先頭位置です。
     * 最終位置 (start + results - 1) は、1000を超えられません。
     */
    @Parameter
    Integer start;

    enum Format {
        /** （デフォルト）*/
        any,
        bmp,
        gif,
        jpeg,
        png
    }

    /** 検索する画像の種類を指定します。 */
    @Parameter
    @Enumerated(value = EnumType.STRING)
    Format format;

    /**
     * 値なし (デフォルト)
     * 1 アダルトコンテンツの検索結果を含めるかどうかを指定します。
     * 1 の場合はアダルトコンテンツを含みます。
     * TODO 値無しの annotation
     */ 
    @Parameter
    Integer adult_ok;

    enum Coloration {
        /** （デフォルト）*/
        any,
        color,
        bw
    }

    /** 検索する画像の種類を指定します。 */
    @Parameter
    @Enumerated(value = EnumType.STRING)
    Format coloration;

    /**
     * 値なし (デフォルト) 
     * 検索するドメイン（例えば www.yahoo.co.jp）を制限します。
     * 30ドメインまで指定することができます。
     * (site=www.yahoo.co.jp&site=www.asahi.com)
     */
    @Parameter
    List<String> site;
}

/* */
