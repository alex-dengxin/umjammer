/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.webquery;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


/**
 * WebQuery. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebQuery {

    /**
     * 
     */
    String url() default "";

    /** */
    boolean doInput() default true;

    /** */
    boolean doOutput() default false;

    /** script engine */
    String scriptEngine() default "js";

    /** */
    Class<? extends QueryHandler> urlHandler();

    /** */
    class Util {

        /** */
        public static QueryHandler getQueryHandler(Object bean) {
            try {
                WebQuery webQuery = bean.getClass().getAnnotation(WebQuery.class);
                QueryHandler urlHandler = webQuery.urlHandler().newInstance();
                return urlHandler;
            } catch (Exception e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }

        /** */
        public static ScriptEngine getScriptEngine(Object bean) {
            WebQuery webQuery = bean.getClass().getAnnotation(WebQuery.class);
            if (webQuery == null) {
                throw new IllegalArgumentException("bean is not annotated with @WebQuery");
            }
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName(webQuery.scriptEngine());
            return engine;
        }

        /**
         * @return {@link Parameter} annotated fields
         */
        public static Set<Field> getParameterFields(Object bean) {
            //
            WebQuery webQuery = bean.getClass().getAnnotation(WebQuery.class);
            if (webQuery == null) {
                throw new IllegalArgumentException("bean is not annotated with @WebQuery");
            }

            //
            Set<Field> parameterFields = new HashSet<Field>(); 

            for (Field field : bean.getClass().getDeclaredFields()) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                if (parameter != null) {
                    parameterFields.add(field);
                }
            }

            return parameterFields;
        }

        /**
         * @return UTF-8 URL encoded 
         */
        public static Query getQuery(Object bean) {
            //
            WebQuery webQuery = bean.getClass().getAnnotation(WebQuery.class);
            if (webQuery == null) {
                throw new IllegalArgumentException("bean is not annotated with @WebQuery");
            }
            String url = webQuery.url();
            boolean doInput = webQuery.doInput();
            boolean doOutput = webQuery.doOutput();
            QueryHandler queryHandler = WebQuery.Util.getQueryHandler(bean);

            //
            Map<String, String> parameters = new HashMap<String, String>(); 

            //
            for (Field field : bean.getClass().getDeclaredFields()) {
//System.err.println("field: " + field.getName());
                Parameter parameter = field.getAnnotation(Parameter.class);
                if (parameter == null) {
System.err.println("not @Parameter: " + field.getName());
                    continue;
                }

                if (!parameter.required() && Ignored.Util.isIgnoreable(field, bean)) {
System.err.println("ignoreable: " + field.getName());
                    continue;
                }

                String name = Parameter.Util.getParameterName(field, bean, parameter);
                String value = Parameter.Util.getParameterValue(field, bean, parameter);
//System.err.println("value: " + name + ", " + value);
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    assert false;
                }
                parameters.put(name, value);
System.err.println("use: " + name + ", " + value);
            }

            return queryHandler.getQuery(url, parameters, doInput, doOutput);
        }
    }
}

/* */
