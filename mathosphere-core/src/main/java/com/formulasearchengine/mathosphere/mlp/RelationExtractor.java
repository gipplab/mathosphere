package com.formulasearchengine.mathosphere.mlp;

import com.google.common.collect.Multiset;

import com.formulasearchengine.mathosphere.mlp.cli.CliParams;
import com.formulasearchengine.mathosphere.mlp.cli.ListCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.MlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextAnnotatorMapper;
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

/**
 * The main Math Language Processor (MLP) class following the original idea of the MLP.
 * That means, it analyzes wikitext, extracts all mathematical expressions and nouns (as definiens).
 * It creates candidates of identifier-definiens via {@link CreateCandidatesMapper} and scores them
 * based on distance calculations.
 *
 * The main entry point is {@link #run(MlpCommandConfig)}.
 * The method {@link #list(ListCommandConfig)} is for testing.
 */
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
        String[] record = {r.getMathTag().getContent(), r.getDefinition(), Double.toString(r.getScore())};
        printer.printRecord(record);
      }
      printer.flush();
      pw.flush();
    }
  }

  /**
   * The main pipeline. It creates {@link WikiDocumentOutput} for a given input. The output
   * already contains all information (scored identifier-definiens candidates).
   * @param config configuration
   * @return final document with scored identifier-definiens pairs.
   * @throws Exception if something went wrong
   */
  private static WikiDocumentOutput getWikiDocumentOutput(MlpCommandConfig config) throws Exception {
    String filePath = config.getInput();
    String text = FileUtils.readFileToString(new File(filePath), "UTF-8");

    // create document based on given input
    RawWikiDocument doc = new RawWikiDocument(text);

    // load wikitext annotator
    WikiTextAnnotatorMapper annotator = new WikiTextAnnotatorMapper(config);
    annotator.open(null);

    // annotate (PoS tagging)
    ParsedWikiDocument parsedDocument = annotator.map(doc);

    // create candidates of identifier-definien pairs and score them
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
      for (Multiset.Entry<String> stringEntry : output.getIdentifiers()) {
        pw.println(stringEntry.getElement());
      }
      pw.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
