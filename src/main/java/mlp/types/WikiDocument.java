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
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mlp.utils.PlaintextDocumentBuilder;
import mlp.utils.TexIdentifierExtractor;

import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;
import org.apache.flink.types.IntValue;
import org.apache.flink.types.StringValue;
import org.apache.flink.types.Value;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;
import org.json.JSONObject;

/**
 * @author rob
 */
public class WikiDocument implements Value {

    private IntValue id = new IntValue();
    private StringValue title = new StringValue();

    private StringValue raw = new StringValue();
    private StringValue plaintext = new StringValue();

    /**
     * Wikipedia pages belong to different namespaces. Below is a list that describes a commonly used
     * namespaces.
     * 
     * -2 Media -1 Special 0 Default 1 Talk 2 User 3 User talk 4 Wikipedia 5 Wikipedia talk 6 File 7 File talk
     * 8 MediaWiki 9 MediaWiki talk 10 Template 11 Template talk 12 Help 13 Help talk 14 Category 15 Category
     * talk 100 Portal 101 Portal talk 108 Book 109 Book talk
     */
    private IntValue ns = new IntValue();

    /*
     * Holds all formulas found within the document. The key of the HashMap is the replacement string in the
     * document and the value contains the TeX String
     */
    private FormulaList formulas = new FormulaList();

    /*
     * Stores all unique identifiers found in this document
     */
    private Identifiers knownIdentifiers = new Identifiers();

    @Override
    public void write(DataOutputView out) throws IOException {
        id.write(out);
        ns.write(out);
        title.write(out);
        raw.write(out);
        plaintext.write(out);
        formulas.write(out);
        knownIdentifiers.write(out);
    }

    @Override
    public void read(DataInputView in) throws IOException {
        id.read(in);
        ns.read(in);
        title.read(in);
        raw.read(in);
        plaintext.read(in);
        formulas.read(in);
        knownIdentifiers.read(in);
    }

    public String getPlainText() {
        if (plaintext.getValue().isEmpty()) {
            StringWriter writer = new StringWriter();
            MarkupParser parser = new MarkupParser();
            MarkupLanguage wiki = new MediaWikiLanguage();
            parser.setMarkupLanguage(wiki);
            parser.setBuilder(new PlaintextDocumentBuilder(writer));
            parser.parse(raw.getValue());
            plaintext.setValue(writer.toString());
        }
        return plaintext.getValue();
    }

    public int getId() {
        return id.getValue();
    }

    public String getTitle() {
        return title.getValue();
    }

    public void setId(int id) {
        this.id.setValue(id);
    }

    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public int getNS() {
        return ns.getValue();
    }

    public void setNS(int ns) {
        this.ns.setValue(ns);
    }

    public String getText() {
        return raw.getValue();
    }

    public void setText(String text) {
        this.raw.setValue(text);
        this.replaceMathTags();
    }

    Pattern p = Pattern.compile("<math(.*?)>(.*?)</math>(\n{2}?)", Pattern.DOTALL);

    /**
     * Helper that replaces all math tags from the document and stores them in a list. Math tags that contain
     * exactly on identifier will be replaced in line with the identifier.
     */
    private void replaceMathTags() {
        String key, formula;
        String text = raw.getValue();
        boolean augmention, period;

        Matcher m = p.matcher(text);

        while (m.find()) {
            formula = m.group(2).trim();
            augmention = !m.group(1).isEmpty();
            // check if the formular terminates a sentence
            period = formula.endsWith(".") || !m.group(3).isEmpty();
            key = " FORMULA" + (period ? ". " : " ");
            List<String> identifiers = augmention ? TexIdentifierExtractor.getAllfromMathML(formula)
                    : TexIdentifierExtractor.getAllfromTex(formula);
            if (identifiers.isEmpty()) {
                text = m.replaceFirst(key);
            } else if (identifiers.size() == 1) {
                try {
                    text = m.replaceFirst(identifiers.get(0));
                } catch (Exception e) {
                    formulas.add(new Formula(key, formula));
                    text = m.replaceFirst(key);
                }
            } else {
                formulas.add(new Formula(key, formula));
                text = m.replaceFirst(key);
            }

            // add found identifers to the page wide list
            for (String identifier : identifiers) {
                if (knownIdentifiers.containsIdentifier(identifier)) {
                    continue;
                }
                knownIdentifiers.add(new StringValue(identifier));
            }
        }

        raw.setValue(text);
    }

    /**
     * Returns the formula list of all found and replaced formulas.
     * 
     * @return a list of all formulas
     */
    public FormulaList getFormulas() {
        return formulas;
    }

    /**
     * Returns a list of all found unique identifiers within this document.
     * 
     * @return a list of unique identifiers
     */
    public Identifiers getKnownIdentifiers() {
        return knownIdentifiers;
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
        Map<String, Object> json = new HashMap<>();
        json.put("title", title.getValue());
        json.put("identifiers", knownIdentifiers.toString());
        json.put("id", id.getValue());
        return new JSONObject(json);
    }

}
