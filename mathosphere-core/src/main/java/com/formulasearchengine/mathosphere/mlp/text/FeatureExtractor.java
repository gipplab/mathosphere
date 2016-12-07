package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.features.Feature;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.google.common.collect.Lists;

import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class FeatureExtractor implements MapFunction<ParsedWikiDocument, MyWikiDocumentOutput> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureExtractor.class);

  private final EvalCommandConfig config;
  private final List<GoldEntry> goldEntries;

  public FeatureExtractor(EvalCommandConfig config, List<GoldEntry> goldEntries) {
    this.config = config;
    this.goldEntries = goldEntries;
  }

  @Override
  public MyWikiDocumentOutput map(ParsedWikiDocument doc) throws Exception {
    List<FeatureVector> foundFeatures = Lists.newArrayList();
    List<Sentence> sentences = doc.getSentences();
    for (int i = 0; i < sentences.size(); i++) {
      Sentence sentence = sentences.get(i);
      if (!sentence.getIdentifiers().isEmpty()) {
        LOGGER.debug("sentence {}", sentence);
      }
      GoldEntry goldEntry = goldEntries.stream().filter(e -> e.getTitle().equals(doc.getTitle().replaceAll(" ", "_"))).findFirst().get();
      final Integer fid = Integer.parseInt(goldEntry.getFid());
      final MathTag seed = doc.getFormulas()
        .stream().filter(e -> e.getMarkUpType().equals(WikiTextUtils.MathMarkUpType.LATEX)).collect(Collectors.toList())
        .get(fid);
      Set<String> identifiers = sentence.getIdentifiers();
      identifiers.retainAll(seed.getIdentifiers(config).elementSet());
      MyPatternMatcher matcher = MyPatternMatcher.generatePatterns(identifiers);
      Collection<FeatureVector> foundMatches = matcher.match(sentence.getWords(), doc);
      for (FeatureVector match : foundMatches) {
        LOGGER.debug("found match {}", match);
        foundFeatures.add(match);
      }
    }
    LOGGER.info("extracted {} relations from {}", foundFeatures.size(), doc.getTitle());
    return new MyWikiDocumentOutput(doc.getTitle(), foundFeatures, doc.getIdentifiers());
  }

}
