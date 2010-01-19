package vavi.apps.lisp.jsr223;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.Test;


/**
 * add $DIR/META-INF/services to CLASSPATH 
 */
public class ScriptEngineTest {
    
    @Test
    public void test1() throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        List<ScriptEngineFactory> list = sem.getEngineFactories();
        ScriptEngineFactory f;
        for (int i = 0; i < list.size(); i++) {
            f = list.get(i);
            String engineName = f.getEngineName();
            String engineVersion = f.getEngineVersion();
            String langName = f.getLanguageName();
            String langVersion = f.getLanguageVersion();
            System.out.println("\n---- " + i + " ----\n" + engineName + " " +
                    engineVersion + " (" +
                    langName + " " +
                    langVersion + ")");
            String statement = null;
            if (engineName.equals("Mozilla Rhino")) {
                statement = f.getOutputStatement("\"hello, world\"");
            } else if (engineName.equals("Vavi Lisp")) {
                statement = f.getOutputStatement("(+ 1 1)");
            }
            System.out.println(statement);
            ScriptEngine e = f.getScriptEngine();
            e.eval(statement);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test2() throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("lisp");

        String statement =
            "(define (append x y)" +
            "  (if (null? x)" +
            "      y" +
            "    (cons (car x) (append (cdr x) y))))" +

            "(define (map f l)" +
            "  (if (null? l)" +
            "      ()" +
            "      (cons (f (car l)) (map f (cdr l)))))" +

            "(define (reverse l)" +
            "  (define (reverse-aux x result)" +
            "    (if (null? x)" +
            "        result" +
            "        (reverse-aux (cdr x) (cons (car x) result))))" +
            "  (reverse-aux l ()))" +
            
            "(define (factorial n)" +
            "  (if (<= n 1)" +
            "      1" +
            "    (* n (factorial (- n 1)))))" +

            "(factorial 10)";
        List<Object> results = (List<Object>) engine.eval(statement);
        for (Object result : results) {
            System.out.println(result);
        }
    }
}

/* */