/*
 * Copyright (c) 2005 by Naohide Sano, All rights rserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Properties;


/**
 * SystemPropertiesSecurityProviderAdder.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (vavi)
 * @version 0.00 051215 nsano initial version <br>
 */
public class PropertiesSecurityProviderAdder {

    /** */
    public static void exec() throws Exception {
        Enumeration<?> e = props.propertyNames();
System.err.println("PropertiesSecurityProviderAdder::exec: " + props.size());
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            if (name.matches("security.provider.\\w+")) {
                String value = props.getProperty(name);
                Provider provider = (Provider) Class.forName(value).newInstance();
System.err.println("PropertiesSecurityProviderAdder::exec: " + name + ", " + provider);
                Security.addProvider(provider);
            }
        }
    }

    /** */
    private static Properties props = new Properties();

    /** */
    static {
        try {
            props.load(GenericInstrumentation.class.getResourceAsStream("PropertiesSecurityProviderAdder.properties"));
        } catch (IOException e) {
e.printStackTrace(System.err);
            throw (RuntimeException) new IllegalStateException().initCause(e);
        }
    }
}

/* */
