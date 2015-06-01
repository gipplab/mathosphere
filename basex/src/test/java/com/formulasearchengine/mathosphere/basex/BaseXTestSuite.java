package com.formulasearchengine.mathosphere.basex;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.net.URL;

/**
 * Created by mas9 on 3/31/15.
 */
@RunWith( Suite.class )
@Suite.SuiteClasses({ServerTest.class,ClientTest.class,BenchmarkTest.class,
                     TexQueryGeneratorTest.class,XMLHelperTest.class})
public class BaseXTestSuite {
	@BeforeClass
	public static void setup() throws Exception {
        final URL fname = BaseXTestSuite.class.getClassLoader().getResource( "sampleHarvest.xml" );
        File file = new File( fname.toURI() );
        Server srv = Server.getInstance();
        srv.startup(file);
	}
}
