package com.formulasearchengine.backend.basex;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.basex.BaseXServer;
import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by Moritz on 08.11.2014.
 */
public class Server {
	//Do not allow for multiple server instances
	private static BaseXServer server = null;
	private static ClientSession session;

	public Server() throws IOException {
		if ( server == null ){
			server = new BaseXServer();
			session = new ClientSession( "localhost", 1984, "admin", "admin" );
		}
	}

	public void shutdown() throws IOException {
		server.stop();
		server = null;
		session = null;
	}

	public void importData( String path ) throws IOException {
		session.execute( "SET mainmem true" );
		try {
			File f = new File( path );
			if ( f.isFile() ) {
				path = Files.toString( f, Charsets.UTF_8 );
			}
		} catch ( Exception e ) {
		}
		session.execute( new CreateDB( "math", path ) );
	}

	public void runQuery( String queryString, PrintStream output ) throws IOException {
		session.setOutputStream( output );
		ClientQuery query = session.query( queryString );
		query.execute();
	}


}
