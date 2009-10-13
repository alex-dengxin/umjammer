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

package tk.wetnet.vnc;


/**
 * Sent updates from VNC server via the RFBProto class.
 * Objects which wish to receive the updates sent from the VNC server should
 * implement this interface.
 */
public interface DrawToable {
    /**
     * A request to draw a rectangle.
     * @param red The about of red, between 0 and 255 of the square.
     * @param green The about of green, between 0 and 255 of the square.
     * @param blue The about of blue, between 0 and 255 of the square.
     * @param x The x-offset from the top-right hand corner
     * @param y The y-offset from the top-right hand corner
     * @param w The width of rectangle.
     * @param h The height of rectangle.
     */
    public void draw(int red, int green, int blue, int x, int y, int w, int h);
    /**
     * A request to copy a rectangle.
     * @param x The starting x-offset of the rectangle to copy.
     * @param y The starting y-offset of the rectangle to copy.
     * @param w The width of the rectangle to copy.
     * @param h The height of the rectangle to copy.
     * @param srcx The location to copy this rectangle to.
     * @param srcy The location to copy this rectangle to.
     */
    public void copyRect( int x, int y, int w, int h, int srcx, int srcy);
    
    /**
     * Informs the current update has started.
     */
    public void startUpdate();
    /**
     * Informs the current update has ended.
     */
    public void endUpdate();
    
    /**
     * A request to ring a bell.
     */
    public void ringBell();

    /**
     * The RFBProto is ready to receive requests.
     */
    public void ready();

    /**
     * Something has gone wrong.
     * @param error The message to display to the user.
     */
    public void error(String error);

    public void incConnectionStatus( );
}

