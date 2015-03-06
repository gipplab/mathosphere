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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mlp.utils.MathUtils.MathMarkUpType;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import com.google.common.collect.ImmutableSet;

/**
 * @author rob
 */
public class MathIdentifierExtractor {

    private static final SnuggleEngine SNUGGLE_ENGINE = new SnuggleEngine();

    /**
     * list of false positive identifiers
     */
    private final static Set<String> BLACKLIST = ImmutableSet.of("sin", "cos", "tan", "min", "max", "inf",
            "lim", "log", "exp", "sup", "lim sup", "lim inf", "arg", "dim", "cosh", "arccos", "arcsin",
            "arctan", "rank", "ln", "det", "ker", "sec", "cot", "csc", "tanh", "sinh", "coth", "cot", "⋯",
            ":", "'", "′", "…", "∞", "⋮", " ", " ", "~", ";", "#", "e", "⋱", "{", "}", "%", "?", "°",
            "′", "−", "★", "\"", "!",

            // ignore identifier that are also English (stop-)words
            "a", "A", "i", "I",

            // ignore special chars
            "$", "\\");

    /**
     * Returns a list of all identifers within a given formula. The formula is coded in TeX.
     * 
     * @param formula TeX representation of a formula
     * @param augmention
     * @return list of identifiers
     */
    public static Set<String> getAllfromTex(String formula) {
        try {
            String mathml = convertLatexToMathML(formula);
            return getIdentifiersFrom(mathml);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public static String convertLatexToMathML(String formula) throws IOException {
        SnuggleSession session = SNUGGLE_ENGINE.createSession();
        SnuggleInput input = new SnuggleInput("$$ " + cleanupTexString(formula) + " $$");
        session.parseInput(input);
        return session.buildXMLString();
    }

    public static Set<String> extractIdentifiersFrom(MathUtils.MathTag tag) {
        if (tag.getMarkUpType() == MathMarkUpType.LATEX) {
            return getAllfromTex(tag.getTagContent());
        } else {
            return getAllfromMathML(tag.getContent());
        }
    }

    public static Set<String> getAllfromMathML(String mathml) {
        return getIdentifiersFrom(mathml);
    }

    /**
     * Returns a list of unique identifiers from a MathML string. This function searches for all <mi/> or
     * <ci/> tags within the string.
     * 
     * @param mathml
     * @return a list of unique identifiers. When no identifiers were found, an empty list will be returned.
     */
    private static Set<String> getIdentifiersFrom(String mathml) {
        Set<String> foundIdentifiers = new LinkedHashSet<>();

        Pattern subsuper = Pattern.compile("<m(sub|sup)><mi>(.)</mi><m(n|i)>(.)</m\\3></m\\1>");

        Matcher m = subsuper.matcher(mathml);
        // check for single sub-/superscripts
        while (m.find()) {
            String identifier = m.group(2);
            String where = m.group(1);
            String script = m.group(4);
            // superscript number aren't identifiers at all
            if (where.equals("sup") && script.matches("\\d")) {
                script = "";
            }
            if (!BLACKLIST.contains(identifier)) {
                if (where.equals("sub")) {
                    script = StringUtils.getSubscriptChar(script);
                } else {
                    script = StringUtils.getSuperscriptChar(script);
                }
                foundIdentifiers.add(identifier + script);
            }
            // remove matched substring from mathml
            mathml = m.replaceFirst("");
        }

        // deal with all remaining identifiers
        Pattern mi = Pattern.compile("<(mi)(.*?)>(.{1,10})</mi>", Pattern.DOTALL);
        m = mi.matcher(mathml);
        while (m.find()) {
            String identifier = m.group(3);
            if (BLACKLIST.contains(identifier)) {
                continue;
            }
            foundIdentifiers.add(identifier);
        }

        return foundIdentifiers;
    }

    /**
     * Returns a cleaned version of the TeX string.
     * 
     * @param tex the TeX string
     * @return the cleaned TeX string
     */
    private static String cleanupTexString(String tex) {
        // strip text blocks
        tex = tex.replaceAll("\\\\(text|math(:?bb|bf|cal|frak|it|sf|tt))\\{.*?\\}", "");
        // strip arbitrary operators
        tex = tex.replaceAll("\\\\operatorname\\{.*?\\}", "");
        // strip some unparseble stuff
        tex = tex.replaceAll("\\\\(rang|left|right|rangle|langle)|\\|", "");
        // strip dim/log
        tex = tex.replaceAll("\\\\(dim|log)_(\\w+)", "$1");
        // strip "is element of" definitions
        tex = tex.replaceAll("^(.*?)\\\\in", "$1");
        // strip indices
        tex = tex.replaceAll("^([^\\s\\\\\\{\\}])_[^\\s\\\\\\{\\}]$", "$1");
        return tex;
    }

}
