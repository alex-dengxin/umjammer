/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.openfeint;

import net.oauth.OAuthServiceProvider;


/**
 * OpenfeintOAuthProvider. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/04 nsano initial version <br>
 */
public class OpenfeintOAuthProvider extends OAuthServiceProvider {

    public OpenfeintOAuthProvider() {
        super("https://api.openfeint.com/oauth/request_token",
              "https://api.openfeint.com/oauth/authorize",
              "https://api.openfeint.com/oauth/access_token");
    }
}

/* */
