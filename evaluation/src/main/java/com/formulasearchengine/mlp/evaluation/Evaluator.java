package com.formulasearchengine.mlp.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mlp.evaluation.cli.EvaluateCommand;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.IdentifierDefinition;
import com.formulasearchengine.mlp.evaluation.pojo.ScoreSummary;
import com.google.common.collect.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Leo on 20.10.2016.
 */
public class Evaluator {

  public static final String FOLDER = "formulasearchengine/mlp/gold/";
  public static final String GOLDFILE = FOLDER + "gold.json";

  //CSV fields
  private static final int QID = 0;
  private static final int TITLE = 1;
  private static final int IDENTIFIER = 2;
  private static final int DEFINITION = 3;

  public Evaluator() {
  }

  /**
   * Evaluate an extraction result against the mlp gold standard. Uses the qId as key.
   *
   * @param extractions multimap containing the identifiers and definitions for every formula.
   * @param gold        the gold data {@link #readGoldEntries}
   * @return [true positives, false negatives, false positives]
   */
  public ScoreSummary evaluate(Multimap<String, IdentifierDefinition> extractions, List<GoldEntry> gold) {
    return evaluate(extractions, gold, false);
  }


  /**
   * Evaluate an extraction result against the mlp gold standard.
   *
   * @param extractions multimap containing the identifiers and definitions for every formula.
   * @param gold        the gold data {@link #readGoldEntries}
   * @param titleKey    if true the title will be used as key instead of the qId.
   * @return [true positives, false negatives, false positives]
   */
  public ScoreSummary evaluate(Multimap<String, IdentifierDefinition> extractions, List<GoldEntry> gold, boolean titleKey) {
    int totalNumberOfIdentifiers = (int) gold.stream().flatMap(ge -> ge.getDefinitions().stream().map(i -> i.getIdentifier()).distinct()).count();
    //initialize [true positives, false negatives, false positives, number of wikidata links matched] array
    ScoreSummary result = new ScoreSummary(0, 0, totalNumberOfIdentifiers, 0, 0);
    for (GoldEntry goldEntry : gold) {
      Collection<IdentifierDefinition> identifierDefinitions;
      if (titleKey) {
        identifierDefinitions = extractions.get(goldEntry.getTitle());
      } else {
        identifierDefinitions = extractions.get(goldEntry.getqID());
      }
      Set<String> identifiersWhosDefinitionWasFound = new HashSet<>();
      for (IdentifierDefinition i : identifierDefinitions) {
        System.out.print(goldEntry.getqID() + ",");
        i.setDefinition(i.getDefinition().replaceAll("(\\[\\[|\\]\\])", "").trim());
        if (goldEntry.getDefinitions().contains(i)) {
          if (!identifiersWhosDefinitionWasFound.contains(i.getIdentifier())) {
            System.out.print("matched,");
            result.tp++;
            result.fn--;
          } else {
            System.out.print("duplicate matched,");
            result.duplicateTp++;
          }
          if (i.getDefinition().matches("(^(q\\d+).*)$")) {
            result.wikidatalinks++;
          }
          System.out.println(String.format("\"%s\",\"%s\"", i.getIdentifier(), i.getDefinition()));
          identifiersWhosDefinitionWasFound.add(i.getIdentifier());
        } else {
          result.fp++;
          System.out.println(String.format("not matched,\"%s\",\"%s\"", i.getIdentifier(), i.getDefinition()));
        }
      }
    }
    return result;
  }

  /**
   * Read a .csv file with extracted identifiers and perform some preliminary checks. Uses the qId as key.
   *
   * @param file        the file to parse
   * @param goldEntries the goldstandard this file wil be checked against
   * @return the parsed file in a format suitable for comparing to the gold standard
   * @throws IOException
   */
  public Multimap<String, IdentifierDefinition> readExtractions(File file, List<GoldEntry> goldEntries) throws IOException {
    return readExtractions(file, goldEntries, false);
  }

  /**
   * Read a .csv file with extracted identifiers and perform some preliminary checks.
   *
   * @param file        the file to parse.
   * @param goldEntries the goldstandard this file wil be checked against.
   * @param titleKey    if true the title will be used as key instead of the qId.
   * @return the parsed file in a format suitable for comparing to the gold standard.
   * @throws IOException
   */
  public Multimap<String, IdentifierDefinition> readExtractions(File file, List<GoldEntry> goldEntries, boolean titleKey) throws IOException {
    return readExtractions(new FileReader(file), goldEntries, titleKey);
  }

  /**
   * Read a .csv file with extracted identifiers and perform some preliminary checks.
   *
   * @param extraction  the extractions to parse.
   * @param goldEntries the goldstandard this file wil be checked against.
   * @param titleKey    if true the title will be used as key instead of the qId.
   * @return the parsed file in a format suitable for comparing to the gold standard.
   * @throws IOException
   */
  public Multimap<String, IdentifierDefinition> readExtractions(Reader extraction, List<GoldEntry> goldEntries, boolean titleKey) throws IOException {
    Multimap<String, IdentifierDefinition> extractions = ArrayListMultimap.create();
    Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(extraction);
    for (CSVRecord record : records) {
      //qId, title, identifier, definition
      String qId2 = record.get(QID).trim();
      String title = record.get(TITLE).trim();
      //check for qId and title
      if (!titleKey && goldEntries.stream().filter(g -> g.getqID().equals(qId2)).collect(Collectors.toList()).size() == 0) {
        throw new IllegalArgumentException(String.format("The formula with qId: %s and title: %s does not exist in the gold standard.", qId2, title));
      }
      if (titleKey && goldEntries.stream().filter(g -> g.getTitle().equals(title)).collect(Collectors.toList()).size() == 0) {
        throw new IllegalArgumentException(String.format("The formula with qId: %s and title: %s does not exist in the gold standard.", qId2, title));
      }
      String identifier = record.get(IDENTIFIER).trim();
      String definition = record.get(DEFINITION).trim();

      if (titleKey) {
        extractions.put(title, new IdentifierDefinition(identifier, definition));
      } else {
        extractions.put(qId2, new IdentifierDefinition(identifier, definition));
      }
    }
    //sanity test
    for (String key : extractions.keySet()) {
      Collection<IdentifierDefinition> definitions = extractions.get(key);
      for (IdentifierDefinition definition : definitions) {
        if (definitions.stream().filter(e -> e.equals(definition)).count() > 1) {
          throw new IllegalArgumentException("Identifier-definition pair \"" + definition.toString() + "\" occured more than once in formula: " + key);
        }
      }
    }
    return extractions;
  }

  public ArrayList<GoldEntry> readGoldEntries(File goldfile) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List goldData = mapper.readValue(goldfile, List.class);
    ArrayList<GoldEntry> goldEntries = new ArrayList<>();
    for (Object o : goldData) {
      goldEntries.add(parseGold((Map<String, Object>) o));
    }
    goldEntries.sort((e1, e2) -> Integer.parseInt(e1.getqID()) - Integer.parseInt(e2.getqID()));
    return goldEntries;
  }

  /**
   * Set the gold standard for this WikiDocument
   */

  private GoldEntry parseGold(Map<String, Object> gold) {
    ArrayList<IdentifierDefinition> definitions = new ArrayList<>();
    Map<String, String> rawDefinitions = (Map<String, String>) gold.get("definitions");
    for (String identifier : rawDefinitions.keySet()) {
      List<String> defeniens = getDefiniens(rawDefinitions, identifier);
      for (String defenien : defeniens) {
        definitions.add(new IdentifierDefinition(identifier, defenien));
      }
    }
    Map<String, String> formula = (Map<String, String>) gold.get("formula");
    return new GoldEntry(formula.get("qID"), formula.get("oldId"), formula.get("fid"), formula.get("math_inputtex"), formula.get("title"), definitions);
  }


  private List<String> getDefiniens(Map definitions, String identifier) {
    List<String> result = new ArrayList<>();
    List definiens = (List) definitions.get(identifier);
    for (Object definien : definiens) {
      if (definien instanceof Map) {
        Map<String, String> var = (Map) definien;
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
          // there is only one entry
          //remove everything in brackets
          final String def = stringStringEntry.getValue().replaceAll("\\s*\\(.*?\\)$", "").trim();
          //extract wikidata link
          final String wikidataLink = stringStringEntry.getKey();
          result.add(wikidataLink);
          result.add(def);
        }
      } else {
        result.add((String) definien);
      }
    }
    return result;
  }


  public ScoreSummary evaluate(EvaluateCommand evaluateCommand) throws IOException {
    ArrayList<GoldEntry> goldEntries = readGoldEntries(new File(evaluateCommand.getGold()));
    Multimap<String, IdentifierDefinition> extractions = readExtractions(new File(evaluateCommand.getIn()), goldEntries, evaluateCommand.isTitleKey());
    return evaluate(extractions, goldEntries, evaluateCommand.isTitleKey());
  }
}

