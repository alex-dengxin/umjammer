/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Enumeration;
import java.util.Properties;

import javassist.ClassPool;


/**
 * GenericInstrumentation.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051215 nsano initial version <br>
 */
public class GenericInstrumentation {

    /** */
    static ClassPool classPool;
    
    /** */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        classPool = ClassPool.getDefault();
        
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
//System.err.println("here 1.1: " + name);
            if (name.matches("classFileTransformer\\.\\w+")) {
                try {
                    String value = props.getProperty(name);
                    ClassFileTransformer classFileTransformer = (ClassFileTransformer) Class.forName(value).newInstance();
System.err.println(name + ", " + classFileTransformer.getClass());
                    instrumentation.addTransformer(classFileTransformer);
                } catch (Exception f) {
                    f.printStackTrace(System.err);
                }
            }
        }
    }

    /** */
    private static Properties props = new Properties();

    /** */
    static {
        try {
            props.load(GenericInstrumentation.class.getResourceAsStream("GenericInstrumentation.properties"));
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }

    /** */
    public static ClassPool getClassPool() {
        return classPool;
    }
}

/* */
