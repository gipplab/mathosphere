package mlp;

import org.apache.commons.cli.*;

/**
 * Created by Moritz on 27.09.2015.
 */
public class Main {
  public static void main(String[] args) {
    Options options = new Options();
    Option help = new Option("help", "print this message");
    //Option projecthelp = new Option( "projecthelp", "print project help information" );
    //Option version = new Option( "version", "print the version information and exit" );
    //Option quiet = new Option( "quiet", "be extra quiet" );
    //Option verbose = new Option( "verbose", "be extra verbose" );
    //Option debug = new Option( "debug", "print debugging information" );
    Option dataSource = Option.builder("d")
      .hasArg()
      .required()
      .argName("file")
      .desc("use given file for data source")
      .longOpt("datasource")
      .build();
    options.addOption(dataSource)
      .addOption(help);
    CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
      if (line.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar FILENAME.jar", options);
      } else {
        System.out.println("not implemented yet.");
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }
}
