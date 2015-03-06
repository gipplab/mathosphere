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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.types.DoubleValue;
import org.apache.flink.types.IntValue;
import org.apache.flink.types.Key;
import org.apache.flink.types.StringValue;
import org.json.JSONObject;

/**
 * @author rob
 */
public class Relation implements Key<Relation>, Cloneable {

    private DoubleValue score = new DoubleValue();
    private IntValue iposition = new IntValue();
    private IntValue wposition = new IntValue();
    private StringValue identifier = new StringValue();
    private Sentence sentence = new Sentence();
    private StringValue title = new StringValue();

    public double getScore() {
        return score.getValue();
    }

    public void setScore(DoubleValue score) {
        this.score = score;
    }

    public void setScore(Double score) {
        this.score = new DoubleValue(score);
    }

    public String getDefinitionWord() {
        return (wposition != null) ? sentence.get(wposition.getValue()).getWord() : null;
    }

    public StringValue getIdentifier() {
        return identifier;
    }

    public void setIdentifier(StringValue identifier) {
        this.identifier = identifier;
    }

    public void setIdentifier(String identifier) {
        setIdentifier(new StringValue(identifier));
    }

    public IntValue getIdentifierPosition() {
        return iposition;
    }

    public void setIdentifierPosition(IntValue position) {
        this.iposition = position;
    }

    public void setIdentifierPosition(Integer position) {
        setIdentifierPosition(new IntValue(position));
    }

    public IntValue getWordPosition() {
        return wposition;
    }

    public void setWordPosition(IntValue position) {
        this.wposition = position;
    }

    public void setWordPosition(Integer position) {
        setWordPosition(new IntValue(position));
    }

    public Sentence getSentence() {
        return sentence;
    }

    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
    }

    public StringValue getTitle() {
        return this.title;
    }

    public void setTitle(StringValue title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = new StringValue(title);
    }

    @Override
    public String toString() {
        String s = toJSON().toString();
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    public JSONObject toJSON() {
        String word;
        try {
            word = sentence.get(wposition.getValue()).getWord();
        } catch (Exception e) {
            word = "";
        }
        Map<String, Object> json = new HashMap<>();
        json.put("page", title.getValue());
        json.put("identifier", identifier.getValue());
        json.put("score", score.getValue());
        json.put("word", word);
        json.put("identifier_position", iposition.getValue());
        json.put("definition_position", wposition.getValue());
        json.put("sentence", sentence.toJSON());
        return new JSONObject(json);
    }

    @Override
    public Relation clone() {
        Relation obj = new Relation();
        obj.setIdentifier(new StringValue(identifier.getValue()));
        obj.setIdentifierPosition(new IntValue(iposition.getValue()));
        obj.setScore(new DoubleValue(score.getValue()));
        obj.setSentence(sentence.clone());
        obj.setTitle(new StringValue(title.getValue()));
        obj.setWordPosition(new IntValue(wposition.getValue()));
        return obj;
    }

    @Override
    public int compareTo(Relation other) {
        return identifier.compareTo(other.identifier);
    }

    @Override
    public void write(DataOutputView out) throws IOException {
        iposition.write(out);
        identifier.write(out);
        wposition.write(out);
        sentence.write(out);
        score.write(out);
        title.write(out);
    }

    @Override
    public void read(DataInputView in) throws IOException {
        iposition.read(in);
        identifier.read(in);
        wposition.read(in);
        sentence.read(in);
        score.read(in);
        title.read(in);
    }
}
