package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.text.MathConverter;
import com.formulasearchengine.mathosphere.mlp.text.PosTagger;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class TextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(TextAnnotatorMapper.class.getName());

  private final BaseConfig config;

  // Needs to be transient for flink
  private transient PosTagger posTagger;

  public TextAnnotatorMapper(BaseConfig config) {
    this.config = config;
  }

  @Override
  public void open(Configuration cfg) {
    posTagger = PosTagger.create(config);
  }

  @Override
  public ParsedWikiDocument map(RawWikiDocument doc) {
    LOGGER.info("processing \"{}\"...", doc.title);

    final ParsedWikiDocument parse = parse(doc.text, doc.title);
    LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.title, parse.getFormulas().size(),
        parse.getIdentifiers());
    return parse;
  }

  public ParsedWikiDocument parse(String wikitext, String title) {
    List<Sentence> sentences;
    List<WikidataLink> links = null;
    List<MathTag> mathTags;
    try {
      String cleanText;
      if (config.getUseTeXIdentifiers()) {
        MathConverter c = new MathConverter(wikitext, title, config);
        cleanText = c.getStrippedOutput();
        mathTags = c.getMathTags();
        links = c.getLinks();
      } else {
        mathTags = WikiTextUtils.findMathTags(wikitext);
        String newText = WikiTextUtils.replaceAllFormulas(wikitext, mathTags);
        cleanText = WikiTextUtils.extractPlainText(newText);
      }
      //formulas = toFormulas(mathTags, config.getUseTeXIdentifiers(),config.getTexvcinfoUrl());
      sentences = posTagger.process(cleanText, mathTags);
    } catch (Exception e) {
      LOGGER.warn("Problem with text processing", title, e);
      mathTags = new ArrayList<>();
      sentences = new ArrayList<>();
    }
    Multiset<String> allIdentifiers = HashMultiset.create();
    for (MathTag formula : mathTags) {
      for (Multiset.Entry<String> entry : formula.getIdentifiers(config).entrySet()) {
        allIdentifiers.add(entry.getElement(), entry.getCount());
      }
    }
    return new ParsedWikiDocument(title, allIdentifiers, mathTags, sentences, links);
  }

  public ParsedWikiDocument parse(String wikitext) {
    return parse(wikitext, "no title specified");
  }


}
