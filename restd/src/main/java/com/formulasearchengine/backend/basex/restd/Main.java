package com.formulasearchengine.backend.basex.restd;

import com.formulasearchengine.backend.basex.Server;
import com.formulasearchengine.backend.basex.restd.rest.BasexController;
import com.formulasearchengine.backend.basex.restd.rest.MWSController;
import org.restexpress.RestExpress;
import org.restexpress.util.Environment;

import java.io.IOException;


public class Main {

	public static void main( String[] args ) {
		//RestExpress.setSerializationProvider(new SerializationProvider());
		Configuration config;
		try {
			if ( args.length > 0 ) {
				config = Environment.from( args[ 0 ], Configuration.class );
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
			System.out.println( "importing data from " + config.getPath() );
			srv.importData( config.getPath() );
		} catch ( IOException e ) {
			e.printStackTrace();
		}


		server.uri( "/xquery", new BasexController() ).noSerialization();
		server.uri( "/mwsquery", new MWSController() ).noSerialization();
		server.bind();
		server.awaitShutdown();
	}

}
