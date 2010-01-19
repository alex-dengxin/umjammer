/*
 * Lisp Interpreter
 *
 * Copyright (c) 1997 by Naohide Sano, All rights reserved.
 *
 * Original by SunSoft Java Series CD-ROM "Java by Example"
 * Programmed by Naohide Sano
 */

package vavi.apps.lisp;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * S 式で表現された文字列をストリームから読み込み実行するクラスです。 インタラクティブなメソッドと非インタラクティブなメソッドがあります． Lisp
 * エンジンは Scheme を基にしています． extension にパッケージ名を指定することによりユーザが作成した Lisp 関数を
 * LispInterpreter に組み込むことが可能です．
 * 
 * <pre>
 *  
 *   Default Functions List
 *   
 * </pre>
 * 
 * <table border="1">
 * <tr>
 * <td bgcolor="#00FFFF">シンボル</td>
 * <td bgcolor="#00FFFF">説明</td>
 * <td bgcolor="#00FFFF">戻り値</td>
 * <td bgcolor="#00FFFF">S 式</td>
 * </tr>
 * <tr>
 * <td bgcolor="#FFFFCC" colspan="4">Special Forms</td>
 * </tr>
 * <tr>
 * <td>quote</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>if</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>lambda</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>define</td>
 * <td> </td>
 * <td>LispSymbol</td>
 * <td>(define (s e) e)</td>
 * </tr>
 * <tr>
 * <td>set!</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>apply</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>while</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td bgcolor="#FFFFCC" colspan="4">Primitives</td>
 * </tr>
 * <tr>
 * <td>cons</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>car</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>cdr</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>set-car!</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>set-cdr!</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>exit</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>not</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>null?</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>number?</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>symbol?</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>pair?</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>make</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>make-environment</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>eval</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>load</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>open</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>close</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>read</td>
 * <td> </td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>print</td>
 * <td> </td>
 * <td>LispBoolean</td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>catch</td>
 * <td>例外処理</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>throw</td>
 * <td>例外発生</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>+</td>
 * <td>足し算</td>
 * <td> </td>
 * <td>(+ number number ...)</td>
 * </tr>
 * <tr>
 * <td>*</td>
 * <td>掛け算</td>
 * <td> </td>
 * <td>(* number number ...)</td>
 * </tr>
 * <tr>
 * <td>-</td>
 * <td>引き算</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>/</td>
 * <td>割り算</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>=</td>
 * <td>イコール</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>&lt;</td>
 * <td>より小さい</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>&gt;</td>
 * <td>より大きい</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>&lt;=</td>
 * <td>以下</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * <tr>
 * <td>&gt;=</td>
 * <td>以上</td>
 * <td> </td>
 * <td> </td>
 * </tr>
 * </table>
 * 
 * <p>
 * コメントは ; で始まり行末までコメントとみなされる．
 * 
 * @see LispPrimitive
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 970730 nsano make the initial version <br>
 *          1.01 971020 nsano refine <br>
 *          1.02 971025 nsano deprecate setEncoding() <br>
 *          1.03 971116 nsano add the prompt to interpret() <br>
 *          1.04 971203 nsano fix the extension path, catch a eof failure <br>
 *          1.05 971216 nsano add main() <br>
 *          1.06 980130 nsano deprecate encoding <br>
 *          1.07 980130 nsano add the encoding to the reader <br>
 *          1.08 990203 nsano add methods for symbol set/get <br>
 */
public final class LispInterpreter {

    /** このクラスのパッケージ名のベース名 */
    private static final String rootPath = "vavi.apps.lisp";

    /** デフォルトプリミティブのパッケージ名のベース名 */
    private static final String primitivePath = rootPath + "." + "prim";

    /** デフォルトの拡張プリミティブのパッケージ名のベース名 */
    private static final String defaultPath = rootPath + "." + "extend";

    /** 拡張プリミティブのクラスがあるパッケージ名 */
    private String extentionPath;

    /** エンコーディング */
    private String encoding;

    /** デフォルトのエンコーディング */
    private static final String defaultEncoding = System.getProperty("file.encoding");

    /**
     * Creates a lisp engine instance using specified parameters.
     * 
     * <ul>
     * <li>the encoding is the value of the System property
     * <li>the extension path is {@link #defaultPath} + ".extend"
     * </ul>
     * 
     * @param in the InputStream of S expression to be read
     * @throws IOException 
     */
    public LispInterpreter(InputStream in) throws IOException {
        this(in, defaultEncoding, defaultPath);
    }

    /**
     * Creates a lisp engine instance using specified parameters.
     * 
     * <ul>
     * <li>the extension path is {@link #defaultPath} + ".extend"
     * </ul>
     * 
     * @param in the InputStream of S expression to be read
     * @param extend the package name of lisp extention
     * @throws IOException 
     */
    public LispInterpreter(InputStream in, String extend) throws IOException {
        this(in, defaultEncoding, extend);
    }

    /**
     * Creates a lisp engine instance using specified parameters.
     * 
     * @param in the InputStream of S expression to be read
     * @param enc the encoding of S expression
     * @param extend the package name of lisp extension
     * @throws IOException 
     */
    public LispInterpreter(InputStream in, String enc, String extend) throws IOException {
        if (enc == null) {
            encoding = defaultEncoding;
        } else {
            encoding = enc;
        }

        extentionPath = extend;

        reader = new LispReader(new InputStreamReader(in, encoding), symbols);
        evaluator = new LispEvaluator(extentionPath);

        init();
    }
    
    /**
     */
    public LispInterpreter() {
        this(defaultEncoding, defaultPath);
    }

    /**
     */
    public LispInterpreter(String enc, String extend) {
        if (enc == null) {
            encoding = defaultEncoding;
        } else {
            encoding = enc;
        }
        
        extentionPath = extend;

        evaluator = new LispEvaluator(extentionPath);

        init();
    }

    /** */
    private void init() {
        createSpecialForm("quote", "QuoteSpecialForm");
        createSpecialForm("if", "IfSpecialForm");
        createSpecialForm("lambda", "LambdaSpecialForm");
        createSpecialForm("define", "DefineSpecialForm");
        createSpecialForm("set!", "SetSpecialForm");
        createSpecialForm("apply", "ApplySpecialForm");
        createSpecialForm("while", "WhileLoopSpecialForm");
        createBuiltIn("cons", "LispConsFun");
        createBuiltIn("car", "LispCarFun");
        createBuiltIn("cdr", "LispCdrFun");
        createBuiltIn("set-car!", "LispSetCarFun");
        createBuiltIn("set-cdr!", "LispSetCdrFun");
        createBuiltIn("exit", "LispExitFun");
        createBuiltIn("not", "LispNotFun");
        createBuiltIn("null?", "LispNullFun");
        createBuiltIn("number?", "LispNumberFun");
        createBuiltIn("symbol?", "LispSymbolFun");
        createBuiltIn("pair?", "LispPairFun");
        createBuiltIn("make", "LispMakeFun");
        createBuiltIn("make-environment", "LispMakeEnvFun");
        createBuiltIn("eval", "LispEvalFun");
        createBuiltIn("load", "LispLoadFun");
        createBuiltIn("open", "LispOpenFun");
        createBuiltIn("close", "LispCloseFun");
        createBuiltIn("read", "LispReadFun");
        createBuiltIn("print", "LispPrintFun");
        createBuiltIn("catch", "LispCatchFun");
        createBuiltIn("throw", "LispThrowFun");
        createBuiltIn("+", "LispPlusFun");
        createBuiltIn("*", "LispTimesFun");
        createBuiltIn("-", "LispMinusFun");
        createBuiltIn("/", "LispDivideFun");
        createBuiltIn("=", "LispEqlFun");
        createBuiltIn(">", "LispGTFun");
        createBuiltIn("<", "LispLTFun");
        createBuiltIn(">=", "LispGEFun");
        createBuiltIn("<=", "LispLEFun");
    }

    /**
     * Gets the encoding of the lisp reader.
     * 
     * @return the encoding of the lisp reader.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Reads the stream and convert to S expression
     * 
     * @return S expression
     * @throws IOException If an IO error occurs
     * @throws LispException If a lisp error occurs
     */
    public Object read() throws LispException, IOException {
        return reader.read();
    }

    /**
     * Evaluates S expression.
     * 
     * @see #read
     * @param expression the S expression got by <code>#read</code>
     * @return the result
     * @throws LispException If a lisp error occurs
     */
    public Object eval(Object expression) throws LispException {
        return evaluator.eval(expression, environment, this);
    }

    /**
     * Prints the result got by <code>#eval</code>.
     * 
     * @see #eval
     * @param result the result got by <code>#eval</code>
     */
    public void print(Object result) {
        LispPrinter.println(result);
    }

    /**
     * Interprets S expression input from the console with prompt
     * <tt>"==> "</tt>, and output the result to the screen.
     */
    public void interpret() {
        while (true) {

            System.err.print("==> ");
            System.err.flush();

            try {
                Object expression = read();
                Object result = eval(expression);
                print(result);
            } catch (CommentLispException e) {
                // ignore
            } catch (ExitLispException e) {
                return;
            } catch (EOFException e) {
                return;
            } catch (LispException e) {
                System.err.println(e);
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Interprets the S expression stream quietly. this method is useful in your
     * application which use the lisp interpreter.
     * 
     * @return Vector of result set. A result in this vector is one S
     *         expression evaluated.
     * @throws LispException If a lisp error occurs
     * @throws IOException If an IO error occurs
     */
    public List<Object> interpretQuiet() throws LispException, IOException {
        List<Object> results = new ArrayList<Object>();

        while (true) {

            try {
                Object expression = read();
                results.add(eval(expression));
            } catch (CommentLispException e) {
                // ignore
            } catch (ExitLispException e) {
                return results;
            } catch (EOFException e) {
                return results;
            }
        }
    }

    /**
     * Gets the symbol hash table (internal use).
     * 
     * @return the symbol hash table
     */
    public Map<String, LispSymbol> getSymbols() {
        return symbols;
    }

    /**
     * Gets the lisp environment (internal use).
     * 
     * @return the lisp environment
     */
    public LispEnv getEnv() {
        return environment;
    }

    /**
     * Gets the lisp reader (internal use).
     * 
     * @return the lisp reader
     */
    public LispReader getReader() {
        return reader;
    }

    /**
     */
    public void setReader(Reader reader) {
        this.reader = new LispReader(reader, symbols);
    }

    /**
     * Gets the lisp evaluator (internal use).
     * 
     * @return the lisp evaluator
     */
    public LispEvaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Creates a "built in" primitive.
     */
    private void createBuiltIn(String name, String funcName) {
        set(name, new LispBuiltIn(funcName, primitivePath));
    }

    /**
     * Creates a "special form" primitive.
     */
    private void createSpecialForm(String name, String funcName) {
        set(name, new LispBuiltIn(funcName, rootPath));
    }

    /**
     * Sets a symbol with value.
     */
    public void set(String name, Object value) {
        LispSymbol symbol = LispSymbol.intern(symbols, name);
        symbol.setLocalValue(value);
    }

    /**
     * Gets a value of specified name of symbol
     */
    public Object get(String name) {
        LispSymbol symbol = LispSymbol.intern(symbols, name);
        return symbol.localValue();
    }

    /**
     * Change the lisp extension path.
     * 
     * @param path the lisp extension class path
     */
    public void changeExtendEvaluator(String path) {
        evaluator = new LispEvaluator(path);
    }

    /** シンボルのテーブル */
    private Map<String, LispSymbol> symbols = new HashMap<String, LispSymbol>();

    /** 環境 */
    private LispEnv environment = new LispEnv();

    /** リーダ */
    private LispReader reader;

    /** 評価エンジン */
    private LispEvaluator evaluator;

    /**
     * Executes the lisp interpreter.
     * 
     * <pre>
     *  
     *   usage: java LispInterpreter extension_path
     *   
     * </pre>
     * 
     * @throws Exception If an error occurs.
     */
    public static void main(String[] args) throws Exception {

        LispInterpreter interpreter;

        if (args.length == 0) {
            interpreter = new LispInterpreter(System.in);
        } else {
            interpreter = new LispInterpreter(new FileInputStream(args[0]));
        }

        List<?> resluts = interpreter.interpretQuiet();
        for (Object reslut : resluts) {
            System.out.println(reslut);
        }
    }
}

/* */
