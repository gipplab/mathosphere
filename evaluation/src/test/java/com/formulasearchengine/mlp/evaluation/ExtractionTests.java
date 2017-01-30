package com.formulasearchengine.mlp.evaluation;

import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import com.formulasearchengine.mlp.evaluation.pojo.ScoreSummary;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Leo on 21.10.2016.
 */
public class ExtractionTests {

  public static final String FOLDER = "formulasearchengine/mlp/gold/";
  public static final String GOLDFILE_SAMPLE = FOLDER + "gold_sample.json";
  public static final String GOLDFILE_SAMPLE_2 = FOLDER + "gold_sample2.json";
  public static final String GOLDFILE_UNSORTED = FOLDER + "gold_unsorted_sample.json";
  public static final String EXTRACTIONS = FOLDER + "extraction.csv";
  public static final String EXTRACTIONS_SAMPLE = FOLDER + "extraction_sample.csv";
  public static final String EXTRACTIONS_SAMPLE_LINK_WIKIDATA_NORMAL = FOLDER + "extraction_sample_link_wikidata_normal.csv";
  public static final String EXTRACTIONS_SAMPLE_EXTRACTIONS_NO_QID = FOLDER + "extraction_sample_noQid.csv";
  public static final String EXTRACTIONS_UNSORTED = FOLDER + "extraction_unsorted.csv";
  public static final String EXTRACTIONS_SAMPLE_WRONG = FOLDER + "extraction_sample_wrong.csv";
  public static final String EXTRACTIONS_SAMPLE_WRONG_2 = FOLDER + "extraction_sample_wrong2.csv";
  public static final String EXTRACTIONS_SAMPLE_WIKIDATA = FOLDER + "extraction_sample_wikidata.csv";
  public static final String EXTRACTIONS_SAMPLE_WIKIDATA_2 = FOLDER + "extraction_sample_wikidata2.csv";

  public static final int NUMBER_OF_FORMULAS = 100;
  public static final int TOTAL_NUMBER_OF_DEFINITIONS = 575;
  public static final int TOTAL_NUMBER_OF_TEXT_DEFINITIONS = 359;
  public static final int TOTAL_NUMBER_OF_WIKIDATA_LINKS = 216;
  public static final int TOTAL_NUMBER_OF_IDENTIFIERS = 310;
  public static final int TOTAL_NUMBER_OF_EXTRACTED_DEFINITIONS = 425;
  public static final int TOTAL_NUMBER_OF_IDENTIFIERS_WITH_DEFINITIONS = 87;
  public static final int TRUE_POSITIVES = 69;
  public static final int FALSE_POSITIVES = 351;
  public static final int UNSORTED_TP = 3;
  public static final int UNSORTED_TOTAL_IDENTIFIERS = 7;
  public static final int UNSORTED_TOTAL_EXTRACTIONS = 15;
  public static final int DUPLICATE_TP = 5;

  public File getFile(String file) {
    ClassLoader classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(file).getFile());
  }

  @Test
  public void testGoldReading() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold_withWikidata = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE));
    List<GoldEntry> gold_withoutWikidata = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE_2));
    assertEquals(gold_withoutWikidata.size(), 1);
    ArrayList<IdentifierDefinition> definitions = new ArrayList<>();
    definitions.add(new IdentifierDefinition("q", "probability"));
    //without wikidata
    assertEquals(1, gold_withoutWikidata.size());
    assertEquals(new GoldEntry("41", "3545", "1", "q^{42}", "Martingale_(betting_system)", definitions), gold_withoutWikidata.get(0));
    //with wikidata
    definitions.add(0, new IdentifierDefinition("q", "Q9492"));
    assertEquals(1, gold_withWikidata.size());
    assertEquals(new GoldEntry("41", "3545", "1", "q^{42}", "Martingale_(betting_system)", definitions), gold_withWikidata.get(0));
  }

  @Test
  public void testFullGoldReading() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    //number of formula
    assertEquals(NUMBER_OF_FORMULAS, gold.size());
    //total number of definitions (575), 359 text definitions, 216 wikidata links
    assertEquals(TOTAL_NUMBER_OF_DEFINITIONS, gold.stream().flatMap(g -> g.getDefinitions().stream()).count());
    assertEquals(TOTAL_NUMBER_OF_TEXT_DEFINITIONS, gold.stream().flatMap(g -> g.getDefinitions().stream().filter(d -> d.getDefinition().matches("(^(?!q\\d+).*)$"))).count());
    assertEquals(TOTAL_NUMBER_OF_WIKIDATA_LINKS, gold.stream().flatMap(g -> g.getDefinitions().stream().filter(d -> d.getDefinition().matches("(^(q\\d+).*)$"))).count());
    //number of distinct identifiers
    assertEquals(TOTAL_NUMBER_OF_IDENTIFIERS, gold.stream().flatMap(g -> g.getDefinitions().stream().map(i -> i.getIdentifier()).distinct()).count());
    ArrayList<IdentifierDefinition> definitions = new ArrayList<>();
    definitions.add(new IdentifierDefinition("q", "q9492"));
    definitions.add(new IdentifierDefinition("q", "probability"));
    //0 indexed but the first qId is 1 => 40 == 41
    assertEquals(new GoldEntry("41", "3545", "1", "q^{42}", "Martingale_(betting_system)", definitions), gold.get(40));
  }

  @Test
  public void testExtractionReading() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE));
    Multimap<String, IdentifierDefinition> extractions = evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE), gold);
    Multimap<String, IdentifierDefinition> testData = ArrayListMultimap.create();
    testData.put("41", new IdentifierDefinition("q", "tosses"));
    assertEquals(extractions, testData);
  }

  @Test
  public void testExtractionReadingFull() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    Multimap<String, IdentifierDefinition> extractions = evaluator.readExtractions(getFile(EXTRACTIONS), gold);
    assertEquals(TOTAL_NUMBER_OF_EXTRACTED_DEFINITIONS, extractions.entries().size());
    assertEquals(TOTAL_NUMBER_OF_IDENTIFIERS_WITH_DEFINITIONS, extractions.keySet().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractionReadingBadQId() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE));
    evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE_WRONG), gold);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtractionReadingBadTitle() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE));
    evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE_WRONG_2), gold);
  }

  @Test
  public void testEvaluation() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    Multimap<String, IdentifierDefinition> extractions = evaluator.readExtractions(getFile(EXTRACTIONS), gold);
    ScoreSummary result = evaluator.evaluate(extractions, gold);
    Assert.assertEquals(new ScoreSummary(TRUE_POSITIVES, DUPLICATE_TP, TOTAL_NUMBER_OF_IDENTIFIERS - TRUE_POSITIVES, FALSE_POSITIVES, 0), result);
  }

  @Test
  public void testEvaluationTitleKey() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    Multimap<String, IdentifierDefinition> extractions = evaluator.readExtractions(getFile(EXTRACTIONS), gold, true);
    ScoreSummary result = evaluator.evaluate(extractions, gold, true);
    Assert.assertEquals(new ScoreSummary(TRUE_POSITIVES, DUPLICATE_TP, TOTAL_NUMBER_OF_IDENTIFIERS - TRUE_POSITIVES, FALSE_POSITIVES, 0), result);
  }


  @Test
  public void testEvaluation_no_qId() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    Multimap<String, IdentifierDefinition> extractions = evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE_EXTRACTIONS_NO_QID), gold, true);
    ScoreSummary result = evaluator.evaluate(extractions, gold, true);
    Assert.assertEquals(new ScoreSummary(1, 0, TOTAL_NUMBER_OF_IDENTIFIERS - 1, 0, 0), result);
  }

  @Test
  public void testEvaluation_entrie_with_link_wikidata_and_normal_definition() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    Multimap<String, IdentifierDefinition> extractions = evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE_LINK_WIKIDATA_NORMAL), gold, true);
    ScoreSummary result = evaluator.evaluate(extractions, gold, true);
    Assert.assertEquals(new ScoreSummary(1, 2, TOTAL_NUMBER_OF_IDENTIFIERS - 1, 0, 1), result);
  }

  @Test
  public void testEvaluationWithWikidataEntries() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    Multimap<String, IdentifierDefinition> extractionsWikidata = evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE_WIKIDATA), gold);
    Multimap<String, IdentifierDefinition> extractionsWikidata2 = evaluator.readExtractions(getFile(EXTRACTIONS_SAMPLE_WIKIDATA_2), gold);
    ScoreSummary resultWikidata = evaluator.evaluate(extractionsWikidata, gold);
    Assert.assertEquals(new ScoreSummary(1, 0, TOTAL_NUMBER_OF_IDENTIFIERS - 1, 0, 1), resultWikidata);
    ScoreSummary resultWikidata2 = evaluator.evaluate(extractionsWikidata2, gold);
    Assert.assertEquals(new ScoreSummary(1, 1, TOTAL_NUMBER_OF_IDENTIFIERS - 1, 0, 1), resultWikidata2);
  }

  @Test
  public void testUnsortedEntries() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(GOLDFILE_UNSORTED));
    Multimap<String, IdentifierDefinition> extractions_unsorted = evaluator.readExtractions(getFile(EXTRACTIONS_UNSORTED), gold);
    ScoreSummary result = evaluator.evaluate(extractions_unsorted, gold);
    Assert.assertEquals(new ScoreSummary(UNSORTED_TP, 0, UNSORTED_TOTAL_IDENTIFIERS - UNSORTED_TP, UNSORTED_TOTAL_EXTRACTIONS - UNSORTED_TP, 0), result);
  }
}
