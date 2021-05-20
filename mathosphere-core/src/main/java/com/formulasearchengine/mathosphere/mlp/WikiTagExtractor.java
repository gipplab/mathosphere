package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.TagsCommandConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.JsonArrayOutputFormat;
import com.formulasearchengine.mathosphere.mlp.contracts.TagExtractionMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextPageExtractorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.core.fs.Path;

public class WikiTagExtractor {
    public static void run(TagsCommandConfig config) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final DataSource<String> dump = FlinkMlpRelationFinder.readWikiDump(config, env);
        dump
                .flatMap(new WikiTextPageExtractorMapper())
                .flatMap(new TagExtractionMapper(config))
                .distinct(MathTag::getContentHash)
                .map(MathTag::toJson)
                .output(new JsonArrayOutputFormat(new Path(config.getOutputDir() + "/formulae.json")))
                .setParallelism(1);
        env.execute();
    }
}
