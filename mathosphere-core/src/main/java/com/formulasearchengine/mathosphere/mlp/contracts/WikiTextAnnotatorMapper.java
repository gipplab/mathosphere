package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mathosphere.mlp.text.TextAnnotator;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextParser;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WikiTextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(WikiTextAnnotatorMapper.class.getName());

  private final BaseConfig config;
  private TextAnnotator annotator;

  public WikiTextAnnotatorMapper(BaseConfig config) {
    this.config = config;
  }

  @Override
  public void open(Configuration cfg) {
    annotator = new TextAnnotator(config);
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
    DocumentMetaLib lib = null;
    List<Sentence> sentences;
    try {
      WikiTextParser c = new WikiTextParser(doc, config);
      List<String> cleanText = c.parse();
      lib = c.getMetaLibrary();
      sentences = annotator.annotate(cleanText, lib);
    } catch (Exception e) {
      LOGGER.warn("Unable to parse wikitext", doc.getTitle(), e);
      sentences = new ArrayList<>();
      if ( lib == null ) lib = new DocumentMetaLib();
    }

    Multiset<String> allIdentifiers = getAllIdentifiers(lib.getFormulaLib(), config);
    return new ParsedWikiDocument(doc.getTitle(), allIdentifiers, sentences, lib);
  }

  public ParsedWikiDocument parse(String wikitext) {
    return parse(new RawWikiDocument("no title specified", -1, wikitext));
  }

  public static Multiset<String> getAllIdentifiers(Map<String, MathTag> mathTags, BaseConfig config) {
    Multiset<String> allIdentifiers = HashMultiset.create();
    for (MathTag formula : mathTags.values()) {
      for (Multiset.Entry<String> entry : formula.getIdentifiers(config).entrySet()) {
        allIdentifiers.add(entry.getElement(), entry.getCount());
      }
    }
    return allIdentifiers;
  }

}
