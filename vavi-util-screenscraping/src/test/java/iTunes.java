/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import vavi.util.CharConverterJa;
import vavi.util.LevenshteinDistance;
import vavix.util.screenscrape.annotation.EachHandler;
import vavix.util.screenscrape.annotation.HtmlXPathParser;
import vavix.util.screenscrape.annotation.InputHandler;
import vavix.util.screenscrape.annotation.SaxonXPathParser;
import vavix.util.screenscrape.annotation.Target;
import vavix.util.screenscrape.annotation.WebScraper;


/**
 * iTunes. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/10/04 nsano initial version <br>
 */
public class iTunes {

    /** iTunes ライブラリ一曲 */
    @WebScraper(url = "file:///Users/nsano/Music/iTunes/iTunes%20Music%20Library.xml",
                parser = SaxonXPathParser.class,
                value = "/plist/dict/dict/dict")
    public static class Title {
        @Target("/dict/key[text()='Artist']/following-sibling::string[1]/text()")
        String artist;
        @Target("/dict/key[text()='Name']/following-sibling::string[1]/text()")
        String name;
        @Target("/dict/key[text()='Composer']/following-sibling::string[1]/text()")
        String composer;
        @Target("/dict/key[text()='Album']/following-sibling::string[1]/text()")
        String album;
        @Target("/dict/key[text()='Album Artist']/following-sibling::string[1]/text()")
        String albumArtist;
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(artist);
            sb.append("\t");
            sb.append(name);
            sb.append("\t");
            sb.append(composer);
            return sb.toString();
        }
    }
    
    static WebClient client = new WebClient(BrowserVersion.FIREFOX_3);

    static {
        client.setJavaScriptEnabled(false);
    }

    static void init() throws IOException {
        HtmlPage page0 = client.getPage("http://www2.jasrac.or.jp/eJwid/");
        HtmlInput button0 = page0.getForms().get(1).getInputByName("input");
        button0.click();
    }

    /** アーティスト、作品名検索 */
    public static class MyInput implements InputHandler<Reader> {
        private String cache;
        /**
         * @param args 0: artist, 1: title
         */
        public Reader getInput(String ... args) throws IOException {
            if (cache != null) {
                return new StringReader(cache);
            }

            String artist = args[0].toUpperCase();
            String title = args[1].toUpperCase();
//System.err.println("ARGS: " + artist + ", " + title);
            HtmlPage page0 = client.getPage("http://www2.jasrac.or.jp/eJwid/");
            HtmlInput button0 = page0.getForms().get(1).getInputByName("input");

            HtmlPage page1 = button0.click();

            FrameWindow frame2 = page1.getFrameByName("frame2");
            HtmlPage page2 = (HtmlPage) frame2.getEnclosedPage();

            HtmlForm form1 = page2.getFormByName("Form");
            HtmlInput inputT = form1.getInputByName("IN_WORKS_TITLE_NAME1");
            inputT.setValueAttribute(title);
//            HtmlSelect selectT = form1.getSelectByName("IN_WORKS_TITLE_OPTION1");
//            selectT.setSelectedAttribute("3", true); // 0:前方一致, 1:後方一致, 2:中間一致 3:完全一致
            HtmlInput inputA = form1.getInputByName("IN_ARTIST_NAME1");
            inputA.setValueAttribute(artist);
            HtmlSelect selectA = form1.getSelectByName("IN_ARTIST_NAME_OPTION1");
            selectA.setSelectedAttribute("3", true); // 0:前方一致, 1:後方一致, 2:中間一致 3:完全一致 
            HtmlInput button1 = form1.getInputByName("CMD_SEARCH");

            HtmlPage page3 = button1.click();

            cache = page3.asXml();
//System.err.println(cache);
            return new StringReader(cache);
        }
    }

    @WebScraper(input = MyInput.class,
                parser = HtmlXPathParser.class,
                encoding = "MS932")
    public static class TitleUrl {
        @Target(value = "//TABLE//TR/TD[2]/DIV/text()")
        String artist;
        @Target(value = "//TABLE//TR/TD[4]/A/text()")
        String title;
        @Target(value = "//TABLE//TR/TD[4]/A/@href")
        String url;
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(CharConverterJa.toHalf2(artist));
            sb.append(", ");
            sb.append(CharConverterJa.toHalf2(title));
            return sb.toString();
        }
    }
    
    /** 作詞、作曲詳細 (単品) */
    public static class MyInput2 implements InputHandler<Reader> {
        private String cache;
        /**
         * @param args 0: url
         */
        public Reader getInput(String ... args) throws IOException {
            if (cache != null) {
                return new StringReader(cache);
            }

            String url = args[0];

            HtmlPage page = client.getPage("http://www2.jasrac.or.jp/eJwid/" + url);

            cache = page.asXml();

            return new StringReader(cache);
        }
    }

    /** 作詞、作曲詳細 (一行) */
    @WebScraper(input = MyInput2.class,
                parser = HtmlXPathParser.class,
                encoding = "MS932")
    public static class Composer {
        @Target(value = "//TABLE[4]//TR/TD[2]/SPAN/text()")
        String name;
        @Target(value = "//TABLE[4]//TR/TD[3]/DIV/text()")
        String type;
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append(", ");
            sb.append(CharConverterJa.toHalf2(name));
            return sb.toString();
        }
    }
    
    /** 作品名で検索 */
    public static class MyInput3 implements InputHandler<Reader> {
        private String cache;
        /**
         * @param args 0: title
         */
        public Reader getInput(String ... args) throws IOException {
            if (cache != null) {
                return new StringReader(cache);
            }

            String title = args[0];
//System.err.println("ARGS: " + artist + ", " + title);
            HtmlPage page0 = client.getPage("http://www2.jasrac.or.jp/eJwid/");
            HtmlInput button0 = page0.getForms().get(1).getInputByName("input");

            HtmlPage page1 = button0.click();

            FrameWindow frame2 = page1.getFrameByName("frame2");
            HtmlPage page2 = (HtmlPage) frame2.getEnclosedPage();

            HtmlForm form1 = page2.getFormByName("Form");
            HtmlInput inputT = form1.getInputByName("IN_WORKS_TITLE_NAME1");
            inputT.setValueAttribute(title);
            HtmlSelect selectT = form1.getSelectByName("IN_WORKS_TITLE_OPTION1");
            selectT.setSelectedAttribute("3", true);
            HtmlInput button1 = form1.getInputByName("CMD_SEARCH");

            HtmlPage page3 = button1.click();
            StringBuffer sb = new StringBuffer(page3.asXml());

            try {
                HtmlPage nextPage = page3;
                while (true) {
                    HtmlAnchor nextAnchor = nextAnchor(nextPage.getAnchors());
                    nextPage = (HtmlPage) nextAnchor.click();
                    sb.append(nextPage.asXml());
                }
            } catch (NoSuchElementException e) {
            }

            cache = page3.asXml();
//System.err.println(cache);
            return new StringReader(cache);
        }

        /** */
        HtmlAnchor nextAnchor(List<HtmlAnchor> anchors) {
            for (HtmlAnchor anchor : anchors) {
                if (anchor.getAttribute("title").equals("次ページの結果を表示します")) {
                    return anchor;
                }
            }
            throw new NoSuchElementException();
        }
    }

    /** 作品名指定の作品 (複数) */
    @WebScraper(input = MyInput3.class,
                parser = HtmlXPathParser.class,
                encoding = "MS932")
    public static class TitleUrl3 {
        @Target(value = "//TABLE//TR/TD[5]/text()")
        String artist;
        @Target(value = "//TABLE//TR/TD[3]/A/text()")
        String title;
        @Target(value = "//TABLE//TR/TD[3]/A/@href")
        String url;
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(CharConverterJa.toHalf2(artist));
            sb.append(", ");
            sb.append(CharConverterJa.toHalf2(title));
            return sb.toString();
        }
    }

    /** アーティストで検索 */
    public static class MyInput4 implements InputHandler<Reader> {
        private String cache;
        /**
         * @param args 0: artist
         */
        public Reader getInput(String ... args) throws IOException {
            if (cache != null) {
                return new StringReader(cache);
            }

            String artist = args[0];

            HtmlPage page0 = client.getPage("http://www2.jasrac.or.jp/eJwid/");
            HtmlInput button0 = page0.getForms().get(1).getInputByName("input");

            HtmlPage page1 = button0.click();

            FrameWindow frame2 = page1.getFrameByName("frame2");
            HtmlPage page2 = (HtmlPage) frame2.getEnclosedPage();

            HtmlForm form1 = page2.getFormByName("Form");
            HtmlInput inputA = form1.getInputByName("IN_ARTIST_NAME1");
            inputA.setValueAttribute(artist);
            HtmlSelect selectA = form1.getSelectByName("IN_ARTIST_NAME_OPTION1");
            selectA.setSelectedAttribute("3", true);
            HtmlInput button1 = form1.getInputByName("CMD_SEARCH");

            HtmlPage page3 = button1.click();
            StringBuffer sb = new StringBuffer(page3.asXml());

            try {
                HtmlPage nextPage = page3;
                while (true) {
                    HtmlAnchor nextAnchor = nextAnchor(nextPage.getAnchors());
                    nextPage = (HtmlPage) nextAnchor.click();
                    sb.append(nextPage.asXml());
                }
            } catch (NoSuchElementException e) {
            }

            cache = sb.toString();

            return new StringReader(cache);
        }

        /** */
        HtmlAnchor nextAnchor(List<HtmlAnchor> anchors) {
            for (HtmlAnchor anchor : anchors) {
                if (anchor.getAttribute("title").equals("次ページの結果を表示します")) {
                    return anchor;
                }
            }
            throw new NoSuchElementException();
        }
    }

    /** アーティスト指定の作品 (複数) */
    @WebScraper(input = MyInput4.class,
                parser = HtmlXPathParser.class,
                encoding = "MS932")
    public static class TitleUrl4 {
        @Target(value = "//TABLE//TR/TD[1]/DIV/text()")
        String artist;
        @Target(value = "//TABLE//TR/TD[4]/A/text()")
        String title;
        @Target(value = "//TABLE//TR/TD[4]/A/@href")
        String url;
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(CharConverterJa.toHalf2(artist));
            sb.append(", ");
            sb.append(CharConverterJa.toHalf2(title));
            return sb.toString();
        }
    }
    
    public static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        } else {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }

    static final Pattern normalizeComposerPattern = Pattern.compile("[\\p{Upper}\\d' _ー\\.\\(\\)-]+");

    /** TODO Mc-, O-, Dr, St, Van, De-, La-, III, II, Jr, Sr, DJ ... and (US1), (GB) ... */
    static String normalizeComposer(String name) {
        Matcher matcher = normalizeComposerPattern.matcher(name);
        if (!matcher.matches()) {
            return name; // 国内
        }
        name = name.replace("ー", "-");
        StringBuilder result = new StringBuilder();
        String[] ns = name.split("\\s");
        if (ns.length > 1) {
            for (int i = 1; i < ns.length; i++) {
                result.append(capitalize(ns[i]));
                result.append(" ");
            }
        }
        result.append(capitalize(ns[0]));
        return result.toString();
    }

    static String getComposer(String url) throws IOException {
        List<Composer> cs = WebScraper.Util.scrape(Composer.class, url);
        StringBuilder lyrics_ = new StringBuilder();
        StringBuilder music_ = new StringBuilder();
        for (Composer composer : cs) {
//System.err.println(composer);
//System.err.println(composer.type + ", " + composer.type.indexOf("作詞") + ", " + composer.type.indexOf("作曲"));
            if ((composer.type.indexOf("作詞") != -1 || composer.type.indexOf("訳詞") != -1) && composer.name.indexOf("権利者") == -1) {
                lyrics_.append(normalizeComposer(CharConverterJa.toHalf2(composer.name)));
                lyrics_.append(", ");
            }
            if ((composer.type.indexOf("作曲") != -1 || composer.type.indexOf("不明") != -1) && composer.name.indexOf("権利者") == -1) {
                music_.append(normalizeComposer(CharConverterJa.toHalf2(composer.name)));
                music_.append(", ");
            }
        }
        if (lyrics_.length() > 1) {
            lyrics_.setLength(lyrics_.length() - 2);
        }
        if (music_.length() > 1) {
            music_.setLength(music_.length() - 2);
        }
        String lyrics = lyrics_.toString();
        String music = music_.toString();
        return lyrics.equals(music) || lyrics.isEmpty() ? music : music + " / " + lyrics;
    }

    /** アーティスト名で近い順 */
    static class MyComparator3 implements Comparator<TitleUrl3> {
        LevenshteinDistance ld = new LevenshteinDistance();
        String artist;
        MyComparator3(String artist) {
            this.artist = artist.toUpperCase();
        }
        public int compare(TitleUrl3 o1, TitleUrl3 o2) {
            int d1 = ld.calculate(artist, CharConverterJa.toHalf2(o1.artist)) - ld.calculate(artist, CharConverterJa.toHalf2(o2.artist));
            return d1;
        }
    }

    /** 作品名で近い順 */
    static class MyComparator4 implements Comparator<TitleUrl4> {
        LevenshteinDistance ld = new LevenshteinDistance();
        String name;
        MyComparator4(String name) {
            this.name = name.toUpperCase();
        }
        public int compare(TitleUrl4 o1, TitleUrl4 o2) {
            int d1 = ld.calculate(name, CharConverterJa.toHalf2(o1.title)) - ld.calculate(name, CharConverterJa.toHalf2(o2.title));
            return d1;
        }
    }

    /**
     * @param args 0: artist, 1: title 
     */
    public static void main(String[] args) throws Exception {
        WebScraper.Util.foreach(Title.class, new EachHandler<Title>() {
            public void exec(Title each) {
                try {
                    doEach(each);
//                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }, args);
    }

    // TODO la, un, los
    static final Pattern normalizeArticlePattern = Pattern.compile("(An|A|The) (.*)");
    
    // TODO "...
    static final Pattern normalizeNamePattern = Pattern.compile("(.*)(feat.*|[~〜].+|[-ー].+|[\\/／].+|[\\(（].+)");

    /**
     * main 
     */
    static void doEach(Title each) throws IOException {
        // SPECIAL, exclude speed leaning
        if ("Speed Learning".equals(each.artist)) {
            return;
        }

        if (!each.composer.isEmpty()) {
            return;
        }

        // 1. plain artist, name
        List<TitleUrl> urls = WebScraper.Util.scrape(TitleUrl.class, each.artist, each.name);
        if (urls.size() > 0) {
            System.out.println("RESULT\t" + each + getComposer(urls.get(0).url));
            return;
        }

        // 2. re-scrape by album artist, name
        String normalizedArtist = each.artist;
        if (each.albumArtist != null && !each.albumArtist.isEmpty()) {
            normalizedArtist = each.albumArtist; 
            List<TitleUrl> urls2 = WebScraper.Util.scrape(TitleUrl.class, normalizedArtist, each.name);
            if (urls2.size() > 0) {
                System.out.println("RESULTa\t" + each + getComposer(urls2.get(0).url));
                return;
            }
        }

        // 3. re-scrape by album artist, normalized name (cut ~XXX, -XXX, feat. XXX)
        // TODO (...), & -> and, II -> 2
        String normalizedName = each.name;
        Matcher matcher = normalizeArticlePattern.matcher(each.name);
        if (matcher.matches()) {
            normalizedName = matcher.group(2);
        }
        matcher = normalizeNamePattern.matcher(normalizedName);
        if (matcher.matches()) {
            normalizedName = matcher.group(1);
        }
        List<TitleUrl> urls3 = WebScraper.Util.scrape(TitleUrl.class, normalizedArtist, normalizedName);
        if (urls3.size() > 0) {
            System.out.println("RESULTn\t" + each + getComposer(urls3.get(0).url));
            return;
        }

        // 4. by artist only
        int ca = 0;
        List<TitleUrl4> url4s = WebScraper.Util.scrape(TitleUrl4.class, normalizedArtist);
        if (url4s.size() > 0) {
            Collections.sort(url4s, new MyComparator4(normalizedName));
            for (TitleUrl4 url4 : url4s) {
                if (ca == 0 && normalizedName.equalsIgnoreCase(CharConverterJa.toHalf2(url4.title))) {
                    System.out.println("RESULTp\t" + each + getComposer(url4.url));
                    return;
                }
                System.out.println("MAYBEa" + ca + "\t" + each + "(" + getComposer(url4.url) + ")" + "\t[" + CharConverterJa.toHalf2(url4.artist) + ", " + CharConverterJa.toHalf2(url4.title) + "]");
                ca++;
                if (ca > 2) {
                    break;
                }
            }
        }
        
        // 5. by name only
        List<TitleUrl3> url3s = WebScraper.Util.scrape(TitleUrl3.class, normalizedName);
        int cn = 0;
        if (url3s.size() > 0) {
            matcher = normalizeArticlePattern.matcher(normalizedArtist);
            if (matcher.matches()) {
                normalizedArtist = matcher.group(2);
            }
            Collections.sort(url3s, new MyComparator3(normalizedArtist));
            for (TitleUrl3 url3 : url3s) {
                System.out.println("MAYBEn" + cn + "\t" + each + "(" + getComposer(url3.url) + ")" + "\t[" + CharConverterJa.toHalf2(url3.artist) + ", " + CharConverterJa.toHalf2(url3.title) + "]");
                cn++;
                if (cn > 2) {
                    break;
                }
            }
            return;
        }
        
        // at last
        if (ca == 0) {
            System.out.println("NONE\t" + each);
        }
    }
}

/* */
