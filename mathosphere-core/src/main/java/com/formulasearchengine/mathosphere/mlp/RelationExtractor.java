package com.formulasearchengine.mathosphere.mlp;

import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.CliParams;
import com.formulasearchengine.mathosphere.mlp.cli.ListCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.MlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class RelationExtractor {

  public static void main(String[] args) throws Exception {
    String params[] = {"extract", "-in", "C:/tmp/mlp/linear_regression.txt"};
    MlpCommandConfig config = CliParams.from(params).getExtractCommandConfig();
    run(config);
  }

  public static void run(MlpCommandConfig config) throws Exception {
    WikiDocumentOutput output = getWikiDocumentOutput(config);

    try (PrintWriter pw = createPrinter(config)) {
      List<Relation> relations = output.getRelations();
      CSVPrinter printer = CSVFormat.DEFAULT.withRecordSeparator("\n").print(pw);
      for (Relation r : relations) {
        String[] record = {r.getIdentifier(), r.getDefinition(), Double.toString(r.getScore())};
        printer.printRecord(record);
      }
      printer.flush();
      pw.flush();
    }
  }

  private static WikiDocumentOutput getWikiDocumentOutput(MlpCommandConfig config) throws Exception {
    TextAnnotatorMapper annotator = new TextAnnotatorMapper(config);
    annotator.open(null);

    String filePath = config.getInput();
    String text = FileUtils.readFileToString(new File(filePath), "UTF-8");
    RawWikiDocument doc = new RawWikiDocument(filePath, 0, text);
    ParsedWikiDocument parsedDocument = annotator.map(doc);

    CreateCandidatesMapper mlp = new CreateCandidatesMapper(config);
    return mlp.map(parsedDocument);
  }

  public static PrintWriter createPrinter(MlpCommandConfig config) throws FileNotFoundException {
    if (StringUtils.isNotBlank(config.getOutput())) {
      return new PrintWriter(new File(config.getOutput()));
    }

    return new PrintWriter(System.out);
  }

  public static void list(ListCommandConfig config) {
    try {
      PrintWriter pw = createPrinter(config);
      WikiDocumentOutput output = getWikiDocumentOutput(config);
      for (String stringEntry : output.getIdentifiers()) {
        pw.println(stringEntry);
      }
      pw.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
