/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.net.proxy;

import java.util.List;

import vavix.net.proxy.ProxyChanger.InternetAddress;


/**
 * ProxyServerDao. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 071004 nsano initial version <br>
 */
public interface ProxyServerDao {

    /** 計算値ではなく静的リストを返すようにしてください。 */
    List<InternetAddress> getProxyInetSocketAddresses();
}

/* */
