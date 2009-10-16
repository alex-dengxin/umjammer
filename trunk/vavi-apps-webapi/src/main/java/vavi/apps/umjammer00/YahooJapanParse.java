/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import vavi.net.rest.Formatted;
import vavi.net.rest.Formatter;
import vavi.net.rest.Parameter;
import vavi.net.rest.Rest;


/**
 * YahooJapanParse. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080319 nsano initial version <br>
 * @see "http://developer.yahoo.co.jp/jlp/MAService/V1/parse.html"
 */
@Rest(protocol = "HTTP",
      method="GET",
      url="http://api.jlp.yahoo.co.jp/MAService/V1/parse")
public class YahooJapanParse {

    /** アプリケーションID。 */ 
    @Parameter(required = true)
    String appid;

    /** 解析対象のテキストです。 */ 
    @Parameter(required = true)
    String sentence;

    /** */
    enum Result {
        /** 形態素解析の結果を ma_result に返します。 */
        ma,
        /** 出現頻度情報を uniq_result に返します。 */
        uniq
    }

    /**
     * 解析結果の種類をコンマで区切って指定します。   
     * "ma": 形態素解析の結果を ma_result に返します。
     * "uniq": 出現頻度情報を uniq_result に返します。
     * 無指定の場合は "ma" になります。
     */
    @Parameter(required = true)
    String results = "ma";

    /** */
    public enum Response {
        surface,
        reading,
        pos,
        baseform,
        feature,
        ma_response,
        uniq_response
    }
     
    /**
     * surface, reading, pos, baseform, feature ma_response, uniq_response のデフォルト設定です。
     * word に返される形態素情報をコンマで区切って指定します。 
     * 無指定の場合は "surface,reading,pos" になります。
     */ 
    @Parameter
    @Formatted(formatter = ResponseFormatter.class)
    String response;

    /** */
    public enum Filter {
        形容詞, 
        形容動詞,
        感動詞,
        副詞 ,
        連体詞 ,
        接続詞 ,
        接頭辞 ,
        接尾辞 ,
        名詞 ,
        動詞 ,
        助詞 ,
        助動詞 ,
        // (句読点、カッコ、記号など)
        特殊
    }

    /**
     * ma_filter, uniq_filter のデフォルト設定です。 
     * 解析結果として出力する品詞番号を "｜" で区切って指定します。
     * <pre>
     * filterに指定可能な品詞番号: 
     *   1 : 形容詞 
     *   2 : 形容動詞 
     *   3 : 感動詞 
     *   4 : 副詞 
     *   5 : 連体詞 
     *   6 : 接続詞 
     *   7 : 接頭辞 
     *   8 : 接尾辞 
     *   9 : 名詞 
     *  10 : 動詞 
     *  11 : 助詞 
     *  12 : 助動詞 
     *  13 : 特殊（句読点、カッコ、記号など）
     * </pre>
     */
    @Parameter
    @Formatted(formatter = FilterFormatter.class)
    Filter[] filter;

    /**
     * ma_result 内の word に返される形態素情報をコンマで区切って指定します。
     * 無指定の場合 response の指定が用いられます。
     */ 
    @Parameter
    @Formatted(formatter = ResponseFormatter.class)
    Response[] ma_response;

    /**
     * ma_result 内に解析結果として出力する品詞番号を "｜" で区切って指定します。
     * 無指定の場合 filter の指定が用いられます。
     */ 
    @Parameter
    @Formatted(formatter = FilterFormatter.class)
    Filter[] ma_filter;

    /**
     * uniq_result 内の word に返される形態素情報をコンマで区切って指定します。
     * 無指定の場合 response の指定が用いられます。
     */
    @Parameter
    @Formatted(formatter = ResponseFormatter.class)
    Response[] uniq_response;

    /**
     * uniq_result 内に解析結果として出力する品詞番号を "｜" で区切って指定します。
     * 無指定の場合 filter の指定が用いられます。
     */ 
    @Parameter
    @Formatted(formatter = FilterFormatter.class)
    Filter[] uniq_filter;

    /**
     * このパラメータが true ならば、基本形の同一性により、
     * uniq_result の結果を求めます。
     */ 
    @Parameter
    Boolean uniq_by_baseform;

    /** */
    public static class ResultFormatter implements Formatter {
        @Override
        public String format(String format, Object value) {
            Result[] results = (Result[]) value;
            StringBuilder sb = new StringBuilder();
            for (Result result : results) {
                sb.append(result.name());
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    /** */
    public static class ResponseFormatter implements Formatter {
        @Override
        public String format(String format, Object value) {
            Response[] responses = (Response[]) value;
            StringBuilder sb = new StringBuilder();
            for (Response response : responses) {
                sb.append(response.name());
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }

    /** */
    public static class FilterFormatter implements Formatter {
        @Override
        public String format(String format, Object value) {
            Filter[] filters = (Filter[]) value;
            StringBuilder sb = new StringBuilder();
            for (Filter filter : filters) {
                sb.append(filter.ordinal() + 1);
                sb.append('|');
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
    }
}

/* */
