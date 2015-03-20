package com.formulasearchengine.mathosphere.basex;

import com.google.common.base.Optional;
import restx.server.JettyWebServer;
import restx.server.WebServer;

import java.io.IOException;

/**
 * This class can be used to run the app.
 * <p/>
 * Alternatively, you can deploy the app as a war in a regular container like tomcat or jetty.
 * <p/>
 * Reading the port from system env PORT makes it compatible with heroku.
 */
public class AppServer {
	public static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
	public static final String WEB_APP_LOCATION = "src/main/webapp";

	public static void main( String[] args ) throws Exception {
		int port = Integer.valueOf( Optional.fromNullable( System.getenv( "PORT" ) ).or( "10043" ) );
		WebServer server = new JettyWebServer( WEB_INF_LOCATION, WEB_APP_LOCATION, port, "0.0.0.0" );

        /*
         * load mode from system property if defined, or default to dev
         * be careful with that setting, if you use this class to launch your server in production, make sure to launch
         * it with -Drestx.mode=prod or change the default here
         */
		System.setProperty( "restx.mode", System.getProperty( "restx.mode", "dev" ) );
		System.setProperty( "restx.app.package", "com.formulasearchengine.backend.basex" );
		String path = System.getProperty( "path" );
		Server srv = null;
		System.out.println("admin password is " + System.getProperty( "password" ));
		try {
			srv = new Server();
			System.out.println( "importing data from " + path );
			srv.importData( path );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		server.startAndAwait();
	}
}
