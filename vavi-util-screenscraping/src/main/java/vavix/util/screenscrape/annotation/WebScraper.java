/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape.annotation;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * WebScraper. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/09/30 nsano initial version <br>
 */
@java.lang.annotation.Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebScraper {

    /** for {@link DefaultInputHandler} */
    String url() default "";

    /** handler for web input */
    Class<? extends InputHandler<?>> input() default DefaultInputHandler.class;

    /** parser for input */
    @SuppressWarnings("rawtypes")
    Class<? extends Parser> parser() default XPathParser.class;

    /** for 2 step XPath */
    String value() default "";

    /** repeatable data or not */
    boolean isCollection() default true;

    /** input encoding */
    String encoding() default "UTF-8";

    /** */
    class Util {

        /** */
        public static InputHandler<?> getInputHandler(Class<?> type) {
            try {
                WebScraper webScraper = type.getAnnotation(WebScraper.class);
                return webScraper.input().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        /** */
        @SuppressWarnings("unchecked")
        public static <T> Parser<?, T> getParser(Class<T> type) {
            try {
                WebScraper webScraper = type.getAnnotation(WebScraper.class);
                return webScraper.parser().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        /** */
        public static boolean isCollection(Class<?> type) {
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }
            return webScraper.isCollection();
        }

        /** for 2 step XPath */
        public static String getValue(Class<?> type) {
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }
            return webScraper.value();
        }

        /** */
        public static String getEncoding(Class<?> type) {
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }
            return webScraper.encoding();
        }

        /** */
        public static String getUrl(Class<?> type) {
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }
            return webScraper.url();
        }

        /**
         * @return {@link Target} annotated fields
         */
        public static Set<Field> getTargetFields(Class<?> type) {
            //
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }

            //
            Set<Field> targetFields = new HashSet<Field>(); 

            for (Field field : type.getDeclaredFields()) {
                Target target = field.getAnnotation(Target.class);
                if (target != null) {
                    targetFields.add(field);
                }
            }

            return targetFields;
        }

        /**
         * Scrapes datum.
         * 
         * @param type type annotated by {@link WebScraper}
         * @param args parameters for input handler
         * @return List of type objects.
         */
        public static <I, T> List<T> scrape(Class<T> type, String ... args) throws IOException {
            //
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }

            @SuppressWarnings("unchecked")
            Parser<I, T> parser = (Parser<I, T>) getParser(type);
            @SuppressWarnings("unchecked")
            InputHandler<I> inputHandler = (InputHandler<I>) getInputHandler(type);
            
            // inputHandler がデフォルトの場合 url が設定されていれば
            // 自動的に url が InputHandler#getInput() の引数に採用される
            if (inputHandler instanceof DefaultInputHandler) {
                String url = WebScraper.Util.getUrl(type);
                if (url != null && !url.isEmpty()) {
                    if (args != null && args.length > 0) {
                        args[0] = url;
                    } else {
                        args = new String[] { url };
                    }
                }
            }
            return parser.parse(type, inputHandler, args);
        }

        /**
         * Scrapes datum.
         * 
         * @param type type annotated by {@link WebScraper}
         * @param args parameters for input handler
         * @return List of type objects.
         */
        public static <I, T> void foreach(Class<T> type, EachHandler<T> eachHandler, String ... args) throws IOException {
            //
            WebScraper webScraper = type.getAnnotation(WebScraper.class);
            if (webScraper == null) {
                throw new IllegalArgumentException("type is not annotated with @WebScraper");
            }

            @SuppressWarnings("unchecked")
            Parser<I, T> parser = (Parser<I, T>) getParser(type);
            @SuppressWarnings("unchecked")
            InputHandler<I> inputHandler = (InputHandler<I>) getInputHandler(type);
            
            // inputHandler がデフォルトの場合 url が設定されていれば
            // 自動的に url が InputHandler#getInput() の引数に採用される
            if (inputHandler instanceof DefaultInputHandler) {
                String url = WebScraper.Util.getUrl(type);
                if (url != null && !url.isEmpty()) {
                    if (args != null && args.length > 0) {
                        args[0] = url;
                    } else {
                        args = new String[] { url };
                    }
                }
            }
            parser.foreach(type, eachHandler, inputHandler, args);
        }
    }
}

/* */
