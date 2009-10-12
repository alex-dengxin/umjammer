/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.betwixt.io.BeanReader;

import vavi.util.StringUtil;


/**
 * MovaDeviceController.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040313 nsano initial version <br>
 */
public class MovaDeviceController {

    /** */
    private MovaDevice md;

    /** */
    public MovaDeviceController(String serialPort, InputStream is) throws Exception {

        md = new MovaDevice(serialPort);

        BeanReader reader = new BeanReader();
        reader.registerBeanClass(CommandList.class);

        CommandList bean = (CommandList) reader.parse(is);
        System.err.println("bean: " + bean);
        System.err.println("count: " + bean.getCommands().size());

        Iterator<Command> i = bean.getCommands().iterator();
        while (i.hasNext()) {
            Command command = i.next();
System.err.println("commands: " + StringUtil.paramString(command));
	    if ("sleep".equals(command.getName())) {
                int time = Integer.parseInt(command.getValue());
                try { Thread.sleep(time); } catch (Exception e) {}
            } else if ("key".equals(command.getName())) {
                md.writeCommand(3, md.getKeyCode(command.getValue()));
            }
        }
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream(args[1]);
        MovaDeviceController c = new MovaDeviceController(args[0], is);
    }
}

/* */
