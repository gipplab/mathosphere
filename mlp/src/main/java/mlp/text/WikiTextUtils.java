package mlp.text;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiTextUtils {

  private static final Pattern MATH_TAG_PATTERN = Pattern.compile("<math.+?</math>", Pattern.DOTALL);

  private static final HashFunction HASHER = Hashing.md5();

  public static enum MathMarkUpType {
    LATEX, MATHML;
  }

  public static class MathTag {
    private final int position;
    private final String content;
    private final MathMarkUpType markUpType;

    public MathTag(int position, String content, MathMarkUpType markUp) {
      this.position = position;
      this.content = content;
      this.markUpType = markUp;
    }

    public int getPosition() {
      return position;
    }

    public String getContent() {
      return content;
    }

    public String getTagContent() {
      return content.replaceAll("<math.*?>", "").replaceAll("</math>", "");
    }

    public String getContentHash() {
      return HASHER.hashString(content, StandardCharsets.UTF_8).toString();
    }

    public String placeholder() {
      return "FORMULA_" + getContentHash();
    }

    public MathMarkUpType getMarkUpType() {
      return markUpType;
    }

    @Override
    public String toString() {
      return "MathTag [position=" + position + ", content=" + content + "]";
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }
  }

  public static List<MathTag> findMathTags(String text) {
    List<MathTag> results = new ArrayList<MathTag>();

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
