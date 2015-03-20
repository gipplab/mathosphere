package mlp.pojos;

import java.util.Objects;

import mlp.text.PosTag;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Word {

    public String word;
    public String posTag;

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

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, posTag);
    }

    public String toLowerCase() {
        if (PosTag.IDENTIFIER.equals(posTag)) {
            return word;
        } else {
            return word.toLowerCase();
        }
    }
}