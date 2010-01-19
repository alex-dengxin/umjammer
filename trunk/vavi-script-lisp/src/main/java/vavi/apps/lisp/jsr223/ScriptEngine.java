/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.jsr223;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import vavi.apps.lisp.CommentLispException;
import vavi.apps.lisp.ExitLispException;
import vavi.apps.lisp.LispInterpreter;


/**
 * ScriptEngine.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080602 nsano make the initial version <br>
 */
public class ScriptEngine implements javax.script.ScriptEngine {
    
    /** */
    private static final String __ENGINE_VERSION__ = "0.0 release 1";
    /** */
    private static final String MY_NAME = "Vavi Lisp";
    /** */
    private static final String MY_SHORT_NAME = "lisp";
    /** */
    private static final String STR_THISLANGUAGE = "Scheme";
    
    /** */
    private static final ScriptEngineFactory myFactory = new ScriptEngineFactory();
    
    /** */
    private ScriptContext defaultContext;

    /** */
    public ScriptEngine() {
        setContext(new SimpleScriptContext());
        // set special values
        put(LANGUAGE_VERSION, "1.0");
        put(LANGUAGE, STR_THISLANGUAGE);
        put(ENGINE, MY_NAME);
        put(ENGINE_VERSION, __ENGINE_VERSION__);
        put(ARGV, ""); // TO DO: set correct value
        put(FILENAME, ""); // TO DO: set correct value
        put(NAME, MY_SHORT_NAME);
        /*
         * I am not sure if this is correct; we need to check if
         * the name really is THREADING. I have no idea why there is
         * no constant as for the other keys
         */
        put("THREADING", null);

        this.interpreter = new LispInterpreter();
    }

    /** */
    private LispInterpreter interpreter;

    /* */
    public Object eval(String script) throws ScriptException {
        return eval(script, getContext());
    }
    
    /* */
    public Object eval(String script, ScriptContext context) throws ScriptException {
        try {
            Reader reader = new StringReader(script);
            interpreter.setReader(reader);

            List<Object> results = new ArrayList<Object>();

            while (true) {
                try {
                    Object expression = interpreter.read();
                    results.add(interpreter.eval(expression));
                } catch (CommentLispException e) {
                    // ignore
                } catch (ExitLispException e) {
                    return results;
                } catch (EOFException e) {
                    return results;
                }
            }
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }
    
    /* */
    public Object eval(String script, Bindings bindings) throws ScriptException {
        Bindings current = getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        getContext().setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        Object result = eval(script);
        getContext().setBindings(current, ScriptContext.ENGINE_SCOPE);
        return result;
    }
    
    /* */
    public Object eval(Reader reader) throws ScriptException {
        return eval(getScriptFromReader(reader));
    }
    
    /* */
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        return eval(getScriptFromReader(reader), scriptContext);
    }
    
    /* */
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return eval(getScriptFromReader(reader), bindings);
    }
    
    /* */
    public void put(String key, Object value) {
        getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
    }
    
    /* */
    public Object get(String key) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
    }
    
    /* */
    public Bindings getBindings(int scope) {
        return getContext().getBindings(scope);
    }
    
    /* */
    public void setBindings(Bindings bindings, int scope) {
        getContext().setBindings(bindings, scope);
    }
    
    /* */
    public Bindings createBindings() {
        return new SimpleBindings();
    }
    
    /* */
    public ScriptContext getContext() {
        return defaultContext;
    }
    
    /* */
    public void setContext(ScriptContext context) {
        defaultContext = context;
    }
    
    /* */
    public ScriptEngineFactory getFactory() {
        return myFactory;
    }
    
    /** */
    private static String getScriptFromReader(Reader reader) {
        try {
            StringWriter script = new StringWriter();
            int data;
            while ((data = reader.read()) != -1) {
                script.write(data);
            }
            script.flush();
            return script.toString();
        } catch (IOException e) {
e.printStackTrace(System.err);
            return null;
        }
    }
}

/* */
