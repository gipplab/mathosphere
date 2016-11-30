package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.features.Feature;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.google.common.collect.Lists;

import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FeatureExtractor implements MapFunction<ParsedWikiDocument, MyWikiDocumentOutput> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureExtractor.class);

  @Override
  public MyWikiDocumentOutput map(ParsedWikiDocument doc) throws Exception {
    List<FeatureVector> foundFeatures = Lists.newArrayList();
    List<Sentence> sentences = doc.getSentences();
    for (int i = 0; i < sentences.size(); i++) {
      Sentence sentence = sentences.get(i);
      if (!sentence.getIdentifiers().isEmpty()) {
        LOGGER.debug("sentence {}", sentence);
      }

      Set<String> identifiers = sentence.getIdentifiers();
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
