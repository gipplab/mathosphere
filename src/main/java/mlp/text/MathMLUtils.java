package mlp.text;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mlp.text.WikiTextUtils.MathMarkUpType;
import mlp.text.WikiTextUtils.MathTag;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

public class MathMLUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MathMLUtils.class);
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

    public static Set<String> extractIdentifiers(MathTag math) {
        if (math.getMarkUpType() == MathMarkUpType.LATEX) {
            return extractIdentifiersFromTex(math.getTagContent());
        } else {
            return extractIdentifiersFromMathML(math.getContent());
        }
    }

    public static Set<String> extractIdentifiersFromTex(String tex) {
        String mathML = texToMathML(tex);
        LOGGER.debug("converted {} to {}", tex.replaceAll("\\s+", " "), mathML);
        return extractIdentifiersFromMathML(mathML);
    }

    public static Set<String> extractIdentifiersFromMathML(String mathML) {
        XML xml = new XMLDocument(mathML);
        xml = xml.registerNs("m", "http://www.w3.org/1998/Math/MathML");

        Set<String> result = new LinkedHashSet<>();

        List<XML> subscript = xml.nodes("//m:msub");
        for (XML msubNode : subscript) {
            List<String> text = msubNode.xpath("*[normalize-space()]/text()");
            if (text.size() != 2) {
                String debugText = text.toString().replaceAll("\\s+", " ");
                String nmsubMathMl = msubNode.toString().replaceAll("\\s+", " ");
                LOGGER.debug("unexpected input: {} for {}", debugText, nmsubMathMl);
                continue;
            }
            result.add(text.get(0) + "_" + text.get(1));
        }

        List<String> allIdentifiers = xml.xpath("//m:mi/text()");
        result.addAll(allIdentifiers);
        result.removeAll(BLACKLIST);
        return result;
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
