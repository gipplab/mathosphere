package com.formulasearchengine.mathosphere.basex;

import org.basex.api.client.ClientSession;
import org.basex.core.cmd.DropDB;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class ServerTest  {
    @BeforeClass
    public static void setServerModeProd() {
        System.setProperty("restx.mode", "prod");
    }

    @After
    public void shutdownServer() throws IOException {
        if (Server.getInstance() != null) {
            Server.getInstance().shutdown();
        }
    }

	@Test
	public void restartServerTest() throws Exception {
		final URL fname = BaseXTestSuite.class.getClassLoader().getResource( "sampleHarvest.xml" );
		File file = new File(fname.toURI());
		Server srv = Server.getInstance();
        srv.startup( file );
		srv.shutdown();
		srv.startup( file );
        srv.shutdown();
		srv.startup( file );
	}

	@Test
	public void testImportData() throws Exception {
        Server srv = Server.getInstance();
        final URL fname = BaseXTestSuite.class.getClassLoader().getResource( "sampleHarvest.xml" );
		File file = new File(fname.toURI());
        srv.startup(file);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
        ClientSession session = new ClientSession(srv.baseXServer.context, "admin", "admin");

        session.execute("SET mainmem true");
        //session.execute( "SET DEBUG true" );
        session.execute("SET SERIALIZER newline=\"\\n\"");
        session.execute("SET SERIALIZER item-separator=\"\\n\"");
        session.execute("OPEN math");
        session.setOutputStream(baos);
        session.query("count(./*/*)").execute();
		assertEquals("104",baos.toString("UTF-8"));
        session.execute("CLOSE");
        session.close();
	}

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /*
	@Test
	public void testImportDataFolder() throws Exception {
        final URL fname1 = BaseXTestSuite.class.getClassLoader().getResource("sampleHarvest.xml");
        final URL fname2 = BaseXTestSuite.class.getClassLoader().getResource("mws.xml");
        final Path path1 = new File(fname1.toURI()).toPath();
        final Path path2 = new File(fname2.toURI()).toPath();

        //copy test files to temp folder
        Files.copy(path1, folder.getRoot().toPath().resolve(path1.getFileName()));
        Files.copy(path2, folder.getRoot().toPath().resolve(path2.getFileName()));

        final Server srv = Server.getInstance();
        srv.startup(folder.getRoot());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(baos);
        final ClientSession session = new ClientSession(srv.baseXServer.context, "admin", "admin");
        session.execute("SET mainmem true");
        //session.execute( "SET DEBUG true" );
        session.execute("SET SERIALIZER newline=\"\\n\"");
        session.execute("SET SERIALIZER item-separator=\"\\n\"");
        session.execute("OPEN" + srv.DATABASE_NAME);
        session.setOutputStream(baos);
*/
 //       session.query("count(./*/*)").execute();
/*		assertEquals("104",baos.toString("UTF-8"));
        session.execute("CLOSE");
        session.close();
	}*/
}