package mlp.contracts;

import java.util.List;
import java.util.Set;

import mlp.pojos.Formula;
import mlp.pojos.Sentence;
import mlp.pojos.WikiDocument;
import mlp.pojos.WikiDocumentText;
import mlp.text.MathMLUtils;
import mlp.text.PosTagger;
import mlp.text.WikiTextUtils;
import mlp.text.WikiTextUtils.MathTag;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TextAnnotatorMapper extends RichMapFunction<WikiDocumentText, WikiDocument> {

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
    public WikiDocument map(WikiDocumentText doc) throws Exception {
        LOGGER.debug("processing \"{}\"...", doc.title);

        List<MathTag> mathTags = WikiTextUtils.findMathTags(doc.text);
        List<Formula> formulas = toFormulas(mathTags);
        Set<String> allIdentifiers = Sets.newLinkedHashSet();
        for (Formula formula : formulas) {
            allIdentifiers.addAll(formula.getIndentifiers());
        }

        LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.title, formulas.size(), allIdentifiers);

        String newText = WikiTextUtils.replaceAllFormulas(doc.text, mathTags);
        String cleanText = WikiTextUtils.extractPlainText(newText);
        List<Sentence> sentences = posTagger.process(cleanText, formulas);

        return new WikiDocument(doc.id, doc.title, allIdentifiers, formulas, sentences);
    }

    public static List<Formula> toFormulas(List<MathTag> mathTags) {
        List<Formula> formulas = Lists.newArrayList();
        for (MathTag math : mathTags) {
            Set<String> identifiers = MathMLUtils.extractIdentifiers(math);
            formulas.add(new Formula(math.placeholder(), math.getContent(), identifiers));
        }
        return formulas;
    }

}
