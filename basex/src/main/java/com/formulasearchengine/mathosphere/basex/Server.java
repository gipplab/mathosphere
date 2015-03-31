package com.formulasearchengine.mathosphere.basex;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.basex.BaseXServer;
import org.basex.api.client.ClientQuery;
import org.basex.api.client.ClientSession;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by Moritz on 08.11.2014.
 */
public class Server {
	private File file;
	@Nullable
	private static volatile Server instance;

	/**
	 * Get the only instance of this class.
	 *
	 * @return the single instance.
	 */
	public static Server getInstance( File f ) throws IOException {
		synchronized ( Server.class ) {
			if ( instance != null ) {
				instance.shutdown();
			}
			instance = new Server( f );
			return instance;
		}
	}

	@Nullable
	public static Server getInstance() {
		synchronized ( Server.class ) {
			return instance;
		}
	}

	//Do not allow for multiple server instances
	@Nullable
	public BaseXServer server = null;
	@Nullable
	public ClientSession session;
	ServerMonitor monitor;


	public Server(File input) throws IOException {
		startup( );
		file = input;
		importDataFromFile( );
	}

	private void startup() throws IOException {
		server = new BaseXServer( ); // "-d" for debug
		session = new ClientSession( server.context, "admin", "admin" );
	}

	public void shutdown() throws IOException {
		if (monitor !=null ){
			monitor.shutdown();
		}
		if (session !=null){
			session.execute( "CLOSE" );
			DropDB db = new DropDB( "math" );
			db.execute( server.context );
			session.close();
		}
		if (server != null){
			server.stop();
		}
		server = null;
		session = null;
		instance = null;
		ensureFileLockRelease();
		System.gc();
	}

	private void ensureFileLockRelease() {
		System.out.println( "Server could not be started. Try to lock database source file.");
		FileChannel channel = null;
		try {
			channel = new RandomAccessFile( file, "rw" ).getChannel();
		} catch ( FileNotFoundException e ) {
			System.out.println( "File is gone.");
			e.printStackTrace();
			return;
		}
		FileLock lock = null;
		try {
			lock = channel.lock();
			System.out.println("File locked");
			// make sure that org.basex.io.random.TableDiskAccess.locked will find an unlocked file
			lock.release();
			System.out.println( "File unlocked. Restarting the server." );
		} catch ( IOException e ) {
			System.out.println("File could not be unlocked");
			e.printStackTrace();
		}
	}

	private boolean importDataFromFile( ) throws IOException {
		session.execute( "SET mainmem true" );
		//session.execute( "SET DEBUG true" );
		session.execute( "SET SERIALIZER newline=\"\\n\"" );
		session.execute( "SET SERIALIZER item-separator=\"\\n\"" );
		String fname = Files.toString( file, Charsets.UTF_8 );
		CreateDB db = new CreateDB( "math", fname );
		db.execute( server.context );
		db=null;
		System.out.println( "Import completed. Start Monitoring.");
		session.execute( "OPEN math" );
		monitor = new ServerMonitor();
		monitor.start();
		return true;
	}

	public void runQuery( String queryString, PrintStream output ) throws IOException {
		session.setOutputStream( output );
		ClientQuery query = session.query( queryString );
		query.execute();
	}

}
