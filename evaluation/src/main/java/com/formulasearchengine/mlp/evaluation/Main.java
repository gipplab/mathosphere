package com.formulasearchengine.mlp.evaluation;

import com.formulasearchengine.mlp.evaluation.cli.CliParams;
import com.formulasearchengine.mlp.evaluation.pojo.ScoreSummary;

import java.io.IOException;

/**
 * Created by Leo on 25.10.2016.
 */
public class Main {
  public static void main(String[] args) throws IOException {
    CliParams params = CliParams.from(args);
    String command = params.getCommand();
    if (command == null) {
      params.printHelp();
    } else {
      switch (command) {
        case "eval":
          ScoreSummary result = (new Evaluator()).evaluate(params.getEvaluateCommand());
          System.out.println(result.toString());
          break;
        case "help":
        default:
          params.printHelp();
      }
    }
  }
}
