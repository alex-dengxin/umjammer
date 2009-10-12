/*
 * Copyright (c) 2004 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * CommandList.
 * 
 * @author <a href=mailto:vavivavi@yahoo.co.jp>Naohide Sano</a> (nsano)
 * @version 0.00 040315 nsano initial version <br>
 */
public class CommandList {

    /** */
    private List<Command> commands = new ArrayList<Command>();

    /** */
    public void addCommand(Command command) {
        commands.add(command);
    }

    /** */
    public Collection<Command> getCommands() {
        return commands;
    }
}

/* */
