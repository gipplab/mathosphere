package com.formulasearchengine.mathosphere.mlp.text;

//import com.formulasearchengine.mathmltools.xmlhelper.NonWhitespaceNodeList;
//import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
//import com.formulasearchengine.mathmltools.xmlhelper.XmlNamespaceTranslator;

import com.formulasearchengine.mathmltools.helper.XMLHelper;
import com.formulasearchengine.mathmltools.xml.NonWhitespaceNodeList;
import com.formulasearchengine.mathmltools.xml.XmlNamespaceTranslator;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils.MathMarkUpType;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import org.apache.commons.lang3.CharUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathMLUtils {

    private static final Logger LOGGER = LogManager.getLogger(MathMLUtils.class.getName());
    private static final SnuggleEngine SNUGGLE_ENGINE = new SnuggleEngine();

    /**
     * list of false positive identifiers
     */
    public final static Set<String> BLACKLIST = prepareBlacklist();
    private static boolean summarizeSubscripts = false;

    public static String getEngine() {
        return engine;
    }

    public static void setEngine(String engine) {
        MathMLUtils.engine = engine;
    }

    //@TODO: Make this configurable
    private static String engine = "snuggle";

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

    public static Multiset<String> extractIdentifiers(MathTag math, Boolean useTeXIdentifiers, String url) {
        try {
            return tryExtractIdentifiers(math, useTeXIdentifiers, url);
        } catch (Exception e) {
            LOGGER.warn("exception occurred during 'extractIdentifiers'. Returning an empty set", e);
            return LinkedHashMultiset.create();
        }
    }

    private static Multiset<String> tryExtractIdentifiers(MathTag math, Boolean useTeXIdentifiers, String url) {
        if (math.getMarkUpType() != MathMarkUpType.MATHML) {
            return extractIdentifiersFromTex(math.getTagContent(), useTeXIdentifiers, url);
        } else {
            return extractIdentifiersFromMathML(math.getContent(), useTeXIdentifiers, false);
        }
    }

    public static Multiset<String> extractIdentifiersFromTex(String tex, boolean useTeX, String url) {
        if (useTeX) {
            try {
                Multiset<String> identifiers = TexInfo.getIdentifiers(tex, url);
                //TODO: Migrate to texvcinfo
                identifiers.removeIf(x -> x.equals("\\infty") || x.startsWith("\\operatorname"));
                if (summarizeSubscripts) {
                    for (String identifier : identifiers.elementSet()) {
                        if (identifier.matches("(.*?)_\\{[a-zA-Z0-9]\\}$")) {
                            identifiers.remove(identifier, Integer.MAX_VALUE);
                            identifiers.add(identifier.replaceAll("(.*?)_\\{[a-zA-Z0-9]\\}$", "$1_"));
                        }
                    }
                }
                return identifiers;

            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException | TransformerException e) {
                e.printStackTrace();
                return HashMultiset.create();
            }
        }
        String mathML = texToMathML(tex);
        LOGGER.debug("converted {} to {}", tex.replaceAll("\\s+", " "), mathML);
        return extractIdentifiersFromMathML(mathML, false, false);
    }

    public static Multiset<String> extractIdentifiersFromMathML(String mathML, Boolean useTeXIdentifiers, boolean useBlacklist) {
        try {
            return tryParseWithXpath(mathML, useTeXIdentifiers, useBlacklist);
        } catch (Exception e) {
            LOGGER.warn("exception occurred while trying to parse mathML with xpath... "
                    + "backing off to the regexp parser.", e);
            return parseWithRegex(mathML);
        }
    }

    private static Multiset<String> tryParseWithXpath(String mathML, boolean useTeX, boolean useBlacklist) {
        Document doc = XMLHelper.string2Doc(mathML, true);

        new XmlNamespaceTranslator()
                .setDefaultNamespace( "http://www.w3.org/1998/Math/MathML" )
                .addTranslation(null, "http://www.w3.org/1998/Math/MathML")
                .addTranslation("m", "http://www.w3.org/1998/Math/MathML")
                .translateNamespaces( doc, "m" );

        Element root = doc.getDocumentElement();

        NonWhitespaceNodeList list = null;

        XPath xpath = XMLHelper.namespaceAwareXpath( "m", "http://www.w3.org/1998/Math/MathML" );

        try {
            list = new NonWhitespaceNodeList(XMLHelper.getElementsB(root, xpath.compile("//m:msub")));
            //List<XML> subscript = doc.get.nodes("//m:msub");
            Multiset<String> result = HashMultiset.create();

            for (Node msubNode : list) {
                NonWhitespaceNodeList nodeList = new NonWhitespaceNodeList(XMLHelper.getElementsB(msubNode, xpath.compile("*[normalize-space()]/text()")));


                List<String> text;
                if (nodeList.getLength() != 2) {
                    String debugText = nodeList.toString().replaceAll("\\s+", " ");
                    String nmsubMathMl = msubNode.toString().replaceAll("\\s+", " ");
                    LOGGER.debug("unexpected input: {} for {}", debugText, nmsubMathMl);
                    continue;
                }
                // we use always tex now for identifier, no more normalization
//                String id;
//                String sub;
//                if (useTeX) {
                    String id = UnicodeMap.string2TeX(nodeList.item(0).getTextContent());
                    String sub = "{" + UnicodeMap.string2TeX(nodeList.item(1).getTextContent()) + "}";
//                } else {
//                    id = UnicodeUtils.normalizeString(nodeList.item(0).getTextContent());
//                    sub = UnicodeUtils.normalizeString(nodeList.item(1).getTextContent());
//                }
                if (useBlacklist && BLACKLIST.contains(id)) {
                    continue;
                }
                if (isNumeric(id)) {
                    continue;
                }
                result.add(id + "_" + sub);
            }

            NonWhitespaceNodeList allIdentifiers =
                    new NonWhitespaceNodeList(XMLHelper.getElementsB( root, xpath.compile("//m:mi[not(ancestor::m:msub)]/text()") ));

            //List<String> allIdentifiers = xml.xpath("//m:mi[not(ancestor::m:msub)]/text()");
            for (Node identifierNode : allIdentifiers) {
                String rawId = identifierNode.getTextContent();
                String id;
//                if (useTeX) {
                    id = UnicodeMap.string2TeX(rawId);
                    id = id.replaceAll("^\\{(.*)\\}$", "$1");
//                } else {
//                    id = UnicodeUtils.normalizeString(rawId);
//                }
                if (useBlacklist && BLACKLIST.contains(id)) {
                    continue;
                }
                if (isNumeric(id)) {
                    continue;
                }

                result.add(id);
            }

            return result;

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
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
            if (engine.equals("snuggle")) {
                SnuggleSession session = SNUGGLE_ENGINE.createSession();
                String cleanTexString = cleanTexString(tex);
                session.parseInput(new SnuggleInput("$$ " + cleanTexString + " $$"));
                String xmlString = session.buildXMLString();
                return xmlString;
            } else {
                return TeX2MathML.TeX2MML(tex);
            }
        } catch (Exception e) {
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
