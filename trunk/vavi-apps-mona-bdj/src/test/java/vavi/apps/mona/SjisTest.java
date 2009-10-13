/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.mona;

import vavi.util.bdj.Sjis;

import junit.framework.TestCase;


/**
 * SjisTest. 
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 080901 nsano initial version <br>
 */
public class SjisTest extends TestCase {

    /** OK */
    public void test01() throws Exception {
        String a = "abcd‚ ‚¢‚¤‚¦‚¨efg‰^Ž×–‚—…àYhijk";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** XX */
    public void test021() throws Exception {
        String a = "_`a|‘’Ê";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** UNMAPPED */
    public void $test022() throws Exception {
        String a = "‡@‡A‡B‡C‡D‡E‡F‡G‡H‡I‡J‡K‡L‡M‡N‡O‡P‡Q‡R‡S";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** UNMAPPED */
    public void $test023() throws Exception {
       String a = "‡T‡U‡V‡W‡X‡Y‡Z‡[‡\‡]";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** UNMAPPED */
    public void $test024() throws Exception {
        String a = "‡_‡`‡a‡b‡c‡d‡e‡f‡g‡h‡i‡j‡k‡l‡m‡n‡o‡p‡q‡r‡s‡t‡u";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** UNMAPPED */
    public void $test025() throws Exception {
        String a = "‡~‡€‡‡‚‡ƒ‡„‡…‡†‡‡‡ˆ‡‰‡Š‡‹‡Œ‡‡Ž‡";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** XX */
    public void test026() throws Exception {
        String a = "àßç‡“ãÛÚ‡˜‡™æ¿¾";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** OK */
    public void test03() throws Exception {
        String a = "·À„ª(ßÍß)„ª!!";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
        assertEquals(c, a);
    }

    /** XX */
    public void test041() throws Exception {
        String a = "^‡”";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** XX */
    public void test051() throws Exception {
        String a =
            "@ABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ[\]^_" +
            "`abcdefghijklmno" +
            "pqrstuvwxyz{|}~" +
            "€‚ƒ„…†‡ˆ‰Š‹ŒŽ" +
            "‘’“”•–—˜™š›œžŸ" +
            " ¡¢£¤¥¦§¨©ª«¬EEE" +
            "EEEEEEEE¸¹º»¼½¾¿" +
            "EEEEEEEEÈÉÊËÌÍÎE" +
            "EEEEEEEEEEÚÛÜÝÞß" +
            "àáâãäåæçèEEEEEEE" +
            "ðñòóôõö÷EEEEü" +
            
            "EEEEEEEEEEEEEEE‚O" +
            "‚P‚Q‚R‚S‚T‚U‚V‚W‚XEEEEEEE" +
            "‚`‚a‚b‚c‚d‚e‚f‚g‚h‚i‚j‚k‚l‚m‚n‚o" +
            "‚p‚q‚r‚s‚t‚u‚v‚w‚x‚yEEEEE" +
            "E‚‚‚‚ƒ‚„‚…‚†‚‡‚ˆ‚‰‚Š‚‹‚Œ‚‚Ž‚" +
            "‚‚‘‚’‚“‚”‚•‚–‚—‚˜‚™‚šEEEE‚Ÿ" +
            "‚ ‚¡‚¢‚£‚¤‚¥‚¦‚§‚¨‚©‚ª‚«‚¬‚­‚®‚¯" +
            "‚°‚±‚²‚³‚´‚µ‚¶‚·‚¸‚¹‚º‚»‚¼‚½‚¾‚¿" +
            "‚À‚Á‚Â‚Ã‚Ä‚Å‚Æ‚Ç‚È‚É‚Ê‚Ë‚Ì‚Í‚Î‚Ï" +
            "‚Ð‚Ñ‚Ò‚Ó‚Ô‚Õ‚Ö‚×‚Ø‚Ù‚Ú‚Û‚Ü‚Ý‚Þ‚ß" +
            "‚à‚á‚â‚ã‚ä‚å‚æ‚ç‚è‚é‚ê‚ë‚ì‚í‚î‚ï" +
            "‚ð‚ñEEEEEEEEEEE" +
            
            "ƒ@ƒAƒBƒCƒDƒEƒFƒGƒHƒIƒJƒKƒLƒMƒNƒO" +
            "ƒPƒQƒRƒSƒTƒUƒVƒWƒXƒYƒZƒ[ƒ\ƒ]ƒ^ƒ_" +
            "ƒ`ƒaƒbƒcƒdƒeƒfƒgƒhƒiƒjƒkƒlƒmƒnƒo" +
            "ƒpƒqƒrƒsƒtƒuƒvƒwƒxƒyƒzƒ{ƒ|ƒ}ƒ~" +
            "ƒ€ƒƒ‚ƒƒƒ„ƒ…ƒ†ƒ‡ƒˆƒ‰ƒŠƒ‹ƒŒƒƒŽƒ" +
            "ƒƒ‘ƒ’ƒ“ƒ”ƒ•ƒ–ƒŸ" +
            "ƒ ƒ¡ƒ¢ƒ£ƒ¤ƒ¥ƒ¦ƒ§ƒ¨ƒ©ƒªƒ«ƒ¬ƒ­ƒ®ƒ¯" +
            "ƒ°ƒ±ƒ²ƒ³ƒ´ƒµƒ¶EEEEEEEEƒ¿" +
            "ƒÀƒÁƒÂƒÃƒÄƒÅƒÆƒÇƒÈƒÉƒÊƒËƒÌƒÍƒÎƒÏ" +
            "ƒÐƒÑƒÒƒÓƒÔƒÕƒÖEEEEEEEEE" +
            
            
            
            "„@„A„B„C„D„E„F„G„H„I„J„K„L„M„N„O" +
            "„P„Q„R„S„T„U„V„W„X„Y„Z„[„\„]„^„_" +
            "„`EEEEEEEEEEEEEEE" +
            "„p„q„r„s„t„u„v„w„x„y„z„{„|„}„~" +
            "„€„„‚„ƒ„„„…„†„‡„ˆ„‰„Š„‹„Œ„„Ž„" +
            "„„‘EEEEEEEEEEEEE„Ÿ" +
            "„ „¡„¢„£„¤„¥„¦„§„¨„©„ª„«„¬„­„®„¯" +
            "„°„±„²„³„´„µ„¶„·„¸„¹„º„»„¼„½„¾E" +
            "EEEEEEEEEEEEEEEE" +
            "EEEEEEEEEEEEEEEE" +
            "EEEEEEEEEEEEEEEE" +
            "EEEEEEEEEEEEE" +
            
            "‡@‡A‡B‡C‡D‡E‡F‡G‡H‡I‡J‡K‡L‡M‡N‡O" +
            "‡P‡Q‡R‡S‡T‡U‡V‡W‡X‡Y‡Z‡[‡\‡]E‡_" +
            "‡`‡a‡b‡c‡d‡e‡f‡g‡h‡i‡j‡k‡l‡m‡n‡o" +
            "‡p‡q‡r‡s‡t‡uEEEEEEEE‡~" +
            "‡€‡‡‚‡ƒ‡„‡…‡†‡‡‡ˆ‡‰‡Š‡‹‡Œ‡‡Ž‡" +
            "àßç‡“‡”ãÛÚ‡˜‡™æ¿¾";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }
            
    /** OK */
    public void $test052() throws Exception {
        String a =
            "EEEEEEEEEEEEEEEˆŸ" +
            "ˆ ˆ¡ˆ¢ˆ£ˆ¤ˆ¥ˆ¦ˆ§ˆ¨ˆ©ˆªˆ«ˆ¬ˆ­ˆ®ˆ¯" +
            "ˆ°ˆ±ˆ²ˆ³ˆ´ˆµˆ¶ˆ·ˆ¸ˆ¹ˆºˆ»ˆ¼ˆ½ˆ¾ˆ¿" +
            "ˆÀˆÁˆÂˆÃˆÄˆÅˆÆˆÇˆÈˆÉˆÊˆËˆÌˆÍˆÎˆÏ" +
            "ˆÐˆÑˆÒˆÓˆÔˆÕˆÖˆ×ˆØˆÙˆÚˆÛˆÜˆÝˆÞˆß" +
            "ˆàˆáˆâˆãˆäˆåˆæˆçˆèˆéˆêˆëˆìˆíˆîˆï" +
            "ˆðˆñˆòˆóˆôˆõˆöˆ÷ˆøˆùˆúˆûˆü" +
            
            "‰@‰A‰B‰C‰D‰E‰F‰G‰H‰I‰J‰K‰L‰M‰N‰O" +
            "‰P‰Q‰R‰S‰T‰U‰V‰W‰X‰Y‰Z‰[‰\‰]‰^‰_" +
            "‰`‰a‰b‰c‰d‰e‰f‰g‰h‰i‰j‰k‰l‰m‰n‰o" +
            "‰p‰q‰r‰s‰t‰u‰v‰w‰x‰y‰z‰{‰|‰}‰~" +
            "‰€‰‰‚‰ƒ‰„‰…‰†‰‡‰ˆ‰‰‰Š‰‹‰Œ‰‰Ž‰" +
            "‰‰‘‰’‰“‰”‰•‰–‰—‰˜‰™‰š‰›‰œ‰‰ž‰Ÿ" +
            "‰ ‰¡‰¢‰£‰¤‰¥‰¦‰§‰¨‰©‰ª‰«‰¬‰­‰®‰¯" +
            "‰°‰±‰²‰³‰´‰µ‰¶‰·‰¸‰¹‰º‰»‰¼‰½‰¾‰¿" +
            "‰À‰Á‰Â‰Ã‰Ä‰Å‰Æ‰Ç‰È‰É‰Ê‰Ë‰Ì‰Í‰Î‰Ï" +
            "‰Ð‰Ñ‰Ò‰Ó‰Ô‰Õ‰Ö‰×‰Ø‰Ù‰Ú‰Û‰Ü‰Ý‰Þ‰ß" +
            "‰à‰á‰â‰ã‰ä‰å‰æ‰ç‰è‰é‰ê‰ë‰ì‰í‰î‰ï" +
            "‰ð‰ñ‰ò‰ó‰ô‰õ‰ö‰÷‰ø‰ù‰ú‰û‰ü" +
            
            "Š@ŠAŠBŠCŠDŠEŠFŠGŠHŠIŠJŠKŠLŠMŠNŠO" +
            "ŠPŠQŠRŠSŠTŠUŠVŠWŠXŠYŠZŠ[Š\Š]Š^Š_" +
            "Š`ŠaŠbŠcŠdŠeŠfŠgŠhŠiŠjŠkŠlŠmŠnŠo" +
            "ŠpŠqŠrŠsŠtŠuŠvŠwŠxŠyŠzŠ{Š|Š}Š~" +
            "Š€ŠŠ‚ŠƒŠ„Š…Š†Š‡ŠˆŠ‰ŠŠŠ‹ŠŒŠŠŽŠ" +
            "ŠŠ‘Š’Š“Š”Š•Š–Š—Š˜Š™ŠšŠ›ŠœŠŠžŠŸ" +
            "Š Š¡Š¢Š£Š¤Š¥Š¦Š§Š¨Š©ŠªŠ«Š¬Š­Š®Š¯" +
            "Š°Š±Š²Š³Š´ŠµŠ¶Š·Š¸Š¹ŠºŠ»Š¼Š½Š¾Š¿" +
            "ŠÀŠÁŠÂŠÃŠÄŠÅŠÆŠÇŠÈŠÉŠÊŠËŠÌŠÍŠÎŠÏ" +
            "ŠÐŠÑŠÒŠÓŠÔŠÕŠÖŠ×ŠØŠÙŠÚŠÛŠÜŠÝŠÞŠß" +
            "ŠàŠáŠâŠãŠäŠåŠæŠçŠèŠéŠêŠëŠìŠíŠîŠï" +
            "ŠðŠñŠòŠóŠôŠõŠöŠ÷ŠøŠùŠúŠûŠü" +
            
            "‹@‹A‹B‹C‹D‹E‹F‹G‹H‹I‹J‹K‹L‹M‹N‹O" +
            "‹P‹Q‹R‹S‹T‹U‹V‹W‹X‹Y‹Z‹[‹\‹]‹^‹_" +
            "‹`‹a‹b‹c‹d‹e‹f‹g‹h‹i‹j‹k‹l‹m‹n‹o" +
            "‹p‹q‹r‹s‹t‹u‹v‹w‹x‹y‹z‹{‹|‹}‹~" +
            "‹€‹‹‚‹ƒ‹„‹…‹†‹‡‹ˆ‹‰‹Š‹‹‹Œ‹‹Ž‹" +
            "‹‹‘‹’‹“‹”‹•‹–‹—‹˜‹™‹š‹›‹œ‹‹ž‹Ÿ" +
            "‹ ‹¡‹¢‹£‹¤‹¥‹¦‹§‹¨‹©‹ª‹«‹¬‹­‹®‹¯" +
            "‹°‹±‹²‹³‹´‹µ‹¶‹·‹¸‹¹‹º‹»‹¼‹½‹¾‹¿" +
            "‹À‹Á‹Â‹Ã‹Ä‹Å‹Æ‹Ç‹È‹É‹Ê‹Ë‹Ì‹Í‹Î‹Ï" +
            "‹Ð‹Ñ‹Ò‹Ó‹Ô‹Õ‹Ö‹×‹Ø‹Ù‹Ú‹Û‹Ü‹Ý‹Þ‹ß" +
            "‹à‹á‹â‹ã‹ä‹å‹æ‹ç‹è‹é‹ê‹ë‹ì‹í‹î‹ï" +
            "‹ð‹ñ‹ò‹ó‹ô‹õ‹ö‹÷‹ø‹ù‹ú‹û‹ü" +
            
            "Œ@ŒAŒBŒCŒDŒEŒFŒGŒHŒIŒJŒKŒLŒMŒNŒO" +
            "ŒPŒQŒRŒSŒTŒUŒVŒWŒXŒYŒZŒ[Œ\Œ]Œ^Œ_" +
            "Œ`ŒaŒbŒcŒdŒeŒfŒgŒhŒiŒjŒkŒlŒmŒnŒo" +
            "ŒpŒqŒrŒsŒtŒuŒvŒwŒxŒyŒzŒ{Œ|Œ}Œ~" +
            "Œ€ŒŒ‚ŒƒŒ„Œ…Œ†Œ‡ŒˆŒ‰ŒŠŒ‹ŒŒŒŒŽŒ" +
            "ŒŒ‘Œ’Œ“Œ”Œ•Œ–Œ—Œ˜Œ™ŒšŒ›ŒœŒŒžŒŸ" +
            "Œ Œ¡Œ¢Œ£Œ¤Œ¥Œ¦Œ§Œ¨Œ©ŒªŒ«Œ¬Œ­Œ®Œ¯" +
            "Œ°Œ±Œ²Œ³Œ´ŒµŒ¶Œ·Œ¸Œ¹ŒºŒ»Œ¼Œ½Œ¾Œ¿" +
            "ŒÀŒÁŒÂŒÃŒÄŒÅŒÆŒÇŒÈŒÉŒÊŒËŒÌŒÍŒÎŒÏ" +
            "ŒÐŒÑŒÒŒÓŒÔŒÕŒÖŒ×ŒØŒÙŒÚŒÛŒÜŒÝŒÞŒß" +
            "ŒàŒáŒâŒãŒäŒåŒæŒçŒèŒéŒêŒëŒìŒíŒîŒï" +
            "ŒðŒñŒòŒóŒôŒõŒöŒ÷ŒøŒùŒúŒûŒü" +
            
            "@ABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ[\]^_" +
            "`abcdefghijklmno" +
            "pqrstuvwxyz{|}~" +
            "€‚ƒ„…†‡ˆ‰Š‹ŒŽ" +
            "‘’“”•–—˜™š›œžŸ" +
            " ¡¢£¤¥¦§¨©ª«¬­®¯" +
            "°±²³´µ¶·¸¹º»¼½¾¿" +
            "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ" +
            "ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß" +
            "àáâãäåæçèéêëìíîï" +
            "ðñòóôõö÷øùúûü" +
            
            "Ž@ŽAŽBŽCŽDŽEŽFŽGŽHŽIŽJŽKŽLŽMŽNŽO" +
            "ŽPŽQŽRŽSŽTŽUŽVŽWŽXŽYŽZŽ[Ž\Ž]Ž^Ž_" +
            "Ž`ŽaŽbŽcŽdŽeŽfŽgŽhŽiŽjŽkŽlŽmŽnŽo" +
            "ŽpŽqŽrŽsŽtŽuŽvŽwŽxŽyŽzŽ{Ž|Ž}Ž~" +
            "Ž€ŽŽ‚ŽƒŽ„Ž…Ž†Ž‡ŽˆŽ‰ŽŠŽ‹ŽŒŽŽŽŽ" +
            "ŽŽ‘Ž’Ž“Ž”Ž•Ž–Ž—Ž˜Ž™ŽšŽ›ŽœŽŽžŽŸ" +
            "Ž Ž¡Ž¢Ž£Ž¤Ž¥Ž¦Ž§Ž¨Ž©ŽªŽ«Ž¬Ž­Ž®Ž¯" +
            "Ž°Ž±Ž²Ž³Ž´ŽµŽ¶Ž·Ž¸Ž¹ŽºŽ»Ž¼Ž½Ž¾Ž¿" +
            "ŽÀŽÁŽÂŽÃŽÄŽÅŽÆŽÇŽÈŽÉŽÊŽËŽÌŽÍŽÎŽÏ" +
            "ŽÐŽÑŽÒŽÓŽÔŽÕŽÖŽ×ŽØŽÙŽÚŽÛŽÜŽÝŽÞŽß" +
            "ŽàŽáŽâŽãŽäŽåŽæŽçŽèŽéŽêŽëŽìŽíŽîŽï" +
            "ŽðŽñŽòŽóŽôŽõŽöŽ÷ŽøŽùŽúŽûŽü" +
            
            "@ABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ[\]^_" +
            "`abcdefghijklmno" +
            "pqrstuvwxyz{|}~" +
            "€‚ƒ„…†‡ˆ‰Š‹ŒŽ" +
            "‘’“”•–—˜™š›œžŸ" +
            " ¡¢£¤¥¦§¨©ª«¬­®¯" +
            "°±²³´µ¶·¸¹º»¼½¾¿" +
            "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ" +
            "ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß" +
            "àáâãäåæçèéêëìíîï" +
            "ðñòóôõö÷øùúûü" +
            
            "@ABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ[\]^_" +
            "`abcdefghijklmno" +
            "pqrstuvwxyz{|}~" +
            "€‚ƒ„…†‡ˆ‰Š‹ŒŽ" +
            "‘’“”•–—˜™š›œžŸ" +
            " ¡¢£¤¥¦§¨©ª«¬­®¯" +
            "°±²³´µ¶·¸¹º»¼½¾¿" +
            "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ" +
            "ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß" +
            "àáâãäåæçèéêëìíîï" +
            "ðñòóôõö÷øùúûü" +
            
            "‘@‘A‘B‘C‘D‘E‘F‘G‘H‘I‘J‘K‘L‘M‘N‘O" +
            "‘P‘Q‘R‘S‘T‘U‘V‘W‘X‘Y‘Z‘[‘\‘]‘^‘_" +
            "‘`‘a‘b‘c‘d‘e‘f‘g‘h‘i‘j‘k‘l‘m‘n‘o" +
            "‘p‘q‘r‘s‘t‘u‘v‘w‘x‘y‘z‘{‘|‘}‘~" +
            "‘€‘‘‚‘ƒ‘„‘…‘†‘‡‘ˆ‘‰‘Š‘‹‘Œ‘‘Ž‘" +
            "‘‘‘‘’‘“‘”‘•‘–‘—‘˜‘™‘š‘›‘œ‘‘ž‘Ÿ" +
            "‘ ‘¡‘¢‘£‘¤‘¥‘¦‘§‘¨‘©‘ª‘«‘¬‘­‘®‘¯" +
            "‘°‘±‘²‘³‘´‘µ‘¶‘·‘¸‘¹‘º‘»‘¼‘½‘¾‘¿" +
            "‘À‘Á‘Â‘Ã‘Ä‘Å‘Æ‘Ç‘È‘É‘Ê‘Ë‘Ì‘Í‘Î‘Ï" +
            "‘Ð‘Ñ‘Ò‘Ó‘Ô‘Õ‘Ö‘×‘Ø‘Ù‘Ú‘Û‘Ü‘Ý‘Þ‘ß" +
            "‘à‘á‘â‘ã‘ä‘å‘æ‘ç‘è‘é‘ê‘ë‘ì‘í‘î‘ï" +
            "‘ð‘ñ‘ò‘ó‘ô‘õ‘ö‘÷‘ø‘ù‘ú‘û‘ü" +
            
            "’@’A’B’C’D’E’F’G’H’I’J’K’L’M’N’O" +
            "’P’Q’R’S’T’U’V’W’X’Y’Z’[’\’]’^’_" +
            "’`’a’b’c’d’e’f’g’h’i’j’k’l’m’n’o" +
            "’p’q’r’s’t’u’v’w’x’y’z’{’|’}’~" +
            "’€’’‚’ƒ’„’…’†’‡’ˆ’‰’Š’‹’Œ’’Ž’" +
            "’’‘’’’“’”’•’–’—’˜’™’š’›’œ’’ž’Ÿ" +
            "’ ’¡’¢’£’¤’¥’¦’§’¨’©’ª’«’¬’­’®’¯" +
            "’°’±’²’³’´’µ’¶’·’¸’¹’º’»’¼’½’¾’¿" +
            "’À’Á’Â’Ã’Ä’Å’Æ’Ç’È’É’Ê’Ë’Ì’Í’Î’Ï" +
            "’Ð’Ñ’Ò’Ó’Ô’Õ’Ö’×’Ø’Ù’Ú’Û’Ü’Ý’Þ’ß" +
            "’à’á’â’ã’ä’å’æ’ç’è’é’ê’ë’ì’í’î’ï" +
            "’ð’ñ’ò’ó’ô’õ’ö’÷’ø’ù’ú’û’ü" +
            
            "“@“A“B“C“D“E“F“G“H“I“J“K“L“M“N“O" +
            "“P“Q“R“S“T“U“V“W“X“Y“Z“[“\“]“^“_" +
            "“`“a“b“c“d“e“f“g“h“i“j“k“l“m“n“o" +
            "“p“q“r“s“t“u“v“w“x“y“z“{“|“}“~" +
            "“€““‚“ƒ“„“…“†“‡“ˆ“‰“Š“‹“Œ““Ž“" +
            "““‘“’“““”“•“–“—“˜“™“š“›“œ““ž“Ÿ" +
            "“ “¡“¢“£“¤“¥“¦“§“¨“©“ª“«“¬“­“®“¯" +
            "“°“±“²“³“´“µ“¶“·“¸“¹“º“»“¼“½“¾“¿" +
            "“À“Á“Â“Ã“Ä“Å“Æ“Ç“È“É“Ê“Ë“Ì“Í“Î“Ï" +
            "“Ð“Ñ“Ò“Ó“Ô“Õ“Ö“×“Ø“Ù“Ú“Û“Ü“Ý“Þ“ß" +
            "“à“á“â“ã“ä“å“æ“ç“è“é“ê“ë“ì“í“î“ï" +
            "“ð“ñ“ò“ó“ô“õ“ö“÷“ø“ù“ú“û“ü" +
            
            "”@”A”B”C”D”E”F”G”H”I”J”K”L”M”N”O" +
            "”P”Q”R”S”T”U”V”W”X”Y”Z”[”\”]”^”_" +
            "”`”a”b”c”d”e”f”g”h”i”j”k”l”m”n”o" +
            "”p”q”r”s”t”u”v”w”x”y”z”{”|”}”~" +
            "”€””‚”ƒ”„”…”†”‡”ˆ”‰”Š”‹”Œ””Ž”" +
            "””‘”’”“”””•”–”—”˜”™”š”›”œ””ž”Ÿ" +
            "” ”¡”¢”£”¤”¥”¦”§”¨”©”ª”«”¬”­”®”¯" +
            "”°”±”²”³”´”µ”¶”·”¸”¹”º”»”¼”½”¾”¿" +
            "”À”Á”Â”Ã”Ä”Å”Æ”Ç”È”É”Ê”Ë”Ì”Í”Î”Ï" +
            "”Ð”Ñ”Ò”Ó”Ô”Õ”Ö”×”Ø”Ù”Ú”Û”Ü”Ý”Þ”ß" +
            "”à”á”â”ã”ä”å”æ”ç”è”é”ê”ë”ì”í”î”ï" +
            "”ð”ñ”ò”ó”ô”õ”ö”÷”ø”ù”ú”û”ü" +
            
            "•@•A•B•C•D•E•F•G•H•I•J•K•L•M•N•O" +
            "•P•Q•R•S•T•U•V•W•X•Y•Z•[•\•]•^•_" +
            "•`•a•b•c•d•e•f•g•h•i•j•k•l•m•n•o" +
            "•p•q•r•s•t•u•v•w•x•y•z•{•|•}•~" +
            "•€••‚•ƒ•„•…•†•‡•ˆ•‰•Š•‹•Œ••Ž•" +
            "••‘•’•“•”•••–•—•˜•™•š•›•œ••ž•Ÿ" +
            "• •¡•¢•£•¤•¥•¦•§•¨•©•ª•«•¬•­•®•¯" +
            "•°•±•²•³•´•µ•¶•·•¸•¹•º•»•¼•½•¾•¿" +
            "•À•Á•Â•Ã•Ä•Å•Æ•Ç•È•É•Ê•Ë•Ì•Í•Î•Ï" +
            "•Ð•Ñ•Ò•Ó•Ô•Õ•Ö•×•Ø•Ù•Ú•Û•Ü•Ý•Þ•ß" +
            "•à•á•â•ã•ä•å•æ•ç•è•é•ê•ë•ì•í•î•ï" +
            "•ð•ñ•ò•ó•ô•õ•ö•÷•ø•ù•ú•û•ü" +
            
            "–@–A–B–C–D–E–F–G–H–I–J–K–L–M–N–O" +
            "–P–Q–R–S–T–U–V–W–X–Y–Z–[–\–]–^–_" +
            "–`–a–b–c–d–e–f–g–h–i–j–k–l–m–n–o" +
            "–p–q–r–s–t–u–v–w–x–y–z–{–|–}–~" +
            "–€––‚–ƒ–„–…–†–‡–ˆ–‰–Š–‹–Œ––Ž–" +
            "––‘–’–“–”–•–––—–˜–™–š–›–œ––ž–Ÿ" +
            "– –¡–¢–£–¤–¥–¦–§–¨–©–ª–«–¬–­–®–¯" +
            "–°–±–²–³–´–µ–¶–·–¸–¹–º–»–¼–½–¾–¿" +
            "–À–Á–Â–Ã–Ä–Å–Æ–Ç–È–É–Ê–Ë–Ì–Í–Î–Ï" +
            "–Ð–Ñ–Ò–Ó–Ô–Õ–Ö–×–Ø–Ù–Ú–Û–Ü–Ý–Þ–ß" +
            "–à–á–â–ã–ä–å–æ–ç–è–é–ê–ë–ì–í–î–ï" +
            "–ð–ñ–ò–ó–ô–õ–ö–÷–ø–ù–ú–û–ü" +
            
            "—@—A—B—C—D—E—F—G—H—I—J—K—L—M—N—O" +
            "—P—Q—R—S—T—U—V—W—X—Y—Z—[—\—]—^—_" +
            "—`—a—b—c—d—e—f—g—h—i—j—k—l—m—n—o" +
            "—p—q—r—s—t—u—v—w—x—y—z—{—|—}—~" +
            "—€——‚—ƒ—„—…—†—‡—ˆ—‰—Š—‹—Œ——Ž—" +
            "——‘—’—“—”—•—–———˜—™—š—›—œ——ž—Ÿ" +
            "— —¡—¢—£—¤—¥—¦—§—¨—©—ª—«—¬—­—®—¯" +
            "—°—±—²—³—´—µ—¶—·—¸—¹—º—»—¼—½—¾—¿" +
            "—À—Á—Â—Ã—Ä—Å—Æ—Ç—È—É—Ê—Ë—Ì—Í—Î—Ï" +
            "—Ð—Ñ—Ò—Ó—Ô—Õ—Ö—×—Ø—Ù—Ú—Û—Ü—Ý—Þ—ß" +
            "—à—á—â—ã—ä—å—æ—ç—è—é—ê—ë—ì—í—î—ï" +
            "—ð—ñ—ò—ó—ô—õ—ö—÷—ø—ù—ú—û—ü" +
            
            "˜@˜A˜B˜C˜D˜E˜F˜G˜H˜I˜J˜K˜L˜M˜N˜O" +
            "˜P˜Q˜R˜S˜T˜U˜V˜W˜X˜Y˜Z˜[˜\˜]˜^˜_" +
            "˜`˜a˜b˜c˜d˜e˜f˜g˜h˜i˜j˜k˜l˜m˜n˜o" +
            "˜p˜q˜rEEEEEEEEEEEE" +
            "EEEEEEEEEEEEEEEE" +
            "EEEEEEEEEEEEEEE˜Ÿ" +
            "˜ ˜¡˜¢˜£˜¤˜¥˜¦˜§˜¨˜©˜ª˜«˜¬˜­˜®˜¯" +
            "˜°˜±˜²˜³˜´˜µ˜¶˜·˜¸˜¹˜º˜»˜¼˜½˜¾˜¿" +
            "˜À˜Á˜Â˜Ã˜Ä˜Å˜Æ˜Ç˜È˜É˜Ê˜Ë˜Ì˜Í˜Î˜Ï" +
            "˜Ð˜Ñ˜Ò˜Ó˜Ô˜Õ˜Ö˜×˜Ø˜Ù˜Ú˜Û˜Ü˜Ý˜Þ˜ß" +
            "˜à˜á˜â˜ã˜ä˜å˜æ˜ç˜è˜é˜ê˜ë˜ì˜í˜î˜ï" +
            "˜ð˜ñ˜ò˜ó˜ô˜õ˜ö˜÷˜ø˜ù˜ú˜û˜ü";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }
            
    /** OK */
    public void $test053() throws Exception {
        String a =
            "™@™A™B™C™D™E™F™G™H™I™J™K™L™M™N™O" +
            "™P™Q™R™S™T™U™V™W™X™Y™Z™[™\™]™^™_" +
            "™`™a™b™c™d™e™f™g™h™i™j™k™l™m™n™o" +
            "™p™q™r™s™t™u™v™w™x™y™z™{™|™}™~" +
            "™€™™‚™ƒ™„™…™†™‡™ˆ™‰™Š™‹™Œ™™Ž™" +
            "™™‘™’™“™”™•™–™—™˜™™™š™›™œ™™ž™Ÿ" +
            "™ ™¡™¢™£™¤™¥™¦™§™¨™©™ª™«™¬™­™®™¯" +
            "™°™±™²™³™´™µ™¶™·™¸™¹™º™»™¼™½™¾™¿" +
            "™À™Á™Â™Ã™Ä™Å™Æ™Ç™È™É™Ê™Ë™Ì™Í™Î™Ï" +
            "™Ð™Ñ™Ò™Ó™Ô™Õ™Ö™×™Ø™Ù™Ú™Û™Ü™Ý™Þ™ß" +
            "™à™á™â™ã™ä™å™æ™ç™è™é™ê™ë™ì™í™î™ï" +
            "™ð™ñ™ò™ó™ô™õ™ö™÷™ø™ù™ú™û™ü" +
            
            "š@šAšBšCšDšEšFšGšHšIšJšKšLšMšNšO" +
            "šPšQšRšSšTšUšVšWšXšYšZš[š\š]š^š_" +
            "š`šašbšcšdšešfšgšhšišjškšlšmšnšo" +
            "špšqšršsštšušvšwšxšyšzš{š|š}š~" +
            "š€šš‚šƒš„š…š†š‡šˆš‰šŠš‹šŒššŽš" +
            "šš‘š’š“š”š•š–š—š˜š™ššš›šœššžšŸ" +
            "š š¡š¢š£š¤š¥š¦š§š¨š©šªš«š¬š­š®š¯" +
            "š°š±š²š³š´šµš¶š·š¸š¹šºš»š¼š½š¾š¿" +
            "šÀšÁšÂšÃšÄšÅšÆšÇšÈšÉšÊšËšÌšÍšÎšÏ" +
            "šÐšÑšÒšÓšÔšÕšÖš×šØšÙšÚšÛšÜšÝšÞšß" +
            "šàšášâšãšäšåšæšçšèšéšêšëšìšíšîšï" +
            "šðšñšòšóšôšõšöš÷šøšùšúšûšü" +
            
            "›@›A›B›C›D›E›F›G›H›I›J›K›L›M›N›O" +
            "›P›Q›R›S›T›U›V›W›X›Y›Z›[›\›]›^›_" +
            "›`›a›b›c›d›e›f›g›h›i›j›k›l›m›n›o" +
            "›p›q›r›s›t›u›v›w›x›y›z›{›|›}›~" +
            "›€››‚›ƒ›„›…›†›‡›ˆ›‰›Š›‹›Œ››Ž›" +
            "››‘›’›“›”›•›–›—›˜›™›š›››œ››ž›Ÿ" +
            "› ›¡›¢›£›¤›¥›¦›§›¨›©›ª›«›¬›­›®›¯" +
            "›°›±›²›³›´›µ›¶›·›¸›¹›º›»›¼›½›¾›¿" +
            "›À›Á›Â›Ã›Ä›Å›Æ›Ç›È›É›Ê›Ë›Ì›Í›Î›Ï" +
            "›Ð›Ñ›Ò›Ó›Ô›Õ›Ö›×›Ø›Ù›Ú›Û›Ü›Ý›Þ›ß" +
            "›à›á›â›ã›ä›å›æ›ç›è›é›ê›ë›ì›í›î›ï" +
            "›ð›ñ›ò›ó›ô›õ›ö›÷›ø›ù›ú›û›ü" +
            
            "œ@œAœBœCœDœEœFœGœHœIœJœKœLœMœNœO" +
            "œPœQœRœSœTœUœVœWœXœYœZœ[œ\œ]œ^œ_" +
            "œ`œaœbœcœdœeœfœgœhœiœjœkœlœmœnœo" +
            "œpœqœrœsœtœuœvœwœxœyœzœ{œ|œ}œ~" +
            "œ€œœ‚œƒœ„œ…œ†œ‡œˆœ‰œŠœ‹œŒœœŽœ" +
            "œœ‘œ’œ“œ”œ•œ–œ—œ˜œ™œšœ›œœœœžœŸ" +
            "œ œ¡œ¢œ£œ¤œ¥œ¦œ§œ¨œ©œªœ«œ¬œ­œ®œ¯" +
            "œ°œ±œ²œ³œ´œµœ¶œ·œ¸œ¹œºœ»œ¼œ½œ¾œ¿" +
            "œÀœÁœÂœÃœÄœÅœÆœÇœÈœÉœÊœËœÌœÍœÎœÏ" +
            "œÐœÑœÒœÓœÔœÕœÖœ×œØœÙœÚœÛœÜœÝœÞœß" +
            "œàœáœâœãœäœåœæœçœèœéœêœëœìœíœîœï" +
            "œðœñœòœóœôœõœöœ÷œøœùœúœûœü" +
            
            "@ABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ[\]^_" +
            "`abcdefghijklmno" +
            "pqrstuvwxyz{|}~" +
            "€‚ƒ„…†‡ˆ‰Š‹ŒŽ" +
            "‘’“”•–—˜™š›œžŸ" +
            " ¡¢£¤¥¦§¨©ª«¬­®¯" +
            "°±²³´µ¶·¸¹º»¼½¾¿" +
            "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ" +
            "ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß" +
            "àáâãäåæçèéêëìíîï" +
            "ðñòóôõö÷øùúûü" +
            
            "ž@žAžBžCžDžEžFžGžHžIžJžKžLžMžNžO" +
            "žPžQžRžSžTžUžVžWžXžYžZž[ž\ž]ž^ž_" +
            "ž`žažbžcždžežfžgžhžižjžkžlžmžnžo" +
            "žpžqžržsžtžužvžwžxžyžzž{ž|ž}ž~" +
            "ž€žž‚žƒž„ž…ž†ž‡žˆž‰žŠž‹žŒžžŽž" +
            "žž‘ž’ž“ž”ž•ž–ž—ž˜ž™žšž›žœžžžžŸ" +
            "ž ž¡ž¢ž£ž¤ž¥ž¦ž§ž¨ž©žªž«ž¬ž­ž®ž¯" +
            "ž°ž±ž²ž³ž´žµž¶ž·ž¸ž¹žºž»ž¼ž½ž¾ž¿" +
            "žÀžÁžÂžÃžÄžÅžÆžÇžÈžÉžÊžËžÌžÍžÎžÏ" +
            "žÐžÑžÒžÓžÔžÕžÖž×žØžÙžÚžÛžÜžÝžÞžß" +
            "žàžážâžãžäžåžæžçžèžéžêžëžìžížîžï" +
            "žðžñžòžóžôžõžöž÷žøžùžúžûžü" +
            
            "Ÿ@ŸAŸBŸCŸDŸEŸFŸGŸHŸIŸJŸKŸLŸMŸNŸO" +
            "ŸPŸQŸRŸSŸTŸUŸVŸWŸXŸYŸZŸ[Ÿ\Ÿ]Ÿ^Ÿ_" +
            "Ÿ`ŸaŸbŸcŸdŸeŸfŸgŸhŸiŸjŸkŸlŸmŸnŸo" +
            "ŸpŸqŸrŸsŸtŸuŸvŸwŸxŸyŸzŸ{Ÿ|Ÿ}Ÿ~" +
            "Ÿ€ŸŸ‚ŸƒŸ„Ÿ…Ÿ†Ÿ‡ŸˆŸ‰ŸŠŸ‹ŸŒŸŸŽŸ" +
            "ŸŸ‘Ÿ’Ÿ“Ÿ”Ÿ•Ÿ–Ÿ—Ÿ˜Ÿ™ŸšŸ›ŸœŸŸžŸŸ" +
            "Ÿ Ÿ¡Ÿ¢Ÿ£Ÿ¤Ÿ¥Ÿ¦Ÿ§Ÿ¨Ÿ©ŸªŸ«Ÿ¬Ÿ­Ÿ®Ÿ¯" +
            "Ÿ°Ÿ±Ÿ²Ÿ³Ÿ´ŸµŸ¶Ÿ·Ÿ¸Ÿ¹ŸºŸ»Ÿ¼Ÿ½Ÿ¾Ÿ¿" +
            "ŸÀŸÁŸÂŸÃŸÄŸÅŸÆŸÇŸÈŸÉŸÊŸËŸÌŸÍŸÎŸÏ" +
            "ŸÐŸÑŸÒŸÓŸÔŸÕŸÖŸ×ŸØŸÙŸÚŸÛŸÜŸÝŸÞŸß" +
            "ŸàŸáŸâŸãŸäŸåŸæŸçŸèŸéŸêŸëŸìŸíŸîŸï" +
            "ŸðŸñŸòŸóŸôŸõŸöŸ÷ŸøŸùŸúŸûŸü" +
            
            "à@àAàBàCàDàEàFàGàHàIàJàKàLàMàNàO" +
            "àPàQàRàSàTàUàVàWàXàYàZà[à\à]à^à_" +
            "à`àaàbàcàdàeàfàgàhàiàjàkàlàmànào" +
            "àpàqàràsàtàuàvàwàxàyàzà{à|à}à~" +
            "à€àà‚àƒà„à…à†à‡àˆà‰àŠà‹àŒààŽà" +
            "àà‘à’à“à”à•à–à—à˜à™àšà›àœààžàŸ" +
            "à à¡à¢à£à¤à¥à¦à§à¨à©àªà«à¬à­à®à¯" +
            "à°à±à²à³à´àµà¶à·à¸à¹àºà»à¼à½à¾à¿" +
            "àÀàÁàÂàÃàÄàÅàÆàÇàÈàÉàÊàËàÌàÍàÎàÏ" +
            "àÐàÑàÒàÓàÔàÕàÖà×àØàÙàÚàÛàÜàÝàÞàß" +
            "àààáàâàãàäàåàæàçàèàéàêàëàìàíàîàï" +
            "àðàñàòàóàôàõàöà÷àøàùàúàûàü" +
            
            "á@áAáBáCáDáEáFáGáHáIáJáKáLáMáNáO" +
            "áPáQáRáSáTáUáVáWáXáYáZá[á\á]á^á_" +
            "á`áaábácádáeáfágáháiájákálámánáo" +
            "ápáqárásátáuáváwáxáyázá{á|á}á~" +
            "á€áá‚áƒá„á…á†á‡áˆá‰áŠá‹áŒááŽá" +
            "áá‘á’á“á”á•á–á—á˜á™ášá›áœáážáŸ" +
            "á á¡á¢á£á¤á¥á¦á§á¨á©áªá«á¬á­á®á¯" +
            "á°á±á²á³á´áµá¶á·á¸á¹áºá»á¼á½á¾á¿" +
            "áÀáÁáÂáÃáÄáÅáÆáÇáÈáÉáÊáËáÌáÍáÎáÏ" +
            "áÐáÑáÒáÓáÔáÕáÖá×áØáÙáÚáÛáÜáÝáÞáß" +
            "áàáááâáãáäáåáæáçáèáéáêáëáìáíáîáï" +
            "áðáñáòáóáôáõáöá÷áøáùáúáûáü" +
            
            "â@âAâBâCâDâEâFâGâHâIâJâKâLâMâNâO" +
            "âPâQâRâSâTâUâVâWâXâYâZâ[â\â]â^â_" +
            "â`âaâbâcâdâeâfâgâhâiâjâkâlâmânâo" +
            "âpâqârâsâtâuâvâwâxâyâzâ{â|â}â~" +
            "â€ââ‚âƒâ„â…â†â‡âˆâ‰âŠâ‹âŒââŽâ" +
            "ââ‘â’â“â”â•â–â—â˜â™âšâ›âœââžâŸ" +
            "â â¡â¢â£â¤â¥â¦â§â¨â©âªâ«â¬â­â®â¯" +
            "â°â±â²â³â´âµâ¶â·â¸â¹âºâ»â¼â½â¾â¿" +
            "âÀâÁâÂâÃâÄâÅâÆâÇâÈâÉâÊâËâÌâÍâÎâÏ" +
            "âÐâÑâÒâÓâÔâÕâÖâ×âØâÙâÚâÛâÜâÝâÞâß" +
            "âàâáâââãâäâåâæâçâèâéâêâëâìâíâîâï" +
            "âðâñâòâóâôâõâöâ÷âøâùâúâûâü" +
            
            "ã@ãAãBãCãDãEãFãGãHãIãJãKãLãMãNãO" +
            "ãPãQãRãSãTãUãVãWãXãYãZã[ã\ã]ã^ã_" +
            "ã`ãaãbãcãdãeãfãgãhãiãjãkãlãmãnão" +
            "ãpãqãrãsãtãuãvãwãxãyãzã{ã|ã}ã~" +
            "ã€ãã‚ãƒã„ã…ã†ã‡ãˆã‰ãŠã‹ãŒããŽã" +
            "ãã‘ã’ã“ã”ã•ã–ã—ã˜ã™ãšã›ãœããžãŸ" +
            "ã ã¡ã¢ã£ã¤ã¥ã¦ã§ã¨ã©ãªã«ã¬ã­ã®ã¯" +
            "ã°ã±ã²ã³ã´ãµã¶ã·ã¸ã¹ãºã»ã¼ã½ã¾ã¿" +
            "ãÀãÁãÂãÃãÄãÅãÆãÇãÈãÉãÊãËãÌãÍãÎãÏ" +
            "ãÐãÑãÒãÓãÔãÕãÖã×ãØãÙãÚãÛãÜãÝãÞãß" +
            "ãàãáãâãããäãåãæãçãèãéãêãëãìãíãîãï" +
            "ãðãñãòãóãôãõãöã÷ãøãùãúãûãü" +
            
            "ä@äAäBäCäDäEäFäGäHäIäJäKäLäMäNäO" +
            "äPäQäRäSäTäUäVäWäXäYäZä[ä\ä]ä^ä_" +
            "ä`äaäbäcädäeäfägähäiäjäkälämänäo" +
            "äpäqäräsätäuäväwäxäyäzä{ä|ä}ä~" +
            "ä€ää‚äƒä„ä…ä†ä‡äˆä‰äŠä‹äŒääŽä" +
            "ää‘ä’ä“ä”ä•ä–ä—ä˜ä™äšä›äœääžäŸ" +
            "ä ä¡ä¢ä£ä¤ä¥ä¦ä§ä¨ä©äªä«ä¬ä­ä®ä¯" +
            "ä°ä±ä²ä³ä´äµä¶ä·ä¸ä¹äºä»ä¼ä½ä¾ä¿" +
            "äÀäÁäÂäÃäÄäÅäÆäÇäÈäÉäÊäËäÌäÍäÎäÏ" +
            "äÐäÑäÒäÓäÔäÕäÖä×äØäÙäÚäÛäÜäÝäÞäß" +
            "äàäáäâäãäääåäæäçäèäéäêäëäìäíäîäï" +
            "äðäñäòäóäôäõäöä÷äøäùäúäûäü" +
            
            "å@åAåBåCåDåEåFåGåHåIåJåKåLåMåNåO" +
            "åPåQåRåSåTåUåVåWåXåYåZå[å\å]å^å_" +
            "å`åaåbåcådåeåfågåhåiåjåkålåmånåo" +
            "åpåqåråsåtåuåvåwåxåyåzå{å|å}å~" +
            "å€åå‚åƒå„å…å†å‡åˆå‰åŠå‹åŒååŽå" +
            "åå‘å’å“å”å•å–å—å˜å™åšå›åœååžåŸ" +
            "å å¡å¢å£å¤å¥å¦å§å¨å©åªå«å¬å­å®å¯" +
            "å°å±å²å³å´åµå¶å·å¸å¹åºå»å¼å½å¾å¿" +
            "åÀåÁåÂåÃåÄåÅåÆåÇåÈåÉåÊåËåÌåÍåÎåÏ" +
            "åÐåÑåÒåÓåÔåÕåÖå×åØåÙåÚåÛåÜåÝåÞåß" +
            "åàåáåâåãåäåååæåçåèåéåêåëåìåíåîåï" +
            "åðåñåòåóåôåõåöå÷åøåùåúåûåü" +
            
            "æ@æAæBæCæDæEæFæGæHæIæJæKæLæMæNæO" +
            "æPæQæRæSæTæUæVæWæXæYæZæ[æ\æ]æ^æ_" +
            "æ`æaæbæcædæeæfægæhæiæjækælæmænæo" +
            "æpæqæræsætæuævæwæxæyæzæ{æ|æ}æ~" +
            "æ€ææ‚æƒæ„æ…æ†æ‡æˆæ‰æŠæ‹æŒææŽæ" +
            "ææ‘æ’æ“æ”æ•æ–æ—æ˜æ™æšæ›æœææžæŸ" +
            "æ æ¡æ¢æ£æ¤æ¥æ¦æ§æ¨æ©æªæ«æ¬æ­æ®æ¯" +
            "æ°æ±æ²æ³æ´æµæ¶æ·æ¸æ¹æºæ»æ¼æ½æ¾æ¿" +
            "æÀæÁæÂæÃæÄæÅæÆæÇæÈæÉæÊæËæÌæÍæÎæÏ" +
            "æÐæÑæÒæÓæÔæÕæÖæ×æØæÙæÚæÛæÜæÝæÞæß" +
            "æàæáæâæãæäæåæææçæèæéæêæëæìæíæîæï" +
            "æðæñæòæóæôæõæöæ÷æøæùæúæûæü" +
            
            "ç@çAçBçCçDçEçFçGçHçIçJçKçLçMçNçO" +
            "çPçQçRçSçTçUçVçWçXçYçZç[ç\ç]ç^ç_" +
            "ç`çaçbçcçdçeçfçgçhçiçjçkçlçmçnço" +
            "çpçqçrçsçtçuçvçwçxçyçzç{ç|ç}ç~" +
            "ç€çç‚çƒç„ç…ç†ç‡çˆç‰çŠç‹çŒççŽç" +
            "çç‘ç’ç“ç”ç•ç–ç—ç˜ç™çšç›çœççžçŸ" +
            "ç ç¡ç¢ç£ç¤ç¥ç¦ç§ç¨ç©çªç«ç¬ç­ç®ç¯" +
            "ç°ç±ç²ç³ç´çµç¶ç·ç¸ç¹çºç»ç¼ç½ç¾ç¿" +
            "çÀçÁçÂçÃçÄçÅçÆçÇçÈçÉçÊçËçÌçÍçÎçÏ" +
            "çÐçÑçÒçÓçÔçÕçÖç×çØçÙçÚçÛçÜçÝçÞçß" +
            "çàçáçâçãçäçåçæçççèçéçêçëçìçíçîçï" +
            "çðçñçòçóçôçõçöç÷çøçùçúçûçü" +
            
            "è@èAèBèCèDèEèFèGèHèIèJèKèLèMèNèO" +
            "èPèQèRèSèTèUèVèWèXèYèZè[è\è]è^è_" +
            "è`èaèbècèdèeèfègèhèièjèkèlèmènèo" +
            "èpèqèrèsètèuèvèwèxèyèzè{è|è}è~" +
            "è€èè‚èƒè„è…è†è‡èˆè‰èŠè‹èŒèèŽè" +
            "èè‘è’è“è”è•è–è—è˜è™èšè›èœèèžèŸ" +
            "è è¡è¢è£è¤è¥è¦è§è¨è©èªè«è¬è­è®è¯" +
            "è°è±è²è³è´èµè¶è·è¸è¹èºè»è¼è½è¾è¿" +
            "èÀèÁèÂèÃèÄèÅèÆèÇèÈèÉèÊèËèÌèÍèÎèÏ" +
            "èÐèÑèÒèÓèÔèÕèÖè×èØèÙèÚèÛèÜèÝèÞèß" +
            "èàèáèâèãèäèåèæèçèèèéèêèëèìèíèîèï" +
            "èðèñèòèóèôèõèöè÷èøèùèúèûèü" +
            
            "é@éAéBéCéDéEéFéGéHéIéJéKéLéMéNéO" +
            "éPéQéRéSéTéUéVéWéXéYéZé[é\é]é^é_" +
            "é`éaébécédéeéfégéhéiéjékéléménéo" +
            "épéqérésétéuévéwéxéyézé{é|é}é~" +
            "é€éé‚éƒé„é…é†é‡éˆé‰éŠé‹éŒééŽé" +
            "éé‘é’é“é”é•é–é—é˜é™éšé›éœééžéŸ" +
            "é é¡é¢é£é¤é¥é¦é§é¨é©éªé«é¬é­é®é¯" +
            "é°é±é²é³é´éµé¶é·é¸é¹éºé»é¼é½é¾é¿" +
            "éÀéÁéÂéÃéÄéÅéÆéÇéÈéÉéÊéËéÌéÍéÎéÏ" +
            "éÐéÑéÒéÓéÔéÕéÖé×éØéÙéÚéÛéÜéÝéÞéß" +
            "éàéáéâéãéäéåéæéçéèéééêéëéìéíéîéï" +
            "éðéñéòéóéôéõéöé÷éøéùéúéûéü" +
            
            "ê@êAêBêCêDêEêFêGêHêIêJêKêLêMêNêO" +
            "êPêQêRêSêTêUêVêWêXêYêZê[ê\ê]ê^ê_" +
            "ê`êaêbêcêdêeêfêgêhêiêjêkêlêmênêo" +
            "êpêqêrêsêtêuêvêwêxêyêzê{ê|ê}ê~" +
            "ê€êê‚êƒê„ê…ê†ê‡êˆê‰êŠê‹êŒêêŽê" +
            "êê‘ê’ê“ê”ê•ê–ê—ê˜ê™êšê›êœêêžêŸ" +
            "ê ê¡ê¢ê£ê¤EEEEEEEEEEE";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }
            
    /** UNMAPPED */
    public void $test054() throws Exception {
        String a =
            "ú\ú]ú^ú_ú`úaúbúcúdúeúfúgúhúiújúk" +
            "úlúmúnúoúpúqúrúsútúuúvúwúxúyúzú{" +
            "ú|ú}ú~ú€úú‚úƒú„ú…ú†ú‡úˆú‰úŠú‹úŒ" +
            "úúŽúúú‘ú’ú“ú”ú•ú–ú—ú˜ú™úšú›" +
            "úœúúžúŸú ú¡ú¢ú£ú¤ú¥ú¦ú§ú¨ú©úªú«" +
            "ú¬ú­ú®ú¯ú°ú±ú²ú³ú´úµú¶ú·ú¸ú¹úºú»" +
            "ú¼ú½ú¾ú¿úÀúÁúÂúÃúÄúÅúÆúÇúÈúÉúÊúË" +
            "úÌúÍúÎúÏúÐúÑúÒúÓúÔúÕúÖú×úØúÙúÚúÛ" +
            "úÜúÝúÞúßúàúáúâúãúäúåúæúçúèúéúêúë" +
            "úìúíúîúïúðúñúòúóúôúõúöú÷úøúùúúúû" +
            "úüû@ûAûBûCûDûEûFûGûHûIûJûKûLûMûN" +
            "ûOûPûQûRûSûTûUûVûWûXûYûZû[" +
            
            "û\û]û^û_û`ûaûbûcûdûeûfûgûhûiûjûk" +
            "ûlûmûnûoûpûqûrûsûtûuûvûwûxûyûzû{" +
            "û|û}û~û€ûû‚ûƒû„û…û†û‡ûˆû‰ûŠû‹ûŒ" +
            "ûûŽûûû‘û’û“û”û•û–û—û˜û™ûšû›" +
            "ûœûûžûŸû û¡û¢û£û¤û¥û¦û§û¨û©ûªû«" +
            "û¬û­û®û¯û°û±û²û³û´ûµû¶û·û¸û¹ûºû»" +
            "û¼û½û¾û¿ûÀûÁûÂûÃûÄûÅûÆûÇûÈûÉûÊûË" +
            "ûÌûÍûÎûÏûÐûÑûÒûÓûÔûÕûÖû×ûØûÙûÚûÛ" +
            "ûÜûÝûÞûßûàûáûâûãûäûåûæûçûèûéûêûë" +
            "ûìûíûîûïûðûñûòûóûôûõûöû÷ûøûùûúûû" +
            "ûüü@üAüBüCüDüEüFüGüHüIüJüKEEú@" +
            "úAúBúCúDúEúFúGúHúIÊúUúVúW";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** UNMAPPED */
    public void $test061() throws Exception {
        String a =
            "ú\ú]ú^ú_ú`úaúbúcúdúeúfúgúhúiújúk" +
            "úlúmúnúoúpúqúrúsútúuúvúwúxúyúzú{" +
            "ú|ú}ú~ú€úú‚úƒú„ú…ú†ú‡úˆú‰úŠú‹úŒ" +
            "úúŽúúú‘ú’ú“ú”ú•ú–ú—ú˜ú™úšú›" +
            "úœúúžúŸú ú¡ú¢ú£ú¤ú¥ú¦ú§ú¨ú©úªú«" +
            "ú¬ú­ú®ú¯ú°ú±ú²ú³ú´úµú¶ú·ú¸ú¹úºú»" +
            "ú¼ú½ú¾ú¿úÀúÁúÂúÃúÄúÅúÆúÇúÈúÉúÊúË" +
            "úÌúÍúÎúÏúÐúÑúÒúÓúÔúÕúÖú×úØúÙúÚúÛ" +
            "úÜúÝúÞúßúàúáúâúãúäúåúæúçúèúéúêúë" +
            "úìúíúîúïúðúñúòúóúôúõúöú÷úøúùúúúû" +
            "úüû@ûAûBûCûDûEûFûGûHûIûJûKûLûMûN" +
            "ûOûPûQûRûSûTûUûVûWûXûYûZû[";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }

    /** UNMAPPED */
    public void $test062() throws Exception {
        String a =
            "û\û]û^û_û`ûaûbûcûdûeûfûgûhûiûjûk" +
            "ûlûmûnûoûpûqûrûsûtûuûvûwûxûyûzû{" +
            "û|û}û~û€ûû‚ûƒû„û…û†û‡ûˆû‰ûŠû‹ûŒ" +
            "ûûŽûûû‘û’û“û”û•û–û—û˜û™ûšû›" +
            "ûœûûžûŸû û¡û¢û£û¤û¥û¦û§û¨û©ûªû«" +
            "û¬û­û®û¯û°û±û²û³û´ûµû¶û·û¸û¹ûºû»" +
            "û¼û½û¾û¿ûÀûÁûÂûÃûÄûÅûÆûÇûÈûÉûÊûË" +
            "ûÌûÍûÎûÏûÐûÑûÒûÓûÔûÕûÖû×ûØûÙûÚûÛ" +
            "ûÜûÝûÞûßûàûáûâûãûäûåûæûçûèûéûêûë" +
            "ûìûíûîûïûðûñûòûóûôûõûöû÷ûøûùûúûû" +
            "ûüü@üAüBüCüDüEüFüGüHüIüJüKEEú@" +
            "úAúBúCúDúEúFúGúHúIÊúUúVúW";
        byte[] b = a.getBytes("Shift_JIS");
        String c = Sjis.toUnicode(b);
System.err.println(a);
System.err.println(c);
        assertEquals(c, a);
    }
}

/* */
