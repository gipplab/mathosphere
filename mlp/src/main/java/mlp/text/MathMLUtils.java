package mlp.text;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mlp.text.WikiTextUtils.MathMarkUpType;
import mlp.text.WikiTextUtils.MathTag;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

public class MathMLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MathMLUtils.class);
    private static final SnuggleEngine SNUGGLE_ENGINE = new SnuggleEngine();

    /**
     * list of false positive identifiers
     */
    public final static Set<String> BLACKLIST = prepareBlacklist();

    private static Set<String> prepareBlacklist() {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        // operators
        builder.add("sin", "cos", "tan", "min", "max", "argmax", "arg max", "argmin", "arg min", "inf",
                "lim", "log", "lg", "ln", "exp", "sup", "supp", "lim sup", "lim inf", "arg", "dim",
                "dimension", "cosh", "arccos", "arcsin", "arctan", "atan", "arcsec", "rank", "nullity",
                "det", "Det", "ker", "sec", "cot", "csc", "sinh", "coth", "tanh", "arcsinh", "arccosh",
                "arctanh", "atanh", "def", "image", "avg", "average", "mean", "var", "Var", "cov", "Cov",
                "diag", "span", "floor", "ceil", "head", "tail", "tr", "trace", "div", "mod", "round", "sum",
                "Re", "Im", "gcd", "sng", "sign", "length");

        // math symbols
        // http://unicode-table.com/en/blocks/mathematical-operators/
        List<String> mathOperators = listOfStrings('\u2200', 256);
        mathOperators.remove("∇"); // nabla is often an identifier, so we should keep it
        builder.addAll(mathOperators);
        // http://unicode-table.com/en/blocks/supplemental-mathematical-operators/
        builder.addAll(listOfStrings('\u2A00', 256));

        // others math-related
        builder.add(":=", "=", "+", "~", "e", "°", "′", "^");

        // symbols
        // http://unicode-table.com/en/blocks/spacing-modifier-letters/
        builder.addAll(listOfStrings('\u02B0', 80));
        // http://unicode-table.com/en/blocks/miscellaneous-symbols/
        builder.addAll(listOfStrings('\u2600', 256));
        // http://unicode-table.com/en/blocks/geometric-shapes/
        builder.addAll(listOfStrings('\u25A0', 96));
        // http://unicode-table.com/en/blocks/arrows/
        builder.addAll(listOfStrings('\u2190', 112));
        // http://unicode-table.com/en/blocks/miscellaneous-technical/
        builder.addAll(listOfStrings('\u2300', 256));
        // http://unicode-table.com/en/blocks/box-drawing/
        builder.addAll(listOfStrings('\u2500', 128));

        // false identifiers
        builder.add("constant", "const", "true", "false", "new", "even", "odd", "subject", "vs", "versus",
                "iff");
        builder.add("where", "unless", "otherwise", "else", "on", "of", "or", "with", "if", "then", "from",
                "to", "by", "has", "within", "when", "out", "and", "for", "as", "is", "at", "such", "that",
                "before", "after");

        // identifier that are also English (stop-)words
        builder.add("a", "A", "i", "I");

        // punctuation
        builder.add("%", "?", "!", ":", "'", "…", ";", "(", ")", "\"", "′′′′′", "′′′′", "′′′", "′′", "′",
                " ", " ");

        // special chars
        builder.add("_", "|", "*", "#", "{", "}", "[", "]", "$", "&", "/", "\\");

        // units
        builder.add("mol", "dB", "mm", "cm", "km", "Hz");

        return builder.build();
    }

    private static List<String> listOfStrings(char from, int amount) {
        List<String> result = Lists.newArrayListWithCapacity(amount);
        for (char c = from; c < from + amount; c++) {
            result.add(CharUtils.toString(c));
        }
        return result;
    }

    public static Multiset<String> extractIdentifiers(MathTag math) {
        try {
            return tryExtractIdentifiers(math);
        } catch (Exception e) {
            LOGGER.warn("exception occurred during 'extractIdentifiers'. Returning an empty set", e);
            return HashMultiset.create();
        }
    }

    private static Multiset<String> tryExtractIdentifiers(MathTag math) {
        if (math.getMarkUpType() == MathMarkUpType.LATEX) {
            return extractIdentifiersFromTex(math.getTagContent());
        } else {
            return extractIdentifiersFromMathML(math.getContent());
        }
    }

    public static Multiset<String> extractIdentifiersFromTex(String tex) {
        String mathML = texToMathML(tex);
        LOGGER.debug("converted {} to {}", tex.replaceAll("\\s+", " "), mathML);
        return extractIdentifiersFromMathML(mathML);
    }

    public static Multiset<String> extractIdentifiersFromMathML(String mathML) {
        try {
            return tryParseWithXpath(mathML);
        } catch (Exception e) {
            LOGGER.warn("exception occurred while trying to parse mathML with xpath... "
                    + "backing off to the regexp parser.", e);
            return parseWithRegex(mathML);
        }
    }

    private static Multiset<String> tryParseWithXpath(String mathML) {
        XML xml = new XMLDocument(mathML);
        xml = xml.registerNs("m", "http://www.w3.org/1998/Math/MathML");
        Multiset<String> result = HashMultiset.create();

        List<XML> subscript = xml.nodes("//m:msub");
        for (XML msubNode : subscript) {
            List<String> text = msubNode.xpath("*[normalize-space()]/text()");
            if (text.size() != 2) {
                String debugText = text.toString().replaceAll("\\s+", " ");
                String nmsubMathMl = msubNode.toString().replaceAll("\\s+", " ");
                LOGGER.debug("unexpected input: {} for {}", debugText, nmsubMathMl);
                continue;
            }
            String id = UnicodeUtils.normalizeString(text.get(0));
            String sub = UnicodeUtils.normalizeString(text.get(1));
            if (BLACKLIST.contains(id)) {
                continue;
            }
            if (isNumeric(id)) {
                continue;
            }
            result.add(id + "_" + sub);
        }

        List<String> allIdentifiers = xml.xpath("//m:mi[not(ancestor::m:msub)]/text()");
        for (String rawId : allIdentifiers) {
            String id = UnicodeUtils.normalizeString(rawId);
            if (BLACKLIST.contains(id)) {
                continue;
            }
            if (isNumeric(id)) {
                continue;
            }

            result.add(id);
        }

        return result;
    }

    public static boolean isNumeric(String id) {
        return id.matches("\\d+.?\\d*");
    }

    private static Multiset<String> parseWithRegex(String mathML) {
        Pattern miTag = Pattern.compile("<mi.*?>(.+?)</mi>");
        Matcher matcher = miTag.matcher(mathML);

        Multiset<String> ids = HashMultiset.create();
        while (matcher.find()) {
            String id = matcher.group(1);
            ids.add(id);
        }

        ids.removeAll(BLACKLIST);
        return ids;
    }

    public static String texToMathML(String tex) {
        try {
            SnuggleSession session = SNUGGLE_ENGINE.createSession();
            String cleanTexString = cleanTexString(tex);
            session.parseInput(new SnuggleInput("$$ " + cleanTexString + " $$"));
            String xmlString = session.buildXMLString();
            return xmlString;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String cleanTexString(String tex) {
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
