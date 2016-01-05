package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.PatternMatchingRelationFinder;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class PatternMatcherMapperTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatcherMapperTest.class);

  @Test
  public void testShodingerFull() throws Exception {
    ParsedWikiDocument doc = CreateCandidatesMapperTest.read("com/formulasearchengine/mathosphere/mlp/augmentendwikitext.xml", 1);

    PatternMatcherMapper patternMatcher = new PatternMatcherMapper();
    WikiDocumentOutput identifiers = patternMatcher.map(doc);

    for (Relation relation : identifiers.getRelations()) {
      LOGGER.debug("relation: {}", relation);
    }
  }

  @Test
  public void testShrodingerPart() throws Exception {
    InputStream input = PatternMatchingRelationFinder.class.getResourceAsStream("escaped.txt");
    String text = IOUtils.toString(input);
    RawWikiDocument documentText = new RawWikiDocument("Document", 0, text);

    TextAnnotatorMapper textAnnotator = TextAnnotatorMapperTest.TEST_INSTANCE;
    ParsedWikiDocument doc = textAnnotator.map(documentText);

    PatternMatcherMapper patternMatcher = new PatternMatcherMapper();
    WikiDocumentOutput identifiers = patternMatcher.map(doc);

    for (Relation relation : identifiers.getRelations()) {
      LOGGER.debug("relation: {}, sentence: {}", relation, relation.getSentence());
    }
  }

}
