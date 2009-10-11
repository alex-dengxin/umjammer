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
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import vavi.beans.BeanUtil;


/**
 * Ignored. 
 * <p>
 * When the "when" field does not set, ignored when field value is null.
 * </p>
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
         * <pre>
         * 1. @Ignored
         * 1.1 when = "..."
         *      script evaled value
         * 1.2 when = ""
         * 1.2.1 field value == null
         *      true
         * 1.2.2 field value != null
         *      false
         * 2 !@Ignored
         * 2.1 field value == null
         *      true
         * 2.2 field value != null
         *      false
         * </pre>
         * @param field should be "not required"
         * @param bean
         */
        public static boolean isIgnoreable(Field field, Object bean) {
            Ignored ignored = field.getAnnotation(Ignored.class);
            if (ignored == null) { // && "not required"
                return BeanUtil.getFieldValue(field, bean) == null;
            }

            String script = ignored.when();
            if (script.isEmpty()) {
                return BeanUtil.getFieldValue(field, bean) == null;
            }

            ScriptEngine engine = Rest.Util.getScriptEngine(bean);
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

            Set<Field> parameterFields = Rest.Util.getParameterFields(bean);
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
