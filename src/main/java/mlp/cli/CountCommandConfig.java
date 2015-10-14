package mlp.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Counts different things")
public class CountCommandConfig extends MlpCommandConfig {

  @Parameter(names = {"--formulas", "--formulae"}, arity = 0, description = "Counts how many formulas are there")
  private boolean formulas = true;

  @Parameter(names = {"--ids", "--identifiers"}, arity = 0, description = "Counts how identifiers are there in the formulas")
  private boolean identifiers = false;

  @Parameter(names = {"--defs", "--definitions"}, arity = 0, description = "Counts how definition candidates can be extracted")
  private boolean definitions = false;

  @Parameter(names = {"--csv"}, arity = 0, description = "Uses csv output format")
  private boolean csv = false;


  public boolean isFormulas() {
    return formulas;
  }

  public boolean isIdentifiers() {
    return identifiers;
  }

  public boolean isDefinitions() {
    return definitions;
  }

  public boolean isCsv() {
    return csv;
  }
}
