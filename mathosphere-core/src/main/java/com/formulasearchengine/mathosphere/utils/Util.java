package com.formulasearchengine.mathosphere.utils;

import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for functions needed throughout the project
 */
public class Util {
  private Util() {
  }

  public static void writeExtractedDefinitionsAsCsv(String file, String qId, String title, List<Relation> relations) throws IOException {
    if (file != null) {
      final File output = new File(file);
      if (!output.exists())
        output.createNewFile();
      OutputStreamWriter w = new FileWriter(output, true);
      CSVPrinter printer = CSVFormat.DEFAULT.withRecordSeparator("\n").print(w);
      for (Relation relation : relations) {
        //qId, title, identifier, definition
        String[] out = new String[]{
          qId,
          title,
          relation.getIdentifier(),
          relation.getDefinition(), "Word number: " + String.valueOf(relation.getIdentifierPosition()),
          relation.getSentence().toString(),
          getHumanReadableSentence(relation)};
        printer.printRecord(out);
      }
      w.flush();
      w.close();
    }
  }

  public static String getHumanReadableSentence(Relation relation) {
    List<String> words = relation.getSentence().getWords()
      //get words
      .stream().map(word -> word.getWord()).collect(Collectors.toList());
    //replace link
    words.remove(relation.getWordPosition());
    words.add(relation.getWordPosition(), relation.getDefinition());
    return words.toString().replaceAll(",", "");


  }
}
