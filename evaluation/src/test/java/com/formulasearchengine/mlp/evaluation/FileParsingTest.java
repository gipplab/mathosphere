package com.formulasearchengine.mlp.evaluation;

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
public class FileParsingTest {

  public static final String FOLDER = "formulasearchengine/mlp/gold/";
  public static final String GOLDFILE_SAMPLE = FOLDER + "gold_sample.json";
  public static final String GOLDFILE_SAMPLE_2 = FOLDER + "gold_sample2.json";
  public static final String EXTRACTIONS = FOLDER + "extraction.csv";
  public static final String EXTRACTIONS_SAMPLE = FOLDER + "extraction_sample.csv";
  public static final String EXTRACTIONS_SAMPLE_WRONG = FOLDER + "extraction_sample_wrong.csv";
  public static final String EXTRACTIONS_SAMPLE_WRONG_2 = FOLDER + "extraction_sample_wrong2.csv";


  public File getFile(String file) {
    ClassLoader classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(file).getFile());
  }
  
  @Test
  public void testGoldReading() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE));
    List<GoldEntry> gold2 = evaluator.readGoldEntries(getFile(GOLDFILE_SAMPLE_2));
    assertEquals(gold.size(), 1);
    List<IdentifierDefinition> definitions = new ArrayList<>();
    definitions.add(new IdentifierDefinition("q", "probability"));
    assertEquals(new GoldEntry("41", "3545", "1", "q^{42}", "Martingale_(betting_system)", definitions), gold.get(0));
    assertEquals(new GoldEntry("41", "3545", "1", "q^{42}", "Martingale_(betting_system)", definitions), gold2.get(0));
  }

  @Test
  public void testFullGoldReading() throws IOException {
    Evaluator evaluator = new Evaluator();
    List<GoldEntry> gold = evaluator.readGoldEntries(getFile(Evaluator.GOLDFILE));
    //number of formula
    assertEquals(100, gold.size());
    //total number of definitions
    assertEquals(359, gold.stream().flatMap(g -> g.getDefinitions().stream()).count());
    //number of distinct identifiers
    assertEquals(310, gold.stream().flatMap(g -> g.getDefinitions().stream().map(i -> i.getIdentifier()).distinct()).count());
    List<IdentifierDefinition> definitions = new ArrayList<>();
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
    assertEquals(425, extractions.entries().size());
    assertEquals(87, extractions.keySet().size());
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
    int[] result = evaluator.evaluate(extractions, gold);
    System.out.println(String.format("tp: %d, fn: %d, fp: %d", result[0],result[1],result[2]));
  }
}
