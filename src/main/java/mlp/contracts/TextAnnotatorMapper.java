package mlp.contracts;

import java.util.List;

import mlp.pojos.Formula;
import mlp.pojos.ParsedWikiDocument;
import mlp.pojos.RawWikiDocument;
import mlp.pojos.Sentence;
import mlp.text.MathMLUtils;
import mlp.text.PosTagger;
import mlp.text.WikiTextUtils;
import mlp.text.WikiTextUtils.MathTag;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

public class TextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextAnnotatorMapper.class);

    private String posModel;
    private PosTagger posTagger;

    public TextAnnotatorMapper(String posModel) {
        this.posModel = posModel;
    }

    @Override
    public void open(Configuration cfg) throws Exception {
        posTagger = PosTagger.create(posModel);
    }

    @Override
    public ParsedWikiDocument map(RawWikiDocument doc) throws Exception {
        LOGGER.info("processing \"{}\"...", doc.title);

        List<MathTag> mathTags = WikiTextUtils.findMathTags(doc.text);
        List<Formula> formulas = toFormulas(mathTags);

        Multiset<String> allIdentifiers = HashMultiset.create();
        for (Formula formula : formulas) {
            for (Multiset.Entry<String> entry : formula.getIndentifiers().entrySet()) {
                allIdentifiers.add(entry.getElement(), entry.getCount());
            }
        }

        LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.title, formulas.size(), allIdentifiers);

        String newText = WikiTextUtils.replaceAllFormulas(doc.text, mathTags);
        String cleanText = WikiTextUtils.extractPlainText(newText);
        List<Sentence> sentences = posTagger.process(cleanText, formulas);

        return new ParsedWikiDocument(doc.title, allIdentifiers, formulas, sentences);
    }

    public static List<Formula> toFormulas(List<MathTag> mathTags) {
        List<Formula> formulas = Lists.newArrayList();
        for (MathTag math : mathTags) {
            Multiset<String> identifiers = MathMLUtils.extractIdentifiers(math);
            formulas.add(new Formula(math.placeholder(), math.getContent(), identifiers));
        }
        return formulas;
    }

}
