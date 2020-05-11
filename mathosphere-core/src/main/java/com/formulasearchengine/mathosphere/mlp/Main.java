package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mathpd.FlinkPd;
import com.formulasearchengine.mathosphere.mlp.cli.CliParams;
import com.formulasearchengine.mathosphere.mlp.text.TokenCounter;

/**
 * Created by Moritz on 27.09.2015.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        CliParams params = CliParams.from(args);
        String command = params.getCommand();
        switch (command) {
            case CliParams.COUNT:
                TokenCounter.run(params.getCount());
                break;
            case CliParams.LIST:
                // OLD, should be deleted or fixed for MOIs if really needed
                // it just extracts all identifiers from a document and lists them...
                RelationExtractor.list(params.getListCommandConfig());
                break;
            case CliParams.EXTRACT:
                // extract and MLP are identical!!!
                // The only difference is that MLP starts flink for multiple docs while extract
                // only presumes a single doc input.
                RelationExtractor.run(params.getExtractCommandConfig());
                break;
            case CliParams.MLP:
                // identical to EXTRACT
                FlinkMlpRelationFinder.run(params.getMlpCommandConfig());
                break;
            case CliParams.EVAL:
                // identical to EXTRACT and MLP but also takes the gold dataset as input
                // to evaluate the results (true positives, etc).
                FlinkMlpRelationFinder.evaluate(params.getEvalCommandConfig());
                break;
            case CliParams.ML:
                MachineLearningModelGenerator.find(params.getMachineLearningCommand());
                break;
            case CliParams.MLLIST:
                MachineLearningRelationExtractor.start(params.getMLListCommandConfig());
                break;
            case CliParams.CLASSIFY:
                MachineLearningRelationClassifier.find(params.getClassifyCommand());
                break;
            case CliParams.PD:
                FlinkPd.run(params.getPdCommandConfig());
                break;
            case CliParams.TAGS:
                WikiTagExtractor.run(params.getTagsCommandConfig());
                break;
            case CliParams.HELP:
            default:
                params.printHelp();
        }
    }
}
