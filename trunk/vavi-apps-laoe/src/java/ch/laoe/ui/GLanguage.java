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

package ch.laoe.ui;

import ch.oli4.ui.UiLanguage;


/**
 * language handling.
 * 
 * @autor olivier gäumann, neuchâtel (switzerland)
 * @target JDK 1.3
 * @version 02.09.00 erster Entwurf oli4
 *          14.01.01 convert to singleton oli4
 */
public class GLanguage {
    /**
     * constructor
     */
    private GLanguage() {
    }

    // singleton
    private static String fileName;

    private static UiLanguage language;

    /**
     * create singleton
     */
    public static void createLanguage(String fileName) {
        if (language == null) {
            language = new UiLanguage(fileName);
System.err.println(GPersistance.createPersistance().getString("language"));
            language.setLanguage(GPersistance.createPersistance().getString("language"), "");
        }
    }

    /**
     * set the language and country.
     */
    public static void setLanguage(String lang, String country) {
        language.setLanguage(lang, country);
    }

    /**
     * translates into the current language
     */
    public static String translate(String text) {
        if (text != null) {
            return language.getText(text);
        } else {
            return "";
        }
    }

    public static String getActualLanguage() {
        return language.getActualLanguage();
    }

    public static UiLanguage getUiLanguage() {
        return language;
    }
}
