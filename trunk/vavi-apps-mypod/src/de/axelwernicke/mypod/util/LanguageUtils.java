// LanguageUtils
// $Id: LanguageUtils.java,v 1.3 2003/02/03 19:07:15 axelwernicke Exp $
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

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author  axel wernicke
 */
public class LanguageUtils {
    private static final String[][] iso639_2 = {
        { "Abkhazian", "abk" },
        { "Achinese", "ace" },
        { "Acoli", "ach" },
        { "Adangme", "ada" },
        { "Afar", "aar" },
        { "Afrihili", "afh" },
        { "Afrikaans", "afr" },
        {
            "Afro-Asiatic (Other)",
            "afa"
        },
        { "Akan", "aka" },
        { "Akkadian", "akk" },
        { "Albanian", "alb" },
        { "Aleut", "ale" },
        {
            "Algonquian languages",
            "alg"
        },
        { "Altaic (Other)", "tut" },
        { "Amharic", "amh" },
        { "Apache languages", "apa" },
        { "Arabic", "ara" },
        { "Aramaic", "arc" },
        { "Arapaho", "arp" },
        { "Araucanian", "arn" },
        { "Arawak", "arw" },
        { "Armenian", "arm" },
        { "Artificial (Other)", "art" },
        {
            "Assamese, asmAsturian",
            "ast"
        },
        {
            "Athapascan languages",
            "ath"
        },
        {
            "Australian languages",
            "aus"
        },
        {
            "Austronesian (Other)",
            "map"
        },
        { "Avaric", "ava" },
        { "Avestan", "ave" },
        { "Awadhi", "awa" },
        { "Aymara", "aym" },
        { "Azerbaijani", "aze" },
        { "Bable", "ast" },
        { "Balinese", "ban" },
        { "Baltic (Other)", "bat" },
        { "Baluchi", "bal" },
        { "Bambara", "bam" },
        { "Bamileke languages", "bai" },
        { "Banda", "bad" },
        { "Bantu (Other)", "bnt" },
        { "Basa", "bas" },
        { "Bashkir", "bak" },
        { "Basque", "baq" },
        { "Batak (Indonesia)", "btk" },
        { "Beja", "bej" },
        { "Belarusian", "bel" },
        { "Bemba", "bem" },
        { "Bengali", "ben" },
        { "Berber (Other)", "ber" },
        { "Bhojpuri", "bho" },
        { "Bihari", "bih" },
        { "Bikol", "bik" },
        { "Bini", "bin" },
        { "Bislama", "bis" },
        { "Bokmål, Norwegian", "nob" },
        { "Bosnian", "bos" },
        { "Braj", "bra" },
        { "Breton", "bre" },
        { "Buginese", "bug" },
        { "Bulgarian", "bul" },
        { "Buriat", "bua" },
        { "Burmese", "bur" },
        { "Caddo", "cad" },
        { "Carib", "car" },
        { "Castilian", "spa" },
        { "Catalan", "cat" },
        { "Caucasian (Other)", "cau" },
        { "Cebuano", "ceb" },
        { "Celtic (Other)", "cel" },
        {
            "Central American Indian (Other)",
            "cai"
        },
        { "Chagatai", "chg" },
        { "Chamic languages", "cmc" },
        { "Chamorro", "cha" },
        { "Chechen", "che" },
        { "Cherokee", "chr" },
        { "Chewa", "nya" },
        { "Cheyenne", "chy" },
        { "Chibcha", "chb" },
        { "Chichewa", "nya" },
        { "Chinese", "chi" },
        { "Chinook jargon", "chn" },
        { "Chipewyan", "chp" },
        { "Choctaw", "cho" },
        { "Chuang", "zha" },
        { "Church Slavic", "chu" },
        { "Church Slavonic", "chu" },
        { "Chuukese", "chk" },
        { "Chuvash", "chv" },
        { "Coptic", "cop" },
        { "Cornish", "cor" },
        { "Corsican", "cos" },
        { "Cree", "cre" },
        { "Creek", "mus" },
        {
            "Creoles and pidgins(Other)",
            "crp"
        },
        {
            "Creoles and pidgins, English-based (Other)",
            "cpe"
        },
        {
            "Creoles and pidgins, French-based (Other)",
            "cpf"
        },
        {
            "Creoles and pidgins, Portuguese-based (Other)",
            "cpp"
        },
        { "Croatian", "scr" },
        { "Cushitic (Other)", "cus" },
        { "Czech", "cze" },
        { "Dakota", "dak" },
        { "Danish", "dan" },
        { "Dayak", "day" },
        { "Delaware", "del" },
        { "Dinka", "din" },
        { "Divehi", "div" },
        { "Dogri", "doi" },
        { "Dogrib", "dgr" },
        { "Dravidian (Other)", "dra" },
        { "Duala", "dua" },
        { "Dutch", "dut" },
        {
            "Dutch, Middle (ca. 1050-1350)",
            "dum"
        },
        { "Dyula", "dyu" },
        { "Dzongkha", "dzo" },
        { "Efik", "efi" },
        { "Egyptian (Ancient)", "egy" },
        { "Ekajuk", "eka" },
        { "Elamite", "elx" },
        { "English", "eng" },
        {
            "English, Middle (1100-1500)",
            "enm"
        },
        {
            "English, Old (ca.450-1100)",
            "ang"
        },
        { "Esperanto", "epo" },
        { "Estonian", "est" },
        { "Ewe", "ewe" },
        { "Ewondo", "ewo" },
        { "Fang", "fan" },
        { "Fanti", "fat" },
        { "Faroese", "fao" },
        { "Fijian", "fij" },
        { "Finnish", "fin" },
        {
            "Finno-Ugrian (Other)",
            "fiu"
        },
        { "Fon", "fon" },
        { "French", "fre" },
        {
            "French, Middle (ca.1400-1600)",
            "frm"
        },
        {
            "French, Old (842-ca.1400)",
            "fro"
        },
        { "Frisian", "fry" },
        { "Friulian", "fur" },
        { "Fulah", "ful" },
        { "Ga", "gaa" },
        { "Gaelic", "gla" },
        { "Gallegan", "glg" },
        { "Ganda", "lug" },
        { "Gayo", "gay" },
        { "Gbaya", "gba" },
        { "Geez", "gez" },
        { "Georgian", "geo" },
        { "German", "ger" },
        { "German, Low", "nds" },
        {
            "German, Middle High (ca.1050-1500)",
            "gmh"
        },
        {
            "German, Old High (ca.750-1050)",
            "goh"
        },
        { "Germanic (Other)", "gem" },
        { "Gikuyu", "kik" },
        { "Gilbertese", "gil" },
        { "Gondi", "gon" },
        { "Gorontalo", "gor" },
        { "Gothic", "got" },
        { "Grebo", "grb" },
        {
            "Greek, Ancient (to 1453)",
            "grc"
        },
        {
            "Greek, Modern (1453-)",
            "gre"
        },
        { "Guarani", "grn" },
        { "Gujarati", "guj" },
        { "Gwich´in", "gwi" },
        { "Haida", "hai" },
        { "Hausa", "hau" },
        { "Hawaiian", "haw" },
        { "Hebrew", "heb" },
        { "Herero", "her" },
        { "Hiligaynon", "hil" },
        { "Himachali", "him" },
        { "Hindi", "hin" },
        { "Hiri Motu", "hmo" },
        { "Hittite", "hit" },
        { "Hmong", "hmn" },
        { "Hungarian", "hun" },
        { "Hupa", "hup" },
        { "Iban", "iba" },
        { "Icelandic", "ice" },
        { "Ido", "ido" },
        { "Igbo", "ibo" },
        { "Ijo", "ijo" },
        { "Iloko", "ilo" },
        { "Inari Sami", "smn" },
        { "Indic (Other)", "inc" },
        {
            "Indo-European (Other)",
            "ine"
        },
        { "Indonesian", "ind" },
        { "Interlingua (IALA)", "ina" },
        { "Interlingue", "ile" },
        { "Inuktitut", "iku" },
        { "Inupiaq", "ipk" },
        { "Iranian (Other)", "ira" },
        { "Irish", "gle" },
        {
            "Irish, Middle (900-1200)",
            "mga"
        },
        { "Irish, Old (to 900)", "sga" },
        { "Iroquoian languages", "iro" },
        { "Italian", "ita" },
        { "Japanese", "jpn" },
        { "Javanese", "jav" },
        { "Judeo-Arabic", "jrb" },
        { "Judeo-Persian", "jpr" },
        { "Kabyle", "kab" },
        { "Kachin", "kac" },
        { "Kalaallisut", "kal" },
        { "Kamba", "kam" },
        { "Kannada", "kan" },
        { "Kanuri", "kau" },
        { "Kara-Kalpak", "kaa" },
        { "Karen", "kar" },
        { "Kashmiri", "kas" },
        { "Kawi", "kaw" },
        { "Kazakh", "kaz" },
        { "Khasi", "kha" },
        { "Khmer", "khm" },
        { "Khoisan (Other)", "khi" },
        { "Khotanese", "kho" },
        { "Kikuyu", "kik" },
        { "Kimbundu", "kmb" },
        { "Kinyarwanda", "kin" },
        { "Kirghiz", "kir" },
        { "Komi", "kom" },
        { "Kongo", "kon" },
        { "Konkani", "kok" },
        { "Korean", "kor" },
        { "Kosraean", "kos" },
        { "Kpelle", "kpe" },
        { "Kru", "kro" },
        { "Kuanyama", "kua" },
        { "Kumyk", "kum" },
        { "Kurdish", "kur" },
        { "Kurukh", "kru" },
        { "Kutenai", "kut" },
        { "Kwanyama", "kua" },
        { "Ladino", "lad" },
        { "Lahnda", "lah" },
        { "Lamba", "lam" },
        { "Lao", "lao" },
        { "Latin", "lat" },
        { "Latvian", "lav" },
        { "Letzeburgesch", "ltz" },
        {
            "Lezghian, lezLimburgan, limlimburgish",
            "lim"
        },
        { "Lingala", "lin" },
        { "Lithuanian", "lit" },
        { "Low German", "nds" },
        { "Low Saxon", "nds" },
        { "Lozi", "loz" },
        { "Luba-Katanga", "lub" },
        { "Luba-Lulua", "lua" },
        { "Luiseno", "lui" },
        { "Lule Sami", "smj" },
        { "Lunda", "lun" },
        {
            "Luo (Kenya and Tanzania)",
            "luo"
        },
        { "Luxembourgish", "ltz" },
        { "Lushai", "lus" },
        { "Macedonian", "mac" },
        { "Madurese", "mad" },
        { "Magahi", "mag" },
        { "Maithili", "mai" },
        { "Makasar", "mak" },
        { "Malagasy", "mlg" },
        { "Malay", "may" },
        { "Malayalam", "mal" },
        { "Maltese", "mlt" },
        { "Manchu", "mnc" },
        { "Mandar", "mdr" },
        { "Mandingo", "man" },
        { "Manipuri", "mni" },
        { "Manobo languages", "mno" },
        { "Manx", "glv" },
        { "Maori", "mao" },
        { "Marathi", "mar" },
        { "Mari", "chm" },
        { "Marshallese", "mah" },
        { "Marwari", "mwr" },
        { "Masai", "mas" },
        { "Mayan languages", "myn" },
        { "Mende", "men" },
        { "Micmac", "mic" },
        { "Minangkabau", "min" },
        {
            "Miscellaneous languages",
            "mis"
        },
        { "Mohawk", "moh" },
        { "Moldavian", "mol" },
        { "Mon-Khmer (Other)", "mkh" },
        { "Mongo", "lol" },
        { "Mongolian", "mon" },
        { "Mossi", "mos" },
        { "Multiple languages", "mul" },
        { "Munda languages", "mun" },
        { "Nahuatl", "nah" },
        { "Nauru", "nau" },
        { "Navaho", "nav" },
        { "Navajo", "nav" },
        { "Ndebele, North", "nde" },
        { "Ndebele, South", "nbl" },
        {
            "Ndonga", "ndoNeapolitan",
            "nap"
        },
        { "Nepali", "nep" },
        { "Newari", "new" },
        { "Nias", "nia" },
        {
            "Niger-Kordofanian (Other)",
            "nic"
        },
        {
            "Nilo-Saharan (Other)",
            "ssa"
        },
        { "Niuean", "niu" },
        { "Norse, Old", "non" },
        {
            "North American Indian (Other)",
            "nai"
        },
        { "Northern Sami", "sme" },
        { "North Ndebele", "nde" },
        { "Norwegian", "nor" },
        { "Norwegian Bokmål", "nob" },
        { "Norwegian Nynorsk", "nno" },
        { "Nubian languages", "nub" },
        { "Nyamwezi", "nym" },
        { "Nyanja", "nya" },
        { "Nyankole", "nyn" },
        { "Nynorsk, Norwegian", "nno" },
        { "Nyoro", "nyo" },
        { "Nzima", "nzi" },
        { "Occitan (post 1500)", "oci" },
        { "Ojibwa", "oji" },
        { "Old Bulgarian", "chu" },
        { "Old Church Slavonic", "chu" },
        { "Old Slavonic", "chu" },
        { "Oriya", "ori" },
        { "Oromo", "orm" },
        { "Osage", "osa" },
        { "Ossetian", "oss" },
        { "Ossetic", "oss" },
        { "Otomian languages", "oto" },
        { "Pahlavi", "pal" },
        { "Palauan", "pau" },
        { "Pali", "pli" },
        { "Pampanga", "pam" },
        { "Pangasinan", "pag" },
        { "Panjabi", "pan" },
        { "Papiamento", "pap" },
        { "Papuan (Other)", "paa" },
        { "Persian", "per" },
        {
            "Persian, Old (ca.600-400)",
            "peo"
        },
        { "Philippine (Other)", "phi" },
        { "Phoenician", "phn" },
        { "Pohnpeian", "pon" },
        { "Polish", "pol" },
        { "Portuguese", "por" },
        { "Prakrit languages", "pra" },
        { "Provençal", "oci" },
        {
            "Provençal, Old (to 1500)",
            "pro"
        },
        { "Pushto", "pus" },
        { "Quechua", "que" },
        { "Raeto-Romance", "roh" },
        { "Rajasthani", "raj" },
        { "Rapanui", "rap" },
        { "Rarotongan", "rar" },
        {
            "Reserved for local user",
            "qaa-qtz"
        },
        { "Romance (Other)", "roa" },
        { "Romanian", "rum" },
        { "Romany", "rom" },
        { "Rundi", "run" },
        { "Russian", "rus" },
        { "Salishan languages", "sal" },
        { "Samaritan Aramaic", "sam" },
        {
            "Sami languages (Other)",
            "smi"
        },
        { "Samoan", "smo" },
        { "Sandawe", "sad" },
        { "Sango", "sag" },
        { "Sanskrit", "san" },
        { "Santali", "sat" },
        { "Sardinian", "srd" },
        { "Sasak", "sas" },
        { "Saxon, Low", "nds" },
        { "Scots", "sco" },
        { "Scottish Gaelic", "gla" },
        { "Selkup", "sel" },
        { "Semitic (Other)", "sem" },
        { "Serbian", "scc" },
        { "Serer", "srr" },
        { "Shan", "shn" },
        { "Shona", "sna" },
        { "Sidamo", "sid" },
        { "Sign languages", "sgn" },
        { "Siksika", "bla" },
        { "Sindhi", "snd" },
        { "Sinhalese", "sin" },
        {
            "Sino-Tibetan (Other)",
            "sit"
        },
        { "Siouan languages", "sio" },
        { "Skolt Sami", "sms" },
        { "Slave (Athapascan)", "den" },
        { "Slavic (Other)", "sla" },
        { "Slovak", "slo" },
        { "Slovenian", "slv" },
        { "Sogdian", "sog" },
        { "Somali", "som" },
        { "Songhai", "son" },
        { "Soninke", "snk" },
        { "Sorbian languages", "wen" },
        { "Sotho, Northern", "nso" },
        { "Sotho, Southern", "sot" },
        {
            "South American Indian (Other)",
            "sai"
        },
        { "Southern Sami", "sma" },
        { "South Ndebele", "nbl" },
        { "Spanish", "spa" },
        { "Sukuma", "suk" },
        { "Sumerian", "sux" },
        { "Sundanese", "sun" },
        { "Susu", "sus" },
        { "Swahili", "swa" },
        { "Swati", "ssw" },
        { "Swedish", "swe" },
        { "Syriac", "syr" },
        { "Tagalog", "tgl" },
        { "Tahitian", "tah" },
        { "Tai (Other)", "tai" },
        { "Tajik", "tgk" },
        { "Tamashek", "tmh" },
        { "Tamil", "tam" },
        { "Tatar", "tat" },
        { "Telugu", "tel" },
        { "Tereno", "ter" },
        { "Tetum", "tet" },
        { "Thai", "tha" },
        { "Tibetan", "tib" },
        { "Tigre", "tig" },
        { "Tigrinya", "tir" },
        { "Timne", "tem" },
        { "Tiv", "tiv" },
        { "Tlingit", "tli" },
        { "Tok Pisin", "tpi" },
        { "Tokelau", "tkl" },
        { "Tonga (Nyasa)", "tog" },
        {
            "Tonga (Tonga Islands)",
            "ton"
        },
        { "Tsimshian", "tsi" },
        { "Tsonga", "tso" },
        { "Tswana", "tsn" },
        { "Tumbuka", "tum" },
        { "Tupi languages", "tup" },
        { "Turkish", "tur" },
        {
            "Turkish, Ottoman (1500-1928)",
            "ota"
        },
        { "Turkmen", "tuk" },
        { "Tuvalu", "tvl" },
        { "Tuvinian", "tyv" },
        { "Twi", "twi" },
        { "Ugaritic", "uga" },
        { "Uighur", "uig" },
        { "Ukrainian", "ukr" },
        { "Umbundu", "umb" },
        { "Undetermined", "und" },
        { "Urdu", "urd" },
        { "Uzbek", "uzb" },
        { "Vai", "vai" },
        { "Venda", "ven" },
        { "Vietnamese", "vie" },
        { "Volap?k", "vol" },
        { "Votic", "vot" },
        { "Wakashan languages", "wak" },
        { "Walamo", "wal" },
        { "Walloon", "wln" },
        { "Waray", "war" },
        { "Washo", "was" },
        { "Welsh", "wel" },
        { "Wolof", "wol" },
        { "Xhosa", "xho" },
        { "Yakut", "sah" },
        { "Yao", "yao" },
        { "Yapese", "yap" },
        { "Yiddish", "yid" },
        { "Yoruba", "yor" },
        { "Yupik languages", "ypk" },
        { "Zande", "znd" },
        { "Zapotec", "zap" },
        { "Zenaga", "zen" },
        { "Zhuang", "zha" },
        { "Zulu", "zul" },
        { "Zuni", "zun" }
    };

    /**
     * Gets the language code (iso 639-2) for a language name
     *
     * @param language
     * @return
     */
    public static String getLanguageCode(String language) {
        String langCode = "";

        // finde the language name
        boolean found = false;
        for (int i = 0; (i < iso639_2.length) && !found; i++) {
            if (iso639_2[i][0].equals(language)) {
                found = true;
                langCode = iso639_2[i][1];
            }
        }

        return langCode;
    }

    /**
     * Gets the name of a language from a language code (iso 639-2)
     *
     * @param langCode
     * @return
     */
    public static String getLanguageName(String langCode) {
        String langName = "";

        // finde the language code
        boolean found = false;
        for (int i = 0; (i < iso639_2.length) && !found; i++) {
            if (iso639_2[i][1].equals(langCode)) {
                found = true;
                langName = iso639_2[i][0];
            }
        }

        return langName;
    }

    /**
     * Gets names of all iso 639_2 languages
     */
    public static List<String> getAllLanguageNames() {
        List<String> result = new ArrayList<String>(iso639_2.length);

        for (int i = 0; i < iso639_2.length; i++) {
            result.add(iso639_2[i][0]);
        }

        return result;
    }
}
