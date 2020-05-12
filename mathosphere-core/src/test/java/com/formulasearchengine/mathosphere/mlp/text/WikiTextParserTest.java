package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextPageExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
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
        assertEquals(34, mathTags.size());
        assertEquals(22, mathTags.values().stream().filter(mathTag -> mathTag.getMarkUpType()
                == WikiTextUtils.MathMarkUpType.LATEXCE).count());
    }

    @Test
    public void testGo() throws Exception {
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
        assertEquals(1, mathTags.size());

        String key = mathTags.keySet().stream().findFirst().get();
        assertEquals("a_{x}", mathTags.get(key).getContent());
        assertEquals(" " + key + " . ", real);
    }

    @Test
    public void testNonSuccessiveFollowingSub() throws Exception {
        String wikiText = "''a'' but <sub>x</sub>.";
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final List<String> res = mathConverter.parse();
        final String real = res.get(0);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(2, mathTags.size());

        List<MathTag> tags = new ArrayList<>(mathTags.values());
        int aTagIdx = tags.get(0).getContent().equals("a") ? 0 : 1;
        int xTagIdx = 1-aTagIdx;
        assertEquals(" " + tags.get(aTagIdx).placeholder() + " but "+ tags.get(xTagIdx).placeholder() +" . ", real);
    }

    @Test
    public void testSuccessiveFollowingSub() throws Exception {
        String wikiText = "Lets test ''d''&lt;sup&gt;''j''&lt;/sup&gt;&lt;sub&gt;''m''’,''m''&lt;/sub&gt;, a rather complex wikitext.";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        assertEquals(1, mathTags.size());

        String key = mathTags.keySet().stream().findFirst().get();
        assertEquals("d^{j}_{m’,m}", mathTags.get(key).getContent());
    }

    @Test
    public void testNoWrapTemplate() throws Exception {
        String wikiText = "Let the coin tosses be represented by a sequence {{nowrap|1=''X''&lt;sub&gt;0&lt;/sub&gt;, ''X''&lt;sub&gt;1&lt;/sub&gt;, &amp;hellip;}}";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse().get(0);

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.contains("\\mathit{X}_{0}"));
        assertTrue(maths.contains("\\mathit{X}_{1}"));
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
    public void testLongInput() throws Exception {
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
        mathConverter.parse();

        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.contains("\\exist x \\exist y Lxy"));
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
        String wikiText = "{{NumBlk|::|Input &amp;nbsp; &lt;math&gt;\\int_{-\\infty}^{\\infty} c_{\\omega}\\,x_{\\omega}(t) \\, \\operatorname{d}\\omega&lt;/math&gt; &amp;nbsp; produces output &amp;nbsp; &lt;math&gt;\\int_{-\\infty}^{\\infty} c_{\\omega}\\,y_{\\omega}(t) \\, \\operatorname{d}\\omega\\,&lt;/math&gt;|{{EquationRef|Eq.1}}}}\n";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        Map<String, MathTag> mathTags = mathConverter.getMetaLibrary().getFormulaLib();
        List<String> maths = mathTags.values().stream().map(MathTag::getContent).collect(Collectors.toList());
        assertTrue(maths.contains("\\int_{-\\infty}^{\\infty} c_{\\omega}\\,x_{\\omega}(t) \\, \\operatorname{d}\\omega"));
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
        assertThat(real, containsString("Word "+mathTags.get(key).placeholder()+" end."));
    }

    @Test
    public void testWiki2Tex() throws Exception {
        String text = "<sub>a</sub>, <sup>b</sup>, '''c''', ''d''";
        final String real = WikiTextParser.replaceClearMath(text);
        assertThat(real, containsString("_{a}"));
        assertThat(real, containsString("^{b}"));
        assertThat(real, containsString("\\mathbf{c}"));
        assertThat(real, containsString("\\mathit{d}"));
        assertThat(real, equalTo("_{a}, ^{b}, \\mathbf{c}, \\mathit{d}"));
    }

    @Test
    public void testWrongMathAnnotationInWikitext() throws Exception {
        String wikiText = "The term is uniform on the interval [ε, {{pi}}-ε] for every ε &gt; 0.";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
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

        assertEquals("[\\epsilon, \\pi-\\epsilon]", mTagList.get(idxFirst).getContent());
        assertEquals("\\epsilon > 0", mTagList.get(idxSecond).getContent());

        String expectedOutput = "The term is uniform on the interval " +
                mTagList.get(idxFirst).placeholder() +
                " for every " +
                mTagList.get(idxSecond).placeholder() +
                ".";
        assertEquals( expectedOutput, sentence.trim() );
    }
}