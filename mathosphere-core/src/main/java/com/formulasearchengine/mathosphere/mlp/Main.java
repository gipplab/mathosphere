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
                RelationExtractor.list(params.getListCommandConfig());
                break;
            case CliParams.EXTRACT:
                RelationExtractor.run(params.getExtractCommandConfig());
                break;
            case CliParams.MLP:
                FlinkMlpRelationFinder.run(params.getMlpCommandConfig());
                break;
            case CliParams.EVAL:
                FlinkMlpRelationFinder.evaluate(params.getEvalCommandConfig());
                break;
            case CliParams.ML:
                MachineLearningModelGenerator.find(params.getMachineLearningCommand());
                break;
            case CliParams.CLASSIFY:
                MachineLearningRelationClassifier.find(params.getClassifyCommand());
                break;
            case CliParams.PD:
                FlinkPd.run(params.getPdCommandConfig());
                break;
            case CliParams.HELP:
            default:
                params.printHelp();
        }
    }
}
