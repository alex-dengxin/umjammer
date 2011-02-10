/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;


/**
 * Target. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/09/30 nsano initial version <br>
 */
@java.lang.annotation.Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Target {

    /** depends on {@link Parser} */
    String value();

    /**
     * TODO アノテーションがメソッド指定の場合 
     */
    class Util {

        /**
         * @param field {@link @Target} annotated
         */
        public static String getValue(Field field) {
            Target target = field.getAnnotation(Target.class);
            if (target == null) {
                throw new IllegalArgumentException("bean is not annotated with @Target");
            }
            return target.value();
        }
    }
}

/* */
