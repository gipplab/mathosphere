package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.mlp.text.MathMLUtils;
import com.formulasearchengine.mathosphere.mlp.text.PosTagger;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextAnnotatorMapper.class);

  private final BaseConfig config;
  private final String language;
  private final String model;

  private PosTagger posTagger;

  public TextAnnotatorMapper(BaseConfig config) {
    this.config= config;
    this.language = config.getLanguage();
    this.model = config.getModel();
  }

  @Override
  public void open(Configuration cfg) throws Exception {
    posTagger = PosTagger.create(language, model);
    //posTagger = PosTagger.create(config.getLanguage(), config.getModel());
  }

  @Override
  public ParsedWikiDocument map(RawWikiDocument doc) throws Exception {
    LOGGER.info("processing \"{}\"...", doc.title);

	  final ParsedWikiDocument parse = parse(doc.text,doc.title);
	  LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.title, parse.getFormulas().size(),
			  parse.getIdentifiers());
	  return parse;
  }

	public ParsedWikiDocument parse(String wikitext, String title) {
		List<MathTag> mathTags = WikiTextUtils.findMathTags(wikitext);
		List<Formula> formulas = toFormulas(mathTags,config.getUseTeXIdentifiers());

		Multiset<String> allIdentifiers = HashMultiset.create();
		for (Formula formula : formulas) {
		  for (Multiset.Entry<String> entry : formula.getIndentifiers().entrySet()) {
		    allIdentifiers.add(entry.getElement(), entry.getCount());
		  }
		}

		String newText = WikiTextUtils.replaceAllFormulas(wikitext, mathTags);
		String cleanText = WikiTextUtils.extractPlainText(newText);
		List<Sentence> sentences = posTagger.process(cleanText, formulas);

		return new ParsedWikiDocument(title, allIdentifiers, formulas, sentences);
	}

	public ParsedWikiDocument parse(String wikitext){
		return parse(wikitext,"no title specified");
	}

	public static List<Formula> toFormulas(List<MathTag> mathTags, Boolean useTeXIdentifiers) {
    List<Formula> formulas = Lists.newArrayList();
    for (MathTag math : mathTags) {
      Multiset<String> identifiers = MathMLUtils.extractIdentifiers(math,useTeXIdentifiers);
      formulas.add(new Formula(math.placeholder(), math.getContent(), identifiers));
    }
    return formulas;
  }

}
