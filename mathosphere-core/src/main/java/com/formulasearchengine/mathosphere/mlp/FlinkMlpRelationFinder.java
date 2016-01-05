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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
          if (!parsedWikiDocument.getFormulas().get(pos).getContent().equals(tex)) {
            System.err.println("PROBLEM WITH" + title);
            System.err.println(parsedWikiDocument.getFormulas().get(pos).getContent());
            System.err.println(tex);
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
