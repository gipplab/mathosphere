package com.formulasearchengine.mathosphere.restd.rest;

import com.formulasearchengine.mathosphere.basex.Server;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import restx.tests.FindSpecsIn;
import restx.tests.RestxSpecTestsRunner;

import java.io.File;
import java.io.IOException;

@RunWith (RestxSpecTestsRunner.class)
@FindSpecsIn ("specs/base-x")
public class BaseXResourceTest {

	@BeforeClass
	public static void config() throws IOException {
		if (Server.getInstance() == null ){
			BaseXResourceTest instance = new BaseXResourceTest();
			File f = new File( instance.getClass().getClassLoader().getResource( "sampleHarvest.xml" ).getFile() );
			Server srv = Server.getInstance();
			srv.startup(f);
		}
	}
}