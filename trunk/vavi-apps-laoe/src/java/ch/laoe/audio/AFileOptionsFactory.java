/*
 * This file is part of LAoE.
 * 
 * LAoE is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * LAoE is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LAoE; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.laoe.audio;


/**
 * factory of all file-options classes.
 * 
 * @author olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * 
 * @version 25.02.02 first draft oli4 <br>
 */
public class AFileOptionsFactory {

    /**
     * factory
     */
    public static AFileOptions create(String extension) {
        if (extension.equals(".wav"))
            return new AFileOptionsWav();
        else if (extension.equals(".aiff"))
            return new AFileOptionsAiff();
        else if (extension.equals(".aifc"))
            return new AFileOptionsAifc();
        else if (extension.equals(".au"))
            return new AFileOptionsAu();
        else if (extension.equals(".snd"))
            return new AFileOptionsSnd();
        else if (extension.equals(".mp3"))
            return new AFileOptionsMp3();
        else if (extension.equals(".gsm"))
            return new AFileOptionsGsm();
        else if (extension.equals(".laoe"))
            return new AFileOptionsLaoe();
        // ...

        return null;
    }
}
