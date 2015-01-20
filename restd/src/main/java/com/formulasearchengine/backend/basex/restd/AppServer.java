package com.formulasearchengine.backend.basex.restd;

import com.formulasearchengine.backend.basex.Server;
import com.google.common.base.Optional;
import org.apache.commons.cli.*;
import restx.server.JettyWebServer;
import restx.server.WebServer;

/**
 * This class can be used to run the app.
 *
 * Alternatively, you can deploy the app as a war in a regular container like tomcat or jetty.
 *
 * Reading the port from system env PORT makes it compatible with heroku.
 */
public class AppServer {
    public static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    public static final String WEB_APP_LOCATION = "src/main/webapp";
    public static String adminPassword = "2015";
    public static String dataPath;
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option help = new Option( "help", "print this message" );
        Option dataSource   = OptionBuilder.withArgName( "file" )
            .hasArg()
            .isRequired()
            .withDescription( "use given file for data source" )
            .withLongOpt( "datasource" )
            .create( "d" );
        options
            .addOption( dataSource )
            .addOption( help )
            .addOption( "p","password",true,"The admin password." );
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse( options, args );
            if (line.hasOption( "help" )){
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "java -jar FILENAME.jar", options );
            } else {
                if ( line.hasOption( "p" ) ){
                    adminPassword = line.getOptionValue( "p" );
                }
                dataPath = line.getOptionValue( "d" );
                Server srv = new Server();
                srv.importData( AppServer.dataPath );
            }

        } catch( ParseException exp ) {
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            return;
        }
        int port = Integer.valueOf(Optional.fromNullable(System.getenv("PORT")).or("10043"));
        WebServer server = new JettyWebServer(WEB_INF_LOCATION, WEB_APP_LOCATION, port, "0.0.0.0");

        /*
         * load mode from system property if defined, or default to dev
         * be careful with that setting, if you use this class to launch your server in production, make sure to launch
         * it with -Drestx.mode=prod or change the default here
         */
        System.setProperty("restx.mode", System.getProperty("restx.mode", "dev"));
        System.setProperty("restx.app.package", "com.formulasearchengine.backend.basex");
        server.startAndAwait();

    }
}
