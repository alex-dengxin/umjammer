/*
 * This file is part of J2ME VNC.
 *
 * Copyright (c) 2003 Michael Lloyd Lee
 *
 * J2ME VNC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * J2ME VNC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2ME VNC; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/* This has to be the ugliest code in existence!
 */

package tk.wetnet.j2me.vnc;

import com.nttdocomo.util.Timer;
import com.nttdocomo.util.TimerListener;


/**
 * Ticker. 
 *
 * @author	<a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version	0.00	2004/09/11	nsano	initial version <br>
 */
class Ticker implements TimerListener {

    VNCCanvas vnc;
    
    public Ticker(VNCCanvas v) {
	vnc = v;
    }

    public void timerExpired(Timer source) {
	vnc.tick();
    }
}

/* */
