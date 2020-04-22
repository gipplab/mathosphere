package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextPageExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Moritz on 15.12.2015.
 * Fixed by Andre on 21.04.2020.
 */
public class WikiTextParserTest {

    @Test
    public void findFormulaFromWikiText() throws Exception {
        String text = WikiTextUtilsTest.getTestResource("com/formulasearchengine/mathosphere/mlp/gold/eval_dataset_sample.xml");
        RawWikiDocument doc = WikiTextPageExtractorMapper.getRawWikiDocumentFromSinglePage(text);
        final WikiTextParser mathConverter = new WikiTextParser(doc);
        final String real = mathConverter.parse();
        System.out.println(real);
    }

    @Test
    public void testChem1() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("../titration_wiki.txt"),"UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        final List<MathTag> mathTags = mathConverter.getMathTags();
        assertEquals(34, mathTags.size());
        assertEquals(22, mathTags.stream().filter(mathTag -> mathTag.getMarkUpType()
                == WikiTextUtils.MathMarkUpType.LATEXCE).count());
    }

    @Test
    public void testGo() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("legendre_wiki.txt"),"UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();
        assertThat(real, containsString("Let FORMULA_"));
    }

    @Test
    public void testGo10() throws Exception {
        String wikiText = "''a''<sub>x</sub>.";
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();

        List<MathTag> mathTags = mathConverter.getMathTags();
        assertEquals(1, mathTags.size());
        assertEquals("a_{x}", mathTags.get(0).getContent());
        assertThat(real, containsString(mathTags.get(0).placeholder()));
    }

    @Test
    public void testGo11() throws Exception {
        String wikiText = "Let the coin tosses be represented by a sequence {{nowrap|1=''X''&lt;sub&gt;0&lt;/sub&gt;, ''X''&lt;sub&gt;1&lt;/sub&gt;, &amp;hellip;}}";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();

        List<MathTag> mathTags = mathConverter.getMathTags();
        assertEquals(1, mathTags.size());
        assertEquals("\\mathit{X}_{0}, \\mathit{X}_1, …", mathTags.get(0).getContent());
        assertThat(real, containsString(mathTags.get(0).placeholder()));
    }

    @Test
    public void testGo2() throws Exception {
        String wikiText = IOUtils.toString(getClass().getResourceAsStream("../performance/hamiltonian_wiki.txt"),"UTF-8");
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();

        List<MathTag> mathTags = mathConverter.getMathTags();
        MathTag f1 = null, f2 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("H") ) f1 = t;
            else if ( t.getContent().equals("\\hat{H}") ) f2 = t;
        }

        assertNotNull(f1);
        assertNotNull(f2);
    }

    @Test
    public void testGo3() throws Exception {
        String wikiText = "Let '''<math> \\Omega </math>''' be an [[open subset]] of ℝ''<sup>n</sup>''. A  function '''<math> u </math>''' belonging to '''[[Lp space|<math>L^1(\\Omega)</math>]]''' is said of '''bounded variation''' ('''BV function'''), and written";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        List<MathTag> mathTags = mathConverter.getMathTags();
        MathTag f1 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("L^1(\\Omega)") ) f1 = t;
        }
        assertNotNull(f1);
    }

    @Test
    public void testGo4() throws Exception {
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
        List<MathTag> mathTags = mathConverter.getMathTags();
        MathTag f1 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("\\exist x \\exist y Lxy") ) f1 = t;
        }
        assertNotNull(f1);
    }

    @Test
    public void testGo5() throws Exception {
        String wikiText = "{{mvar|φ}}";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();

        List<MathTag> mathTags = mathConverter.getMathTags();
        assertEquals(1, mathTags.size());
        assertEquals("\\varphi", mathTags.get(0).getContent());
        assertThat(real, containsString(mathTags.get(0).placeholder()));
    }

    @Test
    public void testGo6() throws Exception {
        String wikiText = "{{math|10/19 &amp;middot; 7000 &amp;minus; 18/38 &amp;middot; 7000 {{=}} 368.42}}";
        wikiText = RawWikiDocument.unescapeText(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        List<MathTag> mathTags = mathConverter.getMathTags();
        MathTag f1 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("10/19 {\\cdot} 7000 - 18/38 {\\cdot} 7000 = 368.42") ) f1 = t;
        }
        assertNotNull(f1);
    }

    @Test
    public void testGo7() throws Exception {
        String wikiText = "{{NumBlk|::|Input &amp;nbsp; &lt;math&gt;\\int_{-\\infty}^{\\infty} c_{\\omega}\\,x_{\\omega}(t) \\, \\operatorname{d}\\omega&lt;/math&gt; &amp;nbsp; produces output &amp;nbsp; &lt;math&gt;\\int_{-\\infty}^{\\infty} c_{\\omega}\\,y_{\\omega}(t) \\, \\operatorname{d}\\omega\\,&lt;/math&gt;|{{EquationRef|Eq.1}}}}\n";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        mathConverter.parse();
        List<MathTag> mathTags = mathConverter.getMathTags();
        MathTag f1 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("\\int_{-\\infty}^{\\infty} c_{\\omega}\\,x_{\\omega}(t) \\, \\operatorname{d}\\omega") ) f1 = t;
        }
        assertNotNull(f1);
    }

    @Test
    public void testGo8() throws Exception {
        String wikiText =
                "&lt;ref&gt;&lt;math&gt;\\beta= \\alpha/3&lt;/math&gt; where &lt;math&gt;\\alpha&lt;/math&gt; is the\n"
                        +
                        "quantity used by Deshpande–Fleck&lt;/ref&gt;";
        wikiText = StringEscapeUtils.unescapeXml(wikiText);
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();
        List<MathTag> mathTags = mathConverter.getMathTags();
        MathTag f1 = null;
        for ( MathTag t : mathTags ) {
            if ( t.getContent().equals("\\beta= \\alpha/3") ) f1 = t;
        }
        assertNotNull(f1);
        assertThat(real, containsString(f1.placeholder()));
    }

    @Test
    public void testGo9() throws Exception {
        String wikiText = "Word\n<math>x</math>\nend.";
        final WikiTextParser mathConverter = new WikiTextParser(wikiText);
        final String real = mathConverter.parse();
        List<MathTag> mathTags = mathConverter.getMathTags();
        assertEquals(1, mathTags.size());
        assertEquals("x", mathTags.get(0).getContent());
        assertThat(real, containsString("Word "+mathTags.get(0).placeholder()+" end."));
    }

    @Test
    public void testWiki2Tex() throws Exception {
        String text = "<sub>a</sub>, <sup>b</sup>, '''c''', ''d''";
        final WikiTextParser mathConverter = new WikiTextParser(text);
        final String real = mathConverter.wiki2Tex(text);
        assertThat(real, containsString("_{a}"));
        assertThat(real, containsString("^{b}"));
        assertThat(real, containsString("\\mathbf{c}"));
        assertThat(real, containsString("\\mathit{d}"));
        assertThat(real, equalTo("_{a}, ^{b}, \\mathbf{c}, \\mathit{d}"));
    }
}