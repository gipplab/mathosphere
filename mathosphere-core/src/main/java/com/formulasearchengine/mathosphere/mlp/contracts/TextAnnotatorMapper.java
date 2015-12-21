package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.Formula;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.text.MathConverter;
import com.formulasearchengine.mathosphere.mlp.text.MathMLUtils;
import com.formulasearchengine.mathosphere.mlp.text.PosTagger;
import com.formulasearchengine.mathosphere.mlp.text.WikidataLinkMap;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TextAnnotatorMapper extends RichMapFunction<RawWikiDocument, ParsedWikiDocument> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextAnnotatorMapper.class);

  private final BaseConfig config;
  private final String language;
  private final String model;
  private final WikidataLinkMap wl;

  private PosTagger posTagger;

  public TextAnnotatorMapper(BaseConfig config) {
    this.config = config;
    this.language = config.getLanguage();
    this.model = config.getModel();
    if (config.getWikiDataFile() != null) {
      wl = new WikidataLinkMap(config.getWikiDataFile());
    } else {
      wl = null;
    }
  }

  @Override
  public void open(Configuration cfg) throws Exception {
    posTagger = PosTagger.create(language, model);
  }

  @Override
  public ParsedWikiDocument map(RawWikiDocument doc) throws Exception {
    LOGGER.info("processing \"{}\"...", doc.title);

    final ParsedWikiDocument parse = parse(doc.text, doc.title);
    LOGGER.debug("identifiers in \"{}\" from {} formulas: {}", doc.title, parse.getFormulas().size(),
        parse.getIdentifiers());
    return parse;
  }

  public ParsedWikiDocument parse(String wikitext, String title) {
    List<Formula> formulas;
    List<Sentence> sentences;
    try {
      String cleanText;
      List<MathTag> mathTags;
      if (config.getUseTeXIdentifiers()) {
        MathConverter c = new MathConverter(wikitext, title, wl);
        cleanText = c.getStrippedOutput();
        mathTags = c.getMathTags();
      } else {
        mathTags = WikiTextUtils.findMathTags(wikitext);
        String newText = WikiTextUtils.replaceAllFormulas(wikitext, mathTags);
        cleanText = WikiTextUtils.extractPlainText(newText);
      }
      formulas = toFormulas(mathTags, config.getUseTeXIdentifiers(),config.getTexvcinfoUrl());
      sentences = posTagger.process(cleanText, formulas);
    } catch (Exception e) {
      LOGGER.warn("Problem with text processing", title, e);
      formulas = new ArrayList<>();
      sentences = new ArrayList<>();
    }
    Multiset<String> allIdentifiers = HashMultiset.create();
    for (Formula formula : formulas) {
      for (Multiset.Entry<String> entry : formula.getIndentifiers().entrySet()) {
        allIdentifiers.add(entry.getElement(), entry.getCount());
      }
    }
    return new ParsedWikiDocument(title, allIdentifiers, formulas, sentences);
  }

  public ParsedWikiDocument parse(String wikitext) {
    return parse(wikitext, "no title specified");
  }

  public static List<Formula> toFormulas(List<MathTag> mathTags, Boolean useTeXIdentifiers,String url) {
    List<Formula> formulas = Lists.newArrayList();
    for (MathTag math : mathTags) {
      Multiset<String> identifiers = MathMLUtils.extractIdentifiers(math, useTeXIdentifiers,url);
      formulas.add(new Formula(math.placeholder(), math.getContent(), identifiers));
    }
    return formulas;
  }

}
