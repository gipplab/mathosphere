package com.formulasearchengine.mathosphere.mlp.contracts;

import com.google.common.collect.Lists;

import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.Sentence;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.DefinitionUtils;
import com.formulasearchengine.mathosphere.mlp.text.PatternMatcher;
import com.formulasearchengine.mathosphere.mlp.text.PatternMatcher.IdentifierMatch;

import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class PatternMatcherMapper implements MapFunction<ParsedWikiDocument, WikiDocumentOutput> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcherMapper.class);

  @Override
  public WikiDocumentOutput map(ParsedWikiDocument doc) throws Exception {
    List<Relation> foundRelations = Lists.newArrayList();
    List<Sentence> sentences = doc.getSentences();
    for (Sentence sentence : sentences) {
      if (!sentence.getIdentifiers().isEmpty()) {
        LOGGER.debug("sentence {}", sentence);
      }

      Set<String> identifiers = sentence.getIdentifiers();
      PatternMatcher matcher = PatternMatcher.generatePatterns(identifiers);
      List<IdentifierMatch> foundMatches = matcher.match(sentence.getWords(), doc);

      for (IdentifierMatch match : foundMatches) {
        if (!DefinitionUtils.isValid(match.getDefinition())) {
          continue;
        }

        Relation relation = new Relation();
        relation.setIdentifier(match.getIdentifier());
        relation.setDefinition(match.getDefinition());
        relation.setSentence(sentence);
        relation.setScore(1.0d);
        relation.setIdentifierPosition(match.getPosition());

        if (!relationWasFoundBefore(foundRelations, relation)) {
          LOGGER.debug("found match {}", relation);
          foundRelations.add(relation);
        }
      }
    }

    LOGGER.info("extracted {} relations from {}", foundRelations.size(), doc.getTitle());
    return new WikiDocumentOutput(doc.getTitle(), foundRelations, doc.getIdentifiers());
  }

  private boolean relationWasFoundBefore(List<Relation> foundRelations, Relation relation) {
    return foundRelations.stream().filter(
      e -> e.getIdentifier().toLowerCase().equals(relation.getIdentifier().toLowerCase())
        && e.getDefinition().toLowerCase().equals(relation.getDefinition().toLowerCase())
    ).findAny().isPresent();
  }

}
