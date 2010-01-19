/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp.jsr223;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * ScriptEngineFactory.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080602 nsano make the initial version <br>
 */
public class ScriptEngineFactory implements javax.script.ScriptEngineFactory {
    
    /** */
    private static final String FILEEXT = ".lisp";
    
    /** */
    private static final String [] MIMETYPES = {
        "text/plain",
        "text/x-lisp",
        "application/x-lisp"
    };
    
    /** */
    private static final String [] NAMES = {
        "VaviLisp",
        "lisp"
    };
    
    /** */
    private ScriptEngine myScriptEngine;
    /** */
    private List<String> extensions;
    /** */
    private List<String> mimeTypes;
    /** */
    private List<String> names;
    
    /** */
    public ScriptEngineFactory() {
        myScriptEngine = new ScriptEngine();
        extensions = Collections.nCopies(1, FILEEXT);
        mimeTypes = Arrays.asList(MIMETYPES);
        names = Arrays.asList(NAMES);
    }
    
    /* */
    public String getEngineName() {
        return getScriptEngine().get(ScriptEngine.ENGINE).toString();
    }
    
    /* */
    public String getEngineVersion() {
        return getScriptEngine().get(ScriptEngine.ENGINE_VERSION).toString();
    }
    
    /* */
    public List<String> getExtensions() {
        return extensions;
    }
    
    /* */
    public List<String> getMimeTypes() {
        return mimeTypes;
    }
    
    /* */
    public List<String> getNames() {
        return names;
    }
    
    /* */
    public String getLanguageName() {
        return getScriptEngine().get(ScriptEngine.LANGUAGE).toString();
    }
    
    /* */
    public String getLanguageVersion() {
        return getScriptEngine().get(ScriptEngine.LANGUAGE_VERSION).toString();
    }
    
    /* */
    public Object getParameter(String key) {
        return getScriptEngine().get(key).toString();
    }
    
    /* */
    public String getMethodCallSyntax(String obj, String m, String... args)  {
        StringBuilder sb = new StringBuilder();
        sb.append("(" + obj + " " + m);
        int len = args.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        sb.append(")");
        return sb.toString();
    }
    
    /* */
    public String getOutputStatement(String toDisplay) {
        return "(print " + toDisplay + ")";
    }
    
    /* */
    public String getProgram(String ... statements) {
        StringBuilder sb = new StringBuilder();
        int len = statements.length;
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(statements[i]);
        }
        return sb.toString();
    }
    
    /* */
    public ScriptEngine getScriptEngine() {
        return myScriptEngine;
    }
}

/* */
