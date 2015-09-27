package mlp;

import org.apache.commons.lang3.NotImplementedException;

import mlp.cli.CliParams;


/**
 * Created by Moritz on 27.09.2015.
 */
public class Main {

  public static void main(String[] args) throws Exception {
    CliParams params = CliParams.from(args);
    String command = params.getCommand();

    if ("help".equals(command)) {
      params.printHelp();
    } else if ("count".equals(command)) {
      throw new NotImplementedException("The 'count' command is not yet implemented");
    } else if ("list".equals(command)) {
      throw new NotImplementedException("The 'list' command is not yet implemented");
    } else if ("extract".equals(command)) {
      RelationExtractor.run(params.getMlp());
    } else if ("mlp".equals(command)) {
      FlinkMlpRelationFinder.run(params.getFlinkMlp());
    } else {
      params.printHelp();
    }
  }
}
