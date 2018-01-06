package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienListConfig;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A combination of the {@link RelationExtractor} and {@link MachineLearningModelGenerator}.
 * The idea is to provide a program that can extract and list all identifier-definien
 * pairs without using a gold standard for comparison.
 *
 * @author Andre Greiner-Petter
 */
public class MachineLearningRelationExtractor {

    private static final Logger LOG = LogManager.getLogger( MachineLearningRelationExtractor.class.getName() );

    public static void start( MachineLearningDefinienListConfig config ){
        LOG.info("Start machine learning approach for listing identifier-definien pairs");

        // first, create a flink environment
        ExecutionEnvironment flinkEnv = ExecutionEnvironment.getExecutionEnvironment();
        flinkEnv.setParallelism( config.getParallelism() );

        LOG.debug("Read wikidump via flink");
        DataSource<String> dataSource = FlinkMlpRelationFinder.readWikiDump( config, flinkEnv );
    }
}
