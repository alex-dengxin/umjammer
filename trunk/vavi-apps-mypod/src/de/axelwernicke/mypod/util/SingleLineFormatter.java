// myPod
// $Id: SingleLineFormatter.java,v 1.2 2003/07/27 19:34:56 axelwernicke Exp $
//
// Copyright (C) 2002-2003 Axel Wernicke <axel.wernicke@gmx.de>
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package de.axelwernicke.mypod.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;


/**
 * This class provides a logging formatter that gets single line output.
 *
 * @author  axel wernicke
 */
public class SingleLineFormatter extends java.util.logging.Formatter {
    /** date formatter */
    private static String dateFormat = "yyyy.MM.dd HH:mm:ss.S";

    /** Formats a given log record.
     * @param record to format
     * @return log record formatted to a single line
     */
    public String format(LogRecord record) {
        return new StringBuilder().append(new SimpleDateFormat(dateFormat).format(new Date(record.getMillis()))).append(" ").append(record.getLevel()).append(" ").append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append('\t').append(record.getMessage()).append('\n').toString();
    }
}
