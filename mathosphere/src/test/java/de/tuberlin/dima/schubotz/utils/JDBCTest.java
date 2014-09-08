package de.tuberlin.dima.schubotz.utils;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static de.tuberlin.dima.schubotz.utils.TestUtils.getFileContents;
import static org.apache.flink.api.java.typeutils.BasicTypeInfo.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Moritz on 07.09.2014.
 */
@RunWith(JUnit4.class)
public class JDBCTest {
	private final static String DBURL="jdbc:mysql://localhost:3306/test";
	public static final String DRIVERNAME = "org.mariadb.jdbc.Driver";
	public static final String USER = "test";
	public static String PASSWORD ;


    @Before
    public void beforeMethod() {
        if (this.getClass().getClassLoader().getResource( "testpassword") == null){
            Assume.assumeTrue( false );
        }
        try {
            PASSWORD = getFileContents( "testpassword" );
        } catch ( IOException e ) {
            e.printStackTrace();

        }
    }


@Test
	public void testMaria() throws Exception{
		Connection  connection = DriverManager.getConnection(DBURL, USER, PASSWORD );
		Statement stmt = connection.createStatement();
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS  a (id int not null primary key, value varchar(20))");
		stmt.close();
		connection.close();
	}
    @Test
	public void testMain() throws Exception {
		prepareTestDb();
		ExecutionEnvironment environment = ExecutionEnvironment.getExecutionEnvironment();
		DataSet<Tuple5> source
			= environment.createInput( JDBCInputFormat.buildJDBCInputFormat()
				.setDrivername( DRIVERNAME )
				.setDBUrl( DBURL )
				.setUsername( USER )
				.setPassword( PASSWORD )
				.setQuery( "select * from books" )
				.finish(),
			new TupleTypeInfo( Tuple5.class, INT_TYPE_INFO, STRING_TYPE_INFO, STRING_TYPE_INFO, FLOAT_TYPE_INFO, INT_TYPE_INFO )
		);
		source.output( JDBCOutputFormat.buildJDBCOutputFormat()
			.setDrivername( DRIVERNAME )
			.setDBUrl( DBURL )
			.setUsername( USER )
			.setPassword( PASSWORD )
			.setQuery( "insert ignore into newbooks (id,title,author,price,qty) values (?,?,?,?,?)" )
			.finish() );
		environment.execute();
	}
	private static void prepareTestDb() throws Exception {

		Class.forName( DRIVERNAME );
		Connection conn = DriverManager.getConnection(DBURL,USER, PASSWORD );
		StringBuilder sqlQueryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS books (");
		sqlQueryBuilder.append("id INT NOT NULL DEFAULT 0,");
		sqlQueryBuilder.append("title VARCHAR(50) DEFAULT NULL,");
		sqlQueryBuilder.append("author VARCHAR(50) DEFAULT NULL,");
		sqlQueryBuilder.append("price FLOAT DEFAULT NULL,");
		sqlQueryBuilder.append("qty INT DEFAULT NULL,");
		sqlQueryBuilder.append("PRIMARY KEY (id))");
		Statement stat = conn.createStatement();
		stat.executeUpdate(sqlQueryBuilder.toString());
		stat.close();
		sqlQueryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS newbooks (");
		sqlQueryBuilder.append("id INT NOT NULL DEFAULT 0,");
		sqlQueryBuilder.append("title VARCHAR(50) DEFAULT NULL,");
		sqlQueryBuilder.append("author VARCHAR(50) DEFAULT NULL,");
		sqlQueryBuilder.append("price FLOAT DEFAULT NULL,");
		sqlQueryBuilder.append("qty INT DEFAULT NULL,");
		sqlQueryBuilder.append("PRIMARY KEY (id))");
		stat = conn.createStatement();
		stat.executeUpdate(sqlQueryBuilder.toString());
		stat.close();
		sqlQueryBuilder = new StringBuilder("INSERT IGNORE INTO books (id, title, author, price, qty) VALUES ");
		sqlQueryBuilder.append("(1001, 'Java for dummies', 'Tan Ah Teck', 11.11, 11),");
		sqlQueryBuilder.append("(1002, 'More Java for dummies', 'Tan Ah Teck', 22.22, 22),");
		sqlQueryBuilder.append("(1003, 'More Java for more dummies', 'Mohammad Ali', 33.33, 33),");
		sqlQueryBuilder.append("(1004, 'A Cup of Java', 'Kumar', 44.44, 44),");
		sqlQueryBuilder.append("(1005, 'A Teaspoon of Java', 'Kevin Jones', 55.55, 55)");
		stat = conn.createStatement();
		stat.execute(sqlQueryBuilder.toString());
		stat.close();
		conn.close();
	}
    @Test
    public void testSize(){
        String sql = "UPDATE formulae_name set isEquation = ? WHERE pageId = ? and formula_name = ? LIMIT 1";
         assertEquals(1,sql.split("\\?,").length);
    }
}
