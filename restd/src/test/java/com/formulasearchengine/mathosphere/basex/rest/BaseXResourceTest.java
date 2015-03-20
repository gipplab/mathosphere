package com.formulasearchengine.mathosphere.basex.rest;

import com.formulasearchengine.mathosphere.basex.Server;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import restx.tests.FindSpecsIn;
import restx.tests.RestxSpecTestsRunner;

import java.io.IOException;

@RunWith (RestxSpecTestsRunner.class)
@FindSpecsIn ("specs/base-x")
public class BaseXResourceTest {

	@BeforeClass
	public static void config() throws IOException {
		BaseXResourceTest instance = new BaseXResourceTest();
		Server srv = new Server();
		srv.importData( instance.getClass().getClassLoader().getResource( "sampleHarvest.xml" ).getFile()   );
	}

}