package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonSerializerMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.PatternMatcherMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.FileSystem.WriteMode;

public class PatternMatchingRelationFinder {

  public static void main(String[] args) throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.test();

    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents = source.flatMap(new TextExtractorMapper())
      .map(new TextAnnotatorMapper(config));

    DataSet<WikiDocumentOutput> relations = documents.map(new PatternMatcherMapper());
    relations.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);

    env.execute("Pattern Matcher Relation Finder");
  }

  public static DataSource<String> readWikiDump(FlinkMlpCommandConfig config, ExecutionEnvironment env) {
    return FlinkMlpRelationFinder.readWikiDump(config,env);
  }

}
