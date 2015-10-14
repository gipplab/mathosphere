package mlp.cli;

import java.io.Serializable;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

public class CliParams implements Serializable {

  @Parameters(commandDescription = "Prints this help message")
  static private class HelpCommand {
  }

  private JCommander jc;

  private CountCommandConfig count;
  private ListCommandConfig list;
  private MlpCommandConfig mlp;
  private FlinkMlpCommandConfig flinkMlp;

  private String command;

  private CliParams() {
  }

  public static CliParams from(String[] args) {
    JCommander jc = new JCommander();

    CliParams params = new CliParams();
    params.count = new CountCommandConfig();
    params.list = new ListCommandConfig();
    params.flinkMlp = new FlinkMlpCommandConfig();
    params.mlp = new MlpCommandConfig();

    jc.addCommand("count", params.count);
    jc.addCommand("list", params.list);
    jc.addCommand("extract", params.mlp);
    jc.addCommand("mlp", params.flinkMlp);
    jc.addCommand("help", new HelpCommand());

    jc.parse(args);

    params.command = jc.getParsedCommand();
    params.jc = jc;
    return params;
  }

  public void printHelp() {
    jc.usage();
  }

  public String getCommand() {
    return command;
  }

  public CountCommandConfig getCount() {
    return count;
  }

  public ListCommandConfig getList() {
    return list;
  }

  public FlinkMlpCommandConfig getFlinkMlp() {
    return flinkMlp;
  }

  public MlpCommandConfig getMlp() {
    return mlp;
  }
}
