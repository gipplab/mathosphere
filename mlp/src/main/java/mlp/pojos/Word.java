package mlp.pojos;

import mlp.text.PosTag;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Objects;

public class Word {

  private String word;
  private String posTag;

  public Word() {
  }

  public Word(String word, String posTag) {
    this.word = word;
    this.posTag = posTag;
  }

  @Override
  public String toString() {
    return "'" + word + "':" + posTag;
  }

  public String getWord() {
    return word;
  }

  public String getPosTag() {
    return posTag;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getWord(), posTag);
  }

  public String toLowerCase() {
    if (PosTag.IDENTIFIER.equals(posTag)) {
      return word;
    } else {
      return word.toLowerCase();
    }
  }

}
