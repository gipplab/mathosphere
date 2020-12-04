package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextPageExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.SpecialToken;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiCitation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Created by Moritz on 15.12.2015.
 * Fixed by Andre on 21.04.2020.
 */
public class WikiTextParserTest {
    private static final Logger LOG = LogManager.getLogger(WikiTextParserTest.class.getName());

    private static final int TRIGGER_WARN_PERFORMANCE_MILLISECONDS = 5;

    @Test
    public void findFormulaFromWikiText() throws Exception {
        String text = WikiTextUtilsTest.getTestResource("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset_sample.xml");
        RawWikiDocument doc = WikiTextPageExtractorMapper.getRawWikiDocumentFromSinglePage(text);
        final WikiTextParser mathConverter = new WikiTextParser(doc);
        final List<String> real = mathConverter.parse();
        System.out.println(real);
    }

    @Test
    public void testChem1() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("../titration_wiki.txt"),"UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        final Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(mathTags.values().toString(), 36, mathTags.size());
        assertEquals(15, mathTags
                .values()
                .stream()
                .filter(
                        mathTag -> mathTag.getMarkUpType() == WikiTextUtils.MathMarkUpType.LATEXCE
                ).count()
        );
    }

    @Test
    public void testLegendreWiki() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("legendre_wiki.txt"),"UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final List<String> real = mathConverter.parse();
        assertThat(real.toString(), containsString("Let FORMULA_"));
    }

    @Test
    public void testFollowingSub() throws Exception {
        String wikiText = "''a''<sub>x</sub>.";
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final List<String> res = mathConverter.parse();
        final String real = res.get(0);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(mathTags.values().toString(), 1, mathTags.size());

        String key = mathTags.keySet().stream().findFirst().get();
        assertEquals("a_{x}", mathTags.get(key).getContent());
        assertEquals( key + ". ", real);
    }

    @Test
    public void testNonSuccessiveFollowingSub() throws Exception {
        String wikiText = "''a'' but <sub>x</sub>.";
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final List<String> res = mathConverter.parse();
        final String real = res.get(0);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(mathTags.values().toString(), 2, mathTags.size());

        List<MathTag> tags = new ArrayList<>(mathTags.values());
        int aTagIdx = tags.get(0).getContent().equals("a") ? 0 : 1;
        int xTagIdx = 1-aTagIdx;
        assertEquals(tags.get(aTagIdx).placeholder() + " but "+ tags.get(xTagIdx).placeholder() + ". ", real);
    }

    @Test
    public void testSuccessiveFollowingSub() throws Exception {
        String wikiText = "Lets test ''d''&lt;sup&gt;''j''&lt;/sup&gt;&lt;sub&gt;''m''’,''m''&lt;/sub&gt;, a rather complex wikitext.";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(mathTags.values().toString(), 1, mathTags.size());

        String key = mathTags.keySet().stream().findFirst().get();
        assertEquals(mathTags.values().toString(), "d^{j}_{m',m}", mathTags.get(key).getContent());
    }

    @Test
    public void testNoWrapTemplate() throws Exception {
        String wikiText = "Let the coin tosses be represented by a sequence {{nowrap|1=''X''&lt;sub&gt;0&lt;/sub&gt;, ''X''&lt;sub&gt;1&lt;/sub&gt;, &amp;hellip;}}";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse().get(0);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.toString(), maths.contains("X_{0},X_{1}, \\ldots"));
        assertThat(real, containsString(mathTags.keySet().stream().findFirst().get()));
    }

    @Test
    public void performanceTest() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("../performance/hamiltonian_wiki.txt"),"UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);

        Instant start = Instant.now();
        final List<String> real = mathConverter.parse();
        long time = Duration.between(start, Instant.now()).toMillis();
        if ( time > TRIGGER_WARN_PERFORMANCE_MILLISECONDS ) LOG.warn("Performance test took quite long, worth to investigate it. " +
                "Hamiltonian-wiki took " + time + "ms");
        else LOG.info("Performance test seems legit, hamiltonian-wiki took " + time + "ms");

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();

        // test if specific math was extracted
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.contains("H"));
        assertTrue(maths.contains("\\hat{H}"));
        assertTrue(maths.contains("\\hat{H} = \\sum_{n=1}^N \\hat{T}_n + V"));

        // test if all expressions exist in the cleaned text
        String totalText = real.toString();
        mathTags.values().forEach(m -> assertTrue( totalText.contains(m.placeholder()) ));
    }

    @Test
    public void testItalicMathDouble() throws Exception {
        String wikiText = "Let '''<math> \\Omega </math>''' be an [[open subset]] of ℝ''<sup>n</sup>''. A  function '''<math> u </math>''' belonging to '''[[Lp space|<math>L^1(\\Omega)</math>]]''' is said of '''bounded variation''' ('''BV function'''), and written";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.contains("L^1(\\Omega)"));
    }

    @Test
    public void tableTest() throws Exception {
        String wikiText = "{| style=&quot;margin: 1em auto 1em auto;&quot;\n" +
                "|-\n" +
                "| style=&quot;width:350px&quot; |\n" +
                "&lt;!--START--&gt;{| style=&quot;text-align: center; border: 1px solid darkgray; width:300px&quot;\n" +
                "|-\n" +
                "|colspan=&quot;2&quot;|&lt;span style=&quot;color:darkgray;&quot;&gt;No column/row is empty:&lt;/span&gt;\n"
                +
                "|- style=&quot;vertical-align:top;&quot;\n" +
                "|[[File:Predicate logic; 2 variables; example matrix a2e1.svg|thumb|center|120px|1. &lt;math&gt;\\forall x \\exist y Lyx&lt;/math&gt;:&lt;br&gt;Everyone is loved by someone.]]\n"
                +
                "|[[File:Predicate logic; 2 variables; example matrix a1e2.svg|thumb|center|120px|2. &lt;math&gt;\\forall x \\exist y Lxy&lt;/math&gt;:&lt;br&gt;Everyone loves someone.]]\n"
                +
                "|}&lt;!--END--&gt;\n" +
                "| rowspan=&quot;2&quot; style=&quot;width:210px&quot; |\n" +
                "&lt;!--START--&gt;{| style=&quot;text-align: center; border: 1px solid darkgray; width:160px&quot;\n" +
                "|-\n" +
                "|colspan=&quot;2&quot;|&lt;span style=&quot;color:darkgray;&quot;&gt;The diagonal is&lt;br&gt;nonempty/full:&lt;/span&gt;\n"
                +
                "|- style=&quot;vertical-align:top;&quot;\n" +
                "|[[File:Predicate logic; 2 variables; example matrix e(12).svg|thumb|center|120px|5. &lt;math&gt;\\exist x Lxx&lt;/math&gt;:&lt;br&gt;Someone loves himself.]]\n"
                +
                "|- style=&quot;vertical-align:top;&quot;\n" +
                "|[[File:Predicate logic; 2 variables; example matrix a(12).svg|thumb|center|120px|6. &lt;math&gt;\\forall x Lxx&lt;/math&gt;:&lt;br&gt;Everyone loves himself.]]\n"
                +
                "|}&lt;!--END--&gt;\n" +
                "| rowspan=&quot;2&quot; style=&quot;width:250px&quot; |\n" +
                "&lt;!--START--&gt;{| style=&quot;text-align: center; border: 1px solid darkgray; width:160px&quot;\n" +
                "|-\n" +
                "|colspan=&quot;2&quot;|&lt;span style=&quot;color:darkgray;&quot;&gt;The matrix is&lt;br&gt;nonempty/full:&lt;/span&gt;\n"
                +
                "|- style=&quot;vertical-align:top;&quot;\n" +
                "|[[File:Predicate logic; 2 variables; example matrix e12.svg|thumb|center|120px|7. &lt;math&gt;\\exist x \\exist y Lxy&lt;/math&gt;:&lt;br&gt;Someone loves someone.&lt;br&gt;&lt;br&gt;8. &lt;math&gt;\\exist x \\exist y Lyx&lt;/math&gt;:&lt;br&gt;Someone is loved by someone.]]\n"
                +
                "|- style=&quot;vertical-align:top;&quot;\n" +
                "|[[File:Predicate logic; 2 variables; example matrix a12.svg|thumb|center|120px|9. &lt;math&gt;\\forall x \\forall y Lxy&lt;/math&gt;:&lt;br&gt;Everyone loves everyone.&lt;br&gt;&lt;br&gt;10. &lt;math&gt;\\forall x \\forall y Lyx&lt;/math&gt;:&lt;br&gt;Everyone is loved by everyone.]]\n"
                +
                "|}&lt;!--END--&gt;\n" +
                "|rowspan=&quot;2&quot;|[[File:Predicate logic; 2 variables; implications.svg|thumb|250px|right|[[Hasse diagram]] of the implications]]\n"
                +
                "|-\n" +
                "|\n" +
                "&lt;!--START--&gt;{| style=&quot;text-align: center; border: 1px solid darkgray; width:300px&quot;\n" +
                "|-\n" +
                "|colspan=&quot;2&quot;|&lt;span style=&quot;color:darkgray;&quot;&gt;One row/column is full:&lt;/span&gt;\n"
                +
                "|- style=&quot;vertical-align:top;&quot;\n" +
                "|[[File:Predicate logic; 2 variables; example matrix e1a2.svg|thumb|center|120px|3. &lt;math&gt;\\exist x \\forall y Lxy&lt;/math&gt;:&lt;br&gt;Someone loves everyone.]]\n"
                +
                "|[[File:Predicate logic; 2 variables; example matrix e2a1.svg|thumb|center|120px|4. &lt;math&gt;\\exist x \\forall y Lyx&lt;/math&gt;:&lt;br&gt;Someone is loved by everyone.]]\n"
                +
                "|}&lt;!--END--&gt;\n" +
                "|}";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        List<String> sections = mathConverter.parse();

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        // tables are ignored! so we should get an empty string and no math.
        assertEquals(1, sections.size());
        assertEquals(0, mathTags.values().size());
        assertEquals("", sections.get(0));
//        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
//        assertTrue(maths.toString(), maths.contains("\\exist x \\exist y Lxy"));
    }

    @Test
    public void nonPerfectRefTest() throws Exception {
        String wikiText = "where {{mvar|λ}} blabla numbers.&lt;ref name = Abramowitz_9_1_74&gt;Abramowitz and Stegun, [http://www.math.sfu.ca/~cbm/aands/page_363.htm p. 363, 9.1.74].&lt;/ref&gt; For {{math|{{abs|''λ''&lt;sup&gt;2&lt;/sup&gt; − 1}} &lt; 1}},&lt;ref name = Abramowitz_9_1_74 /&gt;";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);

        Map<String, SpecialToken> citeLib = mathConverter.getMetaLibrary().getCiteLib();
        assertEquals(1, citeLib.size());
        SpecialToken specialToken = citeLib.values().stream().findAny().orElseThrow();
        assertTrue( specialToken instanceof WikiCitation );
        assertNotNull( "Abramowitz_9_1_74", ((WikiCitation) specialToken).getCiteKey() );

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(2, mathTags.size());

        List<MathTag> maths = mathTags.values().stream().sorted(Comparator.comparing(MathTag::getKey)).collect(Collectors.toList());
        assertEquals("\\lambda", maths.get(0).getContent());
        assertEquals("\\left|\\lambda^{2} - 1\\right|< 1", maths.get(1).getContent());
    }

    @Test
    public void testMVarTemplate() throws Exception {
        String wikiText = "{{mvar|φ}}";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse().get(0);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(1, mathTags.size());

        String key = mathTags.keySet().stream().findFirst().get();
        assertEquals("\\varphi", mathTags.get(key).getContent());
        assertEquals(key, real.trim());
    }

    @Test
    public void testMathTemplateWithUnicode() throws Exception {
        String wikiText = "{{math|10/19 &amp;middot; 7000 &amp;minus; 18/38 &amp;middot; 7000 {{=}} 368.42}}";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.toString(), maths.contains("10/19 \\cdot 7000 - 18/38 \\cdot 7000 = 368.42"));
    }

    @Test
    public void testNumBlk() throws Exception {
        String wikiText = "Input &amp;nbsp;{{NumBlk|::|&lt;math&gt;\\int_{-\\infty}^{\\infty} c_{\\omega}\\,x_{\\omega}(t) \\, \\operatorname{d}\\omega&lt;/math&gt; &amp;nbsp; produces output &amp;nbsp; &lt;math&gt;\\int_{-\\infty}^{\\infty} c_{\\omega}\\,y_{\\omega}(t) \\, \\operatorname{d}\\omega\\,&lt;/math&gt;|{{EquationRef|Eq.1}}}}\n";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        String sentence = mathConverter.parse().get(0);
        System.out.println(sentence);
        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertEquals(maths.toString(), 2, maths.size());
        assertTrue(maths.toString(), maths.contains("\\int_{-\\infty}^{\\infty} c_{\\omega}\\,x_{\\omega}(t) \\, \\operatorname{d}\\omega"));
    }

    @Test
    public void testMathInTextTest() throws Exception {
        String wikiText =
                "&lt;math&gt;\\beta= \\alpha/3&lt;/math&gt; where &lt;math&gt;\\alpha&lt;/math&gt; is the\n"
                        +
                        "quantity used by &lt;ref&gt;Deshpande–Fleck&lt;/ref&gt;";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        List<String> sections = mathConverter.parse();
        final String real = sections.get(0);

        Collection<MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib().values();
        MathTag f1 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("\\beta= \\alpha/3") ) f1 = t;
        }
        assertNotNull(real, f1);
        assertThat(real, containsString(f1.placeholder()));
    }

    @Test
    public void testMultilineTest() throws Exception {
        String wikiText = "Word\n<math>x</math>\nend.";
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse().get(0);
        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(1, mathTags.size());

        String key = mathTags.keySet().stream().findFirst().get();
        assertEquals("x", mathTags.get(key).getContent());
        assertThat(real, containsString("Word "+mathTags.get(key).placeholder()+" end. "));
    }

    @Test
    public void testWiki2Tex() throws Exception {
        String text = "<sub>a</sub>, <sup>b</sup>, '''c''', ''d''";
        final String real = WikiTextParser.replaceClearMath(text);
        assertThat(real, containsString("_{a}"));
        assertThat(real, containsString("^{b}"));
        assertThat(real, containsString("\\mathbf{c}"));
        // replaceClearMath is only called inside math environments (in the new version of
        // the wiki parser, which means, every text is by default italic. Hence ''x'' will
        // no longer be replaced by \\mathit{x}.
//        assertThat(real, containsString("\\mathit{d}"));
//        assertThat(real, equalTo("_{a}, ^{b}, \\mathbf{c}, \\mathit{d}"));
        assertThat(real, containsString("d"));
        assertThat(real, equalTo("_{a}, ^{b}, \\mathbf{c}, d"));
    }

    @Test
    public void testVanDerWaerdenExpression() throws Exception {
        String text = "The Van-der-Waerden Number ''W''(''k'',''n'').";
        final WikiTextParser mathConverter = new WikiTextParser(text);
        List<String> sections = mathConverter.parse();

        assertEquals(1, sections.size());
        String sentence = sections.get(0);
        Collection<MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib().values();

        assertEquals(sections.toString(), 1, sections.size());
        assertEquals(mathTags.toString(), 1, mathTags.size());

        Set<String> formulae = mathTags.stream().map(MathTag::getContent).collect(Collectors.toSet());
        assertTrue(formulae.toString(), formulae.contains("W(k,n)"));

        MathTag mTag = mathTags.stream().findFirst().get();
        assertEquals("The Van-der-Waerden Number "+mTag.placeholder()+". ", sentence);
    }

    @Test
    public void testWrongMathAnnotationInWikitext() throws Exception {
        String wikiText = "The term is uniform on the interval [ε, {{pi}}-ε] for every ε &gt; 0.";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        List<String> sections = mathConverter.parse();

        assertEquals(1, sections.size());
        String sentence = sections.get(0);
        System.out.println(sentence);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(2, mathTags.size());

        List<MathTag> mTagList = new ArrayList<>(mathTags.values());
        int idxFirst = mTagList.get(0).getContent().contains("pi") ? 0 : 1;
        int idxSecond = 1-idxFirst;

        assertEquals("[\\epsilon,\\pi-\\epsilon]", mTagList.get(idxFirst).getContent());
        assertEquals("\\epsilon > 0", mTagList.get(idxSecond).getContent());

        String expectedOutput = "The term is uniform on the interval " +
                mTagList.get(idxFirst).placeholder() +
                " for every " +
                mTagList.get(idxSecond).placeholder() +
                ".";
        assertEquals( expectedOutput, sentence.trim() );
    }

    @Test
    public void jacobiIntroTest() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("../JacobiPolynomial.txt"), "UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        List<String> sections = mathConverter.parse();

        Collection<MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib().values();

        assertEquals(sections.toString(), 3, sections.size());
        assertEquals(mathTags.toString(), 7, mathTags.size());

        Set<String> formulae = mathTags.stream().map(MathTag::getContent).collect(Collectors.toSet());
        assertTrue(formulae.toString(), formulae.contains("P_{n}^{(\\alpha, \\beta)}(x)"));
        assertTrue(formulae.toString(), formulae.contains("(1 - x)^{\\alpha}(1 + x)^{\\beta}"));
        assertTrue(formulae.toString(), formulae.contains("[-1, 1]"));
        assertTrue(formulae.toString(), formulae.contains("O"));
        assertTrue(formulae.toString(), formulae.contains("[\\epsilon,\\pi-\\epsilon]"));
        assertTrue(formulae.toString(), formulae.contains("\\epsilon > 0"));
        assertTrue(formulae.toString(), formulae.contains("s"));

        System.out.println(sections.get(0));
    }

    @Test
    public void simpleInlineTest() throws Exception {
        String wikiText = "The term ''s'' for something.";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        List<String> sections = mathConverter.parse();

        Collection<MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib().values();

        assertEquals(sections.toString(), 1, sections.size());
        assertEquals(mathTags.toString(), 1, mathTags.size());

        MathTag sTag = mathTags.stream().findFirst().get();
        assertEquals(mathTags.toString(), "s", sTag.getContent());

        String output = sections.get(0);
        assertEquals("The term " + sTag.placeholder() + " for something. ", output);
    }

    @Test
    public void vanDerWaerdenTrickyXMLTest() throws Exception {
        String wikiText = "We have &lt;math&gt;W(2, k) &gt; 2^k/k^\\varepsilon&lt;/math&gt;, for all.";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        List<String> sections = mathConverter.parse();

        Collection<MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib().values();

        assertEquals(sections.toString(), 1, sections.size());
        assertEquals(mathTags.toString(), 1, mathTags.size());

        MathTag sTag = mathTags.stream().findFirst().get();
        assertEquals(mathTags.toString(), "W(2, k) > 2^k/k^\\varepsilon", sTag.getContent());
    }
}