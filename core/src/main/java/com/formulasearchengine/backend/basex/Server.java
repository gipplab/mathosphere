package com.formulasearchengine.backend.basex;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.basex.BaseXServer;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.server.ClientSession;
import org.basex.server.Query;
import org.basex.server.Session;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by Moritz on 08.11.2014.
 */
public class Server {
	//Do not allow for multiple server instances
	private static BaseXServer server = null;
	private static Session session;
	public static Context context;
	public static boolean empty = true;

	public static boolean isEmpty () {
		return empty;
	}

	public Server() throws IOException {
		if ( server == null ){
			server = new BaseXServer();
			context = new Context() ;
			session = new ClientSession( context, "admin", "admin" );
		}
	}

	public void shutdown() throws IOException {
		server.stop();
		server = null;
		session = null;
		empty = true;
	}

	public void importData( String path ) throws IOException {
		session.execute( "SET mainmem true" );
		session.execute( "SET SERIALIZER newline=\"\\n\"" );
		session.execute( "SET SERIALIZER item-separator=\"\\n\"" );
		try {
			File f = new File( path );
			if ( f.isFile() ) {
				path = Files.toString( f, Charsets.UTF_8 );
			}
		} catch ( Exception ignored ) {	}
		CreateDB db = new CreateDB( "math", path );
		//db.execute( context );
		session.execute( db );
		empty = false;
	}

	public void runQuery( String queryString, PrintStream output ) throws IOException {
		session.setOutputStream( output );
		Query query = session.query( queryString );
		query.execute();
	}

}
