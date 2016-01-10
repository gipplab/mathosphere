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

import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.jcabi.log.Logger;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.utils.StringUtils;

import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngPage;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.nodes.WtBold;
import org.sweble.wikitext.parser.nodes.WtContentNode.WtContentNodeImpl;
import org.sweble.wikitext.parser.nodes.WtExternalLink;
import org.sweble.wikitext.parser.nodes.WtHorizontalRule;
import org.sweble.wikitext.parser.nodes.WtIllegalCodePoint;
import org.sweble.wikitext.parser.nodes.WtImageLink;
import org.sweble.wikitext.parser.nodes.WtInternalLink;
import org.sweble.wikitext.parser.nodes.WtItalics;
import org.sweble.wikitext.parser.nodes.WtListItem;
import org.sweble.wikitext.parser.nodes.WtNewline;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtOrderedList;
import org.sweble.wikitext.parser.nodes.WtPageSwitch;
import org.sweble.wikitext.parser.nodes.WtParagraph;
import org.sweble.wikitext.parser.nodes.WtSection;
import org.sweble.wikitext.parser.nodes.WtTable;
import org.sweble.wikitext.parser.nodes.WtTableCaption;
import org.sweble.wikitext.parser.nodes.WtTableCell;
import org.sweble.wikitext.parser.nodes.WtTableHeader;
import org.sweble.wikitext.parser.nodes.WtTableImplicitTableBody;
import org.sweble.wikitext.parser.nodes.WtTableRow;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTemplate;
import org.sweble.wikitext.parser.nodes.WtTemplateArgument;
import org.sweble.wikitext.parser.nodes.WtTemplateParameter;
import org.sweble.wikitext.parser.nodes.WtText;
import org.sweble.wikitext.parser.nodes.WtUnorderedList;
import org.sweble.wikitext.parser.nodes.WtUrl;
import org.sweble.wikitext.parser.nodes.WtWhitespace;
import org.sweble.wikitext.parser.nodes.WtXmlCharRef;
import org.sweble.wikitext.parser.nodes.WtXmlComment;
import org.sweble.wikitext.parser.nodes.WtXmlElement;
import org.sweble.wikitext.parser.nodes.WtXmlEntityRef;
import org.sweble.wikitext.parser.parser.LinkTargetException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * A visitor to convert an article AST into a pure text representation. To better understand the
 * visitor pattern as implemented by the Visitor class, please take a look at the following
 * resources: <ul> <li><a href="http://en.wikipedia.org/wiki/Visitor_pattern">http://en.wikipedia
 * .org/wiki/Visitor_pattern</a> (classic pattern)</li> <li><a href="http://www.javaworld.com/javaworld/javatips/jw-javatip98.html">http
 * ://www.javaworld.com/javaworld/javatips/jw-javatip98.html</a> (the version we use here)</li>
 * </ul>
 *
 * The methods needed to descend into an AST and visit the children of a given node <code>n</code>
 * are <ul> <li><code>dispatch(n)</code> - visit node <code>n</code>,</li>
 * <li><code>iterate(n)</code> - visit the <b>children</b> of node <code>n</code>,</li>
 * <li><code>map(n)</code> - visit the <b>children</b> of node <code>n</code> and gather the return
 * values of the <code>visit()</code> calls in a list,</li> <li><code>mapInPlace(n)</code> - visit
 * the <b>children</b> of node <code>n</code> and replace each child node <code>c</code> with the
 * return value of the call to <code>visit(c)</code>.</li> </ul>
 */
public class MathConverter
    extends
    AstVisitor<WtNode> {
  private final static Pattern subMatch = Pattern.compile("[{<]sub[}>](.+?)[{<]/sub[}>]");
  private final static WikiConfig config = DefaultConfigEnWp.generate();
  private final static WtEngineImpl engine = new WtEngineImpl(config);
  private static final Pattern ws = Pattern.compile("\\s+");
  private final EngProcessedPage page;
  private List<MathTag> mathTags = new ArrayList<>();
  private List<WikidataLink> links = new ArrayList<>();
  private StringBuilder sb;
  private StringBuilder line;
  private int extLinkNum;
  private WikidataLinkMap wl = null;
  private static int i = 0;
  /**
   * Becomes true if we are no long at the Beginning Of the whole Document.
   */
  private boolean pastBod;
  private int needNewlines;
  private boolean needSpace;
  private boolean noWrap;
  private LinkedList<Integer> sections;
  private PageTitle pageTitle;
  private String texInfoUrl;

  public MathConverter(String wikiText, String name) throws LinkTargetException, EngineException {
    pageTitle = PageTitle.make(config, name);
    PageId pageId = new PageId(pageTitle, -1);
    page = engine.postprocess(pageId, wikiText, null);
    texInfoUrl = (new BaseConfig()).getTexvcinfoUrl();
  }

  public MathConverter(String wikiText) throws LinkTargetException, EngineException {
    this(wikiText, "noname");
  }

  public MathConverter(String wikitext, String title, BaseConfig config) throws LinkTargetException, EngineException {
    this(wikitext, title);
    if (config.getWikiDataFile() != null) {
      wl = new WikidataLinkMap(config.getWikiDataFile());
    } else {
      wl = null;
    }
    texInfoUrl = config.getTexvcinfoUrl();
  }

  public List<MathTag> getMathTags() {
    return mathTags;
  }

  // =========================================================================

  @Override
  protected boolean before(WtNode node) {
    // This method is called by go() before visitation starts
    sb = new StringBuilder();
    line = new StringBuilder();
    extLinkNum = 1;
    pastBod = false;
    needNewlines = 0;
    needSpace = false;
    noWrap = false;
    sections = new LinkedList<>();
    return super.before(node);
  }

  @Override
  protected Object after(WtNode node, Object result) {
    finishLine();

    // This method is called by go() after visitation has finished
    // The return value will be passed to go() which passes it to the caller
    return sb.toString();
  }

  // =========================================================================

  public void visit(WtNode n) {
    // Fallback for all nodes that are not explicitly handled below
//		System.out.println(n.getNodeName());
//		write("<");
//		write(n.getNodeName());
//		write(" />");
  }

  public void visit(WtNodeList n) {
    iterate(n);
  }

  public void visit(WtUnorderedList e) {
    iterate(e);
  }

  public void visit(WtOrderedList e) {
    iterate(e);
  }

  public void visit(WtListItem item) {
    writeNewlines(1);
    iterate(item);
  }

  public void visit(EngPage p) {
    iterate(p);
  }

  public void visit(WtText text) {
    write(text.getContent());
  }

  public void visit(WtWhitespace w) {
    write(" ");
  }

  public void visit(WtBold b) {
    if (detectHiddenMath(b)) {
      return;
    }
    write("\"");
    iterate(b);
    write("\"");
  }

  public void visit(WtItalics i) {
    if (detectHiddenMath(i)) return;
    write("\"");
    iterate(i);
    write("\"");
  }

  public boolean detectHiddenMath(WtNode i) {
    if (i.size() == 1 && i.get(0) instanceof WtText) {
      final String tex = getTex(i, false);
      if (tex != null) {
        int location;
        try {
          location = i.getLocation().line;
        } catch (NullPointerException n) {
          location = 0;
        }
        MathTag tag = new MathTag(location, tex, WikiTextUtils.MathMarkUpType.MATH_TEMPLATE);
        mathTags.add(tag);
        needSpace = true;
        writeWord(tag.placeholder());
        needSpace = true;
        return true;
      }
    } else {
      if (i.size() == 2 && i.get(0) instanceof WtText && i.get(1) instanceof WtXmlElement) {
        //discover hidden subscripts
        final WtXmlElement xml = (WtXmlElement) i.get(1);
        if (xml.getName().matches("sub") &&
            xml.getBody().size() == 1 &&
            xml.getBody().get(0) instanceof WtText) {
          //String subtext = ((WtText) ((WtXmlElement) i.get(1)).getBody().get(0)).getContent();
          final String subTex = getTex((WtContentNodeImpl) xml.getBody(), true);
          final String mainTex = getTex(i, true);
          if (mainTex != null) {
            String tex = mainTex + "_{" + subTex + "}";
            int location;
            try {
              location = i.getLocation().line;
            } catch (NullPointerException n) {
              location = 0;
            }
            MathTag tag = new MathTag(location, tex, WikiTextUtils.MathMarkUpType.MATH_TEMPLATE);
            mathTags.add(tag);
            needSpace = true;
            writeWord(tag.placeholder());
            needSpace = true;
            return true;
          }
        }
      }
    }
    return false;
  }

  public void visit(WtXmlCharRef cr) {
    write(Character.toChars(cr.getCodePoint()));
  }

  public void visit(WtXmlEntityRef er) {
    String ch = er.getResolved();
    if (ch == null) {
      write('&');
      write(er.getName());
      write(';');
    } else {
      write(ch);
    }
  }

  public void visit(WtUrl wtUrl) {
    if (!wtUrl.getProtocol().isEmpty()) {
      write(wtUrl.getProtocol());
      write(':');
    }
    write(wtUrl.getPath());
  }

  public void visit(WtExternalLink link) {
    write('[');
    write(extLinkNum++);
    write(']');
  }

  public void visit(WtInternalLink link) {
    String linkName = link.getTarget().getAsString().split("#")[0];
    if (wl != null) {
      String newName = wl.title2Data(linkName);
      if (newName != null) {
        write("LINK_" + newName);
        return;
      }
    }
    WikidataLink wl = new WikidataLink(linkName);
    write("LINK_" + wl.getContentHash());
    needSpace = true;
    if (link.getTitle().size() > 0) {
      StringBuilder tmp = this.line;
      this.line = new StringBuilder();
      iterate(link.getTitle());
      wl.setTitle(this.line.toString());
      this.line = tmp;
    }
    links.add(wl);

  }

  public void visit(WtSection s) {
    finishLine();
    StringBuilder saveSb = sb;
    boolean saveNoWrap = noWrap;

    sb = new StringBuilder();
    noWrap = true;

    iterate(s.getHeading());
    finishLine();
    String title = sb.toString().trim();

    sb = saveSb;

    if (s.getLevel() >= 1) {
      while (sections.size() > s.getLevel())
        sections.removeLast();
      while (sections.size() < s.getLevel())
        sections.add(1);

      StringBuilder sb2 = new StringBuilder();
      for (int i = 0; i < sections.size(); ++i) {
        if (i < 1)
          continue;

        sb2.append(sections.get(i));
        sb2.append('.');
      }

      if (sb2.length() > 0)
        sb2.append(' ');
      sb2.append(title);
      title = sb2.toString();
    }

    newline(2);
    write(title);
    newline(1);
    write(StringUtils.strrep('-', title.length()));
    newline(2);

    noWrap = saveNoWrap;
    try {
      // Don't care about errors
      iterate(s.getBody());
    } catch (Exception e) {
      Logger.info(e, "Problem prcessing page", pageTitle.getTitle());
      e.printStackTrace();
    }


    while (sections.size() > s.getLevel())
      sections.removeLast();
    sections.add(sections.removeLast() + 1);
  }

  public void visit(WtParagraph p) {
    iterate(p);
    newline(2);
  }

  public void visit(WtHorizontalRule hr) {
    newline(1);
    write(StringUtils.strrep('-', 10));
    newline(2);
  }

  public void visit(WtXmlElement e) {
    if (e.getName().equalsIgnoreCase("br")) {
      newline(1);
    } else {
      iterate(e.getBody());
    }
  }

  // =========================================================================
  // Stuff we want to hide

  public void visit(WtImageLink n) {
    iterate(n.getTitle());
  }

  public void visit(WtIllegalCodePoint n) {
  }

  public void visit(WtXmlComment n) {
  }

  public void visit(WtTable b) {
    iterate(b.getBody());
  }

  public void visit(WtTableRow b) {
    iterate(b);
  }

  public void visit(WtTableCell b) {
    iterate(b);
  }

  public void visit(WtTableImplicitTableBody b) {
    iterate(b);
  }

  public void visit(WtTableHeader b) {
    iterate(b);
  }

  public void visit(WtTableCaption b) {
    iterate(b);
  }

  public void visit(WtNewline n) {
    writeNewlines(1);
  }

  public void visit(WtTemplate n) {
    try {
      switch (n.getName().getAsString().toLowerCase()) {
        case "math":
          try {
            WtTemplateArgument arg0 = (WtTemplateArgument) n.getArgs().get(0);
            String content = ((WtText) arg0.getValue().get(0)).getContent().trim();
            content = TextExtractorMapper.unescape(content);
            //content = content.replaceAll("'''([a-zA-Z]+)'''","\\mathbf{$1}");
            content = wiki2Tex(content);
            MathTag tag = new MathTag(n.getLocation().line, content, WikiTextUtils.MathMarkUpType.MATH_TEMPLATE);
            mathTags.add(tag);
            needSpace = true;
            writeWord(tag.placeholder());
            needSpace = true;
          } catch (Exception ignored) {

          }
          break;
        case "mvar":
          try {
            WtTemplateArgument arg0 = (WtTemplateArgument) n.getArgs().get(0);
            String content = ((WtText) arg0.getValue().get(0)).getContent().trim();
            content = wiki2Tex(content);
            MathTag tag = new MathTag(n.getLocation().line, content, WikiTextUtils.MathMarkUpType.MVAR_TEMPLATE);
            mathTags.add(tag);
            needSpace = true;
            writeWord(tag.placeholder());
            needSpace = true;
          } catch (Exception ignored) {
          }
          break;
        default:
          iterate(n.getArgs());
      }
    } catch (Exception e) {
      Logger.info(e, "Problem prcessing page", pageTitle.getTitle());
    }
  }

  public String wiki2Tex(String content) {
    content = subMatch.matcher(content).replaceAll("_{$1}")
        .replaceAll("[{<]sup[}>](.+?)[{<]/sup[}>]", "^{$1}")
        .replaceAll("'''(.+?)'''", "\\\\mathbf{$1}")
        .replaceAll("''(.+?)''", "$1");
    return UnicodeMap.string2TeX(content);
//    int[] chars = content.codePoints().toArray();
//    StringBuilder res = new StringBuilder();
//
//    for (int code : chars) {
//      if (code > 128) {
//        res.append(UnicodeMap.char2TeX(code));
//      } else {
//        res.append((char) code);
//      }
//    }
//    return res.toString().trim();
  }

  public void visit(WtTemplateArgument n) {
    if (!detectHiddenMath(n.getValue())) {
      iterate(n.getValue());
    }
  }

  public void visit(WtTemplateParameter n) {
  }

  public void visit(WtTagExtension n) {
    if (n.getName().equals("math")) {
      MathTag tag = new MathTag(n.getLocation().line, n.getBody().getContent(), WikiTextUtils.MathMarkUpType.LATEX);
      // System.err.println(i+++" : "+ n.getBody().getContent());
      mathTags.add(tag);
      if (needNewlines > 0) {
        write(" ");
      }
      needSpace = true;
      writeWord(tag.placeholder());
      needSpace = true;
    } else if (n.getName().equals("ref")) {
      String content = n.getBody().getContent();
      if (!content.contains("<math")) {
        return;
      }
      final List<MathTag> tags = WikiTextUtils.findMathTags(content);
      content = WikiTextUtils.replaceAllFormulas(content, tags);
      mathTags.addAll(tags);
      write("(");
      write(content);
      write("}");
    }
  }


  public void visit(WtPageSwitch n) {
  }

  // =========================================================================

  private void newline(int num) {
    if (pastBod) {
      if (num > needNewlines)
        needNewlines = num;
    }
  }

  private void wantSpace() {
    if (pastBod)
      needSpace = true;
  }

  private void finishLine() {
    sb.append(line.toString());
    sb.append(" ");
    line.setLength(0);
  }

  private void writeNewlines(int num) {
    finishLine();
    sb.append(StringUtils.strrep('\n', num));
    needNewlines = 0;
    needSpace = false;
  }

  private void writeWord(String s) {
    int length = s.length();
    if (length == 0)
      return;

    if (needSpace && needNewlines <= 0)
      line.append(' ');

    if (needNewlines > 0)
      writeNewlines(needNewlines);

    needSpace = false;
    pastBod = true;
    line.append(s);
  }

  private void write(String s) {
    if (s.isEmpty())
      return;

    if (Character.isSpaceChar(s.charAt(0)))
      wantSpace();

    String[] words = ws.split(s);
    for (int i = 0; i < words.length; ) {
      writeWord(words[i]);
      if (++i < words.length)
        wantSpace();
    }

    final char lastChar = s.charAt(s.length() - 1);
    if (Character.isSpaceChar(lastChar) || lastChar == '\n')
      wantSpace();
  }


  private void write(char[] cs) {
    write(String.valueOf(cs));
  }

  private void write(char ch) {
    writeWord(String.valueOf(ch));
  }

  private void write(int num) {
    writeWord(String.valueOf(num));
  }

  public String getStrippedOutput() {
    return (String) this.go(page.getPage());
  }

  public String getOutput() {
    String output = getStrippedOutput();
    for (WikidataLink link : links) {
      if (link.getTitle() == null) {
        output = output.replace("LINK_" + link.getContentHash(), "[[" + link.getContent() + "]]");
      } else {
        output = output.replace("LINK_" + link.getContentHash(), "[[" + link.getContent() + "|" + link.getTitle() + "]]");
      }
    }
    for (MathTag tag : mathTags) {
      output = output.replace("FORMULA_" + tag.getContentHash(),
          "<math>" + tag.getContent() + "</math>");
    }
    return output;
  }

  public List<WikidataLink> getLinks() {
    return links;
  }

  private String getTex(WtNode i, boolean force) {
    if (i.get(0) instanceof WtText) {
      String content = ((WtText) i.get(0)).getContent().trim();
      content = TextExtractorMapper.unescape(content);
      String tex = wiki2Tex(content);
      if (tex.length() > 0 && (content.length() == 1
          || (content.length() < 100 && !content.equals(tex)))) {
        Multiset<String> idents;
        try {
          idents = TexInfo.getIdentifiers(tex, texInfoUrl);
        } catch (XPathExpressionException | ParserConfigurationException | IOException
            | SAXException | TransformerException ignored) {
          return null;
        }
        if (idents.size() == 0 && !force) {
          return null;
        }
        if (i instanceof WtBold) {
          tex = "\\mathbf{" + tex + "}";
        }
        return tex;
      }
      if (force) {
        return tex;
      }
    }
    return null;
  }
}

