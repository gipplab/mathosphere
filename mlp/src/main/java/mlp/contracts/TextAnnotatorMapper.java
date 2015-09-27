package mlp.contracts;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import mlp.cli.BaseConfig;
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

import java.util.List;

public class TextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextAnnotatorMapper.class);

  private final String language;
  private final String model;

  private PosTagger posTagger;

  public TextAnnotatorMapper(BaseConfig config) {
    this.language = config.getLanguage();
    this.model = config.getModel();
  }

  @Override
  public void open(Configuration cfg) throws Exception {
    posTagger = PosTagger.create(language, model);
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
