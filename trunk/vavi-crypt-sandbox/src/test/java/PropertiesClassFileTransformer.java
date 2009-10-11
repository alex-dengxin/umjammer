/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Properties;

import javassist.CtClass;
import javassist.CtMethod;


/**
 * PropertiesClassFileTransformer. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 051215 nsano initial version <br>
 */
class PropertiesClassFileTransformer implements ClassFileTransformer {
    /** */
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
        throws IllegalClassFormatException {

        if (className.matches(clazz)) {
System.err.println("PropertiesClassFileTransformer::transform: " + className);
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(classfileBuffer);
                CtClass ctClass = GenericInstrumentation.getClassPool().makeClass(stream);
                
                CtMethod ctMethod = ctClass.getDeclaredMethod(method);
                ctMethod.insertBefore(instruction);
                
                return ctClass.toBytecode();
            } catch (Exception e) {
e.printStackTrace(System.err);
                throw (IllegalClassFormatException) new IllegalClassFormatException().initCause(e);
            }
        } else {
            return null;
        }
    }

    /** */
    static String instruction;

    /** */
    static String clazz;

    /** */
    static String method;

    /** */
    static {
        try {
            Properties props = new Properties();
            props.load(GenericInstrumentation.class.getResourceAsStream("PropertiesClassFileTransformer.properties"));

            instruction = props.getProperty("instruction");
            clazz = props.getProperty("class");
            method = props.getProperty("method");
System.err.println("* PropertiesClassFileTransformer class will modify: ");
System.err.println(" class: " + clazz);
System.err.println(" method: " + method);
System.err.println(" instruction: " + instruction);
        } catch (IOException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
