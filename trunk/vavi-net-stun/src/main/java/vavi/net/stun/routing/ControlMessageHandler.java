/*
 * Copyright (c) 2005 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.net.stun.routing;

import vavi.net.stun.messaging.Message;


/**
 * @author suno
 * Created on 2003/07/01
 */
public interface ControlMessageHandler {
    void onControlMessageArrived(Message aMessage);
}
