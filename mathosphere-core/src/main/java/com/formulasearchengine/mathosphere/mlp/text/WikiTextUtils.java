package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiTextUtils {

  private static final Pattern MATH_TAG_PATTERN = Pattern.compile("<math.+?</math>", Pattern.DOTALL);



  public enum MathMarkUpType {
    LATEX, MATHML;
  }


  public static List<MathTag> findMathTags(String text) {
    List<MathTag> results = new ArrayList<>();

    Matcher matcher = MATH_TAG_PATTERN.matcher(text);

    while (matcher.find()) {
      String tag = matcher.group();
      MathMarkUpType markUp = guessMarkupType(tag);
      results.add(new MathTag(matcher.start(), tag, markUp));
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
}
