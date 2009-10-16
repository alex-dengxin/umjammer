/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer00;

import vavi.net.rest.Parameter;
import vavi.net.rest.Rest;


/**
 * YouTubeListByTag. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070322 nsano initial version <br>
 */
@Rest(protocol = "HTTP",
      method="GET",
      url="http://www.youtube.com/api2_rest")
public class YouTubeListByTag {

    @Parameter(name = "dev_id", required = true)
    private String token; 
    @Parameter(required = true)
    private String method = "youtube.videos.list_by_tag"; 
    @Parameter(required = true)
    private String tag; 

    /** */
    public String getToken() {
        return token;
    }

    /** */
    public void setToken(String token) {
        this.token = token;
    }

    /** */
    public String getTag() {
        return tag;
    }

    /** */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /** */
    public String getMethod() {
        return method;
    } 
}

/* */
