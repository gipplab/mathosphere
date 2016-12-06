package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;

import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.pojos.Word;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiTextUtils {
  private static int i = 0;

  private static final Pattern MATH_TAG_PATTERN = Pattern.compile("<math.+?</math>", Pattern.DOTALL);


  public enum MathMarkUpType {
    LATEX, MATHML, MATH_TEMPLATE, MVAR_TEMPLATE, LATEXII
  }


  public static List<MathTag> findMathTags(String text) {
    List<MathTag> results = new ArrayList<>();

    Matcher matcher = MATH_TAG_PATTERN.matcher(text);

    while (matcher.find()) {
      String tag = matcher.group();
      MathMarkUpType markUp = guessMarkupType(tag);
      if (markUp == MathMarkUpType.LATEX) {
        tag = tag.replaceAll("<math>", "").replaceAll("</math>", "");
      }
      results.add(new MathTag(matcher.start(), tag, markUp));
      // System.err.println(i+++":"+tag);
    }

    return results;
  }

  private static MathMarkUpType guessMarkupType(String math) {
    int closingBracket = math.indexOf(">", "<math".length());
    String afterTag = math.substring(closingBracket + 1, math.length()).trim();
    if (afterTag.startsWith("<")) {
      return MathMarkUpType.MATHML;
    } else {
      return MathMarkUpType.LATEX;
    }
  }

  public static String replaceAllFormulas(String text, List<MathTag> mathTags) {
    StringBuilder newText = new StringBuilder(text.length());

    int offset = 0;
    for (MathTag tag : mathTags) {
      newText.append(text.substring(offset, tag.getPosition()));
      newText.append(tag.placeholder());
      offset = tag.getPosition() + tag.getContent().length();
      if (tag.getMarkUpType() == MathMarkUpType.LATEX && !tag.getContent().startsWith("<math")) {
        offset += 13; //<math></math>
      }
    }

    newText.append(text.substring(offset, text.length()));
    return newText.toString();
  }

  public static String renderAllFormulae(String text) {
    return StringReplacer.replace(text, MATH_TAG_PATTERN, (Matcher m) ->
        {
          try {
            return TeX2MathML.TeX2MML(m.group(0).replaceAll("<math.*?>", "").replaceAll("</math>", ""));
          } catch (Exception e) {
            e.printStackTrace();
            return m.group(0);
          }
        }
    );
  }

  public static String subsup(String markup) {
    return markup.replaceAll("[{<]sub[}>](.+?)[{<]/sub[}>]", "_$1")
        .replaceAll("[{<]sup[}>](.+?)[{<]/sup[}>]", "^$1");
  }

  public static String extractPlainText(String wikiMarkup) {
    MarkupParser parser = new MarkupParser();
    parser.setMarkupLanguage(new MediaWikiLanguage());
    PlaintextDocumentBuilder builder = new PlaintextDocumentBuilder();
    parser.setBuilder(builder);
    parser.parse(wikiMarkup);
    return builder.getResult();
  }

  /**
   * Gets the fId'th {@link WikiTextUtils.MathMarkUpType#LATEX LATEX} type formula in a {@link ParsedWikiDocument}.
   *
   * @param parsedWikiDocument The Document containing the formula.
   * @param fId                The fId of the formula.
   * @return {@link MathTag} representing the formula.
   */
  public static MathTag getLatexFormula(ParsedWikiDocument parsedWikiDocument, Integer fId) {
    int count = -1;
    int i;
    for (i = 0; i < parsedWikiDocument.getFormulas().size(); i++) {
      final MathTag t = parsedWikiDocument.getFormulas().get(i);
      if (t.getMarkUpType() == WikiTextUtils.MathMarkUpType.LATEX) {
        count++;
        if (count == fId) {
          return parsedWikiDocument.getFormulas().get(i);
        }
      }
    }
    throw new IndexOutOfBoundsException("A Latex type formula with fId " + fId + " does not exist in " + parsedWikiDocument.getTitle());
  }

  /**
   * Get a definiens from a link. I.e. convert LINK_******** to [[LinkContent]] or [[LinkDefiniens]].
   * This method removes the explicit information where this link pints to and replaces it with a human readable representation.
   *
   * @param word Link the definiens is wanted for.
   * @param doc  Document containing the link.
   * @return The underlying, human readable definiens.
   */
  public static String deLinkify(Word word, ParsedWikiDocument doc) {
    String definition;
    if (word.getPosTag().equals(PosTag.LINK)) {
      String hash = word.getWord().replaceAll("^LINK_", "");
      WikidataLink link = doc.getLinkMap().get(hash);
      if (link != null) {
        definition = "[[" + link.getContent() + "]]";
      } else {
        definition = "[[" + word.getWord() + "]]";
      }
    } else {
      definition = word.getWord();
    }
    return definition;
  }
}
