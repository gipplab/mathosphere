package com.formulasearchengine.mathosphere.basex;

import com.google.common.base.Charsets;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.query.QueryException;
import org.jetbrains.annotations.NotNull;

import javax.xml.xquery.XQException;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * Singleton server for handling BaseX queries.
 * Created by Moritz on 08.11.2014.
 */
public final class Server {
	private static final Pattern DB_LOCK_ERR_MATCH = Pattern.compile(
			".*Database.*is currently opened by another process.*", Pattern.DOTALL);
	private static final Pattern DB_DROPPED_ERR_MATCH = Pattern.compile(".*Database.*was not found.*");
	private static Server serverInstance;
	private File file;
	public BaseXServer baseXServer;
	private Timer healthTimer;
	public final String SERVER_NAME = "localhost";
	public final String PORT = "1984";
	public final String DATABASE_NAME = "math";
	public final String USER = "admin";
	public final String PASSWORD = System.getProperty("password") != null ? System.getProperty("password") : "admin";

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
		baseXServer = new BaseXServer("-p" + PORT); // "-d" for debug
		file = input;
		final Charset charset = Charsets.UTF_8;
		final StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
			String line = fileReader.readLine();
			while (line != null) {
				stringBuilder.append(line);
				stringBuilder.append(System.getProperty("line.separator"));
				line = fileReader.readLine();
			}
		}

		final CreateDB db = new CreateDB(DATABASE_NAME, stringBuilder.toString());
		db.execute(baseXServer.context);
		System.out.println("Import completed. Start Monitoring.");
		healthTimer = new Timer();

		healthTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkHealth();
			}
		}, 0, 30 * 1000);
	}

	/**
	 * Checks the health of the server to see if it is still running.
	 *
	 * @return True if running and responsive to queries, false otherwise.
	 */
	public boolean checkHealth() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final Date date = new Date();
		System.out.println("Check health at " + dateFormat.format(date));
		if (baseXServer != null) {
			Client c = new Client();
			try {
				c.directXQuery("count(./*/*)");
				System.out.println("Server is healthy!");
			} catch (XQException e) {
				e.printStackTrace();
				System.out.println("Server crashed or is in the process of shutting down!");
				return false;
			}
			return true;
		} else {
			System.out.println("Server is not running");
			return false;
		}
	}

	/**
	 * Shuts down the server.
	 *
	 * @throws IOException Thrown if server fails to shutdown.
	 */
	public void shutdown() throws IOException {
		if (baseXServer != null) {
			System.out.println("Shutting down");
			healthTimer.cancel();
			//Somewhat nasty but BaseX does not have a database lock checking function as far as I can tell
			while (true) {
				try {
					//Make sure this does not hog cpu
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
				try {
					final DropDB db = new DropDB(DATABASE_NAME);
					db.execute(baseXServer.context);
					break;
				} catch (final BaseXException e) {
					final String msg = e.toString();
					if (DB_LOCK_ERR_MATCH.matcher(msg).matches()) {
						//DB locked, waiting
						System.out.println("Waiting to acquire database lock...");
					} else if (DB_DROPPED_ERR_MATCH.matcher(msg).matches()) {
						//DB already dropped
						break;
					} else {
						throw e;
					}
				}
			}
			System.out.println("Database dropped or server thread interrupted.");
			baseXServer.stop();
		}
		ensureFileLockRelease();
		baseXServer = null;
	}

	private void ensureFileLockRelease() {
		if (file != null) {
			System.out.println("Trying to ensure release of database source file.");
			try (RandomAccessFile dummyFile = new RandomAccessFile(file, "rw");
				 FileChannel channel = dummyFile.getChannel()) {
				final FileLock lock = channel.lock();
				System.out.println("File locked");
				// make sure that org.basex.io.random.TableDiskAccess.locked will find an unlocked file
				lock.release();
				System.out.println("File unlocked.");
			} catch (final FileNotFoundException e) {
				System.out.println("File is gone or is a directory.");
				e.printStackTrace();
			} catch (final IOException e) {
				System.out.println("File could not be unlocked");
				e.printStackTrace();
			}
		}
	}
}
