package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikidataLink;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextParser;
import com.formulasearchengine.mathosphere.mlp.text.PosTagger;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class WikiTextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(WikiTextAnnotatorMapper.class.getName());

  private final BaseConfig config;


  private transient PosTagger posTagger;

  public WikiTextAnnotatorMapper(BaseConfig config) {
    this.config = config;
  }

  @Override
  public void open(Configuration cfg) {
    posTagger = PosTagger.create(config);
  }

  @Override
  public ParsedWikiDocument map(RawWikiDocument doc) {
    LOGGER.info("processing \"{}\"...", doc.getTitle());

    final ParsedWikiDocument parse = parse(doc);
    LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.getTitle(), parse.getFormulas().size(),
        parse.getIdentifiers());
    return parse;
  }

  public ParsedWikiDocument parse(RawWikiDocument doc) {
    List<Sentence> sentences;
    List<WikidataLink> links = null;
    List<MathTag> mathTags;
    try {
      String cleanText;
      if (config.getUseTeXIdentifiers()) {
        WikiTextParser c = new WikiTextParser(doc, config);
        cleanText = c.parse();
        mathTags = c.getMathTags();
        links = c.getLinks();
      } else {
        mathTags = WikiTextUtils.findMathTags(doc.getPageContent());
        String newText = WikiTextUtils.replaceAllFormulas(doc.getPageContent(), mathTags);
        cleanText = WikiTextUtils.extractPlainText(newText);
      }
      //formulas = toFormulas(mathTags, config.getUseTeXIdentifiers(),config.getTexvcinfoUrl());
      sentences = posTagger.process(cleanText, mathTags);
    } catch (Exception e) {
      LOGGER.warn("Problem with text processing", doc.getTitle(), e);
      mathTags = new ArrayList<>();
      sentences = new ArrayList<>();
    }
    Multiset<String> allIdentifiers = HashMultiset.create();
    for (MathTag formula : mathTags) {
      for (Multiset.Entry<String> entry : formula.getIdentifiers(config).entrySet()) {
        allIdentifiers.add(entry.getElement(), entry.getCount());
      }
    }
    return new ParsedWikiDocument(doc.getTitle(), allIdentifiers, mathTags, sentences, links);
  }

  public ParsedWikiDocument parse(String wikitext) {
    return parse(new RawWikiDocument("no title specified", -1, wikitext));
  }


}
