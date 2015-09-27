package mlp.cli;

import java.io.Serializable;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Counts different things")
public class CountCommandConfig implements Serializable {

  @Parameter(names = { "--formulas", "--formulae" }, arity = 0, description = "Counts how many formulas are there")
  private boolean formulas = true;

  @Parameter(names = { "--ids", "--identifiers" }, arity = 0, description = "Counts how identifiers are there in the formulas")
  private boolean identifiers = false;

  @Parameter(names = { "--defs", "--definitions" }, arity = 0, description = "Counts how definition candidates can be extracted")
  private boolean definitions = false;

  @Parameter(names = { "-in", "--inputDir" }, description = "path to the directory with wikidump")
  private String dataset;

  public boolean isFormulas() {
    return formulas;
  }

  public boolean isIdentifiers() {
    return identifiers;
  }

  public boolean isDefinitions() {
    return definitions;
  }

  public String getDataset() {
    return dataset;
  }

}
