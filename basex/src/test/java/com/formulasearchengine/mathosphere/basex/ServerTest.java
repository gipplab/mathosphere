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

}