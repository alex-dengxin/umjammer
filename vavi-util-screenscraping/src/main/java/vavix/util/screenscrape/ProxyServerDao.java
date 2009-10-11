/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.util.screenscrape;

import java.util.List;

import vavix.util.screenscrape.ProxyChanger.InternetAddress;


/**
 * ProxyServerDao. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071004 nsano initial version <br>
 */
public interface ProxyServerDao {

    /** */
    List<InternetAddress> getProxyInetSocketAddresses();
}

/* */
