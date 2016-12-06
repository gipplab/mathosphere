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

    if (CliParams.HELP.equals(command)) {
      params.printHelp();
    } else if (CliParams.COUNT.equals(command)) {
      TokenCounter.run(params.getCount());
    } else if (CliParams.LIST.equals(command)) {
      RelationExtractor.list(params.getListCommandConfig());
    } else if (CliParams.EXTRACT.equals(command)) {
      RelationExtractor.run(params.getExtractCommandConfig());
    } else if (CliParams.MLP.equals(command)) {
      FlinkMlpRelationFinder.run(params.getMlpCommandConfig());
    } else if (CliParams.EVAL.equals(command)) {
      FlinkMlpRelationFinder.evaluate(params.getEvalCommandConfig());
    } else if (CliParams.PD.equals(command)) {
      FlinkPd.run(params.getPdCommandConfig());
    } else {
      params.printHelp();
    }
  }
}
