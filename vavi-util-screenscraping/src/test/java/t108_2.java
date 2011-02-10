/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.net.URL;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * t108_2. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050604 nsano initial version <br>
 */
public class t108_2 {
    
    /** */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(args[0]);
        org.apache.commons.jxpath.XMLDocumentContainer docCtr = new org.apache.commons.jxpath.XMLDocumentContainer(url);
        JXPathContext context = JXPathContext.newContext(docCtr);
System.out.println("-->" + context.getValue("/a/b/c[@id='0001']")); //「aaa」と表示
        context.setValue("/a/b/c[@id='0006']", "test"); //正常に更新
        
        Document doc1 = (Document) docCtr.getValue();
System.out.println("### Word doc -- \n" + doc1.getDocumentElement());
        
        // 失敗
//  	context.createPathAndSetValue("/a/b/c[1]/e", "bbb");
        
        // 成功(1)
        context.createPathAndSetValue("/a/b/c[1]/d", "abcd");
        // 成功(2)
//  	context.createPathAndSetValue("/a/b/c[@id='0001']/d", "abcd");
        
        // 成功(3)
        context.setFactory(new DOMFactory());
        context.createPathAndSetValue("/a/b/c[1]/e", "bbb");
        
        Document doc2 = (Document) docCtr.getValue();
System.out.println("### Word doc -- \n" + doc2.getDocumentElement());
    }
    
    private static class DOMFactory extends AbstractFactory {
        
        public boolean createObject(JXPathContext context,
                                    Pointer pointer,
                                    Object parent,
                                    String name,
                                    int index) {

            if (parent instanceof Document) {
                Document owner = (Document) parent;
                if ( owner.getDocumentElement() == null) {
                    owner.appendChild(owner.createElement(name));
                    return true;
                }
            } else if (parent instanceof Element) {
                Element parentElement = (Element) parent;
                Document owner = parentElement.getOwnerDocument();
                parentElement.appendChild(owner.createElement(name));
                return true;
            }

            return false;
        }
    }
}

/* */
