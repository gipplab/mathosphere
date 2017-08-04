package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.TagsCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.TagExtractionMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.TextExtractorMapper;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;

public class WikiTagExtractor {


    public static void run(TagsCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final DataSource<String> dump = FlinkMlpRelationFinder.readWikiDump(config, env);
        dump
                .flatMap(new TextExtractorMapper())
                .flatMap(new TagExtractionMapper())
                .writeAsText(config.getOutputDir() + "/formulae.txt");
        env.execute();
    }
}
