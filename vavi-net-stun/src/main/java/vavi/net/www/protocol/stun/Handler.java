/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.www.protocol.stun;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import vavi.net.stun.NodeInfo;
import vavi.net.stun.stun.STUNCheckServer.StunType;


/**
 * stun Handler.
 *
 * @protocol "stun:nodeId?(node=nodeInfo)+(&type=GLOBAL)*"
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 031220 nsano initial version <br>
 */
public final class Handler extends URLStreamHandler {

    /** */
    private List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();

    /** */
    private String id;

    /** */
    private StunType type;

    /** */
    protected void parseURL(URL url, String spec, int start, int limit) { 
System.err.println("spec: " + spec);
        String protocol = spec.substring(0, start - 1);
System.err.println("protocol: " + protocol);
        if ("stun".equals(protocol)) {
            int p = spec.indexOf('?', start);
            if (p != -1) {
                //
                this.id = spec.substring(start, p);
System.err.println("id: " + id);

                //
                String parameters = spec.substring(p + 1);
                StringTokenizer st = new StringTokenizer(parameters, "&");
                while (st.hasMoreTokens()) {
                    String pair = st.nextToken();
                    p = pair.indexOf('=');
                    String name;
                    String value = null;
                    if (p != -1) {
                        name = pair.substring(0, p);
                        value = pair.substring(p + 1);
                    } else {
                        name = pair;
                    }

                    if ("node".equals(name)) {
                        NodeInfo nodeInfo = NodeInfo.parseNodeInfo(value);
System.err.println("nodeInfo: " + nodeInfo);
                        nodeInfos.add(nodeInfo);
                    }
                    if ("type".equals(name)) {
                        this.type = StunType.valueOf(value);
System.err.println("type: " + type);
                    }
                }
            }
            start = limit;
        }

        super.parseURL(url, spec, start, limit); 
    } 

    /** */
    protected URLConnection openConnection(URL url) 
        throws IOException {

        URLConnection uc = null;

        if (type == null) {
            uc = new StunURLConnection(url, id, nodeInfos.toArray(new NodeInfo[nodeInfos.size()]));
        } else {
            uc = new StunURLConnection(url, id, nodeInfos.toArray(new NodeInfo[nodeInfos.size()]), type);
        }

        return uc;
    }

    //----

    /** */
    public static void main(String[] args) throws Exception {
        URLConnection uc = new URL(args[0]).openConnection();
        ((StunURLConnection) uc).connect("server");
    }
}

/* */
