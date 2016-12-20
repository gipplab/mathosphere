package com.formulasearchengine.mathosphere.mlp.text;

import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.features.FeatureVector;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import com.google.common.collect.Lists;
import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleFeatureExtractor implements MapFunction<ParsedWikiDocument, Instances> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFeatureExtractor.class);
  public static final String MATCH = "match";
  public static final String NO_MATCH = "no match";

  private final EvalCommandConfig config;
  private final List<GoldEntry> goldEntries;

  public SimpleFeatureExtractor(EvalCommandConfig config, List<GoldEntry> goldEntries) {
    this.config = config;
    this.goldEntries = goldEntries;
  }

  @Override
  public Instances map(ParsedWikiDocument doc) throws Exception {
    List<Relation> foundFeatures = Lists.newArrayList();
    List<Sentence> sentences = doc.getSentences();
    ArrayList<Attribute> atts = new ArrayList<>();
    //meta information
    atts.add(new Attribute("title"));
    atts.add(new Attribute("identifier"));
    atts.add(new Attribute("definiens"));
    atts.add(new Attribute("identifierPos"));
    atts.add(new Attribute("definiensPos"));
    //this is where the real attrs begin
    atts.add(new Attribute("sentence"));
    //TODO expand
    //classification
    FastVector attVals = new FastVector();
    attVals.addElement(MATCH);
    attVals.addElement(NO_MATCH);
    atts.add(new Attribute("classification", attVals));
    Instances result = new Instances("MyRelation", atts, 0);

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
      SimplePatternMatcher matcher = SimplePatternMatcher.generatePatterns(identifiers);
      Collection<Relation> foundMatches = matcher.match(sentence, doc);
      for (Relation match : foundMatches) {
        LOGGER.debug("found match {}", match);
        foundFeatures.add(match);

        double[] values = new double[result.numAttributes()];
        values[0] = result.attribute(1).addStringValue(doc.getTitle());
        values[1] = result.attribute(2).addStringValue(match.getIdentifier());
        values[2] = result.attribute(3).addStringValue(match.getDefinition());
        values[3] = match.getIdentifierPosition();
        values[4] = match.getWordPosition();
        values[5] = result.attribute(4).addStringValue(match.getSentence().getWords().toString());
        values[6] = matchesGold(match, goldEntry) ? attVals.indexOf(MATCH) : attVals.indexOf(NO_MATCH);
        result.add(new DenseInstance(1.0, values));
      }
    }
    LOGGER.info("extracted {} relations from {}", foundFeatures.size(), doc.getTitle());
    return result;
  }


  public boolean matchesGold(Relation relation, GoldEntry gold) {
    return matchesGold(relation.getIdentifier(), relation.getDefinition(), gold);
  }

  public boolean matchesGold(String identifier, String definiens, GoldEntry gold) {
    List<IdentifierDefinition> identifierDefinitions = gold.getDefinitions();
    return identifierDefinitions.contains(new IdentifierDefinition(identifier, definiens.replaceAll("\\[|\\]", "").trim().toLowerCase()));
  }
}
