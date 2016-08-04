package com.formulasearchengine.mathosphere.basex;

import java.io.File;
import java.io.IOException;

import org.basex.BaseXServer;
import org.jetbrains.annotations.NotNull;

/**
 * Singleton server for handling BaseX queries.
 * Created by Moritz on 08.11.2014.
 */
public final class Server {
	private static Server serverInstance;
	public BaseXServer baseXServer;
	public static final String SERVER_NAME = "localhost";
	public static final int PORT = 1984;
	public static final String DATABASE_NAME = "math";

	private Server() {
	}

	/**
	 * @return The only instance of Server.
	 */
	@NotNull public static Server getInstance() {
		synchronized (Server.class) {
			if (serverInstance == null) {
				serverInstance = new Server();
			}
		}
		return serverInstance;
	}

	/**
	 * Shuts down the server if it is already running, and starts it with the specified the data file.
	 * Schedules the monitor task as well.
	 *
	 * @param input The data file or directory to use.
	 * @throws IOException Thrown if it fails to read input
	 */
	public void startup(@NotNull File input) throws IOException {
		shutdown();
		
		/* [CG] If a client is used (as I initially) proposed, the database will get lost
     * once the client connection is closed. So we’ll have (at least) 2 options here:
     *
     * - Create a client, set MAINMEM to true and create database only close it if server is closed
     * - Create main-memory database at startup (it will then be bound to the server process).
		 *
		 * I went for the second option... */

		// "-d" for debug
		baseXServer = new BaseXServer( "-p" + PORT, "-n" + SERVER_NAME,
		    "-c " + "set mainmem on;set intparse on;create db " + DATABASE_NAME + " " + input.getAbsolutePath());

		/* [CG] I dropped all health checks. If something should be going wrong here, please give me a note;
		 * it should definitely be fixed! */

		System.out.println("Import completed.");
	}

	/**
	 * Shuts down the server.
	 *
	 * @throws IOException Thrown if server fails to shutdown.
	 */
	public void shutdown() throws IOException {
		if (baseXServer != null) {
		  baseXServer.stop();
	    baseXServer = null;
		}
	}
}
