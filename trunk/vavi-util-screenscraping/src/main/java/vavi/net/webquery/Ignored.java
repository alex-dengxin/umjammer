/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.webquery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import vavi.beans.BeanUtil;


/**
 * Ignored. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070224 nsano initial version <br>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignored {

    /**
     * 無視する条件を Script で表記
     * result must be boolean
     * access field value by field name.
     * default: ignored when field value is null.
     */
    String when() default "";

    /**
     * TODO アノテーションがメソッド指定の場合 
     */
    static class Util {

        /**
         * @param field
         * @param bean
         * @param value
         */
        public static boolean isIgnoreable(Field field, Object bean) {
            Ignored ignored = field.getAnnotation(Ignored.class);
            if (ignored == null) {
                return false;
            }

            String script = ignored.when();
            if (script.isEmpty()) {
                return BeanUtil.getFieldValue(field, bean) == null;
            }

            ScriptEngine engine = WebQuery.Util.getScriptEngine(bean);
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

            Set<Field> parameterFields = WebQuery.Util.getParameterFields(bean);
            for (Field parameterField : parameterFields) {
                Object fieldValue = BeanUtil.getFieldValue(parameterField, bean);
                bindings.put(parameterField.getName(), fieldValue);
System.err.println("field: " + parameterField.getName() + ", " + (fieldValue == null ? null : fieldValue.getClass().getSimpleName() + ", " + fieldValue));
            }

            try {
                Object result = engine.eval(script);
System.err.println("script: " + script);
                if (Boolean.class.isInstance(result)) {
                    return (Boolean) result;
                } else {
                    throw new IllegalArgumentException("script doesn't return boolean: " + result);
                }
            } catch (ScriptException e) {
                throw new IllegalArgumentException("invalid script: " + script);
            }
        }
    }
}

/* */
