/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package mlp.types;

import edu.stanford.nlp.ling.TaggedWord;

import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.types.Key;
import org.apache.flink.types.StringValue;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rob
 */
public class Word implements Key<Word> {

    private StringValue word = new StringValue();
    private StringValue tag = new StringValue();

    public Word() {
    }

    public Word(String word, String tag) {
        setWord(word);
        setTag(tag);
    }

    public Word(TaggedWord word) {
        setWord(word.value());
        setTag(word.tag());
    }

    /**
     * Returns this Word as a TaggedWord from the Stanford NLP Project (@see
     * edu.stanford.nlp.ling.TaggedWord).
     * 
     * @return a TaggedWord
     */
    public TaggedWord getTaggedWord() {
        return new TaggedWord(word.getValue(), tag.getValue());
    }

    public String getWord() {
        return word.getValue();
    }

    public void setWord(String string) {
        String w = string;
        switch (w) {
        case "-LRB-":
            w = "(";
            break;
        case "-RRB-":
            w = ")";
            break;
        case "-LCB-":
            w = "{";
            break;
        case "-RCB-":
            w = "}";
            break;
        case "-LSB-":
            w = "[";
            break;
        case "-RSB-":
            w = "]";
            break;
        case "``":
            w = "\"";
            break;
        case "''":
            w = "\"";
            break;
        case "--":
            w = "-";
            break;
        }
        word.setValue(w);
    }

    public String getTag() {
        return tag.getValue();
    }

    public void setTag(String string) {
        String t = string;
        switch (t) {
        case "``":
            t = "\"";
            break;
        case "''":
            t = "\"";
            break;
        }
        tag.setValue(string);
    }

    @Override
    public int compareTo(Word other) {
        return this.word.compareTo(other.word);
    }

    @Override
    public String toString() {
        return word + " (" + tag + ")";
    }

    public JSONObject toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("word", getWord());
        json.put("tag", getTag());
        return new JSONObject(json);
    }

    @Override
    public void write(DataOutputView out) throws IOException {
        word.write(out);
        tag.write(out);
    }

    @Override
    public void read(DataInputView in) throws IOException {
        word.read(in);
        tag.read(in);
    }
}
