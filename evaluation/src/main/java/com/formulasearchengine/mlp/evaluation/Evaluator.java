package com.formulasearchengine.mlp.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Leo on 20.10.2016.
 */
public class Evaluator {

  public static final String FOLDER = "formulasearchengine/mlp/gold/";
  public static final String GOLDFILE = FOLDER + "gold.json";

  public Evaluator() {}

  /**
   * Evaluate an extraction result against the mlp gold standard.
   * @param extractions multimap containing the identifiers and definitions for every formula.
   * @param gold the gold data {@link #readGoldEntries}
   * @return [true positives, false negatives, false positives]
   */
  public int[] evaluate(Multimap<String, IdentifierDefinition> extractions, List<GoldEntry> gold) {
    int totalNumberOfIdentifiers = (int) gold.stream().flatMap(ge -> ge.getDefinitions().stream().map(i -> i.getIdentifier()).distinct()).count();
    //initialize [true positives, false negatives, false positives] array
    int[] tpfnfp = {0, totalNumberOfIdentifiers, 0};
    for (GoldEntry goldEntry : gold) {
      Collection<IdentifierDefinition> identifierDefinitions = extractions.get(goldEntry.getqID());
      Set<String> identifiersWhosDefinitionWasFound = new HashSet<>();
      for (IdentifierDefinition i : identifierDefinitions) {
        System.out.print(goldEntry.getqID() + ",");
        i.setDefinition(i.getDefinition().replaceAll("(\\[\\[|\\]\\])", "").trim());
        if (goldEntry.getDefinitions().contains(i)) {
          if (!identifiersWhosDefinitionWasFound.contains(i.getIdentifier())) {
            System.out.print("matched,");
            tpfnfp[0]++;
            tpfnfp[1]--;
          } else {
            System.out.print("duplicate matched,");
          }
          System.out.println(String.format("\"%s\",\"%s\"", i.getIdentifier(), i.getDefinition()));
          identifiersWhosDefinitionWasFound.add(i.getIdentifier());
        } else {
          tpfnfp[2]++;
          System.out.println(String.format("not matched,\"%s\",\"%s\"", i.getIdentifier(), i.getDefinition()));
        }
      }
    }
    return tpfnfp;
  }

  public Multimap<String, IdentifierDefinition> readExtractions(File file, List<GoldEntry> goldEntries) throws IOException {
    Multimap<String, IdentifierDefinition> extractions = ArrayListMultimap.create();
    final FileReader extraction = new FileReader(file);
    Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(extraction);
    for (CSVRecord record : records) {
      //qId, title, identifier, definition
      String qId2 = record.get(0);
      String title = record.get(1);
      //check for qId and title
      if (goldEntries.stream().filter(g -> g.getqID().equals(qId2) && g.getTitle().equals(title)).collect(Collectors.toList()).size() == 0) {
        throw new IllegalArgumentException(String.format("The formula with qId: %s and title: %s does not exist in the gold standard.", qId2, title));
      }
      String identifier = record.get(2);
      String definition = record.get(3);
      extractions.put(qId2, new IdentifierDefinition(identifier, definition));
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
    List<IdentifierDefinition> definitions = new ArrayList<>();
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
          final String def = stringStringEntry.getValue().trim().replaceAll("\\s*\\(.*?\\)$", "");
          result.add(def);
        }
      } else {
        result.add((String) definien);
      }
    }
    return result;
  }


  public int[] evaluate(EvaluateCommand evaluateCommand) throws IOException {
    ArrayList<GoldEntry> goldEntries = readGoldEntries(new File(evaluateCommand.getGold()));
    Multimap<String, IdentifierDefinition> extractions = readExtractions(new File(evaluateCommand.getIn()), goldEntries);
    return evaluate(extractions, goldEntries);
  }
}

