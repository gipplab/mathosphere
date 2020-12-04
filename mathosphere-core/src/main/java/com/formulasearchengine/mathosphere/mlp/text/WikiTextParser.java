package com.formulasearchengine.mathosphere.mlp.text;
/**
 * Copyright 2011 The Open Source Research Group, University of Erlangen-Nürnberg <p> Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at <p> http://www.apache.org/licenses/LICENSE-2.0
 * <p> Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.utils.sweble.MlpConfigEnWpImpl;
import com.google.common.collect.Multiset;
import de.fau.cs.osr.ptk.common.AstVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.parser.nodes.*;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A visitor to convert an article AST into a pure text representation. To better understand the
 * visitor pattern as implemented by the Visitor class, please take a look at the following
 * resources: <ul> <li><a href="http://en.wikipedia.org/wiki/Visitor_pattern">http://en.wikipedia
 * .org/wiki/Visitor_pattern</a> (classic pattern)</li> <li><a href="http://www.javaworld.com/javaworld/javatips/jw-javatip98.html">http
 * ://www.javaworld.com/javaworld/javatips/jw-javatip98.html</a> (the version we use here)</li>
 * </ul>
 * <p>
 * The methods needed to descend into an AST and visit the children of a given node <code>n</code>
 * are <ul> <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code> and gather the return
 * values of the <code>visit()</code> calls in a list,</li> <li><code>mapInPlace(n)</code> - visit
 * the <b>children</b> of node <code>n</code> and replace each child node <code>c</code> with the
 * return value of the call to <code>visit(c)</code>.</li> </ul>
 * </p>
 * <p>
 *     The source code of sweble wikitext extension can be found here:<br>
 *     <a href="https://github.com/sweble/sweble-wikitext">https://github.com/sweble/sweble-wikitext</a><br><br>
 *     A complete list of AST wikitext nodes can be found here: <br>
 *     <a href="https://github.com/sweble/sweble-wikitext/blob/develop/sweble-wikitext-components-parent/swc-engine/doc/ast-nodes.txt">
 *         sweble-wikitext/sweble-wikitext-components-parent/swc-engine/doc/ast-nodes.txt</a>
 * </p>
 */
@SuppressWarnings("unused")
//@SuppressWarnings("all")
public class WikiTextParser extends AstVisitor<WtNode> {
    private static final Logger LOG = LogManager.getLogger(WikiTextParser.class.getName());

    private final static Pattern MATH_ONLY_END_PATTERN = Pattern.compile("^\\s*([.,;!?]+)\\s*$");
    private final static Pattern MATH_END_PATTERN = Pattern.compile("^\\s*(.*)\\s*([\"'`´.,;!?]+)\\s*$");
    private static final Pattern SUB_PATTERN = Pattern.compile("[{<]sub[}>](.+?)[{<]/sub[}>]");
    private static final Pattern MATH_IN_TEXT_SEQUENCE_PATTERN = Pattern.compile(
            "(?<=^|[^A-Za-z]|)[\\p{InGreek}\\p{N}\\p{Ps}\\p{Pe}\\p{Pd}\\p{Sm}\\p{Sk}\\p{Po} ]+(?=[^A-Za-z]|$)"
    );
    private static final Pattern MATH_SPECIAL_CHAR_PATTERN = Pattern.compile(
            "[^\\p{InGreek}\\p{N}]*[\\p{P}\\p{Sm}][^\\p{InGreek}\\p{N}]*"
    );
    private final static Pattern MML_TOKENS_PATTERN = Pattern.compile(
            "<(?:m[a-z]+|apply|c[in]|csymbol|semantics)>"
    );

    private final static Pattern FORMULA_PATTERN = Pattern.compile(
            "FORMULA_[A-Za-z0-9]+"
    );

    private final static WikiConfig CONFIG = MlpConfigEnWpImpl.generate();
    private final static WtEngineImpl ENGINE = new WtEngineImpl(CONFIG);

    private final EngProcessedPage page;

    private int extLinkNum;
    private WikidataLinkMap wl = null;

    // just for better error handling
    private final PageTitle pageTitle;
    private String texInfoUrl;

    private boolean skipHiddenMath;

//    private final RawText rawText;

    private DocumentMetaLib metaLib;
    private MathTag previousMathTag;
    private String previousMathTagEnding;

    private final LinkedList<String> sections;

    boolean innerMathMode = false;

    public WikiTextParser(RawWikiDocument wikiDoc, BaseConfig config) throws LinkTargetException, EngineException {
        this(wikiDoc, config, new DocumentMetaLib(config));
    }

    public WikiTextParser(RawWikiDocument wikidoc, BaseConfig config, DocumentMetaLib metaLib) throws LinkTargetException, EngineException {
        this(wikidoc);
        if (config.getWikiDataFile() != null) {
            this.wl = new WikidataLinkMap(config.getWikiDataFile());
        } else this.wl = null;
        this.texInfoUrl = config.getTexvcinfoUrl();
        this.metaLib = metaLib == null ? new DocumentMetaLib(config) : metaLib;
    }

    public WikiTextParser(String partialWikiDoc) throws LinkTargetException, EngineException {
        this(new RawWikiDocument("unknown-title", -1, partialWikiDoc));
    }

    public WikiTextParser(RawWikiDocument wikidoc) throws LinkTargetException, EngineException {
        this.pageTitle = PageTitle.make(CONFIG, wikidoc.getTitle());
        PageId pageId = new PageId(pageTitle, -1);
        String content = preProcess(wikidoc.getContent());
        this.page = ENGINE.postprocess(pageId, content, null);
        this.texInfoUrl = (new BaseConfig()).getTexvcinfoUrl();
//        this.rawText = new RawText();
        this.metaLib = new DocumentMetaLib(new BaseConfig());
        this.sections = new LinkedList<>();
    }

    public DocumentMetaLib getMetaLibrary() {
        return metaLib;
    }

    /**
     * Process only the tags without to generate text output.
     * Suppressing the output might be useful if one is only interested in the math tags and not in the text.
     * In that case this option speeds up the process
     */
    public void processTags() {
//        this.rawText.suppressOutput(true);
        this.parse();
//        this.rawText.suppressOutput(false);
    }

    public List<String> parse() {
        try {
            this.sections.clear();
            Object result = this.go(page.getPage());
            if ( sections.isEmpty() ) {
                if ( result instanceof String ) {
                    List<String> tmp = new LinkedList<>();
                    tmp.add((String)result);
                    return tmp;
                } else {
                    LOG.error("Unable to create result. Result was: " + result);
                    return null;
                }
            } else return this.sections;
        } catch (Exception e) {
            LOG.error("Error parsing page " + this.pageTitle, e);
            List<String> txt = new LinkedList<>();
            txt.add("");
            return txt;
        }
    }

    public void setSkipHiddenMath(boolean skipHiddenMath) {
        this.skipHiddenMath = skipHiddenMath;
    }

    // =========================================================================
    // Time for all visitation methods

    @Override
    protected WtNode before(WtNode node) {
        // This method is called by go() before visitation starts
        this.extLinkNum = 1;
        this.previousMathTag = null;
        this.previousMathTagEnding = null;
        return super.before(node);
    }

    /**
     * This method is called by go() after visitation has finished
     * The return value will be passed to go() which passes it to the caller
     * @param node the node to parse
     * @param result the returned result (should be of type string)
     * @return {@link RawText}
     */
    @Override
    protected Object after(WtNode node, Object result) {
        return result;
    }

    private String iterateString(WtNode node) {
        return iterateString(node, " ");
    }

    private String iterateString(WtNode node, String betweenElements) {
        return iterateString(node, betweenElements, false);
    }

    private String iterateString(WtNode node, String betweenElements, boolean resetMath) {
        StringBuilder sb = new StringBuilder();
        if (node == null) return "";
        for (WtNode n : node) {
            Object o = dispatch(n);
            addLatestElement(o, sb, betweenElements, resetMath);
        }
        return sb.toString();
    }

    private void addLatestElement(Object o, StringBuilder sb, String betweenElements, boolean resetMath) {
        if ( o instanceof String && !((String)o).isBlank()) {
            sb.append(((String)o).trim());
        } else if ( o == null ) {
            LOG.warn("A node returned null, miss implementation? Node: " + o);
        }

        if ( resetMath )
            resetPreviousMath(sb);
        if ( !betweenElements.isEmpty() && (sb.lastIndexOf(betweenElements) != (sb.length()-1) ) )
            sb.append(betweenElements);
    }

    /**
     * Entry point of parse method...
     */
    public String visit(EngPage p) {
        // just in case there were a page before... yeah... i know...
        StringBuilder sb = new StringBuilder();

        boolean preSection = true;
        for ( WtNode n : p ) {
            if ( preSection && n instanceof WtSection ) {
                this.sections.add(sb.toString());
                preSection = false;
            }

            Object o = dispatch(n);
            addLatestElement(o, sb, " ", true);
        }

        // flush math, if any
        resetPreviousMath(sb);
        return sb.toString();
    }

    /**
     * If there are sections, we will visit this node. It will not only return the text of the
     * current section but also (more importantly) fill up {@link #sections}.
     * @param s the section
     * @return the string section
     */
    public String visit(WtSection s) {
        StringBuilder sb = new StringBuilder();

        boolean finishedOnSection = false;
        for ( WtNode n : s.getBody() ) {
            if ( n instanceof WtSection ) {
                resetPreviousMath(sb);
                if ( !sb.toString().isBlank() ) {
                    sections.add(sb.toString());
                    sb = new StringBuilder();
                }
                String innerSec = (String)dispatch((WtSection)n);
                // since that's a section, it were already added to the list of sections
                // so nothing to do here.
                finishedOnSection = true;
                continue;
            }

            Object o = dispatch(n);
            addLatestElement(o, sb, " ", true);
            finishedOnSection = false;
        }

        resetPreviousMath(sb);
        if ( !sb.toString().isBlank() && !finishedOnSection )
            sections.add(sb.toString());
        return sb.toString();
    }

    public String visit(WtParagraph p) {
        StringBuilder sb = new StringBuilder();
        return resetPreviousMath(sb).append(iterateString(p, " ")).toString();
    }

    public String visit(WtNodeList n) {
        return iterateString(n, " ", true);
    }

    public String visit(WtDefinitionList list) {
        return iterateString(list, " ", true);
    }

    public String visit(WtDefinitionListDef d) {
        return iterateString(d, " ");
    }

    public String visit(WtDefinitionListTerm t) {
        return iterateString(t, " ");
    }

    public String visit(WtUnorderedList e) {
        return iterateString(e);
    }

    public String visit(WtOrderedList e) {
        return iterateString(e);
    }

    public String visit(WtListItem item) {
        return iterateString(item, "\n");
    }

    public String visit(WtText text) {
        if ( text.getContent().isBlank() ) return "";

        if ( innerMathMode ) return text.getContent(); // lol

        StringBuilder stringBuilder = new StringBuilder();
        Matcher m = MATH_IN_TEXT_SEQUENCE_PATTERN.matcher(text.getContent().trim());
        int previousHitPosition = 0;
        while (m.find()) {
            int startIdx = m.start();

            Matcher onlySpecialMatcher = MATH_SPECIAL_CHAR_PATTERN.matcher(m.group(0));
            if ( (onlySpecialMatcher.matches() && previousMathTag == null) || m.group(0).isBlank()) {
                previousHitPosition = m.end()+1;
                m.appendReplacement(stringBuilder, m.group(0));
                continue;
            }

            // first version, there was text between new hit and previous hit
            // but the previous math tag was not added yet
            if ( startIdx > previousHitPosition && previousMathTag != null ) {
                // well, there were actually text, before we hit first... so
                // time to reset previous hit...
                resetPreviousMath(stringBuilder, false);

                if ( onlySpecialMatcher.matches() ) {
                    // so we have new math... but if this new math is just a special character and there
                    // is no previous math now (and there is no, we just reset it), we have to do the same
                    // as before and just add it as normal math...
                    previousHitPosition = m.end()+1;

                    StringBuilder tmp = new StringBuilder();
                    m.appendReplacement(tmp, m.group(0));
                    if ( stringBuilder.lastIndexOf(" ") != stringBuilder.length()-1
                            && tmp.charAt(0) != ' ') {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append(tmp);
                    continue;
                }
            }

            // ok, but wait... what if the previous ending is null and this just matches
            // an ending: Then we dont need to update anything, just set the ending.
            Matcher endMatcher = MATH_ONLY_END_PATTERN.matcher(m.group(0));
            if ( endMatcher.matches() && previousMathTagEnding == null) {
                // haha, indeed... just an ending and there is no previous ending... nice
                previousMathTagEnding = endMatcher.group(1);
                previousHitPosition = m.end();
                m.appendReplacement(stringBuilder, "");
                continue;
            }

            // otherwise we update or create a new math tag (if or if not previous Math exists
            String potentialMath = fixMathInRow(m.group(0));
            updateOrCreateNewMath(potentialMath, WikiTextUtils.MathMarkUpType.LATEX);

            // and update our constraint
            previousHitPosition = m.end()+1;
            // nothing to add, we updated the previous hit.
            m.appendReplacement(stringBuilder, "");
        }

        // now it comes, if the string gets extended here, it means there was text after the previous
        // hit. in this case... we must add the math first, if any and then add the end.
        StringBuilder tmpBuilder = new StringBuilder();
        m.appendTail(tmpBuilder);
        String tmpString = tmpBuilder.toString();
        // last test is this bullshit &nbsp; char
        if ( tmpBuilder.length() > 0 && !tmpString.isBlank() && !tmpString.matches("\\s* \\s*")) {
            resetPreviousMath(stringBuilder);
            stringBuilder.append(tmpBuilder);
        }
        return stringBuilder.toString();
    }

    public String visit(WtWhitespace w) {
        // white space alone does not split math... so we dont update previous MathTag if any
        return " ";
    }

    public String visit(WtBold b) {
        return handleItalicBold(b);
    }

    public String visit(WtItalics i) {
        return handleItalicBold(i);
    }

    private String handleItalicBold(WtNode n) {
        StringBuilder sb = new StringBuilder();
        String detectedTex = checkItalicBoldForMath(n);
        if ( detectedTex != null ) {
            if ( innerMathMode ) {
                return detectedTex;
            } else {
                return updateOrCreateNewMath(detectedTex, WikiTextUtils.MathMarkUpType.LATEX);
            }
        } else {
            return resetPreviousMath(sb)
                    .append("\"").append(iterateString(n).trim()).append("\"")
                    .toString();
        }
    }

    private String checkItalicBoldForMath(WtNode n) {
        if ( n.get(0) instanceof WtInternalLink ) {
            WtInternalLink in = (WtInternalLink)n.get(0);
            WtNode title = in.getTitle();
            if ( title == null || title.size() == 0) title = in.getTarget();
            if ( title != null && title.size() != 0) {
                return detectHiddenMath(title);
            }
        }
        return detectHiddenMath(n);
    }

    public String visit(WtXmlCharRef cr) {
        return new String(Character.toChars(cr.getCodePoint()));
    }

    public String visit(WtXmlEntityRef er) {
        // I think, this should not happen if somebody really unescaped XML... so lets warn the user
        LOG.warn("WikiTextParser assumes unescaped XML input but found " + er.getResolved());

        String ch = er.getResolved();
        return Objects.requireNonNullElseGet(ch, () -> "&" + er.getName() + ";");
    }

    // =========================================================================
    // Stuff we want to hide

    public String visit(WtUrl wtUrl) {
        LOG.debug("Hide URL: " + wtUrl.getPath());
        StringBuilder sb = new StringBuilder();
        return resetPreviousMath(sb).toString();
    }

    public String visit(WtExternalLink link) {
        StringBuilder sb = new StringBuilder();
        resetPreviousMath(sb).append("[").append(extLinkNum).append("]");
        extLinkNum++;
        return sb.toString();
    }

    public String visit(WtInternalLink link) {
        StringBuilder sb = new StringBuilder();
        resetPreviousMath(sb);

        String linkName = link.getTarget().getAsString().split("#")[0];

        if (wl != null) {
            String newName = wl.title2Data(linkName);
            if (newName != null) {
                WikidataLink wikiLink = new WikidataLink(newName);
                sb.append(wikiLink.placeholder());
                metaLib.addLink(wikiLink);
                return sb.toString();
            }
        }

        WikidataLink wl = new WikidataLink(linkName);
        sb.append(wl.placeholder());

        if (link.getTitle().size() > 0) {
            String title = iterateString(link.getTitle(), " ");
            wl.setTitle(title);
        }

        metaLib.addLink(wl);
        return sb.toString();
    }

    public String visit(WtXmlElement e) {
        StringBuilder sb = new StringBuilder();
        String name = e.getName();
        boolean sup = true;
        switch ( name.toLowerCase() ) {
            case "br":
                // ignore but reset math if there was any before
                return resetPreviousMath(sb).toString();
            case "var":
                WtNode contentNode = e.getBody().get(0);
                Object objContent = dispatch(contentNode);
                if ( objContent instanceof String ) {
                    String content = fixMathInRow((String)objContent);
                    return updateOrCreateNewMath(content, WikiTextUtils.MathMarkUpType.LATEX);
                }
                break;
            case "text":
                return iterateString(e.getBody(), " ");
            case "sub":
                sup = false;
            case "sup":
                WtNode bodyNode = e.getBody();
                String contentStr = fixMathInRow(handleSuccessiveSubSups(bodyNode));
                sb.append(sup ? "^{" : "_{");
                sb.append(contentStr).append("}");
                return updateOrCreateNewMath(sb.toString(), WikiTextUtils.MathMarkUpType.LATEX);
            case "math":
                LOG.warn("It seems <math> was not registered as special treatment. " +
                        "Check config of WikiTextParser.");
                break;
            case "span":
            case "p":
                return (String)dispatch(e.getBody());
            default:
                LOG.warn("Encountered unknown XML tag: " + name + ". Try to ignore it and parse the body of it.");
                return (String)dispatch(e.getBody());
        }
        return sb.toString();
    }

    private String handleSuccessiveSubSups(WtNode element) {
        innerMathMode = true;
        StringBuilder content = new StringBuilder();
        for (WtNode e : element) {
            if (e.size() > 1) {
                content.append(handleSuccessiveSubSups(e));
                continue;
            }
            content.append(dispatch(e));
        }
        innerMathMode = false;
        return content.toString();
    }

    public String visit(WtTemplate n) {
        try {
            StringBuilder sb = new StringBuilder();
            WtTemplateArgument arg0;
            WikiCitation cite;
            String name = n.getName().getAsString();

            if ( name.toLowerCase().startsWith("equation") ) {
                return handleEquationTemplate(n);
            }

            int mathElementIndex = 0;
            boolean sub = false;

            switch (name.toLowerCase()) {
                case "numblk":
                    arg0 = (WtTemplateArgument) n.getArgs().get(1);
                    // there might be idiots who use num bulk just for text.. who knows man
                    String numBlkArg = iterateString(arg0.getValue());
                    sb.append(numBlkArg);
                    resetPreviousMath(sb);
                    return sb.toString();
                case "math":
                case "mvar":
                    arg0 = (WtTemplateArgument) n.getArgs().get(mathElementIndex);
                    return handleMathTemplateArgument(arg0, WikiTextUtils.MathMarkUpType.MATH_TEMPLATE);
                case "pi":
                    return updateOrCreateNewMath("\\pi", WikiTextUtils.MathMarkUpType.LATEX);
                case "=":
                    return updateOrCreateNewMath(" = ", WikiTextUtils.MathMarkUpType.LATEX);
                case "su":
                    return updateOrCreateNewMath(handleSuElement(n.getArgs()), WikiTextUtils.MathMarkUpType.LATEX);
                case "sub":
                    sub = true;
                case "sup":
                    arg0 = (WtTemplateArgument) n.getArgs().get(0);
                    Object res = dispatch(arg0.getValue());
                    if ( res instanceof String ) {
                        String content = fixMathInRow((String)res);
                        if ( sub ) content = "_{"+content+"}";
                        else content = "^{"+content+"}";
                        return content;
                    } else {
                        LOG.error("Unable to parse " + n);
                        break;
                    }
                case "abs":
                    arg0 = (WtTemplateArgument) n.getArgs().get(0);
                    Object absValObj = dispatch(arg0.getValue());
                    if ( absValObj instanceof String ) {
                        String content = fixMathInRow((String)absValObj);
                        return "\\left|" + content + "\\right|";
                    } else {
                        LOG.error("Unable to parse " + n);
                        break;
                    }
                case "Citation":
                    resetPreviousMath(sb);
                    cite = new WikiCitation(n.toString());
                    metaLib.addCite(cite);
                    // we don't add this to the text
                    return sb.toString();
                case "dlmf":
                    resetPreviousMath(sb);
                    cite = new WikiCitation("dlmf", n.toString());
                    metaLib.addCite(cite);
                    // we don't add this to the text
                    return sb.toString();
                case "nowrap":
                    arg0 = (WtTemplateArgument) n.getArgs().get(0);
                    return iterateString(arg0.getValue());
                case "short description":
                case "for":
                case "use american english":
                default:
                    LOG.warn("Template gets ignored: " + name);
                    return resetPreviousMath(sb).toString();
            }
            return "";
        } catch (Exception e) {
            LOG.info("Problem prcessing page" + pageTitle.getTitle(), e);
            return "";
        }
    }

    private String handleSuElement(WtTemplateArguments args) {
        String sub = "", sup = "";
        for ( int i = 0; i < args.size(); i++ ) {
            WtTemplateArgument arg = (WtTemplateArgument)args.get(i);
            String key = arg.getName().getAsString();
            Boolean subKey = null;
            if ( key.equals("b") ) {
                subKey = true;
            } else if ( key.equals("p") ) {
                subKey = false;
            }

            if ( subKey != null ) {
                String c = ((WtText)arg.getValue().get(0)).getContent();
                c = fixMathInRow(c);
                if ( subKey ) sub = "_{"+c+"}";
                else sup = "^{"+c+"}";
            }
        }
        return sub+sup;
    }

    private String handleMathTemplateArgument(WtTemplateArgument arg, WikiTextUtils.MathMarkUpType type) {
        StringBuilder sb = new StringBuilder();
        innerMathMode = true;
        for ( WtNode n : arg.getValue() ) {
            if ( n instanceof WtText ) {
                sb.append(fixMathInRow(((WtText) n).getContent()));
            } else if ( n instanceof WtTemplate ) {
                sb.append(visit((WtTemplate)n));
                if ( previousMathTag != null ) {
                    String content = previousMathTag.getContent();
                    sb.append(content);
                    if ( previousMathTagEnding != null ) {
                        sb.append(previousMathTagEnding);
                        previousMathTagEnding = null;
                    }
                    // is this in SB already? I dont think so
                    previousMathTag = null;
                }
            } else {
                sb.append(dispatch(n));
            }
        }
        innerMathMode = false;

        // well, math templates are always unique and should not be merged!
        MathTag math = new MathTag(sb.toString(), WikiTextUtils.MathMarkUpType.LATEX);
        metaLib.addFormula(math);
        return " " + math.placeholder() + " ";
    }

    public String visit(WtTemplateArgument n) {
        LOG.error("Parsing a template argument? That shouldn't happen. We handle templates differently.");
        return "";
    }

    public String visit(WtTagExtension n) {
        boolean chem = false;
        StringBuilder sb = new StringBuilder();
        switch (n.getName()) {
            case "ce":
            case "chem":
                chem = true;
            case "math":
                WikiTextUtils.MathMarkUpType markUpType;
                if (chem) {
                    markUpType = WikiTextUtils.MathMarkUpType.LATEXCE;
                } else if ( hasMathMLNamespaceAttribute(n.getXmlAttributes()) || hasMathMLTokens(n.getBody()) ) {
                    markUpType = WikiTextUtils.MathMarkUpType.MATHML;
                } else {
                    markUpType= WikiTextUtils.MathMarkUpType.LATEX;
                }

                String content = n.getBody().getContent();
                switch ( markUpType ) {
                    case LATEXCE:
                        return updateOrCreateNewMath(fixMathInRow(content), markUpType);
                    case LATEX:
                        content = fixMathInRow(content);
                    case MATHML:
                        resetPreviousMath(sb);
                        updateOrCreateNewMath(content, markUpType);
                        resetPreviousMath(sb);
                        return sb.toString();
                }
            case "ref":
                resetPreviousMath(sb);
                String attribute = "";
                if ( n.getXmlAttributes().size() > 0 ) {
                    boolean wasNameBefore = false;
                    for (WtNode wtNode : n.getXmlAttributes()) {
                        WtXmlAttribute att = (WtXmlAttribute) wtNode;
                        if (att.getName().getAsString().equals("name")) {
                            if ( !att.getValue().isEmpty() )
                                attribute = ((WtText) att.getValue().get(0)).getContent();
                            else {
                                wasNameBefore = true;
                                continue;
                            }
                            break;
                        } else if ( wasNameBefore ) {
                            attribute = att.getName().getAsString();
                            break;
                        }
                        wasNameBefore = false;
                    }
                }
                // we ignore citations in the text. They are just stored as meta information
                addCitation(n.getBody().toString(), attribute);
                break;
            default:
                resetPreviousMath(sb);
                LOG.warn("Unknown tag extension we cannot handle.");
        }
        return sb.toString();
    }

    // =====================================================================
    // We ignore some node types. Here they are:

    public String visit(WtNode n) {
        // Fallback for all nodes that are not explicitly handled elsewhere
        // if there is something in between, we need to reset math
        LOG.debug("Ignore node: " + n);
        StringBuilder sb = new StringBuilder();
        return resetPreviousMath(sb).toString();
    }

    /**
     * The functions below should be ignored. So they return an empty string.
     * By not implementing these methods, we take advantage of the fallback
     * method above for a general WtNode.
     */
//    public void visit(WtTemplateParameter n) {}
//
//    public void visit(WtPageSwitch n) {}
//
//    public void visit(WtIllegalCodePoint n) {}
//
//    public void visit(WtXmlComment n) {}
//
//    public void visit(WtHorizontalRule hr) {}
//
//    public void visit(WtNewline n) {}


//    public void visit(WtImageLink n) {
//        LOG.debug("We ignore image links.");
//        iterate(n.getTitle());
//    }
//
//    public void visit(WtTable b) {
//        iterate(b.getBody());
//    }
//
//    public void visit(WtTableRow b) {
//        iterate(b);
//    }
//
//    public void visit(WtTableCell b) {
//        iterate(b);
//    }
//
//    public void visit(WtTableImplicitTableBody b) {
//        iterate(b);
//    }
//
//    public void visit(WtTableHeader b) {
//        iterate(b);
//    }
//
//    public void visit(WtTableCaption b) {
//        iterate(b);
//    }

    // =====================================================================
    // And some class specific helper methods.

    public String addCitation(String content, String attribute) {
        WikiCitation cite = new WikiCitation(
                attribute,
                content
        );

        metaLib.addCite(cite);
        return cite.placeholder();
    }

    private StringBuilder resetPreviousMath(StringBuilder sb) {
        return resetPreviousMath(sb, true);
    }

    private StringBuilder resetPreviousMath(StringBuilder sb, boolean endingSpace) {
        if ( previousMathTag != null ) {
            if ( sb.lastIndexOf(" ") != (sb.length()-1) && sb.length() > 0 )
                sb.append(" ");
            sb.append(previousMathTag.placeholder());
            if (previousMathTagEnding != null) {
                sb.append(previousMathTagEnding);
                previousMathTagEnding = null;
            }
            metaLib.addFormula(previousMathTag);
            previousMathTag = null;
            if ( endingSpace )
                sb.append(" ");
        }
        return sb;
    }

    private String updateOrCreateNewMath(String content, WikiTextUtils.MathMarkUpType type) {
        if ( innerMathMode ) return content;

        Matcher endingMatcher = MATH_END_PATTERN.matcher(content);
        String newEndingString = null;
        if ( endingMatcher.matches() ){
            content = endingMatcher.group(1);
            newEndingString = endingMatcher.group(2);
        }

        if ( previousMathTag != null ) {
            if ( previousMathTagEnding != null ) {
                previousMathTag.extendContent(previousMathTagEnding);
                previousMathTagEnding = null;
            }

            previousMathTag.extendContent(content);
        } else {
            previousMathTag = new MathTag(content, type);
        }

        if ( newEndingString != null )
            previousMathTagEnding = newEndingString;
        return "";
    }

    private String getTex(WtNode i, boolean force) {
        if (i.get(0) instanceof WtText) {
            String content = ((WtText) i.get(0)).getContent().trim();
            String tex = replaceClearMath(content);
            if (tex.length() == 1 || !content.equals(tex)) {
                // it triggered a math replacement... so first we have to fix unicode, if exists
                String unicodeTex = replaceMathUnicode(tex);
                Multiset<String> idents;
                try {
                    idents = TexInfo.getIdentifiers(unicodeTex, texInfoUrl);
                } catch (XPathExpressionException | ParserConfigurationException | IOException
                        | SAXException | TransformerException ignored) {
                    return null;
                }
                // a single UTF character should always be interpreted as math, no?
                if ( tex.length() == 1 && !tex.equals(unicodeTex) ){
                    return unicodeTex;
                }
                if (idents.size() == 0 && !force) {
                    return null;
                }
                if (i instanceof WtBold) {
                    unicodeTex = "\\mathbf{" + unicodeTex + "}";
                }
                return unicodeTex;
            }
            if (force) {
                return tex;
            }
        }
        return null;
    }

    private String handleEquationTemplate(WtTemplate equation) {
        WtTemplateArguments args = equation.getArgs();
        WtTemplateArgument titleArg = null, equationArg = null;
        for (WtNode wtNode : args) {
            WtTemplateArgument arg = (WtTemplateArgument) wtNode;
            if ( arg.getName().size() < 1 ) continue;
            String name = arg.getName().getAsString();
            if ( "title".equals(name) ) {
                titleArg = arg;
            } else if ( "equation".equals(name) ) {
                equationArg = arg;
            }
        }

        StringBuilder sb = new StringBuilder();
        if ( titleArg != null ) {
            sb.append(dispatch(titleArg.getValue()));
            sb.append(": ");
        }

        if ( equationArg != null ) {
            sb.append(dispatch(equationArg.getValue()));
            resetPreviousMath(sb);
        }
        return sb.toString();
    }

    private String detectHiddenMath(WtNode i) {
        if (skipHiddenMath){
            return null;
        }

        if (i.size() == 1 && i.get(0) instanceof WtText) {
            return getTex(i, false);
        } else {
            if (i.size() == 2 && i.get(0) instanceof WtText && i.get(1) instanceof WtXmlElement) {
                //discover hidden subscripts
                final WtXmlElement xml = (WtXmlElement) i.get(1);
                if (xml.getName().matches("sub") &&
                        xml.getBody().size() == 1 &&
                        xml.getBody().get(0) instanceof WtText) {
                    final String subTex = getTex(xml.getBody(), true);
                    final String mainTex = getTex(i, true);
                    if (mainTex != null) {
                        String tex = mainTex + "_{" + subTex + "}";
                        return tex;
                    }
                }
            }
        }
        return null;
    }

    // ====================================================================
    // And of course we need some static stuff to handle typical procedures

    public static String getContentFromMathTemplate(WtTemplateArgument arg) {
        String content = "";
        for ( int i = 0; i < arg.getValue().size(); i++ ){
            WtNode node = arg.getValue().get(i);
            if ( node instanceof WtText) {
                content += ((WtText) node).getContent().trim();
            } else if ( node instanceof WtTemplate) {
                content += innerMathTemplateReplacement((WtTemplate)node);
            } else {
                LOG.warn("Ignore unknown node within math template: " + node.toString());
            }
        }
        return content;
    }

    public static String innerMathTemplateReplacement(WtTemplate t) {
        String name = t.getName().getAsString();
        WtTemplateArgument arg;
        String result = "";
        switch (name.toLowerCase()) {
            case "pi":
                result = " \\pi ";
                break;
            case "=":
                result = " = ";
                break;
            case "su":
                WtTemplateArguments args = t.getArgs();
                String sub = "", sup = "";
                for ( int i = 0; i < args.size(); i++ ) {
                    arg = (WtTemplateArgument)args.get(i);
                    String key = arg.getName().getAsString();
                    Boolean subKey = null;
                    if ( key.equals("b") ) {
                        subKey = true;
                    } else if ( key.equals("p") ) {
                        subKey = false;
                    }

                    if ( subKey != null ) {
                        String c = ((WtText)arg.getValue().get(0)).getContent();
                        if ( subKey ) sub = "_{"+c+"}";
                        else sup = "^{"+c+"}";
                    }
                }
                result = sub+sup;
                break;
            case "sub":
                arg = (WtTemplateArgument) t.getArgs().get(0);
                result = ((WtText)arg.getValue().get(0)).getContent();
                result = "_{"+result+"}";
                break;
            case "sup":
                arg = (WtTemplateArgument) t.getArgs().get(0);
                result = ((WtText)arg.getValue().get(0)).getContent();
                result = "^{"+result+"}";
                break;
            default: return null;
        }
        return result;
    }

    private static boolean hasMathMLNamespaceAttribute(WtXmlAttributes attributes) {
        for (WtNode attribute : attributes) {
            WtXmlAttribute att = (WtXmlAttribute) attribute;
            String name = att.getName().getAsString();
            if ("xmlns".equals(name.toLowerCase())) {
                // ok its xmlns, lets check if its actually mathml
                String value = ((WtText) att.getValue().get(0)).getContent();
                if ("http://www.w3.org/1998/Math/MathML".equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasMathMLTokens(WtTagExtensionBody body) {
        String mightBeMathML = body.getContent();
        Matcher mmlMatcher = MML_TOKENS_PATTERN.matcher(mightBeMathML);
        return mmlMatcher.find();
    }

    public static String fixMathInRow(String content) {
        content = replaceClearMath(content);
        return replaceMathUnicode(content);
    }

    public static String replaceClearMath(String content) {
        return SUB_PATTERN.matcher(content)
                .replaceAll("_{$1}")
                .replaceAll("[{<]sup[}>](.+?)[{<]/sup[}>]", "^{$1}")
                .replaceAll("'''\\[{0,2}(\\S)]{0,2}'''", "\\\\mathbf{$1}")
                .replaceAll("''\\[{0,2}(\\S)]{0,2}''", "$1");
    }

    public static String replaceMathUnicode(String content) {
        return UnicodeMap.string2TeX(content);
    }

    private static final Pattern NOWRAP_PATTERN = Pattern.compile(
            "\\{{2}nowrap\\|(?:\\d+=)?([^{]*?)}{2}"
    );

    public static String preProcess(String input) {
        // the nowrap template is always WtText... even if there is ''x'' or even XML like <sub>
        // are just in WtText which simply breaks EVERYTHING... Simple idea... delete the nowrap shit.
        StringBuilder sb = new StringBuilder();
        Matcher m = NOWRAP_PATTERN.matcher(input);
        while ( m.find() ) {
            m.appendReplacement(sb, m.group(1));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

