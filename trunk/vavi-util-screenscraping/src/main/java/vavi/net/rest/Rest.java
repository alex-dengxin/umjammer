/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.rest;

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
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


/**
 * Rest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rest {

    /** プロトコル */
    String protocol() default "HTTP";

    /**
     * protocol が HTTP の場合の URL
     * TODO WADL では base と path に分かれてる
     */
    String url() default "";

    /** protocol が HTTP の場合の HTTP メソッド */
    String method() default "";

    /** script engine */
    String scriptEngine() default "js";

    /** */
    class Util {
        /** */
        public static String getProtocol(Object bean) {
            Rest rest = bean.getClass().getAnnotation(Rest.class);
            if (rest == null) {
                throw new IllegalArgumentException("bean is not annotated with @Rest");
            }
            return rest.protocol();
        }

        /** */
        public static String getMethod(Object bean) {
            Rest rest = bean.getClass().getAnnotation(Rest.class);
            if (rest == null) {
                throw new IllegalArgumentException("bean is not annotated with @Rest");
            }
            return rest.method();
        }

        /** */
        public static ScriptEngine getScriptEngine(Object bean) {
            Rest rest = bean.getClass().getAnnotation(Rest.class);
            if (rest == null) {
                throw new IllegalArgumentException("bean is not annotated with @Rest");
            }
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName(rest.scriptEngine());
            return engine;
        }

        /**
         * @return {@link Parameter} annotated fields
         */
        public static Set<Field> getParameterFields(Object bean) {
            //
            Rest rest = bean.getClass().getAnnotation(Rest.class);
            if (rest == null) {
                throw new IllegalArgumentException("bean is not annotated with @Rest");
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
        public static String getUrl(Object bean) {
            //
            Rest rest = bean.getClass().getAnnotation(Rest.class);
            if (rest == null) {
                throw new IllegalArgumentException("bean is not annotated with @Rest");
            }
            String protocol = rest.protocol();
            String method = rest.method();
            String url = rest.url();

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
System.err.println("value: " + name + ", " + value);
                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    assert false;
                }
                parameters.put(name, value);
System.err.println("use: " + name + ", " + value);
            }

            StringBuilder sb = new StringBuilder();
            if ("HTTP".equals(protocol)) {
                if ("GET".equals(method)) {
                    for (Entry<String, String> entry : parameters.entrySet()) {
                        sb.append(sb.length() == 0 ? '?' : '&');
                        sb.append(entry.getKey());
                        sb.append('=');
                        sb.append(entry.getValue());
                    }
                } else if ("POST".equals(method)) {
                    throw new IllegalArgumentException("unknown method: " + method);
                } else {
                    throw new IllegalArgumentException("unknown method: " + method);
                }
            } else {
                throw new IllegalArgumentException("unknown protocol: " + protocol);
            }

            return url + sb.toString();
        }
    }
}

/* */
