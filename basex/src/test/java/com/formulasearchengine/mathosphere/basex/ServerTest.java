package com.formulasearchengine.mathosphere.basex;

import net.xqj.basex.BaseXXQDataSource;
import org.basex.api.client.ClientSession;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

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
		srv.startup(file);
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

    @Test
    public void testXQConnection() throws Exception {
        final Server srv = Server.getInstance();
        final URL fname = BaseXTestSuite.class.getClassLoader().getResource( "sampleHarvest.xml" );
		File file = new File(fname.toURI());
        srv.startup(file);
		final XQDataSource xqs = new BaseXXQDataSource();
		xqs.setProperty("serverName", srv.SERVER_NAME);
		xqs.setProperty("port", srv.PORT);
		xqs.setProperty("databaseName", srv.DATABASE_NAME);
		xqs.setProperty("user", Client.USER);
		xqs.setProperty("password", Client.PASSWORD);

		final XQConnection conn = xqs.getConnection();
		try {
			final String query = "declare default element namespace \"http://www.w3.org/1998/Math/MathML\";\n" +
					"for $m in //*:expr return \n" +
					"for $x in $m//*:apply\n" +
					"[*[1]/name() = 'divide']\n" +
					"where\n" +
					"fn:count($x/*) = 3\n" +
					"return\n" +
					"<result>{$m/@url}</result>";

			final XQPreparedExpression xqpe = conn.prepareExpression( query );
			final XQResultSequence rs = xqpe.executeQuery();

			final String res = rs.getSequenceAsString( null );

			assertEquals( "<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"4#math.4.5\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"4#math.4.5\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.2\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.17\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.18\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.18\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.19\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.19\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.19\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.20\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.21\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.22\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"5#math.5.23\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.11\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.14\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.15\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"6#math.6.20\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.0\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.1\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.1\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.2\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.2\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.3\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.3\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"7#math.7.4\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.6\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.7\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.21\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.22\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.23\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.33\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.34\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"8#math.8.35\"/> " +
					"<result xmlns=\"http://www.w3.org/1998/Math/MathML\" url=\"dummy29\"/>"
					, res );
		} finally {
			conn.close();
		}
    }

}