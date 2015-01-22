package com.formulasearchengine.backend.basex.restd;

import com.formulasearchengine.backend.basex.Server;
import org.restexpress.RestExpress;
import org.restexpress.util.Environment;

import java.io.IOException;


public class Main {

	public static void main (String[] args) {
		//RestExpress.setSerializationProvider(new SerializationProvider());
		Configuration config;
		try {
		if ( args.length > 0 ) {
			config =  Environment.from( args[0], Configuration.class );
		} else {

				config = Environment.fromDefault( Configuration.class );
		}
		} catch ( IOException e ) {
			e.printStackTrace();
			return;
		}
		RestExpress server = new RestExpress()
			.setName( "BaseX Query" )
			.setPort( config.getPort() );
		Server srv = null;
		try {
			srv = new Server();
			srv.importData( config.getPath() );
		} catch ( IOException e ) {
			e.printStackTrace();
		}



		server.uri( "/xquery", config.getBasexController() ).noSerialization();

		server.bind();
		server.awaitShutdown();
	}

}
