/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package mlp.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * @author rob
 */
public class StringUtils {

    /**
     * map of alphabetic chars to their unicode superscript codepoints
     */
    private final static Map<String, String> superscripts = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                {
                    // 0-9
                    put("0", "⁰");
                    put("1", "¹");
                    put("2", "²");
                    put("3", "³");
                    put("4", "⁴");
                    put("5", "⁵");
                    put("6", "⁶");
                    put("7", "⁷");
                    put("8", "⁸");
                    put("9", "⁹");
                    // a-z (except q)
                    put("a", "ᵃ");
                    put("b", "ᵇ");
                    put("c", "ᶜ");
                    put("d", "ᵈ");
                    put("e", "ᵉ");
                    put("f", "ᶠ");
                    put("g", "ᵍ");
                    put("h", "ʰ");
                    put("i", "ⁱ");
                    put("j", "ʲ");
                    put("k", "ᵏ");
                    put("l", "ˡ");
                    put("m", "ᵐ");
                    put("n", "ⁿ");
                    put("o", "ᵒ");
                    put("p", "ᵖ");
                    put("r", "ʳ");
                    put("s", "ˢ");
                    put("t", "ᵗ");
                    put("u", "ᵘ");
                    put("v", "ᵛ");
                    put("w", "ʷ");
                    put("x", "ˣ");
                    put("y", "ʸ");
                    put("z", "ᶻ");
                    // most uppercases
                    put("A", "ᴬ");
                    put("b", "ᴮ");
                    put("D", "ᴰ");
                    put("E", "ᴱ");
                    put("G", "ᴳ");
                    put("H", "ᴴ");
                    put("I", "ᴵ");
                    put("J", "ᴶ");
                    put("K", "ᴷ");
                    put("L", "ᴸ");
                    put("M", "ᴹ");
                    put("N", "ᴺ");
                    put("O", "ᴼ");
                    put("P", "ᴾ");
                    put("R", "ᴿ");
                    put("T", "ᵀ");
                    put("U", "ᵁ");
                    put("V", "ⱽ");
                    put("W", "ᵂ");
                    // some greeks
                    put("α", "ᵅ");
                    put("β", "ᵝ");
                    put("γ", "ᵞ");
                    put("δ", "ᵟ");
                    put("ε", "ᵋ");
                    put("θ", "ᶿ");
                    put("ι", "ᶥ");
                    put("Φ", "ᶲ");
                    put("φ", "ᵠ");
                    put("χ", "ᵡ");
                }
            });

    /**
     * map of alphabetic chars to their unicode subscript codepoints
     */
    private static Map<String, String> subscripts = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                {
                    // 0-9
                    put("0", "₀");
                    put("1", "₁");
                    put("2", "₂");
                    put("3", "₃");
                    put("4", "₄");
                    put("5", "₅");
                    put("6", "₆");
                    put("7", "₇");
                    put("8", "₈");
                    put("9", "₉");
                    // few lowercases
                    put("a", "ₐ");
                    put("e", "ₑ");
                    put("i", "ᵢ");
                    put("j", "ⱼ");
                    put("o", "ₒ");
                    put("r", "ᵣ");
                    put("u", "ᵤ");
                    put("v", "ᵥ");
                    put("x", "ₓ");
                    // some greeks
                    put("β", "ᵦ");
                    put("γ", "ᵧ");
                    put("ρ", "ᵨ");
                    put("φ", "ᵩ");
                    put("χ", "ᵪ");
                }
            });

    /**
     * Unescapes special entity char sequences like &lt; to its UTF-8 representation. All ISO-8859-1, HTML4
     * and Basic entities will be translated.
     * 
     * @param text the text that will be unescaped
     * @return the unescaped version of the string text
     */
    public static String unescapeEntities(String text) {
        CharSequenceTranslator iso = new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE());
        CharSequenceTranslator basic = new LookupTranslator(EntityArrays.BASIC_UNESCAPE());
        CharSequenceTranslator html4 = new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE());
        CharSequenceTranslator translator = new AggregateTranslator(iso, basic, html4);
        String translated = translator.translate(text);
        return subsup(translated);
    }

    public static String getSuperscriptChar(String chr) {
        return superscripts.containsKey(chr) ? superscripts.get(chr) : chr;
    }

    public static String getSubscriptChar(String chr) {
        return subscripts.containsKey(chr) ? subscripts.get(chr) : chr;
    }

    private static String subsup(String html) {
        // match <sub|sup> tags as well as wikipedias {{sub|}} {{sup|}} templates
        Pattern subsuper = Pattern.compile("(?:\\{\\{|<)(sub|sup)(?:>|\\|)(.)(?:\\}\\}|<\\/\\1>)");
        Matcher m;
        while ((m = subsuper.matcher(html)).find()) {
            String where = m.group(1);
            String what = m.group(2);
            if (where.equals("sub")) {
                what = getSubscriptChar(what);
            } else {
                what = getSuperscriptChar(what);
            }
            html = m.replaceFirst(what);
        }
        return html;
    }
}
