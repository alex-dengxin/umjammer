/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;


/**
 * Formatted. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Formatted {

    /**
     * パラメータのフォーマットタイプ 
     */
    Class<? extends Formatter> formatter();

    /**
     * フォーマットする場合の値 
     */
    String value() default "";

    /**
     * TODO アノテーションがメソッド指定の場合 
     */
    static class Util {

        /** */
        public static boolean isFormatted(Field field) {
            return field.getAnnotation(Formatted.class) != null;
        }

        /**
         * 
         * @param field @{@link Parameter} annotated field.
         * @param fieldValue field value
         * @throws NullPointerException when field is not annotated by {@link Formatted}
         */
        public static String getFieldValueAsString(Field field, Object fieldValue) {
            try {
                Formatted formatted = field.getAnnotation(Formatted.class);
                Formatter formatter = formatted.formatter().newInstance();
                String format = formatted.value();
//System.err.println("formatter: " + formatter + ", " + format);
                return fieldValue == null ? "null" : formatter.format(format, fieldValue); 
            } catch (Exception e) {
                throw (RuntimeException) new IllegalStateException().initCause(e);
            }
        }
    }
}

/* */
