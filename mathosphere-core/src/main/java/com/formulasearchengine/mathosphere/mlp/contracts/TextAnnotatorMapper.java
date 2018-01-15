package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.text.MathConverter;
import com.formulasearchengine.mathosphere.mlp.text.PosTagger;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LogManager.getLogger(TextAnnotatorMapper.class.getName());

  private final BaseConfig config;

  // Needs to be transient for flink
  private transient PosTagger posTagger;

  private List<Sentence> sentences;
  private List<WikidataLink> links;
  private List<MathTag> mathTags;

  public TextAnnotatorMapper(BaseConfig config) {
    this.config = config;
    reset();
  }

  @Override
  public void open(Configuration cfg) {
    LOGGER.debug("Create PoS tagger.");
    posTagger = PosTagger.create(config);
    try {
      super.open(cfg);
    } catch ( Exception e ){
      LOGGER.error("Cannot call open method of RichMapFunction.", e);
    }
  }

  @Override
  public ParsedWikiDocument map(RawWikiDocument doc) {
    LOGGER.info("processing \"{}\"...", doc.title);

    final ParsedWikiDocument parse = parse(doc.getType(), doc.text, doc.title);
    LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.title, parse.getFormulas().size(),
        parse.getIdentifiers());
    return parse;
  }

  /**
   * Reset previous results, if this function will called twice accidentally
   */
  private void reset(){
    sentences = null;
    links     = null;
    mathTags  = null;
  }

  private ParsedWikiDocument parse(ContentType type, String content, String title) {
    reset();

    try {
      String cleanText;

      if ( type == ContentType.UNKNOWN ){
        type = config.getUseTeXIdentifiers() ? ContentType.WikiData : ContentType.HTML;
      }

      switch ( type ){
        case HTML:
          cleanText = parseHtmlDocuments( content );
          break;
        case WikiData:
          cleanText = parseWikidataDocument( content, title );
          break;
        default:
          LOGGER.warn("Unknown content type. Neither WikiData nor HTML.");
          cleanText = "";
      }

      // natural language processing in cleaned text
      sentences = posTagger.process(cleanText, mathTags);
    } catch ( LinkTargetException | EngineException e ) {
      LOGGER.warn("Problem with wiki data text processing '{}'", title, e);
      mathTags = new ArrayList<>();
      sentences = new ArrayList<>();
    }

    // collect all identifiers from all formulae
    Multiset<String> allIdentifiers = HashMultiset.create();
    for (MathTag formula : mathTags) {
      for (Multiset.Entry<String> entry : formula.getIdentifiers(config).entrySet()) {
        allIdentifiers.add(entry.getElement(), entry.getCount());
      }
    }

    return new ParsedWikiDocument(title, allIdentifiers, mathTags, sentences, links);
  }

  private String parseWikidataDocument( String wikitext, String title ) throws LinkTargetException, EngineException {
    // using math converter for wikitex
    MathConverter c = new MathConverter(wikitext, title, config);

    // get math tags, links and cleaned text
    mathTags = c.getMathTags();
    links = c.getLinks();
    return c.getStrippedOutput();
  }

  private String parseHtmlDocuments( String htmltext ){
    mathTags = WikiTextUtils.findMathTags(htmltext);
    String newText = WikiTextUtils.replaceAllFormulas(htmltext, mathTags);
    // TODO links?
    return WikiTextUtils.extractPlainText(newText);
  }

  public ParsedWikiDocument parse(String wikitext) {
    return parse(ContentType.WikiData ,wikitext, "no title specified");
  }


}
