package com.formulasearchengine.mathosphere.mlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlinkMlpRelationFinder {

  public static void main(String[] args) throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.from(args);
    run(config);
  }

  public static void run(FlinkMlpCommandConfig config) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents =
        source.flatMap(new TextExtractorMapper())
            .map(new TextAnnotatorMapper(config));

    DataSet<WikiDocumentOutput> result = documents.map(new CreateCandidatesMapper(config));

    result.map(new JsonSerializerMapper<>())
        .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
    //int cores = Runtime.getRuntime().availableProcessors();
    //env.setParallelism(1); // rounds down
    env.execute("Relation Finder");
  }

  public String runFromText(FlinkMlpCommandConfig config, String input) throws Exception {
    final JsonSerializerMapper<Object> serializerMapper = new JsonSerializerMapper<>();
    return serializerMapper.map(outDocFromText(config, input));
  }

  public WikiDocumentOutput outDocFromText(FlinkMlpCommandConfig config, String input) throws Exception {
    final TextAnnotatorMapper textAnnotatorMapper = new TextAnnotatorMapper(config);
    textAnnotatorMapper.open(null);
    final CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);

    final ParsedWikiDocument parsedWikiDocument = textAnnotatorMapper.parse(input);
    return candidatesMapper.map(parsedWikiDocument);
  }

  public static DataSource<String> readWikiDump(FlinkMlpCommandConfig config, ExecutionEnvironment env) {
    Path filePath = new Path(config.getDataset());
    TextInputFormat inp = new TextInputFormat(filePath);
    inp.setCharsetName("UTF-8");
    inp.setDelimiter("</page>");
    return env.readFile(inp, config.getDataset());
  }


  public static void evaluate(EvalCommandConfig config) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    DataSource<String> source = readWikiDump(config, env);
    final CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);
    DataSet<ParsedWikiDocument> documents =
        source.flatMap(new TextExtractorMapper())
            .map(new TextAnnotatorMapper(config));
    final File file = new File(config.getQueries());
    ObjectMapper mapper = new ObjectMapper();
    List userData = mapper.readValue(file, List.class);
    Map<String, Object> gold = new HashMap<>();
    for (Object o : userData) {
      final Map entry = (Map) o;
      Map formula = (Map) entry.get("formula");
      gold.put((String) formula.get("title"), o);
    }
    MapFunction<ParsedWikiDocument, ParsedWikiDocument> checker = new MapFunction<ParsedWikiDocument, ParsedWikiDocument>() {
      @Override
      public ParsedWikiDocument map(ParsedWikiDocument parsedWikiDocument) {
        String title = parsedWikiDocument.getTitle().replaceAll(" ", "_");
        try {
          Map goldElement = (Map) gold.get(title);
          Map formula = (Map) goldElement.get("formula");
          final Integer fid = Integer.parseInt((String) formula.get("fid"));
          final String tex = (String) formula.get("math_inputtex");
          int pos = getFormulaPos(parsedWikiDocument, fid);
          final MathTag seed = parsedWikiDocument.getFormulas().get(pos);
          if (!seed.getContent().equals(tex)) {
            System.err.println("PROBLEM WITH" + title);
            System.err.println(seed.getContent());
            System.err.println(tex);
            throw new Exception("Invalid numbering.");
          }
          final Set<String> real = seed.getIdentifiers(config).elementSet();
          final Map definitions = (Map) goldElement.get("definitions");
          final Set expected = definitions.keySet();
          Set<String> tp = new HashSet<>(expected);
          Set<String> fn = new HashSet<>(expected);
          Set<String> fp = new HashSet<>(real);
          fn.removeAll(real);
          fp.removeAll(expected);
          tp.retainAll(real);
          double rec = ((double) tp.size()) / (tp.size() + fn.size());
          double prec = ((double) tp.size()) / (tp.size() + fp.size());
          if (rec < 1. || prec < 1.) {
            System.err.println(title + " $" + tex + "$ Precision" + prec + "; Recall" + rec);
            System.err.println("fp:" + fp.toString());
            System.err.println("fn:" + fn.toString());
            System.err.println("https://en.formulasearchengine.com/wiki/" + title + "#math." + formula.get("oldId") + "." + fid);
          }
          final WikiDocumentOutput wikiDocumentOutput = candidatesMapper.map(parsedWikiDocument);
          List<Relation> relations = wikiDocumentOutput.getRelations();
          relations.removeIf(r -> !tp.contains(r.getIdentifier()));
          for (Relation relation : relations) {
            final List<String> refList = getDefiniens(definitions, relation);
            final String definition = relation.getDefinition().replaceAll("(\\[\\[|\\]\\])", "");
            if (refList.contains(definition)) {
              System.err.println("MATCH");
            }
            System.err.println(relation.getIdentifier() + ":" + relation.getDefinition() + " (" + relation.getScore() + ")");
          }


        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("Problem with " + title);
        }
        return parsedWikiDocument;
      }
    };

    env.setParallelism(1);
    documents.map(checker).print();
    //documents.writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
    // rounds down
    //env.execute("Evaluate Performance");
//    System.exit(0);
//
//    DataSet<WikiDocumentOutput> result = documents.map(new CreateCandidatesMapper(config));
//
//    result.map(new JsonSerializerMapper<>())
//        .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
//    //int cores = Runtime.getRuntime().availableProcessors();
//    //env.setParallelism(1); // rounds down
//    env.execute("Evaluate Performance");
  }

  public static List<String> getDefiniens(Map definitions, Relation relation) {
    List<String> result = new ArrayList<>();
    List definiens = (List) definitions.get(relation.getIdentifier());
    for (Object definien : definiens) {
      if (definien instanceof Map) {
        Map<String, String> var = (Map) definien;
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
          // there is only one entry
          final String def = stringStringEntry.getValue().trim().replaceAll("\\s*\\(.*?\\)$", "");
          result.add(def);
        }
      } else {
        result.add((String) definien);
      }
    }
    return result;
  }

  private static int getFormulaPos(ParsedWikiDocument parsedWikiDocument, Integer fid) {
    int count = -1;
    int i;
    for (i = 0; i < parsedWikiDocument.getFormulas().size(); i++) {
      final MathTag t = parsedWikiDocument.getFormulas().get(i);
      if (t.getMarkUpType() == WikiTextUtils.MathMarkUpType.LATEX) {
        count++;
        if (count == fid) {
          break;
        }
      }
    }
    return i;
  }
}
